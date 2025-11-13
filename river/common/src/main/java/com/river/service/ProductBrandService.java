package com.river.service;

import com.river.entity.ProductBrand;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

/**
 *  服务接口
 */
public interface ProductBrandService extends IService<ProductBrand> {
    /**
     * 查询分页列表
     */
    IPage<ProductBrand> selectPage(ProductBrand productBrand);

    /**
     * 查询列表
     */
    List<ProductBrand> selectList(ProductBrand productBrand);

    /**
     * 新增
     */
    boolean insert(ProductBrand productBrand);

    /**
     * 修改
     */
    boolean update(ProductBrand productBrand);

    /**
     * 批量删除
     */
    boolean deleteByIds(List<Long> ids);
}
