package com.river.service.impl;

import java.util.List;

import com.river.entity.ProductOrder;
import org.springframework.stereotype.Service;
import com.river.mapper.ProductOrderMapper;
import com.river.service.ProductOrderService;
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
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrder> implements ProductOrderService {

    /**
     * 查询分页列表
     */
    @Override
    public IPage<ProductOrder> selectPage(ProductOrder productOrder) {
        LambdaQueryWrapper<ProductOrder> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(productOrder.getId() != null, ProductOrder::getId, productOrder.getId());
        wrapper.eq(productOrder.getOrderNumber() != null, ProductOrder::getOrderNumber, productOrder.getOrderNumber());
        wrapper.eq(productOrder.getStatus() != null, ProductOrder::getStatus, productOrder.getStatus());
        wrapper.eq(productOrder.getUserId() != null, ProductOrder::getUserId, productOrder.getUserId());
        wrapper.eq(productOrder.getConsignee() != null, ProductOrder::getConsignee, productOrder.getConsignee());
        wrapper.eq(productOrder.getConsigneeAddress() != null, ProductOrder::getConsigneeAddress, productOrder.getConsigneeAddress());
        wrapper.eq(productOrder.getConsigneePhone() != null, ProductOrder::getConsigneePhone, productOrder.getConsigneePhone());
        wrapper.eq(productOrder.getEmail() != null, ProductOrder::getEmail, productOrder.getEmail());
        wrapper.eq(productOrder.getPayMethod() != null, ProductOrder::getPayMethod, productOrder.getPayMethod());
        wrapper.eq(productOrder.getPayStatus() != null, ProductOrder::getPayStatus, productOrder.getPayStatus());
        wrapper.eq(productOrder.getAmount() != null, ProductOrder::getAmount, productOrder.getAmount());
        wrapper.eq(productOrder.getOrderTime() != null, ProductOrder::getOrderTime, productOrder.getOrderTime());
        wrapper.eq(productOrder.getCheckoutTime() != null, ProductOrder::getCheckoutTime, productOrder.getCheckoutTime());
        wrapper.eq(productOrder.getCreateTime() != null, ProductOrder::getCreateTime, productOrder.getCreateTime());
        wrapper.eq(productOrder.getUpdateTime() != null, ProductOrder::getUpdateTime, productOrder.getUpdateTime());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询列表
     */
    @Override
    public List<ProductOrder> selectList(ProductOrder productOrder) {
        LambdaQueryWrapper<ProductOrder> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(productOrder.getId() != null, ProductOrder::getId, productOrder.getId());
        wrapper.eq(productOrder.getOrderNumber() != null, ProductOrder::getOrderNumber, productOrder.getOrderNumber());
        wrapper.eq(productOrder.getStatus() != null, ProductOrder::getStatus, productOrder.getStatus());
        wrapper.eq(productOrder.getUserId() != null, ProductOrder::getUserId, productOrder.getUserId());
        wrapper.eq(productOrder.getConsignee() != null, ProductOrder::getConsignee, productOrder.getConsignee());
        wrapper.eq(productOrder.getConsigneeAddress() != null, ProductOrder::getConsigneeAddress, productOrder.getConsigneeAddress());
        wrapper.eq(productOrder.getConsigneePhone() != null, ProductOrder::getConsigneePhone, productOrder.getConsigneePhone());
        wrapper.eq(productOrder.getEmail() != null, ProductOrder::getEmail, productOrder.getEmail());
        wrapper.eq(productOrder.getPayMethod() != null, ProductOrder::getPayMethod, productOrder.getPayMethod());
        wrapper.eq(productOrder.getPayStatus() != null, ProductOrder::getPayStatus, productOrder.getPayStatus());
        wrapper.eq(productOrder.getAmount() != null, ProductOrder::getAmount, productOrder.getAmount());
        wrapper.eq(productOrder.getOrderTime() != null, ProductOrder::getOrderTime, productOrder.getOrderTime());
        wrapper.eq(productOrder.getCheckoutTime() != null, ProductOrder::getCheckoutTime, productOrder.getCheckoutTime());
        wrapper.eq(productOrder.getCreateTime() != null, ProductOrder::getCreateTime, productOrder.getCreateTime());
        wrapper.eq(productOrder.getUpdateTime() != null, ProductOrder::getUpdateTime, productOrder.getUpdateTime());
        return list(wrapper);
    }

    /**
     * 新增
     */
    @Override
    public boolean insert(ProductOrder productOrder) {
        return save(productOrder);
    }

    /**
     * 修改
     */
    @Override
    public boolean update(ProductOrder productOrder) {
        return updateById(productOrder);
    }

    /**
     * 批量删除
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }
}
