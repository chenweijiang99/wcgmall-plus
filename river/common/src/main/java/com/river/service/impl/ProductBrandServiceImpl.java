package com.river.service.impl;

import java.util.List;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Service;
import com.river.mapper.ProductBrandMapper;
import com.river.entity.ProductBrand;
import com.river.service.ProductBrandService;
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
public class ProductBrandServiceImpl extends ServiceImpl<ProductBrandMapper, ProductBrand> implements ProductBrandService {

    /**
     * 查询分页列表
     */
    @Override
    public IPage<ProductBrand> selectPage(ProductBrand productBrand) {
        LambdaQueryWrapper<ProductBrand> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.like(productBrand.getName() != null, ProductBrand::getName, productBrand.getName());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询列表
     */
    @Override
    public List<ProductBrand> selectList(ProductBrand productBrand) {
        LambdaQueryWrapper<ProductBrand> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.like(productBrand.getName() != null, ProductBrand::getName, productBrand.getName());
        return list(wrapper);
    }

    /**
     * 新增
     */
    @Override
    public boolean insert(ProductBrand productBrand) {
        productBrand.setUserId(StpUtil.getLoginIdAsLong());
        return save(productBrand);
    }

    /**
     * 修改
     */
    @Override
    public boolean update(ProductBrand productBrand) {
        return updateById(productBrand);
    }

    /**
     * 批量删除
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }
}
