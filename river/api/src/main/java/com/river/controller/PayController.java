package com.river.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.river.common.Result;

import com.river.service.AliPayService;
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

    private final AliPayService aliPayService;

    @GetMapping("/alipay/{orderNumber}")
    @Operation(summary = "支付宝支付")
    public Result<String> alipayPay(@PathVariable String orderNumber) throws Exception {
        log.info("订单支付{}",orderNumber);
        // 支付账号：  oworsb4854@sandbox.com    密码：111111
        return Result.success(aliPayService.alipayPay(orderNumber));
    }



    @PostMapping("/notify")  // 注意这里必须是POST接口
    @Operation(summary = "支付宝支付回调")
    //目前使用的是前端轮询的方式，这个回调保留
    public void payNotify(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {
        log.info("支付宝支付回调开始");
        aliPayService.payNotify(request, httpServletResponse);
        log.info("支付宝支付回调结束");
    }

    @PutMapping("/refund")
    @Operation(summary = "支付宝退款")
    public Result<String> refund(String orderNumber) throws Exception {
        log.info("订单退款{}",orderNumber);
        boolean refundSuccess = aliPayService.refund(orderNumber);
        if (refundSuccess) {
            return Result.success("退款成功");
        } else {
            return Result.error("退款失败");
        }

    }

    @GetMapping("/query/{orderNumber}")
    @Operation(summary = "支付宝支付查询")
    public Result<Boolean> queryTrade(@PathVariable String orderNumber) throws Exception {
        log.info("订单支付查询{}",orderNumber);
        boolean paySuccess = aliPayService.queryTrade(orderNumber);
        if (paySuccess) {
            return Result.success(true);
        } else {
            return Result.success(false);
        }
    }

}
