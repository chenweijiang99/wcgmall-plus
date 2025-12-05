package com.river.controller.comments;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.river.annotation.OperationLogger;
import org.springframework.web.bind.annotation.*;
import com.river.entity.Comments;
import com.river.service.CommentsService;
import com.river.common.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
/**
 *  评论管理控制器
 */
@RestController
@RequestMapping("/sys/comments")
@RequiredArgsConstructor
@Tag(name = "评论管理")
public class CommentsController {

    private final CommentsService commentsService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Result<IPage<Comments>> list(Comments comments) {
        return Result.success(commentsService.selectPage(comments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取详情")
    public Result<Comments> getInfo(@PathVariable("id") Long id) {
        return Result.success(commentsService.getById(id));
    }

    @PostMapping("/add")
    @Operation(summary = "添加")
    @OperationLogger("添加评论")
    @SaCheckPermission("sys:comments:add")
    public Result<Object> add(@RequestBody Comments comments) {
        return Result.success(commentsService.insert(comments));
    }

    @PutMapping("/update")
    @Operation(summary = "修改")
    @OperationLogger("修改评论")
    @SaCheckPermission("sys:comments:update")
    public Result<Object> edit(@RequestBody Comments comments) {
        return Result.success(commentsService.update(comments));
    }

    @DeleteMapping("/delete/{ids}")
    @Operation(summary = "删除")
    @OperationLogger("删除评论")
    @SaCheckPermission("sys:comments:delete")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(commentsService.deleteByIds(ids));
    }
}
