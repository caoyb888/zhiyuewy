package com.zhaoxinms.resi.cashier.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoxinms.resi.archive.entity.ResiCustomer;
import com.zhaoxinms.resi.archive.entity.ResiCustomerAsset;
import com.zhaoxinms.resi.archive.entity.ResiRoom;
import com.zhaoxinms.resi.archive.service.IResiCustomerAssetService;
import com.zhaoxinms.resi.archive.service.IResiCustomerService;
import com.zhaoxinms.resi.archive.service.IResiRoomService;
import com.zhaoxinms.resi.cashier.dto.ResiCashierRoomSearchVo;
import com.zhaoxinms.resi.cashier.dto.ResiCashierRoomSummaryVo;
import com.zhaoxinms.resi.cashier.service.IResiCashierService;
import com.zhaoxinms.resi.common.ResiConstants;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;
import com.zhaoxinms.resi.receivable.mapper.ResiReceivableMapper;

/**
 * 收银台 Service实现
 */
@Service
public class ResiCashierServiceImpl implements IResiCashierService {

    private static final Logger log = LoggerFactory.getLogger(ResiCashierServiceImpl.class);

    @Autowired
    private IResiRoomService roomService;

    @Autowired
    private IResiCustomerService customerService;

    @Autowired
    private IResiCustomerAssetService customerAssetService;

    @Autowired
    private ResiReceivableMapper receivableMapper;

    @Override
    public List<ResiCashierRoomSearchVo> searchRoom(String keyword, Long projectId) {
        List<ResiCashierRoomSearchVo> result = new ArrayList<>();
        Map<Long, ResiCashierRoomSearchVo> roomMap = new LinkedHashMap<>();

        // 1. 按 room_alias / room_no 搜索房间
        List<ResiRoom> roomsByKeyword = roomService.searchRoom(keyword, projectId);
        for (ResiRoom room : roomsByKeyword) {
            if (!roomMap.containsKey(room.getId())) {
                roomMap.put(room.getId(), convertToVo(room));
            }
        }

        // 2. 按 customer_name 搜索客户，找到其绑定的房间
        if (keyword != null && !keyword.trim().isEmpty()) {
            ResiCustomer customerQuery = new ResiCustomer();
            customerQuery.setCustomerName(keyword.trim());
            List<ResiCustomer> customers = customerService.selectResiCustomerList(customerQuery);
            for (ResiCustomer customer : customers) {
                // 查询该客户当前绑定的房间
                List<ResiCustomerAsset> assets = customerAssetService.lambdaQuery()
                        .eq(ResiCustomerAsset::getCustomerId, customer.getId())
                        .eq(ResiCustomerAsset::getAssetType, ResiConstants.ASSET_TYPE_ROOM)
                        .eq(ResiCustomerAsset::getIsCurrent, 1)
                        .list();
                for (ResiCustomerAsset asset : assets) {
                    if (projectId != null && !projectId.equals(asset.getProjectId())) {
                        continue;
                    }
                    Long roomId = asset.getAssetId();
                    if (!roomMap.containsKey(roomId)) {
                        ResiRoom room = roomService.getById(roomId);
                        if (room != null && Integer.valueOf(1).equals(room.getEnabledMark())) {
                            ResiCashierRoomSearchVo vo = convertToVo(room);
                            vo.setCustomerId(customer.getId());
                            vo.setCustomerName(customer.getCustomerName());
                            roomMap.put(roomId, vo);
                        }
                    } else {
                        // 已存在，补充客户信息
                        ResiCashierRoomSearchVo vo = roomMap.get(roomId);
                        if (vo.getCustomerId() == null) {
                            vo.setCustomerId(customer.getId());
                            vo.setCustomerName(customer.getCustomerName());
                        }
                    }
                }
            }
        }

        result.addAll(roomMap.values());
        log.info("收银台搜索房间 keyword={} projectId={} resultSize={}", keyword, projectId, result.size());
        return result;
    }

