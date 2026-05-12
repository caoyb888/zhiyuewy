package com.zhaoxinms.resi.meter.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.zhaoxinms.common.annotation.Excel;
import com.zhaoxinms.common.annotation.Excel.Type;

/**
 * 抄表Excel导入数据VO
 * 用于模板生成和Excel解析
 *
 * @author zhaoxinms
 */
public class ResiMeterReadingImportVo {

    /** 仪表编号 */
    @Excel(name = "仪表编号", sort = 1, prompt = "请填写项目内已存在的仪表编号")
    private String meterCode;

    /** 房间名称（参考，不可编辑） */
    @Excel(name = "房间名称", sort = 2, isExport = true)
    private String roomName;

    /** 抄表期间（yyyy-MM） */
    @Excel(name = "抄表期间", sort = 3, prompt = "格式：2026-05")
    private String period;

    /** 上次读数（参考，自动带入） */
    @Excel(name = "上次读数", sort = 4)
    private BigDecimal lastReading;

    /** 上次抄表日期（参考，自动带入） */
    @Excel(name = "上次抄表日期", sort = 5, dateFormat = "yyyy-MM-dd")
    private Date lastDate;

    /** 本次读数（必填） */
    @Excel(name = "本次读数", sort = 6, prompt = "必填，必须大于等于0")
    private BigDecimal currReading;

    /** 抄表日期（必填） */
    @Excel(name = "抄表日期", sort = 7, dateFormat = "yyyy-MM-dd", prompt = "必填，格式：2026-05-10")
    private Date currDate;

    /** 倍率（参考，自动带入） */
    @Excel(name = "倍率", sort = 8)
    private BigDecimal multiplier;

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
}
