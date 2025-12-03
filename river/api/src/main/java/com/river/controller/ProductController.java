package com.river.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.common.Result;
import com.river.dto.product.ProductPageDTO;
import com.river.entity.Product;
import com.river.entity.ShopSlider;
import com.river.service.ProductService;
import com.river.service.ShopSliderService;
import com.river.vo.product.ProductDetailVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userProductController")
@RequestMapping("/user/product")
@Slf4j
@Tag(name = "商品相关接口")
@RequiredArgsConstructor
public class ProductController {

    private  final ProductService productService;
    private  final ShopSliderService shopSliderService;


    @GetMapping("/getShopSlider")
    @Operation(summary = "获取轮播图数据")
    public Result<List<ShopSlider>> getShopSlider(){
        return Result.success(shopSliderService.getAll());
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取商品列表")
    public Result<IPage<Product>> list( @RequestParam(value = "searchNames", required = false) String[] searchNames,
                                        @RequestParam(value = "category", required = false) Long[] category,
                                        @RequestParam(value = "minPrice", required = false) Integer minPrice,
                                        @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
                                        @RequestParam(value = "brand", required = false) Long[] brand) {
        ProductPageDTO productPageDTO = ProductPageDTO.builder()
                .searchNames(searchNames)
                .category(category)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .brand(brand)
                .build();
        log.info("分页获取商品列表:{}", productPageDTO);
        return Result.success(productService.selectPage1(productPageDTO));
    }
    @GetMapping("/getProductDetail/{id}")
    @Operation(summary = "商品详情")
    public Result<ProductDetailVo> userGetProduct(@PathVariable Long id){
        return Result.success(productService.getDetailById(id));
    }
    @PutMapping("/addToWishList/{id}")
    @Operation(summary = "添加商品到心愿单")
    public Result<Object> addToWishList(@PathVariable Long id){
        return Result.success(productService.addToWishList(id));
    }

//    @GetMapping("/search")
//    @Operation(summary = "搜索商品")
//    public Result<List<Product>> search(String searchQuery){
//        log.info("搜索商品:{}", searchQuery);
//        List<Product> productList = productService.search(searchQuery);
//        return Result.success(productList);
//    }
}
