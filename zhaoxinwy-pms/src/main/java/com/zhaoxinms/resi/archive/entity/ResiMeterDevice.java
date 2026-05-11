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
 * 仪表档案实体
 *
 * @author zhaoxinms
 */
@TableName("resi_meter_device")
public class ResiMeterDevice extends ResiBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 仪表ID */
    @NotNull(message = "仪表ID不能为空", groups = {EditGroup.class})
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属项目ID */
    @NotNull(message = "所属项目不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long projectId;

    /** 所属房间ID（公摊表可为NULL） */
    private Long roomId;

    /** 仪表编号 */
    @NotBlank(message = "仪表编号不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 50, message = "仪表编号长度不能超过50个字符", groups = {AddGroup.class, EditGroup.class})
    private String meterCode;

    /** 仪表类型：1水表 2电表 3燃气表 4暖气表 */
    @NotNull(message = "仪表类型不能为空", groups = {AddGroup.class, EditGroup.class})
    private Integer meterType;

    /** 安装日期 */
    private java.util.Date installDate;

    /** 初始读数 */
    private java.math.BigDecimal initReading;

    /** 倍率 */
    private java.math.BigDecimal multiplier;

    /** 是否公摊总表：0分户表 1公摊总表 */
    private Integer isPublic;

    /** 公摊组编号 */
    @Size(max = 50, message = "公摊组编号长度不能超过50个字符", groups = {AddGroup.class, EditGroup.class})
    private String publicGroup;

    /** 房间名称（非数据库字段） */
    @TableField(exist = false)
    private String roomName;

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

    public String getMeterCode() {
        return meterCode;
    }

    public void setMeterCode(String meterCode) {
        this.meterCode = meterCode;
    }

    public Integer getMeterType() {
        return meterType;
    }

    public void setMeterType(Integer meterType) {
        this.meterType = meterType;
    }

    public java.util.Date getInstallDate() {
        return installDate;
    }

    public void setInstallDate(java.util.Date installDate) {
        this.installDate = installDate;
    }

    public java.math.BigDecimal getInitReading() {
        return initReading;
    }

    public void setInitReading(java.math.BigDecimal initReading) {
        this.initReading = initReading;
    }

    public java.math.BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(java.math.BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public Integer getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Integer isPublic) {
        this.isPublic = isPublic;
    }

    public String getPublicGroup() {
        return publicGroup;
    }

    public void setPublicGroup(String publicGroup) {
        this.publicGroup = publicGroup;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
