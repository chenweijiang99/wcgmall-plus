package com.river.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.river.entity.ChatFriendRequest;
import com.river.vo.ChatFriendRequestVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 好友申请Mapper
 */
@Mapper
public interface ChatFriendRequestMapper extends BaseMapper<ChatFriendRequest> {

    /**
     * 获取收到的好友申请列表
     */
    List<ChatFriendRequestVO> selectReceivedRequestList(
            @Param("userId") Long userId,
            @Param("userType") Integer userType);
}
