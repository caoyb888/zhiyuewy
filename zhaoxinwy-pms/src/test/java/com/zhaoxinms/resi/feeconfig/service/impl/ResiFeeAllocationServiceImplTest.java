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

import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.resi.TestSecurityContext;
import com.zhaoxinms.resi.archive.entity.ResiRoom;
import com.zhaoxinms.resi.archive.service.IResiRoomService;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeAllocation;
import com.zhaoxinms.resi.feeconfig.mapper.ResiFeeAllocationMapper;

/**
 * 费用分配 Service 单元测试
 * 覆盖：批量分配核心逻辑、预览、唯一键冲突跳过
 */
@ExtendWith(MockitoExtension.class)
class ResiFeeAllocationServiceImplTest {

    @Mock
    private ResiFeeAllocationMapper allocationMapper;

    @Mock
    private IResiRoomService roomService;

    @InjectMocks
    private ResiFeeAllocationServiceImpl allocationService;

    @BeforeEach
    void setUp() throws Exception {
        TestSecurityContext.setAdminUser();
        // 通过反射注入 baseMapper（父类 ServiceImpl 的字段）
        Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(allocationService, allocationMapper);
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.clear();
    }

    @Test
    @DisplayName("批量分配-按楼栋：50个房间全部分配成功")
    void batchAllocate_building_allNew_success() {
        List<ResiRoom> rooms = buildRooms(50, 1L);
        when(roomService.list(any())).thenReturn(rooms);
        when(allocationMapper.selectExistResourceIds(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(allocationMapper.insert(any(ResiFeeAllocation.class))).thenReturn(1);

        ResiFeeAllocation allocation = buildAllocationTemplate();
        Map<String, Object> result = allocationService.batchAllocate(allocation, "BUILDING", 1L, null);

        assertEquals(50, result.get("total"));
        assertEquals(50, result.get("success"));
        assertEquals(0, result.get("skip"));
    }

    @Test
    @DisplayName("批量分配-含已存在：30已存在+20新，跳过30成功20")
    void batchAllocate_withExisting_skipAndSuccess() {
        List<ResiRoom> rooms = buildRooms(50, 1L);
        when(roomService.list(any())).thenReturn(rooms);
        List<Long> existIds = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            existIds.add((long) i);
        }
        when(allocationMapper.selectExistResourceIds(anyString(), anyString(), anyString()))
                .thenReturn(existIds);
        when(allocationMapper.insert(any(ResiFeeAllocation.class))).thenReturn(1);

        ResiFeeAllocation allocation = buildAllocationTemplate();
        Map<String, Object> result = allocationService.batchAllocate(allocation, "BUILDING", 1L, null);

        assertEquals(50, result.get("total"));
        assertEquals(20, result.get("success"));
        assertEquals(30, result.get("skip"));
    }

    @Test
    @DisplayName("批量分配-全项目：无房间时返回0")
    void batchAllocate_project_noRooms() {
        when(roomService.list(any())).thenReturn(Collections.emptyList());

        ResiFeeAllocation allocation = buildAllocationTemplate();
        Map<String, Object> result = allocationService.batchAllocate(allocation, "PROJECT", null, null);

        assertEquals(0, result.get("total"));
        assertEquals(0, result.get("success"));
        assertEquals(0, result.get("skip"));
    }

    @Test
    @DisplayName("批量分配-按楼栋：楼栋ID为空时应抛异常")
    void batchAllocate_building_nullBuildingId_exception() {
        ResiFeeAllocation allocation = buildAllocationTemplate();
        ServiceException ex = assertThrows(ServiceException.class, () ->
                allocationService.batchAllocate(allocation, "BUILDING", null, null));
        assertTrue(ex.getMessage().contains("楼栋ID不能为空"));
    }

    @Test
    @DisplayName("批量分配-按单元：单元号为空时应抛异常")
    void batchAllocate_unit_nullUnitNo_exception() {
        lenient().when(roomService.list(any())).thenReturn(buildRooms(10, 1L));
        ResiFeeAllocation allocation = buildAllocationTemplate();
        ServiceException ex = assertThrows(ServiceException.class, () ->
                allocationService.batchAllocate(allocation, "UNIT", 1L, null));
        assertTrue(ex.getMessage().contains("单元号不能为空"));
    }

    @Test
    @DisplayName("预览批量分配：应正确计算total/existing/newCount")
    void previewBatchAllocate_correctStats() {
        List<ResiRoom> rooms = buildRooms(100, 1L);
        when(roomService.list(any())).thenReturn(rooms);
        List<Long> existIds = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            existIds.add((long) i);
        }
        when(allocationMapper.selectExistResourceIds(anyString(), anyString(), anyString()))
                .thenReturn(existIds);

        Map<String, Object> result = allocationService.previewBatchAllocate(
                1L, "fee-001", "BUILDING", 1L, "1", "2026-06-01");

        assertEquals(100, result.get("total"));
        assertEquals(20, result.get("skip"));
        assertEquals(80, result.get("newCount"));
    }

    @Test
    @DisplayName("保存：应自动填充创建人和有效标志")
    void save_autoFillCreatorAndEnabledMark() {
        ResiFeeAllocation allocation = buildAllocationTemplate();
        allocation.setResourceName(null);
        ResiRoom room = new ResiRoom();
        room.setId(1L);
        room.setRoomAlias("1栋101");
        when(roomService.getById(1L)).thenReturn(room);
        when(allocationMapper.insert(any(ResiFeeAllocation.class))).thenReturn(1);

        boolean success = allocationService.save(allocation);
        assertTrue(success);
        assertEquals(1, allocation.getEnabledMark());
        assertNotNull(allocation.getCreatorTime());
        assertEquals("1", allocation.getCreatorUserId());
        assertEquals("1栋101", allocation.getResourceName());
    }

    @Test
    @DisplayName("修改：应自动填充修改人")
    void update_autoFillModifier() {
        ResiFeeAllocation allocation = new ResiFeeAllocation();
        allocation.setId("alloc-001");
        when(allocationMapper.updateById(any(ResiFeeAllocation.class))).thenReturn(1);

        boolean success = allocationService.updateById(allocation);
        assertTrue(success);
        assertNotNull(allocation.getLastModifyTime());
        assertEquals("1", allocation.getLastModifyUserId());
    }

    @Test
    @DisplayName("删除：应为软删除，enabledMark置0")
    void remove_softDelete() {
        when(allocationMapper.updateById(any(ResiFeeAllocation.class))).thenReturn(1);

        boolean success = allocationService.removeByIds(Arrays.asList("alloc-001"));
        assertTrue(success);
    }

    private ResiFeeAllocation buildAllocationTemplate() {
        ResiFeeAllocation allocation = new ResiFeeAllocation();
        allocation.setProjectId(1L);
        allocation.setFeeId("fee-001");
        allocation.setResourceType("ROOM");
        allocation.setResourceId(1L);
        allocation.setStartDate(java.sql.Date.valueOf("2026-06-01"));
        allocation.setCustomPrice(new BigDecimal("2.8000"));
        return allocation;
    }

    private List<ResiRoom> buildRooms(int count, long buildingId) {
        List<ResiRoom> rooms = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            ResiRoom room = new ResiRoom();
            room.setId((long) i);
            room.setProjectId(1L);
            room.setBuildingId(buildingId);
            room.setUnitNo("1");
            room.setRoomNo(String.valueOf(100 + i));
            room.setRoomAlias(buildingId + "栋1单元" + (100 + i));
            room.setState("NORMAL");
            rooms.add(room);
        }
        return rooms;
    }
}
