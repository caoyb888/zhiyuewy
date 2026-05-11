package com.zhaoxinms.resi.archive.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.zhaoxinms.base.util.DesUtil;
import com.zhaoxinms.common.annotation.Log;
import com.zhaoxinms.common.core.controller.BaseController;
import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.common.core.page.TableDataInfo;
import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.archive.entity.ResiCustomer;
import com.zhaoxinms.resi.archive.service.IResiCustomerService;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 客户档案 Controller
 *
 * @author zhaoxinms
 */
@Api(tags = "住宅收费-客户管理")
@RestController
@RequestMapping("/resi/archive/customer")
public class ResiCustomerController extends BaseController {

    @Autowired
    private IResiCustomerService customerService;

    /**
     * 查询客户列表
     */
    @ApiOperation("查询客户列表")
    @PreAuthorize("@ss.hasPermi('resi:customer:list')")
    @ResiProjectScope
    @GetMapping
    public TableDataInfo list(ResiCustomer customer) {
        startPage();
        List<ResiCustomer> list = customerService.selectResiCustomerList(customer);
        // 返回前将身份证号解密（前端负责脱敏展示）
        for (ResiCustomer c : list) {
            decryptIdCard(c);
        }
        return getDataTable(list);
    }

    /**
     * 获取客户详细信息
     */
    @ApiOperation("获取客户详情")
    @PreAuthorize("@ss.hasPermi('resi:customer:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        ResiCustomer customer = customerService.getById(id);
        decryptIdCard(customer);
        return AjaxResult.success(customer);
    }

    /**
     * 新增客户
     */
    @ApiOperation("新增客户")
    @PreAuthorize("@ss.hasPermi('resi:customer:add')")
    @Log(title = "住宅收费-客户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody @Validated(AddGroup.class) ResiCustomer customer) {
        // 空字符串转为 null，避免 @Pattern 校验失败
        if (customer.getIdCard() != null && customer.getIdCard().trim().isEmpty()) {
            customer.setIdCard(null);
        }
        // AES 加密身份证号
        if (StringUtils.isNotBlank(customer.getIdCard())) {
            customer.setIdCard(DesUtil.aesEncode(customer.getIdCard()));
        }
        customer.setCreateBy(SecurityUtils.getUsername());
        return toAjax(customerService.save(customer));
    }

    /**
     * 修改客户
     */
    @ApiOperation("修改客户")
    @PreAuthorize("@ss.hasPermi('resi:customer:edit')")
    @Log(title = "住宅收费-客户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") Long id,
                           @RequestBody @Validated(EditGroup.class) ResiCustomer customer) {
        customer.setId(id);
        ResiCustomer old = customerService.getById(id);
        // 空字符串转为 null，避免 @Pattern 校验失败；同时表示清空身份证号
        if (customer.getIdCard() != null && customer.getIdCard().trim().isEmpty()) {
            customer.setIdCard(null);
        }
        // 若传了身份证号，判断是否已为密文：能成功解密则视为密文，否则视为明文并加密
        if (StringUtils.isNotBlank(customer.getIdCard())) {
            if (!isEncrypted(customer.getIdCard())) {
                customer.setIdCard(DesUtil.aesEncode(customer.getIdCard()));
            }
        } else if (old != null) {
            // 若前端未传 idCard（null），保留旧值；若前端传空字符串（已转为null），表示清空
            customer.setIdCard(old.getIdCard());
        }
        customer.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(customerService.updateById(customer));
    }

    /**
     * 删除客户
     */
    @ApiOperation("删除客户")
    @PreAuthorize("@ss.hasPermi('resi:customer:remove')")
    @Log(title = "住宅收费-客户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(customerService.removeByIds(java.util.Arrays.asList(ids)));
    }

    /**
     * 判断身份证号是否已加密
     * 原理：密文为16进制字符串，长度固定且远大于明文18位；尝试解密成功即为密文
     */
    private boolean isEncrypted(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return false;
        }
        // 密文为16进制，长度通常远大于明文18位
        if (idCard.length() > 32) {
            return true;
        }
        // 若长度接近明文，尝试解密验证
        try {
            String decoded = DesUtil.aesDecode(idCard);
            return StringUtils.isNotBlank(decoded);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解密身份证号（内部方法）
     * 解密失败时保留原值，避免将合法数据覆盖为null
     */
    private void decryptIdCard(ResiCustomer customer) {
        if (customer != null && StringUtils.isNotBlank(customer.getIdCard())) {
            try {
                String decoded = DesUtil.aesDecode(customer.getIdCard());
                if (StringUtils.isNotBlank(decoded)) {
                    customer.setIdCard(decoded);
                }
                // 若 decoded 为 null 或空，保留原值（可能不是加密值）
            } catch (Exception e) {
                // 解密失败则保留原值
            }
        }
    }
}
