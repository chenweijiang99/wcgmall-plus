package com.river.service.impl;

import java.util.List;

import cn.dev33.satoken.stp.StpUtil;
import com.river.entity.Product;
import com.river.entity.UserWishList;
import com.river.exception.ServiceException;
import com.river.mapper.ProductMapper;
import com.river.mapper.UserWishListMapper;
import com.river.vo.shoppingCart.ShoppingCartVo;
import com.river.vo.userWishList.UserWishListVo;
import org.springframework.stereotype.Service;
import com.river.mapper.ShoppingCartMapper;
import com.river.entity.ShoppingCart;
import com.river.service.ShoppingCartService;
import com.river.utils.PageUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;

/**
 *  服务实现类
 */
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    private final ProductMapper productMapper;
    private final UserWishListMapper userWishListMapper;
    /**
     * 查询分页列表
     */
    @Override
    public IPage<ShoppingCart> selectPage(ShoppingCart shoppingCart) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(shoppingCart.getId() != null, ShoppingCart::getId, shoppingCart.getId());
        wrapper.eq(shoppingCart.getProductId() != null, ShoppingCart::getProductId, shoppingCart.getProductId());
        wrapper.eq(shoppingCart.getUserId() != null, ShoppingCart::getUserId, shoppingCart.getUserId());
        wrapper.eq(shoppingCart.getNumber() != null, ShoppingCart::getNumber, shoppingCart.getNumber());
        wrapper.eq(shoppingCart.getCreateTime() != null, ShoppingCart::getCreateTime, shoppingCart.getCreateTime());
        wrapper.eq(shoppingCart.getUpdateTime() != null, ShoppingCart::getUpdateTime, shoppingCart.getUpdateTime());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询列表
     */
    @Override
    public List<ShoppingCart> selectList(ShoppingCart shoppingCart) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(shoppingCart.getId() != null, ShoppingCart::getId, shoppingCart.getId());
        wrapper.eq(shoppingCart.getProductId() != null, ShoppingCart::getProductId, shoppingCart.getProductId());
        wrapper.eq(shoppingCart.getUserId() != null, ShoppingCart::getUserId, shoppingCart.getUserId());
        wrapper.eq(shoppingCart.getNumber() != null, ShoppingCart::getNumber, shoppingCart.getNumber());
        wrapper.eq(shoppingCart.getCreateTime() != null, ShoppingCart::getCreateTime, shoppingCart.getCreateTime());
        wrapper.eq(shoppingCart.getUpdateTime() != null, ShoppingCart::getUpdateTime, shoppingCart.getUpdateTime());
        return list(wrapper);
    }

    /**
     * 新增
     */
    @Override
    public boolean insert(ShoppingCart shoppingCart) {
        return save(shoppingCart);
    }

    /**
     * 修改
     */
    @Override
    public boolean update(ShoppingCart shoppingCart) {
        return updateById(shoppingCart);
    }

    /**
     * 批量删除
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public List<ShoppingCartVo> listByUserId() {
        return baseMapper.listByUserId(StpUtil.getLoginIdAsLong());
    }

    @Override
    public boolean addProductToCart(Long productId) {
        ShoppingCart shoppingCart = baseMapper.selectOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, StpUtil.getLoginIdAsLong())
                .eq(ShoppingCart::getProductId, productId));
        Product product = productMapper.selectById(productId);
        if (shoppingCart == null) {
            if(product == null ){
                throw new ServiceException("商品不存在");
            }
            if(product.getInventory() <= 1){
                throw new ServiceException("商品库存不足");
            }
            ShoppingCart addShoppingCart = ShoppingCart.builder()
                    .userId(StpUtil.getLoginIdAsLong())
                    .productId(productId)
                    .number(1)
                    .build();
            insert(addShoppingCart);
        } else {
            if(product.getInventory() <= shoppingCart.getNumber()){
                throw new ServiceException("商品库存不足");
            }
            shoppingCart.setNumber(shoppingCart.getNumber() + 1);
            update(shoppingCart);
        }
        return true;
    }

    @Override
    public boolean insertByUserId(Long productId) {
        ShoppingCart shoppingCart = baseMapper.selectOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, StpUtil.getLoginIdAsLong())
                .eq(ShoppingCart::getProductId, productId));
        Product product = productMapper.selectById(productId);
        if (shoppingCart == null) {
            if(product == null ){
                throw new ServiceException("商品不存在");
            }
            if(product.getInventory() <= 1){
                throw new ServiceException("商品库存不足");
            }
            ShoppingCart addShoppingCart = ShoppingCart.builder()
                    .userId(StpUtil.getLoginIdAsLong())
                    .productId(productId)
                    .number(1)
                    .build();
            insert(addShoppingCart);
        }else {
            if(product.getInventory() <= shoppingCart.getNumber()){
                throw new ServiceException("商品库存不足");
            }
            shoppingCart.setNumber(shoppingCart.getNumber() + 1);
            update(shoppingCart);
        }
        return true;
    }

    @Override
    public boolean deleteByUserId(Long productId) {
        ShoppingCart shoppingCart = baseMapper.selectOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, StpUtil.getLoginIdAsLong())
                .eq(ShoppingCart::getProductId, productId));
        if (shoppingCart == null) {
            throw new ServiceException("购物车中没有该商品");
        }
        if (shoppingCart.getNumber() > 1) {
            shoppingCart.setNumber(shoppingCart.getNumber() - 1);
            update(shoppingCart);
        } else {
            baseMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                    .eq(ShoppingCart::getUserId, StpUtil.getLoginIdAsLong())
                    .eq(ShoppingCart::getProductId, productId));
        }
        return true;
    }

    @Override
    public boolean addCarProductToWishList(Long productId) {
        ShoppingCart shoppingCart = baseMapper.selectOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, StpUtil.getLoginIdAsLong())
                .eq(ShoppingCart::getProductId, productId));
        if (shoppingCart == null) {
            throw new ServiceException("商品不存在");
        }
        List<UserWishListVo> userWishListVos = userWishListMapper.selectListByUserId(StpUtil.getLoginIdAsLong());
        userWishListVos.forEach(userWishListVo -> {
            if (userWishListVo.getProductId().equals(productId)) {
                throw new ServiceException("商品已添加到收藏夹");
            }
        });
        UserWishList userWishList = UserWishList.builder()
                .userId(StpUtil.getLoginIdAsLong())
                .productId(productId)
                .build();
        userWishListMapper.insert(userWishList);
        baseMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, StpUtil.getLoginIdAsLong())
                .eq(ShoppingCart::getProductId, productId));
        return true;
    }

    @Override
    public boolean addProduct(Long productId) {
        ShoppingCart shoppingCart = baseMapper.selectOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, StpUtil.getLoginIdAsLong())
                .eq(ShoppingCart::getProductId, productId));
        if (shoppingCart != null) {
            shoppingCart.setNumber(shoppingCart.getNumber() + 1);
            baseMapper.updateById(shoppingCart);
        }else {
            throw new ServiceException("商品不存在");
        }
        return true;
    }

    @Override
    public boolean reduceProduct(Long productId) {
        ShoppingCart shoppingCart = baseMapper.selectOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, StpUtil.getLoginIdAsLong())
                .eq(ShoppingCart::getProductId, productId));
        if (shoppingCart != null) {
            if (shoppingCart.getNumber() <= 1) {
                baseMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                        .eq(ShoppingCart::getUserId, StpUtil.getLoginIdAsLong())
                        .eq(ShoppingCart::getProductId, productId));
            } else {
                shoppingCart.setNumber(shoppingCart.getNumber() - 1);
                baseMapper.updateById(shoppingCart);
            }
        } else {
            throw new ServiceException("商品不存在");
        }
        return false;
    }

    @Override
    public boolean deleteByProductId(Long productId) {
        baseMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, StpUtil.getLoginIdAsLong())
                .eq(ShoppingCart::getProductId, productId));
        return true;
    }
}
