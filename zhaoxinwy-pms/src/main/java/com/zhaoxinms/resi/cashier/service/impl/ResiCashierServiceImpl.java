package com.zhaoxinms.resi.cashier.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoxinms.base.service.BillRuleService;
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.archive.entity.ResiCustomer;
import com.zhaoxinms.resi.archive.entity.ResiCustomerAsset;
import com.zhaoxinms.resi.archive.entity.ResiRoom;
import com.zhaoxinms.resi.archive.service.IResiCustomerAssetService;
import com.zhaoxinms.resi.archive.service.IResiCustomerService;
import com.zhaoxinms.resi.archive.service.IResiRoomService;
import com.zhaoxinms.resi.cashier.dto.ResiCashierCalcReq;
import com.zhaoxinms.resi.cashier.dto.ResiCashierCalcVo;
import com.zhaoxinms.resi.cashier.dto.ResiCashierCollectReq;
import com.zhaoxinms.resi.cashier.dto.ResiCashierCollectVo;
import com.zhaoxinms.resi.cashier.dto.ResiCashierRefundReq;
import com.zhaoxinms.resi.cashier.dto.ResiCashierRoomSearchVo;
import com.zhaoxinms.resi.cashier.dto.ResiCashierRoomSummaryVo;
import com.zhaoxinms.resi.cashier.dto.ResiCashierWaiveOverdueReq;
import com.zhaoxinms.resi.cashier.dto.ResiCashierWriteOffReq;
import com.zhaoxinms.resi.cashier.service.IResiCashierService;
import com.zhaoxinms.resi.common.ResiConstants;
import com.zhaoxinms.resi.feeconfig.entity.ResiDiscount;
import com.zhaoxinms.resi.feeconfig.mapper.ResiDiscountMapper;
import com.zhaoxinms.resi.finance.entity.ResiAdjustLog;
import com.zhaoxinms.resi.finance.entity.ResiPayLog;
import com.zhaoxinms.resi.finance.entity.ResiPreAccount;
import com.zhaoxinms.resi.finance.mapper.ResiAdjustLogMapper;
import com.zhaoxinms.resi.finance.mapper.ResiPayLogMapper;
import com.zhaoxinms.resi.finance.service.IResiDepositService;
import com.zhaoxinms.resi.finance.service.IResiPrePayService;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;
import com.zhaoxinms.resi.receivable.mapper.ResiReceivableMapper;

/**
 * 收银台 Service实现
 */
@Service
public class ResiCashierServiceImpl implements IResiCashierService {

    private static final Logger log = LoggerFactory.getLogger(ResiCashierServiceImpl.class);

    @Autowired
    private IResiRoomService roomService;

    @Autowired
    private IResiCustomerService customerService;

    @Autowired
    private IResiCustomerAssetService customerAssetService;

    @Autowired
    private ResiReceivableMapper receivableMapper;

    @Autowired
    private ResiPayLogMapper payLogMapper;

    @Autowired
    private ResiDiscountMapper discountMapper;

    @Autowired
    private BillRuleService billRuleService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private IResiPrePayService prePayService;

    @Autowired
    private IResiDepositService depositService;

    @Autowired
    private ResiAdjustLogMapper adjustLogMapper;

    // ==================== S4-03 查询接口 ====================

    @Override
    public List<ResiCashierRoomSearchVo> searchRoom(String keyword, Long projectId) {
        List<ResiCashierRoomSearchVo> result = new ArrayList<>();
        Map<Long, ResiCashierRoomSearchVo> roomMap = new LinkedHashMap<>();

        // 1. 按 room_alias / room_no 搜索房间
        List<ResiRoom> roomsByKeyword = roomService.searchRoom(keyword, projectId);
        for (ResiRoom room : roomsByKeyword) {
            if (!roomMap.containsKey(room.getId())) {
                roomMap.put(room.getId(), convertToVo(room));
            }
        }

        // 2. 按 customer_name 搜索客户，找到其绑定的房间
        if (keyword != null && !keyword.trim().isEmpty()) {
            ResiCustomer customerQuery = new ResiCustomer();
            customerQuery.setCustomerName(keyword.trim());
            List<ResiCustomer> customers = customerService.selectResiCustomerList(customerQuery);
            for (ResiCustomer customer : customers) {
                List<ResiCustomerAsset> assets = customerAssetService.lambdaQuery()
                        .eq(ResiCustomerAsset::getCustomerId, customer.getId())
                        .eq(ResiCustomerAsset::getAssetType, ResiConstants.ASSET_TYPE_ROOM)
                        .eq(ResiCustomerAsset::getIsCurrent, 1)
                        .list();
                for (ResiCustomerAsset asset : assets) {
                    if (projectId != null && !projectId.equals(asset.getProjectId())) {
                        continue;
                    }
                    Long roomId = asset.getAssetId();
                    if (!roomMap.containsKey(roomId)) {
                        ResiRoom room = roomService.getById(roomId);
                        if (room != null && Integer.valueOf(1).equals(room.getEnabledMark())) {
                            ResiCashierRoomSearchVo vo = convertToVo(room);
                            vo.setCustomerId(customer.getId());
                            vo.setCustomerName(customer.getCustomerName());
                            roomMap.put(roomId, vo);
                        }
                    } else {
                        ResiCashierRoomSearchVo vo = roomMap.get(roomId);
                        if (vo.getCustomerId() == null) {
                            vo.setCustomerId(customer.getId());
                            vo.setCustomerName(customer.getCustomerName());
                        }
                    }
                }
            }
        }

        result.addAll(roomMap.values());
        log.info("收银台搜索房间 keyword={} projectId={} resultSize={}", keyword, projectId, result.size());
        return result;
    }

