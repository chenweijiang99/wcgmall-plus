package com.river.service;

import com.river.dto.BlogPageDto;
import com.river.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

/**
 *  服务接口
 */
public interface BlogService extends IService<Blog> {
    /**
     * 查询分页列表
     */
    IPage<Blog> selectPage(Blog blog);

    /**
     * 查询列表
     */
    List<Blog> selectList(Blog blog);

    /**
     * 新增
     */
    boolean insert(Blog blog);

    /**
     * 修改
     */
    boolean update(Blog blog);

    /**
     * 批量删除
     */
    boolean deleteByIds(List<Long> ids);
    /**
     * 用户获取最新博客列表
     */
    List<Blog> userGetNewBlogList();

    IPage<Blog> userGetListPage(BlogPageDto blogPageDto);

    List<Blog> getHotBlogWithComment();

    List<Blog> userGetListByUserId();

    boolean userDeleteBlog(Long id);
}
