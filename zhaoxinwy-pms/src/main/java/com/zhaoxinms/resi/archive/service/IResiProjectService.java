package com.zhaoxinms.resi.archive.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoxinms.resi.archive.entity.ResiProject;

/**
 * 住宅项目（小区）档案 Service接口
 *
 * @author zhaoxinms
 */
public interface IResiProjectService extends IService<ResiProject> {

    /**
     * 查询项目列表
     *
     * @param project 查询条件
     * @return 项目列表
     */
    List<ResiProject> selectResiProjectList(ResiProject project);

    /**
     * 根据编号查询项目
     *
     * @param code 项目编号
     * @return 项目信息
     */
    ResiProject selectByCode(String code);

    /**
     * 校验项目编号是否唯一
     *
     * @param code 项目编号
     * @return 结果 true=唯一 false=不唯一
     */
    boolean checkCodeUnique(String code);

    /**
     * 校验项目编号是否唯一（排除指定ID，用于修改时校验）
     *
     * @param code 项目编号
     * @param id   排除的项目ID
     * @return 结果 true=唯一 false=不唯一
     */
    boolean checkCodeUnique(String code, Long id);
}
