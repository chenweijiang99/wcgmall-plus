package com.river.controller.product;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.river.annotation.OperationLogger;
import org.springframework.web.bind.annotation.*;
import com.river.entity.ProductBrand;
import com.river.service.ProductBrandService;
import com.river.common.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
/**
 *  商品品牌 控制器
 */
@RestController
@RequestMapping("/sys/productBrand")
@RequiredArgsConstructor
@Tag(name = "商品品牌管理")
public class ProductBrandController {

    private final ProductBrandService productBrandService;

    @GetMapping("/list")
    @Operation(summary = "获取商品品牌列表")
    public Result<IPage<ProductBrand>> list(ProductBrand productBrand) {
        return Result.success(productBrandService.selectPage(productBrand));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有商品品牌")
    public Result<List<ProductBrand>> getAll(ProductBrand productBrand) {
        return Result.success(productBrandService.selectList(productBrand));
    }

    @GetMapping("/{id}")
    @OperationLogger("获取商品品牌详情")
    @Operation(summary = "获取商品品牌详情")
    public Result<ProductBrand> getInfo(@PathVariable("id") Long id) {
        return Result.success(productBrandService.getById(id));
    }

    @PostMapping("/add")
    @Operation(summary = "添加商品品牌")
    @OperationLogger("添加商品品牌")
    @SaCheckPermission("sys:brand:add")
    public Result<Object> add(@RequestBody ProductBrand productBrand) {
        return Result.success(productBrandService.insert(productBrand));
    }

    @PutMapping("/update")
    @Operation(summary = "修改商品品牌")
    @OperationLogger("修改商品品牌")
    @SaCheckPermission("sys:brand:update")
    public Result<Object> edit(@RequestBody ProductBrand productBrand) {
        return Result.success(productBrandService.update(productBrand));
    }

    @DeleteMapping("/delete/{ids}")
    @Operation(summary = "删除商品品牌")
    @OperationLogger("删除商品品牌")
    @SaCheckPermission("sys:brand:delete")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(productBrandService.deleteByIds(ids));
    }
}
