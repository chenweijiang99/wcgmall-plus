package com.river.service;

import com.river.entity.Comments;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.vo.comments.CommentsVo;

import java.util.List;

/**
 *  服务接口
 */
public interface CommentsService extends IService<Comments> {
    /**
     * 查询分页列表
     */
    IPage<Comments> selectPage(Comments comments);

    /**
     * 查询列表
     */
    List<Comments> selectList(Comments comments);

    /**
     * 新增
     */
    boolean insert(Comments comments);

    /**
     * 修改
     */
    boolean update(Comments comments);

    /**
     * 批量删除
     */
    boolean deleteByIds(List<Long> ids);

    IPage<CommentsVo> selectTree(Comments comments);

    Long selectCount(Integer fid, String module);

    boolean addComments(Comments comments);
}
