package com.zhaoxinms.resi.finance.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhaoxinms.resi.common.ResiFlowBaseEntity;

/**
 * 押金台账实体
 */
@TableName("resi_deposit")
public class ResiDeposit extends ResiFlowBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属项目ID */
    private Long projectId;

    /** 资源类型：ROOM/PARKING/STORAGE */
    private String resourceType;

    /** 资源ID */
    private Long resourceId;

    /** 资源名称（冗余） */
    private String resourceName;

    /** 押金费用定义ID */
    private String feeId;

    /** 费用名称（冗余） */
    private String feeName;

    /** 缴纳人姓名（冗余） */
    private String customerName;

    /** 押金金额 */
    private BigDecimal amount;

    /** 收款方式 */
    private String payMethod;

    /** 收款单号 */
    private String payNo;

    /** 押金状态：COLLECTED已收 REFUNDED已退 */
    private String state;

    /** 已退还金额 */
    private BigDecimal refundAmount;

    /** 退款方式 */
    private String refundMethod;

    /** 退款单号 */
    private String refundNo;

    /** 退款日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date refundTime;

    /** 备注 */
    private String remark;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getPayNo() {
        return payNo;
    }

    public void setPayNo(String payNo) {
        this.payNo = payNo;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRefundMethod() {
        return refundMethod;
    }

    public void setRefundMethod(String refundMethod) {
        this.refundMethod = refundMethod;
    }

    public String getRefundNo() {
        return refundNo;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }

    public Date getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(Date refundTime) {
        this.refundTime = refundTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
