package com.river.service;

import com.river.entity.ProductCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

/**
 *  商品分类 服务接口
 */
public interface ProductCategoryService extends IService<ProductCategory> {
    /**
     * 查询分页列表
     */
    IPage<ProductCategory> selectPage(ProductCategory productCategory);

    /**
     * 查询列表
     */
    List<ProductCategory> selectList(ProductCategory productCategory);

    /**
     * 新增
     */
    boolean insert(ProductCategory productCategory);

    /**
     * 修改
     */
    boolean update(ProductCategory productCategory);

    /**
     * 批量删除
     */
    boolean deleteByIds(List<Long> ids);
}
