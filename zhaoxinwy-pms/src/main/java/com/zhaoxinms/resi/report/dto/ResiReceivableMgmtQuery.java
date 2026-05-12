package com.zhaoxinms.resi.report.dto;

import com.zhaoxinms.common.core.page.PageDomain;

/**
 * 应收管理报表查询参数
 */
public class ResiReceivableMgmtQuery extends PageDomain {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long projectId;

    /** 账单月份 */
    private String billPeriod;

    /** 费用定义ID */
    private String feeId;

    /** 缴费状态 */
    private String payState;

    /** 资源名称（模糊） */
    private String resourceName;

    /** 客户姓名（模糊） */
    private String customerName;

    /** 费用类型 */
    private String feeType;

    /** 导出标志 */
    private boolean export;

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

    public String getFeeId() {
        return feeId;
    }

    public void setFeeId(String feeId) {
        this.feeId = feeId;
    }

    public String getPayState() {
        return payState;
    }

    public void setPayState(String payState) {
        this.payState = payState;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public boolean isExport() {
        return export;
    }

    public void setExport(boolean export) {
        this.export = export;
    }
}
