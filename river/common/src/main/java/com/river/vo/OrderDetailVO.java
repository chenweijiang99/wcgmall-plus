package com.river.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "订单详情VO")
public class OrderDetailVO {
    @Schema(description = "订单详情ID")
    private Long id;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "购买数量")
    private Integer number;

    @Schema(description = "商品确认收货状态: 0=未确认, 1=已确认")
    private Integer confirmStatus;

    @Schema(description = "商品确认收货时间")
    private LocalDateTime confirmTime;

    @Schema(description = "商品评价状态: 0=未评价, 1=已评价")
    private Integer reviewStatus;
}
