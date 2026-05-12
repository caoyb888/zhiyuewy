package com.zhaoxinms.resi.finance.dto;

import com.zhaoxinms.common.core.page.PageDomain;

/**
 * 押金台账查询DTO
 */
public class ResiDepositQuery extends PageDomain {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long projectId;

    /** 资源类型 */
    private String resourceType;

    /** 资源ID */
    private Long resourceId;

    /** 押金状态：COLLECTED/REFUNDED */
    private String state;

    /** 费用ID */
    private String feeId;

    /** 收款单号 */
    private String payNo;

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFeeId() {
        return feeId;
    }

    public void setFeeId(String feeId) {
        this.feeId = feeId;
    }

    public String getPayNo() {
        return payNo;
    }

    public void setPayNo(String payNo) {
        this.payNo = payNo;
    }
}
