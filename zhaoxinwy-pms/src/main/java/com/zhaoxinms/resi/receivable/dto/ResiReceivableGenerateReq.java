package com.zhaoxinms.resi.receivable.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 批量生成应收请求
 */
public class ResiReceivableGenerateReq {

    /** 项目ID */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /** 账单月份，格式 yyyy-MM */
    @NotBlank(message = "账单月份不能为空")
    private String billPeriod;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getBillPeriod() {
        return billPeriod;
    }

    public void setBillPeriod(String billPeriod) {
        this.billPeriod = billPeriod;
    }
}
