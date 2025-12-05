package com.river.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.common.Result;
import com.river.dto.OrderSubmitDTO;
import com.river.entity.ProductOrder;
import com.river.service.LogisticsService;
import com.river.service.ProductOrderService;
import com.river.vo.LogisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController("userProductOrderController")
@RequestMapping("/user/productOrder")
@Slf4j
@Tag(name = "商品订单相关接口")
@RequiredArgsConstructor
public class ProductOrderController {
    private final ProductOrderService productOrderService;
    private final LogisticsService logisticsService;

    @Operation(summary = "提交订单")
    @PostMapping("/submit")
    public Result<String> submitOrder(@RequestBody OrderSubmitDTO submitDTO) {
        log.info("用户提交订单: {}", submitDTO);
        String orderNo = productOrderService.submitOrder(submitDTO);
        return Result.success(orderNo);
    }

    @Operation(summary = "查询订单")
    @GetMapping("/page")
    public Result<IPage<ProductOrder>> list(ProductOrder productOrder) {
        return Result.success(productOrderService.userSelectPage(productOrder));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/detail/{orderNumber}")
    public Result<java.util.List<com.river.vo.OrderDetailVO>> getOrderDetail(@PathVariable String orderNumber) {
        return Result.success(productOrderService.getOrderDetail(orderNumber));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/cancel/{orderNumber}")
    public Result<String> cancelOrder(@PathVariable String orderNumber) {
        productOrderService.cancelOrder(orderNumber);
        return Result.success("取消成功");
    }

    @Operation(summary = "申请退款")
    @PostMapping("/refund/{orderNumber}")
    public Result<String> refundOrder(@PathVariable String orderNumber) {
        productOrderService.refundOrder(orderNumber);
        return Result.success("退款申请成功");
    }

    @Operation(summary = "确认收货")
    @PostMapping("/confirm/{orderNumber}")
    public Result<String> confirmReceipt(@PathVariable String orderNumber) {
        productOrderService.confirmReceipt(orderNumber);
        return Result.success("确认收货成功");
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{orderNumber}")
    public Result<Object> deleteOrder(@PathVariable String orderNumber) {
        return Result.success(productOrderService.deleteOrder(orderNumber));
    }

    @Operation(summary = "查询物流信息")
    @GetMapping("/logistics/{orderNumber}")
    public Result<LogisticsVO> getLogistics(@PathVariable String orderNumber) {
        return Result.success(logisticsService.getLogistics(orderNumber));
    }
}
