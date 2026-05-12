package com.zhaoxinms.resi.meter.controller;

import java.util.Arrays;
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
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;
import com.zhaoxinms.resi.meter.entity.ResiMeterReading;
import com.zhaoxinms.resi.meter.service.IResiMeterReadingService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 抄表记录 Controller
 *
 * @author zhaoxinms
 */
@Api(tags = "住宅收费-抄表管理")
@RestController
@RequestMapping("/resi/meter/reading")
public class ResiMeterReadingController extends BaseController {

    @Autowired
    private IResiMeterReadingService meterReadingService;

    /**
     * 查询抄表记录列表
     */
    @ApiOperation("查询抄表记录列表")
    @PreAuthorize("@ss.hasPermi('resi:meter:list')")
    @ResiProjectScope
    @GetMapping
    public TableDataInfo list(ResiMeterReading reading) {
        startPage();
        List<ResiMeterReading> list = meterReadingService.selectResiMeterReadingList(reading);
        return getDataTable(list);
    }

    /**
     * 获取抄表记录详情
     */
    @ApiOperation("获取抄表记录详情")
    @PreAuthorize("@ss.hasPermi('resi:meter:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(meterReadingService.getById(id));
    }

    /**
     * 新增抄表记录
     */
    @ApiOperation("新增抄表记录")
    @PreAuthorize("@ss.hasPermi('resi:meter:add')")
    @Log(title = "住宅收费-抄表管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody @Validated(AddGroup.class) ResiMeterReading reading) {
        // 业务校验：本次读数不能小于上次读数（预警但不阻止）
        if (reading.getCurrReading() != null && reading.getLastReading() != null
                && reading.getCurrReading().compareTo(reading.getLastReading()) < 0) {
            // 允许保存，但记录日志（预警逻辑在S3-02导入阶段处理）
        }

        return toAjax(meterReadingService.save(reading));
    }

    /**
     * 修改抄表记录
     */
    @ApiOperation("修改抄表记录")
    @PreAuthorize("@ss.hasPermi('resi:meter:edit')")
    @Log(title = "住宅收费-抄表管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") String id,
                           @RequestBody @Validated(EditGroup.class) ResiMeterReading reading) {
        // 已入账的记录不允许修改
        ResiMeterReading existing = meterReadingService.getById(id);
        if (existing != null && "BILLED".equals(existing.getStatus())) {
            return AjaxResult.error("已入账的抄表记录不允许修改");
        }
        if (existing != null && "VERIFIED".equals(existing.getStatus())) {
            return AjaxResult.error("已复核的抄表记录不允许修改");
        }

        reading.setId(id);
        return toAjax(meterReadingService.updateById(reading));
    }

    /**
     * 删除抄表记录
     */
    @ApiOperation("删除抄表记录")
    @PreAuthorize("@ss.hasPermi('resi:meter:remove')")
    @Log(title = "住宅收费-抄表管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        // 已入账/已复核的记录不允许删除
        for (String id : ids) {
            ResiMeterReading reading = meterReadingService.getById(id);
            if (reading != null && ("BILLED".equals(reading.getStatus()) || "VERIFIED".equals(reading.getStatus()))) {
                return AjaxResult.error("已入账或已复核的抄表记录不允许删除");
            }
        }
        return toAjax(meterReadingService.removeByIds(Arrays.asList(ids)));
    }
}
