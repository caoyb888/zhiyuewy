package com.zhaoxinms.resi.receivable.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhaoxinms.common.annotation.Log;
import com.zhaoxinms.common.core.controller.BaseController;
import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.common.core.page.TableDataInfo;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;
import com.zhaoxinms.resi.receivable.dto.ResiReceivableCreateTempReq;
import com.zhaoxinms.resi.receivable.dto.ResiReceivableGenerateReq;
import com.zhaoxinms.resi.receivable.dto.ResiReceivableGenerateVo;
import com.zhaoxinms.resi.receivable.dto.ResiReceivableQuery;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;
import com.zhaoxinms.resi.receivable.mapper.ResiReceivableMapper;
import com.zhaoxinms.resi.receivable.service.IResiReceivableGenService;
import com.zhaoxinms.resi.receivable.service.IResiReceivableService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 应收账单 Controller
 */
@Api(tags = "住宅收费-应收管理")
@RestController
@RequestMapping("/resi/receivable")
public class ResiReceivableController extends BaseController {

    @Autowired
    private IResiReceivableService receivableService;

    @Autowired
    private IResiReceivableGenService receivableGenService;

    @Autowired
    private ResiReceivableMapper receivableMapper;

    /**
     * 查询应收账单列表
     */
    @ApiOperation("查询应收账单列表")
    @PreAuthorize("@ss.hasPermi('resi:receivable:list')")
    @ResiProjectScope
    @GetMapping
    public TableDataInfo list(ResiReceivableQuery query) {
        startPage();
        List<ResiReceivable> list = receivableMapper.selectResiReceivableList(query);
        return getDataTable(list);
    }

    /**
     * 获取应收账单详情
     */
    @ApiOperation("获取应收账单详情")
    @PreAuthorize("@ss.hasPermi('resi:receivable:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(receivableService.getById(id));
    }

    /**
     * 批量生成周期费应收
     */
    @ApiOperation("批量生成周期费应收")
    @PreAuthorize("@ss.hasPermi('resi:receivable:generate')")
    @Log(title = "住宅收费-应收管理-批量生成", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody @Validated ResiReceivableGenerateReq req) {
        if (StringUtils.isBlank(req.getBillPeriod())) {
            return AjaxResult.error("账单月份不能为空");
        }
        // 简单校验月份格式 yyyy-MM
        if (!req.getBillPeriod().matches("\\d{4}-\\d{2}")) {
            return AjaxResult.error("账单月份格式不正确，应为 yyyy-MM");
        }
        ResiReceivableGenerateVo result = receivableGenService.batchGenerate(req);
        return AjaxResult.success(result);
    }

    /**
     * 按批次删除未收应收（支持重新生成）
     */
    @ApiOperation("按批次删除未收应收")
    @PreAuthorize("@ss.hasPermi('resi:receivable:remove')")
    @Log(title = "住宅收费-应收管理-按批次删除", businessType = BusinessType.DELETE)
    @DeleteMapping("/generate/{genBatch}")
    public AjaxResult deleteGenerate(@PathVariable("genBatch") String genBatch) {
        int count = receivableGenService.deleteByGenBatch(genBatch);
        return AjaxResult.success("已删除 " + count + " 条未收记录");
    }

    /**
     * 手动录入临时费
     */
    @ApiOperation("手动录入临时费")
    @PreAuthorize("@ss.hasPermi('resi:receivable:add')")
    @Log(title = "住宅收费-应收管理-临时费录入", businessType = BusinessType.INSERT)
    @PostMapping("/create-temp")
    public AjaxResult createTemp(@RequestBody @Validated ResiReceivableCreateTempReq req) {
        receivableGenService.createTempReceivable(req);
        return AjaxResult.success();
    }

    /**
     * 删除应收账单（仅未收状态可删除）
     */
    @ApiOperation("删除应收账单")
    @PreAuthorize("@ss.hasPermi('resi:receivable:remove')")
    @Log(title = "住宅收费-应收管理-删除", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        for (String id : ids) {
            ResiReceivable r = receivableService.getById(id);
            if (r == null) {
                continue;
            }
            if (!"0".equals(r.getPayState())) {
                return AjaxResult.error("删除失败，包含已收款或部分收款的记录，ID：" + id);
            }
        }
        return toAjax(receivableService.removeByIds(Arrays.asList(ids)));
    }
}
