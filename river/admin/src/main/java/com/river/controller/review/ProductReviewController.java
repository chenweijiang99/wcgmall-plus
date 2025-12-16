package com.river.controller.review;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.annotation.OperationLogger;
import com.river.common.Result;
import com.river.entity.ProductReview;
import com.river.service.ProductReviewService;
import com.river.vo.ProductReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品评价管理控制器
 */
@RestController
@RequestMapping("/sys/review")
@RequiredArgsConstructor
@Tag(name = "商品评价管理")
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    @GetMapping("/list")
    @Operation(summary = "获取评价列表")
    public Result<IPage<ProductReviewVO>> list(
            ProductReview query,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(productReviewService.adminSelectPage(query, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取评价详情")
    public Result<ProductReviewVO> getDetail(@PathVariable Long id) {
        return Result.success(productReviewService.getReviewDetail(id));
    }

    @PostMapping("/reply")
    @Operation(summary = "管理员回复评价")
    @OperationLogger("回复评价")
    @SaCheckPermission("sys:review:reply")
    public Result<Boolean> reply(@RequestBody Map<String, Object> params) {
        Long parentReviewId = Long.valueOf(params.get("parentReviewId").toString());
        String content = params.get("content").toString();
        Long adminId = StpUtil.getLoginIdAsLong();
        return Result.success(productReviewService.adminReply(parentReviewId, content, adminId));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除评价")
    @OperationLogger("删除评价")
    @SaCheckPermission("sys:review:delete")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(productReviewService.adminDeleteReview(id));
    }
}
