package com.river.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.river.entity.ChatConversation;
import com.river.vo.ChatConversationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户会话Mapper
 */
@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {

    /**
     * 获取用户会话列表
     */
    List<ChatConversationVO> selectConversationList(
            @Param("userId") Long userId,
            @Param("userType") Integer userType);
}
