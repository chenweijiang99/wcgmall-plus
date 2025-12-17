package com.river.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.river.dto.FriendRequestDTO;
import com.river.dto.HandleFriendRequestDTO;
import com.river.entity.ChatFriend;
import com.river.entity.ChatFriendRequest;
import com.river.entity.ChatRoom;
import com.river.enums.FriendRequestStatusEnum;
import com.river.exception.ServiceException;
import com.river.mapper.ChatFriendMapper;
import com.river.mapper.ChatFriendRequestMapper;
import com.river.service.ChatFriendService;
import com.river.service.ChatRoomService;
import com.river.vo.ChatFriendRequestVO;
import com.river.vo.ChatFriendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 好友Service实现
 */
@Service
@RequiredArgsConstructor
public class ChatFriendServiceImpl extends ServiceImpl<ChatFriendMapper, ChatFriend> implements ChatFriendService {

    private final ChatFriendRequestMapper requestMapper;
    private final ChatRoomService roomService;

    @Override
    public List<ChatFriendVO> getFriendList(Long userId, Integer userType) {
        return baseMapper.selectFriendListWithUserInfo(userId, userType);
    }

    @Override
    public boolean sendFriendRequest(FriendRequestDTO dto, Long userId, Integer userType) {
        Integer toUserType = dto.getToUserType() != null ? dto.getToUserType() : 1;

        // 不能添加自己
        if (dto.getToUserId().equals(userId) && toUserType.equals(userType)) {
            throw new ServiceException("不能添加自己为好友");
        }

        // 检查是否已经是好友
        if (isFriend(userId, userType, dto.getToUserId(), toUserType)) {
            throw new ServiceException("已经是好友了");
        }

        // 检查是否有待处理的申请
        ChatFriendRequest existRequest = requestMapper.selectOne(new LambdaQueryWrapper<ChatFriendRequest>()
                .eq(ChatFriendRequest::getFromUserId, userId)
                .eq(ChatFriendRequest::getFromUserType, userType)
                .eq(ChatFriendRequest::getToUserId, dto.getToUserId())
                .eq(ChatFriendRequest::getToUserType, toUserType)
                .eq(ChatFriendRequest::getStatus, FriendRequestStatusEnum.PENDING.getCode()));

        if (existRequest != null) {
            throw new ServiceException("已发送过好友申请,请等待对方处理");
        }

        // 检查对方是否已经向自己发送过申请
        ChatFriendRequest reverseRequest = requestMapper.selectOne(new LambdaQueryWrapper<ChatFriendRequest>()
                .eq(ChatFriendRequest::getFromUserId, dto.getToUserId())
                .eq(ChatFriendRequest::getFromUserType, toUserType)
                .eq(ChatFriendRequest::getToUserId, userId)
                .eq(ChatFriendRequest::getToUserType, userType)
                .eq(ChatFriendRequest::getStatus, FriendRequestStatusEnum.PENDING.getCode()));

        if (reverseRequest != null) {
            // 直接同意对方的申请
            HandleFriendRequestDTO handleDto = new HandleFriendRequestDTO();
            handleDto.setRequestId(reverseRequest.getId());
            handleDto.setAction(1);
            return handleFriendRequest(handleDto, userId, userType);
        }

        // 创建好友申请
        ChatFriendRequest request = ChatFriendRequest.builder()
                .fromUserId(userId)
                .fromUserType(userType)
                .toUserId(dto.getToUserId())
                .toUserType(toUserType)
                .message(dto.getMessage())
                .status(FriendRequestStatusEnum.PENDING.getCode())
                .build();

        return requestMapper.insert(request) > 0;
    }

    @Override
    public List<ChatFriendRequestVO> getReceivedRequestList(Long userId, Integer userType) {
        return requestMapper.selectReceivedRequestList(userId, userType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleFriendRequest(HandleFriendRequestDTO dto, Long userId, Integer userType) {
        ChatFriendRequest request = requestMapper.selectById(dto.getRequestId());
        if (request == null) {
            throw new ServiceException("申请不存在");
        }

        // 检查是否是接收者
        if (!request.getToUserId().equals(userId) || !request.getToUserType().equals(userType)) {
            throw new ServiceException("无权处理此申请");
        }

        // 检查状态
        if (!FriendRequestStatusEnum.PENDING.getCode().equals(request.getStatus())) {
            throw new ServiceException("该申请已处理");
        }

        // 更新申请状态
        request.setStatus(dto.getAction());
        request.setHandleTime(LocalDateTime.now());
        requestMapper.updateById(request);

        // 如果同意,创建好友关系
        if (dto.getAction().equals(FriendRequestStatusEnum.ACCEPTED.getCode())) {
            // 创建私聊聊天室
            ChatRoom room = roomService.getOrCreatePrivateRoom(
                    userId, userType,
                    request.getFromUserId(), request.getFromUserType());

            // 创建双向好友关系
            ChatFriend friend1 = ChatFriend.builder()
                    .userId(userId)
                    .userType(userType)
                    .friendId(request.getFromUserId())
                    .friendType(request.getFromUserType())
                    .roomId(room.getId())
                    .status(1)
                    .build();
            save(friend1);

            ChatFriend friend2 = ChatFriend.builder()
                    .userId(request.getFromUserId())
                    .userType(request.getFromUserType())
                    .friendId(userId)
                    .friendType(userType)
                    .roomId(room.getId())
                    .status(1)
                    .build();
            save(friend2);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFriend(Long friendId, Integer friendType, Long userId, Integer userType) {
        // 删除双向好友关系
        remove(new LambdaQueryWrapper<ChatFriend>()
                .eq(ChatFriend::getUserId, userId)
                .eq(ChatFriend::getUserType, userType)
                .eq(ChatFriend::getFriendId, friendId)
                .eq(ChatFriend::getFriendType, friendType));

        remove(new LambdaQueryWrapper<ChatFriend>()
                .eq(ChatFriend::getUserId, friendId)
                .eq(ChatFriend::getUserType, friendType)
                .eq(ChatFriend::getFriendId, userId)
                .eq(ChatFriend::getFriendType, userType));

        return true;
    }

    @Override
    public boolean updateRemark(Long friendId, Integer friendType, String remark, Long userId, Integer userType) {
        ChatFriend friend = getOne(new LambdaQueryWrapper<ChatFriend>()
                .eq(ChatFriend::getUserId, userId)
                .eq(ChatFriend::getUserType, userType)
                .eq(ChatFriend::getFriendId, friendId)
                .eq(ChatFriend::getFriendType, friendType));

        if (friend == null) {
            throw new ServiceException("好友关系不存在");
        }

        friend.setRemark(remark);
        return updateById(friend);
    }

    @Override
    public boolean isFriend(Long userId, Integer userType, Long friendId, Integer friendType) {
        return count(new LambdaQueryWrapper<ChatFriend>()
                .eq(ChatFriend::getUserId, userId)
                .eq(ChatFriend::getUserType, userType)
                .eq(ChatFriend::getFriendId, friendId)
                .eq(ChatFriend::getFriendType, friendType)
                .eq(ChatFriend::getStatus, 1)) > 0;
    }
}
