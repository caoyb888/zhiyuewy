package com.zhaoxinms.resi.meter.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoxinms.resi.meter.entity.ResiMeterReading;

/**
 * 抄表记录 Service接口
 *
 * @author zhaoxinms
 */
public interface IResiMeterReadingService extends IService<ResiMeterReading> {

    /**
     * 查询抄表记录列表
     *
     * @param reading 查询条件
     * @return 抄表记录列表
     */
    List<ResiMeterReading> selectResiMeterReadingList(ResiMeterReading reading);
}
