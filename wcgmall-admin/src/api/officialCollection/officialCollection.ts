import request from '@/utils/request'

/**
 * 获取列表
 */
export function listOfficialCollectionApi() {
    return request({
        url: '/sys/officialCollection/all',
        method: 'get',
    })
}


/**
 * 添加
 */
export function addOfficialCollectionApi(productId: any) {
    return request({
        url: '/sys/officialCollection/add/' + productId,
        method: 'put',
    })
}


/**
 * 删除
 */
export function deleteOfficialCollectionApi(productId: any) {
    return request({
        url: `/sys/officialCollection/delete/` + productId,
        method: 'delete'
    })
}