    @Override
    public List<ResiReceivable> getRoomReceivables(Long roomId, Map<String, String> params) {
        QueryWrapper<ResiReceivable> qw = new QueryWrapper<>();
        qw.eq("resource_type", ResiConstants.RESOURCE_TYPE_ROOM);
        qw.eq("resource_id", roomId);
        qw.in("pay_state", ResiConstants.PAY_STATE_UNPAID, ResiConstants.PAY_STATE_PART_PAID);
        qw.isNull("delete_time");

        if (params != null) {
            String feeId = params.get("feeId");
            if (feeId != null && !feeId.isEmpty()) {
                qw.eq("fee_id", feeId);
            }
            String year = params.get("year");
            if (year != null && !year.isEmpty()) {
                qw.likeRight("bill_period", year);
            }
            String period = params.get("period");
            if (period != null && !period.isEmpty()) {
                qw.eq("bill_period", period);
            }
            String feeType = params.get("feeType");
            if (feeType != null && !feeType.isEmpty()) {
                qw.eq("fee_type", feeType);
            }
        }

        qw.orderByAsc("bill_period");
        return receivableMapper.selectList(qw);
    }

    @Override
    public ResiCashierRoomSummaryVo getRoomSummary(Long roomId) {
        QueryWrapper<ResiReceivable> qw = new QueryWrapper<>();
        qw.eq("resource_type", ResiConstants.RESOURCE_TYPE_ROOM);
        qw.eq("resource_id", roomId);
        qw.isNull("delete_time");

        List<ResiReceivable> list = receivableMapper.selectList(qw);
        ResiCashierRoomSummaryVo summary = new ResiCashierRoomSummaryVo();

        for (ResiReceivable r : list) {
            String payState = r.getPayState();
            BigDecimal receivable = r.getReceivable() != null ? r.getReceivable() : BigDecimal.ZERO;
            BigDecimal paidAmount = r.getPaidAmount() != null ? r.getPaidAmount() : BigDecimal.ZERO;

            summary.setTotalReceivable(summary.getTotalReceivable().add(receivable));
            summary.setTotalPaid(summary.getTotalPaid().add(paidAmount));

            if (ResiConstants.PAY_STATE_UNPAID.equals(payState)) {
                summary.setTotalArrears(summary.getTotalArrears().add(receivable));
                summary.setUnpaidCount(summary.getUnpaidCount() + 1);
            } else if (ResiConstants.PAY_STATE_PART_PAID.equals(payState)) {
                summary.setTotalArrears(summary.getTotalArrears().add(receivable.subtract(paidAmount)));
                summary.setPartPaidCount(summary.getPartPaidCount() + 1);
            } else if (ResiConstants.PAY_STATE_PAID.equals(payState)) {
                summary.setPaidCount(summary.getPaidCount() + 1);
            }
        }

        return summary;
    }

    // ==================== S4-04 收款核心 ====================

