import request from "@/utils/request";

// 提交订单
export const submitOrderApi = (data: any) => {
  return request.post("/user/productOrder/submit", data);
};

// 支付宝支付
export const alipayPayApi = (orderNumber: string) => {
  return request.get(`/pay/alipay/${orderNumber}`);
};

export const getOrderStatusApi = (orderNumber: string) => {
  return request.get(`/pay/query/${orderNumber}`);
};

// 获取订单列表
export const getOrderListApi = (params: any) => {
  return request.get("/user/productOrder/page", params);
};

// 获取订单详情
export const getOrderDetailApi = (orderNumber: string) => {
  return request.get(`/user/productOrder/detail/${orderNumber}`);
};

// 取消订单
export const cancelOrderApi = (orderNumber: string) => {
  return request.post(`/user/productOrder/cancel/${orderNumber}`);
};

// 申请退款
export const refundOrderApi = (orderNumber: string) => {
  return request.post(`/user/productOrder/refund/${orderNumber}`);
};

// 确认收货
export const confirmReceiptApi = (orderNumber: string) => {
  return request.post(`/user/productOrder/confirm/${orderNumber}`);
};

// 删除订单
export const deleteOrderApi = (orderNumber: string) => {
  return request.delete(`/user/productOrder/${orderNumber}`);
};