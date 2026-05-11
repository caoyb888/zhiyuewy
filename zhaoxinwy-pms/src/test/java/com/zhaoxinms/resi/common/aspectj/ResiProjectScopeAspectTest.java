package com.zhaoxinms.resi.common.aspectj;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoxinms.resi.common.ResiBaseEntity;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;
import com.zhaoxinms.resi.common.service.ResiProjectPermissionService;
import com.zhaoxinms.resi.TestSecurityContext;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResiProjectScopeAspectTest {

    @Mock
    private ResiProjectPermissionService permissionService;

    @InjectMocks
    private ResiProjectScopeAspect aspect;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private ResiProjectScope annotation;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.clear();
    }

    @Test
    @DisplayName("ignore=true skip")
    void doBefore_ignoreTrue_skip() throws Throwable {
        when(annotation.ignore()).thenReturn(true);
        aspect.doBefore(joinPoint, annotation);
        verify(permissionService, never()).getUserAllowedProjectIds(any());
    }

    @Test
    @DisplayName("admin skip isolation")
    void doBefore_admin_skip() throws Throwable {
        when(annotation.ignore()).thenReturn(false);
        TestSecurityContext.setAdminUser();

        QueryWrapper<Object> qw = new QueryWrapper<>();
        Object[] args = new Object[]{qw};
        when(joinPoint.getArgs()).thenReturn(args);

        aspect.doBefore(joinPoint, annotation);
        verify(permissionService, never()).getUserAllowedProjectIds(any());
    }

    @Test
    @DisplayName("normal user injects project_id IN condition")
    void doBefore_normalUser_withPermission_queryWrapper() throws Throwable {
        when(annotation.ignore()).thenReturn(false);
        when(annotation.projectColumn()).thenReturn("");
        TestSecurityContext.setNormalUser();

        List<Long> allowedIds = Arrays.asList(1L, 2L);
        when(permissionService.getUserAllowedProjectIds(2L)).thenReturn(allowedIds);

        QueryWrapper<Object> qw = new QueryWrapper<>();
        Object[] args = new Object[]{qw};
        when(joinPoint.getArgs()).thenReturn(args);

        aspect.doBefore(joinPoint, annotation);

        String sqlSegment = qw.getSqlSegment();
        assertNotNull(sqlSegment);
        assertTrue(sqlSegment.toLowerCase().contains("in"),
                "QueryWrapper should contain IN condition");
    }

    @Test
    @DisplayName("no permission injects -1")
    void doBefore_noPermission_injectNegativeOne() throws Throwable {
        when(annotation.ignore()).thenReturn(false);
        when(annotation.projectColumn()).thenReturn("");
        TestSecurityContext.setNormalUser();

        when(permissionService.getUserAllowedProjectIds(2L)).thenReturn(Collections.emptyList());

        QueryWrapper<Object> qw = new QueryWrapper<>();
        Object[] args = new Object[]{qw};
        when(joinPoint.getArgs()).thenReturn(args);

        aspect.doBefore(joinPoint, annotation);

        String sqlSegment = qw.getSqlSegment();
        assertNotNull(sqlSegment);
        assertTrue(sqlSegment.toLowerCase().contains("in"),
                "QueryWrapper should contain IN condition");

        // Verify parameter contains -1
        Object paramValue = qw.getParamNameValuePairs().values().iterator().next();
        assertTrue(paramValue.toString().contains("-1"),
                "Parameter should contain -1");
    }

    @Test
    @DisplayName("custom projectColumn")
    void doBefore_customColumn_useSpecifiedColumn() throws Throwable {
        when(annotation.ignore()).thenReturn(false);
        when(annotation.projectColumn()).thenReturn("projectId");
        TestSecurityContext.setNormalUser();

        List<Long> allowedIds = Arrays.asList(1L);
        when(permissionService.getUserAllowedProjectIds(2L)).thenReturn(allowedIds);

        QueryWrapper<Object> qw = new QueryWrapper<>();
        Object[] args = new Object[]{qw};
        when(joinPoint.getArgs()).thenReturn(args);

        aspect.doBefore(joinPoint, annotation);

        String sqlSegment = qw.getSqlSegment();
        assertNotNull(sqlSegment);
        assertTrue(sqlSegment.toLowerCase().contains("in"),
                "Should use custom column name");
    }

    @Test
    @DisplayName("inject projectIds into ResiBaseEntity")
    void doBefore_entityInjection_setProjectIds() throws Throwable {
        when(annotation.ignore()).thenReturn(false);
        TestSecurityContext.setNormalUser();

        List<Long> allowedIds = Arrays.asList(1L, 3L);
        when(permissionService.getUserAllowedProjectIds(2L)).thenReturn(allowedIds);

        ResiBaseEntity entity = new ResiBaseEntity() {};
        Object[] args = new Object[]{entity};
        when(joinPoint.getArgs()).thenReturn(args);

        aspect.doBefore(joinPoint, annotation);

        assertNotNull(entity.getProjectIds());
        assertEquals(allowedIds, entity.getProjectIds());
    }

    @Test
    @DisplayName("no applicable parameter only logs warning")
    void doBefore_noApplicableParameter_warnOnly() throws Throwable {
        when(annotation.ignore()).thenReturn(false);
        TestSecurityContext.setNormalUser();

        when(permissionService.getUserAllowedProjectIds(2L)).thenReturn(Arrays.asList(1L));

        Object[] args = new Object[]{"string-arg", 123};
        when(joinPoint.getArgs()).thenReturn(args);

        assertDoesNotThrow(() -> aspect.doBefore(joinPoint, annotation));
    }
}
