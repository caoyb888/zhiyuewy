package com.zhaoxinms.resi.meter.dto;

import java.math.BigDecimal;

/**
 * 公摊计算明细VO
 *
 * @author zhaoxinms
 */
public class ResiMeterShareDetailVo {

    /** 抄表记录ID */
    private String readingId;

    /** 仪表ID */
    private Long meterId;

    /** 仪表编号 */
    private String meterCode;

    /** 房间ID */
    private Long roomId;

    /** 房间名称 */
    private String roomName;

    /** 建筑面积 */
    private BigDecimal buildingArea;

    /** 原始用量 */
    private BigDecimal rawUsage;

    /** 损耗量 */
    private BigDecimal lossAmount;

    /** 公摊分摊量（计算后） */
    private BigDecimal shareAmount;

    /** 计费用量（计算后） */
    private BigDecimal billedUsage;

    /** 面积占比 */
    private BigDecimal areaRatio;

    public String getReadingId() {
        return readingId;
    }

    public void setReadingId(String readingId) {
        this.readingId = readingId;
    }

    public Long getMeterId() {
        return meterId;
    }

    public void setMeterId(Long meterId) {
        this.meterId = meterId;
    }

    public String getMeterCode() {
        return meterCode;
    }

    public void setMeterCode(String meterCode) {
        this.meterCode = meterCode;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public BigDecimal getBuildingArea() {
        return buildingArea;
    }

    public void setBuildingArea(BigDecimal buildingArea) {
        this.buildingArea = buildingArea;
    }

    public BigDecimal getRawUsage() {
        return rawUsage;
    }

    public void setRawUsage(BigDecimal rawUsage) {
        this.rawUsage = rawUsage;
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

    public BigDecimal getAreaRatio() {
        return areaRatio;
    }

    public void setAreaRatio(BigDecimal areaRatio) {
        this.areaRatio = areaRatio;
    }
}
