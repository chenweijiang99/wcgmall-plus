package com.river.mapper;

import com.river.entity.ShoppingCart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.river.vo.shoppingCart.ShoppingCartVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 *  Mapper接口
 */
@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
    List<ShoppingCartVo> listByUserId(long loginIdAsLong);
}