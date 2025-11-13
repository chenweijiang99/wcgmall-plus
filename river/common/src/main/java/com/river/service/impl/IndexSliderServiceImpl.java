package com.river.service.impl;

import java.util.List;

import com.river.common.RedisConstants;
import com.river.utils.RedisUtil;
import org.springframework.stereotype.Service;
import com.river.mapper.IndexSliderMapper;
import com.river.entity.IndexSlider;
import com.river.service.IndexSliderService;
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
public class IndexSliderServiceImpl extends ServiceImpl<IndexSliderMapper, IndexSlider> implements IndexSliderService {

    private final RedisUtil redisUtil;

    /**
     * 查询分页列表
     */
    @Override
    public IPage<IndexSlider> selectPage(IndexSlider indexSlider) {
        LambdaQueryWrapper<IndexSlider> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
//        wrapper.eq(indexSlider.getId() != null, IndexSlider::getId, indexSlider.getId());
//        wrapper.eq(indexSlider.getUrl() != null, IndexSlider::getUrl, indexSlider.getUrl());
//        wrapper.eq(indexSlider.getCreateTime() != null, IndexSlider::getCreateTime, indexSlider.getCreateTime());
//        wrapper.eq(indexSlider.getUpdateTime() != null, IndexSlider::getUpdateTime, indexSlider.getUpdateTime());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询列表
     */
    @Override
    public List<IndexSlider> selectList(IndexSlider indexSlider) {
        LambdaQueryWrapper<IndexSlider> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(indexSlider.getId() != null, IndexSlider::getId, indexSlider.getId());
        wrapper.eq(indexSlider.getUrl() != null, IndexSlider::getUrl, indexSlider.getUrl());
        wrapper.eq(indexSlider.getCreateTime() != null, IndexSlider::getCreateTime, indexSlider.getCreateTime());
        wrapper.eq(indexSlider.getUpdateTime() != null, IndexSlider::getUpdateTime, indexSlider.getUpdateTime());
        return list(wrapper);
    }

    /**
     * 新增
     */
    @Override
    public boolean insert(IndexSlider indexSlider) {
        if(redisUtil.hasKey(RedisConstants.INDEX_SLIDER_LIST)){
            redisUtil.delete(RedisConstants.INDEX_SLIDER_LIST);
        }
        save(indexSlider);
        redisUtil.set(RedisConstants.INDEX_SLIDER_LIST, list());
        return true;
    }

    /**
     * 修改
     */
    @Override
    public boolean update(IndexSlider indexSlider) {
        if(redisUtil.hasKey(RedisConstants.INDEX_SLIDER_LIST)){
            redisUtil.delete(RedisConstants.INDEX_SLIDER_LIST);
        }
        updateById(indexSlider);
        redisUtil.set(RedisConstants.INDEX_SLIDER_LIST, list());
        return true;
    }

    /**
     * 批量删除
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        if(redisUtil.hasKey(RedisConstants.INDEX_SLIDER_LIST)){
            redisUtil.delete(RedisConstants.INDEX_SLIDER_LIST);
        }
        removeByIds(ids);
        if(!list().isEmpty()){
            redisUtil.set(RedisConstants.INDEX_SLIDER_LIST, list());
        }
        return true;
    }

    @Override
    public List<IndexSlider> getAll() {
        if(redisUtil.hasKey(RedisConstants.INDEX_SLIDER_LIST)){
           return (List<IndexSlider>) redisUtil.get(RedisConstants.INDEX_SLIDER_LIST);
        }
        return list();
    }
}
