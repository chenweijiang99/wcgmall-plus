package com.river.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.river.dto.ProductReviewDTO;
import com.river.entity.OrderDetail;
import com.river.entity.Product;
import com.river.entity.ProductOrder;
import com.river.entity.ProductReview;
import com.river.entity.SysUser;
import com.river.exception.ServiceException;
import com.river.mapper.OrderDetailMapper;
import com.river.mapper.ProductMapper;
import com.river.mapper.ProductOrderMapper;
import com.river.mapper.ProductReviewMapper;
import com.river.mapper.SysUserMapper;
import com.river.service.ProductReviewService;
import com.river.vo.ProductReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.river.vo.ReviewStatisticsVO;

import com.river.utils.SensitiveWordFilter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl extends ServiceImpl<ProductReviewMapper, ProductReview> implements ProductReviewService {
    private final SysUserMapper sysUserMapper;
    private final ProductMapper productMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final ProductOrderMapper productOrderMapper;

    @Override
    public boolean addReview(ProductReviewDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 敏感词过滤
        if (dto.getContent() != null && SensitiveWordFilter.containsSensitiveWord(dto.getContent())) {
            List<String> sensitiveWords = SensitiveWordFilter.getSensitiveWords(dto.getContent());
            throw new ServiceException("评价内容包含敏感词：" + String.join("、", sensitiveWords));
        }

        // 检查是否已评价（仅对根评价检查，回复不限制）
        if (dto.getParentReviewId() == null) {
            LambdaQueryWrapper<ProductReview> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(ProductReview::getOrderNumber, dto.getOrderNumber())
                    .eq(ProductReview::getProductId, dto.getProductId())
                    .eq(ProductReview::getUserId, userId)
                    .isNull(ProductReview::getParentReviewId);
            Long existCount = count(checkWrapper);
            if (existCount > 0) {
                throw new ServiceException("该商品已评价，不能重复评价");
            }
        }

        // 构建ProductReview实体
        ProductReview review = ProductReview.builder()
                .orderNumber(dto.getOrderNumber())
                .productId(dto.getProductId())
                .userId(userId)
                .productScore(dto.getProductScore())
                .logisticsScore(dto.getLogisticsScore())
                .merchantScore(dto.getMerchantScore())
                .content(dto.getContent())
                .images(dto.getImageList() != null ? String.join(",", dto.getImageList()) : "")
                .parentReviewId(dto.getParentReviewId())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 插入评价
        boolean success = save(review);
        if (!success) {
            throw new ServiceException("评价失败");
        }

        // 处理树形结构
        if (dto.getParentReviewId() != null) {
            // 子评价：继承父评价的rootReviewId
            ProductReview parent = getById(dto.getParentReviewId());
            if (parent == null) {
                throw new ServiceException("父评价不存在");
            }
            review.setRootReviewId(parent.getRootReviewId());
        } else {
            // 根评价：rootReviewId指向自己
            review.setRootReviewId(review.getId());
        }

        updateById(review);

        // 检查订单是否所有商品都已评价，如果是则更新订单状态为已完成
        if (dto.getParentReviewId() == null && dto.getOrderNumber() != null) {
            checkAndUpdateOrderStatus(dto.getOrderNumber(), userId);
        }

        return true;
    }

    /**
     * 检查订单是否所有商品都已评价，如果是则更新订单状态为已完成
     */
    private void checkAndUpdateOrderStatus(String orderNumber, Long userId) {
        // 获取订单详情中的所有商品
        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(OrderDetail::getOrderNumber, orderNumber);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(detailWrapper);

        // 检查每个商品是否都已评价
        boolean allReviewed = true;
        for (OrderDetail detail : orderDetails) {
            LambdaQueryWrapper<ProductReview> reviewWrapper = new LambdaQueryWrapper<>();
            reviewWrapper.eq(ProductReview::getOrderNumber, orderNumber)
                    .eq(ProductReview::getProductId, detail.getProductId())
                    .eq(ProductReview::getUserId, userId)
                    .isNull(ProductReview::getParentReviewId);
            Long reviewCount = count(reviewWrapper);
            if (reviewCount == 0) {
                allReviewed = false;
                break;
            }
        }

        // 如果所有商品都已评价，更新订单状态为已完成
        if (allReviewed) {
            LambdaQueryWrapper<ProductOrder> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(ProductOrder::getOrderNumber, orderNumber);
            ProductOrder order = productOrderMapper.selectOne(orderWrapper);
            if (order != null && order.getStatus() == 3) { // 待评价状态
                order.setStatus(4); // 已完成
                productOrderMapper.updateById(order);
            }
        }
    }

    @Override
    public IPage<ProductReviewVO> selectReviewTree(Long productId, Integer pageNum, Integer pageSize) {
        Page<ProductReviewVO> page = new Page<>(pageNum, pageSize);

        // 查询根评价
        List<ProductReviewVO> rootReviews = selectRootReviews(productId);

        // 为每个根评价加载子评价
        for (ProductReviewVO root : rootReviews) {
            List<ProductReviewVO> children = selectChildrenByRootId(root.getId());
            root.setChildren(children);
        }

        page.setRecords(rootReviews);
        page.setTotal(rootReviews.size());
        return page;
    }

    @Override
    public Long selectReviewCount(Long productId) {
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductReview::getProductId, productId)
                .isNull(ProductReview::getParentReviewId);  // 只统计根评价，不统计回复
        return count(wrapper);
    }

    @Override
    public boolean deleteReview(Long id) {
        ProductReview review = getById(id);
        if (review == null) {
            throw new ServiceException("评价不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!review.getUserId().equals(userId)) {
            throw new ServiceException("只能删除自己的评价");
        }

        return removeById(id);
    }

    @Override
    public List<ProductReviewVO> selectPendingReviewProducts(Long userId) {
        // 查询用户已确认收货但未评价的订单商品
        List<ProductReviewVO> result = new ArrayList<>();

        // 查询已确认的订单详情
        LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDetail::getConfirmStatus, 1); // 已确认收货
        List<OrderDetail> confirmedDetails = orderDetailMapper.selectList(wrapper);

        for (OrderDetail detail : confirmedDetails) {
            // 检查是否已评价
            LambdaQueryWrapper<ProductReview> reviewWrapper = new LambdaQueryWrapper<>();
            reviewWrapper.eq(ProductReview::getOrderNumber, detail.getOrderNumber())
                    .eq(ProductReview::getProductId, detail.getProductId())
                    .eq(ProductReview::getUserId, userId)
                    .isNull(ProductReview::getParentReviewId); // 只查根评价
            Long count = count(reviewWrapper);

            if (count == 0) {
                // 未评价，加入待评价列表
                Product product = productMapper.selectById(detail.getProductId());
                ProductReviewVO vo = ProductReviewVO.builder()
                        .orderNumber(detail.getOrderNumber())
                        .productId(detail.getProductId())
                        .productName(product != null ? product.getName() : "")
                        .productImage(product != null ? product.getImage() : "")
                        .build();
                result.add(vo);
            }
        }

        return result;
    }

    @Override
    public List<ProductReviewVO> selectRootReviews(Long productId) {
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductReview::getProductId, productId)
                .isNull(ProductReview::getParentReviewId)
                .orderByDesc(ProductReview::getCreateTime);

        List<ProductReview> reviews = list(wrapper);
        return reviews.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<ProductReviewVO> selectChildrenByRootId(Long rootId) {
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductReview::getRootReviewId, rootId)
                .isNotNull(ProductReview::getParentReviewId)
                .orderByAsc(ProductReview::getCreateTime);

        List<ProductReview> reviews = list(wrapper);
        return reviews.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 转换为VO
     */
    private ProductReviewVO convertToVO(ProductReview review) {
        SysUser user = sysUserMapper.selectById(review.getUserId());
        Product product = productMapper.selectById(review.getProductId());

        // 处理图片URL列表
        List<String> imageList = new ArrayList<>();
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            imageList = Arrays.asList(review.getImages().split(","));
        }

        // 获取父评价用户名
        String parentUsername = null;
        if (review.getParentReviewId() != null) {
            ProductReview parent = getById(review.getParentReviewId());
            if (parent != null) {
                SysUser parentUser = sysUserMapper.selectById(parent.getUserId());
                parentUsername = parentUser != null ? parentUser.getUsername() : "";
            }
        }

        // 获取管理员名称
        String adminName = null;
        if (review.getIsAdmin() != null && review.getIsAdmin() == 1 && review.getAdminId() != null) {
            SysUser admin = sysUserMapper.selectById(review.getAdminId());
            adminName = admin != null ? admin.getUsername() : "管理员";
        }

        return ProductReviewVO.builder()
                .id(review.getId())
                .orderNumber(review.getOrderNumber())
                .productId(review.getProductId())
                .productName(product != null ? product.getName() : "")
                .productImage(product != null ? product.getImage() : "")
                .userId(review.getUserId())
                .username(user != null ? user.getUsername() : "")
                .userAvatar(user != null ? user.getAvatar() : "")
                .productScore(review.getProductScore())
                .logisticsScore(review.getLogisticsScore())
                .merchantScore(review.getMerchantScore())
                .content(review.getContent())
                .imageList(imageList)
                .parentReviewId(review.getParentReviewId())
                .parentUsername(parentUsername)
                .rootReviewId(review.getRootReviewId())
                .isAdmin(review.getIsAdmin())
                .adminId(review.getAdminId())
                .adminName(adminName)
                .createTime(review.getCreateTime())
                .updateTime(review.getUpdateTime())
                .build();
    }

    // ========== 后台管理方法实现 ==========

    @Override
    public IPage<ProductReviewVO> adminSelectPage(ProductReview query, Integer pageNum, Integer pageSize) {
        Page<ProductReview> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        // 只查询根评价
        wrapper.isNull(ProductReview::getParentReviewId);
        // 条件查询
        if (query.getProductId() != null) {
            wrapper.eq(ProductReview::getProductId, query.getProductId());
        }
        if (query.getUserId() != null) {
            wrapper.eq(ProductReview::getUserId, query.getUserId());
        }
        if (query.getProductScore() != null) {
            wrapper.eq(ProductReview::getProductScore, query.getProductScore());
        }
        wrapper.orderByDesc(ProductReview::getCreateTime);

        IPage<ProductReview> reviewPage = page(page, wrapper);

        // 转换为VO并加载子评价
        Page<ProductReviewVO> voPage = new Page<>(pageNum, pageSize);
        List<ProductReviewVO> voList = reviewPage.getRecords().stream().map(review -> {
            ProductReviewVO vo = convertToVO(review);
            // 加载子评价(回复)
            List<ProductReviewVO> children = selectChildrenByRootId(review.getId());
            vo.setChildren(children);
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        voPage.setTotal(reviewPage.getTotal());
        return voPage;
    }

    @Override
    public boolean adminReply(Long parentReviewId, String content, Long adminId) {
        ProductReview parent = getById(parentReviewId);
        if (parent == null) {
            throw new ServiceException("评价不存在");
        }

        // 构建管理员回复
        ProductReview reply = ProductReview.builder()
                .orderNumber(parent.getOrderNumber())
                .productId(parent.getProductId())
                .userId(adminId)  // 使用管理员ID作为userId
                .productScore(0)  // 管理员回复不需要评分，设置默认值
                .logisticsScore(0)
                .merchantScore(0)
                .content(content)
                .images("")  // 管理员回复暂不支持图片
                .parentReviewId(parentReviewId)
                .rootReviewId(parent.getRootReviewId() != null ? parent.getRootReviewId() : parent.getId())
                .isAdmin(1)  // 标记为管理员回复
                .adminId(adminId)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        return save(reply);
    }

    @Override
    public boolean adminDeleteReview(Long id) {
        ProductReview review = getById(id);
        if (review == null) {
            throw new ServiceException("评价不存在");
        }

        // 如果是根评价，同时删除所有子评价
        if (review.getParentReviewId() == null) {
            LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductReview::getRootReviewId, id);
            remove(wrapper);
        }

        return removeById(id);
    }

    @Override
    public ProductReviewVO getReviewDetail(Long id) {
        ProductReview review = getById(id);
        if (review == null) {
            throw new ServiceException("评价不存在");
        }

        ProductReviewVO vo = convertToVO(review);

        // 如果是根评价，加载所有子评价
        if (review.getParentReviewId() == null) {
            List<ProductReviewVO> children = selectChildrenByRootId(id);
            vo.setChildren(children);
        }

        return vo;
    }

    @Override
    public boolean adminDeleteBatch(List<Long> ids) {
        for (Long id : ids) {
            adminDeleteReview(id);
        }
        return true;
    }

    // ========== 评价统计方法实现 ==========

    @Override
    public ReviewStatisticsVO getReviewStatistics(Long productId) {
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductReview::getProductId, productId)
                .isNull(ProductReview::getParentReviewId);  // 只统计根评价

        List<ProductReview> reviews = list(wrapper);

        long totalCount = reviews.size();
        if (totalCount == 0) {
            return ReviewStatisticsVO.builder()
                    .totalCount(0L)
                    .goodCount(0L)
                    .mediumCount(0L)
                    .badCount(0L)
                    .goodRate(BigDecimal.ZERO)
                    .avgProductScore(BigDecimal.ZERO)
                    .avgLogisticsScore(BigDecimal.ZERO)
                    .avgMerchantScore(BigDecimal.ZERO)
                    .star1Count(0L)
                    .star2Count(0L)
                    .star3Count(0L)
                    .star4Count(0L)
                    .star5Count(0L)
                    .withImageCount(0L)
                    .build();
        }

        // 统计各星级数量
        long star1 = reviews.stream().filter(r -> r.getProductScore() != null && r.getProductScore() == 1).count();
        long star2 = reviews.stream().filter(r -> r.getProductScore() != null && r.getProductScore() == 2).count();
        long star3 = reviews.stream().filter(r -> r.getProductScore() != null && r.getProductScore() == 3).count();
        long star4 = reviews.stream().filter(r -> r.getProductScore() != null && r.getProductScore() == 4).count();
        long star5 = reviews.stream().filter(r -> r.getProductScore() != null && r.getProductScore() == 5).count();

        long goodCount = star4 + star5;
        long mediumCount = star3;
        long badCount = star1 + star2;

        // 计算好评率
        BigDecimal goodRate = BigDecimal.valueOf(goodCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP);

        // 计算平均分
        double avgProduct = reviews.stream()
                .filter(r -> r.getProductScore() != null)
                .mapToInt(ProductReview::getProductScore)
                .average().orElse(0);
        double avgLogistics = reviews.stream()
                .filter(r -> r.getLogisticsScore() != null)
                .mapToInt(ProductReview::getLogisticsScore)
                .average().orElse(0);
        double avgMerchant = reviews.stream()
                .filter(r -> r.getMerchantScore() != null)
                .mapToInt(ProductReview::getMerchantScore)
                .average().orElse(0);

        // 有图评价数
        long withImageCount = reviews.stream()
                .filter(r -> r.getImages() != null && !r.getImages().isEmpty())
                .count();

        return ReviewStatisticsVO.builder()
                .totalCount(totalCount)
                .goodCount(goodCount)
                .mediumCount(mediumCount)
                .badCount(badCount)
                .goodRate(goodRate)
                .avgProductScore(BigDecimal.valueOf(avgProduct).setScale(1, RoundingMode.HALF_UP))
                .avgLogisticsScore(BigDecimal.valueOf(avgLogistics).setScale(1, RoundingMode.HALF_UP))
                .avgMerchantScore(BigDecimal.valueOf(avgMerchant).setScale(1, RoundingMode.HALF_UP))
                .star1Count(star1)
                .star2Count(star2)
                .star3Count(star3)
                .star4Count(star4)
                .star5Count(star5)
                .withImageCount(withImageCount)
                .build();
    }

    @Override
    public IPage<ProductReviewVO> selectReviewByScore(Long productId, Integer scoreType, Integer pageNum, Integer pageSize) {
        Page<ProductReview> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductReview::getProductId, productId)
                .isNull(ProductReview::getParentReviewId);

        // scoreType: 0-全部, 1-好评(4-5), 2-中评(3), 3-差评(1-2), 4-有图
        if (scoreType != null) {
            switch (scoreType) {
                case 1: // 好评
                    wrapper.in(ProductReview::getProductScore, 4, 5);
                    break;
                case 2: // 中评
                    wrapper.eq(ProductReview::getProductScore, 3);
                    break;
                case 3: // 差评
                    wrapper.in(ProductReview::getProductScore, 1, 2);
                    break;
                case 4: // 有图
                    wrapper.isNotNull(ProductReview::getImages)
                            .ne(ProductReview::getImages, "");
                    break;
            }
        }

        wrapper.orderByDesc(ProductReview::getCreateTime);
        IPage<ProductReview> reviewPage = page(page, wrapper);

        // 批量转换
        List<ProductReviewVO> voList = batchConvertToVO(reviewPage.getRecords());

        // 加载子评价
        for (ProductReviewVO vo : voList) {
            List<ProductReviewVO> children = selectChildrenByRootId(vo.getId());
            vo.setChildren(children);
        }

        Page<ProductReviewVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setRecords(voList);
        voPage.setTotal(reviewPage.getTotal());
        return voPage;
    }

    /**
     * 批量转换为VO（优化N+1查询）
     */
    private List<ProductReviewVO> batchConvertToVO(List<ProductReview> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return new ArrayList<>();
        }

        // 收集所有需要查询的ID
        Set<Long> userIds = new HashSet<>();
        Set<Long> productIds = new HashSet<>();
        Set<Long> parentReviewIds = new HashSet<>();

        for (ProductReview review : reviews) {
            if (review.getUserId() != null) userIds.add(review.getUserId());
            if (review.getProductId() != null) productIds.add(review.getProductId());
            if (review.getParentReviewId() != null) parentReviewIds.add(review.getParentReviewId());
            if (review.getAdminId() != null) userIds.add(review.getAdminId());
        }

        // 批量查询用户
        Map<Long, SysUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));
        }

        // 批量查询商品
        Map<Long, Product> productMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<Product> products = productMapper.selectBatchIds(productIds);
            productMap = products.stream().collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));
        }

        // 批量查询父评价
        Map<Long, ProductReview> parentReviewMap = new HashMap<>();
        if (!parentReviewIds.isEmpty()) {
            List<ProductReview> parentReviews = listByIds(parentReviewIds);
            parentReviewMap = parentReviews.stream().collect(Collectors.toMap(ProductReview::getId, Function.identity(), (a, b) -> a));
        }

        // 转换
        final Map<Long, SysUser> finalUserMap = userMap;
        final Map<Long, Product> finalProductMap = productMap;
        final Map<Long, ProductReview> finalParentReviewMap = parentReviewMap;

        return reviews.stream().map(review -> {
            SysUser user = finalUserMap.get(review.getUserId());
            Product product = finalProductMap.get(review.getProductId());

            // 处理图片URL列表
            List<String> imageList = new ArrayList<>();
            if (review.getImages() != null && !review.getImages().isEmpty()) {
                imageList = Arrays.asList(review.getImages().split(","));
            }

            // 获取父评价用户名
            String parentUsername = null;
            if (review.getParentReviewId() != null) {
                ProductReview parent = finalParentReviewMap.get(review.getParentReviewId());
                if (parent != null) {
                    SysUser parentUser = finalUserMap.get(parent.getUserId());
                    parentUsername = parentUser != null ? parentUser.getUsername() : "";
                }
            }

            // 获取管理员名称
            String adminName = null;
            if (review.getIsAdmin() != null && review.getIsAdmin() == 1 && review.getAdminId() != null) {
                SysUser admin = finalUserMap.get(review.getAdminId());
                adminName = admin != null ? admin.getUsername() : "管理员";
            }

            return ProductReviewVO.builder()
                    .id(review.getId())
                    .orderNumber(review.getOrderNumber())
                    .productId(review.getProductId())
                    .productName(product != null ? product.getName() : "")
                    .productImage(product != null ? product.getImage() : "")
                    .userId(review.getUserId())
                    .username(user != null ? user.getUsername() : "")
                    .userAvatar(user != null ? user.getAvatar() : "")
                    .productScore(review.getProductScore())
                    .logisticsScore(review.getLogisticsScore())
                    .merchantScore(review.getMerchantScore())
                    .content(review.getContent())
                    .imageList(imageList)
                    .parentReviewId(review.getParentReviewId())
                    .parentUsername(parentUsername)
                    .rootReviewId(review.getRootReviewId())
                    .isAdmin(review.getIsAdmin())
                    .adminId(review.getAdminId())
                    .adminName(adminName)
                    .createTime(review.getCreateTime())
                    .updateTime(review.getUpdateTime())
                    .build();
        }).collect(Collectors.toList());
    }
}
