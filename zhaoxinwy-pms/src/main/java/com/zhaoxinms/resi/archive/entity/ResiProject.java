package com.zhaoxinms.resi.archive.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.resi.common.ResiBaseEntity;

/**
 * 住宅项目（小区）档案实体
 *
 * @author zhaoxinms
 */
@TableName("resi_project")
public class ResiProject extends ResiBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    @NotNull(message = "项目ID不能为空", groups = {EditGroup.class})
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 项目编号 */
    @NotBlank(message = "项目编号不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 50, message = "项目编号长度不能超过50个字符", groups = {AddGroup.class, EditGroup.class})
    private String code;

    /** 项目名称 */
    @NotBlank(message = "项目名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 100, message = "项目名称长度不能超过100个字符", groups = {AddGroup.class, EditGroup.class})
    private String name;

    /** 详细地址 */
    @Size(max = 255, message = "详细地址长度不能超过255个字符", groups = {AddGroup.class, EditGroup.class})
    private String address;

    /** 联系电话 */
    @Size(max = 20, message = "联系电话长度不能超过20个字符", groups = {AddGroup.class, EditGroup.class})
    private String contactPhone;

    /** 负责人姓名 */
    @Size(max = 50, message = "负责人姓名长度不能超过50个字符", groups = {AddGroup.class, EditGroup.class})
    private String managerName;

    /** 负责人电话 */
    @Size(max = 20, message = "负责人电话长度不能超过20个字符", groups = {AddGroup.class, EditGroup.class})
    private String managerPhone;

    /** 项目Logo */
    @Size(max = 500, message = "Logo地址长度不能超过500个字符", groups = {AddGroup.class, EditGroup.class})
    private String logoUrl;

    /** 公章图片（收据打印用） */
    @Size(max = 500, message = "公章图片地址长度不能超过500个字符", groups = {AddGroup.class, EditGroup.class})
    private String sealUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getManagerPhone() {
        return managerPhone;
    }

    public void setManagerPhone(String managerPhone) {
        this.managerPhone = managerPhone;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getSealUrl() {
        return sealUrl;
    }

    public void setSealUrl(String sealUrl) {
        this.sealUrl = sealUrl;
    }
}
