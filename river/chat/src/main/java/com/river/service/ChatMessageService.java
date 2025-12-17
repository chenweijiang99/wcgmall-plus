package com.river.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.river.dto.ChatMessageDTO;
import com.river.entity.ChatMessage;
import com.river.vo.ChatMessageVO;

import java.util.List;

/**
 * 聊天消息Service
 */
public interface ChatMessageService extends IService<ChatMessage> {

    /**
     * 发送消息
     */
    ChatMessageVO sendMessage(ChatMessageDTO dto, Long senderId, Integer senderType);

    /**
     * 获取历史消息
     */
    List<ChatMessageVO> getHistoryMessages(Long roomId, Long lastMsgId, Integer pageSize, Long userId, Integer userType);

    /**
     * 撤回消息
     */
    boolean revokeMessage(Long msgId, Long userId, Integer userType);

    /**
     * 标记消息已读
     */
    boolean markAsRead(Long roomId, Long msgId, Long userId, Integer userType);

    /**
     * 获取消息详情
     */
    ChatMessageVO getMessageDetail(Long msgId);
}
