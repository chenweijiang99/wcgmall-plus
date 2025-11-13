package com.river.vo.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailVo implements Serializable {
    private Long id;
    private String name;
    private String categoryName;
    private String brandName;
    private BigDecimal price;
    private Integer inventory;
    private String image;
    private String detailImages;
    private String description;
    private String descriptionImage;
}
