package com.zhaoxinms.resi.cashier.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 收款请求
 */
public class ResiCashierCollectReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /** 资源类型 */
    @NotBlank(message = "资源类型不能为空")
    private String resourceType;

    /** 资源ID */
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    /** 应收记录ID列表 */
    @NotEmpty(message = "请至少选择一条应收费用")
    private List<String> receivableIds;

    /** 支付方式 */
    @NotBlank(message = "支付方式不能为空")
    private String payMethod;

    /** 实收金额 */
    @NotNull(message = "实收金额不能为空")
    @DecimalMin(value = "0.01", message = "实收金额必须大于0")
    private BigDecimal payAmount;

    /** 折扣ID（可选） */
    private String discountId;

    /** 找零金额 */
    private BigDecimal changeAmount;

    /** 收据备注 */
    private String note;

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

    public List<String> getReceivableIds() {
        return receivableIds;
    }

    public void setReceivableIds(List<String> receivableIds) {
        this.receivableIds = receivableIds;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public String getDiscountId() {
        return discountId;
    }

    public void setDiscountId(String discountId) {
        this.discountId = discountId;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
