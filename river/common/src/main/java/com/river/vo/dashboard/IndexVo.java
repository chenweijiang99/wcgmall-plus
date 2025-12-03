package com.river.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndexVo {

    @Schema(description = "用户数量")
    private Long userCount;

    @Schema(description = "角色数量")
    private Long roleCount;

    @Schema(description = "商品数量")
    private Long productCount;

    @Schema(description = "商品分类数量")
    private Long categoryCount;

    @Schema(description = "商品品牌数量")
    private Long brandCount;

    @Schema(description = "订单数量")
    private Long orderCount;

    @Schema(description = "博客数量")
    private Long blogCount;

    @Schema(description = "评论数量")
    private Long commentCount;

    @Schema(description = "购物车数量")
    private Long cartCount;

    @Schema(description = "菜单数量")
    private Long menuCount;

    @Schema(description = "用户增长趋势")
    private List<Map<String, Object>> userGrowth;

    @Schema(description = "订单趋势")
    private List<Map<String, Object>> orderGrowth;

    @Schema(description = "商品分类分布")
    private List<Map<String, Object>> categoryDistribution;

    @Schema(description = "最新订单")
    private List<Map<String, Object>> latestOrders;

    @Schema(description = "最新商品")
    private List<Map<String, Object>> latestProducts;

    @Schema(description = "总销售额")
    private Double totalSales;

    @Schema(description = "待处理订单数量")
    private Long pendingOrders;

    @Schema(description = "今日新增用户")
    private Long todayNewUsers;

    @Schema(description = "上架商品数量")
    private Long activeProducts;

}
