package com.river.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.river.entity.ChatFriend;
import com.river.vo.ChatFriendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 好友关系Mapper
 */
@Mapper
public interface ChatFriendMapper extends BaseMapper<ChatFriend> {

    /**
     * 获取好友列表(带用户信息)
     */
    List<ChatFriendVO> selectFriendListWithUserInfo(
            @Param("userId") Long userId,
            @Param("userType") Integer userType);
}
