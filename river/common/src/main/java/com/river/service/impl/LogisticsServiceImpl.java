package com.river.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.river.dto.ShipOrderDTO;
import com.river.entity.OrderLogistics;
import com.river.entity.ProductOrder;
import com.river.mapper.OrderLogisticsMapper;
import com.river.mapper.ProductOrderMapper;
import com.river.service.LogisticsService;
import com.river.utils.SfExpressClient;
import com.river.vo.LogisticsRouteVO;
import com.river.vo.LogisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 物流服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsServiceImpl implements LogisticsService {

    private final OrderLogisticsMapper logisticsMapper;
    private final ProductOrderMapper orderMapper;
    private final SfExpressClient sfExpressClient;

    private static final DateTimeFormatter SF_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderLogistics shipOrder(ShipOrderDTO dto) {
        // 查询订单
        LambdaQueryWrapper<ProductOrder> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(ProductOrder::getOrderNumber, dto.getOrderNumber());
        ProductOrder order = orderMapper.selectOne(orderQuery);

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 检查订单状态 - 1已付款 或 2待发货 可以发货
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new RuntimeException("订单状态不允许发货");
        }

        // 检查是否已发货
        LambdaQueryWrapper<OrderLogistics> logisticsQuery = new LambdaQueryWrapper<>();
        logisticsQuery.eq(OrderLogistics::getOrderNumber, dto.getOrderNumber());
        OrderLogistics existLogistics = logisticsMapper.selectOne(logisticsQuery);
        if (existLogistics != null && existLogistics.getStatus() >= 1) {
            throw new RuntimeException("该订单已发货");
        }

        // 调用顺丰下单接口
        JSONObject result = sfExpressClient.createOrder(dto, order);
        log.info("顺丰下单结果: {}", result);

        // 解析响应
        String waybillNo = null;
        String sfOrderId = null;
        Integer filterResult = null;

        // 尝试从不同的响应结构中获取数据
        if (result.containsKey("msgData")) {
            JSONObject msgData = JSONUtil.parseObj(result.getStr("msgData"));
            waybillNo = msgData.getStr("waybillNoInfoList");
            sfOrderId = msgData.getStr("orderId");
            filterResult = msgData.getInt("filterResult");

            // 解析运单号列表
            if (msgData.containsKey("waybillNoInfoList")) {
                JSONArray waybillList = msgData.getJSONArray("waybillNoInfoList");
                if (waybillList != null && !waybillList.isEmpty()) {
                    JSONObject firstWaybill = waybillList.getJSONObject(0);
                    waybillNo = firstWaybill.getStr("waybillNo");
                }
            }
        } else {
            // 直接从结果中获取
            waybillNo = result.getStr("waybillNo");
            sfOrderId = result.getStr("orderId");
            filterResult = result.getInt("filterResult");

            if (result.containsKey("waybillNoInfoList")) {
                JSONArray waybillList = result.getJSONArray("waybillNoInfoList");
                if (waybillList != null && !waybillList.isEmpty()) {
                    JSONObject firstWaybill = waybillList.getJSONObject(0);
                    waybillNo = firstWaybill.getStr("waybillNo");
                }
            }
        }

        // 创建或更新物流记录
        OrderLogistics logistics;
        if (existLogistics != null) {
            logistics = existLogistics;
        } else {
            logistics = new OrderLogistics();
            logistics.setOrderNumber(dto.getOrderNumber());
            logistics.setCreateTime(LocalDateTime.now());
        }

        logistics.setWaybillNo(waybillNo);
        logistics.setSfOrderId(sfOrderId);
        logistics.setSenderName(dto.getSenderName());
        logistics.setSenderPhone(dto.getSenderPhone());
        logistics.setSenderProvince(dto.getSenderProvince());
        logistics.setSenderCity(dto.getSenderCity());
        logistics.setSenderCounty(dto.getSenderCounty());
        logistics.setSenderAddress(dto.getSenderAddress());
        logistics.setStatus(1); // 已下单
        logistics.setFilterResult(filterResult);
        logistics.setUpdateTime(LocalDateTime.now());

        if (existLogistics != null) {
            logisticsMapper.updateById(logistics);
        } else {
            logisticsMapper.insert(logistics);
        }

        // 更新订单状态为已发货
        order.setStatus(3); // 已发货
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        return logistics;
    }

    @Override
    public LogisticsVO getLogistics(String orderNumber) {
        // 查询本地物流记录
        LambdaQueryWrapper<OrderLogistics> query = new LambdaQueryWrapper<>();
        query.eq(OrderLogistics::getOrderNumber, orderNumber);
        OrderLogistics logistics = logisticsMapper.selectOne(query);

        if (logistics == null) {
            // 未发货
            return LogisticsVO.builder()
                    .orderNumber(orderNumber)
                    .status(0)
                    .statusDesc("待发货")
                    .routes(Collections.emptyList())
                    .build();
        }

        return buildLogisticsVO(logistics);
    }

    @Override
    public LogisticsVO getLogisticsByWaybillNo(String waybillNo) {
        LambdaQueryWrapper<OrderLogistics> query = new LambdaQueryWrapper<>();
        query.eq(OrderLogistics::getWaybillNo, waybillNo);
        OrderLogistics logistics = logisticsMapper.selectOne(query);

        if (logistics == null) {
            throw new RuntimeException("物流信息不存在");
        }

        return buildLogisticsVO(logistics);
    }

    /**
     * 构建物流信息VO，包含实时轨迹
     */
    private LogisticsVO buildLogisticsVO(OrderLogistics logistics) {
        LogisticsVO vo = LogisticsVO.builder()
                .orderNumber(logistics.getOrderNumber())
                .waybillNo(logistics.getWaybillNo())
                .status(logistics.getStatus())
                .statusDesc(getStatusDesc(logistics.getStatus()))
                .routes(new ArrayList<>())
                .build();

        // 查询实时物流轨迹
        if (logistics.getWaybillNo() != null) {
            try {
                JSONObject routeResult = sfExpressClient.searchRoutes(logistics.getWaybillNo());
                List<LogisticsRouteVO> routes = parseRoutes(routeResult);
                vo.setRoutes(routes);

                // 根据路由更新状态
                if (!routes.isEmpty()) {
                    updateStatusFromRoutes(logistics, routes);
                    vo.setStatus(logistics.getStatus());
                    vo.setStatusDesc(getStatusDesc(logistics.getStatus()));
                }
            } catch (Exception e) {
                log.warn("查询物流轨迹失败: {}", e.getMessage());
                // 轨迹查询失败不影响整体返回
            }
        }

        return vo;
    }

    /**
     * 解析物流轨迹
     */
    private List<LogisticsRouteVO> parseRoutes(JSONObject result) {
        List<LogisticsRouteVO> routes = new ArrayList<>();

        JSONArray routeList = null;
        if (result.containsKey("msgData")) {
            JSONObject msgData = JSONUtil.parseObj(result.getStr("msgData"));
            if (msgData.containsKey("routeResps")) {
                JSONArray routeResps = msgData.getJSONArray("routeResps");
                if (routeResps != null && !routeResps.isEmpty()) {
                    JSONObject firstResp = routeResps.getJSONObject(0);
                    routeList = firstResp.getJSONArray("routes");
                }
            }
        } else if (result.containsKey("routeResps")) {
            JSONArray routeResps = result.getJSONArray("routeResps");
            if (routeResps != null && !routeResps.isEmpty()) {
                JSONObject firstResp = routeResps.getJSONObject(0);
                routeList = firstResp.getJSONArray("routes");
            }
        }

        if (routeList != null) {
            for (int i = 0; i < routeList.size(); i++) {
                JSONObject route = routeList.getJSONObject(i);
                LogisticsRouteVO routeVO = LogisticsRouteVO.builder()
                        .acceptTime(parseDateTime(route.getStr("acceptTime")))
                        .remark(route.getStr("remark"))
                        .opCode(route.getStr("opCode"))
                        .acceptAddress(route.getStr("acceptAddress"))
                        .build();
                routes.add(routeVO);
            }
        }

        return routes;
    }

    /**
     * 解析日期时间
     */
    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, SF_DATE_FORMAT);
        } catch (Exception e) {
            log.warn("解析日期失败: {}", dateStr);
            return null;
        }
    }

    /**
     * 根据路由更新物流状态
     */
    private void updateStatusFromRoutes(OrderLogistics logistics, List<LogisticsRouteVO> routes) {
        if (routes.isEmpty()) {
            return;
        }

        // 检查最新的路由状态
        for (LogisticsRouteVO route : routes) {
            String opCode = route.getOpCode();
            if (opCode == null) continue;

            // 80=已签收
            if ("80".equals(opCode)) {
                if (logistics.getStatus() < 3) {
                    logistics.setStatus(3);
                    logistics.setUpdateTime(LocalDateTime.now());
                    logisticsMapper.updateById(logistics);
                }
                break;
            }
            // 运输中状态码
            if ("30".equals(opCode) || "31".equals(opCode) || "36".equals(opCode)) {
                if (logistics.getStatus() < 2) {
                    logistics.setStatus(2);
                    logistics.setUpdateTime(LocalDateTime.now());
                    logisticsMapper.updateById(logistics);
                }
            }
        }
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待发货";
            case 1: return "已下单";
            case 2: return "运输中";
            case 3: return "已签收";
            default: return "未知";
        }
    }
}
