package com.zhaoxinms.resi.receivable.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.archive.entity.ResiCustomerAsset;
import com.zhaoxinms.resi.archive.entity.ResiCustomer;
import com.zhaoxinms.resi.archive.entity.ResiMeterDevice;
import com.zhaoxinms.resi.archive.entity.ResiRoom;
import com.zhaoxinms.resi.archive.service.IResiCustomerAssetService;
import com.zhaoxinms.resi.archive.service.IResiCustomerService;
import com.zhaoxinms.resi.archive.service.IResiMeterDeviceService;
import com.zhaoxinms.resi.archive.service.IResiRoomService;
import com.zhaoxinms.resi.common.ResiConstants;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition;
import com.zhaoxinms.resi.feeconfig.service.IResiFeeDefinitionService;
import com.zhaoxinms.resi.meter.entity.ResiMeterReading;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;
import com.zhaoxinms.resi.receivable.mapper.ResiReceivableMapper;
import com.zhaoxinms.resi.receivable.service.IResiReceivableService;

/**
 * 应收账单 Service实现
 *
 * @author zhaoxinms
 */
@Service
public class ResiReceivableServiceImpl extends ServiceImpl<ResiReceivableMapper, ResiReceivable>
        implements IResiReceivableService {

    @Autowired
    private IResiFeeDefinitionService feeDefinitionService;

    @Autowired
    private IResiRoomService roomService;

    @Autowired
    private IResiMeterDeviceService meterDeviceService;

    @Autowired
    private IResiCustomerAssetService customerAssetService;

    @Autowired
    private IResiCustomerService customerService;

    @Override
    public ResiReceivable createFromMeterReading(ResiMeterReading reading) {
        if (reading == null) {
            return null;
        }

        // 校验：已入账的记录不再重复生成
        if (ResiConstants.METER_STATUS_BILLED.equals(reading.getStatus())
                || ResiConstants.METER_STATUS_VERIFIED.equals(reading.getStatus())) {
            return null;
        }

        // 校验：必须已关联费用定义
        if (StringUtils.isBlank(reading.getFeeId())) {
            return null;
        }

        // 查询费用定义
        ResiFeeDefinition feeDef = feeDefinitionService.getById(reading.getFeeId());
        if (feeDef == null) {
            return null;
        }

        // 查询仪表设备
        ResiMeterDevice device = meterDeviceService.getById(reading.getMeterId());

        // 查询房间
        ResiRoom room = null;
        if (reading.getRoomId() != null) {
            room = roomService.getById(reading.getRoomId());
        }

        // 查询当前业主
        Long customerId = null;
        String customerName = null;
        if (room != null) {
            List<ResiCustomerAsset> assets = customerAssetService.lambdaQuery()
                    .eq(ResiCustomerAsset::getAssetType, ResiConstants.ASSET_TYPE_ROOM)
                    .eq(ResiCustomerAsset::getAssetId, room.getId())
                    .eq(ResiCustomerAsset::getIsCurrent, 1)
                    .list();
            if (assets != null && !assets.isEmpty()) {
                customerId = assets.get(0).getCustomerId();
                ResiCustomer customer = customerService.getById(customerId);
                if (customer != null) {
                    customerName = customer.getCustomerName();
                }
            }
        }

        Date now = new Date();
        String userId = String.valueOf(SecurityUtils.getUserId());

        // 计算金额
        BigDecimal num = reading.getBilledUsage() != null ? reading.getBilledUsage() : BigDecimal.ZERO;
        BigDecimal price = feeDef.getUnitPrice() != null ? feeDef.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal total = num.multiply(price);

        // 根据取整方式处理
        String roundType = feeDef.getRoundType();
        if (StringUtils.isBlank(roundType)) {
            roundType = ResiConstants.ROUND_TYPE_ROUND;
        }
        if (ResiConstants.ROUND_TYPE_CEIL.equals(roundType)) {
            total = total.setScale(2, RoundingMode.CEILING);
        } else if (ResiConstants.ROUND_TYPE_FLOOR.equals(roundType)) {
            total = total.setScale(2, RoundingMode.FLOOR);
        } else {
            total = total.setScale(2, RoundingMode.HALF_UP);
        }

        // 应收合计 = total + overdue_fee - discount_amount
        BigDecimal receivable = total;

        ResiReceivable entity = new ResiReceivable();
        entity.setProjectId(reading.getProjectId());
        entity.setResourceType(ResiConstants.RESOURCE_TYPE_ROOM);
        entity.setResourceId(reading.getRoomId());
        if (room != null) {
            entity.setResourceName(room.getRoomAlias());
        }
        entity.setCustomerId(customerId);
        entity.setCustomerName(customerName);
        entity.setFeeId(feeDef.getId());
        entity.setFeeName(feeDef.getFeeName());
        entity.setFeeType(feeDef.getFeeType());
        entity.setBillPeriod(reading.getPeriod());
        entity.setNum(num);
        entity.setPrice(price);
        entity.setTotal(total);
        entity.setOverdueFee(BigDecimal.ZERO);
        entity.setDiscountAmount(BigDecimal.ZERO);
        entity.setReceivable(receivable);
        entity.setPayState(ResiConstants.PAY_STATE_UNPAID);
        entity.setPaidAmount(BigDecimal.ZERO);
        entity.setMeterReadingId(reading.getId());
        entity.setRemark("抄表入账：" + reading.getPeriod());
        entity.setEnabledMark(1);
        entity.setCreatorTime(now);
        entity.setCreatorUserId(userId);
        entity.setLastModifyTime(now);
        entity.setLastModifyUserId(userId);

        baseMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ResiReceivable> createFromMeterReadings(List<ResiMeterReading> readings) {
        List<ResiReceivable> results = new ArrayList<>();
        if (readings == null || readings.isEmpty()) {
            return results;
        }

        for (ResiMeterReading reading : readings) {
            ResiReceivable receivable = createFromMeterReading(reading);
            if (receivable != null) {
                results.add(receivable);
            }
        }
        return results;
    }

    @Override
    public boolean removeByIds(java.util.Collection<? extends java.io.Serializable> idList) {
        Date now = new Date();
        String userId = String.valueOf(SecurityUtils.getUserId());
        for (java.io.Serializable id : idList) {
            ResiReceivable entity = new ResiReceivable();
            entity.setId((String) id);
            entity.setDeleteTime(now);
            entity.setDeleteUserId(userId);
            entity.setLastModifyTime(now);
            entity.setLastModifyUserId(userId);
            baseMapper.updateById(entity);
        }
        return true;
    }
}
