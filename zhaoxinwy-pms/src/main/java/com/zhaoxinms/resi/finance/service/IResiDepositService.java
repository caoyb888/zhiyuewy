package com.zhaoxinms.resi.finance.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoxinms.resi.finance.dto.ResiDepositQuery;
import com.zhaoxinms.resi.finance.dto.ResiDepositRefundReq;
import com.zhaoxinms.resi.finance.entity.ResiDeposit;
import com.zhaoxinms.resi.finance.entity.ResiPayLog;

/**
 * 押金台账 Service接口
 */
public interface IResiDepositService {

    /**
     * 查询押金台账列表
     */
    List<ResiDeposit> list(ResiDepositQuery query);

    /**
     * 根据ID查询押金记录
     */
    ResiDeposit getById(String id);

    /**
     * 收款时自动创建押金记录（fee_type=DEPOSIT）
     */
    void createFromCollect(ResiPayLog payLog, List<com.zhaoxinms.resi.receivable.entity.ResiReceivable> receivables);

    /**
     * 押金退还
     */
    ResiDeposit refund(ResiDepositRefundReq req, String userId);
}
