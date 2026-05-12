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
}