    @Override
    public ResiCashierCalcVo calc(ResiCashierCalcReq req) {
        // 1. 查询应收记录
        List<ResiReceivable> receivables = receivableMapper.selectList(
                new QueryWrapper<ResiReceivable>()
                        .in("id", req.getReceivableIds())
                        .isNull("delete_time"));

        if (receivables.isEmpty()) {
            throw new ServiceException("所选应收记录不存在");
        }

        // 2. 校验状态
        for (ResiReceivable r : receivables) {
            if (ResiConstants.PAY_STATE_PAID.equals(r.getPayState())) {
                throw new ServiceException("费用【" + r.getFeeName() + "】已缴清，不可重复收款");
            }
            if (ResiConstants.PAY_STATE_WAIVED.equals(r.getPayState())) {
                throw new ServiceException("费用【" + r.getFeeName() + "】已减免，不可收款");
            }
        }

        // 3. 计算金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal overdueAmount = BigDecimal.ZERO;
        List<ResiCashierCalcVo.ReceivableItem> items = new ArrayList<>();

        for (ResiReceivable r : receivables) {
            totalAmount = totalAmount.add(r.getTotal() != null ? r.getTotal() : BigDecimal.ZERO);
            overdueAmount = overdueAmount.add(r.getOverdueFee() != null ? r.getOverdueFee() : BigDecimal.ZERO);

            ResiCashierCalcVo.ReceivableItem item = new ResiCashierCalcVo.ReceivableItem();
            item.setId(r.getId());
            item.setFeeName(r.getFeeName());
            item.setBillPeriod(r.getBillPeriod());
            item.setTotal(r.getTotal());
            item.setOverdueFee(r.getOverdueFee());
            item.setDiscountAmount(r.getDiscountAmount());
            item.setReceivable(r.getReceivable());
            items.add(item);
        }

        // 4. 折扣计算
        BigDecimal discountAmount = BigDecimal.ZERO;
        String discountName = null;
        if (StringUtils.isNotBlank(req.getDiscountId())) {
            ResiDiscount discount = discountMapper.selectById(req.getDiscountId());
            if (discount != null && Integer.valueOf(1).equals(discount.getEnabledMark())) {
                // 校验有效期
                Date today = new Date();
                if (discount.getValidStart() != null && today.before(discount.getValidStart())) {
                    throw new ServiceException("折扣尚未生效");
                }
                if (discount.getValidEnd() != null && today.after(discount.getValidEnd())) {
                    throw new ServiceException("折扣已过期");
                }
                // 校验费用范围
                boolean applicable = checkDiscountApplicable(discount, receivables);
                if (!applicable) {
                    throw new ServiceException("该折扣不适用于所选费用");
                }
                // 计算折扣金额
                BigDecimal rate = discount.getDiscountRate();
                if (rate != null && rate.compareTo(BigDecimal.ONE) < 0) {
                    discountAmount = totalAmount.multiply(BigDecimal.ONE.subtract(rate))
                            .setScale(2, RoundingMode.HALF_UP);
                }
                discountName = discount.getDiscountName();
            }
        }

        BigDecimal receivableAmount = totalAmount.add(overdueAmount).subtract(discountAmount);

        // 5. 预收款冲抵计算（预览）
        BigDecimal prePayAmount = BigDecimal.ZERO;
        List<ResiCashierCalcVo.PrePayAccount> prePayAccountVos = new ArrayList<>();
        if (Boolean.TRUE.equals(req.getUsePrePay())
                && StringUtils.isNotBlank(req.getResourceType())
                && req.getResourceId() != null) {
            List<ResiPreAccount> accounts = prePayService.listAccounts(req.getProjectId(), req.getResourceType(), req.getResourceId());
            for (ResiPreAccount acc : accounts) {
                ResiCashierCalcVo.PrePayAccount vo = new ResiCashierCalcVo.PrePayAccount();
                vo.setAccountId(acc.getId());
                vo.setFeeId(acc.getFeeId());
                vo.setBalance(acc.getBalance());
                vo.setEarmark(acc.getFeeId() != null);
                prePayAccountVos.add(vo);
            }

            // 模拟冲抵：专款优先匹配同 fee_id 的费用，通用账户次之
            BigDecimal remainingReceivable = receivableAmount;
            for (ResiReceivable r : receivables) {
                if (remainingReceivable.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                BigDecimal need = r.getReceivable().subtract(r.getPaidAmount() != null ? r.getPaidAmount() : BigDecimal.ZERO);
                if (need.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                // 找专款
                for (ResiPreAccount acc : accounts) {
                    if (acc.getFeeId() != null && acc.getFeeId().equals(r.getFeeId()) && acc.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal use = need.min(acc.getBalance()).min(remainingReceivable);
                        prePayAmount = prePayAmount.add(use);
                        remainingReceivable = remainingReceivable.subtract(use);
                        break;
                    }
                }
                if (remainingReceivable.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                // 找通用
                for (ResiPreAccount acc : accounts) {
                    if (acc.getFeeId() == null && acc.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal use = need.min(acc.getBalance()).min(remainingReceivable);
                        prePayAmount = prePayAmount.add(use);
                        remainingReceivable = remainingReceivable.subtract(use);
                        break;
                    }
                }
            }
        }

        BigDecimal actualPayAmount = receivableAmount.subtract(prePayAmount);
        if (actualPayAmount.compareTo(BigDecimal.ZERO) < 0) {
            actualPayAmount = BigDecimal.ZERO;
        }

        ResiCashierCalcVo result = new ResiCashierCalcVo();
        result.setItems(items);
        result.setTotalAmount(totalAmount);
        result.setOverdueAmount(overdueAmount);
        result.setDiscountAmount(discountAmount);
        result.setReceivableAmount(receivableAmount);
        result.setPayAmount(actualPayAmount);
        result.setActualPayAmount(actualPayAmount);
        result.setPrePayAmount(prePayAmount);
        result.setPrePayAccounts(prePayAccountVos);
        result.setDiscountId(req.getDiscountId());
        result.setDiscountName(discountName);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResiCashierCollectVo collect(ResiCashierCollectReq req) {
        Date now = new Date();
        String userId = String.valueOf(SecurityUtils.getUserId());

        // 1. SELECT FOR UPDATE 锁定应收记录（必须在事务内）
        QueryWrapper<ResiReceivable> lockQw = new QueryWrapper<>();
        lockQw.in("id", req.getReceivableIds())
                .isNull("delete_time")
                .last("FOR UPDATE");
        List<ResiReceivable> lockedList = receivableMapper.selectList(lockQw);

        if (lockedList.isEmpty()) {
            throw new ServiceException("所选应收记录不存在");
        }
        if (lockedList.size() != req.getReceivableIds().size()) {
            throw new ServiceException("部分应收记录不存在或已被删除");
        }

        // 2. 校验锁定后的记录状态
        for (ResiReceivable r : lockedList) {
            if (ResiConstants.PAY_STATE_PAID.equals(r.getPayState())) {
                throw new ServiceException("费用【" + r.getFeeName() + "】已被收取，请刷新后重试");
            }
            if (ResiConstants.PAY_STATE_WAIVED.equals(r.getPayState())) {
                throw new ServiceException("费用【" + r.getFeeName() + "】已减免，不可收款");
            }
        }

        // 3. 计算金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal overdueAmount = BigDecimal.ZERO;
        for (ResiReceivable r : lockedList) {
            totalAmount = totalAmount.add(r.getTotal() != null ? r.getTotal() : BigDecimal.ZERO);
            overdueAmount = overdueAmount.add(r.getOverdueFee() != null ? r.getOverdueFee() : BigDecimal.ZERO);
        }

        // 4. 折扣计算
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (StringUtils.isNotBlank(req.getDiscountId())) {
            ResiDiscount discount = discountMapper.selectById(req.getDiscountId());
            if (discount != null && Integer.valueOf(1).equals(discount.getEnabledMark())) {
                Date today = new Date();
                if ((discount.getValidStart() == null || !today.before(discount.getValidStart()))
                        && (discount.getValidEnd() == null || !today.after(discount.getValidEnd()))) {
                    boolean applicable = checkDiscountApplicable(discount, lockedList);
                    if (applicable && discount.getDiscountRate() != null
                            && discount.getDiscountRate().compareTo(BigDecimal.ONE) < 0) {
                        discountAmount = totalAmount.multiply(BigDecimal.ONE.subtract(discount.getDiscountRate()))
                                .setScale(2, RoundingMode.HALF_UP);
                    }
                }
            }
        }

        BigDecimal receivableAmount = totalAmount.add(overdueAmount).subtract(discountAmount);

        // 5. 预生成收款流水ID（供预收款冲抵关联用）
        String payLogId = java.util.UUID.randomUUID().toString().replace("-", "");

        // 6. 预收款冲抵（实际执行）
        BigDecimal prePayAmount = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(req.getUsePrePay())
                && StringUtils.isNotBlank(req.getResourceType())
                && req.getResourceId() != null) {
            prePayAmount = prePayService.offsetForCollect(payLogId, req.getProjectId(), req.getResourceType(),
                    req.getResourceId(), lockedList, userId, now);
        }

        BigDecimal expectedPay = receivableAmount.subtract(prePayAmount);
        if (expectedPay.compareTo(BigDecimal.ZERO) < 0) {
            expectedPay = BigDecimal.ZERO;
        }

        // 7. 校验实收金额（允许少量误差 0.01）
        if (req.getPayAmount() == null || req.getPayAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("实收金额不能为负数");
        }
        // 若冲抵后还需支付，则校验金额；若冲抵已全额覆盖，则允许payAmount=0
        if (expectedPay.compareTo(BigDecimal.ZERO) > 0) {
            if (req.getPayAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("实收金额必须大于0");
            }
        }
        if (req.getPayAmount().subtract(expectedPay).abs().compareTo(new BigDecimal("0.02")) > 0) {
            throw new ServiceException("实收金额与应收金额不符，应收：" + expectedPay + "元");
        }

        // 8. 生成收据号
        String payNo = billRuleService.getNumber(ResiConstants.BILL_RULE_RESI_RECEIPT);
        if (StringUtils.isBlank(payNo) || payNo.contains("单据规则不存在")) {
            throw new ServiceException("收据号生成失败，请检查单据规则配置");
        }

        // 9. 批量更新应收记录（预收款冲抵可能已更新，此处补充现金收款部分）
        BigDecimal remainingCash = req.getPayAmount();
        for (ResiReceivable r : lockedList) {
            BigDecimal thisReceivable = r.getReceivable() != null ? r.getReceivable() : BigDecimal.ZERO;
            BigDecimal currentPaid = r.getPaidAmount() != null ? r.getPaidAmount() : BigDecimal.ZERO;
            if (currentPaid.compareTo(thisReceivable) < 0 && remainingCash.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal gap = thisReceivable.subtract(currentPaid);
                BigDecimal add = gap.min(remainingCash);
                currentPaid = currentPaid.add(add);
                remainingCash = remainingCash.subtract(add);
                r.setPaidAmount(currentPaid);
            }
            if (currentPaid.compareTo(thisReceivable) >= 0) {
                r.setPayState(ResiConstants.PAY_STATE_PAID);
            } else if (currentPaid.compareTo(BigDecimal.ZERO) > 0) {
                r.setPayState(ResiConstants.PAY_STATE_PART_PAID);
            }
            r.setPayLogId(payLogId);
            r.setPayTime(now);
            r.setDiscountId(req.getDiscountId());
            r.setDiscountAmount(discountAmount);
            r.setLastModifyTime(now);
            r.setLastModifyUserId(userId);
            receivableMapper.updateById(r);
        }

        // 8. 组装资源名称和客户姓名
        String resourceName = null;
        String customerName = null;
        ResiRoom room = roomService.getById(req.getResourceId());
        if (room != null) {
            resourceName = room.getRoomAlias();
        }
        List<ResiCustomerAsset> assets = customerAssetService.lambdaQuery()
                .eq(ResiCustomerAsset::getAssetType, ResiConstants.ASSET_TYPE_ROOM)
                .eq(ResiCustomerAsset::getAssetId, req.getResourceId())
                .eq(ResiCustomerAsset::getIsCurrent, 1)
                .list();
        if (assets != null && !assets.isEmpty()) {
            ResiCustomer customer = customerService.getById(assets.get(0).getCustomerId());
            if (customer != null) {
                customerName = customer.getCustomerName();
            }
        }

        // 9. INSERT 收款流水
        ResiPayLog payLog = new ResiPayLog();
        payLog.setId(payLogId);
        payLog.setProjectId(req.getProjectId());
        payLog.setPayNo(payNo);
        payLog.setResourceType(req.getResourceType());
        payLog.setResourceId(req.getResourceId());
        payLog.setResourceName(resourceName);
        payLog.setCustomerName(customerName);
        payLog.setPayType(ResiConstants.PAY_TYPE_COLLECT);
        payLog.setPayMethod(req.getPayMethod());
        payLog.setReceivableIds(com.alibaba.fastjson.JSON.toJSONString(req.getReceivableIds()));
        payLog.setTotalAmount(totalAmount.add(overdueAmount));
        payLog.setDiscountAmount(discountAmount);
        payLog.setOverdueAmount(overdueAmount);
        payLog.setPrePayAmount(prePayAmount);
        payLog.setPayAmount(req.getPayAmount());
        payLog.setChangeAmount(req.getChangeAmount() != null ? req.getChangeAmount() : BigDecimal.ZERO);
        payLog.setNote(req.getNote());
        payLog.setDiscountId(req.getDiscountId());
        payLog.setIsVerified(0);
        payLog.setClient(1);
        payLog.setCreatorTime(now);
        payLog.setCreatorUserId(userId);
        payLogMapper.insert(payLog);

        // 10. 押金处理：fee_type=DEPOSIT 时自动写入 resi_deposit
        try {
            depositService.createFromCollect(payLog, lockedList);
        } catch (Exception e) {
            log.warn("押金台账写入失败 payLogId={} error={}", payLogId, e.getMessage());
            // 押金写入失败不影响收款主流程，但记录日志
        }

        // 11. 清除 Redis 看板缓存
        try {
            String pattern = "resi:dashboard:" + req.getProjectId() + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("收款后清除看板缓存 projectId={} keys={}", req.getProjectId(), keys.size());
            }
        } catch (Exception e) {
            log.warn("清除看板缓存失败 projectId={} error={}", req.getProjectId(), e.getMessage());
        }

        log.info("收款成功 projectId={} roomId={} payNo={} amount={} prePayAmount={} receivableCount={}",
                req.getProjectId(), req.getResourceId(), payNo, req.getPayAmount(), prePayAmount, lockedList.size());

        // 11. 组装返回结果
        ResiCashierCollectVo result = new ResiCashierCollectVo();
        result.setPayLogId(payLogId);
        result.setPayNo(payNo);
        result.setResourceName(resourceName);
        result.setCustomerName(customerName);
        result.setPayMethod(req.getPayMethod());
        result.setTotalAmount(totalAmount.add(overdueAmount));
        result.setDiscountAmount(discountAmount);
        result.setPayAmount(req.getPayAmount());
        result.setChangeAmount(req.getChangeAmount());
        result.setPayTime(now);
        result.setNote(req.getNote());

        List<ResiCashierCollectVo.FeeItem> feeItems = new ArrayList<>();
        for (ResiReceivable r : lockedList) {
            ResiCashierCollectVo.FeeItem item = new ResiCashierCollectVo.FeeItem();
            item.setFeeName(r.getFeeName());
            item.setBillPeriod(r.getBillPeriod());
            item.setAmount(r.getReceivable());
            feeItems.add(item);
        }
        result.setFeeItems(feeItems);

        return result;
    }

    // ==================== S5-01 退款 + 冲红 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResiPayLog refund(ResiCashierRefundReq req) {
        Date now = new Date();
        String userId = String.valueOf(SecurityUtils.getUserId());

        // 1. 查询原收款流水并锁定（FOR UPDATE 通过事务内查询实现）
        ResiPayLog originalLog = payLogMapper.selectById(req.getPayLogId());
        if (originalLog == null) {
            throw new ServiceException("收款流水不存在");
        }

        // 2. 校验原流水状态
        if (!ResiConstants.PAY_TYPE_COLLECT.equals(originalLog.getPayType())) {
            throw new ServiceException("只有收款流水可以退款");
        }
        if (Integer.valueOf(1).equals(originalLog.getIsVerified())) {
            throw new ServiceException("已复核的收款单不可退款");
        }
        // 校验是否已被冲红（存在parent_log_id指向它的WRITEOFF记录）
        if (isWriteOffed(originalLog.getId())) {
            throw new ServiceException("该收款单已被冲红，不可退款");
        }

        // 3. 校验退款金额
        BigDecimal paidAmount = originalLog.getPayAmount() != null ? originalLog.getPayAmount() : BigDecimal.ZERO;
        BigDecimal refundAmount = req.getRefundAmount();
        if (refundAmount.compareTo(paidAmount) > 0) {
            throw new ServiceException("退款金额不能超出已收金额");
        }
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("退款金额必须大于0");
        }

        // 4. 解析关联的应收记录并锁定
        List<String> receivableIds = parseReceivableIds(originalLog.getReceivableIds());
        if (receivableIds.isEmpty()) {
            throw new ServiceException("原收款流水未关联应收记录");
        }

        QueryWrapper<ResiReceivable> lockQw = new QueryWrapper<>();
        lockQw.in("id", receivableIds).last("FOR UPDATE");
        List<ResiReceivable> lockedList = receivableMapper.selectList(lockQw);

        // 押金费用需走押金退款流程，不支持通用退款
        for (ResiReceivable r : lockedList) {
            if (ResiConstants.FEE_TYPE_DEPOSIT.equals(r.getFeeType())) {
                throw new ServiceException("押金费用请通过押金台账进行退款");
            }
        }

        // 5. 更新应收记录（按退款比例或全额回退）
        // 简化逻辑：优先回退最后一条应收的 paid_amount，若退款金额等于收款金额则全部回退
        BigDecimal remainingRefund = refundAmount;
        for (ResiReceivable r : lockedList) {
            BigDecimal rPaid = r.getPaidAmount() != null ? r.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal rReceivable = r.getReceivable() != null ? r.getReceivable() : BigDecimal.ZERO;
            if (rPaid.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal deduct = remainingRefund.min(rPaid);
            BigDecimal newPaid = rPaid.subtract(deduct);
            r.setPaidAmount(newPaid);
            if (newPaid.compareTo(BigDecimal.ZERO) <= 0) {
                r.setPayState(ResiConstants.PAY_STATE_UNPAID);
                r.setPayLogId(null);
                r.setPayTime(null);
            } else if (newPaid.compareTo(rReceivable) < 0) {
                r.setPayState(ResiConstants.PAY_STATE_PART_PAID);
            }
            r.setLastModifyTime(now);
            r.setLastModifyUserId(userId);
            receivableMapper.updateById(r);
            remainingRefund = remainingRefund.subtract(deduct);
            if (remainingRefund.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
        }

        // 6. 生成退款流水
        String refundLogId = java.util.UUID.randomUUID().toString().replace("-", "");
        String refundPayNo = billRuleService.getNumber(ResiConstants.BILL_RULE_RESI_RECEIPT);
        if (StringUtils.isBlank(refundPayNo) || refundPayNo.contains("单据规则不存在")) {
            throw new ServiceException("退款单号生成失败，请检查单据规则配置");
        }

        ResiPayLog refundLog = new ResiPayLog();
        refundLog.setId(refundLogId);
        refundLog.setProjectId(originalLog.getProjectId());
        refundLog.setPayNo(refundPayNo);
        refundLog.setResourceType(originalLog.getResourceType());
        refundLog.setResourceId(originalLog.getResourceId());
        refundLog.setResourceName(originalLog.getResourceName());
        refundLog.setCustomerName(originalLog.getCustomerName());
        refundLog.setPayType(ResiConstants.PAY_TYPE_REFUND);
        refundLog.setPayMethod(req.getRefundMethod());
        refundLog.setReceivableIds(originalLog.getReceivableIds());
        refundLog.setTotalAmount(originalLog.getTotalAmount());
        refundLog.setDiscountAmount(originalLog.getDiscountAmount());
        refundLog.setOverdueAmount(originalLog.getOverdueAmount());
        refundLog.setPrePayAmount(BigDecimal.ZERO);
        refundLog.setPayAmount(refundAmount.negate()); // 退款金额为负，表示支出
        refundLog.setChangeAmount(BigDecimal.ZERO);
        refundLog.setNote(req.getNote());
        refundLog.setParentLogId(originalLog.getId());
        refundLog.setIsVerified(0);
        refundLog.setClient(ResiConstants.CLIENT_B_END);
        refundLog.setCreatorTime(now);
        refundLog.setCreatorUserId(userId);
        payLogMapper.insert(refundLog);

        // 7. 清除 Redis 看板缓存
        try {
            String pattern = "resi:dashboard:" + originalLog.getProjectId() + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("退款后清除看板缓存 projectId={} keys={}", originalLog.getProjectId(), keys.size());
            }
        } catch (Exception e) {
            log.warn("清除看板缓存失败 projectId={} error={}", originalLog.getProjectId(), e.getMessage());
        }

        log.info("退款成功 payLogId={} refundAmount={} refundPayNo={}", req.getPayLogId(), refundAmount, refundPayNo);
        return refundLog;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResiPayLog writeOff(ResiCashierWriteOffReq req) {
        Date now = new Date();
        String userId = String.valueOf(SecurityUtils.getUserId());

        // 1. 查询原收款流水
        ResiPayLog originalLog = payLogMapper.selectById(req.getPayLogId());
        if (originalLog == null) {
            throw new ServiceException("收款流水不存在");
        }

        // 2. 校验原流水状态
        if (!ResiConstants.PAY_TYPE_COLLECT.equals(originalLog.getPayType())) {
            throw new ServiceException("只有收款流水可以冲红");
        }
        if (Integer.valueOf(1).equals(originalLog.getIsVerified())) {
            throw new ServiceException("已复核的收款单不可冲红");
        }
        if (isWriteOffed(originalLog.getId())) {
            throw new ServiceException("该收款单已被冲红，不可重复操作");
        }

        // 3. 解析关联的应收记录并锁定
        List<String> receivableIds = parseReceivableIds(originalLog.getReceivableIds());
        if (!receivableIds.isEmpty()) {
            QueryWrapper<ResiReceivable> lockQw = new QueryWrapper<>();
            lockQw.in("id", receivableIds).last("FOR UPDATE");
            List<ResiReceivable> lockedList = receivableMapper.selectList(lockQw);

            // 4. 回退应收记录状态
            for (ResiReceivable r : lockedList) {
                r.setPayState(ResiConstants.PAY_STATE_UNPAID);
                r.setPaidAmount(BigDecimal.ZERO);
                r.setPayLogId(null);
                r.setPayTime(null);
                r.setLastModifyTime(now);
                r.setLastModifyUserId(userId);
                receivableMapper.updateById(r);
            }
        }

        // 5. 生成对冲流水（金额为负数）
        String writeOffLogId = java.util.UUID.randomUUID().toString().replace("-", "");
        String writeOffPayNo = billRuleService.getNumber(ResiConstants.BILL_RULE_RESI_RECEIPT);
        if (StringUtils.isBlank(writeOffPayNo) || writeOffPayNo.contains("单据规则不存在")) {
            throw new ServiceException("冲红单号生成失败，请检查单据规则配置");
        }

        ResiPayLog writeOffLog = new ResiPayLog();
        writeOffLog.setId(writeOffLogId);
        writeOffLog.setProjectId(originalLog.getProjectId());
        writeOffLog.setPayNo(writeOffPayNo);
        writeOffLog.setResourceType(originalLog.getResourceType());
        writeOffLog.setResourceId(originalLog.getResourceId());
        writeOffLog.setResourceName(originalLog.getResourceName());
        writeOffLog.setCustomerName(originalLog.getCustomerName());
        writeOffLog.setPayType(ResiConstants.PAY_TYPE_WRITEOFF);
        writeOffLog.setPayMethod(originalLog.getPayMethod());
        writeOffLog.setReceivableIds(originalLog.getReceivableIds());
        writeOffLog.setTotalAmount(originalLog.getTotalAmount().negate());
        writeOffLog.setDiscountAmount(originalLog.getDiscountAmount().negate());
        writeOffLog.setOverdueAmount(originalLog.getOverdueAmount().negate());
        writeOffLog.setPrePayAmount(BigDecimal.ZERO);
        writeOffLog.setPayAmount(originalLog.getPayAmount().negate());
        writeOffLog.setChangeAmount(BigDecimal.ZERO);
        writeOffLog.setNote(req.getNote());
        writeOffLog.setParentLogId(originalLog.getId());
        writeOffLog.setIsVerified(0);
        writeOffLog.setClient(ResiConstants.CLIENT_B_END);
        writeOffLog.setCreatorTime(now);
        writeOffLog.setCreatorUserId(userId);
        payLogMapper.insert(writeOffLog);

        // 6. 清除 Redis 看板缓存
        try {
            String pattern = "resi:dashboard:" + originalLog.getProjectId() + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("冲红后清除看板缓存 projectId={} keys={}", originalLog.getProjectId(), keys.size());
            }
        } catch (Exception e) {
            log.warn("清除看板缓存失败 projectId={} error={}", originalLog.getProjectId(), e.getMessage());
        }

        log.info("冲红成功 payLogId={} writeOffPayNo={}", req.getPayLogId(), writeOffPayNo);
        return writeOffLog;
    }

    /**
     * 判断指定收款流水是否已被冲红
     */
    private boolean isWriteOffed(String payLogId) {
        QueryWrapper<ResiPayLog> qw = new QueryWrapper<>();
        qw.eq("parent_log_id", payLogId);
        qw.eq("pay_type", ResiConstants.PAY_TYPE_WRITEOFF);
        return payLogMapper.selectCount(qw) > 0;
    }

    /**
     * 解析JSON格式的应收ID列表
     */
    private List<String> parseReceivableIds(String receivableIdsJson) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isBlank(receivableIdsJson)) {
            return list;
        }
        try {
            list = com.alibaba.fastjson.JSON.parseArray(receivableIdsJson, String.class);
        } catch (Exception e) {
            log.warn("解析receivableIds失败 json={}", receivableIdsJson);
        }
        return list != null ? list : new ArrayList<>();
    }

    // ==================== S6-02 减免滞纳金 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void waiveOverdue(ResiCashierWaiveOverdueReq req) {
        Date now = new Date();
        String userId = String.valueOf(SecurityUtils.getUserId());

        // 1. 查询应收记录
        ResiReceivable receivable = receivableMapper.selectById(req.getReceivableId());
        if (receivable == null) {
            throw new ServiceException("应收记录不存在");
        }
        if (receivable.getDeleteTime() != null) {
            throw new ServiceException("该应收记录已删除");
        }

        // 2. 校验项目权限
        if (!receivable.getProjectId().equals(req.getProjectId())) {
            throw new ServiceException("项目ID不匹配");
        }

        // 3. 已收款记录不允许减免滞纳金
        if (ResiConstants.PAY_STATE_PAID.equals(receivable.getPayState())) {
            throw new ServiceException("已收款的记录不可减免滞纳金");
        }

        // 4. 校验是否有滞纳金可减免
        BigDecimal overdueFee = receivable.getOverdueFee() != null ? receivable.getOverdueFee() : BigDecimal.ZERO;
        if (overdueFee.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("该记录当前滞纳金为0，无需减免");
        }

        // 5. 记录调整前后的值
        String beforeValue = "overdueFee=" + overdueFee + ",receivable=" + receivable.getReceivable();

        // 6. 减免滞纳金：overdue_fee 置 0，重算 receivable
        receivable.setOverdueFee(BigDecimal.ZERO);
        BigDecimal newReceivable = receivable.getTotal()
                .subtract(receivable.getDiscountAmount() != null ? receivable.getDiscountAmount() : BigDecimal.ZERO);
        receivable.setReceivable(newReceivable);
        receivable.setLastModifyTime(now);
        receivable.setLastModifyUserId(userId);
        receivableMapper.updateById(receivable);

        String afterValue = "overdueFee=0.00,receivable=" + receivable.getReceivable();

        // 7. 写入调账记录
        ResiAdjustLog adjustLog = new ResiAdjustLog();
        adjustLog.setProjectId(receivable.getProjectId());
        adjustLog.setReceivableId(req.getReceivableId());
        adjustLog.setAdjustType(ResiConstants.ADJUST_TYPE_OVERDUE_WAIVE);
        adjustLog.setBeforeValue(beforeValue);
        adjustLog.setAfterValue(afterValue);
        adjustLog.setReason(req.getReason());
        adjustLog.setCreatorTime(now);
        adjustLog.setCreatorUserId(userId);
        adjustLogMapper.insert(adjustLog);

        log.info("减免滞纳金成功 receivableId={} projectId={} waivedAmount={}",
                req.getReceivableId(), req.getProjectId(), overdueFee);
    }

    // ==================== 私有方法 ====================

    /**
     * 检查折扣是否适用于所选费用
     */
    private boolean checkDiscountApplicable(ResiDiscount discount, List<ResiReceivable> receivables) {
        String feeScope = discount.getFeeScope();
        if (feeScope == null || feeScope.trim().isEmpty() || "null".equals(feeScope)) {
            return true; // 适用全部
        }
        for (ResiReceivable r : receivables) {
            if (!feeScope.contains(r.getFeeId())) {
                return false;
            }
        }
        return true;
    }

    private ResiCashierRoomSearchVo convertToVo(ResiRoom room) {
        ResiCashierRoomSearchVo vo = new ResiCashierRoomSearchVo();
        vo.setId(room.getId());
        vo.setProjectId(room.getProjectId());
        vo.setBuildingId(room.getBuildingId());
        vo.setRoomNo(room.getRoomNo());
        vo.setRoomAlias(room.getRoomAlias());
        vo.setBuildingArea(room.getBuildingArea());
        vo.setProjectName(room.getProjectName());
        vo.setBuildingName(room.getBuildingName());

        List<ResiCustomerAsset> assets = customerAssetService.lambdaQuery()
                .eq(ResiCustomerAsset::getAssetType, ResiConstants.ASSET_TYPE_ROOM)
                .eq(ResiCustomerAsset::getAssetId, room.getId())
                .eq(ResiCustomerAsset::getIsCurrent, 1)
                .list();
        if (assets != null && !assets.isEmpty()) {
            Long customerId = assets.get(0).getCustomerId();
            ResiCustomer customer = customerService.getById(customerId);
            if (customer != null) {
                vo.setCustomerId(customerId);
                vo.setCustomerName(customer.getCustomerName());
            }
        }
        return vo;
    }
}
