package com.zhaoxinms.resi.archive.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhaoxinms.resi.archive.entity.ResiMeterDevice;

/**
 * 仪表档案 Mapper接口
 *
 * @author zhaoxinms
 */
public interface ResiMeterDeviceMapper extends BaseMapper<ResiMeterDevice> {

    /**
     * 查询仪表列表（含房间名称）
     */
    List<ResiMeterDevice> selectResiMeterDeviceList(ResiMeterDevice device);
}