    @Override
    public List<ResiReceivable> getRoomReceivables(Long roomId, Map<String, String> params) {
        QueryWrapper<ResiReceivable> qw = new QueryWrapper<>();
        qw.eq("resource_type", ResiConstants.RESOURCE_TYPE_ROOM);
        qw.eq("resource_id", roomId);
        qw.in("pay_state", ResiConstants.PAY_STATE_UNPAID, ResiConstants.PAY_STATE_PART_PAID);
        qw.isNull("delete_time");

        if (params != null) {
            String feeId = params.get("feeId");
            if (feeId != null && !feeId.isEmpty()) {
                qw.eq("fee_id", feeId);
            }
            String year = params.get("year");
            if (year != null && !year.isEmpty()) {
                qw.likeRight("bill_period", year);
            }
            String period = params.get("period");
            if (period != null && !period.isEmpty()) {
                qw.eq("bill_period", period);
            }
            String feeType = params.get("feeType");
            if (feeType != null && !feeType.isEmpty()) {
                qw.eq("fee_type", feeType);
            }
        }

        qw.orderByAsc("bill_period");
        return receivableMapper.selectList(qw);
    }

    @Override
    public ResiCashierRoomSummaryVo getRoomSummary(Long roomId) {
        QueryWrapper<ResiReceivable> qw = new QueryWrapper<>();
        qw.eq("resource_type", ResiConstants.RESOURCE_TYPE_ROOM);
        qw.eq("resource_id", roomId);
        qw.isNull("delete_time");

        List<ResiReceivable> list = receivableMapper.selectList(qw);
        ResiCashierRoomSummaryVo summary = new ResiCashierRoomSummaryVo();

        for (ResiReceivable r : list) {
            String payState = r.getPayState();
            BigDecimal receivable = r.getReceivable() != null ? r.getReceivable() : BigDecimal.ZERO;
            BigDecimal paidAmount = r.getPaidAmount() != null ? r.getPaidAmount() : BigDecimal.ZERO;

            summary.setTotalReceivable(summary.getTotalReceivable().add(receivable));
            summary.setTotalPaid(summary.getTotalPaid().add(paidAmount));

            if (ResiConstants.PAY_STATE_UNPAID.equals(payState)) {
                summary.setTotalArrears(summary.getTotalArrears().add(receivable));
                summary.setUnpaidCount(summary.getUnpaidCount() + 1);
            } else if (ResiConstants.PAY_STATE_PART_PAID.equals(payState)) {
                summary.setTotalArrears(summary.getTotalArrears().add(receivable.subtract(paidAmount)));
                summary.setPartPaidCount(summary.getPartPaidCount() + 1);
            } else if (ResiConstants.PAY_STATE_PAID.equals(payState)) {
                summary.setPaidCount(summary.getPaidCount() + 1);
            }
        }

        return summary;
    }

    /**
     * 将 ResiRoom 转换为搜索结果 VO
     */
    private ResiCashierRoomSearchVo convertToVo(ResiRoom room) {
        ResiCashierRoomSearchVo vo = new ResiCashierRoomSearchVo();
        vo.setId(room.getId());
        vo.setProjectId(room.getProjectId());
        vo.setBuildingId(room.getBuildingId());
        vo.setRoomNo(room.getRoomNo());
        vo.setRoomAlias(room.getRoomAlias());
        vo.setBuildingArea(room.getBuildingArea());
        vo.setProjectName(room.getProjectName());
        vo.setBuildingName(room.getBuildingName());

        // 查询当前业主
        List<ResiCustomerAsset> assets = customerAssetService.lambdaQuery()
                .eq(ResiCustomerAsset::getAssetType, ResiConstants.ASSET_TYPE_ROOM)
                .eq(ResiCustomerAsset::getAssetId, room.getId())
                .eq(ResiCustomerAsset::getIsCurrent, 1)
                .list();
        if (assets != null && !assets.isEmpty()) {
            Long customerId = assets.get(0).getCustomerId();
            ResiCustomer customer = customerService.getById(customerId);
            if (customer != null) {
                vo.setCustomerId(customerId);
                vo.setCustomerName(customer.getCustomerName());
            }
        }

        return vo;
    }
}
