package com.zhaoxinms.resi.cashier.dto;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 收款预览请求
 */
public class ResiCashierCalcReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /** 应收记录ID列表 */
    @NotEmpty(message = "请至少选择一条应收费用")
    private List<String> receivableIds;

    /** 折扣ID（可选） */
    private String discountId;

    /** 是否使用预收款冲抵 */
    private Boolean usePrePay;

    /** 资源类型（冲抵预收款时需要） */
    private String resourceType;

    /** 资源ID（冲抵预收款时需要） */
    private Long resourceId;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<String> getReceivableIds() {
        return receivableIds;
    }

    public void setReceivableIds(List<String> receivableIds) {
        this.receivableIds = receivableIds;
    }

    public String getDiscountId() {
        return discountId;
    }

    public void setDiscountId(String discountId) {
        this.discountId = discountId;
    }

    public Boolean getUsePrePay() {
        return usePrePay;
    }

    public void setUsePrePay(Boolean usePrePay) {
        this.usePrePay = usePrePay;
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
}
