package com.river.controller;

import com.river.common.Result;
import com.river.entity.ProductCategory;
import com.river.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userProductCategoryController")
@RequestMapping("/user/productCategory")
@Slf4j
@Tag(name = "商品分类相关接口")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @GetMapping
    @Operation(summary = "获取分类列表")
    public Result<List<ProductCategory>> list() {
       return Result.success(productCategoryService.list());
    }
}
