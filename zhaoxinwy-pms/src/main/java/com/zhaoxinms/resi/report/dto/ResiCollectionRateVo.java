package com.zhaoxinms.resi.report.dto;

import java.math.BigDecimal;

import com.zhaoxinms.common.annotation.Excel;

/**
 * 收费率报表响应数据
 */
public class ResiCollectionRateVo {

    private static final long serialVersionUID = 1L;

    /** 账单月份 */
    @Excel(name = "账单月份", sort = 1, width = 14)
    private String billPeriod;

    /** 费用名称 */
    @Excel(name = "费用名称", sort = 2, width = 16)
    private String feeName;

    /** 应收笔数 */
    @Excel(name = "应收笔数", sort = 3, width = 12, isStatistics = true)
    private Integer totalCount;

    /** 应收金额 */
    @Excel(name = "应收金额", sort = 4, width = 16, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal receivableAmount;

    /** 实收金额 */
    @Excel(name = "实收金额", sort = 5, width = 16, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal paidAmount;

    /** 收费率（%） */
    @Excel(name = "收费率(%)", sort = 6, width = 14, align = Excel.Align.RIGHT, suffix = "%")
    private BigDecimal collectionRate;

    /** 未收金额 */
    @Excel(name = "未收金额", sort = 7, width = 16, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal unpaidAmount;

    public String getBillPeriod() {
        return billPeriod;
    }

    public void setBillPeriod(String billPeriod) {
        this.billPeriod = billPeriod;
    }

    public String getFeeName() {
        return feeName;
    }

    public void setFeeName(String feeName) {
        this.feeName = feeName;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public BigDecimal getReceivableAmount() {
        return receivableAmount;
    }

    public void setReceivableAmount(BigDecimal receivableAmount) {
        this.receivableAmount = receivableAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getCollectionRate() {
        return collectionRate;
    }

    public void setCollectionRate(BigDecimal collectionRate) {
        this.collectionRate = collectionRate;
    }

    public BigDecimal getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(BigDecimal unpaidAmount) {
        this.unpaidAmount = unpaidAmount;
    }
}
