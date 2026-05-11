package com.zhaoxinms.resi.feeconfig.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeAllocation;

/**
 * 费用分配 Mapper接口
 *
 * @author zhaoxinms
 */
public interface ResiFeeAllocationMapper extends BaseMapper<ResiFeeAllocation> {

    /**
     * 查询费用分配列表（含费用名称、项目名称）
     */
    List<ResiFeeAllocation> selectResiFeeAllocationList(ResiFeeAllocation allocation);

    /**
     * 查询指定费用和生效日期下已存在的资源ID列表
     *
     * @param feeId        费用定义ID
     * @param resourceType 资源类型
     * @param startDate    生效日期
     * @return 已存在的资源ID列表
     */
    List<Long> selectExistResourceIds(@Param("feeId") String feeId,
                                       @Param("resourceType") String resourceType,
                                       @Param("startDate") String startDate);
}
