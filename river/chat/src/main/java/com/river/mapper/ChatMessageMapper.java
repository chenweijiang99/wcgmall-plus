package com.river.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.river.entity.ChatMessage;
import com.river.vo.ChatMessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 获取消息列表(带发送者信息)
     */
    List<ChatMessageVO> selectMessageListWithSenderInfo(
            @Param("roomId") Long roomId,
            @Param("lastMsgId") Long lastMsgId,
            @Param("pageSize") Integer pageSize);

    /**
     * 获取单条消息(带发送者信息)
     */
    ChatMessageVO selectMessageWithSenderInfo(@Param("msgId") Long msgId);
}
