package com.zhaoxinms.resi.meter.mapper;

import java.math.BigDecimal;

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
            "WHERE meter_id = #{meterId} AND enabled_mark = 1 " +
            "ORDER BY period DESC LIMIT 1")
    BigDecimal selectLastReading(@Param("meterId") Long meterId);

    /**
     * 查询指定仪表的最新一期记录（用于带入上期数据）
     */
    @Select("SELECT * FROM resi_meter_reading " +
            "WHERE meter_id = #{meterId} AND enabled_mark = 1 " +
            "ORDER BY period DESC LIMIT 1")
    ResiMeterReading selectLastRecord(@Param("meterId") Long meterId);
}
