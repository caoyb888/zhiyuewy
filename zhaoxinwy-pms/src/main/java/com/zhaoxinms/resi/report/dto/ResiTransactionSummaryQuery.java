package com.zhaoxinms.resi.report.dto;

import com.zhaoxinms.common.core.page.PageDomain;

/**
 * 交易汇总报表查询参数
 */
public class ResiTransactionSummaryQuery extends PageDomain {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long projectId;

    /** 分组维度：payMethod-按支付方式 feeName-按费用名称 */
    private String groupBy = "payMethod";

    /** 支付方式 */
    private String payMethod;

    /** 费用名称（模糊） */
    private String feeName;

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

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getFeeName() {
        return feeName;
    }

    public void setFeeName(String feeName) {
        this.feeName = feeName;
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
