package com.river.mapper;

import com.river.entity.UserWishList;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.river.vo.userWishList.UserWishListVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 *  Mapper接口
 */
@Mapper
public interface UserWishListMapper extends BaseMapper<UserWishList> {
    List<UserWishListVo> selectListByUserId(long userId);
}