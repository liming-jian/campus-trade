package com.campus_trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus_trade.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
