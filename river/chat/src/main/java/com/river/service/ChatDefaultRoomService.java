package com.river.service;

/**
 * 默认群聊服务
 */
public interface ChatDefaultRoomService {

    /**
     * 默认群聊ID
     */
    Long DEFAULT_ROOM_ID = 1L;

    /**
     * 将用户加入默认群聊
     * @param userId 用户ID
     * @param userType 用户类型: 1-普通用户 2-管理员
     */
    void joinDefaultRoom(Long userId, Integer userType);

    /**
     * 检查用户是否已加入默认群聊
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 是否已加入
     */
    boolean hasJoinedDefaultRoom(Long userId, Integer userType);
}
