package com.zhaoxinms.resi.meter.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.resi.common.ResiFlowBaseEntity;

/**
 * 抄表记录实体
 *
 * @author zhaoxinms
 */
@TableName("resi_meter_reading")
public class ResiMeterReading extends ResiFlowBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属项目ID */
    @NotNull(message = "所属项目不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long projectId;

    /** 仪表ID */
    @NotNull(message = "仪表不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long meterId;

    /** 所属房间ID（冗余） */
    private Long roomId;

    /** 关联费用定义ID */
    private String feeId;

    /** 抄表期间，格式 yyyy-MM */
    @NotBlank(message = "抄表期间不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 7, message = "抄表期间长度不能超过7个字符", groups = {AddGroup.class, EditGroup.class})
    private String period;

    /** 上次表读数 */
    private BigDecimal lastReading;

    /** 上次抄表日期 */
    private Date lastDate;

    /** 本次表读数 */
    @NotNull(message = "本次读数不能为空", groups = {AddGroup.class, EditGroup.class})
    private BigDecimal currReading;

    /** 本次抄表日期 */
    @NotNull(message = "抄表日期不能为空", groups = {AddGroup.class, EditGroup.class})
    private Date currDate;

    /** 原始用量 = (curr_reading - last_reading) × 倍率 */
    private BigDecimal rawUsage;

    /** 损耗比率 */
    private BigDecimal lossRate;

    /** 损耗量 */
    private BigDecimal lossAmount;

    /** 公摊分摊量 */
    private BigDecimal shareAmount;

    /** 实际计费用量 = raw_usage - loss_amount + share_amount */
    private BigDecimal billedUsage;

    /** 状态：INPUT已录入 BILLED已入账 VERIFIED已复核 */
    private String status;

    /** 批量导入批次号 */
    private String importBatch;

    /** 抄表员用户ID */
    private String readerId;

    /** 入账后对应的应收记录ID */
    private String receivableId;

    // 非数据库字段：仪表编号（用于列表展示）
    private transient String meterCode;

    // 非数据库字段：房间名称（用于列表展示）
    private transient String roomName;

    // 非数据库字段：倍率（用于计算）
    private transient BigDecimal multiplier;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getMeterId() {
        return meterId;
    }

    public void setMeterId(Long meterId) {
        this.meterId = meterId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getFeeId() {
        return feeId;
    }

    public void setFeeId(String feeId) {
        this.feeId = feeId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public BigDecimal getLastReading() {
        return lastReading;
    }

    public void setLastReading(BigDecimal lastReading) {
        this.lastReading = lastReading;
    }

    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    public BigDecimal getCurrReading() {
        return currReading;
    }

    public void setCurrReading(BigDecimal currReading) {
        this.currReading = currReading;
    }

    public Date getCurrDate() {
        return currDate;
    }

    public void setCurrDate(Date currDate) {
        this.currDate = currDate;
    }

    public BigDecimal getRawUsage() {
        return rawUsage;
    }

    public void setRawUsage(BigDecimal rawUsage) {
        this.rawUsage = rawUsage;
    }

    public BigDecimal getLossRate() {
        return lossRate;
    }

    public void setLossRate(BigDecimal lossRate) {
        this.lossRate = lossRate;
    }

    public BigDecimal getLossAmount() {
        return lossAmount;
    }

    public void setLossAmount(BigDecimal lossAmount) {
        this.lossAmount = lossAmount;
    }

    public BigDecimal getShareAmount() {
        return shareAmount;
    }

    public void setShareAmount(BigDecimal shareAmount) {
        this.shareAmount = shareAmount;
    }

    public BigDecimal getBilledUsage() {
        return billedUsage;
    }

    public void setBilledUsage(BigDecimal billedUsage) {
        this.billedUsage = billedUsage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImportBatch() {
        return importBatch;
    }

    public void setImportBatch(String importBatch) {
        this.importBatch = importBatch;
    }

    public String getReaderId() {
        return readerId;
    }

    public void setReaderId(String readerId) {
        this.readerId = readerId;
    }

    public String getReceivableId() {
        return receivableId;
    }

    public void setReceivableId(String receivableId) {
        this.receivableId = receivableId;
    }

    public String getMeterCode() {
        return meterCode;
    }

    public void setMeterCode(String meterCode) {
        this.meterCode = meterCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }
}
