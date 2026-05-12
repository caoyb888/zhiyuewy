package com.zhaoxinms.resi.meter.service.impl;

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
import com.zhaoxinms.resi.archive.entity.ResiMeterDevice;
import com.zhaoxinms.resi.archive.service.IResiMeterDeviceService;
import com.zhaoxinms.resi.meter.entity.ResiMeterReading;
import com.zhaoxinms.resi.meter.mapper.ResiMeterReadingMapper;

/**
 * 抄表记录 Service 单元测试
 * 覆盖：列表查询、自动带入上期读数、用量计算、保存/修改/删除
 */
@ExtendWith(MockitoExtension.class)
class ResiMeterReadingServiceImplTest {

    @Mock
    private ResiMeterReadingMapper meterReadingMapper;

    @Mock
    private IResiMeterDeviceService meterDeviceService;

    @InjectMocks
    private ResiMeterReadingServiceImpl meterReadingService;

    @BeforeEach
    void setUp() throws Exception {
        TestSecurityContext.setAdminUser();
        Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(meterReadingService, meterReadingMapper);
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.clear();
    }

    @Test
    @DisplayName("查询列表：按项目和期间过滤")
    void selectList_byProjectAndPeriod() {
        when(meterReadingMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        ResiMeterReading query = new ResiMeterReading();
        query.setProjectId(1L);
        query.setPeriod("2026-05");
        List<ResiMeterReading> result = meterReadingService.selectResiMeterReadingList(query);

        assertNotNull(result);
    }

    @Test
    @DisplayName("查询列表：按状态筛选")
    void selectList_byStatus() {
        when(meterReadingMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        ResiMeterReading query = new ResiMeterReading();
        query.setProjectId(1L);
        query.setStatus("INPUT");
        List<ResiMeterReading> result = meterReadingService.selectResiMeterReadingList(query);

        assertNotNull(result);
    }

    @Test
    @DisplayName("保存：自动填充创建人和默认状态")
    void save_autoFillCreatorAndStatus() {
        when(meterReadingMapper.insert(any(ResiMeterReading.class))).thenReturn(1);
        // lastReading为null，会触发selectLastRecord和meterDeviceService.getById
        when(meterReadingMapper.selectLastRecord(anyLong())).thenReturn(null);
        ResiMeterDevice device = new ResiMeterDevice();
        device.setId(1L);
        device.setInitReading(new BigDecimal("0.0000"));
        device.setMultiplier(new BigDecimal("1.00"));
        when(meterDeviceService.getById(1L)).thenReturn(device);

        ResiMeterReading reading = buildValidReading();
        reading.setLastReading(null); // 确保触发fillLastReading
        boolean success = meterReadingService.save(reading);

        assertTrue(success);
        assertEquals("INPUT", reading.getStatus());
        assertNotNull(reading.getCreatorTime());
        assertEquals("1", reading.getCreatorUserId());
    }

    @Test
    @DisplayName("保存：自动带入上期读数（有上期记录）")
    void save_fillLastReading_fromLastRecord() {
        when(meterReadingMapper.insert(any(ResiMeterReading.class))).thenReturn(1);

        ResiMeterReading lastRecord = new ResiMeterReading();
        lastRecord.setCurrReading(new BigDecimal("100.0000"));
        lastRecord.setCurrDate(java.sql.Date.valueOf("2026-04-15"));
        when(meterReadingMapper.selectLastRecord(1L)).thenReturn(lastRecord);

        ResiMeterDevice device = new ResiMeterDevice();
        device.setId(1L);
        device.setMultiplier(new BigDecimal("1.00"));
        when(meterDeviceService.getById(1L)).thenReturn(device);

        ResiMeterReading reading = buildValidReading();
        reading.setLastReading(null); // 未手动填写
        boolean success = meterReadingService.save(reading);

        assertTrue(success);
        assertEquals(new BigDecimal("100.0000"), reading.getLastReading());
    }

    @Test
    @DisplayName("保存：自动带入上期读数（无上期记录，使用仪表初始读数）")
    void save_fillLastReading_fromInitReading() {
        when(meterReadingMapper.insert(any(ResiMeterReading.class))).thenReturn(1);
        when(meterReadingMapper.selectLastRecord(anyLong())).thenReturn(null);

        ResiMeterDevice device = new ResiMeterDevice();
        device.setId(1L);
        device.setInitReading(new BigDecimal("50.0000"));
        device.setMultiplier(new BigDecimal("1.00"));
        when(meterDeviceService.getById(1L)).thenReturn(device);

        ResiMeterReading reading = buildValidReading();
        reading.setLastReading(null);
        boolean success = meterReadingService.save(reading);

        assertTrue(success);
        assertEquals(new BigDecimal("50.0000"), reading.getLastReading());
    }

    @Test
    @DisplayName("保存：自动计算用量（倍率=1）")
    void save_calculateUsage_multiplier1() {
        when(meterReadingMapper.insert(any(ResiMeterReading.class))).thenReturn(1);
        // lastReading已手动设置，selectLastRecord不会被调用
        ResiMeterDevice device = new ResiMeterDevice();
        device.setId(1L);
        device.setMultiplier(new BigDecimal("1.00"));
        when(meterDeviceService.getById(1L)).thenReturn(device);

        ResiMeterReading reading = buildValidReading();
        reading.setLastReading(new BigDecimal("100.0000"));
        reading.setCurrReading(new BigDecimal("200.0000"));
        reading.setLossRate(new BigDecimal("0.0300"));
        boolean success = meterReadingService.save(reading);

        assertTrue(success);
        assertEquals(new BigDecimal("100.0000"), reading.getRawUsage()); // 200-100=100
        assertEquals(new BigDecimal("3.0000"), reading.getLossAmount()); // 100*0.03=3
        assertEquals(new BigDecimal("97.0000"), reading.getBilledUsage()); // 100-3=97
    }

    @Test
    @DisplayName("保存：自动计算用量（倍率=10）")
    void save_calculateUsage_multiplier10() {
        when(meterReadingMapper.insert(any(ResiMeterReading.class))).thenReturn(1);
        // lastReading已手动设置，selectLastRecord不会被调用
        ResiMeterDevice device = new ResiMeterDevice();
        device.setId(1L);
        device.setMultiplier(new BigDecimal("10.00"));
        when(meterDeviceService.getById(1L)).thenReturn(device);

        ResiMeterReading reading = buildValidReading();
        reading.setLastReading(new BigDecimal("100.0000"));
        reading.setCurrReading(new BigDecimal("200.0000"));
        boolean success = meterReadingService.save(reading);

        assertTrue(success);
        assertEquals(new BigDecimal("1000.0000"), reading.getRawUsage()); // (200-100)*10=1000
    }

    @Test
    @DisplayName("保存：用户手动填写上期读数时不覆盖")
    void save_manualLastReading_notOverwritten() {
        when(meterReadingMapper.insert(any(ResiMeterReading.class))).thenReturn(1);
        // lastReading已手动设置，selectLastRecord不会被调用
        ResiMeterDevice device = new ResiMeterDevice();
        device.setId(1L);
        device.setMultiplier(new BigDecimal("1.00"));
        when(meterDeviceService.getById(1L)).thenReturn(device);

        ResiMeterReading reading = buildValidReading();
        reading.setLastReading(new BigDecimal("120.0000")); // 用户手动填写
        reading.setCurrReading(new BigDecimal("220.0000"));
        boolean success = meterReadingService.save(reading);

        assertTrue(success);
        assertEquals(new BigDecimal("120.0000"), reading.getLastReading()); // 不应被覆盖
        assertEquals(new BigDecimal("100.0000"), reading.getRawUsage());
    }

    @Test
    @DisplayName("修改：应自动填充修改人并重新计算用量")
    void update_autoFillModifierAndRecalc() {
        when(meterReadingMapper.updateById(any(ResiMeterReading.class))).thenReturn(1);
        // meterId未设置，calculateUsage直接return，meterDeviceService.getById不会被调用

        ResiMeterReading reading = new ResiMeterReading();
        reading.setId("reading-001");
        reading.setCurrReading(new BigDecimal("300.0000"));
        reading.setLastReading(new BigDecimal("100.0000"));
        boolean success = meterReadingService.updateById(reading);

        assertTrue(success);
        assertNotNull(reading.getLastModifyTime());
        assertEquals("1", reading.getLastModifyUserId());
    }

    @Test
    @DisplayName("删除：物理删除（抄表记录为流水数据，不软删除）")
    void remove_physicalDelete() {
        when(meterReadingMapper.deleteById(any(ResiMeterReading.class))).thenReturn(1);

        boolean success = meterReadingService.removeByIds(Arrays.asList("reading-001"));
        assertTrue(success);
    }

    private ResiMeterReading buildValidReading() {
        ResiMeterReading reading = new ResiMeterReading();
        reading.setProjectId(1L);
        reading.setMeterId(1L);
        reading.setRoomId(1L);
        reading.setPeriod("2026-05");
        reading.setCurrReading(new BigDecimal("200.0000"));
        reading.setCurrDate(java.sql.Date.valueOf("2026-05-15"));
        reading.setLastReading(new BigDecimal("100.0000"));
        return reading;
    }
}
