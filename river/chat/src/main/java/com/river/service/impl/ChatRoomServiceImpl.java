package com.river.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.river.dto.CreateRoomDTO;
import com.river.entity.ChatRoom;
import com.river.entity.ChatRoomMember;
import com.river.enums.ChatRoomTypeEnum;
import com.river.enums.MemberRoleEnum;
import com.river.exception.ServiceException;
import com.river.mapper.ChatRoomMapper;
import com.river.mapper.ChatRoomMemberMapper;
import com.river.service.ChatRoomService;
import com.river.vo.ChatMemberVO;
import com.river.vo.ChatRoomVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天室Service实现
 */
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl extends ServiceImpl<ChatRoomMapper, ChatRoom> implements ChatRoomService {

    private final ChatRoomMemberMapper memberMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatRoom createGroupRoom(CreateRoomDTO dto, Long userId, Integer userType) {
        // 创建聊天室
        ChatRoom room = ChatRoom.builder()
                .name(dto.getName())
                .avatar(dto.getAvatar())
                .type(ChatRoomTypeEnum.GROUP.getCode())
                .ownerId(userId)
                .ownerType(userType)
                .maxMemberCount(500)
                .status(1)
                .build();
        save(room);

        // 添加群主为成员
        ChatRoomMember ownerMember = ChatRoomMember.builder()
                .roomId(room.getId())
                .userId(userId)
                .userType(userType)
                .role(MemberRoleEnum.OWNER.getCode())
                .muted(0)
                .lastReadMsgId(0L)
                .joinTime(LocalDateTime.now())
                .status(1)
                .build();
        memberMapper.insert(ownerMember);

        // 添加其他成员
        if (dto.getMemberIds() != null && !dto.getMemberIds().isEmpty()) {
            for (int i = 0; i < dto.getMemberIds().size(); i++) {
                Long memberId = dto.getMemberIds().get(i);
                Integer memberType = dto.getMemberTypes() != null && i < dto.getMemberTypes().size()
                        ? dto.getMemberTypes().get(i) : 1;

                // 跳过群主自己
                if (memberId.equals(userId) && memberType.equals(userType)) {
                    continue;
                }

                ChatRoomMember member = ChatRoomMember.builder()
                        .roomId(room.getId())
                        .userId(memberId)
                        .userType(memberType)
                        .role(MemberRoleEnum.MEMBER.getCode())
                        .muted(0)
                        .lastReadMsgId(0L)
                        .joinTime(LocalDateTime.now())
                        .status(1)
                        .build();
                memberMapper.insert(member);
            }
        }

        return room;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatRoom getOrCreatePrivateRoom(Long userId, Integer userType, Long friendId, Integer friendType) {
        // 查找已存在的私聊聊天室
        List<Long> userRoomIds = memberMapper.selectRoomIdsByUserId(userId, userType);
        List<Long> friendRoomIds = memberMapper.selectRoomIdsByUserId(friendId, friendType);

        // 找出共同的私聊聊天室
        for (Long roomId : userRoomIds) {
            if (friendRoomIds.contains(roomId)) {
                ChatRoom room = getById(roomId);
                if (room != null && ChatRoomTypeEnum.PRIVATE.getCode().equals(room.getType())) {
                    return room;
                }
            }
        }

        // 创建新的私聊聊天室
        ChatRoom room = ChatRoom.builder()
                .type(ChatRoomTypeEnum.PRIVATE.getCode())
                .maxMemberCount(2)
                .status(1)
                .build();
        save(room);

        // 添加两个成员
        ChatRoomMember member1 = ChatRoomMember.builder()
                .roomId(room.getId())
                .userId(userId)
                .userType(userType)
                .role(MemberRoleEnum.MEMBER.getCode())
                .muted(0)
                .lastReadMsgId(0L)
                .joinTime(LocalDateTime.now())
                .status(1)
                .build();
        memberMapper.insert(member1);

        ChatRoomMember member2 = ChatRoomMember.builder()
                .roomId(room.getId())
                .userId(friendId)
                .userType(friendType)
                .role(MemberRoleEnum.MEMBER.getCode())
                .muted(0)
                .lastReadMsgId(0L)
                .joinTime(LocalDateTime.now())
                .status(1)
                .build();
        memberMapper.insert(member2);

        return room;
    }

    @Override
    public ChatRoomVO getRoomDetail(Long roomId, Long userId, Integer userType) {
        ChatRoom room = getById(roomId);
        if (room == null) {
            throw new ServiceException("聊天室不存在");
        }

        // 检查是否是成员
        if (!isMember(roomId, userId, userType)) {
            throw new ServiceException("您不是该聊天室成员");
        }

        // 获取成员列表
        List<ChatMemberVO> members = memberMapper.selectMemberListWithUserInfo(roomId);

        // 获取当前用户角色
        ChatRoomMember myMember = getMember(roomId, userId, userType);
        Integer myRole = myMember != null ? myMember.getRole() : 0;

        return ChatRoomVO.builder()
                .id(room.getId())
                .name(room.getName())
                .avatar(room.getAvatar())
                .type(room.getType())
                .ownerId(room.getOwnerId())
                .ownerType(room.getOwnerType())
                .announcement(room.getAnnouncement())
                .memberCount(members.size())
                .maxMemberCount(room.getMaxMemberCount())
                .myRole(myRole)
                .members(members)
                .createTime(room.getCreateTime())
                .build();
    }

    @Override
    public boolean updateRoom(ChatRoom room, Long userId, Integer userType) {
        ChatRoom existRoom = getById(room.getId());
        if (existRoom == null) {
            throw new ServiceException("聊天室不存在");
        }

        // 检查权限(只有群主和管理员可以修改)
        ChatRoomMember member = getMember(room.getId(), userId, userType);
        if (member == null || member.getRole() < MemberRoleEnum.ADMIN.getCode()) {
            throw new ServiceException("没有权限修改聊天室信息");
        }

        return updateById(room);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean inviteMembers(Long roomId, List<Long> memberIds, List<Integer> memberTypes, Long userId, Integer userType) {
        ChatRoom room = getById(roomId);
        if (room == null || !ChatRoomTypeEnum.GROUP.getCode().equals(room.getType())) {
            throw new ServiceException("群聊不存在");
        }

        // 检查是否是成员
        if (!isMember(roomId, userId, userType)) {
            throw new ServiceException("您不是该群成员");
        }

        // 检查成员数量限制
        List<ChatMemberVO> currentMembers = memberMapper.selectMemberListWithUserInfo(roomId);
        if (currentMembers.size() + memberIds.size() > room.getMaxMemberCount()) {
            throw new ServiceException("超出群成员数量限制");
        }

        // 添加成员
        for (int i = 0; i < memberIds.size(); i++) {
            Long memberId = memberIds.get(i);
            Integer memberType = memberTypes != null && i < memberTypes.size() ? memberTypes.get(i) : 1;

            // 检查是否已是成员
            if (isMember(roomId, memberId, memberType)) {
                continue;
            }

            ChatRoomMember member = ChatRoomMember.builder()
                    .roomId(roomId)
                    .userId(memberId)
                    .userType(memberType)
                    .role(MemberRoleEnum.MEMBER.getCode())
                    .muted(0)
                    .lastReadMsgId(0L)
                    .joinTime(LocalDateTime.now())
                    .status(1)
                    .build();
            memberMapper.insert(member);
        }

        return true;
    }

    @Override
    public boolean kickMember(Long roomId, Long memberId, Integer memberType, Long userId, Integer userType) {
        ChatRoom room = getById(roomId);
        if (room == null) {
            throw new ServiceException("聊天室不存在");
        }

        // 检查操作者权限
        ChatRoomMember operator = getMember(roomId, userId, userType);
        if (operator == null || operator.getRole() < MemberRoleEnum.ADMIN.getCode()) {
            throw new ServiceException("没有权限踢出成员");
        }

        // 检查被踢者
        ChatRoomMember target = getMember(roomId, memberId, memberType);
        if (target == null) {
            throw new ServiceException("该用户不是群成员");
        }

        // 不能踢出群主
        if (target.getRole().equals(MemberRoleEnum.OWNER.getCode())) {
            throw new ServiceException("不能踢出群主");
        }

        // 管理员不能踢出管理员
        if (operator.getRole().equals(MemberRoleEnum.ADMIN.getCode())
                && target.getRole().equals(MemberRoleEnum.ADMIN.getCode())) {
            throw new ServiceException("管理员不能踢出其他管理员");
        }

        target.setStatus(0);
        return memberMapper.updateById(target) > 0;
    }

    @Override
    public boolean quitRoom(Long roomId, Long userId, Integer userType) {
        ChatRoom room = getById(roomId);
        if (room == null) {
            throw new ServiceException("聊天室不存在");
        }

        ChatRoomMember member = getMember(roomId, userId, userType);
        if (member == null) {
            throw new ServiceException("您不是该群成员");
        }

        // 群主不能直接退出,需要先转让或解散
        if (member.getRole().equals(MemberRoleEnum.OWNER.getCode())) {
            throw new ServiceException("群主不能退出群聊,请先转让群主或解散群聊");
        }

        member.setStatus(0);
        return memberMapper.updateById(member) > 0;
    }

    @Override
    public List<ChatMemberVO> getRoomMembers(Long roomId) {
        return memberMapper.selectMemberListWithUserInfo(roomId);
    }

    @Override
    public boolean isMember(Long roomId, Long userId, Integer userType) {
        return getMember(roomId, userId, userType) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dissolveRoom(Long roomId, Long userId, Integer userType) {
        ChatRoom room = getById(roomId);
        if (room == null) {
            throw new ServiceException("聊天室不存在");
        }

        // 只有群主可以解散
        if (!room.getOwnerId().equals(userId) || !room.getOwnerType().equals(userType)) {
            throw new ServiceException("只有群主可以解散群聊");
        }

        // 更新聊天室状态
        room.setStatus(0);
        updateById(room);

        // 更新所有成员状态
        ChatRoomMember updateMember = new ChatRoomMember();
        updateMember.setStatus(0);
        memberMapper.update(updateMember, new LambdaQueryWrapper<ChatRoomMember>()
                .eq(ChatRoomMember::getRoomId, roomId));

        return true;
    }

    private ChatRoomMember getMember(Long roomId, Long userId, Integer userType) {
        return memberMapper.selectOne(new LambdaQueryWrapper<ChatRoomMember>()
                .eq(ChatRoomMember::getRoomId, roomId)
                .eq(ChatRoomMember::getUserId, userId)
                .eq(ChatRoomMember::getUserType, userType)
                .eq(ChatRoomMember::getStatus, 1));
    }
}
