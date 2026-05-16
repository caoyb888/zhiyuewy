package com.zhaoxinms.resi.receivable.dto;

/**
 * 批量生成应收结果
 */
public class ResiReceivableGenerateVo {

    /** 生成批次号 */
    private String genBatch;

    /** 目标总数 */
    private int total;

    /** 成功生成数 */
    private int success;

    /** 跳过数（已存在） */
    private int skip;

    public String getGenBatch() {
        return genBatch;
    }

    public void setGenBatch(String genBatch) {
        this.genBatch = genBatch;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }
}
