package com.zhaoxinms.resi.finance.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoxinms.resi.finance.dto.ResiAdjustLogQuery;
import com.zhaoxinms.resi.finance.entity.ResiAdjustLog;
import com.zhaoxinms.resi.finance.mapper.ResiAdjustLogMapper;
import com.zhaoxinms.resi.finance.service.IResiAdjustLogService;

/**
 * 调账记录 Service实现
 *
 * @author zhaoxinms
 */
@Service
public class ResiAdjustLogServiceImpl extends ServiceImpl<ResiAdjustLogMapper, ResiAdjustLog>
        implements IResiAdjustLogService {

    @Autowired
    private ResiAdjustLogMapper adjustLogMapper;

    @Override
    public List<ResiAdjustLog> selectResiAdjustLogList(ResiAdjustLogQuery query) {
        return adjustLogMapper.selectResiAdjustLogList(query);
    }
}
