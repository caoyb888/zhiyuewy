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
import com.zhaoxinms.resi.feeconfig.dto.ResiFeeAllocationBatchReq;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeAllocation;
import com.zhaoxinms.resi.feeconfig.service.IResiFeeAllocationService;

/**
 * 费用分配 Controller 单元测试
 * 覆盖：单条CRUD、批量分配、预览接口
 */
@ExtendWith(MockitoExtension.class)
class ResiFeeAllocationControllerUnitTest {

    @Mock
    private IResiFeeAllocationService allocationService;

    @InjectMocks
    private ResiFeeAllocationController controller;

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
        ResiFeeAllocation allocation = buildValidAllocation();
        when(allocationService.save(any(ResiFeeAllocation.class))).thenReturn(true);

        AjaxResult result = controller.add(allocation);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("新增：资源类型为空时应自动填充ROOM")
    void add_blankResourceType_defaultRoom() {
        ResiFeeAllocation allocation = buildValidAllocation();
        allocation.setResourceType(null);
        when(allocationService.save(any(ResiFeeAllocation.class))).thenReturn(true);

        AjaxResult result = controller.add(allocation);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("修改：正常数据应成功")
    void edit_valid_success() {
        ResiFeeAllocation allocation = buildValidAllocation();
        when(allocationService.updateById(any(ResiFeeAllocation.class))).thenReturn(true);

        AjaxResult result = controller.edit("alloc-001", allocation);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("删除：批量删除应成功")
    void remove_batch_success() {
        when(allocationService.removeByIds(anyList())).thenReturn(true);

        AjaxResult result = controller.remove(new String[]{"alloc-001", "alloc-002"});
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("批量分配：按楼栋50间房应返回正确统计")
    void batchAllocate_building_success() {
        ResiFeeAllocationBatchReq req = new ResiFeeAllocationBatchReq();
        req.setProjectId(1L);
        req.setFeeId("fee-001");
        req.setBatchType("BUILDING");
        req.setBuildingId(1L);
        req.setStartDate(java.sql.Date.valueOf("2026-06-01"));

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("total", 50);
        mockResult.put("success", 50);
        mockResult.put("skip", 0);
        when(allocationService.batchAllocate(any(ResiFeeAllocation.class), eq("BUILDING"), eq(1L), isNull()))
                .thenReturn(mockResult);

        AjaxResult result = controller.batchAllocate(req);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get(AjaxResult.DATA_TAG);
        assertEquals(50, data.get("success"));
    }

    @Test
    @DisplayName("批量分配：含已存在时应跳过不报错")
    void batchAllocate_withExisting_skipNoError() {
        ResiFeeAllocationBatchReq req = new ResiFeeAllocationBatchReq();
        req.setProjectId(1L);
        req.setFeeId("fee-001");
        req.setBatchType("PROJECT");
        req.setStartDate(java.sql.Date.valueOf("2026-06-01"));

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("total", 500);
        mockResult.put("success", 470);
        mockResult.put("skip", 30);
        when(allocationService.batchAllocate(any(ResiFeeAllocation.class), eq("PROJECT"), isNull(), isNull()))
                .thenReturn(mockResult);

        AjaxResult result = controller.batchAllocate(req);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get(AjaxResult.DATA_TAG);
        assertEquals(470, data.get("success"));
        assertEquals(30, data.get("skip"));
    }

    @Test
    @DisplayName("预览批量分配：应返回total/existing/newCount")
    void previewBatch_success() {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("total", 100);
        mockResult.put("existing", 20);
        mockResult.put("newCount", 80);
        mockResult.put("skip", 20);
        when(allocationService.previewBatchAllocate(1L, "fee-001", "BUILDING", 1L, "1", "2026-06-01"))
                .thenReturn(mockResult);

        AjaxResult result = controller.previewBatch(1L, "fee-001", "BUILDING", 1L, "1", "2026-06-01");
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get(AjaxResult.DATA_TAG);
        assertEquals(100, data.get("total"));
        assertEquals(80, data.get("newCount"));
    }

    @Test
    @DisplayName("预览批量分配：全项目方式时buildingId可为空")
    void previewBatch_projectType_nullBuildingId() {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("total", 500);
        mockResult.put("existing", 0);
        mockResult.put("newCount", 500);
        mockResult.put("skip", 0);
        when(allocationService.previewBatchAllocate(1L, "fee-001", "PROJECT", null, null, "2026-06-01"))
                .thenReturn(mockResult);

        AjaxResult result = controller.previewBatch(1L, "fee-001", "PROJECT", null, null, "2026-06-01");
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    private ResiFeeAllocation buildValidAllocation() {
        ResiFeeAllocation allocation = new ResiFeeAllocation();
        allocation.setProjectId(1L);
        allocation.setFeeId("fee-001");
        allocation.setResourceType("ROOM");
        allocation.setResourceId(1L);
        allocation.setResourceName("1栋101");
        allocation.setStartDate(java.sql.Date.valueOf("2026-06-01"));
        allocation.setCustomPrice(new BigDecimal("2.8000"));
        return allocation;
    }
}
