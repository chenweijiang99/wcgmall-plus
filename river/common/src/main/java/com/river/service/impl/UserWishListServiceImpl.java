package com.river.service.impl;

import java.util.List;

import cn.dev33.satoken.stp.StpUtil;
import com.river.service.ShoppingCartService;
import com.river.vo.userWishList.UserWishListVo;
import org.springframework.stereotype.Service;
import com.river.mapper.UserWishListMapper;
import com.river.entity.UserWishList;
import com.river.service.UserWishListService;
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
public class UserWishListServiceImpl extends ServiceImpl<UserWishListMapper, UserWishList> implements UserWishListService {
    private final ShoppingCartService shoppingCartService;

    /**
     * 查询分页列表
     */
    @Override
    public IPage<UserWishList> selectPage(UserWishList userWishList) {
        LambdaQueryWrapper<UserWishList> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(userWishList.getId() != null, UserWishList::getId, userWishList.getId());
        wrapper.eq(userWishList.getUserId() != null, UserWishList::getUserId, userWishList.getUserId());
        wrapper.eq(userWishList.getProductId() != null, UserWishList::getProductId, userWishList.getProductId());
        wrapper.eq(userWishList.getCreateTime() != null, UserWishList::getCreateTime, userWishList.getCreateTime());
        wrapper.eq(userWishList.getUpdateTime() != null, UserWishList::getUpdateTime, userWishList.getUpdateTime());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询列表
     */
    @Override
    public List<UserWishList> selectList(UserWishList userWishList) {
        LambdaQueryWrapper<UserWishList> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(userWishList.getId() != null, UserWishList::getId, userWishList.getId());
        wrapper.eq(userWishList.getUserId() != null, UserWishList::getUserId, userWishList.getUserId());
        wrapper.eq(userWishList.getProductId() != null, UserWishList::getProductId, userWishList.getProductId());
        wrapper.eq(userWishList.getCreateTime() != null, UserWishList::getCreateTime, userWishList.getCreateTime());
        wrapper.eq(userWishList.getUpdateTime() != null, UserWishList::getUpdateTime, userWishList.getUpdateTime());
        return list(wrapper);
    }

    /**
     * 新增
     */
    @Override
    public boolean insert(UserWishList userWishList) {
        return save(userWishList);
    }

    /**
     * 修改
     */
    @Override
    public boolean update(UserWishList userWishList) {
        return updateById(userWishList);
    }

    /**
     * 批量删除
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public List<UserWishListVo> selectListByUserId() {
        return baseMapper.selectListByUserId(StpUtil.getLoginIdAsLong());
    }

    @Override
    public boolean deleteByProductId(Long productId) {
        LambdaQueryWrapper<UserWishList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWishList::getUserId, StpUtil.getLoginIdAsLong());
        wrapper.eq(UserWishList::getProductId, productId);
        return remove(wrapper);
    }

    @Override
    public boolean addWishListProductToCart(Long productId) {
        // 从心愿单中删除商品
        boolean isDeleted = deleteByProductId(productId);
        if (!isDeleted) {
            return false;
        }
        // 添加到购物车
        return shoppingCartService.addProductToCart(productId);
    }
}
