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
 * 楼栋档案实体
 *
 * @author zhaoxinms
 */
@TableName("resi_building")
public class ResiBuilding extends ResiBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 楼栋ID */
    @NotNull(message = "楼栋ID不能为空", groups = {EditGroup.class})
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属项目ID */
    @NotNull(message = "所属项目不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long projectId;

    /** 楼栋名称 */
    @NotBlank(message = "楼栋名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 100, message = "楼栋名称长度不能超过100个字符", groups = {AddGroup.class, EditGroup.class})
    private String name;

    /** 楼栋编号 */
    @NotBlank(message = "楼栋编号不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 26, message = "楼栋编号长度不能超过26个字符", groups = {AddGroup.class, EditGroup.class})
    private String number;

    /** 总楼层数 */
    private Integer floors;

    /** 项目名称（非数据库字段，联表查询用） */
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Integer getFloors() {
        return floors;
    }

    public void setFloors(Integer floors) {
        this.floors = floors;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
