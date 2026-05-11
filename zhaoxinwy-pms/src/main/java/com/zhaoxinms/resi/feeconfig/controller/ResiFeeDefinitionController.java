package com.zhaoxinms.resi.feeconfig.controller;

import java.math.BigDecimal;
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
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.common.ResiConstants;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition;
import com.zhaoxinms.resi.feeconfig.service.IResiFeeDefinitionService;
import com.zhaoxinms.util.DynamicExpressiontUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 费用定义 Controller
 *
 * @author zhaoxinms
 */
@Api(tags = "住宅收费-费用定义")
@RestController
@RequestMapping("/resi/feeconfig/definition")
public class ResiFeeDefinitionController extends BaseController {

    @Autowired
    private IResiFeeDefinitionService feeDefinitionService;

    /**
     * 查询费用定义列表
     */
    @ApiOperation("查询费用定义列表")
    @PreAuthorize("@ss.hasPermi('resi:definition:list')")
    @ResiProjectScope
    @GetMapping
    public TableDataInfo list(ResiFeeDefinition feeDefinition) {
        startPage();
        List<ResiFeeDefinition> list = feeDefinitionService.selectResiFeeDefinitionList(feeDefinition);
        return getDataTable(list);
    }

    /**
     * 获取费用定义详情
     */
    @ApiOperation("获取费用定义详情")
    @PreAuthorize("@ss.hasPermi('resi:definition:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(feeDefinitionService.getById(id));
    }

    /**
     * 新增费用定义
     */
    @ApiOperation("新增费用定义")
    @PreAuthorize("@ss.hasPermi('resi:definition:add')")
    @Log(title = "住宅收费-费用定义", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody @Validated(AddGroup.class) ResiFeeDefinition feeDefinition) {
        // 校验费用编码唯一性
        if (!feeDefinitionService.checkCodeUnique(feeDefinition.getFeeCode(), feeDefinition.getProjectId())) {
            return AjaxResult.error("新增失败，费用编码 '" + feeDefinition.getFeeCode() + "' 在该项目下已存在");
        }

        // 业务校验：PERIOD 类型时 cycle_unit 必填
        if (ResiConstants.FEE_TYPE_PERIOD.equals(feeDefinition.getFeeType())
                && StringUtils.isBlank(feeDefinition.getCycleUnit())) {
            return AjaxResult.error("周期费的计费周期单位不能为空");
        }

        // 业务校验：FORMULA 类型时 formula 必填
        if (ResiConstants.CALC_TYPE_FORMULA.equals(feeDefinition.getCalcType())
                && StringUtils.isBlank(feeDefinition.getFormula())) {
            return AjaxResult.error("自定义公式类型的计费公式不能为空");
        }

        // 默认值处理
        if (feeDefinition.getCycleValue() == null) {
            feeDefinition.setCycleValue(1);
        }
        if (feeDefinition.getOverdueEnable() == null) {
            feeDefinition.setOverdueEnable(0);
        }
        if (feeDefinition.getOverdueDays() == null) {
            feeDefinition.setOverdueDays(0);
        }
        if (feeDefinition.getRoundType() == null) {
            feeDefinition.setRoundType(ResiConstants.ROUND_TYPE_ROUND);
        }
        if (feeDefinition.getEarmarkEnable() == null) {
            feeDefinition.setEarmarkEnable(0);
        }
        if (feeDefinition.getSortCode() == null) {
            feeDefinition.setSortCode(0);
        }

        return toAjax(feeDefinitionService.save(feeDefinition));
    }

    /**
     * 修改费用定义
     */
    @ApiOperation("修改费用定义")
    @PreAuthorize("@ss.hasPermi('resi:definition:edit')")
    @Log(title = "住宅收费-费用定义", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") String id,
                           @RequestBody @Validated(EditGroup.class) ResiFeeDefinition feeDefinition) {
        feeDefinition.setId(id);

        // 校验费用编码唯一性（排除自身）
        if (!feeDefinitionService.checkCodeUnique(feeDefinition.getFeeCode(), feeDefinition.getProjectId(), id)) {
            return AjaxResult.error("修改失败，费用编码 '" + feeDefinition.getFeeCode() + "' 在该项目下已存在");
        }

        // 业务校验：PERIOD 类型时 cycle_unit 必填
        if (ResiConstants.FEE_TYPE_PERIOD.equals(feeDefinition.getFeeType())
                && StringUtils.isBlank(feeDefinition.getCycleUnit())) {
            return AjaxResult.error("周期费的计费周期单位不能为空");
        }

        // 业务校验：FORMULA 类型时 formula 必填
        if (ResiConstants.CALC_TYPE_FORMULA.equals(feeDefinition.getCalcType())
                && StringUtils.isBlank(feeDefinition.getFormula())) {
            return AjaxResult.error("自定义公式类型的计费公式不能为空");
        }

        return toAjax(feeDefinitionService.updateById(feeDefinition));
    }

    /**
     * 删除费用定义
     */
    @ApiOperation("删除费用定义")
    @PreAuthorize("@ss.hasPermi('resi:definition:remove')")
    @Log(title = "住宅收费-费用定义", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(feeDefinitionService.removeByIds(java.util.Arrays.asList(ids)));
    }

    /**
     * 获取费用定义下拉选项
     */
    @ApiOperation("获取费用定义下拉选项")
    @PreAuthorize("@ss.hasPermi('resi:definition:list')")
    @GetMapping("/select")
    public AjaxResult select(Long projectId) {
        if (projectId == null) {
            return AjaxResult.error("项目ID不能为空");
        }
        ResiFeeDefinition query = new ResiFeeDefinition();
        query.setProjectId(projectId);
        List<ResiFeeDefinition> list = feeDefinitionService.selectResiFeeDefinitionList(query);
        return AjaxResult.success(list);
    }

    /**
     * 公式预览
     */
    @ApiOperation("公式预览")
    @PreAuthorize("@ss.hasPermi('resi:definition:add') or @ss.hasPermi('resi:definition:edit')")
    @PostMapping("/preview-formula")
    public AjaxResult previewFormula(@RequestBody Map<String, String> params) {
        String formula = params.get("formula");
        String price = params.get("price");
        String num = params.get("num");

        if (StringUtils.isBlank(formula)) {
            return AjaxResult.error("公式不能为空");
        }
        if (StringUtils.isBlank(price)) {
            price = "1";
        }
        if (StringUtils.isBlank(num)) {
            num = "1";
        }

        try {
            // 先校验公式语法
            DynamicExpressiontUtil.validateExpress(formula);
            // 执行计算
            String result = DynamicExpressiontUtil.getExpressResult(formula, num, price);
            Map<String, Object> data = new HashMap<>();
            data.put("result", result);
            return AjaxResult.success(data);
        } catch (Exception e) {
            return AjaxResult.error("公式计算失败：" + e.getMessage());
        }
    }
}
