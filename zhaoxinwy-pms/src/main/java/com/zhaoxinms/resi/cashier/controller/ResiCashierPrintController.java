package com.zhaoxinms.resi.cashier.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhaoxinms.common.annotation.Log;
import com.zhaoxinms.common.core.controller.BaseController;
import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.resi.cashier.dto.ResiNoticePrintVo;
import com.zhaoxinms.resi.cashier.dto.ResiReceiptPrintVo;
import com.zhaoxinms.resi.cashier.service.IResiCashierPrintService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 收银台打印 Controller
 */
@Api(tags = "住宅收费-收银台打印")
@RestController
@RequestMapping("/resi/print")
public class ResiCashierPrintController extends BaseController {

    @Autowired
    private IResiCashierPrintService printService;

    /**
     * 获取收款单打印数据
     */
    @ApiOperation("收银台-收款单打印数据")
    @PreAuthorize("@ss.hasPermi('resi:cashier:collect')")
    @GetMapping("/receipt/{payLogId}")
    public AjaxResult getReceiptPrintData(@PathVariable("payLogId") String payLogId) {
        if (payLogId == null || payLogId.trim().isEmpty()) {
            return AjaxResult.error("收款流水ID不能为空");
        }
        ResiReceiptPrintVo result = printService.getReceiptPrintData(payLogId);
        return AjaxResult.success(result);
    }

    /**
     * 获取单张缴费通知单打印数据
     */
    @ApiOperation("收银台-缴费通知单打印数据（单张）")
    @PreAuthorize("@ss.hasPermi('resi:receivable:list')")
    @Log(title = "住宅收费-缴费通知单打印", businessType = BusinessType.OTHER)
    @GetMapping("/notice/{receivableId}")
    public AjaxResult getNoticePrintData(@PathVariable("receivableId") String receivableId) {
        if (receivableId == null || receivableId.trim().isEmpty()) {
            return AjaxResult.error("应收记录ID不能为空");
        }
        ResiNoticePrintVo result = printService.getNoticePrintData(receivableId);
        return AjaxResult.success(result);
    }

    /**
     * 批量获取缴费通知单打印数据（按房间分组）
     */
    @ApiOperation("收银台-缴费通知单打印数据（批量）")
    @PreAuthorize("@ss.hasPermi('resi:receivable:list')")
    @Log(title = "住宅收费-缴费通知单批量打印", businessType = BusinessType.OTHER)
    @PostMapping("/notice/batch")
    public AjaxResult batchNoticePrintData(@RequestBody List<String> receivableIds) {
        if (receivableIds == null || receivableIds.isEmpty()) {
            return AjaxResult.error("请至少选择一条应收记录");
        }
        List<ResiNoticePrintVo> result = printService.batchNoticePrintData(receivableIds);
        return AjaxResult.success(result);
    }
}
