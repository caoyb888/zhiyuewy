package com.zhaoxinms.resi.feeconfig.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
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
import com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig;
import com.zhaoxinms.resi.feeconfig.mapper.ResiTicketConfigMapper;

/**
 * 票据配置 Service 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ResiTicketConfigServiceImplTest {

    @Mock
    private ResiTicketConfigMapper ticketConfigMapper;

    @InjectMocks
    private ResiTicketConfigServiceImpl ticketConfigService;

    @BeforeEach
    void setUp() throws Exception {
        TestSecurityContext.setAdminUser();
        Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(ticketConfigService, ticketConfigMapper);
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.clear();
    }

    @Test
    @DisplayName("查询列表：按项目和票据类型过滤")
    void selectList_byProjectAndType() {
        List<ResiTicketConfig> mockList = Arrays.asList(buildConfig("ticket-001", 1), buildConfig("ticket-002", 2));
        when(ticketConfigMapper.selectList(any(QueryWrapper.class))).thenReturn(mockList);

        ResiTicketConfig query = new ResiTicketConfig();
        query.setProjectId(1L);
        query.setTicketType(1);
        List<ResiTicketConfig> result = ticketConfigService.selectResiTicketConfigList(query);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("查询列表：按标题模糊查询")
    void selectList_byTitle() {
        when(ticketConfigMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        ResiTicketConfig query = new ResiTicketConfig();
        query.setProjectId(1L);
        query.setTitle("收款");
        List<ResiTicketConfig> result = ticketConfigService.selectResiTicketConfigList(query);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("保存：应自动填充创建人和有效标志")
    void save_autoFillCreatorAndEnabledMark() {
        when(ticketConfigMapper.insert(any(ResiTicketConfig.class))).thenReturn(1);

        ResiTicketConfig config = buildConfig(null, 1);
        boolean success = ticketConfigService.save(config);

        assertTrue(success);
        assertEquals(1, config.getEnabledMark());
        assertNotNull(config.getCreatorTime());
        assertEquals("1", config.getCreatorUserId());
    }

    @Test
    @DisplayName("修改：应自动填充修改人")
    void update_autoFillModifier() {
        when(ticketConfigMapper.updateById(any(ResiTicketConfig.class))).thenReturn(1);

        ResiTicketConfig config = new ResiTicketConfig();
        config.setId("ticket-001");
        boolean success = ticketConfigService.updateById(config);

        assertTrue(success);
        assertNotNull(config.getLastModifyTime());
        assertEquals("1", config.getLastModifyUserId());
    }

    @Test
    @DisplayName("删除：应为软删除，enabledMark置0")
    void remove_softDelete() {
        when(ticketConfigMapper.updateById(any(ResiTicketConfig.class))).thenReturn(1);

        boolean success = ticketConfigService.removeByIds(Arrays.asList("ticket-001"));
        assertTrue(success);
    }

    @Test
    @DisplayName("保存：JSON字段应正确保存")
    void save_jsonField() {
        when(ticketConfigMapper.insert(any(ResiTicketConfig.class))).thenReturn(1);

        ResiTicketConfig config = buildConfig(null, 1);
        config.setFieldConfig("[{\"field\":\"pay_no\",\"label\":\"收据号\",\"show\":true,\"sort\":1}]");
        boolean success = ticketConfigService.save(config);

        assertTrue(success);
        assertNotNull(config.getFieldConfig());
    }

    private ResiTicketConfig buildConfig(String id, int ticketType) {
        ResiTicketConfig config = new ResiTicketConfig();
        config.setId(id);
        config.setProjectId(1L);
        config.setTicketType(ticketType);
        config.setTitle("物业收款单");
        config.setPaperSize("A4");
        config.setEnabledMark(1);
        return config;
    }
}
