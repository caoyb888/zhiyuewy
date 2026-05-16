package com.zhaoxinms.resi.report.dto;

import java.math.BigDecimal;

import com.zhaoxinms.common.annotation.Excel;

/**
 * 交易汇总报表响应数据
 */
public class ResiTransactionSummaryVo {

    private static final long serialVersionUID = 1L;

    /** 分组键（支付方式或费用名称） */
    @Excel(name = "分组项", sort = 1, width = 20)
    private String groupKey;

    /** 交易笔数 */
    @Excel(name = "交易笔数", sort = 2, width = 12)
    private Integer transactionCount;

    /** 应收合计 */
    @Excel(name = "应收合计", sort = 3, width = 16, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal totalAmount;

    /** 折扣减免 */
    @Excel(name = "折扣减免", sort = 4, width = 16, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal discountAmount;

    /** 滞纳金 */
    @Excel(name = "滞纳金", sort = 5, width = 16, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal overdueAmount;

    /** 预收款冲抵 */
    @Excel(name = "预收款冲抵", sort = 6, width = 16, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal prePayAmount;

    /** 实收金额 */
    @Excel(name = "实收金额", sort = 7, width = 16, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal payAmount;

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getOverdueAmount() {
        return overdueAmount;
    }

    public void setOverdueAmount(BigDecimal overdueAmount) {
        this.overdueAmount = overdueAmount;
    }

    public BigDecimal getPrePayAmount() {
        return prePayAmount;
    }

    public void setPrePayAmount(BigDecimal prePayAmount) {
        this.prePayAmount = prePayAmount;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }
}
