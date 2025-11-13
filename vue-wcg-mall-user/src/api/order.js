import request from '@/utils/request.js'
export const userGerOrderListService = ()=> {
  return request.get('/user/order')
}

export const userAddOrderService = (data)=> {
 
  return request.post('/user/order',data)
}

export const userGetOrderStatusService = (orderNumber)=> {
  return request.get('/user/order/getOrderStatus?orderNumber='+orderNumber)
}


export const userGetORderDetailService = (orderNumber)=> {
  return request.get('/user/order/getOrderDetail?orderNumber='+orderNumber)
}

export const userConfirmsService = (orderNumber)=>{
  return request.put('/user/order/confirmReceipt/'+orderNumber)
}
export const userCancelOrderService = (orderNumber)=>{
  return request.put('/user/order/cancelOrder/'+orderNumber)
}
export const userRefundService = (orderNumber)=>{
  return request.put('/user/alipay/refund?orderNumber='+orderNumber)
}