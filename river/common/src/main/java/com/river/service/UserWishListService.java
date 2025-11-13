package com.river.service;

import com.river.entity.UserWishList;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.vo.userWishList.UserWishListVo;

import java.util.List;

/**
 *  服务接口
 */
public interface UserWishListService extends IService<UserWishList> {
    /**
     * 查询分页列表
     */
    IPage<UserWishList> selectPage(UserWishList userWishList);

    /**
     * 查询列表
     */
    List<UserWishList> selectList(UserWishList userWishList);

    /**
     * 新增
     */
    boolean insert(UserWishList userWishList);

    /**
     * 修改
     */
    boolean update(UserWishList userWishList);

    /**
     * 批量删除
     */
    boolean deleteByIds(List<Long> ids);

    List<UserWishListVo> selectListByUserId();

    boolean deleteByProductId(Long productId);

    boolean addWishListProductToCart(Long productId);
}
