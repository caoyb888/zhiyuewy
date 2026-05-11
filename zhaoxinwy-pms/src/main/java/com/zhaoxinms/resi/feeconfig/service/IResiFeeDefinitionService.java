package com.zhaoxinms.resi.feeconfig.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition;

/**
 * 费用定义 Service接口
 *
 * @author zhaoxinms
 */
public interface IResiFeeDefinitionService extends IService<ResiFeeDefinition> {

    /**
     * 查询费用定义列表
     *
     * @param feeDefinition 查询条件
     * @return 费用定义列表
     */
    List<ResiFeeDefinition> selectResiFeeDefinitionList(ResiFeeDefinition feeDefinition);

    /**
     * 校验费用编码是否唯一
     *
     * @param feeCode   费用编码
     * @param projectId 项目ID
     * @return 结果 true=唯一 false=不唯一
     */
    boolean checkCodeUnique(String feeCode, Long projectId);

    /**
     * 校验费用编码是否唯一（排除指定ID，用于修改时校验）
     *
     * @param feeCode   费用编码
     * @param projectId 项目ID
     * @param id        排除的费用定义ID
     * @return 结果 true=唯一 false=不唯一
     */
    boolean checkCodeUnique(String feeCode, Long projectId, String id);
}
