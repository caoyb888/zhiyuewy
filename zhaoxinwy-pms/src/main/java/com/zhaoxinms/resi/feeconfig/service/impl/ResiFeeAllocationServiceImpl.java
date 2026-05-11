package com.zhaoxinms.resi.feeconfig.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.archive.entity.ResiRoom;
import com.zhaoxinms.resi.archive.service.IResiRoomService;
import com.zhaoxinms.resi.common.ResiConstants;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeAllocation;
import com.zhaoxinms.resi.feeconfig.mapper.ResiFeeAllocationMapper;
import com.zhaoxinms.resi.feeconfig.service.IResiFeeAllocationService;

/**
 * 费用分配 Service实现
 *
 * @author zhaoxinms
 */
@Service
public class ResiFeeAllocationServiceImpl extends ServiceImpl<ResiFeeAllocationMapper, ResiFeeAllocation>
        implements IResiFeeAllocationService {

    private static final Logger log = LoggerFactory.getLogger(ResiFeeAllocationServiceImpl.class);

    @Autowired
    private IResiRoomService roomService;

    @Override
    public List<ResiFeeAllocation> selectResiFeeAllocationList(ResiFeeAllocation allocation) {
        return baseMapper.selectResiFeeAllocationList(allocation);
    }

    @Override
    public boolean save(ResiFeeAllocation entity) {
        Date now = new Date();
        entity.setCreatorTime(now);
        entity.setCreatorUserId(String.valueOf(SecurityUtils.getUserId()));
        entity.setLastModifyTime(now);
        entity.setLastModifyUserId(String.valueOf(SecurityUtils.getUserId()));
        entity.setEnabledMark(1);
        // 资源名称兜底
        if (StringUtils.isBlank(entity.getResourceName())
                && ResiConstants.RESOURCE_TYPE_ROOM.equals(entity.getResourceType())) {
            ResiRoom room = roomService.getById(entity.getResourceId());
            if (room != null) {
                entity.setResourceName(room.getRoomAlias());
            }
        }
        return baseMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(ResiFeeAllocation entity) {
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(String.valueOf(SecurityUtils.getUserId()));
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    public boolean removeByIds(java.util.Collection<? extends java.io.Serializable> idList) {
        for (java.io.Serializable id : idList) {
            ResiFeeAllocation entity = new ResiFeeAllocation();
            entity.setId((String) id);
            entity.setEnabledMark(0);
            entity.setLastModifyTime(new Date());
            entity.setLastModifyUserId(String.valueOf(SecurityUtils.getUserId()));
            baseMapper.updateById(entity);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchAllocate(ResiFeeAllocation allocation, String batchType,
                                             Long buildingId, String unitNo) {
        // 1. 查询符合条件的房间列表
        List<ResiRoom> roomList = queryTargetRooms(allocation.getProjectId(), batchType, buildingId, unitNo);
        int total = roomList.size();
        if (total == 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("total", 0);
            result.put("success", 0);
            result.put("skip", 0);
            return result;
        }

        // 2. 查询该费用+生效日期下已存在的资源ID，避免唯一键冲突
        String startDateStr = new SimpleDateFormat("yyyy-MM-dd").format(allocation.getStartDate());
        List<Long> existIds = baseMapper.selectExistResourceIds(
                allocation.getFeeId(), ResiConstants.RESOURCE_TYPE_ROOM, startDateStr);
        java.util.Set<Long> existIdSet = existIds != null ? new java.util.HashSet<>(existIds) : new java.util.HashSet<>();

        // 3. 过滤已存在的，构造待插入列表
        List<ResiFeeAllocation> insertList = new ArrayList<>();
        Date now = new Date();
        String userId = String.valueOf(SecurityUtils.getUserId());
        for (ResiRoom room : roomList) {
            if (existIdSet.contains(room.getId())) {
                continue;
            }
            ResiFeeAllocation item = new ResiFeeAllocation();
            item.setProjectId(allocation.getProjectId());
            item.setFeeId(allocation.getFeeId());
            item.setResourceType(ResiConstants.RESOURCE_TYPE_ROOM);
            item.setResourceId(room.getId());
            item.setResourceName(room.getRoomAlias());
            item.setCustomPrice(allocation.getCustomPrice());
            item.setCustomFormula(allocation.getCustomFormula());
            item.setStartDate(allocation.getStartDate());
            item.setEndDate(allocation.getEndDate());
            item.setEnabledMark(1);
            item.setCreatorTime(now);
            item.setCreatorUserId(userId);
            item.setLastModifyTime(now);
            item.setLastModifyUserId(userId);
            insertList.add(item);
        }

        int skip = total - insertList.size();
        int success = 0;

        // 4. 批量插入，500条一批
        if (!insertList.isEmpty()) {
            int batchSize = 500;
            for (int i = 0; i < insertList.size(); i += batchSize) {
                List<ResiFeeAllocation> batch = insertList.subList(i, Math.min(i + batchSize, insertList.size()));
                for (ResiFeeAllocation item : batch) {
                    int r = baseMapper.insert(item);
                    if (r > 0) {
                        success++;
                    }
                }
            }
        }

        log.info("批量费用分配完成 projectId={} feeId={} batchType={} total={} success={} skip={}",
                allocation.getProjectId(), allocation.getFeeId(), batchType, total, success, skip);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("success", success);
        result.put("skip", skip);
        return result;
    }

    @Override
    public Map<String, Object> previewBatchAllocate(Long projectId, String feeId, String batchType,
                                                     Long buildingId, String unitNo, String startDate) {
        List<ResiRoom> roomList = queryTargetRooms(projectId, batchType, buildingId, unitNo);
        int total = roomList.size();

        List<Long> existIds = baseMapper.selectExistResourceIds(feeId, ResiConstants.RESOURCE_TYPE_ROOM, startDate);
        int existing = existIds != null ? existIds.size() : 0;

        // 实际跳过数 = 交集
        java.util.Set<Long> roomIdSet = roomList.stream().map(ResiRoom::getId).collect(Collectors.toSet());
        int skip = 0;
        if (existIds != null) {
            for (Long id : existIds) {
                if (roomIdSet.contains(id)) {
                    skip++;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("existing", existing);
        result.put("newCount", total - skip);
        result.put("skip", skip);
        return result;
    }

    /**
     * 查询目标房间列表
     */
    private List<ResiRoom> queryTargetRooms(Long projectId, String batchType, Long buildingId, String unitNo) {
        QueryWrapper<ResiRoom> qw = new QueryWrapper<>();
        qw.eq("enabled_mark", 1);

        if ("BUILDING".equals(batchType)) {
            if (buildingId == null) {
                throw new ServiceException("按楼栋批量分配时，楼栋ID不能为空");
            }
            qw.eq("building_id", buildingId);
        } else if ("UNIT".equals(batchType)) {
            if (buildingId == null) {
                throw new ServiceException("按单元批量分配时，楼栋ID不能为空");
            }
            if (StringUtils.isBlank(unitNo)) {
                throw new ServiceException("按单元批量分配时，单元号不能为空");
            }
            qw.eq("building_id", buildingId);
            qw.eq("unit_no", unitNo);
        } else if ("PROJECT".equals(batchType)) {
            if (projectId == null) {
                throw new ServiceException("全项目批量分配时，项目ID不能为空");
            }
            qw.eq("project_id", projectId);
        } else {
            throw new ServiceException("不支持的批量方式：" + batchType);
        }

        return roomService.list(qw);
    }
}
