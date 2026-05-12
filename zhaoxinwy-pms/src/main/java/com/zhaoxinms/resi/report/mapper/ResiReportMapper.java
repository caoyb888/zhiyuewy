package com.zhaoxinms.resi.report.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.zhaoxinms.resi.report.dto.ResiArrearsDetailQuery;
import com.zhaoxinms.resi.report.dto.ResiArrearsDetailVo;
import com.zhaoxinms.resi.report.dto.ResiCollectionRateQuery;
import com.zhaoxinms.resi.report.dto.ResiCollectionRateVo;
import com.zhaoxinms.resi.report.dto.ResiReceivableMgmtQuery;
import com.zhaoxinms.resi.report.dto.ResiReceivableMgmtVo;
import com.zhaoxinms.resi.report.dto.ResiTransactionDetailQuery;
import com.zhaoxinms.resi.report.dto.ResiTransactionDetailVo;
import com.zhaoxinms.resi.report.dto.ResiTransactionSummaryQuery;
import com.zhaoxinms.resi.report.dto.ResiTransactionSummaryVo;

/**
 * 核心报表 Mapper
 */
public interface ResiReportMapper {

    /**
     * 交易汇总 - 按支付方式分组
     */
    List<ResiTransactionSummaryVo> selectTransactionSummaryByPayMethod(ResiTransactionSummaryQuery query);

    /**
     * 交易汇总 - 按费用名称分组
     */
    List<ResiTransactionSummaryVo> selectTransactionSummaryByFeeName(ResiTransactionSummaryQuery query);

    /**
     * 交易明细
     */
    List<ResiTransactionDetailVo> selectTransactionDetail(ResiTransactionDetailQuery query);

    /**
     * 收费率报表
     */
    List<ResiCollectionRateVo> selectCollectionRate(ResiCollectionRateQuery query);

    /**
     * 欠费明细
     */
    List<ResiArrearsDetailVo> selectArrearsDetail(ResiArrearsDetailQuery query);

    /**
     * 应收管理报表
     */
    List<ResiReceivableMgmtVo> selectReceivableMgmt(ResiReceivableMgmtQuery query);
}
