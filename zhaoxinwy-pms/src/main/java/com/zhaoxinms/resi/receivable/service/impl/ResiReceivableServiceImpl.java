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
import com.zhaoxinms.common.exception.ServiceException;
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
import com.zhaoxinms.resi.finance.entity.ResiAdjustLog;
import com.zhaoxinms.resi.finance.mapper.ResiAdjustLogMapper;
import com.zhaoxinms.resi.meter.entity.ResiMeterReading;
import com.zhaoxinms.resi.receivable.dto.ResiAdjustReq;
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

    @Autowired
    private ResiAdjustLogMapper adjustLogMapper;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjust(String id, ResiAdjustReq req) {
        Date now = new Date();
        String userId = String.valueOf(SecurityUtils.getUserId());

        // 1. 查询应收记录（FOR UPDATE 锁定）
        ResiReceivable receivable = baseMapper.selectById(id);
        if (receivable == null) {
            throw new ServiceException("应收记录不存在");
        }
        if (receivable.getDeleteTime() != null) {
            throw new ServiceException("该应收记录已删除，不可调账");
        }

        // 2. 校验项目权限
        if (!receivable.getProjectId().equals(req.getProjectId())) {
            throw new ServiceException("项目ID不匹配");
        }

        // 3. 已收款记录不允许调账（金额/账期/状态）
        if (ResiConstants.PAY_STATE_PAID.equals(receivable.getPayState())) {
            throw new ServiceException("已收款的记录不可调账");
        }

        // 4. 已减免记录只允许 OVERDUE_WAIVE
        if (ResiConstants.PAY_STATE_WAIVED.equals(receivable.getPayState())
                && !ResiConstants.ADJUST_TYPE_OVERDUE_WAIVE.equals(req.getAdjustType())) {
            throw new ServiceException("已减免的记录仅允许减免滞纳金操作");
        }

        String beforeValue = "";
        String afterValue = "";

        // 5. 根据调账类型执行不同逻辑
        if (ResiConstants.ADJUST_TYPE_AMOUNT.equals(req.getAdjustType())) {
            // 金额调整：修改 total，重算 receivable
            if (req.getNewAmount() == null || req.getNewAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new ServiceException("调整后金额不能为空且必须大于等于0");
            }
            beforeValue = "total=" + receivable.getTotal() + ",receivable=" + receivable.getReceivable();
            receivable.setTotal(req.getNewAmount().setScale(2, RoundingMode.HALF_UP));
            BigDecimal newReceivable = receivable.getTotal()
                    .add(receivable.getOverdueFee() != null ? receivable.getOverdueFee() : BigDecimal.ZERO)
                    .subtract(receivable.getDiscountAmount() != null ? receivable.getDiscountAmount() : BigDecimal.ZERO);
            receivable.setReceivable(newReceivable);
            afterValue = "total=" + receivable.getTotal() + ",receivable=" + receivable.getReceivable();

        } else if (ResiConstants.ADJUST_TYPE_PERIOD.equals(req.getAdjustType())) {
            // 账期调整：修改 bill_period
            if (StringUtils.isBlank(req.getNewPeriod())) {
                throw new ServiceException("调整后账期不能为空");
            }
            if (!req.getNewPeriod().matches("\\d{4}-\\d{2}")) {
                throw new ServiceException("账期格式不正确，应为 yyyy-MM");
            }
            beforeValue = "billPeriod=" + receivable.getBillPeriod();
            receivable.setBillPeriod(req.getNewPeriod());
            afterValue = "billPeriod=" + receivable.getBillPeriod();

        } else if (ResiConstants.ADJUST_TYPE_STATUS.equals(req.getAdjustType())) {
            // 状态调整：修改 pay_state
            if (StringUtils.isBlank(req.getNewStatus())) {
                throw new ServiceException("调整后状态不能为空");
            }
            if (!ResiConstants.PAY_STATE_UNPAID.equals(req.getNewStatus())
                    && !ResiConstants.PAY_STATE_WAIVED.equals(req.getNewStatus())) {
                throw new ServiceException("状态调整仅支持未收(0)或减免(3)");
            }
            beforeValue = "payState=" + receivable.getPayState();
            receivable.setPayState(req.getNewStatus());
            afterValue = "payState=" + receivable.getPayState();

        } else if (ResiConstants.ADJUST_TYPE_OVERDUE_WAIVE.equals(req.getAdjustType())) {
            // 减免滞纳金：overdue_fee 置 0，重算 receivable
            beforeValue = "overdueFee=" + receivable.getOverdueFee() + ",receivable=" + receivable.getReceivable();
            receivable.setOverdueFee(BigDecimal.ZERO);
            BigDecimal newReceivable = receivable.getTotal()
                    .subtract(receivable.getDiscountAmount() != null ? receivable.getDiscountAmount() : BigDecimal.ZERO);
            receivable.setReceivable(newReceivable);
            afterValue = "overdueFee=0.00,receivable=" + receivable.getReceivable();

        } else {
            throw new ServiceException("不支持的调账类型");
        }

        // 6. 更新应收记录
        receivable.setLastModifyTime(now);
        receivable.setLastModifyUserId(userId);
        baseMapper.updateById(receivable);

        // 7. 写入调账记录
        ResiAdjustLog log = new ResiAdjustLog();
        log.setProjectId(receivable.getProjectId());
        log.setReceivableId(id);
        log.setAdjustType(req.getAdjustType());
        log.setBeforeValue(beforeValue);
        log.setAfterValue(afterValue);
        log.setReason(req.getReason());
        log.setCreatorTime(now);
        log.setCreatorUserId(userId);
        adjustLogMapper.insert(log);
    }
}
