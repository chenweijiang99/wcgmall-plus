package com.river.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.river.dto.BlogPageDto;
import com.river.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 *  Mapper接口
 */
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {
    /**
     * 获取最新博客列表
     */
    List<Blog> userGetNewBlogList();

    IPage<Blog> userGetListPage(Page<BlogPageDto> page, BlogPageDto blogPageDto);

    List<Blog> getHotBlogWithComment();
}