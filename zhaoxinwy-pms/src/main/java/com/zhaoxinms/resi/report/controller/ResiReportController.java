package com.zhaoxinms.resi.report.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhaoxinms.common.core.controller.BaseController;
import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.common.core.page.TableDataInfo;
import com.zhaoxinms.common.utils.poi.ExcelUtil;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;
import com.zhaoxinms.resi.report.dto.ResiArrearsDetailQuery;
import com.zhaoxinms.resi.report.dto.ResiArrearsDetailVo;
import com.zhaoxinms.resi.report.dto.ResiCollectionRateQuery;
import com.zhaoxinms.resi.report.dto.ResiCollectionRateVo;
import com.zhaoxinms.resi.report.dto.ResiReceivableMgmtQuery;
import com.zhaoxinms.resi.report.dto.ResiReceivableMgmtVo;
import com.zhaoxinms.resi.report.dto.ResiTransactionDetailQuery;
import com.zhaoxinms.resi.report.dto.ResiTransactionDetailVo;
import com.zhaoxinms.resi.report.dto.ResiTransactionSummaryQuery;
import com.zhaoxinms.resi.report.dto.ResiTransactionSummaryVo;
import com.zhaoxinms.resi.report.service.IResiReportService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 住宅收费-核心报表 Controller
 *
 * 5张核心报表：
 * 1. 交易汇总
 * 2. 交易明细
 * 3. 收费率报表
 * 4. 欠费明细
 * 5. 应收管理
 */
@Api(tags = "住宅收费-核心报表")
@RestController
@RequestMapping("/resi/report")
public class ResiReportController extends BaseController {

    @Autowired
    private IResiReportService reportService;

    /**
     * 交易汇总报表
     */
    @ApiOperation("交易汇总报表")
    @PreAuthorize("@ss.hasPermi('resi:report:transactionSummary:query')")
    @ResiProjectScope
    @GetMapping("/transaction-summary")
    public TableDataInfo transactionSummary(ResiTransactionSummaryQuery query, HttpServletResponse response) throws IOException {
        if (query.isExport()) {
            List<ResiTransactionSummaryVo> list = reportService.transactionSummary(query);
            ExcelUtil<ResiTransactionSummaryVo> util = new ExcelUtil<>(ResiTransactionSummaryVo.class);
            util.exportExcel(response, list, "交易汇总报表");
            return null;
        }
        startPage();
        List<ResiTransactionSummaryVo> list = reportService.transactionSummary(query);
        return getDataTable(list);
    }

    /**
     * 交易明细报表
     */
    @ApiOperation("交易明细报表")
    @PreAuthorize("@ss.hasPermi('resi:report:transactionDetail:query')")
    @ResiProjectScope
    @GetMapping("/transaction-detail")
    public TableDataInfo transactionDetail(ResiTransactionDetailQuery query, HttpServletResponse response) throws IOException {
        if (query.isExport()) {
            List<ResiTransactionDetailVo> list = reportService.transactionDetail(query);
            ExcelUtil<ResiTransactionDetailVo> util = new ExcelUtil<>(ResiTransactionDetailVo.class);
            util.exportExcel(response, list, "交易明细报表");
            return null;
        }
        startPage();
        List<ResiTransactionDetailVo> list = reportService.transactionDetail(query);
        return getDataTable(list);
    }

    /**
     * 收费率报表
     */
    @ApiOperation("收费率报表")
    @PreAuthorize("@ss.hasPermi('resi:report:collectionRate:query')")
    @ResiProjectScope
    @GetMapping("/collection-rate")
    public TableDataInfo collectionRate(ResiCollectionRateQuery query, HttpServletResponse response) throws IOException {
        if (query.isExport()) {
            List<ResiCollectionRateVo> list = reportService.collectionRate(query);
            ExcelUtil<ResiCollectionRateVo> util = new ExcelUtil<>(ResiCollectionRateVo.class);
            util.exportExcel(response, list, "收费率报表");
            return null;
        }
        startPage();
        List<ResiCollectionRateVo> list = reportService.collectionRate(query);
        return getDataTable(list);
    }

    /**
     * 欠费明细报表
     */
    @ApiOperation("欠费明细报表")
    @PreAuthorize("@ss.hasPermi('resi:report:arrearsDetail:query')")
    @ResiProjectScope
    @GetMapping("/arrears-detail")
    public TableDataInfo arrearsDetail(ResiArrearsDetailQuery query, HttpServletResponse response) throws IOException {
        if (query.isExport()) {
            List<ResiArrearsDetailVo> list = reportService.arrearsDetail(query);
            ExcelUtil<ResiArrearsDetailVo> util = new ExcelUtil<>(ResiArrearsDetailVo.class);
            util.exportExcel(response, list, "欠费明细报表");
            return null;
        }
        startPage();
        List<ResiArrearsDetailVo> list = reportService.arrearsDetail(query);
        return getDataTable(list);
    }

    /**
     * 应收管理报表
     */
    @ApiOperation("应收管理报表")
    @PreAuthorize("@ss.hasPermi('resi:report:receivableMgmt:query')")
    @ResiProjectScope
    @GetMapping("/receivable-mgmt")
    public TableDataInfo receivableMgmt(ResiReceivableMgmtQuery query, HttpServletResponse response) throws IOException {
        if (query.isExport()) {
            List<ResiReceivableMgmtVo> list = reportService.receivableMgmt(query);
            ExcelUtil<ResiReceivableMgmtVo> util = new ExcelUtil<>(ResiReceivableMgmtVo.class);
            util.exportExcel(response, list, "应收管理报表");
            return null;
        }
        startPage();
        List<ResiReceivableMgmtVo> list = reportService.receivableMgmt(query);
        return getDataTable(list);
    }
}
