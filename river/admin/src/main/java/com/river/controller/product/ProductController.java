package com.river.controller.product;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.river.annotation.OperationLogger;
import com.river.service.OfficialCollectionService;
import org.springframework.web.bind.annotation.*;
import com.river.entity.Product;
import com.river.service.ProductService;
import com.river.common.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
/**
 *  商品控制器
 */
@RestController
@RequestMapping("/sys/product")
@RequiredArgsConstructor
@Tag(name = "商品管理")
public class ProductController {

    private final ProductService productService;
    private final OfficialCollectionService officialCollectionService;

    @GetMapping("/list")
    @Operation(summary = "获取商品列表")
    public Result<IPage<Product>> list(Product product) {
        return Result.success(productService.selectPage(product));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情")
    public Result<Product> getInfo(@PathVariable("id") Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping("/add")
    @Operation(summary = "添加商品")
    @OperationLogger("添加商品")
    @SaCheckPermission("sys:product:add")
    public Result<Object> add(@RequestBody Product product) {
        return Result.success(productService.insert(product));
    }

    @PutMapping("/update")
    @Operation(summary = "修改商品")
    @OperationLogger("修改商品")
    @SaCheckPermission("sys:product:update")
    public Result<Object> edit(@RequestBody Product product) {
        return Result.success(productService.update(product));
    }

    @DeleteMapping("/delete/{ids}")
    @Operation(summary = "删除商品")
    @OperationLogger("删除商品")
    @SaCheckPermission("sys:product:delete")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(productService.deleteByIds(ids));
    }

    @PutMapping("/startOrStop")
    @Operation(summary = "启动或停用商品")
    @OperationLogger("启动或停用商品")
    @SaCheckPermission("sys:product:status")
    public Result<Object> startOrStop(@RequestBody Long id) {
        return Result.success(productService.startOrStop(id));
    }

}
