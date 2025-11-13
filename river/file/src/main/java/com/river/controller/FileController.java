package com.river.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.common.Constants;
import com.river.common.Result;
import com.river.entity.FileDetail;
import com.river.entity.SysFileOss;
import com.river.enums.FileOssEnum;
import com.river.exception.ServiceException;
import com.river.mapper.SysFileOssMapper;
import com.river.service.FileDetailService;
import com.river.utils.DateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequestMapping("/file")
@Tag(name = "文件管理")
@RequiredArgsConstructor
public class FileController {
    private final FileDetailService fileDetailService;

    private final FileStorageService fileStorageService;

    private final SysFileOssMapper sysFileOssMapper;

    @SaCheckLogin
    @GetMapping("/list")
    @Operation(summary = "获取文件记录表列表")
    public Result<IPage<FileDetail>> list(FileDetail fileDetail) {
        return Result.success(fileDetailService.selectPage(fileDetail));
    }

    @SaCheckLogin
    @GetMapping("/getOssConfig")
    @Operation(summary = "获取存储平台配置")
    public Result<List<SysFileOss>> getOssConfig() {
        return Result.success(fileDetailService.getOssConfig());
    }


    @SaCheckLogin
    @PostMapping("/addOss")
    @SaCheckPermission("sys:oss:submit")
    @Operation(summary = "添加存储平台配置")
    public Result<Void> addOss(@RequestBody SysFileOss sysFileOss) {
        fileDetailService.addOss(sysFileOss);
        if (sysFileOss.getIsEnable() == Constants.YES) {
            fileStorageService.getProperties().setDefaultPlatform(sysFileOss.getPlatform());
        }
        return Result.success();
    }

    @SaCheckLogin
    @PutMapping("/updateOss")
    @SaCheckPermission("sys:oss:submit")
    @Operation(summary = "修改存储平台配置")
    public Result<Void> updateOss(@RequestBody SysFileOss sysFileOss) {
        fileDetailService.updateOss(sysFileOss);
        if (sysFileOss.getIsEnable() == Constants.YES) {
            fileStorageService.getProperties().setDefaultPlatform(sysFileOss.getPlatform());
        }
        return Result.success();
    }

    @SaCheckLogin
    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public Result<String> upload(MultipartFile file, String source) {
        MultipartFile uploadFile = file;
        String path = DateUtil.parseDateToStr(DateUtil.YYYYMMDD, DateUtil.getNowDate()) + "/";
        if (StringUtils.isNotBlank(source)) {
            path = path + source + "/";
        }
        FileInfo fileInfo = fileStorageService.of(uploadFile)
                .setPath(path)
                .setSaveFilename(RandomUtil.randomNumbers(2) + "_" + uploadFile.getOriginalFilename())
                .putAttr("source", source).upload();
        if (fileInfo == null) {
            throw new ServiceException("上传文件失败");
        }
        return Result.success(fileInfo.getUrl());
    }

    @SaCheckLogin
    @PostMapping("/uploadBatch")
    @Operation(summary = "批量上传文件")
    public Result<List<String>> uploadBatch(MultipartFile[] files, String source) {
        String path = DateUtil.parseDateToStr(DateUtil.YYYYMMDD, DateUtil.getNowDate()) + "/";
        if (StringUtils.isNotBlank(source)) {
            path = path + source + "/";
        }
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            FileInfo fileInfo = fileStorageService.of(file)
                    .setPath(path)
                    .setSaveFilename(RandomUtil.randomNumbers(2) + "_" + file.getOriginalFilename()) //随机俩个数字，避免相同文件名时文件名冲突
                    .putAttr("source", source)
                    .upload();
            urls.add(fileInfo.getUrl());
        }
        return Result.success(urls);
    }


    @GetMapping("/delete")
    @Operation(summary = "删除文件")
    @SaCheckPermission("sys:file:delete")
    public Result<Boolean> delete(String url) {
        boolean flag = fileStorageService.delete(url);
        if (flag) {
            fileDetailService.delete(url);
        }
        return Result.success();
    }

    @PostMapping("/deleteBatch")
    @Operation(summary = "批量删除文件")
    @SaCheckPermission("sys:file:delete")
    public Result<Boolean> deleteBatch(@RequestBody String[] urls) {
        List<String> deleteUrls = new ArrayList<>();
        for (String url : urls) {
            boolean flag = fileStorageService.delete(url);
            if (flag) {
                deleteUrls.add(url);
            }
        }
        if (deleteUrls.size() == urls.length) {
            fileDetailService.deleteBatch(urls);
        }
        return Result.success(true);
    }
}
