package com.zhaoxinms.resi.finance.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoxinms.base.service.BillRuleService;
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.common.ResiConstants;
import com.zhaoxinms.resi.finance.dto.ResiDepositQuery;
import com.zhaoxinms.resi.finance.dto.ResiDepositRefundReq;
import com.zhaoxinms.resi.finance.entity.ResiDeposit;
import com.zhaoxinms.resi.finance.entity.ResiPayLog;
import com.zhaoxinms.resi.finance.mapper.ResiDepositMapper;
import com.zhaoxinms.resi.finance.mapper.ResiPayLogMapper;
import com.zhaoxinms.resi.finance.service.IResiDepositService;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;

/**
 * 押金台账 Service实现
 */
@Service
public class ResiDepositServiceImpl implements IResiDepositService {

    private static final Logger log = LoggerFactory.getLogger(ResiDepositServiceImpl.class);

    @Autowired
    private ResiDepositMapper depositMapper;

    @Autowired
    private ResiPayLogMapper payLogMapper;

    @Autowired
    private BillRuleService billRuleService;

    @Override
    public List<ResiDeposit> list(ResiDepositQuery query) {
        QueryWrapper<ResiDeposit> qw = new QueryWrapper<>();
        if (query.getProjectId() != null) {
            qw.eq("project_id", query.getProjectId());
        }
        if (StringUtils.isNotBlank(query.getResourceType())) {
            qw.eq("resource_type", query.getResourceType());
        }
        if (query.getResourceId() != null) {
            qw.eq("resource_id", query.getResourceId());
        }
        if (StringUtils.isNotBlank(query.getState())) {
            qw.eq("state", query.getState());
        }
        if (StringUtils.isNotBlank(query.getFeeId())) {
            qw.eq("fee_id", query.getFeeId());
        }
        if (StringUtils.isNotBlank(query.getPayNo())) {
            qw.like("pay_no", query.getPayNo());
        }
        qw.orderByDesc("creator_time");
        return depositMapper.selectList(qw);
    }

    @Override
    public ResiDeposit getById(String id) {
        return depositMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFromCollect(ResiPayLog payLog, List<ResiReceivable> receivables) {
        if (payLog == null || receivables == null || receivables.isEmpty()) {
            return;
        }

        Date now = payLog.getCreatorTime();
        String userId = payLog.getCreatorUserId();

        for (ResiReceivable r : receivables) {
            if (!ResiConstants.FEE_TYPE_DEPOSIT.equals(r.getFeeType())) {
                continue;
            }

            ResiDeposit deposit = new ResiDeposit();
            deposit.setId(java.util.UUID.randomUUID().toString().replace("-", ""));
            deposit.setProjectId(payLog.getProjectId());
            deposit.setResourceType(payLog.getResourceType());
            deposit.setResourceId(payLog.getResourceId());
            deposit.setResourceName(payLog.getResourceName());
            deposit.setFeeId(r.getFeeId());
            deposit.setFeeName(r.getFeeName());
            deposit.setCustomerName(payLog.getCustomerName());
            deposit.setAmount(r.getReceivable() != null ? r.getReceivable() : BigDecimal.ZERO);
            deposit.setPayMethod(payLog.getPayMethod());
            deposit.setPayNo(payLog.getPayNo());
            deposit.setState(ResiConstants.DEPOSIT_STATE_COLLECTED);
            deposit.setRefundAmount(BigDecimal.ZERO);
            deposit.setRemark(r.getRemark());
            deposit.setCreatorTime(now);
            deposit.setCreatorUserId(userId);

            depositMapper.insert(deposit);
            log.info("押金收取成功 depositId={} feeName={} amount={} payNo={}",
                    deposit.getId(), deposit.getFeeName(), deposit.getAmount(), deposit.getPayNo());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResiDeposit refund(ResiDepositRefundReq req, String userId) {
        Date now = new Date();

        ResiDeposit deposit = depositMapper.selectById(req.getDepositId());
        if (deposit == null) {
            throw new ServiceException("押金记录不存在");
        }

        if (ResiConstants.DEPOSIT_STATE_REFUNDED.equals(deposit.getState())) {
            throw new ServiceException("该押金已全额退还，不可重复退款");
        }

        BigDecimal amount = deposit.getAmount() != null ? deposit.getAmount() : BigDecimal.ZERO;
        BigDecimal refunded = deposit.getRefundAmount() != null ? deposit.getRefundAmount() : BigDecimal.ZERO;
        BigDecimal remain = amount.subtract(refunded);

        if (remain.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("该押金已无剩余可退金额");
        }

        BigDecimal refundAmount = req.getRefundAmount();
        if (refundAmount.compareTo(remain) > 0) {
            throw new ServiceException("退款金额超出剩余押金金额，剩余可退：" + remain + " 元");
        }
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("退款金额必须大于0");
        }

        // 生成退款单号
        String refundNo = billRuleService.getNumber(ResiConstants.BILL_RULE_RESI_RECEIPT);
        if (StringUtils.isBlank(refundNo) || refundNo.contains("单据规则不存在")) {
            throw new ServiceException("退款单号生成失败，请检查单据规则配置");
        }

        // 更新押金记录
        BigDecimal newRefundAmount = refunded.add(refundAmount);
        deposit.setRefundAmount(newRefundAmount);
        deposit.setRefundMethod(req.getRefundMethod());
        deposit.setRefundNo(refundNo);
        deposit.setRefundTime(now);
        deposit.setRemark((deposit.getRemark() != null ? deposit.getRemark() + "; " : "") + "退款：" + req.getRemark());
        deposit.setLastModifyTime(now);
        deposit.setLastModifyUserId(userId);

        if (newRefundAmount.compareTo(amount) >= 0) {
            deposit.setState(ResiConstants.DEPOSIT_STATE_REFUNDED);
        }
        depositMapper.updateById(deposit);

        // 写入退款流水（负数的收款流水）
        String refundLogId = java.util.UUID.randomUUID().toString().replace("-", "");
        ResiPayLog refundLog = new ResiPayLog();
        refundLog.setId(refundLogId);
        refundLog.setProjectId(deposit.getProjectId());
        refundLog.setPayNo(refundNo);
        refundLog.setResourceType(deposit.getResourceType());
        refundLog.setResourceId(deposit.getResourceId());
        refundLog.setResourceName(deposit.getResourceName());
        refundLog.setCustomerName(deposit.getCustomerName());
        refundLog.setPayType(ResiConstants.PAY_TYPE_REFUND);
        refundLog.setPayMethod(req.getRefundMethod());
        refundLog.setReceivableIds("[\"" + deposit.getFeeId() + "\"]");
        refundLog.setTotalAmount(BigDecimal.ZERO);
        refundLog.setDiscountAmount(BigDecimal.ZERO);
        refundLog.setOverdueAmount(BigDecimal.ZERO);
        refundLog.setPrePayAmount(BigDecimal.ZERO);
        refundLog.setPayAmount(refundAmount.negate());
        refundLog.setChangeAmount(BigDecimal.ZERO);
        refundLog.setNote("押金退还：" + deposit.getFeeName() + "，" + req.getRemark());
        refundLog.setIsVerified(0);
        refundLog.setClient(ResiConstants.CLIENT_B_END);
        refundLog.setCreatorTime(now);
        refundLog.setCreatorUserId(userId);
        payLogMapper.insert(refundLog);

        log.info("押金退还成功 depositId={} refundAmount={} refundNo={}",
                deposit.getId(), refundAmount, refundNo);
        return deposit;
    }
}
