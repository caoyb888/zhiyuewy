package com.zhaoxinms.resi.archive.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zhaoxinms.common.annotation.Log;
import com.zhaoxinms.common.core.controller.BaseController;
import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.resi.archive.dto.ResiRoomTransferQuery;
import com.zhaoxinms.resi.archive.dto.ResiRoomTransferReq;
import com.zhaoxinms.resi.archive.dto.ResiRoomTransferVo;
import com.zhaoxinms.resi.archive.entity.ResiRoomTransfer;
import com.zhaoxinms.resi.archive.service.IResiRoomTransferService;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 房屋过户记录 Controller
 *
 * @author zhaoxinms
 */
@Api(tags = "住宅收费-房屋过户")
@RestController
@RequestMapping("/resi/archive/room")
public class ResiRoomTransferController extends BaseController {

    @Autowired
    private IResiRoomTransferService transferService;

    /**
     * 房屋过户
     */
    @ApiOperation("房屋过户")
    @PreAuthorize("@ss.hasPermi('resi:room:transfer')")
    @Log(title = "住宅收费-房屋过户", businessType = BusinessType.UPDATE)
    @PostMapping("/transfer")
    public AjaxResult transfer(@RequestBody @Validated ResiRoomTransferReq req) {
        ResiRoomTransfer transfer = transferService.transfer(req);
        return AjaxResult.success(transfer);
    }

    /**
     * 查询过户记录列表（过户查询报表）
     */
    @ApiOperation("过户查询报表")
    @PreAuthorize("@ss.hasPermi('resi:room:transferQuery')")
    @ResiProjectScope
    @GetMapping("/transfer/query")
    public AjaxResult transferQuery(ResiRoomTransferQuery query) {
        List<ResiRoomTransferVo> list = transferService.selectTransferList(query);
        return AjaxResult.success(list);
    }

    /**
     * 查询房间的过户历史
     */
    @ApiOperation("房间过户历史")
    @PreAuthorize("@ss.hasPermi('resi:room:transferQuery')")
    @GetMapping("/transfer/history/{roomId}")
    public AjaxResult transferHistory(@PathVariable("roomId") Long roomId) {
        List<ResiRoomTransferVo> list = transferService.selectTransferByRoomId(roomId);
        return AjaxResult.success(list);
    }
}
