package com.river.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "商品评价DTO")
public class ProductReviewDTO {
    @Schema(description = "订单编号")
    private String orderNumber;

    @Schema(description = "商品ID")
    private Long productId;

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

    @Schema(description = "父评价ID(回复时填写)")
    private Long parentReviewId;
}
