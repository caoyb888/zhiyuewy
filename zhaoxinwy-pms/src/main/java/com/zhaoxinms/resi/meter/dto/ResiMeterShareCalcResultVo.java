package com.zhaoxinms.resi.meter.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 公摊计算结果汇总VO
 *
 * @author zhaoxinms
 */
public class ResiMeterShareCalcResultVo {

    /** 公摊组编号 */
    private String publicGroup;

    /** 总表用量 */
    private BigDecimal totalUsage;

    /** 分户表用量合计 */
    private BigDecimal subTotalUsage;

    /** 公摊总量 */
    private BigDecimal shareTotal;

    /** 组内房间总面积 */
    private BigDecimal totalArea;

    /** 分户数量 */
    private Integer roomCount;

    /** 公摊明细 */
    private List<ResiMeterShareDetailVo> details;

    public String getPublicGroup() {
        return publicGroup;
    }

    public void setPublicGroup(String publicGroup) {
        this.publicGroup = publicGroup;
    }

    public BigDecimal getTotalUsage() {
        return totalUsage;
    }

    public void setTotalUsage(BigDecimal totalUsage) {
        this.totalUsage = totalUsage;
    }

    public BigDecimal getSubTotalUsage() {
        return subTotalUsage;
    }

    public void setSubTotalUsage(BigDecimal subTotalUsage) {
        this.subTotalUsage = subTotalUsage;
    }

    public BigDecimal getShareTotal() {
        return shareTotal;
    }

    public void setShareTotal(BigDecimal shareTotal) {
        this.shareTotal = shareTotal;
    }

    public BigDecimal getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(BigDecimal totalArea) {
        this.totalArea = totalArea;
    }

    public Integer getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(Integer roomCount) {
        this.roomCount = roomCount;
    }

    public List<ResiMeterShareDetailVo> getDetails() {
        return details;
    }

    public void setDetails(List<ResiMeterShareDetailVo> details) {
        this.details = details;
    }
}
