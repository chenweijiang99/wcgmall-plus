package com.river.mapper;

import com.river.entity.ProductOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 *  Mapper接口
 */
@Mapper
public interface ProductOrderMapper extends BaseMapper<ProductOrder> {

    /**
     * 查询最近7天订单趋势
     * @return 订单趋势数据
     */
    @MapKey("date")
    List<Map<String, Object>> selectOrderGrowth();

    /**
     * 查询最新订单
     * @param limit 查询数量
     * @return 最新订单列表
     */
    @MapKey("id")
    List<Map<String, Object>> selectLatestOrders(@Param("limit") int limit);

    /**
     * 查询总销售额
     * @return 总销售额
     */
    Double selectTotalSales();

    /**
     * 查询待付款订单数量
     * @return 待付款订单数量
     */
    Long selectPendingPayments();
}