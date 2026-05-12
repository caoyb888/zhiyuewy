package com.zhaoxinms.resi.cashier.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoxinms.common.core.domain.entity.SysUser;
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.resi.cashier.dto.ResiReceiptPrintVo;
import com.zhaoxinms.resi.cashier.service.IResiCashierPrintService;
import com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig;
import com.zhaoxinms.resi.feeconfig.mapper.ResiTicketConfigMapper;
import com.zhaoxinms.resi.finance.entity.ResiPayLog;
import com.zhaoxinms.resi.finance.mapper.ResiPayLogMapper;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;
import com.zhaoxinms.resi.receivable.mapper.ResiReceivableMapper;
import com.zhaoxinms.system.service.ISysUserService;

/**
 * 收银台打印 Service实现
 */
@Service
public class ResiCashierPrintServiceImpl implements IResiCashierPrintService {

    private static final Logger log = LoggerFactory.getLogger(ResiCashierPrintServiceImpl.class);

    @Autowired
    private ResiPayLogMapper payLogMapper;

    @Autowired
    private ResiReceivableMapper receivableMapper;

    @Autowired
    private ResiTicketConfigMapper ticketConfigMapper;

    @Autowired
    private ISysUserService sysUserService;

    @Override
    public ResiReceiptPrintVo getReceiptPrintData(String payLogId) {
        // 1. 查询收款流水
        ResiPayLog payLog = payLogMapper.selectById(payLogId);
        if (payLog == null) {
            throw new ServiceException("收款记录不存在");
        }

        // 2. 解析 receivableIds JSON
        List<String> receivableIds = new ArrayList<>();
        if (payLog.getReceivableIds() != null && !payLog.getReceivableIds().isEmpty()) {
            try {
                receivableIds = com.alibaba.fastjson.JSON.parseArray(payLog.getReceivableIds(), String.class);
            } catch (Exception e) {
                log.warn("解析 receivableIds 失败 payLogId={} json={}", payLogId, payLog.getReceivableIds());
            }
        }

        // 3. 查询费用明细
        List<ResiReceivable> receivables = new ArrayList<>();
        if (!receivableIds.isEmpty()) {
            receivables = receivableMapper.selectList(
                    new QueryWrapper<ResiReceivable>()
                            .in("id", receivableIds)
                            .isNull("delete_time"));
        }

        // 4. 查询票据模板配置（收款单 ticketType=1）
        ResiTicketConfig ticketConfig = null;
        if (payLog.getProjectId() != null) {
            List<ResiTicketConfig> configs = ticketConfigMapper.selectList(
                    new QueryWrapper<ResiTicketConfig>()
                            .eq("project_id", payLog.getProjectId())
                            .eq("ticket_type", 1)
                            .eq("enabled_mark", 1)
                            .orderByDesc("create_time")
                            .last("LIMIT 1"));
            if (configs != null && !configs.isEmpty()) {
                ticketConfig = configs.get(0);
            }
        }

        // 5. 查询操作人信息
        SysUser creator = null;
        if (payLog.getCreatorUserId() != null && !payLog.getCreatorUserId().isEmpty()) {
            try {
                creator = sysUserService.selectUserById(Long.valueOf(payLog.getCreatorUserId()));
            } catch (NumberFormatException e) {
                log.warn("转换 creatorUserId 失败 id={}", payLog.getCreatorUserId());
            }
        }

        // 6. 组装返回数据
        ResiReceiptPrintVo result = new ResiReceiptPrintVo();

        // 6.1 票据配置
        ResiReceiptPrintVo.TicketConfig configVo = new ResiReceiptPrintVo.TicketConfig();
        if (ticketConfig != null) {
            configVo.setTitle(ticketConfig.getTitle());
            configVo.setCollectOrg(ticketConfig.getCollectOrg());
            configVo.setPaperSize(ticketConfig.getPaperSize());
            configVo.setLogoUrl(ticketConfig.getLogoUrl());
            configVo.setSealUrl(ticketConfig.getSealUrl());
            configVo.setRemark(ticketConfig.getRemark());
            configVo.setFieldConfig(ticketConfig.getFieldConfig());
        }
        result.setConfig(configVo);

        // 6.2 收款流水
        ResiReceiptPrintVo.PayLogInfo payLogVo = new ResiReceiptPrintVo.PayLogInfo();
        payLogVo.setPayLogId(payLog.getId());
        payLogVo.setPayNo(payLog.getPayNo());
        payLogVo.setResourceName(payLog.getResourceName());
        payLogVo.setCustomerName(payLog.getCustomerName());
        payLogVo.setPayMethod(payLog.getPayMethod());
        payLogVo.setTotalAmount(payLog.getTotalAmount());
        payLogVo.setDiscountAmount(payLog.getDiscountAmount());
        payLogVo.setOverdueAmount(payLog.getOverdueAmount());
        payLogVo.setPrePayAmount(payLog.getPrePayAmount());
        payLogVo.setPayAmount(payLog.getPayAmount());
        payLogVo.setChangeAmount(payLog.getChangeAmount());
        payLogVo.setNote(payLog.getNote());
        payLogVo.setPayTime(payLog.getCreatorTime());
        result.setPayLog(payLogVo);

        // 6.3 费用明细
        List<ResiReceiptPrintVo.FeeItem> feeItems = new ArrayList<>();
        for (ResiReceivable r : receivables) {
            ResiReceiptPrintVo.FeeItem item = new ResiReceiptPrintVo.FeeItem();
            item.setReceivableId(r.getId());
            item.setFeeName(r.getFeeName());
            item.setBillPeriod(r.getBillPeriod());
            item.setBeginDate(r.getBeginDate());
            item.setEndDate(r.getEndDate());
            item.setPrice(r.getPrice());
            item.setNum(r.getNum());
            item.setTotal(r.getTotal());
            item.setOverdueFee(r.getOverdueFee());
            item.setDiscountAmount(r.getDiscountAmount());
            item.setReceivable(r.getReceivable());
            feeItems.add(item);
        }
        result.setFeeItems(feeItems);

        // 6.4 操作人
        ResiReceiptPrintVo.OperatorInfo operatorVo = new ResiReceiptPrintVo.OperatorInfo();
        if (creator != null) {
            operatorVo.setUserId(creator.getUserId());
            operatorVo.setNickName(creator.getNickName());
            operatorVo.setUserName(creator.getUserName());
        }
        result.setOperator(operatorVo);

        log.info("获取收款单打印数据 payLogId={} feeItems={} configFound={}",
                payLogId, feeItems.size(), ticketConfig != null);
        return result;
    }
}
