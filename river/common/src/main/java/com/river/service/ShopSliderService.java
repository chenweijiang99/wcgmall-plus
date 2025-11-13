package com.river.service;

import com.river.entity.IndexSlider;
import com.river.entity.ShopSlider;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

/**
 *  服务接口
 */
public interface ShopSliderService extends IService<ShopSlider> {
    /**
     * 查询分页列表
     */
    IPage<ShopSlider> selectPage(ShopSlider shopSlider);

    /**
     * 查询列表
     */
    List<ShopSlider> selectList(ShopSlider shopSlider);

    /**
     * 新增
     */
    boolean insert(ShopSlider shopSlider);

    /**
     * 修改
     */
    boolean update(ShopSlider shopSlider);

    /**
     * 批量删除
     */
    boolean deleteByIds(List<Long> ids);

    /**
     * 获取全部
     */
    List<ShopSlider> getAll();
}
