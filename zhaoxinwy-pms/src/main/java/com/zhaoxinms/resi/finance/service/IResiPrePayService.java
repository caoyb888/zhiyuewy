package com.zhaoxinms.resi.finance.service;

import java.util.List;

import com.zhaoxinms.resi.finance.dto.ResiPrePayAddReq;
import com.zhaoxinms.resi.finance.dto.ResiPrePayBatchOffsetReq;
import com.zhaoxinms.resi.finance.entity.ResiPreAccount;
import com.zhaoxinms.resi.finance.entity.ResiPrePay;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;

/**
 * 预收款 Service接口
 */
public interface IResiPrePayService {

    /**
     * 收取预收款
     */
    ResiPrePay addPrePay(ResiPrePayAddReq req);

    /**
     * 查询资源的预收款账户列表（含通用+专款）
     */
    List<ResiPreAccount> listAccounts(Long projectId, String resourceType, Long resourceId);

    /**
     * 查询账户流水
     */
    List<ResiPrePay> listPayLogs(String accountId);

    /**
     * 批量冲抵预收款
     */
    void batchOffset(ResiPrePayBatchOffsetReq req);

    /**
     * 收银台收款时自动冲抵预收款（事务内调用）
     * @return 实际冲抵金额
     */
    java.math.BigDecimal offsetForCollect(String payLogId, Long projectId, String resourceType, Long resourceId,
            List<ResiReceivable> receivables, String userId, java.util.Date now);
}
