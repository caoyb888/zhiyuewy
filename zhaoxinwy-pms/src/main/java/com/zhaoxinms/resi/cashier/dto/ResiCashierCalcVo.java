package com.zhaoxinms.resi.cashier.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 收款预览结果
 */
public class ResiCashierCalcVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 应收费用明细 */
    private List<ReceivableItem> items;

    /** 费用金额合计（未扣折扣） */
    private BigDecimal totalAmount;

    /** 滞纳金合计 */
    private BigDecimal overdueAmount;

    /** 折扣减免金额 */
    private BigDecimal discountAmount;

    /** 应收合计（total + overdue - discount） */
    private BigDecimal receivableAmount;

    /** 实收金额 */
    private BigDecimal payAmount;

    /** 使用的折扣ID */
    private String discountId;

    /** 折扣名称 */
    private String discountName;

    /** 预收款冲抵金额 */
    private BigDecimal prePayAmount;

    /** 预收款冲抵后实付金额 */
    private BigDecimal actualPayAmount;

    /** 可用预收款账户列表 */
    private List<PrePayAccount> prePayAccounts;

    public List<ReceivableItem> getItems() {
        return items;
    }

    public void setItems(List<ReceivableItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getOverdueAmount() {
        return overdueAmount;
    }

    public void setOverdueAmount(BigDecimal overdueAmount) {
        this.overdueAmount = overdueAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getReceivableAmount() {
        return receivableAmount;
    }

    public void setReceivableAmount(BigDecimal receivableAmount) {
        this.receivableAmount = receivableAmount;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public String getDiscountId() {
        return discountId;
    }

    public void setDiscountId(String discountId) {
        this.discountId = discountId;
    }

    public String getDiscountName() {
        return discountName;
    }

    public void setDiscountName(String discountName) {
        this.discountName = discountName;
    }

    public BigDecimal getPrePayAmount() {
        return prePayAmount;
    }

    public void setPrePayAmount(BigDecimal prePayAmount) {
        this.prePayAmount = prePayAmount;
    }

    public BigDecimal getActualPayAmount() {
        return actualPayAmount;
    }

    public void setActualPayAmount(BigDecimal actualPayAmount) {
        this.actualPayAmount = actualPayAmount;
    }

    public List<PrePayAccount> getPrePayAccounts() {
        return prePayAccounts;
    }

    public void setPrePayAccounts(List<PrePayAccount> prePayAccounts) {
        this.prePayAccounts = prePayAccounts;
    }

    /**
     * 可用预收款账户
     */
    public static class PrePayAccount implements Serializable {
        private static final long serialVersionUID = 1L;

        private String accountId;
        private String feeId;
        private String feeName;
        private BigDecimal balance;
        private boolean earmark;

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getFeeId() {
            return feeId;
        }

        public void setFeeId(String feeId) {
            this.feeId = feeId;
        }

        public String getFeeName() {
            return feeName;
        }

        public void setFeeName(String feeName) {
            this.feeName = feeName;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public boolean isEarmark() {
            return earmark;
        }

        public void setEarmark(boolean earmark) {
            this.earmark = earmark;
        }
    }

    /**
     * 应收费用明细项
     */
    public static class ReceivableItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private String id;
        private String feeName;
        private String billPeriod;
        private BigDecimal total;
        private BigDecimal overdueFee;
        private BigDecimal discountAmount;
        private BigDecimal receivable;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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
    }
}
