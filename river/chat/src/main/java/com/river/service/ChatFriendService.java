package com.river.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.river.dto.FriendRequestDTO;
import com.river.dto.HandleFriendRequestDTO;
import com.river.entity.ChatFriend;
import com.river.vo.ChatFriendRequestVO;
import com.river.vo.ChatFriendVO;

import java.util.List;

/**
 * 好友Service
 */
public interface ChatFriendService extends IService<ChatFriend> {

    /**
     * 获取好友列表
     */
    List<ChatFriendVO> getFriendList(Long userId, Integer userType);

    /**
     * 发送好友申请
     */
    boolean sendFriendRequest(FriendRequestDTO dto, Long userId, Integer userType);

    /**
     * 获取收到的好友申请列表
     */
    List<ChatFriendRequestVO> getReceivedRequestList(Long userId, Integer userType);

    /**
     * 处理好友申请
     */
    boolean handleFriendRequest(HandleFriendRequestDTO dto, Long userId, Integer userType);

    /**
     * 删除好友
     */
    boolean deleteFriend(Long friendId, Integer friendType, Long userId, Integer userType);

    /**
     * 修改好友备注
     */
    boolean updateRemark(Long friendId, Integer friendType, String remark, Long userId, Integer userType);

    /**
     * 检查是否是好友
     */
    boolean isFriend(Long userId, Integer userType, Long friendId, Integer friendType);
}
