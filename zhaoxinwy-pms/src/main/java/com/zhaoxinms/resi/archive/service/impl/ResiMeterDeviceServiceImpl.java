package com.zhaoxinms.resi.archive.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoxinms.resi.archive.entity.ResiMeterDevice;
import com.zhaoxinms.resi.archive.mapper.ResiMeterDeviceMapper;
import com.zhaoxinms.resi.archive.service.IResiMeterDeviceService;

/**
 * 仪表档案 Service实现
 *
 * @author zhaoxinms
 */
@Service
public class ResiMeterDeviceServiceImpl extends ServiceImpl<ResiMeterDeviceMapper, ResiMeterDevice>
        implements IResiMeterDeviceService {

    @Override
    public List<ResiMeterDevice> selectResiMeterDeviceList(ResiMeterDevice device) {
        return baseMapper.selectResiMeterDeviceList(device);
    }
}
