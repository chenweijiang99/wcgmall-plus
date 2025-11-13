package com.river.controller;

import com.river.common.Result;
import com.river.entity.GenTable;
import com.river.service.GenTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/tool/gen")
@RequiredArgsConstructor
@Tag(name = "代码生成业务")
public class GenController {

    private final GenTableService genTableService;

    @GetMapping("/list")
    @Operation(summary = "获取代码生成列表")
    public Result<Map<String, Object>> list(GenTable genTable) {
        return Result.success(genTableService.selectGenTableList(genTable));
    }

    @GetMapping("/preview/{tableId}")
    @Operation(summary = "预览代码")
    public Result<Map<String, String>> preview(@PathVariable Long tableId) {
        return Result.success(genTableService.previewCode(tableId));
    }

    @DeleteMapping("/{tableIds}")
    @Operation(summary = "删除代码生成")
    public Result<Integer> remove(@PathVariable Long[] tableIds) {
        return Result.success(genTableService.deleteGenTableByIds(tableIds));
    }

    @GetMapping("/sync/{tableName}")
    @Operation(summary = "同步数据库")
    public Result<String> syncDb(@PathVariable String tableName) {
        return Result.success(genTableService.syncDb(tableName));
    }

    @GetMapping("/db/list")
    @Operation(summary = "查询数据库列表")
    public Result<Map<String, Object>> dbList(GenTable genTable) {
        Map<String, Object> list = genTableService.selectDbTableList(genTable);
        return Result.success(list);
    }

    @PostMapping("/importTable")
    @Operation(summary = "导入表结构")
    public Result<Void> importTable(@RequestBody String[] tables) {
        genTableService.importGenTable(tables);
        return Result.success();
    }

    @GetMapping("/download/{tableNames}")
    @Operation(summary = "下载代码压缩包")
    public void downloadCode(@PathVariable String[] tableNames, HttpServletResponse response) throws IOException {
        byte[] data = genTableService.downloadCode(tableNames);
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"wcg.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IOUtils.write(data, response.getOutputStream());
    }

}
