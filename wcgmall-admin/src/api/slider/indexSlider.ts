import request from '@/utils/request'

/**
 * 获取列表
 */
export function listIndexSliderApi(params?: any) {
    return request({
        url: '/sys/indexSlider/list',
        method: 'get',
        params
    })
}

/**
 * 获取详情
 */
export function detailIndexSliderApi(id: any) {
    return request({
        url: '/sys/indexSlider/' + id,
        method: 'get'
    })
}

/**
 * 添加
 */
export function addIndexSliderApi(data: any) {
    return request({
        url: '/sys/indexSlider/add',
        method: 'post',
        data
    })
}

/**
 * 修改
 */
export function updateIndexSliderApi(data: any) {
    return request({
        url: `/sys/indexSlider/update`,
        method: 'put',
        data
    })
}


/**
 * 删除
 */
export function deleteIndexSliderApi(ids: number[] | number) {
    return request({
        url: `/sys/indexSlider/delete/` + ids,
        method: 'delete'
    })
}


