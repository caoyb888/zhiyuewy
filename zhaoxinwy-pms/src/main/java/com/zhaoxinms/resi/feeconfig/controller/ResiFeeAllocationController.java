package com.zhaoxinms.resi.feeconfig.controller;

import java.util.List;
import java.util.Map;

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
import com.zhaoxinms.common.core.page.TableDataInfo;
import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.common.ResiConstants;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;
import com.zhaoxinms.resi.feeconfig.dto.ResiFeeAllocationBatchReq;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeAllocation;
import com.zhaoxinms.resi.feeconfig.service.IResiFeeAllocationService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 费用分配 Controller
 *
 * @author zhaoxinms
 */
@Api(tags = "住宅收费-费用分配")
@RestController
@RequestMapping("/resi/feeconfig/allocation")
public class ResiFeeAllocationController extends BaseController {

    @Autowired
    private IResiFeeAllocationService allocationService;

    /**
     * 查询费用分配列表
     */
    @ApiOperation("查询费用分配列表")
    @PreAuthorize("@ss.hasPermi('resi:feeAllocation:list')")
    @ResiProjectScope
    @GetMapping
    public TableDataInfo list(ResiFeeAllocation allocation) {
        startPage();
        List<ResiFeeAllocation> list = allocationService.selectResiFeeAllocationList(allocation);
        return getDataTable(list);
    }

    /**
     * 获取费用分配详情
     */
    @ApiOperation("获取费用分配详情")
    @PreAuthorize("@ss.hasPermi('resi:feeAllocation:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(allocationService.getById(id));
    }

    /**
     * 新增费用分配
     */
    @ApiOperation("新增费用分配")
    @PreAuthorize("@ss.hasPermi('resi:feeAllocation:add')")
    @Log(title = "住宅收费-费用分配", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody @Validated(AddGroup.class) ResiFeeAllocation allocation) {
        // 资源类型默认值
        if (StringUtils.isBlank(allocation.getResourceType())) {
            allocation.setResourceType(ResiConstants.RESOURCE_TYPE_ROOM);
        }
        return toAjax(allocationService.save(allocation));
    }

    /**
     * 修改费用分配
     */
    @ApiOperation("修改费用分配")
    @PreAuthorize("@ss.hasPermi('resi:feeAllocation:edit')")
    @Log(title = "住宅收费-费用分配", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") String id,
                           @RequestBody @Validated(EditGroup.class) ResiFeeAllocation allocation) {
        allocation.setId(id);
        return toAjax(allocationService.updateById(allocation));
    }

    /**
     * 删除费用分配
     */
    @ApiOperation("删除费用分配")
    @PreAuthorize("@ss.hasPermi('resi:feeAllocation:remove')")
    @Log(title = "住宅收费-费用分配", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(allocationService.removeByIds(java.util.Arrays.asList(ids)));
    }

    /**
     * 预览批量分配数量
     */
    @ApiOperation("预览批量分配数量")
    @PreAuthorize("@ss.hasPermi('resi:feeAllocation:add')")
    @GetMapping("/preview")
    public AjaxResult previewBatch(@RequestParam Long projectId,
                                    @RequestParam String feeId,
                                    @RequestParam String batchType,
                                    @RequestParam(required = false) Long buildingId,
                                    @RequestParam(required = false) String unitNo,
                                    @RequestParam String startDate) {
        Map<String, Object> result = allocationService.previewBatchAllocate(
                projectId, feeId, batchType, buildingId, unitNo, startDate);
        return AjaxResult.success(result);
    }

    /**
     * 批量分配费用
     */
    @ApiOperation("批量分配费用")
    @PreAuthorize("@ss.hasPermi('resi:feeAllocation:add')")
    @Log(title = "住宅收费-费用分配-批量分配", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batchAllocate(@RequestBody @Validated ResiFeeAllocationBatchReq req) {
        ResiFeeAllocation allocation = new ResiFeeAllocation();
        allocation.setProjectId(req.getProjectId());
        allocation.setFeeId(req.getFeeId());
        allocation.setStartDate(req.getStartDate());
        allocation.setEndDate(req.getEndDate());
        allocation.setCustomPrice(req.getCustomPrice());
        allocation.setCustomFormula(req.getCustomFormula());

        Map<String, Object> result = allocationService.batchAllocate(
                allocation, req.getBatchType(), req.getBuildingId(), req.getUnitNo());
        return AjaxResult.success(result);
    }
}
