import request from '@/utils/request'

/**
 * 获取列表
 */
export function listProductApi(params?: any) {
    return request({
        url: '/sys/product/list',
        method: 'get',
        params
    })
}

/**
 * 获取详情
 */
export function detailProductApi(id: any) {
    return request({
        url: '/sys/product/' + id,
        method: 'get'
    })
}

/**
 * 添加
 */
export function addProductApi(data: any) {
    return request({
        url: '/sys/product/add',
        method: 'post',
        data
    })
}

/**
 * 修改
 */
export function updateProductApi(data: any) {
    return request({
        url: `/sys/product/update`,
        method: 'put',
        data
    })
}


/**
 * 删除
 */
export function deleteProductApi(ids: number[] | number) {
    return request({
        url: `/sys/product/delete/` + ids,
        method: 'delete'
    })
}

/**
 * 上架下架
 */
export function startOrStopApi(data: any){
    return request({
        url: `/sys/product/startOrStop`,
        method: `put`,
        data
    })
}


