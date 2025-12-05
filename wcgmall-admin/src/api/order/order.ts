import request from '@/utils/request'

/**
 * 获取列表
 */
export function listOrderApi(params?: any) {
    return request({
        url: '/sys/order/list',
        method: 'get',
        params
    })
}

/**
 * 获取信息
 */
export function detailOrderApi(id: any) {
    return request({
        url: '/sys/order/' + id,
        method: 'get'
    })
}


/**
 * 获取详情
 */
export function getOrderDetailApi(orderNumber: any) {
    return request({
        url: '/sys/order/detail/' + orderNumber,
        method: 'get'
    })
}

/**
 * 添加
 */
export function addOrderApi(data: any) {
    return request({
        url: '/sys/order/add',
        method: 'post',
        data
    })
}

/**
 * 修改
 */
export function updateOrderApi(data: any) {
    return request({
        url: `/sys/order/update`,
        method: 'put',
        data
    })
}


/**
 * 删除
 */
export function deleteOrderApi(ids: number[] | number) {
    return request({
        url: `/sys/order/delete/` + ids,
        method: 'delete'
    })
}




