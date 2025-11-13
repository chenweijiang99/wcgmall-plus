import request from '@/utils/request'

/**
 * 获取列表
 */
export function listProductCategoryApi(params?: any) {
    return request({
        url: '/sys/productCategory/list',
        method: 'get',
        params
    })
}

/**
 * 获取所有分类

 */
export function listProductCategoryAllApi(params?: any) {
    return request({
        url: '/sys/productCategory/all',
        method: 'get',
        params
    })
}

/**
 * 获取详情
 */
export function detailProductCategoryApi(id: any) {
    return request({
        url: '/sys/productCategory/' + id,
        method: 'get'
    })
}

/**
 * 添加
 */
export function addProductCategoryApi(data: any) {
    return request({
        url: '/sys/productCategory/add',
        method: 'post',
        data
    })
}

/**
 * 修改
 */
export function updateProductCategoryApi(data: any) {
    return request({
        url: `/sys/productCategory/update`,
        method: 'put',
        data
    })
}


/**
 * 删除
 */
export function deleteProductCategoryApi(ids: number[] | number) {
    return request({
        url: `/sys/productCategory/delete/` + ids,
        method: 'delete'
    })
}


