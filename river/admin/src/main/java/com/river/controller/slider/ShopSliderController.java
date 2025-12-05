package com.river.controller.slider;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.river.annotation.OperationLogger;
import org.springframework.web.bind.annotation.*;
import com.river.entity.ShopSlider;
import com.river.service.ShopSliderService;
import com.river.common.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
/**
 *  店铺轮播图控制器
 */
@RestController
@RequestMapping("/sys/shopSlider")
@RequiredArgsConstructor
@Tag(name = "店铺轮播图管理")
public class ShopSliderController {

    private final ShopSliderService shopSliderService;

    @GetMapping("/list")
    @Operation(summary = "获取店铺轮播图列表")
    public Result<IPage<ShopSlider>> list(ShopSlider shopSlider) {
        return Result.success(shopSliderService.selectPage(shopSlider));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取店铺轮播图详情")
    public Result<ShopSlider> getInfo(@PathVariable("id") Long id) {
        return Result.success(shopSliderService.getById(id));
    }

    @PostMapping("/add")
    @Operation(summary = "添加店铺轮播图")
    @OperationLogger("添加店铺轮播图")
    @SaCheckPermission("sys:shopSlider:add")
    public Result<Object> add(@RequestBody ShopSlider shopSlider) {
        return Result.success(shopSliderService.insert(shopSlider));
    }

    @PutMapping("/update")
    @Operation(summary = "修改店铺轮播图")
    @OperationLogger("修改店铺轮播图")
    @SaCheckPermission("sys:shopSlider:update")
    public Result<Object> edit(@RequestBody ShopSlider shopSlider) {
        return Result.success(shopSliderService.update(shopSlider));
    }

    @DeleteMapping("/delete/{ids}")
    @Operation(summary = "删除店铺轮播图")
    @OperationLogger("删除店铺轮播图")
    @SaCheckPermission("sys:shopSlider:delete")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(shopSliderService.deleteByIds(ids));
    }
}
