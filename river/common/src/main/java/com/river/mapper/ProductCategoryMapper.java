package com.river.mapper;

import com.river.entity.ProductCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 *  商品分类 Mapper接口
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {

    /**
     * 查询商品分类分布
     * @return 商品分类分布数据
     */
    @MapKey("name")
    List<Map<String, Object>> selectCategoryDistribution();
}