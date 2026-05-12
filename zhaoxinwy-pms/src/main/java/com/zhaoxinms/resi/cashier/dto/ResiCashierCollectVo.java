package com.zhaoxinms.resi.cashier.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 收款结果（收据数据）
 */
public class ResiCashierCollectVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 收款流水ID */
    private String payLogId;

    /** 收据号 */
    private String payNo;

    /** 资源名称 */
    private String resourceName;

    /** 客户姓名 */
    private String customerName;

    /** 支付方式 */
    private String payMethod;

    /** 应收合计 */
    private BigDecimal totalAmount;

    /** 折扣减免 */
    private BigDecimal discountAmount;

    /** 实收金额 */
    private BigDecimal payAmount;

    /** 找零金额 */
    private BigDecimal changeAmount;

    /** 收款时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /** 收据备注 */
    private String note;

    /** 费用明细 */
    private List<FeeItem> feeItems;

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

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
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

    public List<FeeItem> getFeeItems() {
        return feeItems;
    }

    public void setFeeItems(List<FeeItem> feeItems) {
        this.feeItems = feeItems;
    }

    public static class FeeItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private String feeName;
        private String billPeriod;
        private BigDecimal amount;

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

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}
