package com.river.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AliPayService {

    String alipayPay(String orderNumber) throws Exception;

    void payNotify(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception;

    boolean refund(String orderNumber) throws Exception;

    boolean queryTrade(String orderNumber) throws Exception;


}
