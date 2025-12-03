package com.river.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.river.dto.product.ProductPageDTO;
import com.river.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.river.vo.product.ProductDetailVo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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

    /**
     * 查询最新商品
     * @return 最新商品列表
     */
    @MapKey("id")
    List<Map<String, Object>> selectLatestProducts(@Param("limit") int limit);


    /**
     * 查询上架商品数量
     * @return 上架商品数量
     */
    Long selectActiveProducts();
}