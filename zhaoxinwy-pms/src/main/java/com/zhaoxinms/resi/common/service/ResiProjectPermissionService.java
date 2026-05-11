package com.zhaoxinms.resi.common.service;

import java.util.List;

/**
 * 住宅物业项目权限服务接口
 * <p>
 * 查询当前用户可访问的项目列表，支持超管 = 全部。
 * <p>
 * 实现类需根据实际的用户-项目关联关系（如 sys_user_project 中间表或角色数据权限）进行查询。
 *
 * @author zhaoxinms
 */
public interface ResiProjectPermissionService {

    /**
     * 获取用户可访问的项目ID列表
     *
     * @param userId 用户ID
     * @return 项目ID列表；超管返回 null 或空列表（由调用方视为全部）
     */
    List<Long> getUserAllowedProjectIds(Long userId);

    /**
     * 校验用户是否有权访问指定项目
     *
     * @param userId    用户ID
     * @param projectId 项目ID
     * @return true 有权访问
     */
    boolean hasProjectPermission(Long userId, Long projectId);
}
