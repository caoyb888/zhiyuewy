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
import com.zhaoxinms.resi.archive.entity.ResiRoom;
import com.zhaoxinms.resi.archive.service.IResiRoomService;
import com.zhaoxinms.resi.TestSecurityContext;

/**
 * Room Controller Unit Test
 * Covers: unique constraint, auto roomAlias generation
 */
@ExtendWith(MockitoExtension.class)
class ResiRoomControllerUnitTest {

    @Mock
    private IResiRoomService roomService;

    @InjectMocks
    private ResiRoomController controller;

    @BeforeEach
    void setUp() {
        TestSecurityContext.setNormalUser();
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.clear();
    }

    @Test
    @DisplayName("add: unique room should succeed")
    void add_uniqueRoom_success() {
        ResiRoom room = new ResiRoom();
        room.setProjectId(1L);
        room.setBuildingId(1L);
        room.setUnitNo("1");
        room.setRoomNo("101");
        room.setState("NORMAL");

        when(roomService.checkRoomUnique(1L, 1L, "1", "101")).thenReturn(true);
        when(roomService.generateRoomAlias(1L, "1", "101")).thenReturn("1号楼1单元101");
        when(roomService.save(any(ResiRoom.class))).thenReturn(true);

        AjaxResult result = controller.add(room);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("add: duplicate room should return error")
    void add_duplicateRoom_error() {
        ResiRoom room = new ResiRoom();
        room.setProjectId(1L);
        room.setBuildingId(1L);
        room.setUnitNo("1");
        room.setRoomNo("101");
        room.setState("NORMAL");

        when(roomService.checkRoomUnique(1L, 1L, "1", "101")).thenReturn(false);

        AjaxResult result = controller.add(room);
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
        String msg = (String) result.get(AjaxResult.MSG_TAG);
        assertTrue(msg.contains("已存在"));
    }

    @Test
    @DisplayName("add: auto generate roomAlias when blank")
    void add_blankAlias_autoGenerate() {
        ResiRoom room = new ResiRoom();
        room.setProjectId(1L);
        room.setBuildingId(1L);
        room.setUnitNo("2");
        room.setRoomNo("202");
        room.setRoomAlias(null);
        room.setState("NORMAL");

        when(roomService.checkRoomUnique(1L, 1L, "2", "202")).thenReturn(true);
        when(roomService.generateRoomAlias(1L, "2", "202")).thenReturn("1号楼2单元202");
        when(roomService.save(any(ResiRoom.class))).thenAnswer(inv -> {
            ResiRoom saved = inv.getArgument(0);
            assertEquals("1号楼2单元202", saved.getRoomAlias());
            return true;
        });

        AjaxResult result = controller.add(room);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("add: keep custom roomAlias when provided")
    void add_customAlias_keep() {
        ResiRoom room = new ResiRoom();
        room.setProjectId(1L);
        room.setBuildingId(1L);
        room.setUnitNo("1");
        room.setRoomNo("101");
        room.setRoomAlias("Custom Alias");
        room.setState("NORMAL");

        when(roomService.checkRoomUnique(1L, 1L, "1", "101")).thenReturn(true);
        when(roomService.save(any(ResiRoom.class))).thenAnswer(inv -> {
            ResiRoom saved = inv.getArgument(0);
            assertEquals("Custom Alias", saved.getRoomAlias());
            return true;
        });

        AjaxResult result = controller.add(room);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("edit: unique check when key fields changed")
    void edit_keyFieldsChanged_checkUnique() {
        Long id = 1L;
        ResiRoom old = new ResiRoom();
        old.setId(id);
        old.setProjectId(1L);
        old.setBuildingId(1L);
        old.setUnitNo("1");
        old.setRoomNo("101");

        ResiRoom update = new ResiRoom();
        update.setProjectId(1L);
        update.setBuildingId(2L); // changed
        update.setUnitNo("1");
        update.setRoomNo("101");
        update.setState("NORMAL");

        when(roomService.getById(id)).thenReturn(old);
        when(roomService.checkRoomUnique(1L, 2L, "1", "101", id)).thenReturn(true);
        when(roomService.updateById(any(ResiRoom.class))).thenReturn(true);

        AjaxResult result = controller.edit(id, update);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("edit: no unique check when key fields unchanged")
    void edit_keyFieldsUnchanged_noUniqueCheck() {
        Long id = 1L;
        ResiRoom old = new ResiRoom();
        old.setId(id);
        old.setProjectId(1L);
        old.setBuildingId(1L);
        old.setUnitNo("1");
        old.setRoomNo("101");

        ResiRoom update = new ResiRoom();
        update.setProjectId(1L);
        update.setBuildingId(1L);
        update.setUnitNo("1");
        update.setRoomNo("101");
        update.setState("RENTED"); // only state changed

        when(roomService.getById(id)).thenReturn(old);
        when(roomService.updateById(any(ResiRoom.class))).thenReturn(true);

        AjaxResult result = controller.edit(id, update);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
        verify(roomService, never()).checkRoomUnique(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("edit: room not found should return error")
    void edit_roomNotFound_error() {
        Long id = 999L;
        ResiRoom update = new ResiRoom();
        update.setState("NORMAL");

        when(roomService.getById(id)).thenReturn(null);

        AjaxResult result = controller.edit(id, update);
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
    }
}
