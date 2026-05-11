package com.zhaoxinms.resi.archive.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhaoxinms.resi.archive.entity.ResiRoom;

/**
 * 房间档案 Mapper接口
 *
 * @author zhaoxinms
 */
public interface ResiRoomMapper extends BaseMapper<ResiRoom> {

    /**
     * 查询房间列表（含项目名称、楼栋名称）
     */
    List<ResiRoom> selectResiRoomList(ResiRoom room);

    /**
     * 全文检索房间（按 room_alias / room_no 模糊匹配）
     */
    List<ResiRoom> searchRoom(@Param("keyword") String keyword, @Param("projectId") Long projectId);
}
