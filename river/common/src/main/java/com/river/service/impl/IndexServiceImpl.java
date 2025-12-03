package com.river.service.impl;

import com.river.mapper.*;
import com.river.service.IndexService;
import com.river.vo.dashboard.IndexVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductBrandMapper productBrandMapper;
    private final ProductOrderMapper productOrderMapper;
    private final BlogMapper blogMapper;
    private final CommentsMapper commentsMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final SysMenuMapper sysMenuMapper;
    private final OrderDetailMapper orderDetailMapper;

    @Override
    public IndexVo index() {
        // 基础数据统计
        Long userCount = sysUserMapper.selectCount(null);
        Long roleCount = sysRoleMapper.selectCount(null);
        Long productCount = productMapper.selectCount(null);
        Long categoryCount = productCategoryMapper.selectCount(null);
        Long brandCount = productBrandMapper.selectCount(null);
        Long orderCount = productOrderMapper.selectCount(null);
        Long blogCount = blogMapper.selectCount(null);
        Long commentCount = commentsMapper.selectCount(null);
        Long cartCount = shoppingCartMapper.selectCount(null);
        Long menuCount = sysMenuMapper.selectCount(null);

        // 真实数据查询 - 用户增长趋势
        List<Map<String, Object>> userGrowth = sysUserMapper.selectUserGrowth();

        // 真实数据查询 - 订单趋势
        List<Map<String, Object>> orderGrowth = productOrderMapper.selectOrderGrowth();

        // 真实数据查询 - 商品分类分布
        List<Map<String, Object>> categoryDistribution = productCategoryMapper.selectCategoryDistribution();

        // 真实数据查询 - 最新订单
        List<Map<String, Object>> latestOrders = productOrderMapper.selectLatestOrders(10);

        // 真实数据查询 - 最新商品
        List<Map<String, Object>> latestProducts = productMapper.selectLatestProducts(10);

        // 数据概览查询
        // 1. 总销售额
        Double totalSales = productOrderMapper.selectTotalSales();
        // 2. 待付款订单数量
        Long pendingOrders = productOrderMapper.selectPendingPayments();
        // 3. 今日新增用户
        Long todayNewUsers = sysUserMapper.selectTodayNewUsers();
        // 4. 上架商品数量
        Long activeProducts = productMapper.selectActiveProducts();

        // 确保数据完整性，处理可能的空数据
        if (userGrowth == null) userGrowth = new ArrayList<>();
        if (orderGrowth == null) orderGrowth = new ArrayList<>();
        if (categoryDistribution == null) categoryDistribution = new ArrayList<>();
        if (latestOrders == null) latestOrders = new ArrayList<>();
        if (latestProducts == null) latestProducts = new ArrayList<>();

        return IndexVo.builder()
                .userCount(userCount)
                .roleCount(roleCount)
                .productCount(productCount)
                .categoryCount(categoryCount)
                .brandCount(brandCount)
                .orderCount(orderCount)
                .blogCount(blogCount)
                .commentCount(commentCount)
                .cartCount(cartCount)
                .menuCount(menuCount)
                .userGrowth(userGrowth)
                .orderGrowth(orderGrowth)
                .categoryDistribution(categoryDistribution)
                .latestOrders(latestOrders)
                .latestProducts(latestProducts)
                .totalSales(totalSales)
                .pendingOrders(pendingOrders)
                .todayNewUsers(todayNewUsers)
                .activeProducts(activeProducts)
                .build();
    }

}
