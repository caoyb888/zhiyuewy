package com.zhaoxinms.resi.archive.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoxinms.resi.archive.entity.ResiCustomerAsset;

/**
 * 客户资产绑定 Service接口
 *
 * @author zhaoxinms
 */
public interface IResiCustomerAssetService extends IService<ResiCustomerAsset> {

    /**
     * 查询客户的资产列表
     */
    List<ResiCustomerAsset> selectAssetsByCustomerId(Long customerId);
}
