package com.zhaoxinms.resi.meter.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoxinms.resi.meter.dto.ResiMeterShareCalcReq;
import com.zhaoxinms.resi.meter.dto.ResiMeterShareCalcResultVo;
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

    /**
     * 公摊计算
     *
     * @param req 公摊计算请求
     * @return 公摊计算结果列表（按公摊组汇总）
     */
    List<ResiMeterShareCalcResultVo> calcShare(ResiMeterShareCalcReq req);

    /**
     * 公摊计算结果预览（不写入数据库）
     *
     * @param req 公摊计算请求
     * @return 公摊计算结果列表（按公摊组汇总）
     */
    List<ResiMeterShareCalcResultVo> previewShare(ResiMeterShareCalcReq req);
}
