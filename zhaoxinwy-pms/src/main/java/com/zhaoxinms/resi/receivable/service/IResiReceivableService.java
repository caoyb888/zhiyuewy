package com.zhaoxinms.resi.receivable.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoxinms.resi.meter.entity.ResiMeterReading;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;

/**
 * 应收账单 Service接口
 *
 * @author zhaoxinms
 */
public interface IResiReceivableService extends IService<ResiReceivable> {

    /**
     * 根据抄表记录生成应收账单
     *
     * @param reading 抄表记录
     * @return 生成的应收账单
     */
    ResiReceivable createFromMeterReading(ResiMeterReading reading);

    /**
     * 根据抄表记录批量生成应收账单
     *
     * @param readings 抄表记录列表
     * @return 生成的应收账单列表
     */
    List<ResiReceivable> createFromMeterReadings(List<ResiMeterReading> readings);
}
