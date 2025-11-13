package com.river.vo.shoppingCart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShoppingCartVo implements Serializable {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer number;
    private String productName;
    private BigDecimal productPrice;
    private String productImage;
}
