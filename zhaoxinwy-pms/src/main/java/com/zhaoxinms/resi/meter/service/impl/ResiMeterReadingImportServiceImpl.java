package com.zhaoxinms.resi.meter.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoxinms.base.util.RedisUtil;
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.common.utils.DateUtils;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.common.utils.poi.ExcelUtil;
import com.zhaoxinms.resi.archive.entity.ResiMeterDevice;
import com.zhaoxinms.resi.archive.service.IResiMeterDeviceService;
import com.zhaoxinms.resi.common.ResiConstants;
import com.zhaoxinms.resi.meter.dto.ResiMeterReadingImportConfirmReq;
import com.zhaoxinms.resi.meter.dto.ResiMeterReadingImportPreviewVo;
import com.zhaoxinms.resi.meter.dto.ResiMeterReadingImportResultVo;
import com.zhaoxinms.resi.meter.dto.ResiMeterReadingImportRowVo;
import com.zhaoxinms.resi.meter.dto.ResiMeterReadingImportVo;
import com.zhaoxinms.resi.meter.entity.ResiMeterReading;
import com.zhaoxinms.resi.meter.mapper.ResiMeterReadingMapper;
import com.zhaoxinms.resi.meter.service.IResiMeterReadingImportService;
import com.zhaoxinms.resi.meter.service.IResiMeterReadingService;

/**
 * 抄表Excel导入Service实现
 *
 * @author zhaoxinms
 */
@Service
public class ResiMeterReadingImportServiceImpl implements IResiMeterReadingImportService {

    private static final Logger log = LoggerFactory.getLogger(ResiMeterReadingImportServiceImpl.class);

    /** Redis TTL：30分钟（单位：秒） */
    private static final long REDIS_TTL_SECONDS = 30 * 60;

    /** 用量异常阈值倍数（超过上期用量的2倍视为异常） */
    private static final BigDecimal USAGE_THRESHOLD_MULTIPLIER = new BigDecimal("2.0");

    /** 用量异常上限绝对值（单月用量超过10000视为异常） */
    private static final BigDecimal USAGE_THRESHOLD_ABSOLUTE = new BigDecimal("10000");

    @Autowired
    private IResiMeterDeviceService meterDeviceService;

    @Autowired
    private IResiMeterReadingService meterReadingService;

    @Autowired
    private ResiMeterReadingMapper meterReadingMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void downloadTemplate(Long projectId, String period, HttpServletResponse response) throws IOException {
        if (projectId == null) {
            throw new ServiceException("项目ID不能为空");
        }

        // 查询项目下所有有效仪表
        ResiMeterDevice query = new ResiMeterDevice();
        query.setProjectId(projectId);
        List<ResiMeterDevice> deviceList = meterDeviceService.selectResiMeterDeviceList(query);

        List<ResiMeterReadingImportVo> templateList = new ArrayList<>();
        for (ResiMeterDevice device : deviceList) {
            ResiMeterReadingImportVo vo = new ResiMeterReadingImportVo();
            vo.setMeterCode(device.getMeterCode());
            vo.setRoomName(device.getRoomName());
            vo.setPeriod(period);
            vo.setMultiplier(device.getMultiplier());

            // 查询上期读数
            ResiMeterReading lastRecord = meterReadingMapper.selectLastRecord(device.getId());
            if (lastRecord != null) {
                vo.setLastReading(lastRecord.getCurrReading());
                vo.setLastDate(lastRecord.getCurrDate());
            } else {
                // 无上期记录，使用仪表初始读数
                vo.setLastReading(device.getInitReading() != null ? device.getInitReading() : BigDecimal.ZERO);
                vo.setLastDate(device.getInstallDate());
            }

            templateList.add(vo);
        }

        // 使用ExcelUtil导出模板
        ExcelUtil<ResiMeterReadingImportVo> util = new ExcelUtil<>(ResiMeterReadingImportVo.class);
        util.exportExcel(response, templateList, "抄表数据导入模板");
    }

