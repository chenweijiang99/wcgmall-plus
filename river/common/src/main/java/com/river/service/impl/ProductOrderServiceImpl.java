package com.river.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import cn.dev33.satoken.stp.StpUtil;
import com.river.dto.OrderSubmitDTO;
import com.river.entity.*;
import com.river.exception.ServiceException;
import com.river.mapper.*;
import org.springframework.stereotype.Service;
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
    private final AddressBookMapper addressBookMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final ProductMapper productMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final SysUserMapper sysUserMapper;

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

    @Override
    public String submitOrder(OrderSubmitDTO submitDTO) {
        String orderNo = System.currentTimeMillis()+ StpUtil.getLoginIdAsString();
        SysUser sysUser = sysUserMapper.selectById(StpUtil.getLoginIdAsLong());
        if (sysUser == null) {
            throw new ServiceException("用户不存在");
        }

        // 核心逻辑：
        // 校验地址是否存在
        AddressBook addressBook = addressBookMapper.selectById(submitDTO.getAddressId());
        if (addressBook == null) {
            throw new ServiceException("地址不存在");
        }
        // 校验购物车商品是否存在
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectBatchIds(submitDTO.getCartItemIds());
        if (shoppingCartList.isEmpty()) {
            throw new ServiceException("购物车商品不存在");
        }
        // 校验商品库存是否充足,并计算总计,添加订单详情,减库存
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (ShoppingCart shoppingCart : shoppingCartList) {
            if(shoppingCart.getProductId() == null){
                throw new ServiceException("商品ID不存在");
            }
            if (shoppingCart.getUserId() != StpUtil.getLoginIdAsLong() || shoppingCart.getUserId() == null) {
                throw new ServiceException("用户不存在");
            }
            Product product = productMapper.selectById(shoppingCart.getProductId());
            if (product == null) {
                throw new ServiceException("商品不存在");
            }
            if (product.getInventory() < shoppingCart.getNumber()) {
                throw new ServiceException("商品库存不足");
            }
            // 计算总计金额
            totalPrice = totalPrice.add(product.getPrice().multiply(new BigDecimal(shoppingCart.getNumber())));
            // 将购物车数据添加到订单详情
            OrderDetail orderDetail = OrderDetail.builder()
                    .orderNumber(orderNo)
                    .productId(product.getId())
                    .productNumber(shoppingCart.getNumber())
                    .build();
            orderDetailMapper.insert(orderDetail);
            product.setInventory(product.getInventory() - shoppingCart.getNumber());
            productMapper.updateById(product);
        }

        // 生成订单，保存数据库
        ProductOrder productOrder = ProductOrder.builder()
                .orderNumber(orderNo)
                .userId(Long.parseLong(StpUtil.getLoginIdAsString()))
                .consignee(addressBook.getConsignee())
                .consigneeAddress(addressBook.getConsigneeAddress())
                .consigneePhone(addressBook.getConsigneePhone())
                .email(sysUser.getEmail())
                .status(0)
                .amount(totalPrice)
                .orderTime(LocalDateTime.now())
                .payStatus(0)
                .build();
        save(productOrder);

        // 清空购物车中对应的商品
        shoppingCartMapper.deleteBatchIds(submitDTO.getCartItemIds());
        return orderNo;
    }
}
