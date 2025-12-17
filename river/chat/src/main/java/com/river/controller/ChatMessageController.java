package com.river.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.river.dto.ChatMessageDTO;
import com.river.common.Result;
import com.river.config.websocket.ChatWebSocketHandler;
import com.river.service.ChatMessageService;
import com.river.vo.ChatMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天消息Controller
 */
@RestController
@RequestMapping("/chat/message")
@RequiredArgsConstructor
@Tag(name = "聊天消息")
public class ChatMessageController {

    private final ChatMessageService messageService;
    private final ChatWebSocketHandler webSocketHandler;

    @PostMapping("/send")
    @Operation(summary = "发送消息(HTTP备用接口)")
    public Result<ChatMessageVO> sendMessage(@Valid @RequestBody ChatMessageDTO dto,
                                             @RequestParam(value = "userType", defaultValue = "1") Integer userType) {
        Long userId = StpUtil.getLoginIdAsLong();
        ChatMessageVO messageVO = messageService.sendMessage(dto, userId, userType);
        // 广播消息给聊天室所有在线成员
        webSocketHandler.broadcastToRoom(dto.getRoomId(), "NEW_MSG", messageVO);
        return Result.success(messageVO);
    }

    @GetMapping("/history")
    @Operation(summary = "获取历史消息")
    public Result<List<ChatMessageVO>> getHistoryMessages(
            @RequestParam Long roomId,
            @RequestParam(required = false) Long lastMsgId,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(value = "userType", defaultValue = "1") Integer userType) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(messageService.getHistoryMessages(roomId, lastMsgId, pageSize, userId, userType));
    }

    @PostMapping("/revoke/{msgId}")
    @Operation(summary = "撤回消息")
    public Result<Boolean> revokeMessage(@PathVariable Long msgId,
                                         @RequestParam(value = "userType", defaultValue = "1") Integer userType) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(messageService.revokeMessage(msgId, userId, userType));
    }

    @PostMapping("/read")
    @Operation(summary = "标记消息已读")
    public Result<Boolean> markAsRead(@RequestParam Long roomId,
                                      @RequestParam Long msgId,
                                      @RequestParam(value = "userType", defaultValue = "1") Integer userType) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(messageService.markAsRead(roomId, msgId, userId, userType));
    }

    @GetMapping("/detail/{msgId}")
    @Operation(summary = "获取消息详情")
    public Result<ChatMessageVO> getMessageDetail(@PathVariable Long msgId) {
        return Result.success(messageService.getMessageDetail(msgId));
    }
}
