package com.zhaoxinms.resi.feeconfig.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig;
import com.zhaoxinms.resi.feeconfig.service.IResiTicketConfigService;

/**
 * 票据配置 Controller 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ResiTicketConfigControllerUnitTest {

    @Mock
    private IResiTicketConfigService ticketConfigService;

    @InjectMocks
    private ResiTicketConfigController controller;

    @BeforeEach
    void setUp() {
        TestSecurityContext.setNormalUser();
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.clear();
    }

    @Test
    @DisplayName("新增：正常数据应成功")
    void add_valid_success() {
        ResiTicketConfig config = buildValidConfig();
        when(ticketConfigService.save(any(ResiTicketConfig.class))).thenReturn(true);

        AjaxResult result = controller.add(config);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("修改：正常数据应成功")
    void edit_valid_success() {
        ResiTicketConfig config = buildValidConfig();
        when(ticketConfigService.updateById(any(ResiTicketConfig.class))).thenReturn(true);

        AjaxResult result = controller.edit("ticket-001", config);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("删除：批量删除应成功")
    void remove_batch_success() {
        when(ticketConfigService.removeByIds(anyList())).thenReturn(true);

        AjaxResult result = controller.remove(new String[]{"ticket-001", "ticket-002"});
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("获取详情：存在且有效应返回数据")
    void getInfo_exists_success() {
        ResiTicketConfig config = buildValidConfig();
        config.setEnabledMark(1);
        when(ticketConfigService.getById("ticket-001")).thenReturn(config);

        AjaxResult result = controller.getInfo("ticket-001");
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("获取详情：已删除应返回错误")
    void getInfo_deleted_error() {
        ResiTicketConfig config = buildValidConfig();
        config.setEnabledMark(0);
        when(ticketConfigService.getById("ticket-001")).thenReturn(config);

        AjaxResult result = controller.getInfo("ticket-001");
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("获取默认字段：应返回11个字段")
    void defaultFields_success() {
        AjaxResult result = controller.defaultFields();
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fields = (List<Map<String, Object>>) result.get(AjaxResult.DATA_TAG);
        assertEquals(11, fields.size());
        assertEquals("pay_no", fields.get(0).get("field"));
        assertEquals(true, fields.get(0).get("show"));
    }

    @Test
    @DisplayName("预览：有效配置应返回预览数据")
    void preview_valid_success() {
        ResiTicketConfig config = buildValidConfig();
        config.setEnabledMark(1);
        when(ticketConfigService.getById("ticket-001")).thenReturn(config);

        AjaxResult result = controller.preview("ticket-001");
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get(AjaxResult.DATA_TAG);
        assertNotNull(data.get("config"));
        assertEquals("示例项目", data.get("projectName"));
    }

    @Test
    @DisplayName("预览：ID为空应返回错误")
    void preview_blankId_error() {
        AjaxResult result = controller.preview("");
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
    }

    private ResiTicketConfig buildValidConfig() {
        ResiTicketConfig config = new ResiTicketConfig();
        config.setProjectId(1L);
        config.setTicketType(1);
        config.setTitle("物业收款单");
        config.setCollectOrg("XX物业管理有限公司");
        config.setPaperSize("A4");
        return config;
    }
}
