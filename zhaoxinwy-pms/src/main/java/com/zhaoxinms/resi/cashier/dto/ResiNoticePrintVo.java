package com.zhaoxinms.resi.cashier.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 缴费通知单打印数据
 */
public class ResiNoticePrintVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 票据模板配置 */
    private TicketConfig config;

    /** 项目信息 */
    private ProjectInfo project;

    /** 业主/房间信息 */
    private CustomerInfo customer;

    /** 费用明细列表 */
    private List<FeeItem> feeItems;

    /** 合计金额 */
    private BigDecimal totalReceivable;

    /** 通知日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date noticeDate;

    public TicketConfig getConfig() {
        return config;
    }

    public void setConfig(TicketConfig config) {
        this.config = config;
    }

    public ProjectInfo getProject() {
        return project;
    }

    public void setProject(ProjectInfo project) {
        this.project = project;
    }

    public CustomerInfo getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerInfo customer) {
        this.customer = customer;
    }

    public List<FeeItem> getFeeItems() {
        return feeItems;
    }

    public void setFeeItems(List<FeeItem> feeItems) {
        this.feeItems = feeItems;
    }

    public BigDecimal getTotalReceivable() {
        return totalReceivable;
    }

    public void setTotalReceivable(BigDecimal totalReceivable) {
        this.totalReceivable = totalReceivable;
    }

    public Date getNoticeDate() {
        return noticeDate;
    }

    public void setNoticeDate(Date noticeDate) {
        this.noticeDate = noticeDate;
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

        /** 固定备注文字 */
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
     * 项目信息
     */
    public static class ProjectInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 项目ID */
        private Long projectId;

        /** 项目名称 */
        private String name;

        /** 项目地址 */
        private String address;

        /** 联系电话 */
        private String contactPhone;

        /** 负责人姓名 */
        private String managerName;

        /** 负责人电话 */
        private String managerPhone;

        public Long getProjectId() {
            return projectId;
        }

        public void setProjectId(Long projectId) {
            this.projectId = projectId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getContactPhone() {
            return contactPhone;
        }

        public void setContactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
        }

        public String getManagerName() {
            return managerName;
        }

        public void setManagerName(String managerName) {
            this.managerName = managerName;
        }

        public String getManagerPhone() {
            return managerPhone;
        }

        public void setManagerPhone(String managerPhone) {
            this.managerPhone = managerPhone;
        }
    }

    /**
     * 业主/房间信息
     */
    public static class CustomerInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 房间ID */
        private Long roomId;

        /** 资源名称（房间名称） */
        private String resourceName;

        /** 客户姓名 */
        private String customerName;

        /** 联系电话 */
        private String phone;

        public Long getRoomId() {
            return roomId;
        }

        public void setRoomId(Long roomId) {
            this.roomId = roomId;
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

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
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

        /** 备注 */
        private String remark;

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

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
