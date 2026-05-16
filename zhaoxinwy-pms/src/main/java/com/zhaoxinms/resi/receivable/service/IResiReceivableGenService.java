package com.zhaoxinms.resi.receivable.service;

import com.zhaoxinms.resi.receivable.dto.ResiReceivableCreateTempReq;
import com.zhaoxinms.resi.receivable.dto.ResiReceivableGenerateReq;
import com.zhaoxinms.resi.receivable.dto.ResiReceivableGenerateVo;

/**
 * 应收账单生成 Service接口
 */
public interface IResiReceivableGenService {

    /**
     * 批量生成周期费应收
     *
     * @param req 生成请求
     * @return 生成结果
     */
    ResiReceivableGenerateVo batchGenerate(ResiReceivableGenerateReq req);

    /**
     * 按批次删除未收应收（支持重新生成）
     *
     * @param genBatch 批次号
     * @return 删除数量
     */
    int deleteByGenBatch(String genBatch);

    /**
     * 手动录入临时费应收
     *
     * @param req 临时费请求
     */
    void createTempReceivable(ResiReceivableCreateTempReq req);
}
