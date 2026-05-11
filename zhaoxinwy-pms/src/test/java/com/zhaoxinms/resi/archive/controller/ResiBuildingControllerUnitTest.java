package com.zhaoxinms.resi.archive.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.resi.archive.entity.ResiBuilding;
import com.zhaoxinms.resi.archive.service.IResiBuildingService;
import com.zhaoxinms.resi.TestSecurityContext;

@ExtendWith(MockitoExtension.class)
class ResiBuildingControllerUnitTest {

    @Mock
    private IResiBuildingService buildingService;

    @InjectMocks
    private ResiBuildingController controller;

    @BeforeEach
    void setUp() {
        TestSecurityContext.setNormalUser();
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.clear();
    }

    @Test
    @DisplayName("add: valid building should succeed")
    void add_valid_success() {
        ResiBuilding building = new ResiBuilding();
        building.setProjectId(1L);
        building.setName("1号楼");
        building.setNumber("B-01");

        when(buildingService.save(any(ResiBuilding.class))).thenReturn(true);

        AjaxResult result = controller.add(building);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("edit: valid update should succeed")
    void edit_valid_success() {
        Long id = 1L;
        ResiBuilding building = new ResiBuilding();
        building.setProjectId(1L);
        building.setName("2号楼");
        building.setNumber("B-02");

        when(buildingService.updateById(any(ResiBuilding.class))).thenReturn(true);

        AjaxResult result = controller.edit(id, building);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("remove: valid delete should succeed")
    void remove_valid_success() {
        Long[] ids = {1L, 2L};
        when(buildingService.removeByIds(anyList())).thenReturn(true);

        AjaxResult result = controller.remove(ids);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }
}
