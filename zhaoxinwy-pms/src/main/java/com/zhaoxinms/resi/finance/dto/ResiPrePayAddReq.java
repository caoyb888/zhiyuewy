package com.zhaoxinms.resi.finance.dto;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 收取预收款请求DTO
 */
public class ResiPrePayAddReq {

    /** 项目ID */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /** 资源类型 */
    @NotBlank(message = "资源类型不能为空")
    private String resourceType;

    /** 资源ID */
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    /** 资源名称（冗余） */
    private String resourceName;

    /** 专款费用ID，NULL表示通用预收款 */
    private String feeId;

    /** 存入金额 */
    @NotNull(message = "存入金额不能为空")
    @DecimalMin(value = "0.01", message = "存入金额必须大于0")
    private BigDecimal amount;

    /** 支付方式 */
    @NotBlank(message = "支付方式不能为空")
    private String payMethod;

    /** 备注 */
    private String remark;

    /** 操作人ID（后台填充） */
    private String creatorUserId;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getFeeId() {
        return feeId;
    }

    public void setFeeId(String feeId) {
        this.feeId = feeId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(String creatorUserId) {
        this.creatorUserId = creatorUserId;
    }
}
