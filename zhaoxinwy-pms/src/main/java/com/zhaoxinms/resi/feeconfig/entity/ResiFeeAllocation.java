package com.zhaoxinms.resi.feeconfig.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.resi.common.ResiFlowBaseEntity;

/**
 * 费用分配实体
 *
 * @author zhaoxinms
 */
@TableName("resi_fee_allocation")
public class ResiFeeAllocation extends ResiFlowBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属项目ID */
    @NotNull(message = "所属项目不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long projectId;

    /** 费用定义ID */
    @NotBlank(message = "费用定义不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 50, message = "费用定义ID长度不能超过50个字符", groups = {AddGroup.class, EditGroup.class})
    private String feeId;

    /** 资源类型：ROOM房间 PARKING车位 STORAGE储藏室 */
    @NotBlank(message = "资源类型不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 20, message = "资源类型长度不能超过20个字符", groups = {AddGroup.class, EditGroup.class})
    private String resourceType;

    /** 资源ID */
    @NotNull(message = "资源ID不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long resourceId;

    /** 资源名称（冗余） */
    @Size(max = 100, message = "资源名称长度不能超过100个字符", groups = {AddGroup.class, EditGroup.class})
    private String resourceName;

    /** 个性化单价 */
    private BigDecimal customPrice;

    /** 个性化公式 */
    private String customFormula;

    /** 生效日期 */
    @NotNull(message = "生效日期不能为空", groups = {AddGroup.class, EditGroup.class})
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /** 截止日期（NULL表示长期有效） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /** 有效标志：1有效 0无效 */
    private Integer enabledMark;

    /** 费用名称（非数据库字段，查询展示用） */
    private transient String feeName;

    /** 费用编码（非数据库字段，查询展示用） */
    private transient String feeCode;

    /** 项目名（非数据库字段） */
    private transient String projectName;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getFeeId() {
        return feeId;
    }

    public void setFeeId(String feeId) {
        this.feeId = feeId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public BigDecimal getCustomPrice() {
        return customPrice;
    }

    public void setCustomPrice(BigDecimal customPrice) {
        this.customPrice = customPrice;
    }

    public String getCustomFormula() {
        return customFormula;
    }

    public void setCustomFormula(String customFormula) {
        this.customFormula = customFormula;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getEnabledMark() {
        return enabledMark;
    }

    public void setEnabledMark(Integer enabledMark) {
        this.enabledMark = enabledMark;
    }

    public String getFeeName() {
        return feeName;
    }

    public void setFeeName(String feeName) {
        this.feeName = feeName;
    }

    public String getFeeCode() {
        return feeCode;
    }

    public void setFeeCode(String feeCode) {
        this.feeCode = feeCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
