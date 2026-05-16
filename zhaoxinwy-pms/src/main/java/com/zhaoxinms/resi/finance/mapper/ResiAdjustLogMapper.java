package com.zhaoxinms.resi.finance.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhaoxinms.resi.finance.dto.ResiAdjustLogQuery;
import com.zhaoxinms.resi.finance.entity.ResiAdjustLog;

/**
 * 调账记录 Mapper接口
 *
 * @author zhaoxinms
 */
public interface ResiAdjustLogMapper extends BaseMapper<ResiAdjustLog> {

    /**
     * 查询调账记录列表
     *
     * @param query 查询条件
     * @return 调账记录列表
     */
    List<ResiAdjustLog> selectResiAdjustLogList(ResiAdjustLogQuery query);
}
