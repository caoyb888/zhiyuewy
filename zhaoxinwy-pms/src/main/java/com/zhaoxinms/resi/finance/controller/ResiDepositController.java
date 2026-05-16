package com.zhaoxinms.resi.finance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhaoxinms.common.annotation.Log;
import com.zhaoxinms.common.core.controller.BaseController;
import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.common.core.page.TableDataInfo;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.resi.finance.dto.ResiDepositQuery;
import com.zhaoxinms.resi.finance.dto.ResiDepositRefundReq;
import com.zhaoxinms.resi.finance.entity.ResiDeposit;
import com.zhaoxinms.resi.finance.service.IResiDepositService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 押金台账 Controller
 */
@Api(tags = "住宅收费-押金台账")
@RestController
@RequestMapping("/resi/finance/deposit")
public class ResiDepositController extends BaseController {

    @Autowired
    private IResiDepositService depositService;

    /**
     * 押金台账列表
     */
    @ApiOperation("押金台账-列表查询")
    @PreAuthorize("@ss.hasPermi('resi:finance:deposit:query')")
    @GetMapping("/list")
    public TableDataInfo list(ResiDepositQuery query) {
        startPage();
        List<ResiDeposit> list = depositService.list(query);
        return getDataTable(list);
    }

    /**
     * 押金详情
     */
    @ApiOperation("押金台账-详情")
    @PreAuthorize("@ss.hasPermi('resi:finance:deposit:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        ResiDeposit deposit = depositService.getById(id);
        if (deposit == null) {
            throw new ServiceException("押金记录不存在");
        }
        return AjaxResult.success(deposit);
    }

    /**
     * 押金退还
     */
    @ApiOperation("押金台账-退还")
    @PreAuthorize("@ss.hasPermi('resi:finance:deposit:refund')")
    @Log(title = "住宅收费-押金-退还", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/refund")
    public AjaxResult refund(@PathVariable("id") String id,
                              @RequestBody @Validated ResiDepositRefundReq req) {
        req.setDepositId(id);
        String userId = String.valueOf(SecurityUtils.getUserId());
        ResiDeposit result = depositService.refund(req, userId);
        return AjaxResult.success(result);
    }
}
