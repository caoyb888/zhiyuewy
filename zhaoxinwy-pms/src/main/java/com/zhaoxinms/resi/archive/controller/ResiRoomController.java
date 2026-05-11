package com.zhaoxinms.resi.archive.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zhaoxinms.common.annotation.Log;
import com.zhaoxinms.common.core.controller.BaseController;
import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.common.core.page.TableDataInfo;
import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.archive.entity.ResiRoom;
import com.zhaoxinms.resi.archive.service.IResiRoomService;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 房间档案 Controller
 *
 * @author zhaoxinms
 */
@Api(tags = "住宅收费-房间管理")
@RestController
@RequestMapping("/resi/archive/room")
public class ResiRoomController extends BaseController {

    @Autowired
    private IResiRoomService roomService;

    /**
     * 查询房间列表
     */
    @ApiOperation("查询房间列表")
    @PreAuthorize("@ss.hasPermi('resi:room:list')")
    @ResiProjectScope
    @GetMapping
    public TableDataInfo list(ResiRoom room) {
        startPage();
        List<ResiRoom> list = roomService.selectResiRoomList(room);
        return getDataTable(list);
    }

    /**
     * 获取房间详细信息
     */
    @ApiOperation("获取房间详情")
    @PreAuthorize("@ss.hasPermi('resi:room:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(roomService.getById(id));
    }

    /**
     * 全文检索房间（供收银台使用）
     */
    @ApiOperation("全文检索房间")
    @PreAuthorize("@ss.hasPermi('resi:room:list')")
    @GetMapping("/search")
    public AjaxResult search(@RequestParam String keyword,
                             @RequestParam(required = false) Long projectId) {
        List<ResiRoom> list = roomService.searchRoom(keyword, projectId);
        return AjaxResult.success(list);
    }

    /**
     * 新增房间
     */
    @ApiOperation("新增房间")
    @PreAuthorize("@ss.hasPermi('resi:room:add')")
    @Log(title = "住宅收费-房间管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody @Validated(AddGroup.class) ResiRoom room) {
        if (!roomService.checkRoomUnique(room.getProjectId(), room.getBuildingId(),
                room.getUnitNo(), room.getRoomNo())) {
            return AjaxResult.error("新增房间失败，同一单元下房号 '" + room.getRoomNo() + "' 已存在");
        }
        // 自动生成 room_alias
        if (StringUtils.isBlank(room.getRoomAlias())) {
            String alias = roomService.generateRoomAlias(
                    room.getBuildingId(), room.getUnitNo(), room.getRoomNo());
            room.setRoomAlias(alias);
        }
        room.setCreateBy(SecurityUtils.getUsername());
        return toAjax(roomService.save(room));
    }

    /**
     * 修改房间
     */
    @ApiOperation("修改房间")
    @PreAuthorize("@ss.hasPermi('resi:room:edit')")
    @Log(title = "住宅收费-房间管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") Long id,
                           @RequestBody @Validated(EditGroup.class) ResiRoom room) {
        room.setId(id);
        ResiRoom oldRoom = roomService.getById(id);
        if (oldRoom == null) {
            return AjaxResult.error("房间不存在");
        }
        // 若关键字段变更，需重新校验唯一性
        if (!oldRoom.getBuildingId().equals(room.getBuildingId())
                || !java.util.Objects.equals(oldRoom.getUnitNo(), room.getUnitNo())
                || !oldRoom.getRoomNo().equals(room.getRoomNo())) {
            if (!roomService.checkRoomUnique(room.getProjectId(), room.getBuildingId(),
                    room.getUnitNo(), room.getRoomNo(), id)) {
                return AjaxResult.error("修改房间失败，同一单元下房号 '" + room.getRoomNo() + "' 已存在");
            }
        }
        // 若 room_alias 为空则重新生成
        if (StringUtils.isBlank(room.getRoomAlias())) {
            String alias = roomService.generateRoomAlias(
                    room.getBuildingId(), room.getUnitNo(), room.getRoomNo());
            room.setRoomAlias(alias);
        }
        room.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(roomService.updateById(room));
    }

    /**
     * 删除房间
     */
    @ApiOperation("删除房间")
    @PreAuthorize("@ss.hasPermi('resi:room:remove')")
    @Log(title = "住宅收费-房间管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(roomService.removeByIds(java.util.Arrays.asList(ids)));
    }
}
