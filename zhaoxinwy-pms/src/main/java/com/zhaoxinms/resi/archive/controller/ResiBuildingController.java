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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zhaoxinms.common.annotation.Log;
import com.zhaoxinms.common.core.controller.BaseController;
import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.common.core.domain.TreeSelect;
import com.zhaoxinms.common.core.page.TableDataInfo;
import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.resi.archive.entity.ResiBuilding;
import com.zhaoxinms.resi.archive.service.IResiBuildingService;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 楼栋档案 Controller
 *
 * @author zhaoxinms
 */
@Api(tags = "住宅收费-楼栋管理")
@RestController
@RequestMapping("/resi/archive/building")
public class ResiBuildingController extends BaseController {

    @Autowired
    private IResiBuildingService buildingService;

    /**
     * 查询楼栋列表
     */
    @ApiOperation("查询楼栋列表")
    @PreAuthorize("@ss.hasPermi('resi:building:list')")
    @ResiProjectScope
    @GetMapping
    public TableDataInfo list(ResiBuilding building) {
        startPage();
        List<ResiBuilding> list = buildingService.selectResiBuildingList(building);
        return getDataTable(list);
    }

    /**
     * 获取楼栋详细信息
     */
    @ApiOperation("获取楼栋详情")
    @PreAuthorize("@ss.hasPermi('resi:building:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(buildingService.getById(id));
    }

    /**
     * 获取楼栋树形选择列表
     */
    @ApiOperation("获取楼栋树形选择")
    @PreAuthorize("@ss.hasPermi('resi:building:list')")
    @GetMapping("/treeselect")
    public AjaxResult treeselect(@RequestParam(required = false) Long projectId) {
        List<TreeSelect> tree = buildingService.selectBuildingTreeSelect(projectId);
        return AjaxResult.success(tree);
    }

    /**
     * 新增楼栋
     */
    @ApiOperation("新增楼栋")
    @PreAuthorize("@ss.hasPermi('resi:building:add')")
    @Log(title = "住宅收费-楼栋管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody @Validated(AddGroup.class) ResiBuilding building) {
        building.setCreateBy(SecurityUtils.getUsername());
        return toAjax(buildingService.save(building));
    }

    /**
     * 修改楼栋
     */
    @ApiOperation("修改楼栋")
    @PreAuthorize("@ss.hasPermi('resi:building:edit')")
    @Log(title = "住宅收费-楼栋管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") Long id, @RequestBody @Validated(EditGroup.class) ResiBuilding building) {
        building.setId(id);
        building.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(buildingService.updateById(building));
    }

    /**
     * 删除楼栋
     */
    @ApiOperation("删除楼栋")
    @PreAuthorize("@ss.hasPermi('resi:building:remove')")
    @Log(title = "住宅收费-楼栋管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(buildingService.removeByIds(java.util.Arrays.asList(ids)));
    }
}
