package com.zhaoxinms.resi.receivable.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhaoxinms.resi.common.ResiFlowBaseEntity;

/**
 * 应收账单实体
 *
 * @author zhaoxinms
 */
@TableName("resi_receivable")
public class ResiReceivable extends ResiFlowBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属项目ID */
    private Long projectId;

    /** 资源类型：ROOM房间 PARKING车位 STORAGE储藏室 */
    private String resourceType;

    /** 资源ID */
    private Long resourceId;

    /** 资源名称（冗余） */
    private String resourceName;

    /** 客户ID（冗余） */
    private Long customerId;

    /** 客户姓名（冗余） */
    private String customerName;

    /** 费用定义ID */
    private String feeId;

    /** 费用名称（冗余） */
    private String feeName;

    /** 费用类型：PERIOD周期 TEMP临时 DEPOSIT押金 PRE预收 */
    private String feeType;

    /** 账单月份，格式 yyyy-MM */
    private String billPeriod;

    /** 计费周期开始日 */
    private Date beginDate;

    /** 计费周期结束日 */
    private Date endDate;

    /** 计费数量（面积/用量/次数） */
    private BigDecimal num;

    /** 单价（元） */
    private BigDecimal price;

    /** 费用金额 = num × price */
    private BigDecimal total;

    /** 当前累计滞纳金 */
    private BigDecimal overdueFee;

    /** 使用的折扣ID */
    private String discountId;

    /** 折扣减免金额 */
    private BigDecimal discountAmount;

    /** 应收合计 = total + overdue_fee - discount_amount */
    private BigDecimal receivable;

    /** 缴费状态：0未收 1部分收 2已收 3减免 */
    private String payState;

    /** 已实收金额 */
    private BigDecimal paidAmount;

    /** 最后一次收款流水ID */
    private String payLogId;

    /** 最后收款时间 */
    private Date payTime;

    /** 批量生成批次号 */
    private String genBatch;

    /** 来源抄表记录ID */
    private String meterReadingId;

    /** 备注 */
    private String remark;

    /** 有效标志 */
    private Integer enabledMark;

    /** 软删除时间 */
    private Date deleteTime;

    /** 执行软删除的用户ID */
    private String deleteUserId;

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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getDiscountId() {
        return discountId;
    }

    public void setDiscountId(String discountId) {
        this.discountId = discountId;
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

    public String getPayState() {
        return payState;
    }

    public void setPayState(String payState) {
        this.payState = payState;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getPayLogId() {
        return payLogId;
    }

    public void setPayLogId(String payLogId) {
        this.payLogId = payLogId;
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public String getGenBatch() {
        return genBatch;
    }

    public void setGenBatch(String genBatch) {
        this.genBatch = genBatch;
    }

    public String getMeterReadingId() {
        return meterReadingId;
    }

    public void setMeterReadingId(String meterReadingId) {
        this.meterReadingId = meterReadingId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getEnabledMark() {
        return enabledMark;
    }

    public void setEnabledMark(Integer enabledMark) {
        this.enabledMark = enabledMark;
    }

    public Date getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getDeleteUserId() {
        return deleteUserId;
    }

    public void setDeleteUserId(String deleteUserId) {
        this.deleteUserId = deleteUserId;
    }
}
