package com.river.controller;

import com.river.common.Result;
import com.river.dto.OrderSubmitDTO;
import com.river.service.ProductOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userProductOrderController")
@RequestMapping("/user/productOrder")
@Slf4j
@Tag(name = "商品订单相关接口")
@RequiredArgsConstructor
public class ProductOrderController {
    private final ProductOrderService productOrderService;

    @Operation(summary = "提交订单")
    @PostMapping("/submit")
    public Result<String> submitOrder(@RequestBody OrderSubmitDTO submitDTO) {
        log.info("用户提交订单: {}", submitDTO);
        String orderNo = productOrderService.submitOrder(submitDTO);
        return Result.success(orderNo);
    }
}
