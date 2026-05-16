package com.zhaoxinms.resi.report.dto;

import com.zhaoxinms.common.core.page.PageDomain;

/**
 * 收费率报表查询参数
 */
public class ResiCollectionRateQuery extends PageDomain {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long projectId;

    /** 费用定义ID */
    private String feeId;

    /** 账单年份 */
    private String year;

    /** 开始月份 */
    private String beginPeriod;

    /** 结束月份 */
    private String endPeriod;

    /** 导出标志 */
    private boolean export;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getFeeId() {
        return feeId;
    }

    public void setFeeId(String feeId) {
        this.feeId = feeId;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getBeginPeriod() {
        return beginPeriod;
    }

    public void setBeginPeriod(String beginPeriod) {
        this.beginPeriod = beginPeriod;
    }

    public String getEndPeriod() {
        return endPeriod;
    }

    public void setEndPeriod(String endPeriod) {
        this.endPeriod = endPeriod;
    }

    public boolean isExport() {
        return export;
    }

    public void setExport(boolean export) {
        this.export = export;
    }
}
