import request from '@/utils/request'

/**
 * 获取列表
 */
export function listCommentsApi(params?: any) {
    return request({
        url: '/sys/comments/list',
        method: 'get',
        params
    })
}

/**
 * 获取详情
 */
export function detailCommentsApi(id: any) {
    return request({
        url: '/sys/comments/' + id,
        method: 'get'
    })
}

/**
 * 添加
 */
export function addCommentsApi(data: any) {
    return request({
        url: '/sys/comments/add',
        method: 'post',
        data
    })
}

/**
 * 修改
 */
export function updateCommentsApi(data: any) {
    return request({
        url: `/sys/comments/update`,
        method: 'put',
        data
    })
}


/**
 * 删除
 */
export function deleteCommentsApi(ids: number[] | number) {
    return request({
        url: `/sys/comments/delete/` + ids,
        method: 'delete'
    })
}


