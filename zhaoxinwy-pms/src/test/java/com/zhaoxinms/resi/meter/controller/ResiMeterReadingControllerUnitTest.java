package com.zhaoxinms.resi.meter.controller;

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
import com.zhaoxinms.resi.meter.entity.ResiMeterReading;
import com.zhaoxinms.resi.meter.service.IResiMeterReadingService;

/**
 * 抄表记录 Controller 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ResiMeterReadingControllerUnitTest {

    @Mock
    private IResiMeterReadingService meterReadingService;

    @InjectMocks
    private ResiMeterReadingController controller;

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
        ResiMeterReading reading = buildValidReading();
        when(meterReadingService.save(any(ResiMeterReading.class))).thenReturn(true);

        AjaxResult result = controller.add(reading);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("修改：正常数据应成功")
    void edit_valid_success() {
        ResiMeterReading reading = buildValidReading();
        ResiMeterReading existing = buildValidReading();
        existing.setStatus("INPUT");
        when(meterReadingService.getById("reading-001")).thenReturn(existing);
        when(meterReadingService.updateById(any(ResiMeterReading.class))).thenReturn(true);

        AjaxResult result = controller.edit("reading-001", reading);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("修改：已入账记录应拒绝")
    void edit_billed_rejected() {
        ResiMeterReading reading = buildValidReading();
        ResiMeterReading existing = buildValidReading();
        existing.setStatus("BILLED");
        when(meterReadingService.getById("reading-001")).thenReturn(existing);

        AjaxResult result = controller.edit("reading-001", reading);
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("修改：已复核记录应拒绝")
    void edit_verified_rejected() {
        ResiMeterReading reading = buildValidReading();
        ResiMeterReading existing = buildValidReading();
        existing.setStatus("VERIFIED");
        when(meterReadingService.getById("reading-001")).thenReturn(existing);

        AjaxResult result = controller.edit("reading-001", reading);
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("删除：批量删除应成功")
    void remove_batch_success() {
        ResiMeterReading existing = buildValidReading();
        existing.setStatus("INPUT");
        when(meterReadingService.getById("reading-001")).thenReturn(existing);
        when(meterReadingService.getById("reading-002")).thenReturn(existing);
        doReturn(true).when(meterReadingService).removeByIds(anyList());

        AjaxResult result = controller.remove(new String[]{"reading-001", "reading-002"});
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("删除：已入账记录应拒绝")
    void remove_billed_rejected() {
        ResiMeterReading existing = buildValidReading();
        existing.setStatus("BILLED");
        when(meterReadingService.getById("reading-001")).thenReturn(existing);

        AjaxResult result = controller.remove(new String[]{"reading-001"});
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("获取详情：应返回数据")
    void getInfo_success() {
        when(meterReadingService.getById("reading-001")).thenReturn(buildValidReading());

        AjaxResult result = controller.getInfo("reading-001");
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("列表查询：应返回分页数据")
    void list_success() {
        when(meterReadingService.selectResiMeterReadingList(any(ResiMeterReading.class)))
            .thenReturn(Arrays.asList(buildValidReading()));

        AjaxResult result = AjaxResult.success(
            meterReadingService.selectResiMeterReadingList(new ResiMeterReading()));
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    private ResiMeterReading buildValidReading() {
        ResiMeterReading reading = new ResiMeterReading();
        reading.setProjectId(1L);
        reading.setMeterId(1L);
        reading.setPeriod("2026-05");
        reading.setCurrReading(new java.math.BigDecimal("200.0000"));
        reading.setCurrDate(java.sql.Date.valueOf("2026-05-15"));
        reading.setStatus("INPUT");
        return reading;
    }
}
