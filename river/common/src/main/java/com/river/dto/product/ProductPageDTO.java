package com.river.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPageDTO {
    private String[] searchNames;
    private Long[] category;
    private Integer minPrice;
    private Integer maxPrice;
    private Long[] brand;
}
