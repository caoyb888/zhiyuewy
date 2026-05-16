package com.zhaoxinms.resi.finance.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 收款流水实体
 */
@TableName("resi_pay_log")
public class ResiPayLog {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 所属项目ID */
    private Long projectId;

    /** 收据号/流水号 */
    private String payNo;

    /** 资源类型 */
    private String resourceType;

    /** 资源ID */
    private Long resourceId;

    /** 资源名称（冗余） */
    private String resourceName;

    /** 客户姓名（冗余） */
    private String customerName;

    /** 操作类型：COLLECT收款 REFUND退款 WRITEOFF冲红 */
    private String payType;

    /** 支付方式 */
    private String payMethod;

    /** 本次关联的应收记录ID列表（JSON数组） */
    private String receivableIds;

    /** 本次应收合计 */
    private BigDecimal totalAmount;

    /** 折扣减免金额 */
    private BigDecimal discountAmount;

    /** 本次实收滞纳金 */
    private BigDecimal overdueAmount;

    /** 本次冲抵的预收款金额 */
    private BigDecimal prePayAmount;

    /** 实收金额 */
    private BigDecimal payAmount;

    /** 找零金额 */
    private BigDecimal changeAmount;

    /** 支付凭证号 */
    private String certificate;

    /** 收据备注 */
    private String note;

    /** 使用的折扣ID */
    private String discountId;

    /** 冲红操作时关联的原始收款流水ID */
    private String parentLogId;

    /** 复核状态：0未复核 1已复核 */
    private Integer isVerified;

    /** 复核人账号 */
    private String verifiedBy;

    /** 复核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date verifiedTime;

    /** 操作来源：1B端手工 2C端微信自助 */
    private Integer client;

    /** 创建时间（收款时间） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date creatorTime;

    /** 操作人用户ID */
    private String creatorUserId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getPayNo() {
        return payNo;
    }

    public void setPayNo(String payNo) {
        this.payNo = payNo;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
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

    public String getReceivableIds() {
        return receivableIds;
    }

    public void setReceivableIds(String receivableIds) {
        this.receivableIds = receivableIds;
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

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDiscountId() {
        return discountId;
    }

    public void setDiscountId(String discountId) {
        this.discountId = discountId;
    }

    public String getParentLogId() {
        return parentLogId;
    }

    public void setParentLogId(String parentLogId) {
        this.parentLogId = parentLogId;
    }

    public Integer getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Integer isVerified) {
        this.isVerified = isVerified;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public Date getVerifiedTime() {
        return verifiedTime;
    }

    public void setVerifiedTime(Date verifiedTime) {
        this.verifiedTime = verifiedTime;
    }

    public Integer getClient() {
        return client;
    }

    public void setClient(Integer client) {
        this.client = client;
    }

    public Date getCreatorTime() {
        return creatorTime;
    }

    public void setCreatorTime(Date creatorTime) {
        this.creatorTime = creatorTime;
    }

    public String getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(String creatorUserId) {
        this.creatorUserId = creatorUserId;
    }
}
