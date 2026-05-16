package com.zhaoxinms.resi.finance.dto;

/**
 * 调账记录查询DTO
 */
public class ResiAdjustLogQuery {

    /** 所属项目ID */
    private Long projectId;

    /** 被调整的应收记录ID */
    private String receivableId;

    /** 调整类型 */
    private String adjustType;

    /** 页码 */
    private Integer pageNum;

    /** 每页条数 */
    private Integer pageSize;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getReceivableId() {
        return receivableId;
    }

    public void setReceivableId(String receivableId) {
        this.receivableId = receivableId;
    }

    public String getAdjustType() {
        return adjustType;
    }

    public void setAdjustType(String adjustType) {
        this.adjustType = adjustType;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
