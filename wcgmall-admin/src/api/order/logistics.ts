import request from '@/utils/request'

/**
 * 发货 - 调用顺丰下单接口
 */
export function shipOrderApi(data: {
    orderNumber: string
    senderName: string
    senderPhone: string
    senderProvince: string
    senderCity: string
    senderCounty: string
    senderAddress: string
}) {
    return request({
        url: '/sys/logistics/ship',
        method: 'post',
        data
    })
}

/**
 * 查询物流轨迹
 */
export function getLogisticsApi(orderNumber: string) {
    return request({
        url: `/sys/logistics/routes/${orderNumber}`,
        method: 'get'
    })
}

/**
 * 根据运单号查询物流轨迹
 */
export function getLogisticsByWaybillNoApi(waybillNo: string) {
    return request({
        url: `/sys/logistics/waybill/${waybillNo}`,
        method: 'get'
    })
}
