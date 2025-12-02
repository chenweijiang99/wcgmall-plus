package com.river.quartz;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.river.entity.ReservationOrder;
import com.river.entity.Room;
import com.river.service.ReservationOrderService;
import com.river.service.RoomService;
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
    private final ReservationOrderService reservationOrderService;
    private final RoomService roomService;

    private final AlipayUtil alipayUtil;

    /**
     * 每分钟检查5分钟内创建的未支付订单的支付状态
     */
    @Scheduled(fixedDelay = 60000) // 每分钟执行一次
    public void checkRecentOrderPayments() {
        try {
            // 获取5分钟内创建的未支付订单（创建时间在5分钟内，且支付状态为未支付）
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
            List<ReservationOrder> recentUnpaidOrders = reservationOrderService.list(
                    new LambdaQueryWrapper<ReservationOrder>()
                            .eq(ReservationOrder::getOrderStatus, 0) // 订单状态为待支付
                            .eq(ReservationOrder::getPayStatus, 0)   // 支付状态为未支付
                            .ge(ReservationOrder::getCreateTime, threshold) // 创建时间在5分钟内
            );

            if (recentUnpaidOrders.isEmpty()) {
                log.info("无近期未支付订单需要检查");
                return;
            }

            log.info("开始检查{}个近期未支付订单的支付状态", recentUnpaidOrders.size());

            // 检查每个订单的支付状态
            for (ReservationOrder order : recentUnpaidOrders) {
                try {
                    checkAndUpdateOrderPaymentStatus(order);
                } catch (Exception e) {
                    log.error("检查订单{}支付状态时发生异常", order.getOrderNo(), e);
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
    private void checkAndUpdateOrderPaymentStatus(ReservationOrder order) {
        try {
            // 调用支付宝查询接口
            String result = alipayUtil.queryTrade(order);

            if (result == null || result.isEmpty()) {
                log.info("订单{}支付状态查询返回空结果", order.getOrderNo());
                return;
            }

            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONObject alipayTradeQueryResponse = jsonObject.getJSONObject("alipay_trade_query_response");

            if (alipayTradeQueryResponse == null) {
                log.info("订单{}支付状态查询响应为空", order.getOrderNo());
                return;
            }

            String tradeStatus = alipayTradeQueryResponse.getString("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                // 更新订单状态为已支付
                order.setPayStatus(1);
                order.setOrderStatus(1);
                order.setPayTime(LocalDateTime.now());
                order.setPayAmount(alipayTradeQueryResponse.getBigDecimal("total_amount"));
                order.setPayTransactionId(alipayTradeQueryResponse.getString("trade_no"));
                order.setPayMethod("支付宝");

                reservationOrderService.update(order);
                log.info("订单{}支付成功，状态已更新", order.getOrderNo());
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                // 交易关闭，可能是用户取消了支付
                log.info("订单{}支付已关闭", order.getOrderNo());
            } else {
                log.info("订单{}当前支付状态: {}", order.getOrderNo(), tradeStatus);
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝查询接口失败，订单号: {}", order.getOrderNo(), e);
        } catch (Exception e) {
            log.error("处理订单{}支付状态时发生异常", order.getOrderNo(), e);
        }
    }

    @Scheduled(fixedDelay = 10000) // 每10秒执行一次
    public void expireUnpaidOrders() {
        // 获取5分钟前的订单，即创建时间在5分钟前的订单
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
         List<ReservationOrder> unpaidOrders = reservationOrderService.list(new LambdaQueryWrapper<ReservationOrder>()
                        .eq(ReservationOrder::getOrderStatus, 0)
                        .eq(ReservationOrder::getPayStatus, 0)
                        .le(ReservationOrder::getCreateTime, threshold));
         if(unpaidOrders.isEmpty()){
             log.info("无超时未支付订单");
            return;
        }
        unpaidOrders.forEach(order -> {
                    try {
                        order.setOrderStatus(5);
                        order.setCancelTime(LocalDateTime.now());
                        reservationOrderService.update(order);
                        if (order.getRoomId() != null) {
                            Room room = roomService.getById(order.getRoomId());
                            if (room != null) {
                                room.setStatus(1);
                                roomService.updateById(room);
                            }
                        }
                        log.info("订单{}已超时取消", order.getOrderNo());
                    } catch (Exception e) {
                        log.error("订单超时取消失败: {}", order.getOrderNo(), e);
                    }
                });
    }
}
