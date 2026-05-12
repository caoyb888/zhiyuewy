package com.zhaoxinms.resi.cashier.service;

import java.util.List;
import java.util.Map;

import com.zhaoxinms.resi.cashier.dto.ResiCashierRoomSearchVo;
import com.zhaoxinms.resi.cashier.dto.ResiCashierRoomSummaryVo;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;

/**
 * 收银台 Service接口
 */
public interface IResiCashierService {

    /**
     * 模糊搜索房间（按 room_alias / room_no / customer_name）
     *
     * @param keyword   关键词
     * @param projectId 项目ID（可选）
     * @return 房间列表（含当前业主信息）
     */
    List<ResiCashierRoomSearchVo> searchRoom(String keyword, Long projectId);

    /**
     * 查询房间的待缴费用
     *
     * @param roomId 房间ID
     * @param params 筛选参数（feeId/year/period/feeType）
     * @return 应收账单列表
     */
    List<ResiReceivable> getRoomReceivables(Long roomId, Map<String, String> params);

    /**
     * 查询房间费用汇总
     *
     * @param roomId 房间ID
     * @return 汇总数据
     */
    ResiCashierRoomSummaryVo getRoomSummary(Long roomId);
}
