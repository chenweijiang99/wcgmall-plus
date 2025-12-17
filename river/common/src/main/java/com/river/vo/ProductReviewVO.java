package com.river.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.river.utils.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "商品评价VO")
public class ProductReviewVO {
    @Schema(description = "评价ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNumber;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "商品评分1-5")
    private Integer productScore;

    @Schema(description = "物流评分1-5")
    private Integer logisticsScore;

    @Schema(description = "商家评分1-5")
    private Integer merchantScore;

    @Schema(description = "评价内容Markdown")
    private String content;

    @Schema(description = "评价图片URL列表")
    private List<String> imageList;

    @Schema(description = "父评价ID")
    private Long parentReviewId;

    @Schema(description = "父评价用户名")
    private String parentUsername;

    @Schema(description = "根评价ID")
    private Long rootReviewId;

    @Schema(description = "是否管理员回复: 0-用户, 1-管理员")
    private Integer isAdmin;

    @Schema(description = "管理员ID")
    private Long adminId;

    @Schema(description = "管理员名称")
    private String adminName;

    @Schema(description = "子评价列表")
    private List<ProductReviewVO> children;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtil.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = DateUtil.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
    private LocalDateTime updateTime;
}
