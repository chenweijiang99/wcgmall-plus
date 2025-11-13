import request from '@/utils/request.js'
export const userPayService = (orderNumber)=> {
 return request.get('/user/alipay?orderNumber='+orderNumber)
}