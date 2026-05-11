package com.zhaoxinms.resi.archive.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.archive.entity.ResiProject;
import com.zhaoxinms.resi.archive.mapper.ResiProjectMapper;
import com.zhaoxinms.resi.archive.service.IResiProjectService;

/**
 * 住宅项目（小区）档案 Service实现
 *
 * @author zhaoxinms
 */
@Service
public class ResiProjectServiceImpl extends ServiceImpl<ResiProjectMapper, ResiProject>
        implements IResiProjectService {

    @Override
    public List<ResiProject> selectResiProjectList(ResiProject project) {
        QueryWrapper<ResiProject> queryWrapper = new QueryWrapper<>();
        // 名称模糊查询
        if (StringUtils.isNotBlank(project.getName())) {
            queryWrapper.like("name", project.getName());
        }
        // 编号精确查询
        if (StringUtils.isNotBlank(project.getCode())) {
            queryWrapper.eq("code", project.getCode());
        }
        // 项目权限隔离（由 AOP 注入 projectIds）
        if (project.getProjectIds() != null && !project.getProjectIds().isEmpty()) {
            queryWrapper.in("id", project.getProjectIds());
        }
        // 只查有效数据（软删除已自动过滤，但显式声明更安全）
        queryWrapper.eq("enabled_mark", 1);
        // 按创建时间倒序
        queryWrapper.orderByDesc("create_time");
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public ResiProject selectByCode(String code) {
        QueryWrapper<ResiProject> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        queryWrapper.eq("enabled_mark", 1);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean checkCodeUnique(String code) {
        ResiProject project = selectByCode(code);
        return project == null;
    }

    @Override
    public boolean checkCodeUnique(String code, Long id) {
        QueryWrapper<ResiProject> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        queryWrapper.eq("enabled_mark", 1);
        queryWrapper.ne("id", id);
        return baseMapper.selectCount(queryWrapper) == 0;
    }
}
