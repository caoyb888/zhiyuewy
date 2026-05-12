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

    /**
     * 单户入账：根据抄表记录生成应收账单
     *
     * @param id 抄表记录ID
     * @return 入账结果 {receivableId: 应收ID, message: 提示信息}
     */
    java.util.Map<String, Object> bill(String id);

    /**
     * 批量入账：按条件批量生成应收账单
     *
     * @param projectId 项目ID
     * @param period    抄表期间
     * @param ids       指定的抄表记录ID列表（可选）
     * @return 入账统计 {total: 处理总数, success: 成功数, skip: 跳过数, fail: 失败数}
     */
    java.util.Map<String, Object> batchBill(Long projectId, String period, java.util.List<String> ids);
}
