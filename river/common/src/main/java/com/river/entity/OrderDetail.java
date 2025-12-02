package com.river.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("product_order")
@Schema(description = "对象")
public class ProductOrderDetail {
    private Long id;
    private String orderNumber;
    private Long productId;
    private Integer productNumber;
}
