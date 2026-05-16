package com.zhaoxinms.resi.finance.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 押金退还请求
 */
public class ResiDepositRefundReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 押金记录ID */
    @NotBlank(message = "押金记录ID不能为空")
    private String depositId;

    /** 退还金额 */
    @NotNull(message = "退还金额不能为空")
    @DecimalMin(value = "0.01", message = "退还金额必须大于0")
    private BigDecimal refundAmount;

    /** 退款方式 */
    @NotBlank(message = "退款方式不能为空")
    private String refundMethod;

    /** 退款备注 */
    private String remark;

    public String getDepositId() {
        return depositId;
    }

    public void setDepositId(String depositId) {
        this.depositId = depositId;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
