package com.zhaoxinms.resi.meter.dto;

import java.util.List;

/**
 * 抄表导入预览结果VO
 *
 * @author zhaoxinms
 */
public class ResiMeterReadingImportPreviewVo {

    /** 批次ID（用于确认导入） */
    private String batchId;

    /** 总行数 */
    private int totalCount;

    /** 正常行数 */
    private int normalCount;

    /** 警告行数 */
    private int warningCount;

    /** 错误行数 */
    private int errorCount;

    /** 预览数据列表 */
    private List<ResiMeterReadingImportRowVo> rows;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getNormalCount() {
        return normalCount;
    }

    public void setNormalCount(int normalCount) {
        this.normalCount = normalCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<ResiMeterReadingImportRowVo> getRows() {
        return rows;
    }

    public void setRows(List<ResiMeterReadingImportRowVo> rows) {
        this.rows = rows;
    }
}
