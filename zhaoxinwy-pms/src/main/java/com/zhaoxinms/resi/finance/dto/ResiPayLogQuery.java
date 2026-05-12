package com.zhaoxinms.resi.finance.dto;

import com.zhaoxinms.common.core.page.PageDomain;

/**
 * 收款流水查询DTO
 */
public class ResiPayLogQuery extends PageDomain {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long projectId;

    /** 资源类型 */
    private String resourceType;

    /** 资源ID */
    private Long resourceId;

    /** 操作类型：COLLECT/REFUND/WRITEOFF */
    private String payType;

    /** 支付方式 */
    private String payMethod;

    /** 收据号 */
    private String payNo;

    /** 开始时间 */
    private String beginTime;

    /** 结束时间 */
    private String endTime;

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

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getPayNo() {
        return payNo;
    }

    public void setPayNo(String payNo) {
        this.payNo = payNo;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
