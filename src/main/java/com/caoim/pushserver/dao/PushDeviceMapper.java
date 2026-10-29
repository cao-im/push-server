package com.caoim.pushserver.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caoim.pushserver.entity.PushDevice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PushDeviceMapper extends BaseMapper<PushDevice> {
}
