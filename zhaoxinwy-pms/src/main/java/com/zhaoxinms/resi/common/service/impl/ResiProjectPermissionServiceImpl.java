package com.zhaoxinms.resi.common.service.impl;

import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.resi.common.mapper.ResiUserProjectMapper;
import com.zhaoxinms.resi.common.service.ResiProjectPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 住宅物业项目权限服务实现
 * <p>
 * 基于 resi_user_project 中间表实现精确的项目数据隔离。
 * 超管（user_id = 1）视为拥有全部项目权限。
 * 若用户在该表无记录，则默认无任何项目权限（查询返回空结果）。
 *
 * @author zhaoxinms
 */
@Service
public class ResiProjectPermissionServiceImpl implements ResiProjectPermissionService {

    private static final Logger log = LoggerFactory.getLogger(ResiProjectPermissionServiceImpl.class);

    @Autowired
    private ResiUserProjectMapper userProjectMapper;

    @Override
    public List<Long> getUserAllowedProjectIds(Long userId) {
        // 超管：返回空列表，由 AOP 判断为不过滤
        if (SecurityUtils.isAdmin(userId)) {
            return new ArrayList<>();
        }

        // 查询 resi_user_project 获取用户有权访问的项目ID列表
        List<Long> projectIds = userProjectMapper.selectProjectIdsByUserId(userId);

        if (projectIds == null || projectIds.isEmpty()) {
            log.warn("用户[{}]在 resi_user_project 中无任何项目权限记录，查询将被限制为空结果", userId);
            return new ArrayList<>();
        }

        log.debug("用户[{}]的项目权限列表：{}", userId, projectIds);
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
