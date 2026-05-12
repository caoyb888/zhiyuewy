package com.zhaoxinms.resi.report.dto;

import java.math.BigDecimal;

import com.zhaoxinms.common.annotation.Excel;

/**
 * 欠费明细报表响应数据
 */
public class ResiArrearsDetailVo {

    private static final long serialVersionUID = 1L;

    /** 应收ID */
    private String id;

    /** 资源名称 */
    @Excel(name = "资源名称", sort = 1, width = 16)
    private String resourceName;

    /** 客户姓名 */
    @Excel(name = "客户姓名", sort = 2, width = 12)
    private String customerName;

    /** 费用名称 */
    @Excel(name = "费用名称", sort = 3, width = 16)
    private String feeName;

    /** 费用类型 */
    @Excel(name = "费用类型", sort = 4, width = 10, readConverterExp = "PERIOD=周期费,TEMP=临时费,DEPOSIT=押金,PRE=预收款")
    private String feeType;

    /** 账单月份 */
    @Excel(name = "账单月份", sort = 5, width = 12)
    private String billPeriod;

    /** 数量 */
    @Excel(name = "数量", sort = 6, width = 12, align = Excel.Align.RIGHT)
    private BigDecimal num;

    /** 单价 */
    @Excel(name = "单价", sort = 7, width = 12, align = Excel.Align.RIGHT)
    private BigDecimal price;

    /** 费用金额 */
    @Excel(name = "费用金额", sort = 8, width = 14, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal total;

    /** 滞纳金 */
    @Excel(name = "滞纳金", sort = 9, width = 14, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal overdueFee;

    /** 折扣减免 */
    @Excel(name = "折扣减免", sort = 10, width = 14, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal discountAmount;

    /** 应收合计 */
    @Excel(name = "应收合计", sort = 11, width = 14, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal receivable;

    /** 已收金额 */
    @Excel(name = "已收金额", sort = 12, width = 14, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal paidAmount;

    /** 欠费金额 */
    @Excel(name = "欠费金额", sort = 13, width = 14, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal arrearsAmount;

    /** 备注 */
    @Excel(name = "备注", sort = 14, width = 20)
    private String remark;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public String getBillPeriod() {
        return billPeriod;
    }

    public void setBillPeriod(String billPeriod) {
        this.billPeriod = billPeriod;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getOverdueFee() {
        return overdueFee;
    }

    public void setOverdueFee(BigDecimal overdueFee) {
        this.overdueFee = overdueFee;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getReceivable() {
        return receivable;
    }

    public void setReceivable(BigDecimal receivable) {
        this.receivable = receivable;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getArrearsAmount() {
        return arrearsAmount;
    }

    public void setArrearsAmount(BigDecimal arrearsAmount) {
        this.arrearsAmount = arrearsAmount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
