package com.river.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.river.common.Result;
import com.river.config.alipay.AlipayProperties;
import com.river.entity.ProductOrder;
import com.river.exception.ServiceException;
import com.river.mapper.ProductOrderMapper;
import com.river.service.AliPayService;
import com.river.utils.AlipayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AliPayServiceImpl implements AliPayService {
    private final AlipayUtil alipayUtil;
    private final AlipayProperties aliPayProperties;
    private final ProductOrderMapper productOrderMapper;
    @Override
    public String alipayPay(String orderNumber) throws Exception {
        ProductOrder order = productOrderMapper.selectOne(new LambdaQueryWrapper<ProductOrder>()
                .eq(ProductOrder::getOrderNumber, orderNumber));
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (order.getPayStatus() == 1) {
            throw new ServiceException("订单已支付");
        }
        return alipayUtil.pay(order);
    }

    @Override
    public void payNotify(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
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
                String tradeNo = params.get("out_trade_no");
                // 更新订单状态为已支付，设置支付信息
                ProductOrder order = productOrderMapper.selectOne(new LambdaQueryWrapper<ProductOrder>()
                        .eq(ProductOrder::getOrderNumber, tradeNo));
                if(order == null){
                    throw new ServiceException("订单不存在");
                }
                order.setPayStatus(1);// 支付状态改为已支付
                order.setStatus(2);// 订单状态改为待发货
                order.setCheckoutTime(LocalDateTime.now());
                order.setAmount(new BigDecimal(params.get("total_amount")));
                order.setPayMethod("支付宝");
                productOrderMapper.updateById(order);
            }else {
                throw new ServiceException("支付宝验签失败");
            }
        }
    }

    @Override
    public boolean refund(String orderNumber) {
        ProductOrder order = productOrderMapper.selectOne(new LambdaQueryWrapper<ProductOrder>()
                .eq(ProductOrder::getOrderNumber, orderNumber));
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if(order.getPayStatus() == null || order.getPayStatus() == 0 || order.getStatus() != 0){
            throw new ServiceException("订单未支付，不能退款");
        }
        //调用支付宝退款接口
        try{
            boolean isRefund = alipayUtil.refund(order);
            if (isRefund) {
                order.setStatus(6);// 订单状态改为已退款
                order.setPayStatus(2);// 支付状态改为已退款
                productOrderMapper.updateById(order);
                return true;
            }

        }catch (Exception e){
            throw new ServiceException("网络异常，退款失败，请重试");
        }
            return false;
    }

    @Override
    public boolean queryTrade(String orderNumber) throws Exception {
        ProductOrder order = productOrderMapper.selectOne(new LambdaQueryWrapper<ProductOrder>()
                .eq(ProductOrder::getOrderNumber, orderNumber));
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        // 本地订单状态检查：如果已支付，直接返回true，无需调用支付宝API
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            return true;
        }

        String result = alipayUtil.queryTrade(order);
        boolean paySuccess = false;
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONObject alipayTradeQueryResponse = jsonObject.getJSONObject("alipay_trade_query_response");
            if (alipayTradeQueryResponse == null) {
                return paySuccess;
            } else if (alipayTradeQueryResponse.get("trade_status") == null) {
                return paySuccess;
            } else if (alipayTradeQueryResponse.get("trade_status").equals("TRADE_SUCCESS")) {
                order.setPayStatus(1);// 支付状态改为已支付
                order.setStatus(2);// 订单状态改为待发货
                order.setCheckoutTime(LocalDateTime.now());
                order.setAmount(new BigDecimal(String.valueOf(alipayTradeQueryResponse.get("total_amount"))));
                order.setPayMethod("支付宝");
                productOrderMapper.updateById(order);
                paySuccess = true;
            }
            return paySuccess;
        }catch (Exception e) {
            return paySuccess;
        }
    }
}
