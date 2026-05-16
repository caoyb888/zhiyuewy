package com.zhaoxinms.resi.finance.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhaoxinms.resi.finance.entity.ResiPreAccount;

/**
 * 预收款余额账户 Mapper接口
 */
public interface ResiPreAccountMapper extends BaseMapper<ResiPreAccount> {

    /**
     * 按资源查询可用预收款账户并加锁（FOR UPDATE）
     */
    List<ResiPreAccount> selectListForUpdate(@Param("projectId") Long projectId,
            @Param("resourceType") String resourceType, @Param("resourceId") Long resourceId);

    /**
     * 按ID批量查询并加锁（FOR UPDATE）
     */
    List<ResiPreAccount> selectBatchIdsForUpdate(@Param("list") List<String> ids);
}
