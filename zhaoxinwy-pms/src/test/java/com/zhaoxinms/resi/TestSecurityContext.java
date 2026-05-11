package com.zhaoxinms.resi;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.zhaoxinms.common.core.domain.entity.SysUser;
import com.zhaoxinms.common.core.domain.model.LoginUser;

/**
 * 测试安全上下文工具类
 * <p>
 * 用于在单元测试中模拟已登录用户，使 SecurityUtils 能正常工作。
 */
public class TestSecurityContext {

    /**
     * 设置为普通用户（userId=2）
     */
    public static void setNormalUser() {
        setUser(2L, "test_user");
    }

    /**
     * 设置为超管（userId=1）
     */
    public static void setAdminUser() {
        setUser(1L, "admin");
    }

    /**
     * 设置为指定用户
     */
    public static void setUser(Long userId, String username) {
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);
        sysUser.setPassword("");

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userId);
        loginUser.setUser(sysUser);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 清除安全上下文
     */
    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
