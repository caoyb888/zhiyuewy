package com.zhaoxinms.resi.cashier.dto;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 退款请求DTO
 */
public class ResiCashierRefundReq {

    /** 原收款流水ID */
    @NotBlank(message = "收款流水ID不能为空")
    private String payLogId;

    /** 退款金额 */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal refundAmount;

    /** 退款方式 */
    @NotBlank(message = "退款方式不能为空")
    private String refundMethod;

    /** 退款备注 */
    private String note;

    public String getPayLogId() {
        return payLogId;
    }

    public void setPayLogId(String payLogId) {
        this.payLogId = payLogId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRefundMethod() {
        return refundMethod;
    }

    public void setRefundMethod(String refundMethod) {
        this.refundMethod = refundMethod;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
