package com.zhaoxinms.resi.archive.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.archive.entity.ResiBuilding;
import com.zhaoxinms.resi.archive.entity.ResiRoom;
import com.zhaoxinms.resi.archive.mapper.ResiRoomMapper;
import com.zhaoxinms.resi.archive.service.IResiBuildingService;
import com.zhaoxinms.resi.archive.service.IResiRoomService;

/**
 * 房间档案 Service实现
 *
 * @author zhaoxinms
 */
@Service
public class ResiRoomServiceImpl extends ServiceImpl<ResiRoomMapper, ResiRoom>
        implements IResiRoomService {

    @Autowired
    private IResiBuildingService buildingService;

    @Override
    public List<ResiRoom> selectResiRoomList(ResiRoom room) {
        return baseMapper.selectResiRoomList(room);
    }

    @Override
    public List<ResiRoom> searchRoom(String keyword, Long projectId) {
        return baseMapper.searchRoom(keyword, projectId);
    }

    @Override
    public boolean checkRoomUnique(Long projectId, Long buildingId, String unitNo, String roomNo) {
        return checkRoomUnique(projectId, buildingId, unitNo, roomNo, null);
    }

    @Override
    public boolean checkRoomUnique(Long projectId, Long buildingId, String unitNo, String roomNo, Long excludeId) {
        QueryWrapper<ResiRoom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_id", projectId);
        queryWrapper.eq("building_id", buildingId);
        queryWrapper.eq("room_no", roomNo);
        if (StringUtils.isNotBlank(unitNo)) {
            queryWrapper.eq("unit_no", unitNo);
        } else {
            queryWrapper.and(qw -> qw.isNull("unit_no").or().eq("unit_no", ""));
        }
        queryWrapper.eq("enabled_mark", 1);
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        return baseMapper.selectCount(queryWrapper) == 0;
    }

    /**
     * 自动生成 room_alias：{楼栋名}{单元号}{房号}
     */
    public String generateRoomAlias(Long buildingId, String unitNo, String roomNo) {
        ResiBuilding building = buildingService.getById(buildingId);
        String buildingName = building != null ? building.getName() : "";
        String alias = buildingName;
        if (StringUtils.isNotBlank(unitNo)) {
            alias = alias + unitNo + "单元";
        }
        alias = alias + roomNo;
        return alias;
    }
}
