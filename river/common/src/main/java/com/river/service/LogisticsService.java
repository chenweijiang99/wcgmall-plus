package com.river.service;

import com.river.dto.ShipOrderDTO;
import com.river.entity.OrderLogistics;
import com.river.vo.LogisticsVO;

/**
 * 物流服务接口
 */
public interface LogisticsService {

    /**
     * 发货 - 调用顺丰下单接口
     *
     * @param dto 发货信息
     * @return 物流信息
     */
    OrderLogistics shipOrder(ShipOrderDTO dto);

    /**
     * 查询物流轨迹
     *
     * @param orderNumber 订单编号
     * @return 物流信息及轨迹
     */
    LogisticsVO getLogistics(String orderNumber);

    /**
     * 根据运单号查询物流轨迹
     *
     * @param waybillNo 运单号
     * @return 物流信息及轨迹
     */
    LogisticsVO getLogisticsByWaybillNo(String waybillNo);
}
