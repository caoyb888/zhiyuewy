package com.zhaoxinms.resi.cashier.service;

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
}
