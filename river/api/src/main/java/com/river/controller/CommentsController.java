package com.river.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageInfo;
import com.river.common.Result;
import com.river.entity.Comments;
import com.river.service.CommentsService;
import com.river.vo.comments.CommentsVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController("userCommentsController")
@RequestMapping("/user/comments")
@Tag(name = "用户评论相关接口")
@Slf4j
@RequiredArgsConstructor
public class CommentsController {
    private final CommentsService commentsService;

    @GetMapping("/selectTree")
    @Operation(summary = "查询评论树")
    public Result<IPage<CommentsVo>> selectTree(Comments comments) {
        return Result.success(commentsService.selectTree(comments));
    }

    @GetMapping("/selectCount/{fid}/{module}")
    @Operation(summary = "查询评论数量")
    public Result<Long> selectCount(@PathVariable Integer fid, @PathVariable String module) {
        return Result.success(commentsService.selectCount(fid, module));
    }

    @PostMapping("/addComments")
    @Operation(summary = "发表评论")
    public Result<Object> addComments(@RequestBody Comments comments){
        return Result.success(commentsService.addComments(comments));
    }

    @DeleteMapping("/deleteComments")
    @Operation(summary = "删除评论")
    public Result<Object> deleteComments(Long id){
        return Result.success(commentsService.removeById(id));
    }
}
