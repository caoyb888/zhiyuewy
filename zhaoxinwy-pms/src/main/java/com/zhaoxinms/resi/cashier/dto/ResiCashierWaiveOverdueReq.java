package com.zhaoxinms.resi.cashier.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 减免滞纳金请求DTO
 */
public class ResiCashierWaiveOverdueReq {

    /** 应收记录ID */
    @NotBlank(message = "应收记录ID不能为空")
    private String receivableId;

    /** 所属项目ID */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /** 减免原因 */
    private String reason;

    public String getReceivableId() {
        return receivableId;
    }

    public void setReceivableId(String receivableId) {
        this.receivableId = receivableId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
