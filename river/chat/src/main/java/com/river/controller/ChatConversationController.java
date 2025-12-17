package com.river.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.river.common.Result;
import com.river.service.ChatConversationService;
import com.river.vo.ChatConversationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话Controller
 */
@RestController
@RequestMapping("/chat/conversation")
@RequiredArgsConstructor
@Tag(name = "会话管理")
public class ChatConversationController {

    private final ChatConversationService conversationService;

    @GetMapping("/list")
    @Operation(summary = "获取会话列表")
    public Result<List<ChatConversationVO>> getConversationList(
            @RequestParam(value = "userType", defaultValue = "1") Integer userType) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(conversationService.getConversationList(userId, userType));
    }

    @PostMapping("/top")
    @Operation(summary = "置顶/取消置顶会话")
    public Result<Boolean> toggleTop(@RequestParam Long conversationId,
                                     @RequestParam(value = "userType", defaultValue = "1") Integer userType) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(conversationService.toggleTop(conversationId, userId, userType));
    }

    @PostMapping("/mute")
    @Operation(summary = "设置/取消免打扰")
    public Result<Boolean> toggleMute(@RequestParam Long conversationId,
                                      @RequestParam(value = "userType", defaultValue = "1") Integer userType) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(conversationService.toggleMute(conversationId, userId, userType));
    }

    @DeleteMapping("/delete/{conversationId}")
    @Operation(summary = "删除会话")
    public Result<Boolean> deleteConversation(@PathVariable Long conversationId,
                                              @RequestParam(value = "userType", defaultValue = "1") Integer userType) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(conversationService.deleteConversation(conversationId, userId, userType));
    }

    @PostMapping("/clearUnread")
    @Operation(summary = "清空未读消息数")
    public Result<Boolean> clearUnread(@RequestParam Long roomId,
                                       @RequestParam(value = "userType", defaultValue = "1") Integer userType) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(conversationService.clearUnread(roomId, userId, userType));
    }
}
