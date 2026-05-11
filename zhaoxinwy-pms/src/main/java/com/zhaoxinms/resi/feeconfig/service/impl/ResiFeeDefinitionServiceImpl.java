package com.zhaoxinms.resi.feeconfig.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition;
import com.zhaoxinms.resi.feeconfig.mapper.ResiFeeDefinitionMapper;
import com.zhaoxinms.resi.feeconfig.service.IResiFeeDefinitionService;

/**
 * 费用定义 Service实现
 *
 * @author zhaoxinms
 */
@Service
public class ResiFeeDefinitionServiceImpl extends ServiceImpl<ResiFeeDefinitionMapper, ResiFeeDefinition>
        implements IResiFeeDefinitionService {

    @Override
    public List<ResiFeeDefinition> selectResiFeeDefinitionList(ResiFeeDefinition feeDefinition) {
        QueryWrapper<ResiFeeDefinition> queryWrapper = new QueryWrapper<>();

        // 按项目过滤
        if (feeDefinition.getProjectId() != null) {
            queryWrapper.eq("project_id", feeDefinition.getProjectId());
        }

        // 费用名称模糊查询
        if (StringUtils.isNotBlank(feeDefinition.getFeeName())) {
            queryWrapper.like("fee_name", feeDefinition.getFeeName());
        }

        // 费用编码精确查询
        if (StringUtils.isNotBlank(feeDefinition.getFeeCode())) {
            queryWrapper.eq("fee_code", feeDefinition.getFeeCode());
        }

        // 费用类型筛选
        if (StringUtils.isNotBlank(feeDefinition.getFeeType())) {
            queryWrapper.eq("fee_type", feeDefinition.getFeeType());
        }

        // 计费方式筛选
        if (StringUtils.isNotBlank(feeDefinition.getCalcType())) {
            queryWrapper.eq("calc_type", feeDefinition.getCalcType());
        }

        // 只查有效数据
        queryWrapper.eq("enabled_mark", 1);

        // 按排序码升序，再按创建时间倒序
        queryWrapper.orderByAsc("sort_code").orderByDesc("creator_time");

        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public boolean save(ResiFeeDefinition entity) {
        // 自动填充创建信息
        Date now = new Date();
        entity.setCreatorTime(now);
        entity.setCreatorUserId(String.valueOf(SecurityUtils.getUserId()));
        entity.setLastModifyTime(now);
        entity.setLastModifyUserId(String.valueOf(SecurityUtils.getUserId()));
        entity.setEnabledMark(1);
        return super.save(entity);
    }

    @Override
    public boolean updateById(ResiFeeDefinition entity) {
        // 自动填充修改信息
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(String.valueOf(SecurityUtils.getUserId()));
        return super.updateById(entity);
    }

    @Override
    public boolean removeByIds(java.util.Collection<? extends java.io.Serializable> idList) {
        // 软删除：更新 enabled_mark=0，填充 delete_time 和 delete_user_id
        for (java.io.Serializable id : idList) {
            ResiFeeDefinition entity = new ResiFeeDefinition();
            entity.setId((String) id);
            entity.setEnabledMark(0);
            entity.setDeleteTime(new Date());
            entity.setDeleteUserId(String.valueOf(SecurityUtils.getUserId()));
            baseMapper.updateById(entity);
        }
        return true;
    }

    @Override
    public boolean checkCodeUnique(String feeCode, Long projectId) {
        QueryWrapper<ResiFeeDefinition> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fee_code", feeCode);
        queryWrapper.eq("project_id", projectId);
        queryWrapper.eq("enabled_mark", 1);
        return baseMapper.selectCount(queryWrapper) == 0;
    }

    @Override
    public boolean checkCodeUnique(String feeCode, Long projectId, String id) {
        QueryWrapper<ResiFeeDefinition> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fee_code", feeCode);
        queryWrapper.eq("project_id", projectId);
        queryWrapper.eq("enabled_mark", 1);
        queryWrapper.ne("id", id);
        return baseMapper.selectCount(queryWrapper) == 0;
    }
}
