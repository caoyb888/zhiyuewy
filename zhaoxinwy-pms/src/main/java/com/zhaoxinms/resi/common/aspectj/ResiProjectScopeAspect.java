package com.zhaoxinms.resi.common.aspectj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;
import com.zhaoxinms.resi.common.service.ResiProjectPermissionService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 住宅物业项目数据隔离 AOP 处理
 * <p>
 * 拦截带有 {@link ResiProjectScope} 注解的方法，自动为第一个 QueryWrapper 参数追加
 * project_id IN (用户有权限的项目列表) 过滤条件。
 * <p>
 * 超管（userId = 1）跳过过滤。
 *
 * @author zhaoxinms
 */
@Aspect
@Component
public class ResiProjectScopeAspect {

    private static final Logger log = LoggerFactory.getLogger(ResiProjectScopeAspect.class);

    @Autowired
    private ResiProjectPermissionService projectPermissionService;

    @Before("@annotation(resiProjectScope)")
    public void doBefore(JoinPoint point, ResiProjectScope resiProjectScope) throws Throwable {
        if (resiProjectScope.ignore()) {
            return;
        }

        // 超管跳过项目隔离
        Long userId = SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin(userId)) {
            return;
        }

        // 获取当前用户可访问的项目列表
        List<Long> allowedProjectIds = projectPermissionService.getUserAllowedProjectIds(userId);
        if (allowedProjectIds == null || allowedProjectIds.isEmpty()) {
            log.warn("用户[{}]无任何项目权限，查询将被限制为空结果", userId);
            // 无可访问项目时，写入 1=0 条件使查询返回空结果
            allowedProjectIds = java.util.Collections.singletonList(-1L);
        }

        // 查找第一个 QueryWrapper 参数并追加 project_id 条件
        Object[] args = point.getArgs();
        boolean applied = false;
        for (Object arg : args) {
            if (arg instanceof QueryWrapper) {
                @SuppressWarnings("unchecked")
                QueryWrapper<Object> qw = (QueryWrapper<Object>) arg;
                String projectColumn = StringUtils.isNotBlank(resiProjectScope.projectColumn())
                        ? resiProjectScope.projectColumn() : "project_id";
                qw.in(projectColumn, allowedProjectIds);
                applied = true;
                if (log.isDebugEnabled()) {
                    log.debug("项目数据隔离已应用(QueryWrapper): userId={}, projectColumn={}, projectIds={}",
                            userId, projectColumn, allowedProjectIds);
                }
                break;
            }
        }

        // 若未找到 QueryWrapper，尝试向实体参数注入 projectIds（供 XML 动态 SQL 使用）
        if (!applied) {
            for (Object arg : args) {
                if (arg instanceof com.zhaoxinms.resi.common.ResiBaseEntity) {
                    ((com.zhaoxinms.resi.common.ResiBaseEntity) arg).setProjectIds(allowedProjectIds);
                    applied = true;
                    if (log.isDebugEnabled()) {
                        log.debug("项目数据隔离已应用(实体注入): userId={}, projectIds={}",
                                userId, allowedProjectIds);
                    }
                    break;
                }
            }
        }

        if (!applied) {
            log.warn("方法 [{}] 标记了 @ResiProjectScope 但未找到 QueryWrapper 或 ResiBaseEntity 参数，隔离条件未应用",
                    point.getSignature().getName());
        }
    }
}
