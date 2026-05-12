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
import com.zhaoxinms.resi.finance.dto.ResiPrePayAddReq;
import com.zhaoxinms.resi.finance.dto.ResiPrePayBatchOffsetReq;
import com.zhaoxinms.resi.finance.entity.ResiPreAccount;
import com.zhaoxinms.resi.finance.entity.ResiPrePay;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition;
import com.zhaoxinms.resi.feeconfig.mapper.ResiFeeDefinitionMapper;
import com.zhaoxinms.resi.finance.mapper.ResiPreAccountMapper;
import com.zhaoxinms.resi.finance.mapper.ResiPrePayMapper;
import com.zhaoxinms.resi.finance.service.IResiPrePayService;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;
import com.zhaoxinms.resi.receivable.mapper.ResiReceivableMapper;

/**
 * 预收款 Service实现
 */
@Service
public class ResiPrePayServiceImpl implements IResiPrePayService {

    private static final Logger log = LoggerFactory.getLogger(ResiPrePayServiceImpl.class);

    @Autowired
    private ResiPreAccountMapper preAccountMapper;

    @Autowired
    private ResiPrePayMapper prePayMapper;

    @Autowired
    private ResiReceivableMapper receivableMapper;

    @Autowired
    private BillRuleService billRuleService;

    @Autowired
    private ResiFeeDefinitionMapper feeDefinitionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResiPrePay addPrePay(ResiPrePayAddReq req) {
        Date now = new Date();
        String userId = req.getCreatorUserId();

        // 1. 查询或创建预收款账户
        QueryWrapper<ResiPreAccount> qw = new QueryWrapper<>();
        qw.eq("resource_type", req.getResourceType())
          .eq("resource_id", req.getResourceId())
          .eq(req.getFeeId() != null, "fee_id", req.getFeeId())
          .isNull(req.getFeeId() == null, "fee_id");
        ResiPreAccount account = preAccountMapper.selectOne(qw);

        if (account == null) {
            account = new ResiPreAccount();
            account.setId(java.util.UUID.randomUUID().toString().replace("-", ""));
            account.setProjectId(req.getProjectId());
            account.setResourceType(req.getResourceType());
            account.setResourceId(req.getResourceId());
            account.setFeeId(req.getFeeId());
            account.setBalance(BigDecimal.ZERO);
            account.setCreateTime(now);
            account.setUpdateTime(now);
            preAccountMapper.insert(account);
        }

        // 2. 更新余额
        BigDecimal addAmount = req.getAmount() != null ? req.getAmount() : BigDecimal.ZERO;
        if (addAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("存入金额必须大于0");
        }
        account.setBalance(account.getBalance().add(addAmount));
        account.setUpdateTime(now);
        preAccountMapper.updateById(account);

        // 3. 生成流水号
        String payNo = billRuleService.getNumber(ResiConstants.BILL_RULE_RESI_RECEIPT);
        if (StringUtils.isBlank(payNo) || payNo.contains("单据规则不存在")) {
            throw new ServiceException("预收款单号生成失败");
        }

        // 4. 写入预收款流水
        ResiPrePay prePay = new ResiPrePay();
        prePay.setId(java.util.UUID.randomUUID().toString().replace("-", ""));
        prePay.setProjectId(req.getProjectId());
        prePay.setAccountId(account.getId());
        prePay.setResourceName(req.getResourceName());
        prePay.setOpType(ResiConstants.PRE_PAY_OP_IN);
        prePay.setAmount(addAmount);
        prePay.setBalanceAfter(account.getBalance());
        prePay.setRefLogId(payNo); // 用收据号作为关联标识
        prePay.setRemark(req.getRemark());
        prePay.setCreatorTime(now);
        prePay.setCreatorUserId(userId);
        prePayMapper.insert(prePay);

        log.info("预收款存入成功 accountId={} amount={} balance={}", account.getId(), addAmount, account.getBalance());
        return prePay;
    }

    @Override
    public List<ResiPreAccount> listAccounts(Long projectId, String resourceType, Long resourceId) {
        QueryWrapper<ResiPreAccount> qw = new QueryWrapper<>();
        qw.eq("project_id", projectId)
          .eq("resource_type", resourceType)
          .eq("resource_id", resourceId)
          .gt("balance", 0)
          .orderByAsc("fee_id"); // 通用(null)在前，专款在后
        List<ResiPreAccount> list = preAccountMapper.selectList(qw);
        // 填充费用名称
        for (ResiPreAccount acc : list) {
            if (StringUtils.isNotBlank(acc.getFeeId())) {
                ResiFeeDefinition feeDef = feeDefinitionMapper.selectById(acc.getFeeId());
                if (feeDef != null) {
                    acc.setFeeName(feeDef.getFeeName());
                }
            }
        }
        return list;
    }

