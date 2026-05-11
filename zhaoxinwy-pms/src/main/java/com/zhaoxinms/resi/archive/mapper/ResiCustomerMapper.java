package com.zhaoxinms.resi.archive.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhaoxinms.resi.archive.entity.ResiCustomer;

/**
 * 客户档案 Mapper接口
 *
 * @author zhaoxinms
 */
public interface ResiCustomerMapper extends BaseMapper<ResiCustomer> {

    /**
     * 查询客户列表
     */
    List<ResiCustomer> selectResiCustomerList(ResiCustomer customer);
}
