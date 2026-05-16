package com.zhaoxinms.resi.archive.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhaoxinms.resi.archive.entity.ResiRoomTransfer;

/**
 * 房屋过户记录 Mapper接口
 *
 * @author zhaoxinms
 */
public interface ResiRoomTransferMapper extends BaseMapper<ResiRoomTransfer> {

    /**
     * 查询过户记录列表（含联表信息）
     */
    List<ResiRoomTransfer> selectTransferList(@Param("projectId") Long projectId,
                                               @Param("projectIds") List<Long> projectIds,
                                               @Param("roomId") Long roomId,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    /**
     * 查询房间的过户历史
     */
    List<ResiRoomTransfer> selectTransferByRoomId(@Param("roomId") Long roomId);
}
