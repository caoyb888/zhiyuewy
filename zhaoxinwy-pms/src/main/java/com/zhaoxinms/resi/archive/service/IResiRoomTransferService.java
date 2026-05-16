package com.zhaoxinms.resi.archive.service;

import java.util.List;

import com.zhaoxinms.resi.archive.dto.ResiRoomTransferQuery;
import com.zhaoxinms.resi.archive.dto.ResiRoomTransferReq;
import com.zhaoxinms.resi.archive.dto.ResiRoomTransferVo;
import com.zhaoxinms.resi.archive.entity.ResiRoomTransfer;

/**
 * 房屋过户记录 Service接口
 *
 * @author zhaoxinms
 */
public interface IResiRoomTransferService {

    /**
     * 执行房屋过户（事务操作）
     *
     * @param req 过户请求
     * @return 过户记录
     */
    ResiRoomTransfer transfer(ResiRoomTransferReq req);

    /**
     * 查询过户记录列表
     *
     * @param query 查询条件
     * @return 过户记录列表
     */
    List<ResiRoomTransferVo> selectTransferList(ResiRoomTransferQuery query);

    /**
     * 查询房间的过户历史
     *
     * @param roomId 房间ID
     * @return 过户记录列表
     */
    List<ResiRoomTransferVo> selectTransferByRoomId(Long roomId);
}
