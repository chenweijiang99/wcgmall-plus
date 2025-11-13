package com.river.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.river.entity.Comments;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.river.vo.comments.CommentsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  Mapper接口
 */
@Mapper
public interface CommentsMapper extends BaseMapper<Comments> {
    IPage<Comments> selectCommentsWithUser(Page<Comments> page, @Param("comments") Comments comments);

    List<CommentsVo> selectRoot(Comments comments);

    List<CommentsVo> selectByRootId(Long rootId);

    int addComments(Comments comments);
}