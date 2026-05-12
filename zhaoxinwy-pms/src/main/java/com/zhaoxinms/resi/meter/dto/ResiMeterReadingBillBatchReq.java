package com.zhaoxinms.resi.meter.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 抄表批量入账请求
 *
 * @author zhaoxinms
 */
public class ResiMeterReadingBillBatchReq {

    /** 项目ID */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /** 抄表期间 */
    @NotBlank(message = "抄表期间不能为空")
    private String period;

    /** 指定的抄表记录ID列表（可选，为空则按项目+期间全部入账） */
    private List<String> ids;

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

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
