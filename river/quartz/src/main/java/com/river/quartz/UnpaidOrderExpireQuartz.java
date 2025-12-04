package com.river.quartz;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.river.entity.OrderDetail;
import com.river.entity.Product;
import com.river.entity.ProductOrder;
import com.river.mapper.OrderDetailMapper;
import com.river.service.ProductOrderService;
import com.river.service.ProductService;
import com.river.utils.AlipayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component("orderExpire")
@RequiredArgsConstructor
@Slf4j
public class UnpaidOrderExpireQuartz {
    private final ProductOrderService productOrderService;
    private final OrderDetailMapper orderDetailMapper;
    private final ProductService productService;
    private final AlipayUtil alipayUtil;

    /**
     * 每分钟检查5分钟内创建的未支付订单的支付状态
     */
    @Scheduled(fixedDelay = 60000) // 每分钟执行一次
    public void checkRecentOrderPayments() {
        try {
            // 获取5分钟内创建的未支付订单（创建时间在5分钟内，且支付状态为未支付）
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
            List<ProductOrder> recentUnpaidOrders = productOrderService.list(
                    new LambdaQueryWrapper<ProductOrder>()
                            .eq(ProductOrder::getStatus, 0) // 订单状态为待支付
                            .eq(ProductOrder::getPayStatus, 0)   // 支付状态为未支付
                            .ge(ProductOrder::getCreateTime, threshold) // 创建时间在5分钟内
            );

            if (recentUnpaidOrders.isEmpty()) {
                log.info("无近期未支付订单需要检查");
                return;
            }

            log.info("开始检查{}个近期未支付订单的支付状态", recentUnpaidOrders.size());

            // 检查每个订单的支付状态
            for (ProductOrder order : recentUnpaidOrders) {
                try {
                    checkAndUpdateOrderPaymentStatus(order);
                } catch (Exception e) {
                    log.error("检查订单{}支付状态时发生异常", order.getOrderNumber(), e);
                }
            }
        } catch (Exception e) {
            log.error("检查近期订单支付状态任务执行失败", e);
        }
    }

    /**
     * 检查并更新单个订单的支付状态
     * @param order 订单对象
     */
    private void checkAndUpdateOrderPaymentStatus(ProductOrder order) {
        try {
            // 调用支付宝查询接口
            String result = alipayUtil.queryTrade(order);

            if (result == null || result.isEmpty()) {
                log.info("订单{}支付状态查询返回空结果", order.getOrderNumber());
                return;
            }

            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONObject alipayTradeQueryResponse = jsonObject.getJSONObject("alipay_trade_query_response");

            if (alipayTradeQueryResponse == null) {
                log.info("订单{}支付状态查询响应为空", order.getOrderNumber());
                return;
            }

            String tradeStatus = alipayTradeQueryResponse.getString("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                // 更新订单状态为已支付
                order.setPayStatus(1);
                order.setStatus(1);
                order.setCheckoutTime(LocalDateTime.now());
                order.setPayMethod("支付宝");

                productOrderService.update(order);
                log.info("订单{}支付成功，状态已更新", order.getOrderNumber());
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                // 交易关闭，可能是用户取消了支付
                log.info("订单{}支付已关闭", order.getOrderNumber());
            } else {
                log.info("订单{}当前支付状态: {}", order.getOrderNumber(), tradeStatus);
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝查询接口失败，订单号: {}", order.getOrderNumber(), e);
        } catch (Exception e) {
            log.error("处理订单{}支付状态时发生异常", order.getOrderNumber(), e);
        }
    }

    @Scheduled(fixedDelay = 10000) // 每10秒执行一次
    public void expireUnpaidOrders() {
        // 获取5分钟前的订单，即创建时间在5分钟前的订单
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
         List<ProductOrder> unpaidOrders = productOrderService.list(new LambdaQueryWrapper<ProductOrder>()
                        .eq(ProductOrder::getStatus, 0)
                        .eq(ProductOrder::getPayStatus, 0)
                        .le(ProductOrder::getCreateTime, threshold));
         if(unpaidOrders.isEmpty()){
             log.info("无超时未支付订单");
            return;
        }
        unpaidOrders.forEach(order -> {
                    try {
                        order.setStatus(5);
                        productOrderService.update(order);
                        // 返回库存
                        List<OrderDetail> orderDetails = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                                .eq(OrderDetail::getOrderNumber, order.getOrderNumber()));
                        orderDetails.forEach(detail -> {
                            Product product = productService.getById(detail.getProductId());
                            product.setInventory(product.getInventory() + detail.getProductNumber());
                            productService.updateById(product);
                        });
                        orderDetailMapper.deleteBatchIds(orderDetails);
                        log.info("订单{}已超时取消", order.getOrderNumber());
                    } catch (Exception e) {
                        log.error("订单超时取消失败: {}", order.getOrderNumber(), e);
                    }
                });
    }
}