    @Override
    public ResiMeterReadingImportPreviewVo uploadPreview(Long projectId, String period, InputStream is) throws Exception {
        if (projectId == null) {
            throw new ServiceException("项目ID不能为空");
        }

        // 1. 解析Excel
        ExcelUtil<ResiMeterReadingImportVo> util = new ExcelUtil<>(ResiMeterReadingImportVo.class);
        List<ResiMeterReadingImportVo> excelList = util.importExcel(is, 0);

        if (excelList == null || excelList.isEmpty()) {
            throw new ServiceException("Excel文件中没有数据");
        }

        // 2. 查询项目下所有仪表，建立编号映射
        ResiMeterDevice query = new ResiMeterDevice();
        query.setProjectId(projectId);
        List<ResiMeterDevice> deviceList = meterDeviceService.selectResiMeterDeviceList(query);
        Map<String, ResiMeterDevice> deviceMap = new HashMap<>();
        for (ResiMeterDevice device : deviceList) {
            deviceMap.put(device.getMeterCode(), device);
        }

        // 3. 查询该期间已存在的抄表记录，用于重复检测
        QueryWrapper<ResiMeterReading> existQw = new QueryWrapper<>();
        existQw.eq("project_id", projectId).eq("period", period);
        List<ResiMeterReading> existList = meterReadingMapper.selectList(existQw);
        Map<Long, ResiMeterReading> existMap = new HashMap<>();
        for (ResiMeterReading r : existList) {
            existMap.put(r.getMeterId(), r);
        }

        // 4. 逐行校验和预警检测
        List<ResiMeterReadingImportRowVo> rows = new ArrayList<>();
        int normalCount = 0;
        int warningCount = 0;
        int errorCount = 0;

        int rowNum = 2; // Excel数据从第2行开始（第1行是表头）
        for (ResiMeterReadingImportVo vo : excelList) {
            ResiMeterReadingImportRowVo row = new ResiMeterReadingImportRowVo();
            row.setRowNum(rowNum);
            row.setMeterCode(vo.getMeterCode());
            row.setRoomName(vo.getRoomName());
            row.setPeriod(StringUtils.isNotBlank(vo.getPeriod()) ? vo.getPeriod().trim() : period);
            row.setLastReading(vo.getLastReading());
            row.setLastDate(vo.getLastDate());
            row.setCurrReading(vo.getCurrReading());
            row.setCurrDate(vo.getCurrDate());
            row.setMultiplier(vo.getMultiplier());

            StringBuilder errorMsg = new StringBuilder();
            StringBuilder warnMsg = new StringBuilder();

            // 4.1 校验仪表编号
            if (StringUtils.isBlank(vo.getMeterCode())) {
                errorMsg.append("仪表编号不能为空；");
            } else {
                ResiMeterDevice device = deviceMap.get(vo.getMeterCode().trim());
                if (device == null) {
                    errorMsg.append("仪表编号[").append(vo.getMeterCode().trim()).append("]不存在；");
                } else {
                    row.setMeterId(device.getId());
                    row.setRoomId(device.getRoomId());
                    row.setMultiplier(device.getMultiplier());

                    // 如果Excel中未填房间名称，使用设备关联的房间名
                    if (StringUtils.isBlank(row.getRoomName())) {
                        row.setRoomName(device.getRoomName());
                    }

                    // 4.2 校验期间重复（uk_meter_period）
                    if (existMap.containsKey(device.getId())) {
                        errorMsg.append("该仪表在期间[").append(row.getPeriod()).append("]已存在抄表记录；");
                    }

                    // 4.3 自动带入上期读数（如果Excel中未填）
                    if (row.getLastReading() == null) {
                        ResiMeterReading lastRecord = meterReadingMapper.selectLastRecord(device.getId());
                        if (lastRecord != null) {
                            row.setLastReading(lastRecord.getCurrReading());
                            row.setLastDate(lastRecord.getCurrDate());
                        } else {
                            row.setLastReading(device.getInitReading() != null ? device.getInitReading() : BigDecimal.ZERO);
                            row.setLastDate(device.getInstallDate());
                        }
                    }
                }
            }

            // 4.4 校验期间格式
            if (StringUtils.isBlank(row.getPeriod())) {
                errorMsg.append("抄表期间不能为空；");
            } else if (!row.getPeriod().matches("\\d{4}-\\d{2}")) {
                errorMsg.append("抄表期间格式错误，应为yyyy-MM；");
            }

            // 4.5 校验本次读数
            if (vo.getCurrReading() == null) {
                errorMsg.append("本次读数不能为空；");
            } else if (vo.getCurrReading().compareTo(BigDecimal.ZERO) < 0) {
                errorMsg.append("本次读数不能小于0；");
            }

            // 4.6 校验抄表日期
            if (vo.getCurrDate() == null) {
                errorMsg.append("抄表日期不能为空；");
            }

            // 4.7 预警检测：读数回退
            if (row.getMeterId() != null && row.getLastReading() != null && vo.getCurrReading() != null) {
                if (vo.getCurrReading().compareTo(row.getLastReading()) < 0) {
                    warnMsg.append("本次读数小于上期读数（读数回退）；");
                }

                // 计算原始用量用于预警
                BigDecimal multiplier = row.getMultiplier() != null ? row.getMultiplier() : BigDecimal.ONE;
                BigDecimal diff = vo.getCurrReading().subtract(row.getLastReading());
                BigDecimal rawUsage = diff.multiply(multiplier).setScale(4, RoundingMode.HALF_UP);
                row.setRawUsage(rawUsage);

                // 4.8 预警检测：用量超阈值
                if (rawUsage.compareTo(USAGE_THRESHOLD_ABSOLUTE) > 0) {
                    warnMsg.append("原始用量[").append(rawUsage.stripTrailingZeros().toPlainString())
                           .append("]超过异常阈值[").append(USAGE_THRESHOLD_ABSOLUTE.stripTrailingZeros().toPlainString())
                           .append("]；");
                }

                // 查询上期用量，判断是否超过2倍
                ResiMeterReading lastRecord = meterReadingMapper.selectLastRecord(row.getMeterId());
                if (lastRecord != null && lastRecord.getRawUsage() != null
                        && lastRecord.getRawUsage().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal threshold = lastRecord.getRawUsage().multiply(USAGE_THRESHOLD_MULTIPLIER);
                    if (rawUsage.compareTo(threshold) > 0) {
                        warnMsg.append("原始用量[").append(rawUsage.stripTrailingZeros().toPlainString())
                               .append("]超过上期用量2倍；");
                    }
                }
            }

            // 5. 判定行类型
            if (errorMsg.length() > 0) {
                row.setRowType("ERROR");
                row.setRowMsg(errorMsg.toString());
                errorCount++;
            } else if (warnMsg.length() > 0) {
                row.setRowType("WARNING");
                row.setRowMsg(warnMsg.toString());
                warningCount++;
            } else {
                row.setRowType("NORMAL");
                row.setRowMsg("正常");
                normalCount++;
            }

            rows.add(row);
            rowNum++;
        }

        // 6. 生成批次ID并暂存Redis
        String batchId = UUID.randomUUID().toString().replace("-", "");
        String redisKey = ResiConstants.REDIS_METER_IMPORT_PREFIX + batchId;
        String jsonData = JSON.toJSONString(rows);
        redisUtil.insert(redisKey, jsonData, REDIS_TTL_SECONDS);

        // 7. 组装预览结果
        ResiMeterReadingImportPreviewVo preview = new ResiMeterReadingImportPreviewVo();
        preview.setBatchId(batchId);
        preview.setTotalCount(rows.size());
        preview.setNormalCount(normalCount);
        preview.setWarningCount(warningCount);
        preview.setErrorCount(errorCount);
        preview.setRows(rows);

        log.info("抄表导入预览完成 projectId={} period={} batchId={} total={} normal={} warning={} error={}",
                projectId, period, batchId, rows.size(), normalCount, warningCount, errorCount);

        return preview;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResiMeterReadingImportResultVo confirmImport(ResiMeterReadingImportConfirmReq req) {
        String batchId = req.getBatchId();
        String redisKey = ResiConstants.REDIS_METER_IMPORT_PREFIX + batchId;

        // 1. 从Redis获取预览数据
        Object redisValue = redisUtil.getString(redisKey);
        if (redisValue == null) {
            throw new ServiceException("导入会话已超时，请重新上传");
        }

        List<ResiMeterReadingImportRowVo> rows = JSON.parseArray(String.valueOf(redisValue), ResiMeterReadingImportRowVo.class);
        if (rows == null || rows.isEmpty()) {
            throw new ServiceException("导入数据为空");
        }

        // 2. 过滤掉ERROR行，只导入NORMAL和WARNING行
        List<ResiMeterReading> importList = new ArrayList<>();
        int skipCount = 0;
        for (ResiMeterReadingImportRowVo row : rows) {
            if ("ERROR".equals(row.getRowType())) {
                skipCount++;
                continue;
            }

            // 检查该期间是否已存在记录（防并发重复）
            QueryWrapper<ResiMeterReading> qw = new QueryWrapper<>();
            qw.eq("meter_id", row.getMeterId()).eq("period", row.getPeriod());
            long existCount = meterReadingMapper.selectCount(qw);
            if (existCount > 0) {
                skipCount++;
                continue;
            }

            ResiMeterReading reading = new ResiMeterReading();
            reading.setProjectId(req.getProjectId());
            reading.setMeterId(row.getMeterId());
            reading.setRoomId(row.getRoomId());
            reading.setPeriod(row.getPeriod());
            reading.setLastReading(row.getLastReading());
            reading.setLastDate(row.getLastDate());
            reading.setCurrReading(row.getCurrReading());
            reading.setCurrDate(row.getCurrDate());
            reading.setMultiplier(row.getMultiplier());
            reading.setImportBatch(batchId);
            reading.setStatus(ResiConstants.METER_STATUS_INPUT);

            importList.add(reading);
        }

        // 3. 批量导入（逐条保存，避免唯一键冲突导致整批回滚）
        int successCount = 0;
        int failCount = 0;
        for (ResiMeterReading reading : importList) {
            try {
                boolean result = meterReadingService.save(reading);
                if (result) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                // 唯一键冲突或其他异常，记录并跳过
                log.warn("抄表导入单条失败 batchId={} meterId={} period={} err={}",
                        batchId, reading.getMeterId(), reading.getPeriod(), e.getMessage());
                failCount++;
            }
        }

        // 4. 清除Redis缓存
        redisUtil.remove(redisKey);

        ResiMeterReadingImportResultVo result = new ResiMeterReadingImportResultVo();
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        result.setSkipCount(skipCount);
        result.setMessage(String.format("导入完成：成功%d条，失败%d条，跳过%d条（含错误行及重复记录）",
                successCount, failCount, skipCount));

        log.info("抄表导入确认完成 batchId={} success={} fail={} skip={}",
                batchId, successCount, failCount, skipCount);

        return result;
    }
}
