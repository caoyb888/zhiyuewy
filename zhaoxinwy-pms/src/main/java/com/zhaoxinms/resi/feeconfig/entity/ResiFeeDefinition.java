package com.zhaoxinms.resi.feeconfig.entity;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.resi.common.ResiFlowBaseEntity;

/**
 * 费用定义实体
 *
 * @author zhaoxinms
 */
@TableName("resi_fee_definition")
public class ResiFeeDefinition extends ResiFlowBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属项目ID */
    @NotNull(message = "所属项目不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long projectId;

    /** 费用名称 */
    @NotBlank(message = "费用名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 100, message = "费用名称长度不能超过100个字符", groups = {AddGroup.class, EditGroup.class})
    private String feeName;

    /** 费用编码 */
    @NotBlank(message = "费用编码不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 50, message = "费用编码长度不能超过50个字符", groups = {AddGroup.class, EditGroup.class})
    private String feeCode;

    /** 费用类型：PERIOD周期费 TEMP临时费 DEPOSIT押金 PRE预收款 */
    @NotBlank(message = "费用类型不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 20, message = "费用类型长度不能超过20个字符", groups = {AddGroup.class, EditGroup.class})
    private String feeType;

    /** 计费方式：FIXED固定金额 AREA按面积 USAGE按用量 FORMULA自定义公式 */
    @NotBlank(message = "计费方式不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 20, message = "计费方式长度不能超过20个字符", groups = {AddGroup.class, EditGroup.class})
    private String calcType;

    /** 单价（元） */
    @DecimalMin(value = "0.0000", message = "单价不能为负数", groups = {AddGroup.class, EditGroup.class})
    private BigDecimal unitPrice;

    /** 自定义计费公式 */
    private String formula;

    /** 计费周期单位：MONTH月 QUARTER季 YEAR年 */
    @Size(max = 10, message = "周期单位长度不能超过10个字符", groups = {AddGroup.class, EditGroup.class})
    private String cycleUnit;

    /** 周期数 */
    private Integer cycleValue;

    /** 是否启用滞纳金：0否 1是 */
    private Integer overdueEnable;

    /** 逾期起算天数 */
    private Integer overdueDays;

    /** 滞纳金计算类型：DAY按天 MONTH按月 */
    @Size(max = 10, message = "滞纳金计算类型长度不能超过10个字符", groups = {AddGroup.class, EditGroup.class})
    private String overdueType;

    /** 滞纳金利率 */
    @DecimalMin(value = "0.000000", message = "滞纳金利率不能为负数", groups = {AddGroup.class, EditGroup.class})
    private BigDecimal overdueRate;

    /** 滞纳金累计上限 */
    private BigDecimal overdueMax;

    /** 金额取整方式：ROUND四舍五入 CEIL向上取整 FLOOR截尾 */
    @Size(max = 10, message = "取整方式长度不能超过10个字符", groups = {AddGroup.class, EditGroup.class})
    private String roundType;

    /** 专款专冲标记：0否 1是 */
    private Integer earmarkEnable;

    /** 发票项目名称 */
    @Size(max = 200, message = "发票项目名称长度不能超过200个字符", groups = {AddGroup.class, EditGroup.class})
    private String invoiceTitle;

    /** 增值税税率（%） */
    @DecimalMin(value = "0.00", message = "税率不能为负数", groups = {AddGroup.class, EditGroup.class})
    @DecimalMax(value = "100.00", message = "税率不能超过100%", groups = {AddGroup.class, EditGroup.class})
    private BigDecimal taxRate;

    /** 排序码 */
    private Integer sortCode;

    /** 有效标志：1有效 0无效 */
    private Integer enabledMark;

    /** 软删除时间 */
    private java.util.Date deleteTime;

    /** 执行软删除的用户ID */
    private String deleteUserId;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public String getCalcType() {
        return calcType;
    }

    public void setCalcType(String calcType) {
        this.calcType = calcType;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getCycleUnit() {
        return cycleUnit;
    }

    public void setCycleUnit(String cycleUnit) {
        this.cycleUnit = cycleUnit;
    }

    public Integer getCycleValue() {
        return cycleValue;
    }

    public void setCycleValue(Integer cycleValue) {
        this.cycleValue = cycleValue;
    }

    public Integer getOverdueEnable() {
        return overdueEnable;
    }

    public void setOverdueEnable(Integer overdueEnable) {
        this.overdueEnable = overdueEnable;
    }

    public Integer getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(Integer overdueDays) {
        this.overdueDays = overdueDays;
    }

    public String getOverdueType() {
        return overdueType;
    }

    public void setOverdueType(String overdueType) {
        this.overdueType = overdueType;
    }

    public BigDecimal getOverdueRate() {
        return overdueRate;
    }

    public void setOverdueRate(BigDecimal overdueRate) {
        this.overdueRate = overdueRate;
    }

    public BigDecimal getOverdueMax() {
        return overdueMax;
    }

    public void setOverdueMax(BigDecimal overdueMax) {
        this.overdueMax = overdueMax;
    }

    public String getRoundType() {
        return roundType;
    }

    public void setRoundType(String roundType) {
        this.roundType = roundType;
    }

    public Integer getEarmarkEnable() {
        return earmarkEnable;
    }

    public void setEarmarkEnable(Integer earmarkEnable) {
        this.earmarkEnable = earmarkEnable;
    }

    public String getInvoiceTitle() {
        return invoiceTitle;
    }

    public void setInvoiceTitle(String invoiceTitle) {
        this.invoiceTitle = invoiceTitle;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public Integer getSortCode() {
        return sortCode;
    }

    public void setSortCode(Integer sortCode) {
        this.sortCode = sortCode;
    }

    public Integer getEnabledMark() {
        return enabledMark;
    }

    public void setEnabledMark(Integer enabledMark) {
        this.enabledMark = enabledMark;
    }

    public java.util.Date getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(java.util.Date deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getDeleteUserId() {
        return deleteUserId;
    }

    public void setDeleteUserId(String deleteUserId) {
        this.deleteUserId = deleteUserId;
    }
}
