package com.river.vo.comments;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.river.entity.Comments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentsVo implements Serializable {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "父级评论ID")
    private Long pid;

    @Schema(description = "关联ID")
    private Long fid;

    @Schema(description = "模块标识")
    private String module;

    @Schema(description = "根节点ID")
    private Long rootId;

    @Schema(description = "评论人ID")
    private Long userId;

    @Schema(description = "评论时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    @Schema(description = "评论内容")
    private String comment;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "父级用户名")
    private String parentUserName;

    @Schema(description = "子评论列表")
    private List<CommentsVo> children;
}
