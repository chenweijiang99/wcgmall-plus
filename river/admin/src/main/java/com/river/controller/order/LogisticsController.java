package com.river.controller.order;

import com.river.common.Result;
import com.river.dto.ShipOrderDTO;
import com.river.entity.OrderLogistics;
import com.river.service.LogisticsService;
import com.river.vo.LogisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端物流控制器
 */
@RestController
@RequestMapping("/sys/logistics")
@RequiredArgsConstructor
@Tag(name = "物流管理")
public class LogisticsController {

    private final LogisticsService logisticsService;

    @PostMapping("/ship")
    @Operation(summary = "发货 - 调用顺丰下单接口")
    public Result<OrderLogistics> shipOrder(@RequestBody @Validated ShipOrderDTO dto) {
        OrderLogistics logistics = logisticsService.shipOrder(dto);
        return Result.success(logistics);
    }

    @GetMapping("/routes/{orderNumber}")
    @Operation(summary = "查询物流轨迹")
    public Result<LogisticsVO> getLogistics(@PathVariable String orderNumber) {
        LogisticsVO logistics = logisticsService.getLogistics(orderNumber);
        return Result.success(logistics);
    }

    @GetMapping("/waybill/{waybillNo}")
    @Operation(summary = "根据运单号查询物流轨迹")
    public Result<LogisticsVO> getLogisticsByWaybillNo(@PathVariable String waybillNo) {
        LogisticsVO logistics = logisticsService.getLogisticsByWaybillNo(waybillNo);
        return Result.success(logistics);
    }
}
