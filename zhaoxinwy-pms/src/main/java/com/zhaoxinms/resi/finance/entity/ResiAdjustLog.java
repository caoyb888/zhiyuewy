package com.zhaoxinms.resi.finance.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhaoxinms.resi.common.ResiFlowBaseEntity;

/**
 * 调账记录实体
 *
 * @author zhaoxinms
 */
@TableName("resi_adjust_log")
public class ResiAdjustLog extends ResiFlowBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属项目ID */
    private Long projectId;

    /** 被调整的应收记录ID */
    private String receivableId;

    /** 调整类型：AMOUNT金额 PERIOD账期 STATUS状态 OVERDUE_WAIVE减免滞纳金 */
    private String adjustType;

    /** 调整前的值（字符串快照） */
    private String beforeValue;

    /** 调整后的值 */
    private String afterValue;

    /** 调整原因 */
    private String reason;

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

    public String getBeforeValue() {
        return beforeValue;
    }

    public void setBeforeValue(String beforeValue) {
        this.beforeValue = beforeValue;
    }

    public String getAfterValue() {
        return afterValue;
    }

    public void setAfterValue(String afterValue) {
        this.afterValue = afterValue;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
