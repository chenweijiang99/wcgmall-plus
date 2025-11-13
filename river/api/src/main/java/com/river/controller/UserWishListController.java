package com.river.controller;

import java.util.List;

import com.river.vo.userWishList.UserWishListVo;
import org.springframework.web.bind.annotation.*;
import com.river.service.UserWishListService;
import com.river.common.Result;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
/**
 *  控制器
 */
@RestController
@RequestMapping("/user/wishList")
@RequiredArgsConstructor
@Tag(name = "管理")
public class UserWishListController {

    private final UserWishListService userWishListService;

    @GetMapping
    @Operation(summary = "获取心愿单")
    public Result<List<UserWishListVo>> getWishList() {
        return Result.success(userWishListService.selectListByUserId());
    }
    @DeleteMapping("/{productId}")
    @Operation(summary = "删除心愿单")
    public Result<Object> deleteWishList(@PathVariable Long productId) {
        return Result.success(userWishListService.deleteByProductId(productId));
    }

    @PostMapping("/{productId}")
    @Operation(summary = "添加心愿单商品到购物车")
    public Result<Object> addWishListProductToCart(@PathVariable Long productId) {
        return Result.success(userWishListService.addWishListProductToCart(productId));
    }

}
