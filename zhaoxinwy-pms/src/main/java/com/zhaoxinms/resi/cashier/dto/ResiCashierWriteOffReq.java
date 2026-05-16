package com.zhaoxinms.resi.cashier.dto;

import javax.validation.constraints.NotBlank;

/**
 * 冲红请求DTO
 */
public class ResiCashierWriteOffReq {

    /** 原收款流水ID */
    @NotBlank(message = "收款流水ID不能为空")
    private String payLogId;

    /** 冲红备注 */
    private String note;

    public String getPayLogId() {
        return payLogId;
    }

    public void setPayLogId(String payLogId) {
        this.payLogId = payLogId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
