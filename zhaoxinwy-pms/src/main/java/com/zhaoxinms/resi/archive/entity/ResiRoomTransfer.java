package com.zhaoxinms.resi.archive.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 房屋过户记录实体
 *
 * @author zhaoxinms
 */
@TableName("resi_room_transfer")
public class ResiRoomTransfer {

    private static final long serialVersionUID = 1L;

    /** 过户记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属项目ID */
    private Long projectId;

    /** 房间ID */
    private Long roomId;

    /** 原业主客户ID */
    private Long oldCustomerId;

    /** 新业主客户ID */
    private Long newCustomerId;

    /** 过户生效日期 */
    private Date transferDate;

    /** 过户备注 */
    private String transferRemark;

    /** 操作员账号 */
    private String operator;

    /** 记录创建时间 */
    private Date createTime;

    /** 房间名称（非数据库字段） */
    @TableField(exist = false)
    private String roomName;

    /** 原业主姓名（非数据库字段） */
    @TableField(exist = false)
    private String oldCustomerName;

    /** 新业主姓名（非数据库字段） */
    @TableField(exist = false)
    private String newCustomerName;

    /** 项目名称（非数据库字段） */
    @TableField(exist = false)
    private String projectName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getOldCustomerId() {
        return oldCustomerId;
    }

    public void setOldCustomerId(Long oldCustomerId) {
        this.oldCustomerId = oldCustomerId;
    }

    public Long getNewCustomerId() {
        return newCustomerId;
    }

    public void setNewCustomerId(Long newCustomerId) {
        this.newCustomerId = newCustomerId;
    }

    public Date getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(Date transferDate) {
        this.transferDate = transferDate;
    }

    public String getTransferRemark() {
        return transferRemark;
    }

    public void setTransferRemark(String transferRemark) {
        this.transferRemark = transferRemark;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getOldCustomerName() {
        return oldCustomerName;
    }

    public void setOldCustomerName(String oldCustomerName) {
        this.oldCustomerName = oldCustomerName;
    }

    public String getNewCustomerName() {
        return newCustomerName;
    }

    public void setNewCustomerName(String newCustomerName) {
        this.newCustomerName = newCustomerName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
