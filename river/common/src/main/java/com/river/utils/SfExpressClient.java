package com.river.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.river.config.SfExpressConfig;
import com.river.dto.ShipOrderDTO;
import com.river.entity.ProductOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 顺丰快递API客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SfExpressClient {

    private final SfExpressConfig config;

    /**
     * 生成签名
     * 官方文档签名规则：
     * 1. 拼接：msgData + timestamp + checkWord
     * 2. URLEncode编码
     * 3. MD5加密
     * 4. Base64编码
     */
    private String generateDigest(String msgData, long timestamp) {
        try {
            // 步骤1: 拼接字符串：msgData + timestamp + checkWord
            String toVerifyText = msgData + timestamp + config.getCheckWord();
            log.debug("签名原始字符串: {}", toVerifyText);

            // 步骤2: URLEncode处理（因业务报文中可能包含加号、空格等特殊字符）
            toVerifyText = URLEncoder.encode(toVerifyText, StandardCharsets.UTF_8);
            log.debug("URL编码后: {}", toVerifyText);

            // 步骤3: MD5加密
            byte[] md5Bytes = DigestUtil.md5(toVerifyText);

            // 步骤4: Base64编码生成数字签名
            String msgDigest = Base64.encode(md5Bytes);
            log.debug("最终签名: {}", msgDigest);

            return msgDigest;
        } catch (Exception e) {
            log.error("生成签名失败", e);
            throw new RuntimeException("生成签名失败", e);
        }
    }

    /**
     * 发送请求到顺丰API
     */
    private JSONObject sendRequest(String serviceCode, JSONObject msgData) {
        long timestamp = Instant.now().toEpochMilli();
        String requestId = IdUtil.fastSimpleUUID();
        String msgDataStr = msgData.toString();
        String digest = generateDigest(msgDataStr, timestamp);  

        log.info("顺丰API请求 - serviceCode: {}, requestId: {}", serviceCode, requestId);
        log.debug("顺丰API请求 - msgData: {}", msgDataStr);

        try {
            // 构建表单参数
            Map<String, Object> formData = new HashMap<>();
            formData.put("partnerID", config.getPartnerId());
            formData.put("requestID", requestId);
            formData.put("serviceCode", serviceCode);
            formData.put("timestamp", String.valueOf(timestamp));
            formData.put("msgDigest", digest);
            // 注意：Hutool的form方法会自动进行URL编码，不需要手动编码
            formData.put("msgData", msgDataStr);

            HttpResponse response = HttpRequest.post(config.getUrl())
                    .contentType("application/x-www-form-urlencoded;charset=UTF-8")
                    .form(formData)
                    .timeout(30000)
                    .execute();

            String body = response.body();
            log.info("顺丰API响应: {}", body);

            // 顺丰API可能返回XML或JSON格式
            JSONObject result;
            if (body.trim().startsWith("<?xml") || body.trim().startsWith("<")) {
                // XML格式响应，手动解析
                result = parseXmlResponse(body);
            } else {
                // JSON格式响应
                result = JSONUtil.parseObj(body);
            }

            // 检查API级别错误
            String apiResultCode = result.getStr("apiResultCode");
            if (!"A1000".equals(apiResultCode)) {
                log.error("顺丰API调用失败 - code: {}, message: {}",
                        apiResultCode, result.getStr("apiErrorMsg"));
                throw new RuntimeException("顺丰API调用失败: " + result.getStr("apiErrorMsg"));
            }

            // 解析业务数据
            String msgDataResp = result.getStr("apiResultData");
            if (msgDataResp != null) {
                return JSONUtil.parseObj(msgDataResp);
            }
            return new JSONObject();
        } catch (Exception e) {
            log.error("顺丰API请求异常", e);
            throw new RuntimeException("顺丰API请求异常: " + e.getMessage(), e);
        }
    }

    /**
     * 解析XML格式的响应
     */
    private JSONObject parseXmlResponse(String xml) {
        JSONObject result = new JSONObject();
        try {
            // 简单的XML解析，提取关键字段
            String apiResultCode = extractXmlValue(xml, "apiResultCode");
            String apiErrorMsg = extractXmlValue(xml, "apiErrorMsg");
            String apiResponseID = extractXmlValue(xml, "apiResponseID");
            String apiResultData = extractXmlValue(xml, "apiResultData");

            result.set("apiResultCode", apiResultCode);
            result.set("apiErrorMsg", apiErrorMsg);
            result.set("apiResponseID", apiResponseID);
            result.set("apiResultData", apiResultData);
        } catch (Exception e) {
            log.error("解析XML响应失败", e);
        }
        return result;
    }

    /**
     * 从XML中提取指定标签的值
     */
    private String extractXmlValue(String xml, String tagName) {
        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";
        int start = xml.indexOf(startTag);
        int end = xml.indexOf(endTag);
        if (start >= 0 && end > start) {
            return xml.substring(start + startTag.length(), end);
        }
        return null;
    }

    /**
     * 下单接口
     *
     * @param shipDTO 发货信息
     * @param order   订单信息
     * @return 下单结果
     */
    public JSONObject createOrder(ShipOrderDTO shipDTO, ProductOrder order) {
        JSONObject msgData = new JSONObject();

        // 语言 - 注意：官方示例使用zh_CN（下划线）
        msgData.set("language", "zh_CN");

        // 客户订单号
        msgData.set("orderId", order.getOrderNumber());

        // 快件产品类别 - 1:顺丰标快（非必填，但建议填写）
        msgData.set("expressTypeId", 1);

        // 付款方式 - 1:寄方付（非必填，但建议填写）
        msgData.set("payMethod", 1);

        // 是否返回运单号 - 1:返回（非必填，但建议填写）
        msgData.set("isReturnWaybillNo", 1);

        // 联系人信息列表
        List<Map<String, Object>> contactInfoList = new ArrayList<>();

        // 寄件人
        Map<String, Object> sender = new HashMap<>();
        sender.put("contactType", 1); // 1-寄件方
        sender.put("contact", shipDTO.getSenderName());
        sender.put("tel", shipDTO.getSenderPhone());
        sender.put("address", shipDTO.getSenderAddress());
        sender.put("province", shipDTO.getSenderProvince());
        sender.put("city", shipDTO.getSenderCity());
        sender.put("county", shipDTO.getSenderCounty());
        sender.put("country", "CN"); // 国家代码
        contactInfoList.add(sender);

        // 收件人 - 从订单获取
        Map<String, Object> receiver = new HashMap<>();
        receiver.put("contactType", 2); // 2-收件方
        receiver.put("contact", order.getConsignee());
        receiver.put("tel", order.getConsigneePhone());
        receiver.put("address", order.getConsigneeAddress());
        receiver.put("country", "CN"); // 国家代码
        // 解析地址中的省市区
        parseAddress(order.getConsigneeAddress(), receiver);
        contactInfoList.add(receiver);

        msgData.set("contactInfoList", contactInfoList);

        // 货物信息
        List<Map<String, Object>> cargoDetails = new ArrayList<>();
        Map<String, Object> cargo = new HashMap<>();
        cargo.put("name", "商品");
        cargo.put("count", 1);
        cargo.put("unit", "件"); // 单位
        cargo.put("weight", 0.5); // 重量（kg）
        cargoDetails.add(cargo);
        msgData.set("cargoDetails", cargoDetails);

        return sendRequest(config.getCreateOrderServiceCode(), msgData);
    }

    /**
     * 解析地址获取省市区
     * 简单处理：假设地址格式为 "省市区具体地址"
     */
    private void parseAddress(String address, Map<String, Object> receiver) {
        // 尝试匹配常见的省份
        String[] provinces = {"北京市", "上海市", "天津市", "重庆市", "广东省", "江苏省", "浙江省",
                "山东省", "河南省", "四川省", "湖北省", "湖南省", "河北省", "福建省", "陕西省",
                "辽宁省", "江西省", "安徽省", "广西壮族自治区", "云南省", "贵州省", "山西省",
                "海南省", "黑龙江省", "吉林省", "甘肃省", "内蒙古自治区", "宁夏回族自治区",
                "新疆维吾尔自治区", "西藏自治区", "青海省"};

        for (String province : provinces) {
            if (address.startsWith(province)) {
                receiver.put("province", province);
                String rest = address.substring(province.length());
                // 尝试提取市
                int cityEnd = rest.indexOf("市");
                if (cityEnd > 0) {
                    String city = rest.substring(0, cityEnd + 1);
                    receiver.put("city", city);
                    rest = rest.substring(cityEnd + 1);
                    // 尝试提取区/县
                    int countyEnd = Math.max(rest.indexOf("区"), rest.indexOf("县"));
                    if (countyEnd > 0) {
                        receiver.put("county", rest.substring(0, countyEnd + 1));
                    }
                }
                return;
            }
        }

        // 直辖市特殊处理
        if (address.startsWith("北京") || address.startsWith("上海") ||
                address.startsWith("天津") || address.startsWith("重庆")) {
            String city = address.substring(0, 2) + "市";
            receiver.put("province", city);
            receiver.put("city", city);
        }
    }

    /**
     * 路由查询接口
     *
     * @param waybillNo 运单号
     * @return 路由信息
     */
    public JSONObject searchRoutes(String waybillNo) {
        JSONObject msgData = new JSONObject();
        msgData.set("language", "zh-CN");
        msgData.set("trackingType", 1); // 1-根据顺丰运单号查询
        msgData.set("trackingNumber", waybillNo);
        msgData.set("methodType", 1); // 1-标准查询

        return sendRequest(config.getSearchRoutesServiceCode(), msgData);
    }

    /**
     * 根据订单号查询路由
     *
     * @param orderId 客户订单号
     * @return 路由信息
     */
    public JSONObject searchRoutesByOrderId(String orderId) {
        JSONObject msgData = new JSONObject();
        msgData.set("language", "zh-CN");
        msgData.set("trackingType", 2); // 2-根据客户订单号查询
        msgData.set("trackingNumber", orderId);
        msgData.set("methodType", 1);

        return sendRequest(config.getSearchRoutesServiceCode(), msgData);
    }
}
