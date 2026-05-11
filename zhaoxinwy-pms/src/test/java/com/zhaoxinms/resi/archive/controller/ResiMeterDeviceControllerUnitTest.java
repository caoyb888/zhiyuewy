package com.zhaoxinms.resi.archive.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.resi.archive.entity.ResiMeterDevice;
import com.zhaoxinms.resi.archive.service.IResiMeterDeviceService;
import com.zhaoxinms.resi.TestSecurityContext;

@ExtendWith(MockitoExtension.class)
class ResiMeterDeviceControllerUnitTest {

    @Mock
    private IResiMeterDeviceService meterDeviceService;

    @InjectMocks
    private ResiMeterDeviceController controller;

    @BeforeEach
    void setUp() {
        TestSecurityContext.setNormalUser();
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.clear();
    }

    @Test
    @DisplayName("add: public meter with null roomId should succeed")
    void add_publicMeter_nullRoomId_success() {
        ResiMeterDevice device = new ResiMeterDevice();
        device.setProjectId(1L);
        device.setMeterCode("WM-001");
        device.setMeterType(1);
        device.setIsPublic(1);
        device.setRoomId(null);
        device.setInitReading(new BigDecimal("0.00"));
        device.setMultiplier(new BigDecimal("1.00"));

        when(meterDeviceService.save(any(ResiMeterDevice.class))).thenReturn(true);

        AjaxResult result = controller.add(device);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("add: private meter with roomId should succeed")
    void add_privateMeter_withRoomId_success() {
        ResiMeterDevice device = new ResiMeterDevice();
        device.setProjectId(1L);
        device.setMeterCode("WM-101");
        device.setMeterType(1);
        device.setIsPublic(0);
        device.setRoomId(101L);
        device.setInitReading(new BigDecimal("0.00"));

        when(meterDeviceService.save(any(ResiMeterDevice.class))).thenReturn(true);

        AjaxResult result = controller.add(device);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("edit: valid update should succeed")
    void edit_valid_success() {
        Long id = 1L;
        ResiMeterDevice device = new ResiMeterDevice();
        device.setProjectId(1L);
        device.setMeterCode("WM-002");
        device.setMeterType(2);

        when(meterDeviceService.updateById(any(ResiMeterDevice.class))).thenReturn(true);

        AjaxResult result = controller.edit(id, device);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("remove: valid delete should succeed")
    void remove_valid_success() {
        Long[] ids = {1L, 2L};
        when(meterDeviceService.removeByIds(anyList())).thenReturn(true);

        AjaxResult result = controller.remove(ids);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }
}
