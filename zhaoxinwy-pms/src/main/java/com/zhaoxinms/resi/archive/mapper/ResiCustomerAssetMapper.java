package com.zhaoxinms.resi.archive.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhaoxinms.resi.archive.entity.ResiCustomerAsset;

/**
 * 客户资产绑定 Mapper接口
 *
 * @author zhaoxinms
 */
public interface ResiCustomerAssetMapper extends BaseMapper<ResiCustomerAsset> {

    /**
     * 查询客户的资产列表
     */
    List<ResiCustomerAsset> selectAssetsByCustomerId(@Param("customerId") Long customerId);
}
