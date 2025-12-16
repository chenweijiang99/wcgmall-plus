package com.river.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.river.dto.ProductReviewDTO;
import com.river.entity.ProductReview;
import com.river.vo.ProductReviewVO;

import java.util.List;

public interface ProductReviewService extends IService<ProductReview> {
    /**
     * 添加商品评价
     */
    boolean addReview(ProductReviewDTO dto);

    /**
     * 查询商品评价树(分页)
     */
    IPage<ProductReviewVO> selectReviewTree(Long productId, Integer pageNum, Integer pageSize);

    /**
     * 查询商品评价数量
     */
    Long selectReviewCount(Long productId);

    /**
     * 删除评价
     */
    boolean deleteReview(Long id);

    /**
     * 查询用户待评价的订单商品列表
     */
    List<ProductReviewVO> selectPendingReviewProducts(Long userId);

    /**
     * 查询根评价
     */
    List<ProductReviewVO> selectRootReviews(Long productId);

    /**
     * 根据根评价ID查询子评价
     */
    List<ProductReviewVO> selectChildrenByRootId(Long rootId);

    // ========== 后台管理方法 ==========

    /**
     * 后台分页查询评价列表
     */
    IPage<ProductReviewVO> adminSelectPage(ProductReview query, Integer pageNum, Integer pageSize);

    /**
     * 管理员回复评价
     */
    boolean adminReply(Long parentReviewId, String content, Long adminId);

    /**
     * 后台删除评价(管理员权限)
     */
    boolean adminDeleteReview(Long id);

    /**
     * 获取评价详情(包含所有回复)
     */
    ProductReviewVO getReviewDetail(Long id);
}
