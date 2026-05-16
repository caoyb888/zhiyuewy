package com.zhaoxinms.resi.finance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhaoxinms.common.core.controller.BaseController;
import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.common.core.page.TableDataInfo;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;
import com.zhaoxinms.resi.finance.dto.ResiAdjustLogQuery;
import com.zhaoxinms.resi.finance.entity.ResiAdjustLog;
import com.zhaoxinms.resi.finance.service.IResiAdjustLogService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 调账记录 Controller
 */
@Api(tags = "住宅收费-调账记录")
@RestController
@RequestMapping("/resi/finance/adjust-log")
public class ResiAdjustLogController extends BaseController {

    @Autowired
    private IResiAdjustLogService adjustLogService;

    /**
     * 查询调账记录列表
     */
    @ApiOperation("查询调账记录列表")
    @PreAuthorize("@ss.hasPermi('resi:finance:adjustLog:list')")
    @ResiProjectScope
    @GetMapping
    public TableDataInfo list(ResiAdjustLogQuery query) {
        startPage();
        List<ResiAdjustLog> list = adjustLogService.selectResiAdjustLogList(query);
        return getDataTable(list);
    }

    /**
     * 获取调账记录详情
     */
    @ApiOperation("获取调账记录详情")
    @PreAuthorize("@ss.hasPermi('resi:finance:adjustLog:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(String id) {
        return AjaxResult.success(adjustLogService.getById(id));
    }
}
