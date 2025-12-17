package com.river.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.river.entity.ChatRoom;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天室Mapper
 */
@Mapper
public interface ChatRoomMapper extends BaseMapper<ChatRoom> {
}
