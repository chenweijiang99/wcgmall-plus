package com.river.controller.order;

import java.util.List;

import com.river.entity.ProductOrder;
import com.river.vo.OrderDetailVO;
import org.springframework.web.bind.annotation.*;
import com.river.service.ProductOrderService;
import com.river.common.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
/**
 *  订单管理控制器
 */
@RestController
@RequestMapping("/sys/order")
@RequiredArgsConstructor
@Tag(name = "订单管理")
public class OrderController {

    private final ProductOrderService orderService;

    @GetMapping("/list")
    @Operation(summary = "获取订单列表")
    public Result<IPage<ProductOrder>> list(ProductOrder productOrder) {
        return Result.success(orderService.selectPage(productOrder));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单信息")
    public Result<ProductOrder> getInfo(@PathVariable("id") Long id) {
        return Result.success(orderService.getById(id));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/detail/{orderNumber}")
    public Result<List<OrderDetailVO>> getOrderDetail(@PathVariable String orderNumber) {
        return Result.success(orderService.getOrderDetail(orderNumber));
    }

    @PostMapping("/add")
    @Operation(summary = "添加订单")
    public Result<Object> add(@RequestBody ProductOrder productOrder) {
        return Result.success(orderService.insert(productOrder));
    }

    @PutMapping("/update")
    @Operation(summary = "修改订单")
    public Result<Object> edit(@RequestBody ProductOrder productOrder) {
        return Result.success(orderService.update(productOrder));
    }

    @DeleteMapping("/delete/{ids}")
    @Operation(summary = "删除订单")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(orderService.deleteByIds(ids));
    }
}
