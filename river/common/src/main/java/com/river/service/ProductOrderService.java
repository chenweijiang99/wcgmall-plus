package com.river.service;

import com.river.entity.ProductOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 *  服务接口
 */
public interface ProductOrderService extends IService<ProductOrder> {
    /**
     * 查询分页列表
     */
    IPage<ProductOrder> selectPage(ProductOrder productOrder);

    /**
     * 查询列表
     */
    List<ProductOrder> selectList(ProductOrder productOrder);

    /**
     * 新增
     */
    boolean insert(ProductOrder productOrder);

    /**
     * 修改
     */
    boolean update(ProductOrder productOrder);

    /**
     * 批量删除
     */
    boolean deleteByIds(List<Long> ids);
}
