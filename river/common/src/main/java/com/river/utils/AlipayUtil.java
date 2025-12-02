package com.river.utils;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.river.config.alipay.AlipayProperties;
import com.river.entity.ReservationOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 支付宝支付工具类
 * @author 枳枳
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AlipayUtil {

    private final AlipayProperties alipayProperties;
    
    // 单例AlipayClient实例
    private AlipayClient alipayClient;
    
    // 初始化AlipayClient实例
    private AlipayClient getAlipayClient() {
        if (alipayClient == null) {
            synchronized (this) {
                if (alipayClient == null) {
                    alipayClient = new DefaultAlipayClient(
                            alipayProperties.getGatewayUrl(),
                            alipayProperties.getAppId(),
                            alipayProperties.getPrivateKey(),
                            alipayProperties.getFormat(),
                            alipayProperties.getCharset(),
                            alipayProperties.getAlipayPublicKey(),
                            alipayProperties.getSignType()
                    );
                }
            }
        }
        return alipayClient;
    }

    public String pay(ReservationOrder  order) throws AlipayApiException{
        log.info("调用支付宝支付接口：{}",alipayProperties.toString());
        AlipayClient alipayClient = getAlipayClient();
        AlipayTradePagePayRequest alipayTradePagePayRequest = new AlipayTradePagePayRequest();
        alipayTradePagePayRequest.setReturnUrl(alipayProperties.getReturnUrl());
//        alipayTradePagePayRequest.setNotifyUrl(alipayProperties.getNotifyUrl());
        JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no",order.getOrderNo());
        //支付金额，最小值0.01元
        bizContent.put("total_amount",order.getTotalAmount());
        //订单标题，不可使用特殊符号
        bizContent.put("subject", order.getOrderNo());
        //电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        //二维码模式，固定传值4，扫码支付模式一（付款码）
//        bizContent.put("qr_pay_mode",4);
        alipayTradePagePayRequest.setBizContent(bizContent.toJSONString());
        AlipayTradePagePayResponse response = alipayClient.pageExecute(alipayTradePagePayRequest,"POST");
        return response.getBody();
    }


    public boolean refund(ReservationOrder  order) throws AlipayApiException{
        // 1. 获取Client，通用SDK提供的Client，负责调用支付宝的API
        AlipayClient alipayClient = getAlipayClient();

        // 2. 创建 Request并设置Request参数
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        cn.hutool.json.JSONObject bizContent = new cn.hutool.json.JSONObject();
        bizContent.set("out_trade_no", order.getOrderNo());  // 订单编号  必须是不重复的退款订单号
        bizContent.set("refund_amount", order.getTotalAmount()); // 订单的总金额
        bizContent.set("out_request_no", IdUtil.fastSimpleUUID());   // 随机数
        request.setBizContent(bizContent.toString());
        try {
            // 退款调用接口
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            return response.isSuccess();
        } catch (AlipayApiException e) {
            return false;
        }
    }

    /**
     * 支付宝交易查询接口
     * @param order 订单信息
     * @return 查询结果
     * @throws AlipayApiException 支付宝API异常
     */
    public String queryTrade(ReservationOrder order) throws AlipayApiException {
        // 1. 获取Client，通用SDK提供的Client，负责调用支付宝的API
        AlipayClient alipayClient = getAlipayClient();

        // 2. 创建 Request并设置Request参数
        AlipayTradeQueryRequest request = new com.alipay.api.request.AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", order.getOrderNo());  // 商户订单号
        request.setBizContent(bizContent.toJSONString());

        // 3. 执行查询请求，添加重试机制
        int maxRetries = 3;
        long retryDelay = 1000; // 初始重试延迟1秒
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                AlipayTradeQueryResponse response = alipayClient.execute(request);
                if (response.isSuccess()) {
                    return response.getBody();
                } else {
                    log.warn("支付宝查询失败，重试第{}次，错误信息：{}", i+1, response.getSubMsg());
                    // 如果是明确的业务错误，直接返回，不重试
                    if (response.getSubCode() != null && response.getSubCode().startsWith("ACQ")) {
                        return response.getBody();
                    }
                }
            } catch (AlipayApiException e) {
                log.warn("支付宝查询异常，重试第{}次，异常信息：{}", i+1, e.getMessage());
                // 如果是网络异常，进行重试；如果是其他异常，直接抛出
                if (!(e.getMessage().contains("Network unreachable") || e.getMessage().contains("Connection timed out"))) {
                    throw e;
                }
            }
            
            // 指数退避重试
            if (i < maxRetries - 1) {
                try {
                    Thread.sleep(retryDelay);
                    retryDelay *= 2; // 每次重试延迟翻倍
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AlipayApiException("查询重试被中断", e);
                }
            }
        }
        
        // 重试次数耗尽，最后执行一次查询
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        return response.getBody();
    }

    /**
     * 支付宝退款查询接口
     * @param order 订单信息
     * @param outRequestNo 退款请求号
     * @return 查询结果
     * @throws AlipayApiException 支付宝API异常
     */
    public String queryRefund(ReservationOrder order, String outRequestNo) throws AlipayApiException {
        // 1. 获取Client，通用SDK提供的Client，负责调用支付宝的API
        AlipayClient alipayClient = getAlipayClient();

        // 2. 创建 Request并设置Request参数
        AlipayTradeFastpayRefundQueryRequest request = new com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", order.getOrderNo());      // 商户订单号
        bizContent.put("out_request_no", outRequestNo);          // 退款请求号
        request.setBizContent(bizContent.toJSONString());

        // 3. 执行查询请求
        AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);
        return response.getBody();
    }


}
