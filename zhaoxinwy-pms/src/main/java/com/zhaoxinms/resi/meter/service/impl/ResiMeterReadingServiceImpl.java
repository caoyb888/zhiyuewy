package com.zhaoxinms.resi.meter.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.archive.entity.ResiMeterDevice;
import com.zhaoxinms.resi.archive.service.IResiMeterDeviceService;
import com.zhaoxinms.resi.meter.entity.ResiMeterReading;
import com.zhaoxinms.resi.meter.mapper.ResiMeterReadingMapper;
import com.zhaoxinms.resi.meter.service.IResiMeterReadingService;

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

        // 补充仪表编号和房间名称
        for (ResiMeterReading item : list) {
            if (item.getMeterId() != null) {
                ResiMeterDevice device = meterDeviceService.getById(item.getMeterId());
                if (device != null) {
                    item.setMeterCode(device.getMeterCode());
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
