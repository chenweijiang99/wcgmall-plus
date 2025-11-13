package com.river.controller.product;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.river.annotation.OperationLogger;
import org.springframework.web.bind.annotation.*;
import com.river.entity.ProductCategory;
import com.river.service.ProductCategoryService;
import com.river.common.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
/**
 *  商品分类 控制器
 */
@RestController
@RequestMapping("/sys/productCategory")
@RequiredArgsConstructor
@Tag(name = "商品分类管理")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @GetMapping("/list")
    @Operation(summary = "获取商品分类列表")
    public Result<IPage<ProductCategory>> list(ProductCategory productCategory) {
        return Result.success(productCategoryService.selectPage(productCategory));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有商品分类")
    public Result<List<ProductCategory>> getAll(ProductCategory productCategory) {
        return Result.success(productCategoryService.selectList(productCategory));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品分类详情")
    public Result<ProductCategory> getInfo(@PathVariable("id") Long id) {
        return Result.success(productCategoryService.getById(id));
    }

    @PostMapping("/add")
    @Operation(summary = "添加商品分类")
    @OperationLogger("添加商品分类")
    @SaCheckPermission("sys:category:add")
    public Result<Object> add(@RequestBody ProductCategory productCategory) {
        return Result.success(productCategoryService.insert(productCategory));
    }

    @PutMapping("/update")
    @Operation(summary = "修改商品分类")
    @OperationLogger("修改商品分类")
    @SaCheckPermission("sys:category:update")
    public Result<Object> edit(@RequestBody ProductCategory productCategory) {
        return Result.success(productCategoryService.update(productCategory));
    }

    @DeleteMapping("/delete/{ids}")
    @Operation(summary = "删除商品分类")
    @OperationLogger("删除商品分类")
    @SaCheckPermission("sys:category:delete")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(productCategoryService.deleteByIds(ids));
    }
}
