package com.zhaoxinms.resi.archive.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.resi.archive.entity.ResiCustomerAsset;
import com.zhaoxinms.resi.archive.service.IResiCustomerAssetService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 客户资产绑定 Controller
 *
 * @author zhaoxinms
 */
@Api(tags = "住宅收费-客户资产绑定")
@RestController
@RequestMapping("/resi/archive/customer")
public class ResiCustomerAssetController extends BaseController {

    @Autowired
    private IResiCustomerAssetService assetService;

    /**
     * 查询客户资产列表
     */
    @ApiOperation("查询客户资产列表")
    @PreAuthorize("@ss.hasPermi('resi:customer:query')")
    @GetMapping("/{customerId}/assets")
    public AjaxResult assets(@PathVariable("customerId") Long customerId) {
        List<ResiCustomerAsset> list = assetService.selectAssetsByCustomerId(customerId);
        return AjaxResult.success(list);
    }

    /**
     * 绑定资产
     */
    @ApiOperation("绑定资产")
    @PreAuthorize("@ss.hasPermi('resi:customer:add')")
    @Log(title = "住宅收费-客户资产绑定", businessType = BusinessType.INSERT)
    @PostMapping("/bind-asset")
    public AjaxResult bindAsset(@RequestBody ResiCustomerAsset asset) {
        asset.setBindDate(new Date());
        asset.setIsCurrent(1);
        asset.setCreateBy(SecurityUtils.getUsername());
        return toAjax(assetService.save(asset));
    }

    /**
     * 解绑资产
     */
    @ApiOperation("解绑资产")
    @PreAuthorize("@ss.hasPermi('resi:customer:edit')")
    @Log(title = "住宅收费-客户资产解绑", businessType = BusinessType.UPDATE)
    @DeleteMapping("/bind-asset/{id}")
    public AjaxResult unbindAsset(@PathVariable("id") Long id) {
        ResiCustomerAsset asset = assetService.getById(id);
        if (asset == null) {
            return AjaxResult.error("绑定记录不存在");
        }
        asset.setIsCurrent(0);
        asset.setUnbindDate(new Date());
        return toAjax(assetService.updateById(asset));
    }
}
