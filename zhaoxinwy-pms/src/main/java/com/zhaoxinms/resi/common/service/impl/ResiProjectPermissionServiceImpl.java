package com.zhaoxinms.resi.common.service.impl;

import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.resi.common.service.ResiProjectPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 住宅物业项目权限服务实现（骨架）
 * <p>
 * TODO Sprint 1 阶段需完善用户-项目关联查询逻辑：
 * 1. 从 sys_user_project 中间表查询（如存在）
 * 2. 或从 sys_role.data_scope + 自定义项目范围表查询
 * 3. 超管返回空列表，由 AOP 视为不过滤
 *
 * @author zhaoxinms
 */
@Service
public class ResiProjectPermissionServiceImpl implements ResiProjectPermissionService {

    private static final Logger log = LoggerFactory.getLogger(ResiProjectPermissionServiceImpl.class);

    @Override
    public List<Long> getUserAllowedProjectIds(Long userId) {
        // 超管：返回空列表，由 AOP 判断为不过滤
        if (SecurityUtils.isAdmin(userId)) {
            return new ArrayList<>();
        }

        // TODO Sprint 1：实现实际查询逻辑
        // 示例：从缓存或数据库查询用户有权限的项目列表
        // List<Long> projectIds = redisTemplate.opsForSet().members("user:project:" + userId);
        // 或：从 sys_user_project 表查询

        log.warn("项目权限查询尚未实现完整逻辑，用户[{}]默认返回空权限列表", userId);
        return new ArrayList<>();
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
