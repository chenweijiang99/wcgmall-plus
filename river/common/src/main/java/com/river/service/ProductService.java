package com.river.service;

import com.river.dto.product.ProductPageDTO;
import com.river.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.vo.product.ProductDetailVo;

import java.util.List;

/**
 *  服务接口
 */
public interface ProductService extends IService<Product> {
    /**
     * 查询分页列表
     */
    IPage<Product> selectPage(Product product);

    /**
     * 查询列表
     */
    List<Product> selectList(Product product);

    /**
     * 新增
     */
    boolean insert(Product product);

    /**
     * 修改
     */
    boolean update(Product product);

    /**
     * 批量删除
     */
    boolean deleteByIds(List<Long> ids);

    /**
     * 启动或停用商品
     */
    boolean startOrStop(Long id);
    /**
     * 用户获取最新商品列表
     */
    List<Product> userGetNewProductList();

    ProductDetailVo getDetailById(Long id);

    boolean addToWishList(Long id);

    IPage<Product> selectPage1(ProductPageDTO productPageDTO);
}
