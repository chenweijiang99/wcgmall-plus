import request from '@/utils/request'

/**
 * 获取列表
 */
export function listProductBrandApi(params?: any) {
    return request({
        url: '/sys/productBrand/list',
        method: 'get',
        params
    })
}

/**
 * 获取所有品牌
 */
export function listProductBrandAllApi(params?: any) {
    return request({
        url: '/sys/productBrand/all',
        method: 'get',
        params
    })
}

/**
 * 获取详情
 */
export function detailProductBrandApi(id: any) {
    return request({
        url: '/sys/productBrand/' + id,
        method: 'get'
    })
}

/**
 * 添加
 */
export function addProductBrandApi(data: any) {
    return request({
        url: '/sys/productBrand/add',
        method: 'post',
        data
    })
}

/**
 * 修改
 */
export function updateProductBrandApi(data: any) {
    return request({
        url: `/sys/productBrand/update`,
        method: 'put',
        data
    })
}


/**
 * 删除
 */
export function deleteProductBrandApi(ids: number[] | number) {
    return request({
        url: `/sys/productBrand/delete/` + ids,
        method: 'delete'
    })
}


