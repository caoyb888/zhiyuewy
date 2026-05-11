package com.zhaoxinms.resi.archive.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoxinms.resi.archive.entity.ResiCustomer;

/**
 * 客户档案 Service接口
 *
 * @author zhaoxinms
 */
public interface IResiCustomerService extends IService<ResiCustomer> {

    /**
     * 查询客户列表
     */
    List<ResiCustomer> selectResiCustomerList(ResiCustomer customer);
}
