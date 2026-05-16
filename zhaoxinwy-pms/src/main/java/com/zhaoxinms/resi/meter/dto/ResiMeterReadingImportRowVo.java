package com.zhaoxinms.resi.meter.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 抄表导入预览单行数据VO
 *
 * @author zhaoxinms
 */
public class ResiMeterReadingImportRowVo {

    /** Excel行号（从2开始，1为表头） */
    private int rowNum;

    /** 仪表编号 */
    private String meterCode;

    /** 房间名称 */
    private String roomName;

    /** 抄表期间 */
    private String period;

    /** 上次读数 */
    private BigDecimal lastReading;

    /** 上次抄表日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date lastDate;

    /** 本次读数 */
    private BigDecimal currReading;

    /** 抄表日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date currDate;

    /** 倍率 */
    private BigDecimal multiplier;

    /** 原始用量（预览计算） */
    private BigDecimal rawUsage;

    /** 行类型：NORMAL正常 / WARNING警告 / ERROR错误 */
    private String rowType;

    /** 行提示信息（错误原因或警告内容） */
    private String rowMsg;

    /** 仪表ID（匹配成功后填充） */
    private Long meterId;

    /** 房间ID（匹配成功后填充） */
    private Long roomId;

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
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

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public BigDecimal getRawUsage() {
        return rawUsage;
    }

    public void setRawUsage(BigDecimal rawUsage) {
        this.rawUsage = rawUsage;
    }

    public String getRowType() {
        return rowType;
    }

    public void setRowType(String rowType) {
        this.rowType = rowType;
    }

    public String getRowMsg() {
        return rowMsg;
    }

    public void setRowMsg(String rowMsg) {
        this.rowMsg = rowMsg;
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
}
