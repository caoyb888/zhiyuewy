package com.zhaoxinms.resi.cashier.dto;

import java.io.Serializable;

/**
 * 收银台房间搜索结果
 */
public class ResiCashierRoomSearchVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 房间ID */
    private Long id;

    /** 项目ID */
    private Long projectId;

    /** 项目名称 */
    private String projectName;

    /** 楼栋ID */
    private Long buildingId;

    /** 楼栋名称 */
    private String buildingName;

    /** 房间编号 */
    private String roomNo;

    /** 房间简称 */
    private String roomAlias;

    /** 当前业主ID */
    private Long customerId;

    /** 当前业主姓名 */
    private String customerName;

    /** 建筑面积 */
    private java.math.BigDecimal buildingArea;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public String getRoomAlias() {
        return roomAlias;
    }

    public void setRoomAlias(String roomAlias) {
        this.roomAlias = roomAlias;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public java.math.BigDecimal getBuildingArea() {
        return buildingArea;
    }

    public void setBuildingArea(java.math.BigDecimal buildingArea) {
        this.buildingArea = buildingArea;
    }
}
