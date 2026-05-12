package com.zhaoxinms.resi.finance.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 预收款明细流水实体
 */
@TableName("resi_pre_pay")
public class ResiPrePay {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 所属项目ID */
    private Long projectId;

    /** 预收款账户ID */
    private String accountId;

    /** 资源名称（冗余） */
    private String resourceName;

    /** 操作类型：IN存入 OUT冲抵 REFUND退还 */
    private String opType;

    /** 本次操作金额（绝对值，正数） */
    private BigDecimal amount;

    /** 操作后账户余额 */
    private BigDecimal balanceAfter;

    /** 关联收款流水ID */
    private String refLogId;

    /** 备注 */
    private String remark;

    /** 创建时间 */
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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getOpType() {
        return opType;
    }

    public void setOpType(String opType) {
        this.opType = opType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getRefLogId() {
        return refLogId;
    }

    public void setRefLogId(String refLogId) {
        this.refLogId = refLogId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
