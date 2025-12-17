package com.river.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.river.dto.ChatMessageDTO;
import com.river.entity.ChatMessage;
import com.river.entity.ChatRoomMember;
import com.river.enums.MessageTypeEnum;
import com.river.exception.ServiceException;
import com.river.mapper.ChatMessageMapper;
import com.river.mapper.ChatRoomMemberMapper;
import com.river.service.ChatConversationService;
import com.river.service.ChatMessageService;
import com.river.service.ChatRoomService;
import com.river.vo.ChatMessageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天消息Service实现
 */
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    private final ChatRoomService roomService;
    private final ChatRoomMemberMapper memberMapper;
    @Lazy
    private final ChatConversationService conversationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageVO sendMessage(ChatMessageDTO dto, Long senderId, Integer senderType) {
        // 检查是否是聊天室成员
        if (!roomService.isMember(dto.getRoomId(), senderId, senderType)) {
            throw new ServiceException("您不是该聊天室成员");
        }

        // 检查是否被禁言
        ChatRoomMember member = memberMapper.selectOne(new LambdaQueryWrapper<ChatRoomMember>()
                .eq(ChatRoomMember::getRoomId, dto.getRoomId())
                .eq(ChatRoomMember::getUserId, senderId)
                .eq(ChatRoomMember::getUserType, senderType)
                .eq(ChatRoomMember::getStatus, 1));

        if (member != null && member.getMuted() == 1) {
            if (member.getMutedEndTime() == null || member.getMutedEndTime().isAfter(LocalDateTime.now())) {
                throw new ServiceException("您已被禁言");
            }
        }

        // 构建消息
        String atUserIds = null;
        if (dto.getAtUserIds() != null && !dto.getAtUserIds().isEmpty()) {
            atUserIds = dto.getAtUserIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }

        ChatMessage message = ChatMessage.builder()
                .roomId(dto.getRoomId())
                .senderId(senderId)
                .senderType(senderType)
                .msgType(dto.getMsgType())
                .content(dto.getContent())
                .mediaUrl(dto.getMediaUrl())
                .mediaDuration(dto.getMediaDuration())
                .mediaSize(dto.getMediaSize())
                .thumbnailUrl(dto.getThumbnailUrl())
                .replyMsgId(dto.getReplyMsgId())
                .atUserIds(atUserIds)
                .isRevoked(0)
                .status(1)
                .build();

        save(message);

        // 更新会话
        String contentSummary = getContentSummary(dto.getMsgType(), dto.getContent());
        conversationService.updateConversation(dto.getRoomId(), message.getId(), contentSummary, senderId, senderType);

        // 返回消息VO
        return baseMapper.selectMessageWithSenderInfo(message.getId());
    }

    @Override
    public List<ChatMessageVO> getHistoryMessages(Long roomId, Long lastMsgId, Integer pageSize, Long userId, Integer userType) {
        // 检查是否是聊天室成员
        if (!roomService.isMember(roomId, userId, userType)) {
            throw new ServiceException("您不是该聊天室成员");
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        List<ChatMessageVO> messages = baseMapper.selectMessageListWithSenderInfo(roomId, lastMsgId, pageSize);

        // 标记是否是自己发送的
        for (ChatMessageVO msg : messages) {
            msg.setIsSelf(msg.getSenderId().equals(userId) && msg.getSenderType().equals(userType));

            // 如果有回复消息,获取回复消息详情
            if (msg.getReplyMsg() != null && msg.getReplyMsg().getId() != null) {
                ChatMessageVO replyMsg = baseMapper.selectMessageWithSenderInfo(msg.getReplyMsg().getId());
                msg.setReplyMsg(replyMsg);
            }
        }

        // 反转列表,使消息按时间正序排列
        Collections.reverse(messages);

        return messages;
    }

    @Override
    public boolean revokeMessage(Long msgId, Long userId, Integer userType) {
        ChatMessage message = getById(msgId);
        if (message == null) {
            throw new ServiceException("消息不存在");
        }

        // 检查是否是发送者
        if (!message.getSenderId().equals(userId) || !message.getSenderType().equals(userType)) {
            throw new ServiceException("只能撤回自己发送的消息");
        }

        // 检查是否超过撤回时间(2分钟)
        if (message.getCreateTime().plusMinutes(2).isBefore(LocalDateTime.now())) {
            throw new ServiceException("消息发送超过2分钟,无法撤回");
        }

        message.setIsRevoked(1);
        message.setRevokeTime(LocalDateTime.now());
        return updateById(message);
    }

    @Override
    public boolean markAsRead(Long roomId, Long msgId, Long userId, Integer userType) {
        ChatRoomMember member = memberMapper.selectOne(new LambdaQueryWrapper<ChatRoomMember>()
                .eq(ChatRoomMember::getRoomId, roomId)
                .eq(ChatRoomMember::getUserId, userId)
                .eq(ChatRoomMember::getUserType, userType)
                .eq(ChatRoomMember::getStatus, 1));

        if (member == null) {
            return false;
        }

        member.setLastReadMsgId(msgId);
        member.setLastReadTime(LocalDateTime.now());
        memberMapper.updateById(member);

        // 清空未读数
        conversationService.clearUnread(roomId, userId, userType);

        return true;
    }

    @Override
    public ChatMessageVO getMessageDetail(Long msgId) {
        return baseMapper.selectMessageWithSenderInfo(msgId);
    }

    private String getContentSummary(Integer msgType, String content) {
        MessageTypeEnum type = MessageTypeEnum.getByCode(msgType);
        return switch (type) {
            case TEXT -> content != null && content.length() > 50 ? content.substring(0, 50) + "..." : content;
            case IMAGE -> "[图片]";
            case VIDEO -> "[视频]";
            case VOICE -> "[语音]";
            case EMOJI -> "[表情]";
            case SYSTEM -> "[系统消息]";
        };
    }
}
