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