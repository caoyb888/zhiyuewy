package com.zhaoxinms.resi.feeconfig.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig;

/**
 * 票据模板配置 Service接口
 *
 * @author zhaoxinms
 */
public interface IResiTicketConfigService extends IService<ResiTicketConfig> {

    /**
     * 查询票据配置列表
     *
     * @param ticketConfig 查询条件
     * @return 票据配置列表
     */
    List<ResiTicketConfig> selectResiTicketConfigList(ResiTicketConfig ticketConfig);
}
