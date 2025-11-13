package com.river.service;

import com.river.entity.ShoppingCart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.vo.shoppingCart.ShoppingCartVo;

import java.util.List;

/**
 *  服务接口
 */
public interface ShoppingCartService extends IService<ShoppingCart> {
    /**
     * 查询分页列表
     */
    IPage<ShoppingCart> selectPage(ShoppingCart shoppingCart);

    /**
     * 查询列表
     */
    List<ShoppingCart> selectList(ShoppingCart shoppingCart);

    /**
     * 新增
     */
    boolean insert(ShoppingCart shoppingCart);

    /**
     * 修改
     */
    boolean update(ShoppingCart shoppingCart);

    /**
     * 批量删除
     */
    boolean deleteByIds(List<Long> ids);

    List<ShoppingCartVo> listByUserId();

    boolean addProductToCart(Long productId);

    boolean insertByUserId(Long productId);

    boolean deleteByUserId(Long productId);

    boolean addCarProductToWishList(Long productId);

    boolean addProduct(Long productId);

    boolean reduceProduct(Long productId);

    boolean deleteByProductId(Long productId);
}
