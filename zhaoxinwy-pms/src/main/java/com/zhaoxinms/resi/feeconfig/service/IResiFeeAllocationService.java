package com.zhaoxinms.resi.feeconfig.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeAllocation;

/**
 * 费用分配 Service接口
 *
 * @author zhaoxinms
 */
public interface IResiFeeAllocationService extends IService<ResiFeeAllocation> {

    /**
     * 查询费用分配列表
     *
     * @param allocation 查询条件
     * @return 费用分配列表
     */
    List<ResiFeeAllocation> selectResiFeeAllocationList(ResiFeeAllocation allocation);

    /**
     * 批量分配费用
     *
     * @param allocation  分配模板（含 projectId/feeId/startDate/endDate/customPrice 等）
     * @param batchType   批量方式：BUILDING按楼栋 UNIT按单元 PROJECT全项目
     * @param buildingId  楼栋ID（按楼栋/单元时必填）
     * @param unitNo      单元号（按单元时必填）
     * @return 分配结果 {total:总数, success:成功数, skip:跳过数}
     */
    Map<String, Object> batchAllocate(ResiFeeAllocation allocation, String batchType,
                                       Long buildingId, String unitNo);

    /**
     * 预览批量分配数量
     *
     * @param projectId   项目ID
     * @param feeId       费用定义ID
     * @param batchType   批量方式
     * @param buildingId  楼栋ID
     * @param unitNo      单元号
     * @param startDate   生效日期
     * @return {total:总数, existing:已存在数, newCount:待分配数}
     */
    Map<String, Object> previewBatchAllocate(Long projectId, String feeId, String batchType,
                                              Long buildingId, String unitNo, String startDate);
}
