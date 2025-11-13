package com.river.controller.officalCollection;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.river.annotation.OperationLogger;
import org.springframework.web.bind.annotation.*;
import com.river.entity.OfficialCollection;
import com.river.service.OfficialCollectionService;
import com.river.common.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
/**
 * 官方收藏控制器
 */
@RestController
@RequestMapping("/sys/officialCollection")
@RequiredArgsConstructor
@Tag(name = "官方收藏管理")
public class OfficialCollectionController {

    private final OfficialCollectionService officialCollectionService;

    @GetMapping("/all")
    @Operation(summary = "获取官方收藏列表")
    public Result<List<OfficialCollection>> getAll() {
        return Result.success(officialCollectionService.list());
    }

    @PutMapping("/add/{productId}")
    @Operation(summary = "添加官方收藏")
    @OperationLogger("添加官方收藏")
    @SaCheckPermission("sys:officialCollection:add")
    public Result<Object> add(@PathVariable Long productId) {
        return Result.success(officialCollectionService.insert(productId));
    }


    @DeleteMapping("/delete/{productId}")
    @Operation(summary = "删除")
    @OperationLogger("删除官方收藏")
    @SaCheckPermission("sys:officialCollection:delete")
    public Result<Object> remove(@PathVariable Long productId) {
        return Result.success(officialCollectionService.deleteByIds(productId));
    }
}
