package com.river.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.river.entity.ChatConversation;
import com.river.entity.ChatRoom;
import com.river.entity.ChatRoomMember;
import com.river.enums.ChatRoomTypeEnum;
import com.river.exception.ServiceException;
import com.river.mapper.ChatConversationMapper;
import com.river.mapper.ChatRoomMemberMapper;
import com.river.service.ChatConversationService;
import com.river.service.ChatRoomService;
import com.river.vo.ChatConversationVO;
import com.river.vo.ChatMemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话Service实现
 */
@Service
@RequiredArgsConstructor
public class ChatConversationServiceImpl extends ServiceImpl<ChatConversationMapper, ChatConversation> implements ChatConversationService {

    private final ChatRoomService roomService;
    private final ChatRoomMemberMapper memberMapper;

    @Override
    public List<ChatConversationVO> getConversationList(Long userId, Integer userType) {
        List<ChatConversationVO> conversations = baseMapper.selectConversationList(userId, userType);

        // 处理私聊会话的名称和头像
        for (ChatConversationVO conv : conversations) {
            if (ChatRoomTypeEnum.PRIVATE.getCode().equals(conv.getRoomType())) {
                // 获取对方信息
                List<ChatMemberVO> members = roomService.getRoomMembers(conv.getRoomId());
                for (ChatMemberVO member : members) {
                    if (!member.getUserId().equals(userId) || !member.getUserType().equals(userType)) {
                        conv.setName(member.getNickname());
                        conv.setAvatar(member.getAvatar());
                        conv.setOnline(member.getOnline());
                        break;
                    }
                }
            }
        }

        return conversations;
    }

    @Override
    public boolean toggleTop(Long conversationId, Long userId, Integer userType) {
        ChatConversation conversation = getById(conversationId);
        if (conversation == null) {
            throw new ServiceException("会话不存在");
        }

        // 检查是否是自己的会话
        if (!conversation.getUserId().equals(userId) || !conversation.getUserType().equals(userType)) {
            throw new ServiceException("无权操作此会话");
        }

        conversation.setIsTop(conversation.getIsTop() == 0 ? 1 : 0);
        return updateById(conversation);
    }

    @Override
    public boolean toggleMute(Long conversationId, Long userId, Integer userType) {
        ChatConversation conversation = getById(conversationId);
        if (conversation == null) {
            throw new ServiceException("会话不存在");
        }

        if (!conversation.getUserId().equals(userId) || !conversation.getUserType().equals(userType)) {
            throw new ServiceException("无权操作此会话");
        }

        conversation.setIsMuted(conversation.getIsMuted() == 0 ? 1 : 0);
        return updateById(conversation);
    }

    @Override
    public boolean deleteConversation(Long conversationId, Long userId, Integer userType) {
        ChatConversation conversation = getById(conversationId);
        if (conversation == null) {
            throw new ServiceException("会话不存在");
        }

        if (!conversation.getUserId().equals(userId) || !conversation.getUserType().equals(userType)) {
            throw new ServiceException("无权操作此会话");
        }

        conversation.setIsDeleted(1);
        return updateById(conversation);
    }

    @Override
    public void updateConversation(Long roomId, Long msgId, String content, Long senderId, Integer senderType) {
        // 获取聊天室所有成员
        List<ChatMemberVO> members = roomService.getRoomMembers(roomId);

        for (ChatMemberVO member : members) {
            ChatConversation conversation = getOrCreateConversation(roomId, member.getUserId(), member.getUserType());

            conversation.setLastMsgId(msgId);
            conversation.setLastMsgContent(content);
            conversation.setLastMsgTime(LocalDateTime.now());
            conversation.setIsDeleted(0);

            // 如果不是发送者,增加未读数
            if (!member.getUserId().equals(senderId) || !member.getUserType().equals(senderType)) {
                conversation.setUnreadCount(conversation.getUnreadCount() + 1);
            }

            updateById(conversation);
        }
    }

    @Override
    public boolean clearUnread(Long roomId, Long userId, Integer userType) {
        ChatConversation conversation = getOne(new LambdaQueryWrapper<ChatConversation>()
                .eq(ChatConversation::getRoomId, roomId)
                .eq(ChatConversation::getUserId, userId)
                .eq(ChatConversation::getUserType, userType));

        if (conversation != null) {
            conversation.setUnreadCount(0);
            return updateById(conversation);
        }
        return false;
    }

    @Override
    public ChatConversation getOrCreateConversation(Long roomId, Long userId, Integer userType) {
        ChatConversation conversation = getOne(new LambdaQueryWrapper<ChatConversation>()
                .eq(ChatConversation::getRoomId, roomId)
                .eq(ChatConversation::getUserId, userId)
                .eq(ChatConversation::getUserType, userType));

        if (conversation == null) {
            conversation = ChatConversation.builder()
                    .userId(userId)
                    .userType(userType)
                    .roomId(roomId)
                    .unreadCount(0)
                    .isTop(0)
                    .isMuted(0)
                    .isDeleted(0)
                    .build();
            save(conversation);
        }

        return conversation;
    }
}
