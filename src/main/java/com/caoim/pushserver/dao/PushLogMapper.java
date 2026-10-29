package com.caoim.pushserver.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caoim.pushserver.entity.PushLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PushLogMapper extends BaseMapper<PushLog> {
}
