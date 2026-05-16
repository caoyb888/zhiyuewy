package com.zhaoxinms.resi.archive.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.resi.archive.dto.ResiRoomTransferQuery;
import com.zhaoxinms.resi.archive.dto.ResiRoomTransferReq;
import com.zhaoxinms.resi.archive.dto.ResiRoomTransferVo;
import com.zhaoxinms.resi.archive.entity.ResiCustomer;
import com.zhaoxinms.resi.archive.entity.ResiCustomerAsset;
import com.zhaoxinms.resi.archive.entity.ResiRoom;
import com.zhaoxinms.resi.archive.entity.ResiRoomTransfer;
import com.zhaoxinms.resi.archive.mapper.ResiRoomTransferMapper;
import com.zhaoxinms.resi.archive.service.IResiCustomerAssetService;
import com.zhaoxinms.resi.archive.service.IResiCustomerService;
import com.zhaoxinms.resi.archive.service.IResiRoomService;
import com.zhaoxinms.resi.archive.service.IResiRoomTransferService;

/**
 * 房屋过户记录 Service实现
 *
 * @author zhaoxinms
 */
@Service
public class ResiRoomTransferServiceImpl implements IResiRoomTransferService {

    @Autowired
    private ResiRoomTransferMapper transferMapper;

    @Autowired
    private IResiRoomService roomService;

    @Autowired
    private IResiCustomerAssetService customerAssetService;

    @Autowired
    private IResiCustomerService customerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResiRoomTransfer transfer(ResiRoomTransferReq req) {
        Long roomId = req.getRoomId();
        Long newCustomerId = req.getNewCustomerId();
        Date transferDate = req.getTransferDate();

        // 1. 校验房间是否存在
        ResiRoom room = roomService.getById(roomId);
        if (room == null) {
            throw new ServiceException("房间不存在");
        }

        // 2. 校验新业主是否存在
        ResiCustomer newCustomer = customerService.getById(newCustomerId);
        if (newCustomer == null) {
            throw new ServiceException("新业主不存在");
        }

        // 3. 查询当前绑定关系（旧业主）
        ResiCustomerAsset currentBinding = customerAssetService.selectCurrentBinding(1, roomId);
        Long oldCustomerId = null;

        if (currentBinding != null) {
            oldCustomerId = currentBinding.getCustomerId();

            // 校验新旧业主不能是同一人
            if (oldCustomerId.equals(newCustomerId)) {
                throw new ServiceException("新业主不能与当前业主为同一人");
            }

            // 3.1 终止旧业主绑定
            currentBinding.setIsCurrent(0);
            currentBinding.setUnbindDate(transferDate);
            customerAssetService.updateById(currentBinding);
        }

        // 4. 新增新业主绑定
        ResiCustomerAsset newBinding = new ResiCustomerAsset();
        newBinding.setCustomerId(newCustomerId);
        newBinding.setProjectId(room.getProjectId());
        newBinding.setAssetType(1); // 1=房间
        newBinding.setAssetId(roomId);
        newBinding.setBindDate(transferDate);
        newBinding.setIsCurrent(1);
        newBinding.setCreateBy(SecurityUtils.getUsername());
        customerAssetService.save(newBinding);

        // 5. 记录过户日志
        ResiRoomTransfer transfer = new ResiRoomTransfer();
        transfer.setProjectId(room.getProjectId());
        transfer.setRoomId(roomId);
        transfer.setOldCustomerId(oldCustomerId != null ? oldCustomerId : 0L);
        transfer.setNewCustomerId(newCustomerId);
        transfer.setTransferDate(transferDate);
        transfer.setTransferRemark(req.getTransferRemark());
        transfer.setOperator(SecurityUtils.getUsername());
        transfer.setCreateTime(new Date());
        transferMapper.insert(transfer);

        // 6. 更新房间状态为已过户
        room.setState("TRANSFERRED");
        room.setUpdateBy(SecurityUtils.getUsername());
        roomService.updateById(room);

        return transfer;
    }

    @Override
    public List<ResiRoomTransferVo> selectTransferList(ResiRoomTransferQuery query) {
        Long projectId = query.getProjectId();
        List<Long> projectIds = query.getProjectIds();
        Long roomId = query.getRoomId();
        String startDate = query.getStartDate();
        String endDate = query.getEndDate();
        
        List<ResiRoomTransfer> list = transferMapper.selectTransferList(projectId, projectIds, roomId, startDate, endDate);
        return convertToVoList(list);
    }

    @Override
    public List<ResiRoomTransferVo> selectTransferByRoomId(Long roomId) {
        List<ResiRoomTransfer> list = transferMapper.selectTransferByRoomId(roomId);
        return convertToVoList(list);
    }

    private List<ResiRoomTransferVo> convertToVoList(List<ResiRoomTransfer> list) {
        List<ResiRoomTransferVo> voList = new ArrayList<>();
        for (ResiRoomTransfer transfer : list) {
            voList.add(convertToVo(transfer));
        }
        return voList;
    }

    private ResiRoomTransferVo convertToVo(ResiRoomTransfer transfer) {
        ResiRoomTransferVo vo = new ResiRoomTransferVo();
        vo.setId(transfer.getId());
        vo.setProjectId(transfer.getProjectId());
        vo.setRoomId(transfer.getRoomId());
        vo.setOldCustomerId(transfer.getOldCustomerId());
        vo.setNewCustomerId(transfer.getNewCustomerId());
        vo.setTransferDate(transfer.getTransferDate());
        vo.setTransferRemark(transfer.getTransferRemark());
        vo.setOperator(transfer.getOperator());
        vo.setCreateTime(transfer.getCreateTime());
        vo.setRoomName(transfer.getRoomName());
        vo.setOldCustomerName(transfer.getOldCustomerName());
        vo.setNewCustomerName(transfer.getNewCustomerName());
        vo.setProjectName(transfer.getProjectName());
        return vo;
    }
}
