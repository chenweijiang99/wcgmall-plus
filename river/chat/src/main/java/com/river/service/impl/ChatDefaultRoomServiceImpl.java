package com.river.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.river.entity.ChatConversation;
import com.river.entity.ChatRoom;
import com.river.entity.ChatRoomMember;
import com.river.enums.MemberRoleEnum;
import com.river.mapper.ChatConversationMapper;
import com.river.mapper.ChatRoomMapper;
import com.river.mapper.ChatRoomMemberMapper;
import com.river.service.ChatDefaultRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 默认群聊服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatDefaultRoomServiceImpl implements ChatDefaultRoomService {

    private final ChatRoomMapper roomMapper;
    private final ChatRoomMemberMapper memberMapper;
    private final ChatConversationMapper conversationMapper;

    @Override
    public void joinDefaultRoom(Long userId, Integer userType) {
        // 检查默认群聊是否存在
        ChatRoom room = roomMapper.selectById(DEFAULT_ROOM_ID);
        if (room == null || room.getStatus() != 1) {
            log.warn("默认群聊不存在或已禁用，无法加入");
            return;
        }

        // 检查是否已加入
        if (hasJoinedDefaultRoom(userId, userType)) {
            log.info("用户 {}:{} 已加入默认群聊", userType, userId);
            return;
        }

        // 加入群聊
        ChatRoomMember member = ChatRoomMember.builder()
                .roomId(DEFAULT_ROOM_ID)
                .userId(userId)
                .userType(userType)
                .role(MemberRoleEnum.MEMBER.getCode())
                .muted(0)
                .lastReadMsgId(0L)
                .joinTime(LocalDateTime.now())
                .status(1)
                .build();

        memberMapper.insert(member);

        // 创建会话记录（这样用户才能在会话列表中看到群聊）
        ChatConversation conversation = ChatConversation.builder()
                .userId(userId)
                .userType(userType)
                .roomId(DEFAULT_ROOM_ID)
                .lastMsgContent("欢迎加入" + room.getName())
                .lastMsgTime(LocalDateTime.now())
                .unreadCount(0)
                .isTop(0)
                .isMuted(0)
                .isDeleted(0)
                .build();
        conversationMapper.insert(conversation);

        log.info("用户 {}:{} 成功加入默认群聊", userType, userId);
    }

    @Override
    public boolean hasJoinedDefaultRoom(Long userId, Integer userType) {
        return memberMapper.selectCount(new LambdaQueryWrapper<ChatRoomMember>()
                .eq(ChatRoomMember::getRoomId, DEFAULT_ROOM_ID)
                .eq(ChatRoomMember::getUserId, userId)
                .eq(ChatRoomMember::getUserType, userType)
                .eq(ChatRoomMember::getStatus, 1)) > 0;
    }
}
