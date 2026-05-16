package com.zhaoxinms.resi.meter.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.archive.entity.ResiMeterDevice;
import com.zhaoxinms.resi.archive.entity.ResiRoom;
import com.zhaoxinms.resi.archive.service.IResiMeterDeviceService;
import com.zhaoxinms.resi.archive.service.IResiRoomService;
import com.zhaoxinms.resi.common.ResiConstants;
import com.zhaoxinms.resi.meter.dto.ResiMeterShareCalcReq;
import com.zhaoxinms.resi.meter.dto.ResiMeterShareCalcResultVo;
import com.zhaoxinms.resi.meter.dto.ResiMeterShareDetailVo;
import com.zhaoxinms.resi.meter.entity.ResiMeterReading;
import com.zhaoxinms.resi.meter.mapper.ResiMeterReadingMapper;
import com.zhaoxinms.resi.meter.service.IResiMeterReadingService;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;
import com.zhaoxinms.resi.receivable.service.IResiReceivableService;

/**
 * 抄表记录 Service实现
 *
 * @author zhaoxinms
 */
@Service
public class ResiMeterReadingServiceImpl extends ServiceImpl<ResiMeterReadingMapper, ResiMeterReading>
        implements IResiMeterReadingService {

    @Autowired
    private IResiMeterDeviceService meterDeviceService;

    @Autowired
    private IResiRoomService roomService;

    @Autowired
    private IResiReceivableService receivableService;

    @Override
    public List<ResiMeterReading> selectResiMeterReadingList(ResiMeterReading reading) {
        QueryWrapper<ResiMeterReading> queryWrapper = new QueryWrapper<>();

        if (reading.getProjectId() != null) {
            queryWrapper.eq("project_id", reading.getProjectId());
        }

        if (reading.getMeterId() != null) {
            queryWrapper.eq("meter_id", reading.getMeterId());
        }

        if (reading.getRoomId() != null) {
            queryWrapper.eq("room_id", reading.getRoomId());
        }

        if (StringUtils.isNotBlank(reading.getPeriod())) {
            queryWrapper.eq("period", reading.getPeriod());
        }

        if (StringUtils.isNotBlank(reading.getStatus())) {
            queryWrapper.eq("status", reading.getStatus());
        }

        queryWrapper.orderByDesc("period").orderByDesc("creator_time");

        List<ResiMeterReading> list = baseMapper.selectList(queryWrapper);

        // 补充仪表编号、公摊组等信息
        for (ResiMeterReading item : list) {
            if (item.getMeterId() != null) {
                ResiMeterDevice device = meterDeviceService.getById(item.getMeterId());
                if (device != null) {
                    item.setMeterCode(device.getMeterCode());
                    item.setIsPublic(device.getIsPublic());
                    item.setPublicGroup(device.getPublicGroup());
                    item.setMultiplier(device.getMultiplier());
                }
            }
            if (item.getRoomId() != null) {
                ResiRoom room = roomService.getById(item.getRoomId());
                if (room != null) {
                    item.setRoomName(room.getRoomAlias());
                }
            }
        }

        return list;
    }

    @Override
    public boolean save(ResiMeterReading entity) {
        // 自动填充创建信息
        Date now = new Date();
        entity.setCreatorTime(now);
        entity.setCreatorUserId(String.valueOf(SecurityUtils.getUserId()));
        entity.setLastModifyTime(now);
        entity.setLastModifyUserId(String.valueOf(SecurityUtils.getUserId()));

        // 默认状态
        if (StringUtils.isBlank(entity.getStatus())) {
            entity.setStatus("INPUT");
        }

        // 默认损耗率
        if (entity.getLossRate() == null) {
            entity.setLossRate(BigDecimal.ZERO);
        }

        // 自动带入上期读数
        fillLastReading(entity);

        // 自动计算用量
        calculateUsage(entity);

        return super.save(entity);
    }

    @Override
    public boolean updateById(ResiMeterReading entity) {
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(String.valueOf(SecurityUtils.getUserId()));

        // 重新计算用量
        calculateUsage(entity);

        return super.updateById(entity);
    }

    @Override
    public boolean removeByIds(java.util.Collection<? extends java.io.Serializable> idList) {
        for (java.io.Serializable id : idList) {
            ResiMeterReading entity = new ResiMeterReading();
            entity.setId((String) id);
            baseMapper.deleteById(entity);
        }
        return true;
    }

    /**
     * 自动带入上期读数
     */
    private void fillLastReading(ResiMeterReading entity) {
        if (entity.getMeterId() == null) {
            return;
        }

        // 如果用户已手动填写上期读数，不再覆盖
        if (entity.getLastReading() != null) {
            return;
        }

        // 查询上期记录
        ResiMeterReading lastRecord = baseMapper.selectLastRecord(entity.getMeterId());
        if (lastRecord != null) {
            entity.setLastReading(lastRecord.getCurrReading());
            entity.setLastDate(lastRecord.getCurrDate());
        } else {
            // 无上期记录，使用仪表初始读数
            ResiMeterDevice device = meterDeviceService.getById(entity.getMeterId());
            if (device != null && device.getInitReading() != null) {
                entity.setLastReading(device.getInitReading());
            } else {
                entity.setLastReading(BigDecimal.ZERO);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ResiMeterShareCalcResultVo> calcShare(ResiMeterShareCalcReq req) {
        List<ResiMeterShareCalcResultVo> results = doCalcShare(req, true);
        return results;
    }

    @Override
    public List<ResiMeterShareCalcResultVo> previewShare(ResiMeterShareCalcReq req) {
        return doCalcShare(req, false);
    }

    /**
     * 公摊计算核心逻辑
     *
     * @param req      请求参数
     * @param persist  是否持久化到数据库
     * @return 公摊计算结果列表
     */
    private List<ResiMeterShareCalcResultVo> doCalcShare(ResiMeterShareCalcReq req, boolean persist) {
        List<ResiMeterReading> readings;
        if (StringUtils.isNotBlank(req.getPublicGroup())) {
            readings = baseMapper.selectShareGroupReadings(req.getProjectId(), req.getPeriod(), req.getPublicGroup());
        } else {
            readings = baseMapper.selectAllShareGroupReadings(req.getProjectId(), req.getPeriod());
        }

        if (readings == null || readings.isEmpty()) {
            return new ArrayList<>();
        }

        // 按公摊组分组
        Map<String, List<ResiMeterReading>> groupMap = new LinkedHashMap<>();
        for (ResiMeterReading reading : readings) {
            String group = reading.getPublicGroup();
            if (StringUtils.isBlank(group)) {
                continue;
            }
            groupMap.computeIfAbsent(group, k -> new ArrayList<>()).add(reading);
        }

        List<ResiMeterShareCalcResultVo> results = new ArrayList<>();
        List<ResiMeterReading> updates = new ArrayList<>();

        for (Map.Entry<String, List<ResiMeterReading>> entry : groupMap.entrySet()) {
            String publicGroup = entry.getKey();
            List<ResiMeterReading> groupReadings = entry.getValue();

            ResiMeterShareCalcResultVo result = calcGroupShare(publicGroup, groupReadings, persist);
            if (result != null) {
                results.add(result);
                if (persist && result.getDetails() != null) {
                    for (ResiMeterShareDetailVo detail : result.getDetails()) {
                        ResiMeterReading update = new ResiMeterReading();
                        update.setId(detail.getReadingId());
                        update.setShareAmount(detail.getShareAmount());
                        update.setBilledUsage(detail.getBilledUsage());
                        update.setLastModifyTime(new Date());
                        update.setLastModifyUserId(String.valueOf(SecurityUtils.getUserId()));
                        updates.add(update);
                    }
                }
            }
        }

        // 批量更新数据库
        if (persist && !updates.isEmpty()) {
            for (ResiMeterReading update : updates) {
                baseMapper.updateById(update);
            }
        }

        return results;
    }

    /**
     * 计算单个公摊组的公摊量
     *
     * @param publicGroup   公摊组编号
     * @param groupReadings 该组下的所有抄表记录
     * @param persist       是否持久化
     * @return 公摊计算结果
     */
    private ResiMeterShareCalcResultVo calcGroupShare(String publicGroup,
                                                       List<ResiMeterReading> groupReadings,
                                                       boolean persist) {
        ResiMeterReading totalMeter = null;
        List<ResiMeterReading> subMeters = new ArrayList<>();

        for (ResiMeterReading reading : groupReadings) {
            if (reading.getIsPublic() != null && reading.getIsPublic().intValue() == 1) {
                totalMeter = reading;
            } else {
                subMeters.add(reading);
            }
        }

        // 校验：必须存在总表
        if (totalMeter == null) {
            throw new ServiceException("公摊组【" + publicGroup + "】缺少公摊总表，无法计算公摊");
        }

        // 校验：总表已录入读数
        if (totalMeter.getRawUsage() == null) {
            throw new ServiceException("公摊总表【" + totalMeter.getMeterCode() + "】尚未录入读数，无法计算公摊");
        }

        // 计算分户表用量合计
        BigDecimal subTotalUsage = BigDecimal.ZERO;
        for (ResiMeterReading sub : subMeters) {
            if (sub.getRawUsage() != null) {
                subTotalUsage = subTotalUsage.add(sub.getRawUsage());
            }
        }

        // 公摊总量 = 总表用量 - 分户表用量合计
        BigDecimal shareTotal = totalMeter.getRawUsage().subtract(subTotalUsage);
        if (shareTotal.compareTo(BigDecimal.ZERO) < 0) {
            shareTotal = BigDecimal.ZERO;
        }

        // 查询各分户房间面积并计算总面积
        BigDecimal totalArea = BigDecimal.ZERO;
        Map<Long, BigDecimal> roomAreaMap = new LinkedHashMap<>();
        Map<Long, String> roomNameMap = new LinkedHashMap<>();

        for (ResiMeterReading sub : subMeters) {
            Long roomId = sub.getRoomId();
            if (roomId == null) {
                continue;
            }
            ResiRoom room = roomService.getById(roomId);
            if (room != null && room.getBuildingArea() != null) {
                BigDecimal area = room.getBuildingArea();
                roomAreaMap.put(roomId, area);
                roomNameMap.put(roomId, room.getRoomAlias());
                totalArea = totalArea.add(area);
            }
        }

        ResiMeterShareCalcResultVo result = new ResiMeterShareCalcResultVo();
        result.setPublicGroup(publicGroup);
        result.setTotalUsage(totalMeter.getRawUsage());
        result.setSubTotalUsage(subTotalUsage);
        result.setShareTotal(shareTotal);
        result.setTotalArea(totalArea);
        result.setRoomCount(subMeters.size());

        List<ResiMeterShareDetailVo> details = new ArrayList<>();
        BigDecimal allocatedShare = BigDecimal.ZERO;

        // 分摊公摊量
        for (int i = 0; i < subMeters.size(); i++) {
            ResiMeterReading sub = subMeters.get(i);
            ResiMeterShareDetailVo detail = new ResiMeterShareDetailVo();
            detail.setReadingId(sub.getId());
            detail.setMeterId(sub.getMeterId());
            detail.setMeterCode(sub.getMeterCode());
            detail.setRoomId(sub.getRoomId());
            detail.setRoomName(sub.getRoomName());
            detail.setRawUsage(sub.getRawUsage() != null ? sub.getRawUsage() : BigDecimal.ZERO);
            detail.setLossAmount(sub.getLossAmount() != null ? sub.getLossAmount() : BigDecimal.ZERO);

            Long roomId = sub.getRoomId();
            BigDecimal area = roomAreaMap.getOrDefault(roomId, BigDecimal.ZERO);
            detail.setBuildingArea(area);

            BigDecimal shareAmount;
            if (totalArea.compareTo(BigDecimal.ZERO) == 0 || shareTotal.compareTo(BigDecimal.ZERO) == 0) {
                shareAmount = BigDecimal.ZERO;
                detail.setAreaRatio(BigDecimal.ZERO);
            } else if (i == subMeters.size() - 1) {
                // 最后一户：差额调整，确保分摊量之和 = 公摊总量
                shareAmount = shareTotal.subtract(allocatedShare);
                detail.setAreaRatio(area.divide(totalArea, 6, RoundingMode.HALF_UP));
            } else {
                // 按比例分摊，保留4位小数
                BigDecimal ratio = area.divide(totalArea, 6, RoundingMode.HALF_UP);
                shareAmount = shareTotal.multiply(ratio).setScale(4, RoundingMode.HALF_UP);
                allocatedShare = allocatedShare.add(shareAmount);
                detail.setAreaRatio(ratio);
            }

            detail.setShareAmount(shareAmount.setScale(4, RoundingMode.HALF_UP));

            // 计费用量 = 原始用量 - 损耗量 + 公摊分摊量
            BigDecimal billedUsage = detail.getRawUsage()
                    .subtract(detail.getLossAmount())
                    .add(detail.getShareAmount());
            detail.setBilledUsage(billedUsage.setScale(4, RoundingMode.HALF_UP));

            details.add(detail);
        }

        result.setDetails(details);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> bill(String id) {
        Map<String, Object> result = new HashMap<>();
        ResiMeterReading reading = getById(id);
        if (reading == null) {
            result.put("success", false);
            result.put("message", "抄表记录不存在");
            return result;
        }

        if (ResiConstants.METER_STATUS_BILLED.equals(reading.getStatus())) {
            result.put("success", false);
            result.put("message", "该抄表记录已入账，请勿重复操作");
            return result;
        }

        if (ResiConstants.METER_STATUS_VERIFIED.equals(reading.getStatus())) {
            result.put("success", false);
            result.put("message", "已复核的抄表记录不可入账");
            return result;
        }

        // 生成应收账单
        ResiReceivable receivable = receivableService.createFromMeterReading(reading);
        if (receivable == null) {
            result.put("success", false);
            result.put("message", "生成应收账单失败，请检查费用配置");
            return result;
        }

        // 更新抄表记录状态
        ResiMeterReading update = new ResiMeterReading();
        update.setId(id);
        update.setStatus(ResiConstants.METER_STATUS_BILLED);
        update.setReceivableId(receivable.getId());
        update.setLastModifyTime(new Date());
        update.setLastModifyUserId(String.valueOf(SecurityUtils.getUserId()));
        baseMapper.updateById(update);

        result.put("success", true);
        result.put("receivableId", receivable.getId());
        result.put("message", "入账成功");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchBill(Long projectId, String period, List<String> ids) {
        Map<String, Object> result = new HashMap<>();

        // 查询待入账的抄表记录
        List<ResiMeterReading> readings;
        if (ids != null && !ids.isEmpty()) {
            readings = baseMapper.selectBatchIds(ids);
            // 过滤出符合项目ID和期间的记录
            readings.removeIf(r -> !r.getProjectId().equals(projectId)
                    || !period.equals(r.getPeriod())
                    || !ResiConstants.METER_STATUS_INPUT.equals(r.getStatus()));
        } else {
            QueryWrapper<ResiMeterReading> qw = new QueryWrapper<>();
            qw.eq("project_id", projectId)
              .eq("period", period)
              .eq("status", ResiConstants.METER_STATUS_INPUT);
            readings = baseMapper.selectList(qw);
        }

        int total = readings.size();
        int success = 0;
        int skip = 0;
        int fail = 0;
        List<String> messages = new ArrayList<>();

        for (ResiMeterReading reading : readings) {
            try {
                // 跳过已入账或已复核的
                if (ResiConstants.METER_STATUS_BILLED.equals(reading.getStatus())
                        || ResiConstants.METER_STATUS_VERIFIED.equals(reading.getStatus())) {
                    skip++;
                    continue;
                }

                // 跳过未关联费用定义的
                if (StringUtils.isBlank(reading.getFeeId())) {
                    skip++;
                    messages.add("抄表记录【" + reading.getMeterCode() + "】未关联费用定义，跳过");
                    continue;
                }

                ResiReceivable receivable = receivableService.createFromMeterReading(reading);
                if (receivable != null) {
                    // 更新抄表记录状态
                    ResiMeterReading update = new ResiMeterReading();
                    update.setId(reading.getId());
                    update.setStatus(ResiConstants.METER_STATUS_BILLED);
                    update.setReceivableId(receivable.getId());
                    update.setLastModifyTime(new Date());
                    update.setLastModifyUserId(String.valueOf(SecurityUtils.getUserId()));
                    baseMapper.updateById(update);
                    success++;
                } else {
                    fail++;
                    messages.add("抄表记录【" + reading.getMeterCode() + "】生成应收失败");
                }
            } catch (Exception e) {
                fail++;
                messages.add("抄表记录【" + reading.getMeterCode() + "】入账异常：" + e.getMessage());
            }
        }

        result.put("total", total);
        result.put("success", success);
        result.put("skip", skip);
        result.put("fail", fail);
        result.put("messages", messages);
        return result;
    }

    /**
     * 计算用量
     */
    private void calculateUsage(ResiMeterReading entity) {
        if (entity.getCurrReading() == null || entity.getLastReading() == null) {
            return;
        }

        // 获取倍率
        BigDecimal multiplier = BigDecimal.ONE;
        if (entity.getMeterId() != null) {
            ResiMeterDevice device = meterDeviceService.getById(entity.getMeterId());
            if (device != null && device.getMultiplier() != null) {
                multiplier = device.getMultiplier();
            }
        }

        // 原始用量 = (本次读数 - 上次读数) × 倍率
        BigDecimal diff = entity.getCurrReading().subtract(entity.getLastReading());
        BigDecimal rawUsage = diff.multiply(multiplier);
        entity.setRawUsage(rawUsage.setScale(4, RoundingMode.HALF_UP));

        // 损耗量
        BigDecimal lossRate = entity.getLossRate() != null ? entity.getLossRate() : BigDecimal.ZERO;
        BigDecimal lossAmount = rawUsage.multiply(lossRate);
        entity.setLossAmount(lossAmount.setScale(4, RoundingMode.HALF_UP));

        // 公摊量（若无则取0）
        BigDecimal shareAmount = entity.getShareAmount() != null ? entity.getShareAmount() : BigDecimal.ZERO;

        // 实际计费用量 = 原始用量 - 损耗量 + 公摊量
        BigDecimal billedUsage = rawUsage.subtract(lossAmount).add(shareAmount);
        entity.setBilledUsage(billedUsage.setScale(4, RoundingMode.HALF_UP));
    }
}
