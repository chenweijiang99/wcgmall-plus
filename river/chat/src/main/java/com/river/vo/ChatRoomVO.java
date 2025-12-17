package com.river.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天室VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "聊天室VO")
public class ChatRoomVO {

    @Schema(description = "聊天室ID")
    private Long id;

    @Schema(description = "聊天室名称")
    private String name;

    @Schema(description = "聊天室头像")
    private String avatar;

    @Schema(description = "聊天室类型: 1-私聊 2-群聊")
    private Integer type;

    @Schema(description = "群主ID")
    private Long ownerId;

    @Schema(description = "群主类型")
    private Integer ownerType;

    @Schema(description = "群主昵称")
    private String ownerNickname;

    @Schema(description = "群公告")
    private String announcement;

    @Schema(description = "成员数量")
    private Integer memberCount;

    @Schema(description = "最大成员数")
    private Integer maxMemberCount;

    @Schema(description = "当前用户角色: 0-普通成员 1-管理员 2-群主")
    private Integer myRole;

    @Schema(description = "成员列表(部分)")
    private List<ChatMemberVO> members;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
