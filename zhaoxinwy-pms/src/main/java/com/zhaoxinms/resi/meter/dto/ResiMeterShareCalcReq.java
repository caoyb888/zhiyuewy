package com.zhaoxinms.resi.meter.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 公摊计算请求DTO
 *
 * @author zhaoxinms
 */
public class ResiMeterShareCalcReq {

    /** 项目ID */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /** 抄表期间，格式 yyyy-MM */
    @NotBlank(message = "抄表期间不能为空")
    @Size(max = 7, message = "抄表期间长度不能超过7个字符")
    private String period;

    /** 公摊组编号（可选，不传则计算该项目该期间所有公摊组） */
    @Size(max = 50, message = "公摊组编号长度不能超过50个字符")
    private String publicGroup;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getPublicGroup() {
        return publicGroup;
    }

    public void setPublicGroup(String publicGroup) {
        this.publicGroup = publicGroup;
    }
}
