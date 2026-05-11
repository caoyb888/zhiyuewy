package com.zhaoxinms.resi.archive.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoxinms.resi.archive.entity.ResiMeterDevice;

/**
 * 仪表档案 Service接口
 *
 * @author zhaoxinms
 */
public interface IResiMeterDeviceService extends IService<ResiMeterDevice> {

    /**
     * 查询仪表列表（含房间名称）
     */
    List<ResiMeterDevice> selectResiMeterDeviceList(ResiMeterDevice device);
}
