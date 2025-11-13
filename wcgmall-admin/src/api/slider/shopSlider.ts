import request from '@/utils/request'

/**
 * 获取列表
 */
export function listShopSliderApi(params?: any) {
    return request({
        url: '/sys/shopSlider/list',
        method: 'get',
        params
    })
}

/**
 * 获取详情
 */
export function detailShopSliderApi(id: any) {
    return request({
        url: '/sys/shopSlider/' + id,
        method: 'get'
    })
}

/**
 * 添加
 */
export function addShopSliderApi(data: any) {
    return request({
        url: '/sys/shopSlider/add',
        method: 'post',
        data
    })
}

/**
 * 修改
 */
export function updateShopSliderApi(data: any) {
    return request({
        url: `/sys/shopSlider/update`,
        method: 'put',
        data
    })
}


/**
 * 删除
 */
export function deleteShopSliderApi(ids: number[] | number) {
    return request({
        url: `/sys/shopSlider/delete/` + ids,
        method: 'delete'
    })
}


