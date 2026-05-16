package com.zhaoxinms.resi.meter.dto;

/**
 * 抄表导入确认结果VO
 *
 * @author zhaoxinms
 */
public class ResiMeterReadingImportResultVo {

    /** 导入成功数 */
    private int successCount;

    /** 导入失败数 */
    private int failCount;

    /** 跳过数（已存在） */
    private int skipCount;

    /** 提示信息 */
    private String message;

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public int getSkipCount() {
        return skipCount;
    }

    public void setSkipCount(int skipCount) {
        this.skipCount = skipCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
