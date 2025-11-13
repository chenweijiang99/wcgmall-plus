package com.river.service.impl;

import java.util.List;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.river.dto.BlogPageDto;
import com.river.exception.ServiceException;
import org.springframework.stereotype.Service;
import com.river.mapper.BlogMapper;
import com.river.entity.Blog;
import com.river.service.BlogService;
import com.river.utils.PageUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;

/**
 * 服务实现类
 */
@Service
@RequiredArgsConstructor
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    /**
     * 查询分页列表
     */
    @Override
    public IPage<Blog> selectPage(Blog blog) {
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.like(blog.getAuthor() != null, Blog::getAuthor, blog.getAuthor());
        wrapper.like(blog.getTitle() != null, Blog::getTitle, blog.getTitle());
        wrapper.eq(blog.getStatus() != null, Blog::getStatus, blog.getStatus());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询列表
     */
    @Override
    public List<Blog> selectList(Blog blog) {
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(blog.getUserId() != null, Blog::getUserId, blog.getUserId());
        wrapper.like(blog.getTitle() != null, Blog::getTitle, blog.getTitle());
        wrapper.eq(blog.getStatus() != null, Blog::getStatus, blog.getStatus());
        return list(wrapper);
    }

    /**
     * 新增
     */
    @Override
    public boolean insert(Blog blog) {
        blog.setUserId(StpUtil.getLoginIdAsLong());
        return save(blog);
    }

    /**
     * 修改
     */
    @Override
    public boolean update(Blog blog) {
        return updateById(blog);
    }

    /**
     * 批量删除
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }

    /**
     * 用户获取最新博客列表
     */
    @Override
    public List<Blog> userGetNewBlogList() {
        return baseMapper.userGetNewBlogList();
    }

    @Override
    public IPage<Blog> userGetListPage(BlogPageDto blogPageDto) {
        Page<BlogPageDto> page = PageUtil.getPage();
        return baseMapper.userGetListPage(page, blogPageDto);
    }

    @Override
    public List<Blog> getHotBlogWithComment() {
        return baseMapper.getHotBlogWithComment();
    }

    @Override
    public List<Blog> userGetListByUserId() {
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blog::getUserId, StpUtil.getLoginIdAsLong());
        return baseMapper.selectList(wrapper);
    }

    @Override
    public boolean userDeleteBlog(Long id) {
        Blog blog = baseMapper.selectById(id);
        if(blog == null){
            throw new ServiceException("博客不存在");
        }
        if(blog.getUserId() != StpUtil.getLoginIdAsLong()){
            throw new ServiceException("您没有权限删除该博客");
        }
        return baseMapper.deleteById(id) > 0;
    }
}
