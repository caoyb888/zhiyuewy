package com.zhaoxinms.resi.feeconfig.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.zhaoxinms.common.annotation.Log;
import com.zhaoxinms.common.core.controller.BaseController;
import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.common.core.page.TableDataInfo;
import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;
import com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig;
import com.zhaoxinms.resi.feeconfig.service.IResiTicketConfigService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 票据模板配置 Controller
 *
 * @author zhaoxinms
 */
@Api(tags = "住宅收费-票据配置")
@RestController
@RequestMapping("/resi/feeconfig/ticket")
public class ResiTicketConfigController extends BaseController {

    @Autowired
    private IResiTicketConfigService ticketConfigService;

    /**
     * 查询票据配置列表
     */
    @ApiOperation("查询票据配置列表")
    @PreAuthorize("@ss.hasPermi('resi:ticketConfig:list')")
    @ResiProjectScope
    @GetMapping
    public TableDataInfo list(ResiTicketConfig ticketConfig) {
        startPage();
        List<ResiTicketConfig> list = ticketConfigService.selectResiTicketConfigList(ticketConfig);
        return getDataTable(list);
    }

    /**
     * 获取票据配置详情
     */
    @ApiOperation("获取票据配置详情")
    @PreAuthorize("@ss.hasPermi('resi:ticketConfig:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        ResiTicketConfig config = ticketConfigService.getById(id);
        if (config == null || Integer.valueOf(0).equals(config.getEnabledMark())) {
            return AjaxResult.error("票据配置不存在或已删除");
        }
        return AjaxResult.success(config);
    }

    /**
     * 新增票据配置
     */
    @ApiOperation("新增票据配置")
    @PreAuthorize("@ss.hasPermi('resi:ticketConfig:add')")
    @Log(title = "住宅收费-票据配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody @Validated(AddGroup.class) ResiTicketConfig ticketConfig) {
        if (ticketConfig.getEnabledMark() == null) {
            ticketConfig.setEnabledMark(1);
        }
        return toAjax(ticketConfigService.save(ticketConfig));
    }

    /**
     * 修改票据配置
     */
    @ApiOperation("修改票据配置")
    @PreAuthorize("@ss.hasPermi('resi:ticketConfig:edit')")
    @Log(title = "住宅收费-票据配置", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") String id,
                           @RequestBody @Validated(EditGroup.class) ResiTicketConfig ticketConfig) {
        ticketConfig.setId(id);
        return toAjax(ticketConfigService.updateById(ticketConfig));
    }

    /**
     * 删除票据配置
     */
    @ApiOperation("删除票据配置")
    @PreAuthorize("@ss.hasPermi('resi:ticketConfig:remove')")
    @Log(title = "住宅收费-票据配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(ticketConfigService.removeByIds(Arrays.asList(ids)));
    }

    /**
     * 获取默认字段配置
     */
    @ApiOperation("获取默认字段配置")
    @PreAuthorize("@ss.hasPermi('resi:ticketConfig:list')")
    @GetMapping("/default-fields")
    public AjaxResult defaultFields() {
        List<Map<String, Object>> fields = Arrays.asList(
            createField("pay_no", "收据号", 1),
            createField("project_name", "项目名称", 2),
            createField("house_name", "房号", 3),
            createField("owner_name", "业主姓名", 4),
            createField("fee_name", "费用名称", 5),
            createField("period", "计费周期", 6),
            createField("amount", "应收金额", 7),
            createField("pay_amount", "实收金额", 8),
            createField("pay_date", "收款日期", 9),
            createField("pay_method", "收款方式", 10),
            createField("remark", "备注", 11)
        );
        return AjaxResult.success(fields);
    }

    /**
     * 票据预览
     */
    @ApiOperation("票据预览")
    @PreAuthorize("@ss.hasPermi('resi:ticketConfig:query')")
    @GetMapping("/preview")
    public AjaxResult preview(String id) {
        if (StringUtils.isBlank(id)) {
            return AjaxResult.error("配置ID不能为空");
        }
        ResiTicketConfig config = ticketConfigService.getById(id);
        if (config == null || Integer.valueOf(0).equals(config.getEnabledMark())) {
            return AjaxResult.error("票据配置不存在或已删除");
        }

        Map<String, Object> previewData = new HashMap<>();
        previewData.put("config", config);
        previewData.put("projectName", "示例项目");
        previewData.put("houseName", "1栋1单元101");
        previewData.put("ownerName", "张三");
        previewData.put("payNo", "SK202401010001");
        previewData.put("feeName", "物业管理费");
        previewData.put("period", "2024-01");
        previewData.put("amount", "120.00");
        previewData.put("payAmount", "120.00");
        previewData.put("payDate", "2024-01-15");
        previewData.put("payMethod", "微信支付");
        previewData.put("remark", config.getRemark());

        return AjaxResult.success(previewData);
    }

    private Map<String, Object> createField(String field, String label, int sort) {
        Map<String, Object> map = new HashMap<>();
        map.put("field", field);
        map.put("label", label);
        map.put("show", true);
        map.put("sort", sort);
        return map;
    }
}
