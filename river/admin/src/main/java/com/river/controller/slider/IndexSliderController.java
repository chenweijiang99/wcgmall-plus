package com.river.controller.slider;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.river.annotation.OperationLogger;
import org.springframework.web.bind.annotation.*;
import com.river.entity.IndexSlider;
import com.river.service.IndexSliderService;
import com.river.common.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
/**
 *  首页轮播图控制器
 */
@RestController
@RequestMapping("/sys/indexSlider")
@RequiredArgsConstructor
@Tag(name = "首页轮播图管理")
public class IndexSliderController {

    private final IndexSliderService indexSliderService;

    @GetMapping("/list")
    @Operation(summary = "获取首页轮播图列表")
    public Result<IPage<IndexSlider>> list(IndexSlider indexSlider) {
        return Result.success(indexSliderService.selectPage(indexSlider));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取首页轮播图详情")
    public Result<IndexSlider> getInfo(@PathVariable("id") Long id) {
        return Result.success(indexSliderService.getById(id));
    }

    @PostMapping("/add")
    @Operation(summary = "添加首页轮播图")
    @OperationLogger("添加首页轮播图")
    @SaCheckPermission("sys:indexSlider:add")
    public Result<Object> add(@RequestBody IndexSlider indexSlider) {
        return Result.success(indexSliderService.insert(indexSlider));
    }

    @PutMapping("/update")
    @Operation(summary = "修改首页轮播图")
    @OperationLogger("修改首页轮播图")
    @SaCheckPermission("sys:indexSlider:update")
    public Result<Object> edit(@RequestBody IndexSlider indexSlider) {
        return Result.success(indexSliderService.update(indexSlider));
    }

    @DeleteMapping("/delete/{ids}")
    @Operation(summary = "删除首页轮播图")
    @OperationLogger("删除首页轮播图")
    @SaCheckPermission("sys:indexSlider:delete")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(indexSliderService.deleteByIds(ids));
    }
}
