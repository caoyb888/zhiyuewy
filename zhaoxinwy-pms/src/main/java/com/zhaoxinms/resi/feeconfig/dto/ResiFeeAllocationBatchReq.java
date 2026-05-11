package com.zhaoxinms.resi.feeconfig.dto;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 费用批量分配请求DTO
 *
 * @author zhaoxinms
 */
public class ResiFeeAllocationBatchReq {

    /** 所属项目ID */
    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    /** 费用定义ID */
    @NotBlank(message = "费用定义不能为空")
    private String feeId;

    /** 批量方式：BUILDING按楼栋 UNIT按单元 PROJECT全项目 */
    @NotBlank(message = "批量方式不能为空")
    private String batchType;

    /** 楼栋ID（按楼栋/单元时必填） */
    private Long buildingId;

    /** 单元号（按单元时必填） */
    private String unitNo;

    /** 生效日期 */
    @NotNull(message = "生效日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /** 截止日期（NULL表示长期有效） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /** 个性化单价（可选，NULL则使用费用定义默认价） */
    private BigDecimal customPrice;

    /** 个性化公式（可选） */
    private String customFormula;

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

    public String getBatchType() {
        return batchType;
    }

    public void setBatchType(String batchType) {
        this.batchType = batchType;
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
}
