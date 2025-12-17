package com.river.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.river.entity.ChatConversation;
import com.river.vo.ChatConversationVO;

import java.util.List;

/**
 * 会话Service
 */
public interface ChatConversationService extends IService<ChatConversation> {

    /**
     * 获取会话列表
     */
    List<ChatConversationVO> getConversationList(Long userId, Integer userType);

    /**
     * 置顶/取消置顶会话
     */
    boolean toggleTop(Long conversationId, Long userId, Integer userType);

    /**
     * 设置免打扰
     */
    boolean toggleMute(Long conversationId, Long userId, Integer userType);

    /**
     * 删除会话
     */
    boolean deleteConversation(Long conversationId, Long userId, Integer userType);

    /**
     * 更新会话(新消息时调用)
     */
    void updateConversation(Long roomId, Long msgId, String content, Long senderId, Integer senderType);

    /**
     * 清空未读数
     */
    boolean clearUnread(Long roomId, Long userId, Integer userType);

    /**
     * 获取或创建会话
     */
    ChatConversation getOrCreateConversation(Long roomId, Long userId, Integer userType);
}
