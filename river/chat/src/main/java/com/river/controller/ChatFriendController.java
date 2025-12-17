package com.river.controller;
import com.river.common.Result;
import cn.dev33.satoken.stp.StpUtil;
import com.river.dto.FriendRequestDTO;
import com.river.dto.HandleFriendRequestDTO;
import com.river.service.ChatFriendService;
import com.river.vo.ChatFriendRequestVO;
import com.river.vo.ChatFriendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 好友Controller
 */
@RestController
@RequestMapping("/chat/friend")
@RequiredArgsConstructor
@Tag(name = "好友管理")
public class ChatFriendController {

    private final ChatFriendService friendService;

    @GetMapping("/list")
    @Operation(summary = "获取好友列表")
    public Result<List<ChatFriendVO>> getFriendList() {
        Long userId = StpUtil.getLoginIdAsLong();
        Integer userType = getUserType();
        return Result.success(friendService.getFriendList(userId, userType));
    }

    @PostMapping("/request")
    @Operation(summary = "发送好友申请")
    public Result<Boolean> sendFriendRequest(@Valid @RequestBody FriendRequestDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        Integer userType = getUserType();
        return Result.success(friendService.sendFriendRequest(dto, userId, userType));
    }

    @GetMapping("/request/list")
    @Operation(summary = "获取收到的好友申请列表")
    public Result<List<ChatFriendRequestVO>> getReceivedRequestList() {
        Long userId = StpUtil.getLoginIdAsLong();
        Integer userType = getUserType();
        return Result.success(friendService.getReceivedRequestList(userId, userType));
    }

    @PostMapping("/request/handle")
    @Operation(summary = "处理好友申请")
    public Result<Boolean> handleFriendRequest(@Valid @RequestBody HandleFriendRequestDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        Integer userType = getUserType();
        return Result.success(friendService.handleFriendRequest(dto, userId, userType));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除好友")
    public Result<Boolean> deleteFriend(@RequestParam Long friendId,
                                        @RequestParam(defaultValue = "1") Integer friendType) {
        Long userId = StpUtil.getLoginIdAsLong();
        Integer userType = getUserType();
        return Result.success(friendService.deleteFriend(friendId, friendType, userId, userType));
    }

    @PostMapping("/remark")
    @Operation(summary = "修改好友备注")
    public Result<Boolean> updateRemark(@RequestParam Long friendId,
                                        @RequestParam(defaultValue = "1") Integer friendType,
                                        @RequestParam String remark) {
        Long userId = StpUtil.getLoginIdAsLong();
        Integer userType = getUserType();
        return Result.success(friendService.updateRemark(friendId, friendType, remark, userId, userType));
    }

    private Integer getUserType() {
        Object loginId = StpUtil.getLoginId();
        if (loginId != null && loginId.toString().contains(":")) {
            String[] parts = loginId.toString().split(":");
            return Integer.parseInt(parts[0]);
        }
        return 1;
    }
}
