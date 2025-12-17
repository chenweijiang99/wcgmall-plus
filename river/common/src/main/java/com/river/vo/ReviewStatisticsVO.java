package com.river.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "评价统计VO")
public class ReviewStatisticsVO {
    @Schema(description = "总评价数")
    private Long totalCount;

    @Schema(description = "好评数(4-5星)")
    private Long goodCount;

    @Schema(description = "中评数(3星)")
    private Long mediumCount;

    @Schema(description = "差评数(1-2星)")
    private Long badCount;

    @Schema(description = "好评率")
    private BigDecimal goodRate;

    @Schema(description = "平均商品评分")
    private BigDecimal avgProductScore;

    @Schema(description = "平均物流评分")
    private BigDecimal avgLogisticsScore;

    @Schema(description = "平均商家评分")
    private BigDecimal avgMerchantScore;

    @Schema(description = "1星数量")
    private Long star1Count;

    @Schema(description = "2星数量")
    private Long star2Count;

    @Schema(description = "3星数量")
    private Long star3Count;

    @Schema(description = "4星数量")
    private Long star4Count;

    @Schema(description = "5星数量")
    private Long star5Count;

    @Schema(description = "有图评价数")
    private Long withImageCount;
}
