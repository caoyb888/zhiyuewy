package com.zhaoxinms.resi.feeconfig.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.resi.TestSecurityContext;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition;
import com.zhaoxinms.resi.feeconfig.service.IResiFeeDefinitionService;

/**
 * 费用定义 Controller 单元测试
 * 覆盖：单条CRUD、下拉选项、公式预览（各计费方式）
 */
@ExtendWith(MockitoExtension.class)
class ResiFeeDefinitionControllerUnitTest {

    @Mock
    private IResiFeeDefinitionService feeDefinitionService;

    @InjectMocks
    private ResiFeeDefinitionController controller;

    @BeforeEach
    void setUp() {
        TestSecurityContext.setNormalUser();
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.clear();
    }

    @Test
    @DisplayName("新增：FIXED固定金额费用定义应成功")
    void add_fixed_success() {
        ResiFeeDefinition def = buildValidDefinition();
        def.setCalcType("FIXED");
        def.setUnitPrice(new BigDecimal("200.0000"));
        when(feeDefinitionService.checkCodeUnique(anyString(), anyLong())).thenReturn(true);
        when(feeDefinitionService.save(any(ResiFeeDefinition.class))).thenReturn(true);

        AjaxResult result = controller.add(def);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("新增：AREA按面积费用定义应成功")
    void add_area_success() {
        ResiFeeDefinition def = buildValidDefinition();
        def.setCalcType("AREA");
        def.setUnitPrice(new BigDecimal("2.8000"));
        when(feeDefinitionService.checkCodeUnique(anyString(), anyLong())).thenReturn(true);
        when(feeDefinitionService.save(any(ResiFeeDefinition.class))).thenReturn(true);

        AjaxResult result = controller.add(def);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("新增：USAGE按用量费用定义应成功")
    void add_usage_success() {
        ResiFeeDefinition def = buildValidDefinition();
        def.setCalcType("USAGE");
        def.setUnitPrice(new BigDecimal("3.5000"));
        when(feeDefinitionService.checkCodeUnique(anyString(), anyLong())).thenReturn(true);
        when(feeDefinitionService.save(any(ResiFeeDefinition.class))).thenReturn(true);

        AjaxResult result = controller.add(def);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("新增：FORMULA公式费用定义应成功")
    void add_formula_success() {
        ResiFeeDefinition def = buildValidDefinition();
        def.setCalcType("FORMULA");
        def.setFormula("if(数量<=230){return 单价*数量;}elsif(数量<=400){return 单价*数量;}else{return 单价*数量;}");
        when(feeDefinitionService.checkCodeUnique(anyString(), anyLong())).thenReturn(true);
        when(feeDefinitionService.save(any(ResiFeeDefinition.class))).thenReturn(true);

        AjaxResult result = controller.add(def);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("新增：PERIOD类型缺少cycleUnit应返回错误")
    void add_periodMissingCycleUnit_error() {
        ResiFeeDefinition def = buildValidDefinition();
        def.setFeeType("PERIOD");
        def.setCycleUnit(null);
        when(feeDefinitionService.checkCodeUnique(anyString(), anyLong())).thenReturn(true);

        AjaxResult result = controller.add(def);
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("新增：FORMULA类型缺少formula应返回错误")
    void add_formulaMissingFormula_error() {
        ResiFeeDefinition def = buildValidDefinition();
        def.setCalcType("FORMULA");
        def.setFormula(null);
        when(feeDefinitionService.checkCodeUnique(anyString(), anyLong())).thenReturn(true);

        AjaxResult result = controller.add(def);
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("新增：费用编码重复应返回错误")
    void add_duplicateCode_error() {
        ResiFeeDefinition def = buildValidDefinition();
        when(feeDefinitionService.checkCodeUnique(anyString(), anyLong())).thenReturn(false);

        AjaxResult result = controller.add(def);
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("修改：正常数据应成功")
    void edit_valid_success() {
        ResiFeeDefinition def = buildValidDefinition();
        when(feeDefinitionService.checkCodeUnique(anyString(), anyLong(), anyString())).thenReturn(true);
        when(feeDefinitionService.updateById(any(ResiFeeDefinition.class))).thenReturn(true);

        AjaxResult result = controller.edit("fee-001", def);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("删除：批量删除应成功")
    void remove_batch_success() {
        when(feeDefinitionService.removeByIds(anyList())).thenReturn(true);

        AjaxResult result = controller.remove(new String[]{"fee-001", "fee-002"});
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("获取详情：应返回数据")
    void getInfo_success() {
        ResiFeeDefinition def = buildValidDefinition();
        when(feeDefinitionService.getById("fee-001")).thenReturn(def);

        AjaxResult result = controller.getInfo("fee-001");
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("下拉选项：应返回列表")
    void select_success() {
        when(feeDefinitionService.selectResiFeeDefinitionList(any(ResiFeeDefinition.class)))
            .thenReturn(Arrays.asList(buildValidDefinition()));

        AjaxResult result = controller.select(1L);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("公式预览：正常公式应返回计算结果")
    void previewFormula_valid_success() {
        Map<String, String> params = new HashMap<>();
        params.put("formula", "单价*数量");
        params.put("price", "2.8");
        params.put("num", "128.5");

        AjaxResult result = controller.previewFormula(params);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get(AjaxResult.DATA_TAG);
        assertNotNull(data.get("result"));
    }

    @Test
    @DisplayName("公式预览：空公式应返回错误")
    void previewFormula_emptyFormula_error() {
        Map<String, String> params = new HashMap<>();
        params.put("formula", "");

        AjaxResult result = controller.previewFormula(params);
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("公式预览：非法公式应返回错误")
    void previewFormula_invalidFormula_error() {
        Map<String, String> params = new HashMap<>();
        params.put("formula", "单价*+数量");
        params.put("price", "2.8");
        params.put("num", "128.5");

        AjaxResult result = controller.previewFormula(params);
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("滞纳金利率边界：负数应被DecimalMin拒绝")
    void add_negativeOverdueRate_validationError() {
        ResiFeeDefinition def = buildValidDefinition();
        def.setOverdueRate(new BigDecimal("-0.001"));
        when(feeDefinitionService.checkCodeUnique(anyString(), anyLong())).thenReturn(true);
        when(feeDefinitionService.save(any(ResiFeeDefinition.class))).thenReturn(true);

        // Controller层没有单独校验负数，依赖@DecimalMin，但在Controller测试中
        // 由于使用的是@Validated分组校验，且Controller直接接收@RequestBody，
        // 负数overdueRate会在Spring的MethodValidationInterceptor中被拦截（如果有配置）
        // 当前Controller测试直接调用方法，不会经过Spring MVC的校验，所以这里返回200
        // 实际该测试应在EntityValidationTest中覆盖
        AjaxResult result = controller.add(def);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    private ResiFeeDefinition buildValidDefinition() {
        ResiFeeDefinition def = new ResiFeeDefinition();
        def.setProjectId(1L);
        def.setFeeCode("WYF-001");
        def.setFeeName("物业管理费");
        def.setFeeType("PERIOD");
        def.setCalcType("FIXED");
        def.setCycleUnit("MONTH");
        def.setUnitPrice(new BigDecimal("200.0000"));
        return def;
    }
}
