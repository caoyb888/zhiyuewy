package com.zhaoxinms.resi.archive.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.resi.common.ResiBaseEntity;

/**
 * 客户（业主/租户）档案实体
 *
 * @author zhaoxinms
 */
@TableName("resi_customer")
public class ResiCustomer extends ResiBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 客户ID */
    @NotNull(message = "客户ID不能为空", groups = {EditGroup.class})
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属项目ID */
    @NotNull(message = "所属项目不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long projectId;

    /** 客户姓名 */
    @NotBlank(message = "客户姓名不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 100, message = "客户姓名长度不能超过100个字符", groups = {AddGroup.class, EditGroup.class})
    private String customerName;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 20, message = "手机号长度不能超过20个字符", groups = {AddGroup.class, EditGroup.class})
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确", groups = {AddGroup.class, EditGroup.class})
    private String phone;

    /** 身份证号（AES加密存储） */
    @Size(max = 64, message = "身份证号长度不能超过64个字符", groups = {AddGroup.class, EditGroup.class})
    @Pattern(regexp = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)", message = "身份证号格式不正确", groups = {AddGroup.class, EditGroup.class})
    private String idCard;

    /** 性别：0未知 1男 2女 */
    private Integer gender;

    /** 客户类型：1业主 2租户 3临时 */
    private Integer customerType;

    /** 微信openid */
    @Size(max = 128, message = "openid长度不能超过128个字符", groups = {AddGroup.class, EditGroup.class})
    private String openid;

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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getCustomerType() {
        return customerType;
    }

    public void setCustomerType(Integer customerType) {
        this.customerType = customerType;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }
}
