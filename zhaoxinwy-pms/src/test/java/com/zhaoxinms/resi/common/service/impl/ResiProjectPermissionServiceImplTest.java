package com.zhaoxinms.resi.common.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zhaoxinms.resi.common.mapper.ResiUserProjectMapper;

/**
 * Project Permission Service Unit Test
 * Covers: admin bypass, normal user project list, empty permission
 */
@ExtendWith(MockitoExtension.class)
class ResiProjectPermissionServiceImplTest {

    @Mock
    private ResiUserProjectMapper userProjectMapper;

    @InjectMocks
    private ResiProjectPermissionServiceImpl permissionService;

    @Test
    @DisplayName("admin user should return empty list (no filter)")
    void getUserAllowedProjectIds_admin_emptyList() {
        List<Long> result = permissionService.getUserAllowedProjectIds(1L);
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Admin should return empty list");
        verify(userProjectMapper, never()).selectProjectIdsByUserId(any());
    }

    @Test
    @DisplayName("normal user with permissions should return project ids")
    void getUserAllowedProjectIds_normal_withPermissions() {
        List<Long> expected = Arrays.asList(1L, 2L, 3L);
        when(userProjectMapper.selectProjectIdsByUserId(2L)).thenReturn(expected);

        List<Long> result = permissionService.getUserAllowedProjectIds(2L);

        assertEquals(expected, result);
        verify(userProjectMapper).selectProjectIdsByUserId(2L);
    }

    @Test
    @DisplayName("normal user without permissions should return empty list")
    void getUserAllowedProjectIds_normal_noPermissions() {
        when(userProjectMapper.selectProjectIdsByUserId(3L)).thenReturn(Collections.emptyList());

        List<Long> result = permissionService.getUserAllowedProjectIds(3L);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "No permission should return empty list");
    }

    @Test
    @DisplayName("normal user with null result should return empty list")
    void getUserAllowedProjectIds_normal_nullResult() {
        when(userProjectMapper.selectProjectIdsByUserId(4L)).thenReturn(null);

        List<Long> result = permissionService.getUserAllowedProjectIds(4L);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Null result should return empty list");
    }

    @Test
    @DisplayName("hasPermission: admin should always return true")
    void hasPermission_admin_alwaysTrue() {
        assertTrue(permissionService.hasProjectPermission(1L, 999L));
    }

    @Test
    @DisplayName("hasPermission: allowed project should return true")
    void hasPermission_allowed_true() {
        when(userProjectMapper.selectProjectIdsByUserId(2L)).thenReturn(Arrays.asList(1L, 2L));
        assertTrue(permissionService.hasProjectPermission(2L, 1L));
    }

    @Test
    @DisplayName("hasPermission: disallowed project should return false")
    void hasPermission_disallowed_false() {
        when(userProjectMapper.selectProjectIdsByUserId(2L)).thenReturn(Arrays.asList(1L, 2L));
        assertFalse(permissionService.hasProjectPermission(2L, 3L));
    }
}
