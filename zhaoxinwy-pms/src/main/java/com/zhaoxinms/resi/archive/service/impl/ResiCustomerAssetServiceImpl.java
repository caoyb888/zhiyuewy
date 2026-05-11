package com.zhaoxinms.resi.archive.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoxinms.resi.archive.entity.ResiCustomerAsset;
import com.zhaoxinms.resi.archive.mapper.ResiCustomerAssetMapper;
import com.zhaoxinms.resi.archive.service.IResiCustomerAssetService;

/**
 * 客户资产绑定 Service实现
 *
 * @author zhaoxinms
 */
@Service
public class ResiCustomerAssetServiceImpl extends ServiceImpl<ResiCustomerAssetMapper, ResiCustomerAsset>
        implements IResiCustomerAssetService {

    @Override
    public List<ResiCustomerAsset> selectAssetsByCustomerId(Long customerId) {
        return baseMapper.selectAssetsByCustomerId(customerId);
    }

    @Override
    public ResiCustomerAsset selectCurrentBinding(Integer assetType, Long assetId) {
        return baseMapper.selectCurrentBinding(assetType, assetId);
    }
}
