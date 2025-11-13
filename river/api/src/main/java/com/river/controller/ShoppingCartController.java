package com.river.controller;

import com.river.common.Result;
import com.river.entity.ShoppingCart;
import com.river.service.ShoppingCartService;
import com.river.vo.shoppingCart.ShoppingCartVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Tag(name = "购物车接口")
@RequiredArgsConstructor
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    @GetMapping
    @Operation(summary = "获取购物车数据")
    public Result<List<ShoppingCartVo>> listByUserId(){
        return Result.success(shoppingCartService.listByUserId());
    }

    @PostMapping
    @Operation(summary = "添加购物车数据")
    public Result<Object> addShoppingCart(Long productId){
        return Result.success(shoppingCartService.insertByUserId(productId));
    }

    @DeleteMapping
    @Operation(summary = "删除购物车数据(单个)")
    public Result<Object> deleteShoppingCart(Long productId){
        return Result.success(shoppingCartService.deleteByUserId(productId));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除数据")
    public Result<Object> deleteShoppingCartData(Long productId){
        return Result.success(shoppingCartService.deleteByProductId(productId));
    }

    @PostMapping("/addCarProductToWishList")
    @Operation(summary = "购物车商品添加到心愿单")
    public Result<Object> addCarProductToWishList(Long productId){
        return Result.success(shoppingCartService.addCarProductToWishList(productId));
    }

    @PostMapping("/addProduct")
    @Operation(summary = "增加购物车数据")
    public Result<Object> addProduct(Long productId){
        return Result.success(shoppingCartService.addProduct(productId));
    }

    @PostMapping("/reduceProduct")
    @Operation(summary = "减少购物车数据")
    public Result<Object> reduceProduct(Long productId){
        return Result.success(shoppingCartService.reduceProduct(productId));
    }


}
