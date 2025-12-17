package com.river.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.river.dto.CreateRoomDTO;
import com.river.entity.ChatRoom;
import com.river.common.Result;
import com.river.service.ChatRoomService;
import com.river.vo.ChatMemberVO;
import com.river.vo.ChatRoomVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天室Controller
 */
@RestController
@RequestMapping("/chat/room")
@RequiredArgsConstructor
@Tag(name = "聊天室管理")
public class ChatRoomController {

    private final ChatRoomService roomService;

    @PostMapping("/create")
    @Operation(summary = "创建群聊")
    public Result<ChatRoom> createRoom(@Valid @RequestBody CreateRoomDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        Integer userType = getUserType();
        ChatRoom room = roomService.createGroupRoom(dto, userId, userType);
        return Result.success(room);
    }

    @GetMapping("/info/{roomId}")
    @Operation(summary = "获取聊天室详情")
    public Result<ChatRoomVO> getRoomInfo(@PathVariable Long roomId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Integer userType = getUserType();
        return Result.success(roomService.getRoomDetail(roomId, userId, userType));
    }

    @PutMapping("/update")
    @Operation(summary = "更新聊天室信息")
    public Result<Boolean> updateRoom(@RequestBody ChatRoom room) {
        Long userId = StpUtil.getLoginIdAsLong();
        Integer userType = getUserType();
        return Result.success(roomService.updateRoom(room, userId, userType));
    }

    @PostMapping("/invite")
    @Operation(summary = "邀请成员加入群聊")
    public Result<Boolean> inviteMembers(@RequestParam Long roomId,
                                         @RequestParam List<Long> memberIds,
                                         @RequestParam(required = false) List<Integer> memberTypes) {
        Long userId = StpUtil.getLoginIdAsLong();
        Integer userType = getUserType();
        return Result.success(roomService.inviteMembers(roomId, memberIds, memberTypes, userId, userType));
    }

    @PostMapping("/kick")
    @Operation(summary = "踢出成员")
    public Result<Boolean> kickMember(@RequestParam Long roomId,
                                      @RequestParam Long memberId,
                                      @RequestParam(defaultValue = "1") Integer memberType) {
        Long userId = StpUtil.getLoginIdAsLong();
        Integer userType = getUserType();
        return Result.success(roomService.kickMember(roomId, memberId, memberType, userId, userType));
    }

    @PostMapping("/quit")
    @Operation(summary = "退出群聊")
    public Result<Boolean> quitRoom(@RequestParam Long roomId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Integer userType = getUserType();
        return Result.success(roomService.quitRoom(roomId, userId, userType));
    }

    @GetMapping("/members/{roomId}")
    @Operation(summary = "获取聊天室成员列表")
    public Result<List<ChatMemberVO>> getRoomMembers(@PathVariable Long roomId) {
        return Result.success(roomService.getRoomMembers(roomId));
    }

    @PostMapping("/dissolve")
    @Operation(summary = "解散群聊")
    public Result<Boolean> dissolveRoom(@RequestParam Long roomId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Integer userType = getUserType();
        return Result.success(roomService.dissolveRoom(roomId, userId, userType));
    }

    @GetMapping("/private")
    @Operation(summary = "获取或创建私聊聊天室")
    public Result<ChatRoom> getOrCreatePrivateRoom(@RequestParam Long friendId,
                                                   @RequestParam(defaultValue = "1") Integer friendType) {
        Long userId = StpUtil.getLoginIdAsLong();
        Integer userType = getUserType();
        return Result.success(roomService.getOrCreatePrivateRoom(userId, userType, friendId, friendType));
    }

    /**
     * 获取用户类型(从登录信息中解析)
     */
    private Integer getUserType() {
        // 默认为普通用户,可根据实际登录逻辑调整
        Object loginId = StpUtil.getLoginId();
        if (loginId != null && loginId.toString().contains(":")) {
            String[] parts = loginId.toString().split(":");
            return Integer.parseInt(parts[0]);
        }
        return 1;
    }
}
