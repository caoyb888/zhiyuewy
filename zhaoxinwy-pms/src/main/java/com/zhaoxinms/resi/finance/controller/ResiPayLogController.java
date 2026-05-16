package com.zhaoxinms.resi.finance.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoxinms.common.annotation.Log;
import com.zhaoxinms.common.core.controller.BaseController;
import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.common.core.page.TableDataInfo;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.finance.dto.ResiPayLogQuery;
import com.zhaoxinms.resi.finance.entity.ResiPayLog;
import com.zhaoxinms.resi.finance.mapper.ResiPayLogMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 收款流水 Controller
 */
@Api(tags = "住宅收费-收款流水")
@RestController
@RequestMapping("/resi/finance/pay-log")
public class ResiPayLogController extends BaseController {

    @Autowired
    private ResiPayLogMapper payLogMapper;

    /**
     * 收款流水列表
     */
    @ApiOperation("收款流水-列表查询")
    @PreAuthorize("@ss.hasPermi('resi:finance:paylog:query')")
    @GetMapping("/list")
    public TableDataInfo list(ResiPayLogQuery query) {
        startPage();
        QueryWrapper<ResiPayLog> qw = new QueryWrapper<>();
        if (query.getProjectId() != null) {
            qw.eq("project_id", query.getProjectId());
        }
        if (StringUtils.isNotBlank(query.getResourceType())) {
            qw.eq("resource_type", query.getResourceType());
        }
        if (query.getResourceId() != null) {
            qw.eq("resource_id", query.getResourceId());
        }
        if (StringUtils.isNotBlank(query.getPayType())) {
            qw.eq("pay_type", query.getPayType());
        }
        if (StringUtils.isNotBlank(query.getPayMethod())) {
            qw.eq("pay_method", query.getPayMethod());
        }
        if (StringUtils.isNotBlank(query.getPayNo())) {
            qw.like("pay_no", query.getPayNo());
        }
        if (StringUtils.isNotBlank(query.getBeginTime())) {
            qw.ge("creator_time", query.getBeginTime() + " 00:00:00");
        }
        if (StringUtils.isNotBlank(query.getEndTime())) {
            qw.le("creator_time", query.getEndTime() + " 23:59:59");
        }
        qw.orderByDesc("creator_time");
        List<ResiPayLog> list = payLogMapper.selectList(qw);
        return getDataTable(list);
    }

    /**
     * 收款流水详情
     */
    @ApiOperation("收款流水-详情")
    @PreAuthorize("@ss.hasPermi('resi:finance:paylog:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        ResiPayLog log = payLogMapper.selectById(id);
        return AjaxResult.success(log);
    }

    /**
     * 复核收款流水
     */
    @ApiOperation("收款流水-复核")
    @PreAuthorize("@ss.hasPermi('resi:finance:paylog:verify')")
    @Log(title = "住宅收费-收款流水-复核", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/verify")
    public AjaxResult verify(@PathVariable("id") String id) {
        ResiPayLog payLog = payLogMapper.selectById(id);
        if (payLog == null) {
            throw new ServiceException("收款流水不存在");
        }
        if (Integer.valueOf(1).equals(payLog.getIsVerified())) {
            throw new ServiceException("该收款单已复核，无需重复操作");
        }
        payLog.setIsVerified(1);
        payLog.setVerifiedBy(String.valueOf(SecurityUtils.getUserId()));
        payLog.setVerifiedTime(new Date());
        payLogMapper.updateById(payLog);
        return AjaxResult.success("复核成功");
    }
}
