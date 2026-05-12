package com.zhaoxinms.resi.cashier.service;

import java.util.List;

import com.zhaoxinms.resi.cashier.dto.ResiNoticePrintVo;
import com.zhaoxinms.resi.cashier.dto.ResiReceiptPrintVo;

/**
 * 收银台打印 Service接口
 */
public interface IResiCashierPrintService {

    /**
     * 获取收款单打印数据
     *
     * @param payLogId 收款流水ID
     * @return 结构化收据打印数据
     */
    ResiReceiptPrintVo getReceiptPrintData(String payLogId);

    /**
     * 获取单张缴费通知单打印数据
     *
     * @param receivableId 应收记录ID
     * @return 缴费通知单打印数据
     */
    ResiNoticePrintVo getNoticePrintData(String receivableId);

    /**
     * 批量获取缴费通知单打印数据
     *
     * @param receivableIds 应收记录ID列表
     * @return 缴费通知单打印数据列表（按房间分组）
     */
    List<ResiNoticePrintVo> batchNoticePrintData(List<String> receivableIds);
}
