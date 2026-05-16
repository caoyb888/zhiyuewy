package com.zhaoxinms.resi.receivable.dto;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 临时费录入请求
 */
public class ResiReceivableCreateTempReq {

    /** 项目ID */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /** 资源类型 */
    @NotBlank(message = "资源类型不能为空")
    private String resourceType;

    /** 资源ID */
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    /** 费用定义ID */
    @NotBlank(message = "费用定义不能为空")
    private String feeId;

    /** 计费数量 */
    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0.0000", message = "数量不能为负数")
    private BigDecimal num;

    /** 单价（可覆盖费用定义默认价） */
    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.0000", message = "单价不能为负数")
    private BigDecimal price;

    /** 备注 */
    private String remark;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public String getFeeId() {
        return feeId;
    }

    public void setFeeId(String feeId) {
        this.feeId = feeId;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
