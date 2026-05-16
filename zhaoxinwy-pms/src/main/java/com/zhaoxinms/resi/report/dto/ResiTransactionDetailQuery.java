package com.zhaoxinms.resi.report.dto;

import com.zhaoxinms.common.core.page.PageDomain;

/**
 * 交易明细报表查询参数
 */
public class ResiTransactionDetailQuery extends PageDomain {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long projectId;

    /** 资源名称（模糊） */
    private String resourceName;

    /** 客户姓名（模糊） */
    private String customerName;

    /** 费用名称（模糊） */
    private String feeName;

    /** 支付方式 */
    private String payMethod;

    /** 操作类型 */
    private String payType;

    /** 收据号（模糊） */
    private String payNo;

    /** 开始日期 */
    private String beginDate;

    /** 结束日期 */
    private String endDate;

    /** 导出标志 */
    private boolean export;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public String getFeeName() {
        return feeName;
    }

    public void setFeeName(String feeName) {
        this.feeName = feeName;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getPayNo() {
        return payNo;
    }

    public void setPayNo(String payNo) {
        this.payNo = payNo;
    }

    public String getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(String beginDate) {
        this.beginDate = beginDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isExport() {
        return export;
    }

    public void setExport(boolean export) {
        this.export = export;
    }
}