    @Override
    public List<ResiPrePay> listPayLogs(String accountId) {
        QueryWrapper<ResiPrePay> qw = new QueryWrapper<>();
        qw.eq("account_id", accountId).orderByDesc("creator_time");
        return prePayMapper.selectList(qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchOffset(ResiPrePayBatchOffsetReq req) {
        Date now = new Date();
        String userId = req.getCreatorUserId();

        // 逐条处理
        for (ResiPrePayBatchOffsetReq.OffsetItem item : req.getItems()) {
            ResiPreAccount account = preAccountMapper.selectById(item.getAccountId());
            if (account == null) {
                throw new ServiceException("预收款账户不存在");
            }
            if (account.getBalance().compareTo(item.getOffsetAmount()) < 0) {
                throw new ServiceException("预收款余额不足");
            }

            // 更新余额
            account.setBalance(account.getBalance().subtract(item.getOffsetAmount()));
            account.setUpdateTime(now);
            preAccountMapper.updateById(account);

            // 写入流水
            ResiPrePay prePay = new ResiPrePay();
            prePay.setId(java.util.UUID.randomUUID().toString().replace("-", ""));
            prePay.setProjectId(account.getProjectId());
            prePay.setAccountId(account.getId());
            prePay.setOpType(ResiConstants.PRE_PAY_OP_OUT);
            prePay.setAmount(item.getOffsetAmount());
            prePay.setBalanceAfter(account.getBalance());
            prePay.setRemark(item.getRemark());
            prePay.setCreatorTime(now);
            prePay.setCreatorUserId(userId);
            prePayMapper.insert(prePay);

            // 如果有应收ID，更新应收记录
            if (StringUtils.isNotBlank(item.getReceivableId())) {
                ResiReceivable receivable = receivableMapper.selectById(item.getReceivableId());
                if (receivable != null) {
                    BigDecimal newPaid = receivable.getPaidAmount().add(item.getOffsetAmount());
                    receivable.setPaidAmount(newPaid);
                    if (newPaid.compareTo(receivable.getReceivable()) >= 0) {
                        receivable.setPayState(ResiConstants.PAY_STATE_PAID);
                    } else {
                        receivable.setPayState(ResiConstants.PAY_STATE_PART_PAID);
                    }
                    receivable.setLastModifyTime(now);
                    receivable.setLastModifyUserId(userId);
                    receivableMapper.updateById(receivable);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal offsetForCollect(String payLogId, Long projectId, String resourceType, Long resourceId,
            List<ResiReceivable> receivables, String userId, Date now) {
        if (receivables == null || receivables.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // SELECT FOR UPDATE 锁定该资源的所有可用预收款账户
        List<ResiPreAccount> accounts = preAccountMapper.selectListForUpdate(projectId, resourceType, resourceId);
        if (accounts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalOffset = BigDecimal.ZERO;

        // 逐条应收尝试冲抵
        for (ResiReceivable r : receivables) {
            BigDecimal remaining = r.getReceivable().subtract(r.getPaidAmount() != null ? r.getPaidAmount() : BigDecimal.ZERO);
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // 先找专款账户（fee_id匹配），再找通用账户（fee_id为NULL）
            ResiPreAccount matchedAccount = null;
            for (ResiPreAccount acc : accounts) {
                if (acc.getFeeId() != null && acc.getFeeId().equals(r.getFeeId()) && acc.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                    matchedAccount = acc;
                    break;
                }
            }
            if (matchedAccount == null) {
                for (ResiPreAccount acc : accounts) {
                    if (acc.getFeeId() == null && acc.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                        matchedAccount = acc;
                        break;
                    }
                }
            }

            if (matchedAccount == null) {
                continue; // 无可用预收款
            }

            // 计算可冲抵金额
            BigDecimal offsetAmount = remaining.min(matchedAccount.getBalance());

            // 更新账户余额（需要重新查询并锁定，确保事务安全）
            // 由于当前已在事务中，直接更新即可（MyBatis-Plus updateById 是原子操作）
            matchedAccount.setBalance(matchedAccount.getBalance().subtract(offsetAmount));
            matchedAccount.setUpdateTime(now);
            preAccountMapper.updateById(matchedAccount);

            // 写入预收款流水
            ResiPrePay prePay = new ResiPrePay();
            prePay.setId(java.util.UUID.randomUUID().toString().replace("-", ""));
            prePay.setProjectId(projectId);
            prePay.setAccountId(matchedAccount.getId());
            prePay.setOpType(ResiConstants.PRE_PAY_OP_OUT);
            prePay.setAmount(offsetAmount);
            prePay.setBalanceAfter(matchedAccount.getBalance());
            prePay.setRefLogId(payLogId);
            prePay.setRemark("收银台收款自动冲抵");
            prePay.setCreatorTime(now);
            prePay.setCreatorUserId(userId);
            prePayMapper.insert(prePay);

            // 更新应收记录
            BigDecimal newPaid = r.getPaidAmount() != null ? r.getPaidAmount().add(offsetAmount) : offsetAmount;
            r.setPaidAmount(newPaid);
            if (newPaid.compareTo(r.getReceivable()) >= 0) {
                r.setPayState(ResiConstants.PAY_STATE_PAID);
            } else {
                r.setPayState(ResiConstants.PAY_STATE_PART_PAID);
            }
            r.setLastModifyTime(now);
            r.setLastModifyUserId(userId);
            receivableMapper.updateById(r);

            totalOffset = totalOffset.add(offsetAmount);

            // 同步更新 accounts 列表中的余额，避免重复冲抵
            for (ResiPreAccount acc : accounts) {
                if (acc.getId().equals(matchedAccount.getId())) {
                    acc.setBalance(matchedAccount.getBalance());
                    break;
                }
            }
        }

        log.info("预收款自动冲抵完成 payLogId={} totalOffset={}", payLogId, totalOffset);
        return totalOffset;
    }
}
