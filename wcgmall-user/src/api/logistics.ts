import request from '../utils/request'

/**
 * 查询订单物流信息
 */
export function getOrderLogistics(orderNumber: string) {
    return request({
        url: `/user/productOrder/logistics/${orderNumber}`,
        method: 'get'
    })
}
