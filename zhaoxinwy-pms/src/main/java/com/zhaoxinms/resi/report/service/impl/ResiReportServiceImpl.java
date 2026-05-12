package com.zhaoxinms.resi.report.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import com.zhaoxinms.resi.report.mapper.ResiReportMapper;
import com.zhaoxinms.resi.report.service.IResiReportService;

/**
 * 核心报表 Service 实现
 */
@Service
public class ResiReportServiceImpl implements IResiReportService {

    @Autowired
    private ResiReportMapper reportMapper;

    @Override
    public List<ResiTransactionSummaryVo> transactionSummary(ResiTransactionSummaryQuery query) {
        if ("feeName".equals(query.getGroupBy())) {
            return reportMapper.selectTransactionSummaryByFeeName(query);
        }
        // 默认按支付方式分组
        return reportMapper.selectTransactionSummaryByPayMethod(query);
    }

    @Override
    public List<ResiTransactionDetailVo> transactionDetail(ResiTransactionDetailQuery query) {
        return reportMapper.selectTransactionDetail(query);
    }

    @Override
    public List<ResiCollectionRateVo> collectionRate(ResiCollectionRateQuery query) {
        return reportMapper.selectCollectionRate(query);
    }

    @Override
    public List<ResiArrearsDetailVo> arrearsDetail(ResiArrearsDetailQuery query) {
        return reportMapper.selectArrearsDetail(query);
    }

    @Override
    public List<ResiReceivableMgmtVo> receivableMgmt(ResiReceivableMgmtQuery query) {
        return reportMapper.selectReceivableMgmt(query);
    }
}
