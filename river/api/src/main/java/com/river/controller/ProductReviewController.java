package com.river.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.common.Result;
import com.river.dto.ProductReviewDTO;
import com.river.service.ProductReviewService;
import com.river.vo.ProductReviewVO;
import com.river.vo.ReviewStatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userProductReviewController")
@RequestMapping("/user/productReview")
@Tag(name = "用户商品评价相关接口")
@RequiredArgsConstructor
public class ProductReviewController {
    private final ProductReviewService productReviewService;

    @PostMapping("/add")
    @Operation(summary = "添加商品评价")
    public Result<Object> addReview(@RequestBody ProductReviewDTO dto) {
        return Result.success(productReviewService.addReview(dto));
    }

    @GetMapping("/tree")
    @Operation(summary = "查询商品评价树")
    public Result<IPage<ProductReviewVO>> selectReviewTree(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(productReviewService.selectReviewTree(productId, pageNum, pageSize));
    }

    @GetMapping("/list")
    @Operation(summary = "按评分筛选评价列表")
    public Result<IPage<ProductReviewVO>> selectReviewByScore(
            @RequestParam Long productId,
            @RequestParam(required = false) Integer scoreType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(productReviewService.selectReviewByScore(productId, scoreType, pageNum, pageSize));
    }

    @GetMapping("/statistics/{productId}")
    @Operation(summary = "获取商品评价统计")
    public Result<ReviewStatisticsVO> getStatistics(@PathVariable Long productId) {
        return Result.success(productReviewService.getReviewStatistics(productId));
    }

    @GetMapping("/count/{productId}")
    @Operation(summary = "查询商品评价数量")
    public Result<Long> selectReviewCount(@PathVariable Long productId) {
        return Result.success(productReviewService.selectReviewCount(productId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评价")
    public Result<Object> deleteReview(@PathVariable Long id) {
        return Result.success(productReviewService.deleteReview(id));
    }

    @GetMapping("/pending")
    @Operation(summary = "查询待评价商品列表")
    public Result<List<ProductReviewVO>> selectPendingReviewProducts() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(productReviewService.selectPendingReviewProducts(userId));
    }
}
