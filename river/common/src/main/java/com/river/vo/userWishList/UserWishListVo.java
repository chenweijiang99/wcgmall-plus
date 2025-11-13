package com.river.vo.userWishList;

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
public class UserWishListVo implements Serializable {
    private Long id;
    private Long productId;
    private Long userId;
    private String productName;
    private BigDecimal productPrice;
    private String productImage;
}
