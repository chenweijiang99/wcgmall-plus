package com.river.service.impl;

import java.util.List;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.river.dto.product.ProductPageDTO;
import com.river.entity.Comments;
import com.river.entity.OfficialCollection;
import com.river.entity.UserWishList;
import com.river.exception.ServiceException;
import com.river.mapper.OfficialCollectionMapper;
import com.river.mapper.UserWishListMapper;
import com.river.vo.product.ProductDetailVo;
import org.springframework.stereotype.Service;
import com.river.mapper.ProductMapper;
import com.river.entity.Product;
import com.river.service.ProductService;
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
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final UserWishListMapper wishListMapper;
    /**
     * 查询分页列表
     */
    @Override
    public IPage<Product> selectPage(Product product) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.like(product.getName() != null, Product::getName, product.getName());
        wrapper.eq(product.getCategoryId() != null, Product::getCategoryId, product.getCategoryId());
        wrapper.eq(product.getBrandId() != null, Product::getBrandId, product.getBrandId());
        wrapper.eq(product.getStatus() != null, Product::getStatus, product.getStatus());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询列表
     */
    @Override
    public List<Product> selectList(Product product) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(product.getId() != null, Product::getId, product.getId());
        wrapper.eq(product.getCreateTime() != null, Product::getCreateTime, product.getCreateTime());
        wrapper.eq(product.getUpdateTime() != null, Product::getUpdateTime, product.getUpdateTime());
        wrapper.eq(product.getUserId() != null, Product::getUserId, product.getUserId());
        wrapper.eq(product.getName() != null, Product::getName, product.getName());
        wrapper.eq(product.getCategoryId() != null, Product::getCategoryId, product.getCategoryId());
        wrapper.eq(product.getBrandId() != null, Product::getBrandId, product.getBrandId());
        wrapper.eq(product.getPrice() != null, Product::getPrice, product.getPrice());
        wrapper.eq(product.getInventory() != null, Product::getInventory, product.getInventory());
        wrapper.eq(product.getImage() != null, Product::getImage, product.getImage());
        wrapper.eq(product.getDetailImages() != null, Product::getDetailImages, product.getDetailImages());
        wrapper.eq(product.getDescription() != null, Product::getDescription, product.getDescription());
        wrapper.eq(product.getDescriptionImage() != null, Product::getDescriptionImage, product.getDescriptionImage());
        wrapper.eq(product.getLabel() != null, Product::getLabel, product.getLabel());
        wrapper.eq(product.getStatus() != null, Product::getStatus, product.getStatus());
        return list(wrapper);
    }

    /**
     * 新增
     */
    @Override
    public boolean insert(Product product) {
        return save(product);
    }

    /**
     * 修改
     */
    @Override
    public boolean update(Product product) {
        return updateById(product);
    }

    /**
     * 批量删除
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }

     /**
      * 启动或停用商品
      */
    @Override
    public boolean startOrStop(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        product.setStatus(product.getStatus() == 0 ? 1 : 0);
        return updateById(product);
    }

    @Override
    public List<Product> userGetNewProductList() {
        return baseMapper.userGetNewProductList();
    }

    @Override
    public ProductDetailVo getDetailById(Long id) {
        return baseMapper.getDetailById(id);
    }

    @Override
    public boolean addToWishList(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        if (product.getStatus() == 0) {
            throw new ServiceException("商品已下架");
        }
        List<UserWishList> userWishLists = wishListMapper.selectList(new LambdaQueryWrapper<UserWishList>()
                .eq(UserWishList::getUserId, StpUtil.getLoginIdAsLong())
                .eq(UserWishList::getProductId, id));
        if (userWishLists.size() > 0) {
            throw new ServiceException("商品已添加到心愿单");
        }
        UserWishList userWishList = UserWishList.builder()
                .userId(StpUtil.getLoginIdAsLong())
                .productId(id)
                .build();
        wishListMapper.insert(userWishList);
        return true;
    }

    @Override
    public IPage<Product> selectPage1(ProductPageDTO productPageDTO) {
        Page<ProductPageDTO> page = PageUtil.getPage();
        return baseMapper.selectPage1(page, productPageDTO);

    }


}
