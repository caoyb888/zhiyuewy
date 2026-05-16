package com.zhaoxinms.resi.report.service;

import java.util.List;

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
 * 核心报表 Service 接口
 */
public interface IResiReportService {

    /**
     * 交易汇总报表
     */
    List<ResiTransactionSummaryVo> transactionSummary(ResiTransactionSummaryQuery query);

    /**
     * 交易明细报表
     */
    List<ResiTransactionDetailVo> transactionDetail(ResiTransactionDetailQuery query);

    /**
     * 收费率报表
     */
    List<ResiCollectionRateVo> collectionRate(ResiCollectionRateQuery query);

    /**
     * 欠费明细报表
     */
    List<ResiArrearsDetailVo> arrearsDetail(ResiArrearsDetailQuery query);

    /**
     * 应收管理报表
     */
    List<ResiReceivableMgmtVo> receivableMgmt(ResiReceivableMgmtQuery query);
}
