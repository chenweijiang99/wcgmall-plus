package com.river.controller.blog;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.river.annotation.OperationLogger;
import org.springframework.web.bind.annotation.*;
import com.river.entity.Blog;
import com.river.service.BlogService;
import com.river.common.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
/**
 *  博客管理控制器
 */
@RestController
@RequestMapping("/sys/blog")
@RequiredArgsConstructor
@Tag(name = "博客管理")
public class BlogController {

    private final BlogService blogService;

    @GetMapping("/list")
    @Operation(summary = "获取博客列表")
    public Result<IPage<Blog>> list(Blog blog) {
        return Result.success(blogService.selectPage(blog));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取博客详情")
    public Result<Blog> getInfo(@PathVariable("id") Long id) {
        return Result.success(blogService.getById(id));
    }

    @PostMapping("/add")
    @Operation(summary = "添加博客")
    @OperationLogger("添加博客")
    @SaCheckPermission("sys:blog:add")
    public Result<Object> add(@RequestBody Blog blog) {
        return Result.success(blogService.insert(blog));
    }

    @PutMapping("/update")
    @Operation(summary = "修改博客")
    @OperationLogger("修改博客")
    @SaCheckPermission("sys:blog:update")
    public Result<Object> edit(@RequestBody Blog blog) {
        return Result.success(blogService.update(blog));
    }

    @DeleteMapping("/delete/{ids}")
    @Operation(summary = "删除博客")
    @OperationLogger("删除博客")
    @SaCheckPermission("sys:blog:delete")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(blogService.deleteByIds(ids));
    }

    @PutMapping("/update/status")
    @Operation(summary = "修改博客状态")
    @OperationLogger("修改博客状态")
    @SaCheckPermission("sys:blog:status")
    public Result<Object> updateStatus(@RequestBody Blog blog) {
        return Result.success(blogService.updateById(blog));
    }
}
