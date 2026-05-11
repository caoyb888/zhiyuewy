package com.zhaoxinms.resi.archive.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhaoxinms.resi.archive.entity.ResiBuilding;

/**
 * 楼栋档案 Mapper接口
 *
 * @author zhaoxinms
 */
public interface ResiBuildingMapper extends BaseMapper<ResiBuilding> {

    /**
     * 查询楼栋列表（含项目名称）
     *
     * @param building 查询条件
     * @return 楼栋列表
     */
    List<ResiBuilding> selectResiBuildingList(ResiBuilding building);
}
