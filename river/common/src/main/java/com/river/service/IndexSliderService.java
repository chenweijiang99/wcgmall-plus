package com.river.service;

import com.river.entity.IndexSlider;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

/**
 *  服务接口
 */
public interface IndexSliderService extends IService<IndexSlider> {
    /**
     * 查询分页列表
     */
    IPage<IndexSlider> selectPage(IndexSlider indexSlider);

    /**
     * 查询列表
     */
    List<IndexSlider> selectList(IndexSlider indexSlider);

    /**
     * 新增
     */
    boolean insert(IndexSlider indexSlider);

    /**
     * 修改
     */
    boolean update(IndexSlider indexSlider);

    /**
     * 批量删除
     */
    boolean deleteByIds(List<Long> ids);

    /**
     * 获取全部
     */
    List<IndexSlider> getAll();
}
