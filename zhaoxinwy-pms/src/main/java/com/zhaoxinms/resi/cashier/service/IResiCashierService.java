package com.zhaoxinms.resi.cashier.service;

import java.util.List;
import java.util.Map;

import com.zhaoxinms.resi.cashier.dto.ResiCashierCalcReq;
import com.zhaoxinms.resi.cashier.dto.ResiCashierCalcVo;
import com.zhaoxinms.resi.cashier.dto.ResiCashierCollectReq;
import com.zhaoxinms.resi.cashier.dto.ResiCashierCollectVo;
import com.zhaoxinms.resi.cashier.dto.ResiCashierRefundReq;
import com.zhaoxinms.resi.cashier.dto.ResiCashierRoomSearchVo;
import com.zhaoxinms.resi.cashier.dto.ResiCashierRoomSummaryVo;
import com.zhaoxinms.resi.cashier.dto.ResiCashierWriteOffReq;
import com.zhaoxinms.resi.finance.entity.ResiPayLog;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;

/**
 * 收银台 Service接口
 */
public interface IResiCashierService {

    /**
     * 模糊搜索房间（按 room_alias / room_no / customer_name）
     */
    List<ResiCashierRoomSearchVo> searchRoom(String keyword, Long projectId);

    /**
     * 查询房间的待缴费用
     */
    List<ResiReceivable> getRoomReceivables(Long roomId, Map<String, String> params);

    /**
     * 查询房间费用汇总
     */
    ResiCashierRoomSummaryVo getRoomSummary(Long roomId);

    /**
     * 收款预览（仅计算不写库）
     */
    ResiCashierCalcVo calc(ResiCashierCalcReq req);

    /**
     * 收款核心（事务+并发保护）
     */
    ResiCashierCollectVo collect(ResiCashierCollectReq req);

    /**
     * 退款（事务保护）
     */
    ResiPayLog refund(ResiCashierRefundReq req);

    /**
     * 冲红（事务保护）
     */
    ResiPayLog writeOff(ResiCashierWriteOffReq req);
}
