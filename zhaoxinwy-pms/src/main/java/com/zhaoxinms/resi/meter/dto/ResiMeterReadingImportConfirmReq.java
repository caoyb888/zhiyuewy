package com.zhaoxinms.resi.meter.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 抄表导入确认请求DTO
 *
 * @author zhaoxinms
 */
public class ResiMeterReadingImportConfirmReq {

    /** 批次ID */
    @NotBlank(message = "批次ID不能为空")
    private String batchId;

    /** 所属项目ID */
    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
