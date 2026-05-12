package com.zhaoxinms.resi.finance.controller;

import java.util.Date;
import java.util.List;

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
import com.zhaoxinms.common.core.page.TableDataInfo;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.resi.finance.dto.ResiPrePayAddReq;
import com.zhaoxinms.resi.finance.dto.ResiPrePayBatchOffsetReq;
import com.zhaoxinms.resi.finance.dto.ResiPrePayQuery;
import com.zhaoxinms.resi.finance.entity.ResiPreAccount;
import com.zhaoxinms.resi.finance.entity.ResiPrePay;
import com.zhaoxinms.resi.finance.service.IResiPrePayService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 预收款 Controller
 */
@Api(tags = "住宅收费-预收款")
@RestController
@RequestMapping("/resi/finance/pre-pay")
public class ResiPrePayController extends BaseController {

    @Autowired
    private IResiPrePayService prePayService;

    /**
     * 查询资源的预收款账户列表
     */
    @ApiOperation("查询预收款账户")
    @PreAuthorize("@ss.hasPermi('resi:finance:prepay:query')")
    @GetMapping("/accounts")
    public AjaxResult listAccounts(@RequestParam Long projectId,
                                    @RequestParam String resourceType,
                                    @RequestParam Long resourceId) {
        List<ResiPreAccount> list = prePayService.listAccounts(projectId, resourceType, resourceId);
        return AjaxResult.success(list);
    }

    /**
     * 收取预收款
     */
    @ApiOperation("收取预收款")
    @PreAuthorize("@ss.hasPermi('resi:finance:prepay:add')")
    @Log(title = "住宅收费-预收款-存入", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@RequestBody @Validated ResiPrePayAddReq req) {
        req.setCreatorUserId(String.valueOf(SecurityUtils.getUserId()));
        ResiPrePay result = prePayService.addPrePay(req);
        return AjaxResult.success(result);
    }

    /**
     * 查询预收款流水
     */
    @ApiOperation("预收款流水列表")
    @PreAuthorize("@ss.hasPermi('resi:finance:prepay:query')")
    @GetMapping("/list")
    public TableDataInfo list(ResiPrePayQuery query) {
        startPage();
        List<ResiPrePay> list = prePayService.listPayLogs(query.getAccountId());
        return getDataTable(list);
    }

    /**
     * 批量冲抵预收款
     */
    @ApiOperation("批量冲抵预收款")
    @PreAuthorize("@ss.hasPermi('resi:finance:prepay:offset')")
    @Log(title = "住宅收费-预收款-批量冲抵", businessType = BusinessType.UPDATE)
    @PostMapping("/batch-offset")
    public AjaxResult batchOffset(@RequestBody @Validated ResiPrePayBatchOffsetReq req) {
        req.setCreatorUserId(String.valueOf(SecurityUtils.getUserId()));
        prePayService.batchOffset(req);
        return AjaxResult.success("冲抵成功");
    }
}
