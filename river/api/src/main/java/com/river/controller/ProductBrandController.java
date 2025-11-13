package com.river.controller;

import com.river.common.Result;
import com.river.entity.ProductBrand;
import com.river.service.ProductBrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userProductBrandController")
@RequestMapping("/user/brand")
@Slf4j
@Tag(name = "品牌相关接口")
@RequiredArgsConstructor
public class ProductBrandController {
    private final ProductBrandService productBrandService;
    @GetMapping
    @Operation(summary = "品牌列表")
    public Result<List<ProductBrand>> list() {
        return Result.success(productBrandService.list());
    }
}
