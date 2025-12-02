package com.river.controller.user;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.river.common.Result;
import com.river.config.alipay.AlipayProperties;
import com.river.entity.ReservationOrder;
import com.river.entity.Room;
import com.river.exception.ServiceException;
import com.river.service.ReservationOrderService;
import com.river.service.RoomService;
import com.river.utils.AlipayUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
@Tag(name = "支付管理")
@Slf4j
public class PayController {
    private final ReservationOrderService reservationOrderService;
    private final RoomService roomService;
    private final AlipayUtil alipayUtil;
    private final AlipayProperties aliPayProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping()
    @Operation(summary = "支付宝支付")
    public Result<String> alipayPay(String orderNumber) throws Exception {
        log.info("订单支付{}",orderNumber);
        // 支付账号：  oworsb4854@sandbox.com    密码：111111
        ReservationOrder order = reservationOrderService.getByOrderNumber(orderNumber);
        String alipayPay = alipayUtil.pay(order);
        return Result.success(alipayPay);
    }



    @PostMapping("/notify")  // 注意这里必须是POST接口
    @Operation(summary = "支付宝支付回调")
    public void payNotify(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            log.info("支付宝异步回调");
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }

            String sign = params.get("sign");
            String content = AlipaySignature.getSignCheckContentV1(params);
            boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, aliPayProperties.getAlipayPublicKey(), "UTF-8"); // 验证签名
            // 支付宝验签
            if (checkSignature) {
                // 验签通过
                log.info("支付宝异步回调验签成功");
                log.info("订单号：{}", params.get("out_trade_no"));
                log.info("交易状态：{}", params.get("trade_status"));
                log.info("交易金额:{} " , params.get("total_amount"));
                String tradeNo = params.get("out_trade_no");
                // 更新订单状态为已支付，设置支付信息
                ReservationOrder order = reservationOrderService.getByOrderNumber(tradeNo);
                if(order == null){
                    throw new ServiceException("订单不存在");
                }
                order.setPayStatus(1);// 支付状态改为已支付
                order.setOrderStatus(1);// 订单状态改为已支付
                order.setPayTime(LocalDateTime.now());
                order.setPayAmount(new BigDecimal(params.get("total_amount")));
                order.setPayTransactionId(params.get("trade_no"));
                order.setPayMethod("支付宝");
                reservationOrderService.update(order);
            }else {
                throw new ServiceException("支付宝验签失败");
            }

        }
    }

    @PutMapping("/refund")
    @Operation(summary = "支付宝退款")
    public Result<String> refund(String orderNumber) throws AlipayApiException {
        ReservationOrder order = reservationOrderService.getByOrderNumber(orderNumber);
        if (ObjectUtil.isNull(order)) {
            throw new ServiceException("订单不存在");
        }
        if(order.getPayStatus() == null || order.getPayStatus() == 0 || order.getOrderStatus() != 1){
            throw new ServiceException("订单未支付，不能退款");
        }
        order.setOrderStatus(6);// 订单状态改为退款中
        reservationOrderService.update(order);
        //调用支付宝退款接口
        try{
            boolean isRefund = alipayUtil.refund(order);
            if (isRefund) {
                log.info("订单号{}退款成功",orderNumber);
                order.setOrderStatus(7);// 订单状态改为已退款
                order.setPayStatus(2);// 支付状态改为已退款
                reservationOrderService.update(order);
                if (order.getRoomId() != null) {
                    Room room = roomService.getById(order.getRoomId());
                    if (room != null) {
                        room.setStatus(1);
                        roomService.updateById(room);
                    }
                }
            }else {
                log.error("订单号{}退款失败",orderNumber);
                order.setOrderStatus(1);
                reservationOrderService.update(order);
                throw new ServiceException("网络异常，退款失败，请重试");
            }
            return Result.success("退款成功");
        }catch (Exception e){
            throw new ServiceException("网络异常，退款失败，请重试");
        }


    }

    @GetMapping("/query/{orderNumber}")
    @Operation(summary = "支付宝支付查询")
    public Result<Boolean> queryTrade(@PathVariable String orderNumber) throws AlipayApiException {
        ReservationOrder order = reservationOrderService.getByOrderNumber(orderNumber);
        if (ObjectUtil.isNull(order)) {
            throw new ServiceException("订单不存在");
        }
        
        // 本地订单状态检查：如果已支付，直接返回true，无需调用支付宝API
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            return Result.success(true);
        }

        String result = alipayUtil.queryTrade(order);
        boolean paySuccess = false;
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONObject alipayTradeQueryResponse = jsonObject.getJSONObject("alipay_trade_query_response");
            if (alipayTradeQueryResponse == null) {
                paySuccess = false;
            } else if (alipayTradeQueryResponse.get("trade_status") == null) {
                paySuccess = false;
            } else if (alipayTradeQueryResponse.get("trade_status").equals("TRADE_SUCCESS")) {
                order.setPayStatus(1);
                order.setOrderStatus(1);
                order.setPayTime(LocalDateTime.now());
                order.setPayAmount(new BigDecimal(String.valueOf(alipayTradeQueryResponse.get("total_amount"))));
                order.setPayTransactionId((String) alipayTradeQueryResponse.get("trade_no"));
                order.setPayMethod("支付宝");
                reservationOrderService.update(order);
                paySuccess = true;
            }

            return Result.success(paySuccess);
        }catch (Exception e) {
            log.error("支付宝支付查询失败",e);
            return Result.success(false);
        }
    }


}
