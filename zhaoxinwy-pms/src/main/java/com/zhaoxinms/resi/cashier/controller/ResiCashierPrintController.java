package com.zhaoxinms.resi.cashier.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhaoxinms.common.core.controller.BaseController;
import com.zhaoxinms.common.core.domain.AjaxResult;
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
}
