package com.zhaoxinms.resi.archive.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoxinms.resi.archive.entity.ResiRoom;

/**
 * 房间档案 Service接口
 *
 * @author zhaoxinms
 */
public interface IResiRoomService extends IService<ResiRoom> {

    /**
     * 查询房间列表（含项目名称、楼栋名称）
     */
    List<ResiRoom> selectResiRoomList(ResiRoom room);

    /**
     * 全文检索房间
     *
     * @param keyword   关键词
     * @param projectId 项目ID（可选）
     * @return 房间列表
     */
    List<ResiRoom> searchRoom(String keyword, Long projectId);

    /**
     * 校验房间唯一性（项目+楼栋+单元+房号）
     */
    boolean checkRoomUnique(Long projectId, Long buildingId, String unitNo, String roomNo);

    /**
     * 校验房间唯一性（排除指定ID）
     */
    boolean checkRoomUnique(Long projectId, Long buildingId, String unitNo, String roomNo, Long excludeId);

    /**
     * 自动生成房间简称
     *
     * @param buildingId 楼栋ID
     * @param unitNo     单元号
     * @param roomNo     房号
     * @return 房间简称
     */
    String generateRoomAlias(Long buildingId, String unitNo, String roomNo);
}
