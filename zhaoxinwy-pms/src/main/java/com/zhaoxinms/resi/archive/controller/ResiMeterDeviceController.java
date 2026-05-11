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
import com.zhaoxinms.resi.archive.entity.ResiMeterDevice;
import com.zhaoxinms.resi.archive.service.IResiMeterDeviceService;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 仪表档案 Controller
 *
 * @author zhaoxinms
 */
@Api(tags = "住宅收费-仪表管理")
@RestController
@RequestMapping("/resi/archive/meter-device")
public class ResiMeterDeviceController extends BaseController {

    @Autowired
    private IResiMeterDeviceService meterDeviceService;

    /**
     * 查询仪表列表
     */
    @ApiOperation("查询仪表列表")
    @PreAuthorize("@ss.hasPermi('resi:meterDevice:list')")
    @ResiProjectScope
    @GetMapping
    public TableDataInfo list(ResiMeterDevice device) {
        startPage();
        List<ResiMeterDevice> list = meterDeviceService.selectResiMeterDeviceList(device);
        return getDataTable(list);
    }

    /**
     * 获取仪表详细信息
     */
    @ApiOperation("获取仪表详情")
    @PreAuthorize("@ss.hasPermi('resi:meterDevice:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(meterDeviceService.getById(id));
    }

    /**
     * 新增仪表
     */
    @ApiOperation("新增仪表")
    @PreAuthorize("@ss.hasPermi('resi:meterDevice:add')")
    @Log(title = "住宅收费-仪表管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody @Validated(AddGroup.class) ResiMeterDevice device) {
        device.setCreateBy(SecurityUtils.getUsername());
        return toAjax(meterDeviceService.save(device));
    }

    /**
     * 修改仪表
     */
    @ApiOperation("修改仪表")
    @PreAuthorize("@ss.hasPermi('resi:meterDevice:edit')")
    @Log(title = "住宅收费-仪表管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") Long id,
                           @RequestBody @Validated(EditGroup.class) ResiMeterDevice device) {
        device.setId(id);
        device.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(meterDeviceService.updateById(device));
    }

    /**
     * 删除仪表
     */
    @ApiOperation("删除仪表")
    @PreAuthorize("@ss.hasPermi('resi:meterDevice:remove')")
    @Log(title = "住宅收费-仪表管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(meterDeviceService.removeByIds(java.util.Arrays.asList(ids)));
    }
}
