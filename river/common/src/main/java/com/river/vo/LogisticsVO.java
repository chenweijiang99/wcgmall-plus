package com.river.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 物流信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "物流信息")
public class LogisticsVO {

    @Schema(description = "订单编号")
    private String orderNumber;

    @Schema(description = "顺丰运单号")
    private String waybillNo;

    @Schema(description = "状态：0待下单 1已下单 2运输中 3已签收")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "物流轨迹列表")
    private List<LogisticsRouteVO> routes;
}
