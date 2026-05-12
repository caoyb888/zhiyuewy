package com.zhaoxinms.resi.feeconfig.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 折扣配置实体
 */
@TableName("resi_discount")
public class ResiDiscount {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 所属项目ID */
    private Long projectId;

    /** 折扣名称 */
    private String discountName;

    /** 折扣比例：0.9500表示95折 */
    private BigDecimal discountRate;

    /** 适用费用ID列表（JSON数组），NULL表示适用全部 */
    private String feeScope;

    /** 有效期开始 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date validStart;

    /** 有效期截止 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date validEnd;

    /** 有效标志 */
    private Integer enabledMark;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

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

    public String getDiscountName() {
        return discountName;
    }

    public void setDiscountName(String discountName) {
        this.discountName = discountName;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public String getFeeScope() {
        return feeScope;
    }

    public void setFeeScope(String feeScope) {
        this.feeScope = feeScope;
    }

    public Date getValidStart() {
        return validStart;
    }

    public void setValidStart(Date validStart) {
        this.validStart = validStart;
    }

    public Date getValidEnd() {
        return validEnd;
    }

    public void setValidEnd(Date validEnd) {
        this.validEnd = validEnd;
    }

    public Integer getEnabledMark() {
        return enabledMark;
    }

    public void setEnabledMark(Integer enabledMark) {
        this.enabledMark = enabledMark;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
