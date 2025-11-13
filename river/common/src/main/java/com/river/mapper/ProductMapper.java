package com.river.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.river.dto.product.ProductPageDTO;
import com.river.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.river.vo.product.ProductDetailVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品 Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    /**
     * 用户获取最新商品列表
     */
    List<Product> userGetNewProductList();

    ProductDetailVo getDetailById(Long id);

    IPage<Product> selectPage1(Page<ProductPageDTO> page, ProductPageDTO productPageDTO);
}