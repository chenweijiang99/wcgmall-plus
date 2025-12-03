package com.river.controller;

import com.river.common.Result;
import com.river.entity.Blog;
import com.river.entity.IndexSlider;
import com.river.entity.OfficialCollection;
import com.river.entity.Product;
import com.river.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户首页接口
 * @author 枳枳
 */
@RestController
@Tag(name = "首页接口")
@RequestMapping("/user/index")
@Slf4j
@RequiredArgsConstructor
public class IndexController {

    private final IndexSliderService indexSliderService;
    private final ProductService productService;
    private final BlogService blogService;
    private final OfficialCollectionService officialCollectionService;


    @GetMapping("/getIndexSlider")
    @Operation(summary = "获取轮播图数据")
    public Result<List<IndexSlider>> getIndexSlider(){
        return Result.success(indexSliderService.getAll());
    }

    @GetMapping("/getOL")
    @Operation(summary = "获取首页官方收藏数据")
    public Result<List<OfficialCollection>> index() {
        log.info("获取首页官方收藏数据");
        return Result.success(officialCollectionService.getAll());
    }
    @GetMapping("/getNewProduct")
    @Operation(summary = "获取首页最新商品数据")
    public Result<List<Product>> getNewProduct() {
        return Result.success(productService.userGetNewProductList());
    }

    @GetMapping("/getNewBlog")
    @Operation(summary = "获取首页最新博客数据")
    public Result<List<Blog>> getNewBlog() {
        return Result.success(blogService.userGetNewBlogList());
    }
}
