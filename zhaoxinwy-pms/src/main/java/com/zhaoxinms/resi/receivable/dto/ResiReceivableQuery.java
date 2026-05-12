package com.zhaoxinms.resi.receivable.dto;

import com.zhaoxinms.common.core.page.PageDomain;

/**
 * 应收账单查询参数
 */
public class ResiReceivableQuery extends PageDomain {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long projectId;

    /** 账单月份 */
    private String billPeriod;

    /** 费用定义ID */
    private String feeId;

    /** 缴费状态：0未收 1部分收 2已收 3减免 */
    private String payState;

    /** 资源类型 */
    private String resourceType;

    /** 资源ID */
    private Long resourceId;

    /** 资源名称（模糊） */
    private String resourceName;

    /** 客户姓名（模糊） */
    private String customerName;

    /** 费用类型 */
    private String feeType;

    /** 生成批次号 */
    private String genBatch;

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

    public String getGenBatch() {
        return genBatch;
    }

    public void setGenBatch(String genBatch) {
        this.genBatch = genBatch;
    }
}
