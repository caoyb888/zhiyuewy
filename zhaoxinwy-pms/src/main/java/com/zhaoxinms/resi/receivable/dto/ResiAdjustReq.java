package com.zhaoxinms.resi.receivable.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 调账请求DTO
 */
public class ResiAdjustReq {

    /** 所属项目ID */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /** 调整类型：AMOUNT/PERIOD/STATUS/OVERDUE_WAIVE */
    @NotBlank(message = "调账类型不能为空")
    private String adjustType;

    /** 调整后金额（AMOUNT类型时必填） */
    private BigDecimal newAmount;

    /** 调整后账期（PERIOD类型时必填） */
    private String newPeriod;

    /** 调整后状态（STATUS类型时必填） */
    private String newStatus;

    /** 调整原因 */
    private String reason;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getAdjustType() {
        return adjustType;
    }

    public void setAdjustType(String adjustType) {
        this.adjustType = adjustType;
    }

    public BigDecimal getNewAmount() {
        return newAmount;
    }

    public void setNewAmount(BigDecimal newAmount) {
        this.newAmount = newAmount;
    }

    public String getNewPeriod() {
        return newPeriod;
    }

    public void setNewPeriod(String newPeriod) {
        this.newPeriod = newPeriod;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
