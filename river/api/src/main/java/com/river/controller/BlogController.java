package com.river.controller;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.annotation.OperationLogger;
import com.river.common.Result;
import com.river.dto.BlogPageDto;
import com.river.entity.Blog;
import com.river.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userBlogController")
@RequestMapping("/user/blog")
@Tag(name = "用户博客相关接口")
@Slf4j
@RequiredArgsConstructor
public class BlogController {
    private final BlogService blogService;

    @GetMapping("/page")
    @Operation(summary = "分页获取博客列表")
    public Result<IPage<Blog>> userGetListPage(BlogPageDto blogPageDto){
        return Result.success(blogService.userGetListPage(blogPageDto));
    }

    @GetMapping("/getHotBlog")
    @Operation(summary = "获取热门博客")
    public Result<List<Blog>> getHotBlog(){
        return Result.success(blogService.getHotBlogWithComment());
    }

    @GetMapping("/userBlog")
    @Operation(summary = "获取用户博客")
    public Result<List<Blog>> userBlog(){
        return Result.success(blogService.userGetListByUserId());
    }

    @PostMapping("/add")
    @Operation(summary = "添加博客")
    public Result<Object> add(@RequestBody Blog blog) {
        return Result.success(blogService.insert(blog));
    }

    @PutMapping("/update")
    @Operation(summary = "修改博客")
    public Result<Object> edit(@RequestBody Blog blog) {
        return Result.success(blogService.update(blog));
    }

    @DeleteMapping
    @Operation(summary = "删除博客")
    public Result<Object> deleteBlog(Long id){
        return Result.success(blogService.userDeleteBlog(id));
    }
}
