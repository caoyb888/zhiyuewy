package com.zhaoxinms.resi.archive.entity;

import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.resi.common.ResiBaseEntity;

/**
 * 客户资产绑定实体
 *
 * @author zhaoxinms
 */
@TableName("resi_customer_asset")
public class ResiCustomerAsset extends ResiBaseEntity {

    private static final long serialVersionUID = 1L;

    /** ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 客户ID */
    @NotNull(message = "客户不能为空", groups = {AddGroup.class})
    private Long customerId;

    /** 项目ID */
    @NotNull(message = "项目不能为空", groups = {AddGroup.class})
    private Long projectId;

    /** 资产类型：1房间 2车位 3储藏室 */
    @NotNull(message = "资产类型不能为空", groups = {AddGroup.class})
    private Integer assetType;

    /** 资产ID */
    @NotNull(message = "资产不能为空", groups = {AddGroup.class})
    private Long assetId;

    /** 绑定日期 */
    @NotNull(message = "绑定日期不能为空", groups = {AddGroup.class})
    private java.util.Date bindDate;

    /** 解绑日期（NULL=当前有效） */
    private java.util.Date unbindDate;

    /** 是否当前绑定：1是 */
    private Integer isCurrent;

    /** 客户姓名（非数据库字段） */
    private transient String customerName;

    /** 资产名称（非数据库字段） */
    private transient String assetName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Integer getAssetType() {
        return assetType;
    }

    public void setAssetType(Integer assetType) {
        this.assetType = assetType;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public java.util.Date getBindDate() {
        return bindDate;
    }

    public void setBindDate(java.util.Date bindDate) {
        this.bindDate = bindDate;
    }

    public java.util.Date getUnbindDate() {
        return unbindDate;
    }

    public void setUnbindDate(java.util.Date unbindDate) {
        this.unbindDate = unbindDate;
    }

    public Integer getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Integer isCurrent) {
        this.isCurrent = isCurrent;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
}
