package com.river.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.river.mapper.ProductCategoryMapper;
import com.river.entity.ProductCategory;
import com.river.service.ProductCategoryService;
import com.river.utils.PageUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;

/**
 *  商品分类 服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    /**
     * 查询分页列表
     */
    @Override
    public IPage<ProductCategory> selectPage(ProductCategory productCategory) {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.like(productCategory.getName() != null, ProductCategory::getName, productCategory.getName());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询列表
     */
    @Override
    public List<ProductCategory> selectList(ProductCategory productCategory) {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.like(productCategory.getName() != null, ProductCategory::getName, productCategory.getName());
        return list(wrapper);
    }

    /**
     * 新增
     */
    @Override
    public boolean insert(ProductCategory productCategory) {
        return save(productCategory);
    }

    /**
     * 修改
     */
    @Override
    public boolean update(ProductCategory productCategory) {
        return updateById(productCategory);
    }

    /**
     * 批量删除
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }
}
