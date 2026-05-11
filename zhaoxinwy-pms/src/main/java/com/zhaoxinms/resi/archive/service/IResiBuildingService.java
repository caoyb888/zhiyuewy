package com.zhaoxinms.resi.archive.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoxinms.common.core.domain.TreeSelect;
import com.zhaoxinms.resi.archive.entity.ResiBuilding;

/**
 * 楼栋档案 Service接口
 *
 * @author zhaoxinms
 */
public interface IResiBuildingService extends IService<ResiBuilding> {

    /**
     * 查询楼栋列表（含项目名称）
     *
     * @param building 查询条件
     * @return 楼栋列表
     */
    List<ResiBuilding> selectResiBuildingList(ResiBuilding building);

    /**
     * 查询楼栋树形选择数据
     *
     * @param projectId 项目ID（为空则返回所有项目→楼栋的树）
     * @return 树形列表
     */
    List<TreeSelect> selectBuildingTreeSelect(Long projectId);
}
