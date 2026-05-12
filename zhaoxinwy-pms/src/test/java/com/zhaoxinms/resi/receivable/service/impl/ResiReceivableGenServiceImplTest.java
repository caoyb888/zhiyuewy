package com.zhaoxinms.resi.receivable.service.impl;

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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.zhaoxinms.resi.TestSecurityContext;
import com.zhaoxinms.resi.archive.entity.ResiCustomerAsset;
import com.zhaoxinms.resi.archive.entity.ResiRoom;
import com.zhaoxinms.resi.archive.service.IResiCustomerAssetService;
import com.zhaoxinms.resi.archive.service.IResiCustomerService;
import com.zhaoxinms.resi.archive.service.IResiRoomService;
import com.zhaoxinms.resi.common.ResiConstants;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeAllocation;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition;
import com.zhaoxinms.resi.feeconfig.service.IResiFeeAllocationService;
import com.zhaoxinms.resi.feeconfig.service.IResiFeeDefinitionService;
import com.zhaoxinms.resi.receivable.dto.ResiReceivableCreateTempReq;
import com.zhaoxinms.resi.receivable.dto.ResiReceivableGenerateReq;
import com.zhaoxinms.resi.receivable.dto.ResiReceivableGenerateVo;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;
import com.zhaoxinms.resi.receivable.mapper.ResiReceivableMapper;

/**
 * 应收账单生成 Service 单元测试
 * 覆盖：批量生成防重复、金额计算、临时费录入、按批次删除
 */
@ExtendWith(MockitoExtension.class)
class ResiReceivableGenServiceImplTest {

    @Mock
    private ResiReceivableMapper receivableMapper;

    @Mock
    private IResiFeeAllocationService feeAllocationService;

    @Mock
    private IResiFeeDefinitionService feeDefinitionService;

    @Mock
    private IResiRoomService roomService;

    @Mock
    private IResiCustomerAssetService customerAssetService;

    @Mock
    private IResiCustomerService customerService;

