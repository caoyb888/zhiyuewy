package com.zhaoxinms.resi.cashier.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 收款单打印数据
 */
public class ResiReceiptPrintVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 票据模板配置 */
    private TicketConfig config;

    /** 收款流水 */
    private PayLogInfo payLog;

    /** 费用明细列表 */
    private List<FeeItem> feeItems;

    /** 操作人信息 */
    private OperatorInfo operator;

    public TicketConfig getConfig() {
        return config;
    }

    public void setConfig(TicketConfig config) {
        this.config = config;
    }

    public PayLogInfo getPayLog() {
        return payLog;
    }

    public void setPayLog(PayLogInfo payLog) {
        this.payLog = payLog;
    }

    public List<FeeItem> getFeeItems() {
        return feeItems;
    }

    public void setFeeItems(List<FeeItem> feeItems) {
        this.feeItems = feeItems;
    }

    public OperatorInfo getOperator() {
        return operator;
    }

    public void setOperator(OperatorInfo operator) {
        this.operator = operator;
    }

    // ==================== 嵌套类 ====================

    /**
     * 票据模板配置（取 resi_ticket_config）
     */
    public static class TicketConfig implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 票据标题 */
        private String title;

        /** 收款单位全称 */
        private String collectOrg;

        /** 纸张规格：A4 / A5 / ROLL */
        private String paperSize;

        /** Logo图片路径 */
        private String logoUrl;

        /** 公章图片路径 */
        private String sealUrl;

        /** 收据固定备注文字 */
        private String remark;

        /** 字段配置 JSON */
        private String fieldConfig;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCollectOrg() {
            return collectOrg;
        }

        public void setCollectOrg(String collectOrg) {
            this.collectOrg = collectOrg;
        }

        public String getPaperSize() {
            return paperSize;
        }

        public void setPaperSize(String paperSize) {
            this.paperSize = paperSize;
        }

        public String getLogoUrl() {
            return logoUrl;
        }

        public void setLogoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
        }

        public String getSealUrl() {
            return sealUrl;
        }

        public void setSealUrl(String sealUrl) {
            this.sealUrl = sealUrl;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getFieldConfig() {
            return fieldConfig;
        }

        public void setFieldConfig(String fieldConfig) {
            this.fieldConfig = fieldConfig;
        }
    }

    /**
     * 收款流水信息
     */
    public static class PayLogInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 流水ID */
        private String payLogId;

        /** 收据号 */
        private String payNo;

        /** 资源名称 */
        private String resourceName;

        /** 客户姓名 */
        private String customerName;

        /** 支付方式 */
        private String payMethod;

        /** 应收合计（含滞纳金，未扣折扣） */
        private BigDecimal totalAmount;

        /** 折扣减免 */
        private BigDecimal discountAmount;

        /** 滞纳金 */
        private BigDecimal overdueAmount;

        /** 预收款冲抵 */
        private BigDecimal prePayAmount;

        /** 实收金额 */
        private BigDecimal payAmount;

        /** 找零金额 */
        private BigDecimal changeAmount;

        /** 收据备注 */
        private String note;

        /** 收款时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date payTime;

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

        public BigDecimal getChangeAmount() {
            return changeAmount;
        }

        public void setChangeAmount(BigDecimal changeAmount) {
            this.changeAmount = changeAmount;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public Date getPayTime() {
            return payTime;
        }

        public void setPayTime(Date payTime) {
            this.payTime = payTime;
        }
    }

    /**
     * 费用明细项
     */
    public static class FeeItem implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 应收记录ID */
        private String receivableId;

        /** 费用名称 */
        private String feeName;

        /** 账单周期 */
        private String billPeriod;

        /** 计费开始日 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date beginDate;

        /** 计费结束日 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date endDate;

        /** 单价 */
        private BigDecimal price;

        /** 数量 */
        private BigDecimal num;

        /** 费用金额 */
        private BigDecimal total;

        /** 滞纳金 */
        private BigDecimal overdueFee;

        /** 折扣减免 */
        private BigDecimal discountAmount;

        /** 应收金额 */
        private BigDecimal receivable;

        public String getReceivableId() {
            return receivableId;
        }

        public void setReceivableId(String receivableId) {
            this.receivableId = receivableId;
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

        public Date getBeginDate() {
            return beginDate;
        }

        public void setBeginDate(Date beginDate) {
            this.beginDate = beginDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getNum() {
            return num;
        }

        public void setNum(BigDecimal num) {
            this.num = num;
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

    /**
     * 操作人信息
     */
    public static class OperatorInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 用户ID */
        private Long userId;

        /** 用户昵称/姓名 */
        private String nickName;

        /** 用户账号 */
        private String userName;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }
}
