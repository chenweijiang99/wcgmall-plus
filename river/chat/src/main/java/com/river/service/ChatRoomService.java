package com.river.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.river.dto.CreateRoomDTO;
import com.river.entity.ChatRoom;
import com.river.vo.ChatMemberVO;
import com.river.vo.ChatRoomVO;

import java.util.List;

/**
 * 聊天室Service
 */
public interface ChatRoomService extends IService<ChatRoom> {

    /**
     * 创建群聊
     */
    ChatRoom createGroupRoom(CreateRoomDTO dto, Long userId, Integer userType);

    /**
     * 创建或获取私聊聊天室
     */
    ChatRoom getOrCreatePrivateRoom(Long userId, Integer userType, Long friendId, Integer friendType);

    /**
     * 获取聊天室详情
     */
    ChatRoomVO getRoomDetail(Long roomId, Long userId, Integer userType);

    /**
     * 更新聊天室信息
     */
    boolean updateRoom(ChatRoom room, Long userId, Integer userType);

    /**
     * 邀请成员加入群聊
     */
    boolean inviteMembers(Long roomId, List<Long> memberIds, List<Integer> memberTypes, Long userId, Integer userType);

    /**
     * 踢出成员
     */
    boolean kickMember(Long roomId, Long memberId, Integer memberType, Long userId, Integer userType);

    /**
     * 退出群聊
     */
    boolean quitRoom(Long roomId, Long userId, Integer userType);

    /**
     * 获取聊天室成员列表
     */
    List<ChatMemberVO> getRoomMembers(Long roomId);

    /**
     * 检查用户是否是聊天室成员
     */
    boolean isMember(Long roomId, Long userId, Integer userType);

    /**
     * 解散群聊
     */
    boolean dissolveRoom(Long roomId, Long userId, Integer userType);
}
