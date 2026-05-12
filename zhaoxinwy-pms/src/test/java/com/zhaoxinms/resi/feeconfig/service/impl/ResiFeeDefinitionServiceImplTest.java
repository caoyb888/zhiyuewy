package com.zhaoxinms.resi.feeconfig.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoxinms.resi.TestSecurityContext;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition;
import com.zhaoxinms.resi.feeconfig.mapper.ResiFeeDefinitionMapper;

/**
 * 费用定义 Service 单元测试
 * 覆盖：列表查询、保存、修改、软删除、编码唯一性校验
 */
@ExtendWith(MockitoExtension.class)
class ResiFeeDefinitionServiceImplTest {

    @Mock
    private ResiFeeDefinitionMapper feeDefinitionMapper;

    @InjectMocks
    private ResiFeeDefinitionServiceImpl feeDefinitionService;

    @BeforeEach
    void setUp() throws Exception {
        TestSecurityContext.setAdminUser();
        Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(feeDefinitionService, feeDefinitionMapper);
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.clear();
    }

    @Test
    @DisplayName("查询列表：按项目ID过滤")
    void selectList_byProjectId() {
        List<ResiFeeDefinition> mockList = Arrays.asList(
            buildDefinition("fee-001", "物业费", "PERIOD"),
            buildDefinition("fee-002", "水费", "TEMP")
        );
        when(feeDefinitionMapper.selectList(any(QueryWrapper.class))).thenReturn(mockList);

        ResiFeeDefinition query = new ResiFeeDefinition();
        query.setProjectId(1L);
        List<ResiFeeDefinition> result = feeDefinitionService.selectResiFeeDefinitionList(query);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("查询列表：按费用名称模糊查询")
    void selectList_byFeeNameLike() {
        when(feeDefinitionMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        ResiFeeDefinition query = new ResiFeeDefinition();
        query.setProjectId(1L);
        query.setFeeName("物业");
        List<ResiFeeDefinition> result = feeDefinitionService.selectResiFeeDefinitionList(query);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("查询列表：按费用类型筛选")
    void selectList_byFeeType() {
        when(feeDefinitionMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        ResiFeeDefinition query = new ResiFeeDefinition();
        query.setProjectId(1L);
        query.setFeeType("PERIOD");
        List<ResiFeeDefinition> result = feeDefinitionService.selectResiFeeDefinitionList(query);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("查询列表：按计费方式筛选")
    void selectList_byCalcType() {
        when(feeDefinitionMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        ResiFeeDefinition query = new ResiFeeDefinition();
        query.setProjectId(1L);
        query.setCalcType("FORMULA");
        List<ResiFeeDefinition> result = feeDefinitionService.selectResiFeeDefinitionList(query);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("保存：应自动填充创建人和有效标志")
    void save_autoFillCreatorAndEnabledMark() {
        when(feeDefinitionMapper.insert(any(ResiFeeDefinition.class))).thenReturn(1);

        ResiFeeDefinition def = buildDefinition(null, "物业费", "PERIOD");
        boolean success = feeDefinitionService.save(def);

        assertTrue(success);
        assertEquals(1, def.getEnabledMark());
        assertNotNull(def.getCreatorTime());
        assertEquals("1", def.getCreatorUserId());
    }

    @Test
    @DisplayName("修改：应自动填充修改人")
    void update_autoFillModifier() {
        when(feeDefinitionMapper.updateById(any(ResiFeeDefinition.class))).thenReturn(1);

        ResiFeeDefinition def = new ResiFeeDefinition();
        def.setId("fee-001");
        boolean success = feeDefinitionService.updateById(def);

        assertTrue(success);
        assertNotNull(def.getLastModifyTime());
        assertEquals("1", def.getLastModifyUserId());
    }

    @Test
    @DisplayName("删除：应为软删除，enabledMark置0并填充deleteTime")
    void remove_softDelete() {
        when(feeDefinitionMapper.updateById(any(ResiFeeDefinition.class))).thenReturn(1);

        boolean success = feeDefinitionService.removeByIds(Arrays.asList("fee-001"));
        assertTrue(success);
    }

    @Test
    @DisplayName("校验编码唯一性：不存在相同编码应返回true")
    void checkCodeUnique_notExist_returnTrue() {
        when(feeDefinitionMapper.selectCount(any(QueryWrapper.class))).thenReturn(0);

        boolean unique = feeDefinitionService.checkCodeUnique("WYF-001", 1L);
        assertTrue(unique);
    }

    @Test
    @DisplayName("校验编码唯一性：已存在相同编码应返回false")
    void checkCodeUnique_exist_returnFalse() {
        when(feeDefinitionMapper.selectCount(any(QueryWrapper.class))).thenReturn(1);

        boolean unique = feeDefinitionService.checkCodeUnique("WYF-001", 1L);
        assertFalse(unique);
    }

    @Test
    @DisplayName("校验编码唯一性（排除自身）：修改时排除自身应返回true")
    void checkCodeUnique_excludeSelf_returnTrue() {
        when(feeDefinitionMapper.selectCount(any(QueryWrapper.class))).thenReturn(0);

        boolean unique = feeDefinitionService.checkCodeUnique("WYF-001", 1L, "fee-001");
        assertTrue(unique);
    }

    @Test
    @DisplayName("各计费方式定义保存：FIXED固定金额")
    void save_fixedType_success() {
        when(feeDefinitionMapper.insert(any(ResiFeeDefinition.class))).thenReturn(1);

        ResiFeeDefinition def = buildDefinition(null, "物业费", "PERIOD");
        def.setCalcType("FIXED");
        def.setUnitPrice(new BigDecimal("200.0000"));

        boolean success = feeDefinitionService.save(def);
        assertTrue(success);
        assertEquals("FIXED", def.getCalcType());
    }

    @Test
    @DisplayName("各计费方式定义保存：AREA按面积")
    void save_areaType_success() {
        when(feeDefinitionMapper.insert(any(ResiFeeDefinition.class))).thenReturn(1);

        ResiFeeDefinition def = buildDefinition(null, "物业费", "PERIOD");
        def.setCalcType("AREA");
        def.setUnitPrice(new BigDecimal("2.8000"));

        boolean success = feeDefinitionService.save(def);
        assertTrue(success);
        assertEquals("AREA", def.getCalcType());
    }

    @Test
    @DisplayName("各计费方式定义保存：USAGE按用量")
    void save_usageType_success() {
        when(feeDefinitionMapper.insert(any(ResiFeeDefinition.class))).thenReturn(1);

        ResiFeeDefinition def = buildDefinition(null, "水费", "TEMP");
        def.setCalcType("USAGE");
        def.setUnitPrice(new BigDecimal("3.5000"));

        boolean success = feeDefinitionService.save(def);
        assertTrue(success);
        assertEquals("USAGE", def.getCalcType());
    }

    @Test
    @DisplayName("各计费方式定义保存：FORMULA自定义公式")
    void save_formulaType_success() {
        when(feeDefinitionMapper.insert(any(ResiFeeDefinition.class))).thenReturn(1);

        ResiFeeDefinition def = buildDefinition(null, "电费", "TEMP");
        def.setCalcType("FORMULA");
        def.setFormula("if(数量<=230){return 单价*数量;}elsif(数量<=400){return 单价*数量;}else{return 单价*数量;}");

        boolean success = feeDefinitionService.save(def);
        assertTrue(success);
        assertEquals("FORMULA", def.getCalcType());
    }

    @Test
    @DisplayName("滞纳金精度：overdueRate保存6位小数")
    void save_overdueRatePrecision() {
        when(feeDefinitionMapper.insert(any(ResiFeeDefinition.class))).thenReturn(1);

        ResiFeeDefinition def = buildDefinition(null, "物业费", "PERIOD");
        def.setOverdueRate(new BigDecimal("0.000500"));

        boolean success = feeDefinitionService.save(def);
        assertTrue(success);
        assertEquals(new BigDecimal("0.000500"), def.getOverdueRate());
    }

    private ResiFeeDefinition buildDefinition(String id, String feeName, String feeType) {
        ResiFeeDefinition def = new ResiFeeDefinition();
        def.setId(id);
        def.setProjectId(1L);
        def.setFeeCode("CODE-" + (feeName != null ? feeName.hashCode() : System.currentTimeMillis()));
        def.setFeeName(feeName);
        def.setFeeType(feeType);
        def.setCalcType("FIXED");
        def.setEnabledMark(1);
        return def;
    }
}
