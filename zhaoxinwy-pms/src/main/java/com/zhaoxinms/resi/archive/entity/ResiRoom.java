package com.zhaoxinms.resi.archive.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.resi.common.ResiBaseEntity;

/**
 * 房间档案实体
 *
 * @author zhaoxinms
 */
@TableName("resi_room")
public class ResiRoom extends ResiBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 房间ID */
    @NotNull(message = "房间ID不能为空", groups = {EditGroup.class})
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属项目ID */
    @NotNull(message = "所属项目不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long projectId;

    /** 所属楼栋ID */
    @NotNull(message = "所属楼栋不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long buildingId;

    /** 单元号 */
    @Size(max = 20, message = "单元号长度不能超过20个字符", groups = {AddGroup.class, EditGroup.class})
    private String unitNo;

    /** 楼层 */
    private Integer floorNo;

    /** 房号 */
    @NotBlank(message = "房号不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 50, message = "房号长度不能超过50个字符", groups = {AddGroup.class, EditGroup.class})
    private String roomNo;

    /** 房间简称（收银台搜索用） */
    @Size(max = 100, message = "房间简称长度不能超过100个字符", groups = {AddGroup.class, EditGroup.class})
    private String roomAlias;

    /** 建筑面积(㎡) */
    private java.math.BigDecimal buildingArea;

    /** 套内面积(㎡) */
    private java.math.BigDecimal innerArea;

    /** 房间类型：1住宅 2商铺 3车库 4储藏室 */
    private Integer roomType;

    /** 附属车库编号 */
    @Size(max = 50, message = "车库编号长度不能超过50个字符", groups = {AddGroup.class, EditGroup.class})
    private String garageNo;

    /** 附属储藏室编号 */
    @Size(max = 50, message = "储藏室编号长度不能超过50个字符", groups = {AddGroup.class, EditGroup.class})
    private String storageNo;

    /** 状态：NORMAL/VACANT/DECORATING/TRANSFERRED */
    @NotBlank(message = "房间状态不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 20, message = "状态长度不能超过20个字符", groups = {AddGroup.class, EditGroup.class})
    private String state;

    /** 备注 */
    @Size(max = 500, message = "备注长度不能超过500个字符", groups = {AddGroup.class, EditGroup.class})
    private String remark;

    /** 项目名称（非数据库字段，联表查询用） */
    @TableField(exist = false)
    private String projectName;

    /** 楼栋名称（非数据库字段，联表查询用） */
    @TableField(exist = false)
    private String buildingName;

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

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public String getUnitNo() {
        return unitNo;
    }

    public void setUnitNo(String unitNo) {
        this.unitNo = unitNo;
    }

    public Integer getFloorNo() {
        return floorNo;
    }

    public void setFloorNo(Integer floorNo) {
        this.floorNo = floorNo;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public String getRoomAlias() {
        return roomAlias;
    }

    public void setRoomAlias(String roomAlias) {
        this.roomAlias = roomAlias;
    }

    public java.math.BigDecimal getBuildingArea() {
        return buildingArea;
    }

    public void setBuildingArea(java.math.BigDecimal buildingArea) {
        this.buildingArea = buildingArea;
    }

    public java.math.BigDecimal getInnerArea() {
        return innerArea;
    }

    public void setInnerArea(java.math.BigDecimal innerArea) {
        this.innerArea = innerArea;
    }

    public Integer getRoomType() {
        return roomType;
    }

    public void setRoomType(Integer roomType) {
        this.roomType = roomType;
    }

    public String getGarageNo() {
        return garageNo;
    }

    public void setGarageNo(String garageNo) {
        this.garageNo = garageNo;
    }

    public String getStorageNo() {
        return storageNo;
    }

    public void setStorageNo(String storageNo) {
        this.storageNo = storageNo;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }
}
