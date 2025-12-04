package com.river.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "订单详情VO")
public class OrderDetailVO {
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
}
