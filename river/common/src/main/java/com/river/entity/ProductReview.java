package com.river.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.river.utils.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@TableName("product_review")
@Schema(description = "商品评价")
@Builder
public class ProductReview {
    @TableId(type = IdType.AUTO)
    @Schema(description = "评价ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNumber;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "商品评分1-5")
    private Integer productScore;

    @Schema(description = "物流评分1-5")
    private Integer logisticsScore;

    @Schema(description = "商家评分1-5")
    private Integer merchantScore;

    @Schema(description = "评价内容Markdown")
    private String content;

    @Schema(description = "评价图片URL，逗号隔开")
    private String images;

    @Schema(description = "父评价ID")
    private Long parentReviewId;

    @Schema(description = "根评价ID")
    private Long rootReviewId;

    @Schema(description = "是否管理员回复: 0-用户, 1-管理员")
    private Integer isAdmin;

    @Schema(description = "管理员ID(当isAdmin=1时)")
    private Long adminId;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtil.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = DateUtil.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
    private LocalDateTime updateTime;
}
