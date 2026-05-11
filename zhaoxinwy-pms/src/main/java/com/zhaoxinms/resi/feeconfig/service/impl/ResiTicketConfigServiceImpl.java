package com.zhaoxinms.resi.feeconfig.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig;
import com.zhaoxinms.resi.feeconfig.mapper.ResiTicketConfigMapper;
import com.zhaoxinms.resi.feeconfig.service.IResiTicketConfigService;

/**
 * 票据模板配置 Service实现
 *
 * @author zhaoxinms
 */
@Service
public class ResiTicketConfigServiceImpl extends ServiceImpl<ResiTicketConfigMapper, ResiTicketConfig>
        implements IResiTicketConfigService {

    @Override
    public List<ResiTicketConfig> selectResiTicketConfigList(ResiTicketConfig ticketConfig) {
        QueryWrapper<ResiTicketConfig> queryWrapper = new QueryWrapper<>();

        if (ticketConfig.getProjectId() != null) {
            queryWrapper.eq("project_id", ticketConfig.getProjectId());
        }

        if (ticketConfig.getTicketType() != null) {
            queryWrapper.eq("ticket_type", ticketConfig.getTicketType());
        }

        if (StringUtils.isNotBlank(ticketConfig.getTitle())) {
            queryWrapper.like("title", ticketConfig.getTitle());
        }

        queryWrapper.eq("enabled_mark", 1);
        queryWrapper.orderByDesc("creator_time");

        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public boolean save(ResiTicketConfig entity) {
        Date now = new Date();
        entity.setCreatorTime(now);
        entity.setCreatorUserId(String.valueOf(SecurityUtils.getUserId()));
        entity.setLastModifyTime(now);
        entity.setLastModifyUserId(String.valueOf(SecurityUtils.getUserId()));
        entity.setEnabledMark(1);
        return super.save(entity);
    }

    @Override
    public boolean updateById(ResiTicketConfig entity) {
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(String.valueOf(SecurityUtils.getUserId()));
        return super.updateById(entity);
    }

    @Override
    public boolean removeByIds(java.util.Collection<? extends java.io.Serializable> idList) {
        for (java.io.Serializable id : idList) {
            ResiTicketConfig entity = new ResiTicketConfig();
            entity.setId((String) id);
            entity.setEnabledMark(0);
            baseMapper.updateById(entity);
        }
        return true;
    }
}
