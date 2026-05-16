package com.zhaoxinms.resi.cashier.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoxinms.common.core.domain.entity.SysUser;
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.resi.archive.entity.ResiProject;
import com.zhaoxinms.resi.archive.mapper.ResiProjectMapper;
import com.zhaoxinms.resi.cashier.dto.ResiNoticePrintVo;
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
    private ResiProjectMapper projectMapper;

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

    @Override
    public ResiNoticePrintVo getNoticePrintData(String receivableId) {
        // 1. 查询应收记录
        ResiReceivable receivable = receivableMapper.selectById(receivableId);
        if (receivable == null) {
            throw new ServiceException("应收记录不存在");
        }
        if (receivable.getDeleteTime() != null) {
            throw new ServiceException("应收记录已删除");
        }

        return buildNoticePrintVo(receivable);
    }

    @Override
    public List<ResiNoticePrintVo> batchNoticePrintData(List<String> receivableIds) {
        if (receivableIds == null || receivableIds.isEmpty()) {
            throw new ServiceException("请选择至少一条应收记录");
        }

        // 批量查询应收记录
        List<ResiReceivable> receivables = receivableMapper.selectList(
                new QueryWrapper<ResiReceivable>()
                        .in("id", receivableIds)
                        .isNull("delete_time"));
        if (receivables.isEmpty()) {
            throw new ServiceException("所选应收记录不存在或已删除");
        }

        // 按 projectId + resourceId 分组（同一房间的通知单合并为一张）
        Map<String, List<ResiReceivable>> groupMap = new LinkedHashMap<>();
        for (ResiReceivable r : receivables) {
            String key = r.getProjectId() + "_" + r.getResourceId();
            groupMap.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
        }

        List<ResiNoticePrintVo> result = new ArrayList<>();
        for (List<ResiReceivable> group : groupMap.values()) {
            // 以组内第一条记录作为基础构建通知单
            ResiNoticePrintVo vo = buildNoticePrintVoForBatch(group);
            result.add(vo);
        }

        log.info("批量缴费通知单打印 receivableIds={} groups={}", receivableIds.size(), result.size());
        return result;
    }

    /**
     * 构建单条应收记录的缴费通知单数据
     */
    private ResiNoticePrintVo buildNoticePrintVo(ResiReceivable receivable) {
        ResiNoticePrintVo result = new ResiNoticePrintVo();

        // 1. 查询票据模板配置（缴费通知单 ticketType=2）
        ResiTicketConfig ticketConfig = null;
        if (receivable.getProjectId() != null) {
            List<ResiTicketConfig> configs = ticketConfigMapper.selectList(
                    new QueryWrapper<ResiTicketConfig>()
                            .eq("project_id", receivable.getProjectId())
                            .eq("ticket_type", 2)
                            .eq("enabled_mark", 1)
                            .orderByDesc("create_time")
                            .last("LIMIT 1"));
            if (configs != null && !configs.isEmpty()) {
                ticketConfig = configs.get(0);
            }
        }

        ResiNoticePrintVo.TicketConfig configVo = new ResiNoticePrintVo.TicketConfig();
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

        // 2. 查询项目信息
        ResiNoticePrintVo.ProjectInfo projectVo = new ResiNoticePrintVo.ProjectInfo();
        if (receivable.getProjectId() != null) {
            ResiProject project = projectMapper.selectById(receivable.getProjectId());
            if (project != null) {
                projectVo.setProjectId(project.getId());
                projectVo.setName(project.getName());
                projectVo.setAddress(project.getAddress());
                projectVo.setContactPhone(project.getContactPhone());
                projectVo.setManagerName(project.getManagerName());
                projectVo.setManagerPhone(project.getManagerPhone());
            }
        }
        result.setProject(projectVo);

        // 3. 业主/房间信息
        ResiNoticePrintVo.CustomerInfo customerVo = new ResiNoticePrintVo.CustomerInfo();
        customerVo.setResourceName(receivable.getResourceName());
        customerVo.setCustomerName(receivable.getCustomerName());
        result.setCustomer(customerVo);

        // 4. 费用明细（单条）
        List<ResiNoticePrintVo.FeeItem> feeItems = new ArrayList<>();
        ResiNoticePrintVo.FeeItem item = new ResiNoticePrintVo.FeeItem();
        item.setReceivableId(receivable.getId());
        item.setFeeName(receivable.getFeeName());
        item.setBillPeriod(receivable.getBillPeriod());
        item.setBeginDate(receivable.getBeginDate());
        item.setEndDate(receivable.getEndDate());
        item.setPrice(receivable.getPrice());
        item.setNum(receivable.getNum());
        item.setTotal(receivable.getTotal());
        item.setOverdueFee(receivable.getOverdueFee());
        item.setDiscountAmount(receivable.getDiscountAmount());
        item.setReceivable(receivable.getReceivable());
        item.setRemark(receivable.getRemark());
        feeItems.add(item);
        result.setFeeItems(feeItems);

        // 5. 合计金额
        result.setTotalReceivable(receivable.getReceivable());
        result.setNoticeDate(new Date());

        return result;
    }

    /**
     * 批量模式下构建缴费通知单（同一房间多费用合并到一张通知单）
     */
    private ResiNoticePrintVo buildNoticePrintVoForBatch(List<ResiReceivable> receivables) {
        if (receivables == null || receivables.isEmpty()) {
            throw new ServiceException("通知单数据为空");
        }

        // 以第一条记录作为基础信息
        ResiReceivable first = receivables.get(0);

        ResiNoticePrintVo result = new ResiNoticePrintVo();

        // 1. 查询票据模板配置（缴费通知单 ticketType=2）
        ResiTicketConfig ticketConfig = null;
        if (first.getProjectId() != null) {
            List<ResiTicketConfig> configs = ticketConfigMapper.selectList(
                    new QueryWrapper<ResiTicketConfig>()
                            .eq("project_id", first.getProjectId())
                            .eq("ticket_type", 2)
                            .eq("enabled_mark", 1)
                            .orderByDesc("create_time")
                            .last("LIMIT 1"));
            if (configs != null && !configs.isEmpty()) {
                ticketConfig = configs.get(0);
            }
        }

        ResiNoticePrintVo.TicketConfig configVo = new ResiNoticePrintVo.TicketConfig();
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

        // 2. 查询项目信息
        ResiNoticePrintVo.ProjectInfo projectVo = new ResiNoticePrintVo.ProjectInfo();
        if (first.getProjectId() != null) {
            ResiProject project = projectMapper.selectById(first.getProjectId());
            if (project != null) {
                projectVo.setProjectId(project.getId());
                projectVo.setName(project.getName());
                projectVo.setAddress(project.getAddress());
                projectVo.setContactPhone(project.getContactPhone());
                projectVo.setManagerName(project.getManagerName());
                projectVo.setManagerPhone(project.getManagerPhone());
            }
        }
        result.setProject(projectVo);

        // 3. 业主/房间信息
        ResiNoticePrintVo.CustomerInfo customerVo = new ResiNoticePrintVo.CustomerInfo();
        customerVo.setResourceName(first.getResourceName());
        customerVo.setCustomerName(first.getCustomerName());
        result.setCustomer(customerVo);

        // 4. 费用明细（多条合并）
        List<ResiNoticePrintVo.FeeItem> feeItems = new ArrayList<>();
        BigDecimal totalReceivable = BigDecimal.ZERO;
        for (ResiReceivable r : receivables) {
            ResiNoticePrintVo.FeeItem item = new ResiNoticePrintVo.FeeItem();
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
            item.setRemark(r.getRemark());
            feeItems.add(item);
            totalReceivable = totalReceivable.add(r.getReceivable());
        }
        result.setFeeItems(feeItems);

        // 5. 合计金额
        result.setTotalReceivable(totalReceivable);
        result.setNoticeDate(new Date());

        return result;
    }
}
