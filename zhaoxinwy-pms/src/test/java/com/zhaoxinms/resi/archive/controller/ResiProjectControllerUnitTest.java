package com.zhaoxinms.resi.archive.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.resi.archive.entity.ResiProject;
import com.zhaoxinms.resi.archive.service.IResiProjectService;
import com.zhaoxinms.resi.TestSecurityContext;

/**
 * 项目档案 Controller 单元测试
 * <p>
 * 覆盖：
 * - 新增时编号唯一性校验
 * - 修改时编号唯一性校验（排除自身）
 * - 正常 CRUD 返回
 */
@ExtendWith(MockitoExtension.class)
class ResiProjectControllerUnitTest {

    @Mock
    private IResiProjectService projectService;

    @InjectMocks
    private ResiProjectController controller;

    @BeforeEach
    void setUp() {
        TestSecurityContext.setNormalUser();
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.clear();
    }

    @Test
    @DisplayName("新增项目-编号唯一，应成功")
    void add_codeUnique_success() {
        ResiProject project = new ResiProject();
        project.setCode("PRJ-NEW");
        project.setName("新项目");

        when(projectService.checkCodeUnique("PRJ-NEW")).thenReturn(true);
        when(projectService.save(any(ResiProject.class))).thenReturn(true);

        AjaxResult result = controller.add(project);

        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("新增项目-编号重复，应返回错误")
    void add_codeDuplicate_error() {
        ResiProject project = new ResiProject();
        project.setCode("PRJ-DUP");
        project.setName("重复项目");

        when(projectService.checkCodeUnique("PRJ-DUP")).thenReturn(false);

        AjaxResult result = controller.add(project);

        assertEquals(500, result.get(AjaxResult.CODE_TAG));
        String msg = (String) result.get(AjaxResult.MSG_TAG);
        assertTrue(msg.contains("已存在"), "错误提示应包含'已存在'");
    }

    @Test
    @DisplayName("修改项目-编号唯一（排除自身），应成功")
    void edit_codeUniqueExcludeSelf_success() {
        Long id = 1L;
        ResiProject project = new ResiProject();
        project.setCode("PRJ-EDIT");
        project.setName("修改后名称");

        when(projectService.checkCodeUnique("PRJ-EDIT", id)).thenReturn(true);
        when(projectService.updateById(any(ResiProject.class))).thenReturn(true);

        AjaxResult result = controller.edit(id, project);

        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("修改项目-编号与其他项目重复，应返回错误")
    void edit_codeDuplicateWithOther_error() {
        Long id = 1L;
        ResiProject project = new ResiProject();
        project.setCode("PRJ-OTHER");
        project.setName("修改后名称");

        when(projectService.checkCodeUnique("PRJ-OTHER", id)).thenReturn(false);

        AjaxResult result = controller.edit(id, project);

        assertEquals(500, result.get(AjaxResult.CODE_TAG));
        String msg = (String) result.get(AjaxResult.MSG_TAG);
        assertTrue(msg.contains("已存在"), "错误提示应包含'已存在'");
    }

    @Test
    @DisplayName("删除项目-正常删除应成功")
    void remove_normal_success() {
        Long[] ids = {1L, 2L};
        when(projectService.removeByIds(anyList())).thenReturn(true);

        AjaxResult result = controller.remove(ids);

        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }
}
