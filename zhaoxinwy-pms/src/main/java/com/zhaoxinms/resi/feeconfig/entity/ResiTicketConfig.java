package com.zhaoxinms.resi.feeconfig.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.resi.common.ResiFlowBaseEntity;

/**
 * 票据模板配置实体
 *
 * @author zhaoxinms
 */
@TableName("resi_ticket_config")
public class ResiTicketConfig extends ResiFlowBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属项目ID */
    @NotNull(message = "所属项目不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long projectId;

    /** 票据类型：1收款单 2缴费通知单 */
    @NotNull(message = "票据类型不能为空", groups = {AddGroup.class, EditGroup.class})
    private Integer ticketType;

    /** 票据标题 */
    @Size(max = 200, message = "票据标题长度不能超过200个字符", groups = {AddGroup.class, EditGroup.class})
    private String title;

    /** 收款单位全称 */
    @Size(max = 200, message = "收款单位长度不能超过200个字符", groups = {AddGroup.class, EditGroup.class})
    private String collectOrg;

    /** 纸张规格：A4 / A5 / ROLL */
    @Size(max = 20, message = "纸张规格长度不能超过20个字符", groups = {AddGroup.class, EditGroup.class})
    private String paperSize;

    /** 公司Logo图片路径 */
    @Size(max = 500, message = "Logo路径长度不能超过500个字符", groups = {AddGroup.class, EditGroup.class})
    private String logoUrl;

    /** 公章图片路径 */
    @Size(max = 500, message = "公章路径长度不能超过500个字符", groups = {AddGroup.class, EditGroup.class})
    private String sealUrl;

    /** 收据固定备注文字 */
    @Size(max = 500, message = "备注长度不能超过500个字符", groups = {AddGroup.class, EditGroup.class})
    private String remark;

    /** 表单字段配置，JSON数组字符串 */
    private String fieldConfig;

    /** 有效标志：1有效 0无效 */
    private Integer enabledMark;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Integer getTicketType() {
        return ticketType;
    }

    public void setTicketType(Integer ticketType) {
        this.ticketType = ticketType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCollectOrg() {
        return collectOrg;
    }

    public void setCollectOrg(String collectOrg) {
        this.collectOrg = collectOrg;
    }

    public String getPaperSize() {
        return paperSize;
    }

    public void setPaperSize(String paperSize) {
        this.paperSize = paperSize;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getFieldConfig() {
        return fieldConfig;
    }

    public void setFieldConfig(String fieldConfig) {
        this.fieldConfig = fieldConfig;
    }

    public Integer getEnabledMark() {
        return enabledMark;
    }

    public void setEnabledMark(Integer enabledMark) {
        this.enabledMark = enabledMark;
    }
}