    @InjectMocks
    private ResiReceivableGenServiceImpl genService;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        TestSecurityContext.setAdminUser();
        // mock lambdaQuery 链式调用，避免 NPE
        LambdaQueryChainWrapper<ResiCustomerAsset> mockWrapper = mock(LambdaQueryChainWrapper.class);
        lenient().when(mockWrapper.eq(any(), any())).thenReturn(mockWrapper);
        lenient().when(mockWrapper.list()).thenReturn(Collections.emptyList());
        lenient().when(customerAssetService.lambdaQuery()).thenReturn(mockWrapper);
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.clear();
    }

    @Test
    @DisplayName("批量生成-正常：3条周期费分配，全部生成成功")
    void batchGenerate_normal_success() {
        // Given
        ResiReceivableGenerateReq req = new ResiReceivableGenerateReq();
        req.setProjectId(1L);
        req.setBillPeriod("2026-07");

        when(receivableMapper.selectCount(any(QueryWrapper.class))).thenReturn(0);

        List<ResiFeeAllocation> allocations = buildAllocations(3, "PERIOD");
        when(feeAllocationService.list(any(QueryWrapper.class))).thenReturn(allocations);

        for (int i = 1; i <= 3; i++) {
            ResiFeeDefinition feeDef = buildFeeDefinition("fee-" + i, ResiConstants.CALC_TYPE_AREA, "PERIOD");
            when(feeDefinitionService.getById("fee-" + i)).thenReturn(feeDef);

            ResiRoom room = buildRoom(i, new BigDecimal("100.00"));
            when(roomService.getById((long) i)).thenReturn(room);
        }

        when(receivableMapper.batchInsert(anyList())).thenReturn(3);

        // When
        ResiReceivableGenerateVo result = genService.batchGenerate(req);

        // Then
        assertEquals("GEN-1-2026-07", result.getGenBatch());
        assertEquals(3, result.getTotal());
        assertEquals(3, result.getSuccess());
        assertEquals(0, result.getSkip());
        verify(receivableMapper, times(1)).batchInsert(anyList());
    }

    @Test
    @DisplayName("批量生成-防重复：同批次已存在，返回跳过")
    void batchGenerate_duplicate_skip() {
        // Given
        ResiReceivableGenerateReq req = new ResiReceivableGenerateReq();
        req.setProjectId(1L);
        req.setBillPeriod("2026-07");

        when(receivableMapper.selectCount(any(QueryWrapper.class))).thenReturn(5);

        // When
        ResiReceivableGenerateVo result = genService.batchGenerate(req);

        // Then
        assertEquals(5, result.getTotal());
        assertEquals(0, result.getSuccess());
        assertEquals(5, result.getSkip());
        verify(receivableMapper, never()).batchInsert(anyList());
    }

    @Test
    @DisplayName("批量生成-固定金额：FIXED类型，单价200，total=200.00")
    void batchGenerate_fixedAmount_calcCorrect() {
        // Given
        ResiReceivableGenerateReq req = new ResiReceivableGenerateReq();
        req.setProjectId(1L);
        req.setBillPeriod("2026-07");

        when(receivableMapper.selectCount(any(QueryWrapper.class))).thenReturn(0);

        List<ResiFeeAllocation> allocations = buildAllocations(1, "PERIOD");
        allocations.get(0).setFeeId("fee-fixed");
        when(feeAllocationService.list(any(QueryWrapper.class))).thenReturn(allocations);

        ResiFeeDefinition feeDef = buildFeeDefinition("fee-fixed", ResiConstants.CALC_TYPE_FIXED, "PERIOD");
        feeDef.setUnitPrice(new BigDecimal("200.0000"));
        when(feeDefinitionService.getById("fee-fixed")).thenReturn(feeDef);

        ResiRoom room = buildRoom(1, new BigDecimal("100.00"));
        when(roomService.getById(1L)).thenReturn(room);

        when(receivableMapper.batchInsert(anyList())).thenAnswer(inv -> {
            List<ResiReceivable> list = inv.getArgument(0);
            assertEquals(1, list.size());
            ResiReceivable r = list.get(0);
            assertEquals(new BigDecimal("1"), r.getNum());
            assertEquals(new BigDecimal("200.0000"), r.getPrice());
            assertEquals(new BigDecimal("200.00"), r.getTotal());
            assertEquals(new BigDecimal("200.00"), r.getReceivable());
            return list.size();
        });

        // When
        genService.batchGenerate(req);

        // Then (verified in Answer)
    }

    @Test
    @DisplayName("批量生成-面积费：2.80×128.5=359.80，ROUND正确")
    void batchGenerate_areaAmount_roundCorrect() {
        // Given
        ResiReceivableGenerateReq req = new ResiReceivableGenerateReq();
        req.setProjectId(1L);
        req.setBillPeriod("2026-07");

        when(receivableMapper.selectCount(any(QueryWrapper.class))).thenReturn(0);

        List<ResiFeeAllocation> allocations = buildAllocations(1, "PERIOD");
        allocations.get(0).setFeeId("fee-area");
        when(feeAllocationService.list(any(QueryWrapper.class))).thenReturn(allocations);

        ResiFeeDefinition feeDef = buildFeeDefinition("fee-area", ResiConstants.CALC_TYPE_AREA, "PERIOD");
        feeDef.setUnitPrice(new BigDecimal("2.8000"));
        when(feeDefinitionService.getById("fee-area")).thenReturn(feeDef);

        ResiRoom room = buildRoom(1, new BigDecimal("128.50"));
        when(roomService.getById(1L)).thenReturn(room);

        when(receivableMapper.batchInsert(anyList())).thenAnswer(inv -> {
            List<ResiReceivable> list = inv.getArgument(0);
            assertEquals(1, list.size());
            ResiReceivable r = list.get(0);
            assertEquals(new BigDecimal("128.50"), r.getNum());
            assertEquals(new BigDecimal("2.8000"), r.getPrice());
            assertEquals(new BigDecimal("359.80"), r.getTotal());
            assertEquals(new BigDecimal("359.80"), r.getReceivable());
            return list.size();
        });

        // When
        genService.batchGenerate(req);
    }

    @Test
    @DisplayName("批量生成-USAGE类型跳过：抄表费不走批量生成")
    void batchGenerate_usageType_skip() {
        // Given
        ResiReceivableGenerateReq req = new ResiReceivableGenerateReq();
        req.setProjectId(1L);
        req.setBillPeriod("2026-07");

        when(receivableMapper.selectCount(any(QueryWrapper.class))).thenReturn(0);

        List<ResiFeeAllocation> allocations = buildAllocations(1, "PERIOD");
        allocations.get(0).setFeeId("fee-usage");
        when(feeAllocationService.list(any(QueryWrapper.class))).thenReturn(allocations);

        ResiFeeDefinition feeDef = buildFeeDefinition("fee-usage", ResiConstants.CALC_TYPE_USAGE, "PERIOD");
        when(feeDefinitionService.getById("fee-usage")).thenReturn(feeDef);

        ResiRoom room = buildRoom(1, new BigDecimal("100.00"));
        when(roomService.getById(1L)).thenReturn(room);

        // When
        ResiReceivableGenerateVo result = genService.batchGenerate(req);

        // Then
        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccess());
        assertEquals(1, result.getSkip());
        verify(receivableMapper, never()).batchInsert(anyList());
    }

    @Test
    @DisplayName("按批次删除：仅删除pay_state=0的记录，已收保留")
    void deleteByGenBatch_onlyUnpaid() {
        // Given
        String genBatch = "GEN-1-2026-07";
        List<ResiReceivable> list = new ArrayList<>();
        ResiReceivable r1 = new ResiReceivable();
        r1.setId("recv-001");
        r1.setPayState(ResiConstants.PAY_STATE_UNPAID);
        list.add(r1);

        when(receivableMapper.selectList(any(QueryWrapper.class))).thenReturn(list);
        when(receivableMapper.updateById(any(ResiReceivable.class))).thenReturn(1);

        // When
        int count = genService.deleteByGenBatch(genBatch);

        // Then
        assertEquals(1, count);
        verify(receivableMapper, times(1)).updateById(any(ResiReceivable.class));
    }

    @Test
    @DisplayName("临时费录入：正常录入一条临时费")
    void createTempReceivable_success() {
        // Given
        ResiReceivableCreateTempReq req = new ResiReceivableCreateTempReq();
        req.setProjectId(1L);
        req.setResourceType(ResiConstants.RESOURCE_TYPE_ROOM);
        req.setResourceId(1L);
        req.setFeeId("fee-temp");
        req.setNum(new BigDecimal("2"));
        req.setPrice(new BigDecimal("100.00"));
        req.setRemark("测试临时费");

        ResiFeeDefinition feeDef = buildFeeDefinition("fee-temp", ResiConstants.CALC_TYPE_FIXED, ResiConstants.FEE_TYPE_TEMP);
        when(feeDefinitionService.getById("fee-temp")).thenReturn(feeDef);

        ResiRoom room = buildRoom(1, new BigDecimal("100.00"));
        when(roomService.getById(1L)).thenReturn(room);

        when(receivableMapper.insert(any(ResiReceivable.class))).thenAnswer(inv -> {
            ResiReceivable r = inv.getArgument(0);
            assertEquals(ResiConstants.FEE_TYPE_TEMP, r.getFeeType());
            assertEquals(new BigDecimal("200.00"), r.getTotal());
            assertEquals(new BigDecimal("200.00"), r.getReceivable());
            assertNull(r.getBillPeriod());
            return 1;
        });

        // When
        genService.createTempReceivable(req);

        // Then (verified in Answer)
    }

    // ======= helper methods =======

    private List<ResiFeeAllocation> buildAllocations(int count, String feeType) {
        List<ResiFeeAllocation> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            ResiFeeAllocation alloc = new ResiFeeAllocation();
            alloc.setProjectId(1L);
            alloc.setFeeId("fee-" + i);
            alloc.setResourceType(ResiConstants.RESOURCE_TYPE_ROOM);
            alloc.setResourceId((long) i);
            alloc.setResourceName("1栋1单元" + (100 + i));
            alloc.setStartDate(java.sql.Date.valueOf("2026-01-01"));
            list.add(alloc);
        }
        return list;
    }

    private ResiFeeDefinition buildFeeDefinition(String id, String calcType, String feeType) {
        ResiFeeDefinition fee = new ResiFeeDefinition();
        fee.setId(id);
        fee.setProjectId(1L);
        fee.setFeeName("测试费");
        fee.setFeeCode("TEST" + id);
        fee.setFeeType(feeType);
        fee.setCalcType(calcType);
        fee.setUnitPrice(new BigDecimal("1.0000"));
        fee.setCycleUnit(ResiConstants.CYCLE_UNIT_MONTH);
        fee.setCycleValue(1);
        fee.setRoundType(ResiConstants.ROUND_TYPE_ROUND);
        fee.setEnabledMark(1);
        return fee;
    }

    private ResiRoom buildRoom(long id, BigDecimal area) {
        ResiRoom room = new ResiRoom();
        room.setId(id);
        room.setProjectId(1L);
        room.setBuildingId(1L);
        room.setRoomNo(String.valueOf(100 + (int) id));
        room.setRoomAlias("1栋1单元" + (100 + (int) id));
        room.setBuildingArea(area);
        room.setState(ResiConstants.ROOM_STATE_NORMAL);
        return room;
    }
}
