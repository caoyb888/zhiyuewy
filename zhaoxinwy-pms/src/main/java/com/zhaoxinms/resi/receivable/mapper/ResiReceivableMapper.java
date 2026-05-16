package com.zhaoxinms.resi.receivable.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhaoxinms.resi.receivable.dto.ResiReceivableQuery;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;

/**
 * 应收账单 Mapper接口
 *
 * @author zhaoxinms
 */
public interface ResiReceivableMapper extends BaseMapper<ResiReceivable> {

    /**
     * 批量插入应收记录
     *
     * @param list 应收记录列表
     * @return 插入条数
     */
    int batchInsert(List<ResiReceivable> list);

    /**
     * 查询应收账单列表
     *
     * @param query 查询条件
     * @return 应收账单列表
     */
    List<ResiReceivable> selectResiReceivableList(ResiReceivableQuery query);
}
