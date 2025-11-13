package com.river.service.impl;

import java.util.List;

import com.river.common.RedisConstants;
import com.river.entity.IndexSlider;
import com.river.utils.RedisUtil;
import org.springframework.stereotype.Service;
import com.river.mapper.ShopSliderMapper;
import com.river.entity.ShopSlider;
import com.river.service.ShopSliderService;
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
public class ShopSliderServiceImpl extends ServiceImpl<ShopSliderMapper, ShopSlider> implements ShopSliderService {
    private final RedisUtil redisUtil;
    /**
     * 查询分页列表
     */
    @Override
    public IPage<ShopSlider> selectPage(ShopSlider shopSlider) {
        LambdaQueryWrapper<ShopSlider> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(shopSlider.getId() != null, ShopSlider::getId, shopSlider.getId());
        wrapper.eq(shopSlider.getUrl() != null, ShopSlider::getUrl, shopSlider.getUrl());
        wrapper.eq(shopSlider.getCreateTime() != null, ShopSlider::getCreateTime, shopSlider.getCreateTime());
        wrapper.eq(shopSlider.getUpdateTime() != null, ShopSlider::getUpdateTime, shopSlider.getUpdateTime());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询列表
     */
    @Override
    public List<ShopSlider> selectList(ShopSlider shopSlider) {
        LambdaQueryWrapper<ShopSlider> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(shopSlider.getId() != null, ShopSlider::getId, shopSlider.getId());
        wrapper.eq(shopSlider.getUrl() != null, ShopSlider::getUrl, shopSlider.getUrl());
        wrapper.eq(shopSlider.getCreateTime() != null, ShopSlider::getCreateTime, shopSlider.getCreateTime());
        wrapper.eq(shopSlider.getUpdateTime() != null, ShopSlider::getUpdateTime, shopSlider.getUpdateTime());
        return list(wrapper);
    }

    /**
     * 新增
     */
    @Override
    public boolean insert(ShopSlider shopSlider) {
        if(redisUtil.hasKey(RedisConstants.SHOP_SLIDER_LIST)){
            redisUtil.delete(RedisConstants.SHOP_SLIDER_LIST);
        }
        save(shopSlider);
        redisUtil.set(RedisConstants.SHOP_SLIDER_LIST, list());
        return true;
    }

    /**
     * 修改
     */
    @Override
    public boolean update(ShopSlider shopSlider) {
        if(redisUtil.hasKey(RedisConstants.SHOP_SLIDER_LIST)){
            redisUtil.delete(RedisConstants.SHOP_SLIDER_LIST);
        }
        updateById(shopSlider);
        redisUtil.set(RedisConstants.SHOP_SLIDER_LIST, list());
        return true;
    }

    /**
     * 批量删除
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        if(redisUtil.hasKey(RedisConstants.SHOP_SLIDER_LIST)){
            redisUtil.delete(RedisConstants.SHOP_SLIDER_LIST);
        }
        removeByIds(ids);
        if(!list().isEmpty()){
            redisUtil.set(RedisConstants.SHOP_SLIDER_LIST, list());
        }
        return true;
    }

    @Override
    public List<ShopSlider> getAll() {
        if(redisUtil.hasKey(RedisConstants.SHOP_SLIDER_LIST)){
            return (List<ShopSlider>) redisUtil.get(RedisConstants.SHOP_SLIDER_LIST);
        }
        return list();
    }
}
