package com.zhaoxinms.resi.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.resi.archive.entity.ResiProject;
import com.zhaoxinms.resi.archive.mapper.ResiProjectMapper;
import com.zhaoxinms.resi.common.service.ResiProjectPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 住宅物业项目权限服务实现
 * <p>
 * Sprint 1 简化实现：非超管用户默认可访问全部项目（返回所有项目ID）。
 * 后续可通过 resi_user_project 中间表或 sys_user.dept_id 实现精确控制。
 *
 * @author zhaoxinms
 */
@Service
public class ResiProjectPermissionServiceImpl implements ResiProjectPermissionService {

    private static final Logger log = LoggerFactory.getLogger(ResiProjectPermissionServiceImpl.class);

    @Autowired
    private ResiProjectMapper projectMapper;

    @Override
    public List<Long> getUserAllowedProjectIds(Long userId) {
        // 超管：返回空列表，由 AOP 判断为不过滤
        if (SecurityUtils.isAdmin(userId)) {
            return new ArrayList<>();
        }

        // Sprint 1：默认返回所有项目ID（简化实现，确保功能可用）
        // TODO 后续接入 resi_user_project 中间表实现精确项目权限控制
        QueryWrapper<ResiProject> qw = new QueryWrapper<>();
        qw.eq("enabled_mark", 1);
        qw.select("id");
        List<ResiProject> projects = projectMapper.selectList(qw);
        List<Long> projectIds = projects.stream().map(ResiProject::getId).collect(Collectors.toList());

        if (projectIds.isEmpty()) {
            log.warn("用户[{}]查询项目权限，但系统中无有效项目，返回空权限", userId);
            return new ArrayList<>();
        }

        return projectIds;
    }

    @Override
    public boolean hasProjectPermission(Long userId, Long projectId) {
        if (SecurityUtils.isAdmin(userId)) {
            return true;
        }
        List<Long> allowed = getUserAllowedProjectIds(userId);
        return allowed.contains(projectId);
    }
}
