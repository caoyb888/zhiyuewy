package com.zhaoxinms.resi.meter.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.resi.TestSecurityContext;
import com.zhaoxinms.resi.archive.entity.ResiMeterDevice;
import com.zhaoxinms.resi.archive.entity.ResiRoom;
import com.zhaoxinms.resi.archive.service.IResiMeterDeviceService;
import com.zhaoxinms.resi.archive.service.IResiRoomService;
import com.zhaoxinms.resi.common.ResiConstants;
import com.zhaoxinms.resi.meter.dto.ResiMeterShareCalcReq;
import com.zhaoxinms.resi.meter.dto.ResiMeterShareCalcResultVo;
import com.zhaoxinms.resi.meter.dto.ResiMeterShareDetailVo;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;
import com.zhaoxinms.resi.receivable.service.IResiReceivableService;
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

    @Mock
    private IResiRoomService roomService;

    @Mock
    private IResiReceivableService receivableService;

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

    // ==================== S3-06 测试：入账与预警 ====================

    @Test
    @DisplayName("TC-METER-001-05：读数回退场景，允许保存（预警但不阻止）")
    void save_readingRollback_allowedButWarn() {
        when(meterReadingMapper.insert(any(ResiMeterReading.class))).thenReturn(1);

        ResiMeterDevice device = new ResiMeterDevice();
        device.setId(1L);
        device.setMultiplier(new BigDecimal("1.00"));
        when(meterDeviceService.getById(1L)).thenReturn(device);

        ResiMeterReading reading = buildValidReading();
        reading.setLastReading(new BigDecimal("200.0000"));
        reading.setCurrReading(new BigDecimal("150.0000")); // 读数回退：150 < 200

        boolean success = meterReadingService.save(reading);

        assertTrue(success);
        assertEquals(new BigDecimal("-50.0000"), reading.getRawUsage()); // (150-200)*1 = -50
        assertTrue(reading.getRawUsage().compareTo(BigDecimal.ZERO) < 0, "原始用量应为负数，触发预警条件");
    }

    @Test
    @DisplayName("TC-CASH-001-01等：单户入账成功，状态变为BILLED并生成应收")
    void bill_success() {
        ResiMeterReading reading = buildValidReading();
        reading.setId("reading-001");
        reading.setStatus(ResiConstants.METER_STATUS_INPUT);
        reading.setFeeId("fee-001");
        when(meterReadingMapper.selectById("reading-001")).thenReturn(reading);

        ResiReceivable receivable = new ResiReceivable();
        receivable.setId("recv-001");
        when(receivableService.createFromMeterReading(any(ResiMeterReading.class)))
            .thenReturn(receivable);

        when(meterReadingMapper.updateById(any(ResiMeterReading.class))).thenReturn(1);

        Map<String, Object> result = meterReadingService.bill("reading-001");

        assertEquals(true, result.get("success"));
        assertEquals("recv-001", result.get("receivableId"));
        // 验证状态更新为BILLED
        verify(meterReadingMapper).updateById(argThat(r ->
            ResiConstants.METER_STATUS_BILLED.equals(((ResiMeterReading) r).getStatus())
        ));
    }

    @Test
    @DisplayName("TC-METER-003-05：单户入账幂等，已入账记录再次入账应失败")
    void bill_alreadyBilled_rejected() {
        ResiMeterReading reading = buildValidReading();
        reading.setId("reading-001");
        reading.setStatus(ResiConstants.METER_STATUS_BILLED);
        when(meterReadingMapper.selectById("reading-001")).thenReturn(reading);

        Map<String, Object> result = meterReadingService.bill("reading-001");

        assertEquals(false, result.get("success"));
        assertTrue(((String) result.get("message")).contains("已入账"));
        verify(receivableService, never()).createFromMeterReading(any());
    }

    @Test
    @DisplayName("单户入账：已复核记录不可入账")
    void bill_alreadyVerified_rejected() {
        ResiMeterReading reading = buildValidReading();
        reading.setId("reading-001");
        reading.setStatus(ResiConstants.METER_STATUS_VERIFIED);
        when(meterReadingMapper.selectById("reading-001")).thenReturn(reading);

        Map<String, Object> result = meterReadingService.bill("reading-001");

        assertEquals(false, result.get("success"));
        assertTrue(((String) result.get("message")).contains("已复核"));
        verify(receivableService, never()).createFromMeterReading(any());
    }

    @Test
    @DisplayName("TC-METER-003：批量入账成功，生成对应应收记录")
    void batchBill_success() {
        ResiMeterReading r1 = buildValidReading();
        r1.setId("r-001");
        r1.setStatus(ResiConstants.METER_STATUS_INPUT);
        r1.setFeeId("fee-001");

        ResiMeterReading r2 = buildValidReading();
        r2.setId("r-002");
        r2.setStatus(ResiConstants.METER_STATUS_INPUT);
        r2.setFeeId("fee-001");

        List<ResiMeterReading> readings = Arrays.asList(r1, r2);
        when(meterReadingMapper.selectList(any(QueryWrapper.class))).thenReturn(readings);

        ResiReceivable recv1 = new ResiReceivable();
        recv1.setId("recv-001");
        ResiReceivable recv2 = new ResiReceivable();
        recv2.setId("recv-002");
        when(receivableService.createFromMeterReading(r1)).thenReturn(recv1);
        when(receivableService.createFromMeterReading(r2)).thenReturn(recv2);

        when(meterReadingMapper.updateById(any(ResiMeterReading.class))).thenReturn(1);

        Map<String, Object> result = meterReadingService.batchBill(1L, "2026-05", null);

        assertEquals(2, result.get("total"));
        assertEquals(2, result.get("success"));
        assertEquals(0, result.get("skip"));
        assertEquals(0, result.get("fail"));
        verify(receivableService, times(2)).createFromMeterReading(any());
    }

    @Test
    @DisplayName("TC-METER-003-05：二次批量入账幂等，已入账记录被跳过不产生重复应收")
    void batchBill_idempotent_secondCall_skipAlreadyBilled() {
        // 第一次批量入账后，记录状态变为 BILLED
        ResiMeterReading r1 = buildValidReading();
        r1.setId("r-001");
        r1.setStatus(ResiConstants.METER_STATUS_BILLED); // 已入账
        r1.setReceivableId("recv-001");
        r1.setFeeId("fee-001");

        ResiMeterReading r2 = buildValidReading();
        r2.setId("r-002");
        r2.setStatus(ResiConstants.METER_STATUS_INPUT); // 未入账
        r2.setFeeId("fee-001");

        List<ResiMeterReading> readings = Arrays.asList(r1, r2);
        when(meterReadingMapper.selectList(any(QueryWrapper.class))).thenReturn(readings);

        ResiReceivable recv2 = new ResiReceivable();
        recv2.setId("recv-002");
        when(receivableService.createFromMeterReading(r2)).thenReturn(recv2);
        when(meterReadingMapper.updateById(any(ResiMeterReading.class))).thenReturn(1);

        // 第二次批量入账
        Map<String, Object> result = meterReadingService.batchBill(1L, "2026-05", null);

        assertEquals(2, result.get("total"));
        assertEquals(1, result.get("success")); // 只有r2成功
        assertEquals(1, result.get("skip"));    // r1已入账被跳过
        assertEquals(0, result.get("fail"));
        // 验证只调用了一次createFromMeterReading（仅对r2）
        verify(receivableService, times(1)).createFromMeterReading(any());
    }

    @Test
    @DisplayName("TC-METER-002-01~05：公摊计算6:4分摊，误差<0.01")
    void previewShare_areaRatio6to4_errorLessThan001() {
        // 总表100度，分户A=40度(60㎡)，分户B=30度(40㎡)，公摊=30度
        List<ResiMeterReading> readings = Arrays.asList(
            buildShareReading("R-Total", 0L, null, 100, 1),
            buildShareReading("R-A", 1L, "A", 40, 0),
            buildShareReading("R-B", 2L, "B", 30, 0)
        );
        when(meterReadingMapper.selectAllShareGroupReadings(1L, "2026-05"))
            .thenReturn(readings);

        when(roomService.getById(1L)).thenReturn(buildRoom("A", new BigDecimal("60.00")));
        when(roomService.getById(2L)).thenReturn(buildRoom("B", new BigDecimal("40.00")));

        ResiMeterShareCalcReq req = new ResiMeterShareCalcReq();
        req.setProjectId(1L);
        req.setPeriod("2026-05");

        List<ResiMeterShareCalcResultVo> results = meterReadingService.previewShare(req);

        assertEquals(1, results.size());
        ResiMeterShareCalcResultVo result = results.get(0);
        assertEquals(new BigDecimal("30.0000"), result.getShareTotal());

        ResiMeterShareDetailVo dA = result.getDetails().get(0);
        ResiMeterShareDetailVo dB = result.getDetails().get(1);

        // A 分摊：30 × 60/100 = 18.00
        assertEquals(new BigDecimal("18.0000"), dA.getShareAmount());
        // B 分摊：30 - 18.00 = 12.00（差额调整）
        assertEquals(new BigDecimal("12.0000"), dB.getShareAmount());

        // 守恒验证
        BigDecimal sum = dA.getShareAmount().add(dB.getShareAmount());
        BigDecimal diff = sum.subtract(new BigDecimal("30.0000")).abs();
        assertTrue(diff.compareTo(new BigDecimal("0.0100")) < 0,
            "误差应小于0.01，实际：" + diff);
    }

    // ==================== 公摊计算测试 ====================

    @Test
    @DisplayName("公摊计算预览：标准场景（总表100度，分户75度，公摊25度）")
    void previewShare_standardScenario() {
        // 准备数据：公摊组 G01
        List<ResiMeterReading> readings = Arrays.asList(
            buildShareReading("R-Total", 0L, null, 100, 1),   // 总表（无房间）
            buildShareReading("R-01", 1L, "101", 30, 0),      // 分户 E01，60㎡
            buildShareReading("R-02", 2L, "102", 25, 0),      // 分户 E02，40㎡
            buildShareReading("R-03", 3L, "103", 20, 0)       // 分户 E03，50㎡
        );
        when(meterReadingMapper.selectShareGroupReadings(1L, "2026-05", "G01"))
            .thenReturn(readings);

        // Mock 房间面积
        when(roomService.getById(1L)).thenReturn(buildRoom("101", new BigDecimal("60.00")));
        when(roomService.getById(2L)).thenReturn(buildRoom("102", new BigDecimal("40.00")));
        when(roomService.getById(3L)).thenReturn(buildRoom("103", new BigDecimal("50.00")));

        ResiMeterShareCalcReq req = new ResiMeterShareCalcReq();
        req.setProjectId(1L);
        req.setPeriod("2026-05");
        req.setPublicGroup("G01");

        List<ResiMeterShareCalcResultVo> results = meterReadingService.previewShare(req);

        assertEquals(1, results.size());
        ResiMeterShareCalcResultVo result = results.get(0);
        assertEquals("G01", result.getPublicGroup());
        assertEquals(new BigDecimal("100.0000"), result.getTotalUsage());
        assertEquals(new BigDecimal("75.0000"), result.getSubTotalUsage());
        assertEquals(new BigDecimal("25.0000"), result.getShareTotal());
        assertEquals(new BigDecimal("150.00"), result.getTotalArea());
        assertEquals(3, result.getRoomCount());

        // 验证分摊量
        List<ResiMeterShareDetailVo> details = result.getDetails();
        assertEquals(3, details.size());

        // 101室：25 × 60/150 = 10.00
        ResiMeterShareDetailVo d1 = details.get(0);
        assertEquals(new BigDecimal("10.0000"), d1.getShareAmount());
        assertEquals(new BigDecimal("40.0000"), d1.getBilledUsage()); // 30 + 10 = 40

        // 102室：25 × 40/150 = 6.6667（保留4位小数）
        ResiMeterShareDetailVo d2 = details.get(1);
        assertEquals(new BigDecimal("6.6667"), d2.getShareAmount());
        assertEquals(new BigDecimal("31.6667"), d2.getBilledUsage()); // 25 + 6.6667 = 31.6667

        // 103室：差额调整 = 25 - 10.00 - 6.6667 = 8.3333
        ResiMeterShareDetailVo d3 = details.get(2);
        assertEquals(new BigDecimal("8.3333"), d3.getShareAmount());
        assertEquals(new BigDecimal("28.3333"), d3.getBilledUsage()); // 20 + 8.3333 = 28.3333

        // 守恒验证：三户分摊量之和 = 25.0000（误差 < 0.01）
        BigDecimal sumShare = d1.getShareAmount().add(d2.getShareAmount()).add(d3.getShareAmount());
        BigDecimal diff = sumShare.subtract(new BigDecimal("25.0000")).abs();
        assertTrue(diff.compareTo(new BigDecimal("0.0001")) <= 0,
            "分摊量之和误差应小于0.0001，实际差值：" + diff);
    }

    @Test
    @DisplayName("公摊计算：无公摊量（总表=分户合计）")
    void previewShare_zeroShare() {
        List<ResiMeterReading> readings = Arrays.asList(
            buildShareReading("R-Total", 0L, null, 100, 1),
            buildShareReading("R-01", 1L, "101", 40, 0),
            buildShareReading("R-02", 2L, "102", 60, 0)
        );
        when(meterReadingMapper.selectShareGroupReadings(1L, "2026-05", "G02"))
            .thenReturn(readings);

        when(roomService.getById(1L)).thenReturn(buildRoom("101", new BigDecimal("60.00")));
        when(roomService.getById(2L)).thenReturn(buildRoom("102", new BigDecimal("40.00")));

        ResiMeterShareCalcReq req = new ResiMeterShareCalcReq();
        req.setProjectId(1L);
        req.setPeriod("2026-05");
        req.setPublicGroup("G02");

        List<ResiMeterShareCalcResultVo> results = meterReadingService.previewShare(req);

        assertEquals(1, results.size());
        assertEquals(new BigDecimal("0.0000"), results.get(0).getShareTotal());
        for (ResiMeterShareDetailVo detail : results.get(0).getDetails()) {
            assertEquals(new BigDecimal("0.0000"), detail.getShareAmount());
        }
    }

    @Test
    @DisplayName("公摊计算：总表缺失应报错")
    void previewShare_missingTotalMeter_shouldThrow() {
        List<ResiMeterReading> readings = Arrays.asList(
            buildShareReading("R-01", 1L, "101", 40, 0),
            buildShareReading("R-02", 2L, "102", 60, 0)
        );
        when(meterReadingMapper.selectShareGroupReadings(1L, "2026-05", "G03"))
            .thenReturn(readings);

        ResiMeterShareCalcReq req = new ResiMeterShareCalcReq();
        req.setProjectId(1L);
        req.setPeriod("2026-05");
        req.setPublicGroup("G03");

        ServiceException ex = assertThrows(ServiceException.class,
            () -> meterReadingService.previewShare(req));
        assertTrue(ex.getMessage().contains("缺少公摊总表"));
    }

    @Test
    @DisplayName("公摊计算：负公摊量按0处理")
    void previewShare_negativeShare_shouldBeZero() {
        List<ResiMeterReading> readings = Arrays.asList(
            buildShareReading("R-Total", 0L, null, 50, 1),   // 总表50度
            buildShareReading("R-01", 1L, "101", 60, 0)      // 分户60度（超过总表）
        );
        when(meterReadingMapper.selectShareGroupReadings(1L, "2026-05", "G04"))
            .thenReturn(readings);

        when(roomService.getById(1L)).thenReturn(buildRoom("101", new BigDecimal("100.00")));

        ResiMeterShareCalcReq req = new ResiMeterShareCalcReq();
        req.setProjectId(1L);
        req.setPeriod("2026-05");
        req.setPublicGroup("G04");

        List<ResiMeterShareCalcResultVo> results = meterReadingService.previewShare(req);

        assertEquals(0, results.get(0).getShareTotal().compareTo(BigDecimal.ZERO));
        assertEquals(0, results.get(0).getDetails().get(0).getShareAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("公摊计算持久化：更新数据库")
    void calcShare_shouldUpdateDatabase() {
        List<ResiMeterReading> readings = Arrays.asList(
            buildShareReading("R-Total", 0L, null, 100, 1),
            buildShareReading("R-01", 1L, "101", 40, 0),
            buildShareReading("R-02", 2L, "102", 30, 0)
        );
        when(meterReadingMapper.selectAllShareGroupReadings(1L, "2026-05"))
            .thenReturn(readings);
        when(meterReadingMapper.updateById(any(ResiMeterReading.class))).thenReturn(1);

        when(roomService.getById(1L)).thenReturn(buildRoom("101", new BigDecimal("60.00")));
        when(roomService.getById(2L)).thenReturn(buildRoom("102", new BigDecimal("40.00")));

        ResiMeterShareCalcReq req = new ResiMeterShareCalcReq();
        req.setProjectId(1L);
        req.setPeriod("2026-05");

        List<ResiMeterShareCalcResultVo> results = meterReadingService.calcShare(req);

        assertEquals(1, results.size());
        // 验证 updateById 被调用了2次（两个分户表）
        verify(meterReadingMapper, times(2)).updateById(any(ResiMeterReading.class));
    }

    private ResiMeterReading buildShareReading(String id, Long meterId, String roomName,
                                                double rawUsage, int isPublic) {
        ResiMeterReading reading = new ResiMeterReading();
        reading.setId(id);
        reading.setMeterId(meterId);
        reading.setRoomId(meterId); // 简化：meterId 作为 roomId
        reading.setProjectId(1L);
        reading.setPeriod("2026-05");
        reading.setRawUsage(new BigDecimal(rawUsage).setScale(4, RoundingMode.HALF_UP));
        reading.setLossAmount(BigDecimal.ZERO);
        reading.setMeterCode("M" + meterId);
        reading.setRoomName(roomName);
        reading.setIsPublic(isPublic);
        reading.setPublicGroup("G01");
        return reading;
    }

    private ResiRoom buildRoom(String alias, BigDecimal area) {
        ResiRoom room = new ResiRoom();
        room.setRoomAlias(alias);
        room.setBuildingArea(area);
        return room;
    }
}
