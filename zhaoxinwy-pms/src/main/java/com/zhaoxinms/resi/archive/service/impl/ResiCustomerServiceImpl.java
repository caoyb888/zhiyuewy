package com.zhaoxinms.resi.archive.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoxinms.resi.archive.entity.ResiCustomer;
import com.zhaoxinms.resi.archive.mapper.ResiCustomerMapper;
import com.zhaoxinms.resi.archive.service.IResiCustomerService;

/**
 * 客户档案 Service实现
 *
 * @author zhaoxinms
 */
@Service
public class ResiCustomerServiceImpl extends ServiceImpl<ResiCustomerMapper, ResiCustomer>
        implements IResiCustomerService {

    @Override
    public List<ResiCustomer> selectResiCustomerList(ResiCustomer customer) {
        return baseMapper.selectResiCustomerList(customer);
    }
}
