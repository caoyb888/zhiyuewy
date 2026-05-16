package com.zhaoxinms.resi.archive.dto;

import java.util.List;

import com.zhaoxinms.common.core.page.PageDomain;

/**
 * 房屋过户查询DTO
 *
 * @author zhaoxinms
 */
public class ResiRoomTransferQuery extends PageDomain {

    /** 项目ID */
    private Long projectId;

    /** 房间ID */
    private Long roomId;

    /** 开始日期 */
    private String startDate;

    /** 结束日期 */
    private String endDate;

    /** 项目ID列表（数据隔离注入用） */
    private List<Long> projectIds;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<Long> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(List<Long> projectIds) {
        this.projectIds = projectIds;
    }
}
