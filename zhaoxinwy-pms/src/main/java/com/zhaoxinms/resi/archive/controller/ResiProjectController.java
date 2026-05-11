package com.zhaoxinms.resi.archive.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhaoxinms.common.annotation.Log;
import com.zhaoxinms.common.core.controller.BaseController;
import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.common.core.page.TableDataInfo;
import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.resi.archive.entity.ResiProject;
import com.zhaoxinms.resi.archive.service.IResiProjectService;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 住宅项目（小区）档案 Controller
 *
 * @author zhaoxinms
 */
@Api(tags = "住宅收费-项目管理")
@RestController
@RequestMapping("/resi/archive/project")
public class ResiProjectController extends BaseController {

    @Autowired
    private IResiProjectService projectService;

    /**
     * 查询项目列表
     */
    @ApiOperation("查询项目列表")
    @PreAuthorize("@ss.hasPermi('resi:project:list')")
    @ResiProjectScope
    @GetMapping
    public TableDataInfo list(ResiProject project) {
        startPage();
        List<ResiProject> list = projectService.selectResiProjectList(project);
        return getDataTable(list);
    }

    /**
     * 获取项目详细信息
     */
    @ApiOperation("获取项目详情")
    @PreAuthorize("@ss.hasPermi('resi:project:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(projectService.getById(id));
    }

    /**
     * 新增项目
     */
    @ApiOperation("新增项目")
    @PreAuthorize("@ss.hasPermi('resi:project:add')")
    @Log(title = "住宅收费-项目管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody @Validated(AddGroup.class) ResiProject project) {
        if (!projectService.checkCodeUnique(project.getCode())) {
            return AjaxResult.error("新增项目失败，项目编号 '" + project.getCode() + "' 已存在");
        }
        project.setCreateBy(SecurityUtils.getUsername());
        return toAjax(projectService.save(project));
    }

    /**
     * 修改项目
     */
    @ApiOperation("修改项目")
    @PreAuthorize("@ss.hasPermi('resi:project:edit')")
    @Log(title = "住宅收费-项目管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") Long id, @RequestBody @Validated(EditGroup.class) ResiProject project) {
        project.setId(id);
        if (!projectService.checkCodeUnique(project.getCode(), project.getId())) {
            return AjaxResult.error("修改项目失败，项目编号 '" + project.getCode() + "' 已存在");
        }
        project.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(projectService.updateById(project));
    }

    /**
     * 删除项目
     */
    @ApiOperation("删除项目")
    @PreAuthorize("@ss.hasPermi('resi:project:remove')")
    @Log(title = "住宅收费-项目管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(projectService.removeByIds(java.util.Arrays.asList(ids)));
    }

    /**
     * 校验项目编号唯一性
     */
    @ApiOperation("校验项目编号唯一性")
    @PreAuthorize("@ss.hasPermi('resi:project:add') or @ss.hasPermi('resi:project:edit')")
    @GetMapping("/checkCodeUnique")
    public AjaxResult checkCodeUnique(String code, Long id) {
        boolean unique;
        if (id != null) {
            unique = projectService.checkCodeUnique(code, id);
        } else {
            unique = projectService.checkCodeUnique(code);
        }
        return AjaxResult.success(unique);
    }
}
