package com.zhaoxinms.resi.meter.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhaoxinms.resi.meter.entity.ResiMeterReading;

/**
 * 抄表记录 Mapper接口
 *
 * @author zhaoxinms
 */
public interface ResiMeterReadingMapper extends BaseMapper<ResiMeterReading> {

    /**
     * 查询指定仪表的最新一期读数
     */
    @Select("SELECT curr_reading FROM resi_meter_reading " +
            "WHERE meter_id = #{meterId} " +
            "ORDER BY period DESC LIMIT 1")
    BigDecimal selectLastReading(@Param("meterId") Long meterId);

    /**
     * 查询指定仪表的最新一期记录（用于带入上期数据）
     */
    @Select("SELECT * FROM resi_meter_reading " +
            "WHERE meter_id = #{meterId} " +
            "ORDER BY period DESC LIMIT 1")
    ResiMeterReading selectLastRecord(@Param("meterId") Long meterId);

    /**
     * 查询指定公摊组的抄表记录（含总表和分户表）
     *
     * @param projectId  项目ID
     * @param period     抄表期间
     * @param publicGroup 公摊组编号
     * @return 抄表记录列表
     */
    List<ResiMeterReading> selectShareGroupReadings(@Param("projectId") Long projectId,
                                                    @Param("period") String period,
                                                    @Param("publicGroup") String publicGroup);

    /**
     * 查询指定项目、期间下所有有公摊组的抄表记录
     *
     * @param projectId 项目ID
     * @param period    抄表期间
     * @return 抄表记录列表
     */
    List<ResiMeterReading> selectAllShareGroupReadings(@Param("projectId") Long projectId,
                                                       @Param("period") String period);

    /**
     * 查询公摊计算预览数据
     *
     * @param projectId   项目ID
     * @param period      抄表期间
     * @param publicGroup 公摊组编号（可选）
     * @return 抄表记录列表
     */
    List<ResiMeterReading> selectSharePreview(@Param("projectId") Long projectId,
                                              @Param("period") String period,
                                              @Param("publicGroup") String publicGroup);
}
