package com.zhaoxinms.resi.cashier.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zhaoxinms.common.annotation.Log;
import com.zhaoxinms.common.core.controller.BaseController;
import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.cashier.dto.ResiCashierCalcReq;
import com.zhaoxinms.resi.cashier.dto.ResiCashierCalcVo;
import com.zhaoxinms.resi.cashier.dto.ResiCashierCollectReq;
import com.zhaoxinms.resi.cashier.dto.ResiCashierCollectVo;
import com.zhaoxinms.resi.cashier.dto.ResiCashierRefundReq;
import com.zhaoxinms.resi.cashier.dto.ResiCashierRoomSearchVo;
import com.zhaoxinms.resi.cashier.dto.ResiCashierRoomSummaryVo;
import com.zhaoxinms.resi.cashier.dto.ResiCashierWriteOffReq;
import com.zhaoxinms.resi.cashier.service.IResiCashierService;
import com.zhaoxinms.resi.finance.entity.ResiPayLog;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 收银台 Controller
 */
@Api(tags = "住宅收费-收银台")
@RestController
@RequestMapping("/resi/cashier")
public class ResiCashierController extends BaseController {

    @Autowired
    private IResiCashierService cashierService;

    /**
     * 模糊搜索房间（按 room_alias / room_no / customer_name）
     */
    @ApiOperation("收银台-模糊搜索房间")
    @PreAuthorize("@ss.hasPermi('resi:cashier:query')")
    @GetMapping("/room/search")
    public AjaxResult searchRoom(@RequestParam String keyword,
                                  @RequestParam(required = false) Long projectId) {
        if (StringUtils.isBlank(keyword)) {
            return AjaxResult.error("搜索关键词不能为空");
        }
        List<ResiCashierRoomSearchVo> list = cashierService.searchRoom(keyword.trim(), projectId);
        return AjaxResult.success(list);
    }

    /**
     * 查询房间待缴费用
     */
    @ApiOperation("收银台-查询房间待缴费用")
    @PreAuthorize("@ss.hasPermi('resi:cashier:query')")
    @Log(title = "住宅收费-收银台-查询待缴费用", businessType = BusinessType.OTHER)
    @GetMapping("/room/{roomId}/receivables")
    public AjaxResult getRoomReceivables(@PathVariable("roomId") Long roomId,
                                          @RequestParam(required = false) Map<String, String> params) {
        if (roomId == null || roomId <= 0) {
            return AjaxResult.error("房间ID不能为空");
        }
        List<ResiReceivable> list = cashierService.getRoomReceivables(roomId, params);
        return AjaxResult.success(list);
    }

    /**
     * 查询房间费用汇总
     */
    @ApiOperation("收银台-查询房间费用汇总")
    @PreAuthorize("@ss.hasPermi('resi:cashier:query')")
    @GetMapping("/room/{roomId}/summary")
    public AjaxResult getRoomSummary(@PathVariable("roomId") Long roomId) {
        if (roomId == null || roomId <= 0) {
            return AjaxResult.error("房间ID不能为空");
        }
        ResiCashierRoomSummaryVo summary = cashierService.getRoomSummary(roomId);
        return AjaxResult.success(summary);
    }

    /**
     * 收款预览（仅计算不写库）
     */
    @ApiOperation("收银台-收款预览")
    @PreAuthorize("@ss.hasPermi('resi:cashier:collect')")
    @PostMapping("/calc")
    public AjaxResult calc(@RequestBody @Validated ResiCashierCalcReq req) {
        ResiCashierCalcVo result = cashierService.calc(req);
        return AjaxResult.success(result);
    }

    /**
     * 收款核心（事务+并发保护）
     */
    @ApiOperation("收银台-确认收款")
    @PreAuthorize("@ss.hasPermi('resi:cashier:collect')")
    @Log(title = "住宅收费-收银台-收款", businessType = BusinessType.UPDATE)
    @PostMapping("/collect")
    public AjaxResult collect(@RequestBody @Validated ResiCashierCollectReq req) {
        ResiCashierCollectVo result = cashierService.collect(req);
        return AjaxResult.success(result);
    }

    /**
     * 退款
     */
    @ApiOperation("收银台-退款")
    @PreAuthorize("@ss.hasPermi('resi:cashier:refund')")
    @Log(title = "住宅收费-收银台-退款", businessType = BusinessType.UPDATE)
    @PostMapping("/refund")
    public AjaxResult refund(@RequestBody @Validated ResiCashierRefundReq req) {
        ResiPayLog result = cashierService.refund(req);
        return AjaxResult.success(result);
    }

    /**
     * 冲红
     */
    @ApiOperation("收银台-冲红")
    @PreAuthorize("@ss.hasPermi('resi:cashier:writeOff')")
    @Log(title = "住宅收费-收银台-冲红", businessType = BusinessType.UPDATE)
    @PostMapping("/write-off")
    public AjaxResult writeOff(@RequestBody @Validated ResiCashierWriteOffReq req) {
        ResiPayLog result = cashierService.writeOff(req);
        return AjaxResult.success(result);
    }
}
