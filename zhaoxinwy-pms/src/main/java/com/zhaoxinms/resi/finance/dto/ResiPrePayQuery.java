package com.zhaoxinms.resi.finance.dto;

import com.zhaoxinms.common.core.page.PageDomain;

/**
 * 预收款流水查询DTO
 */
public class ResiPrePayQuery extends PageDomain {

    private static final long serialVersionUID = 1L;

    /** 预收款账户ID */
    private String accountId;

    /** 操作类型：IN/OUT/REFUND */
    private String opType;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getOpType() {
        return opType;
    }

    public void setOpType(String opType) {
        this.opType = opType;
    }
}
