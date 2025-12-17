package com.river.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.river.entity.ChatRoomMember;
import com.river.vo.ChatMemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天室成员Mapper
 */
@Mapper
public interface ChatRoomMemberMapper extends BaseMapper<ChatRoomMember> {

    /**
     * 获取聊天室成员列表(带用户信息)
     */
    List<ChatMemberVO> selectMemberListWithUserInfo(@Param("roomId") Long roomId);

    /**
     * 获取用户所在的所有聊天室ID
     */
    List<Long> selectRoomIdsByUserId(@Param("userId") Long userId, @Param("userType") Integer userType);
}
