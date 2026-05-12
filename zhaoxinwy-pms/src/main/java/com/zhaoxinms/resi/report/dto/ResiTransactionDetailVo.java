package com.zhaoxinms.resi.report.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhaoxinms.common.annotation.Excel;

/**
 * 交易明细报表响应数据
 */
public class ResiTransactionDetailVo {

    private static final long serialVersionUID = 1L;

    /** 收款流水ID */
    private String payLogId;

    /** 收据号 */
    @Excel(name = "收据号", sort = 1, width = 20)
    private String payNo;

    /** 资源名称 */
    @Excel(name = "资源名称", sort = 2, width = 16)
    private String resourceName;

    /** 客户姓名 */
    @Excel(name = "客户姓名", sort = 3, width = 12)
    private String customerName;

    /** 费用名称 */
    @Excel(name = "费用名称", sort = 4, width = 16)
    private String feeName;

    /** 账单月份 */
    @Excel(name = "账单月份", sort = 5, width = 12)
    private String billPeriod;

    /** 操作类型 */
    @Excel(name = "操作类型", sort = 6, width = 10, readConverterExp = "COLLECT=收款,REFUND=退款,WRITEOFF=冲红")
    private String payType;

    /** 支付方式 */
    @Excel(name = "支付方式", sort = 7, width = 12)
    private String payMethod;

    /** 应收合计 */
    @Excel(name = "应收合计", sort = 8, width = 14, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal totalAmount;

    /** 折扣减免 */
    @Excel(name = "折扣减免", sort = 9, width = 14, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal discountAmount;

    /** 滞纳金 */
    @Excel(name = "滞纳金", sort = 10, width = 14, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal overdueAmount;

    /** 预收款冲抵 */
    @Excel(name = "预收款冲抵", sort = 11, width = 14, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal prePayAmount;

    /** 实收金额 */
    @Excel(name = "实收金额", sort = 12, width = 14, align = Excel.Align.RIGHT, isStatistics = true)
    private BigDecimal payAmount;

    /** 收款时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "收款时间", sort = 13, width = 18, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /** 备注 */
    @Excel(name = "备注", sort = 14, width = 20)
    private String note;

    public String getPayLogId() {
        return payLogId;
    }

    public void setPayLogId(String payLogId) {
        this.payLogId = payLogId;
    }

    public String getPayNo() {
        return payNo;
    }

    public void setPayNo(String payNo) {
        this.payNo = payNo;
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

    public String getBillPeriod() {
        return billPeriod;
    }

    public void setBillPeriod(String billPeriod) {
        this.billPeriod = billPeriod;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
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

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
