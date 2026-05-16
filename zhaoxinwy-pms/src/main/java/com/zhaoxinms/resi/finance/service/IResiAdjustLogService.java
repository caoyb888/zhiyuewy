package com.zhaoxinms.resi.finance.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoxinms.resi.finance.dto.ResiAdjustLogQuery;
import com.zhaoxinms.resi.finance.entity.ResiAdjustLog;

/**
 * 调账记录 Service接口
 *
 * @author zhaoxinms
 */
public interface IResiAdjustLogService extends IService<ResiAdjustLog> {

    /**
     * 查询调账记录列表
     *
     * @param query 查询条件
     * @return 调账记录列表
     */
    List<ResiAdjustLog> selectResiAdjustLogList(ResiAdjustLogQuery query);
}
