import request from '@/utils/request'

/**
 * 获取列表
 */
export function listBlogApi(params?: any) {
    return request({
        url: '/sys/blog/list',
        method: 'get',
        params
    })
}

/**
 * 获取详情
 */
export function detailBlogApi(id: any) {
    return request({
        url: '/sys/blog/' + id,
        method: 'get'
    })
}

/**
 * 添加
 */
export function addBlogApi(data: any) {
    return request({
        url: '/sys/blog/add',
        method: 'post',
        data
    })
}

/**
 * 修改
 */
export function updateBlogApi(data: any) {
    return request({
        url: `/sys/blog/update`,
        method: 'put',
        data
    })
}


/**
 * 删除
 */
export function deleteBlogApi(ids: number[] | number) {
    return request({
        url: `/sys/blog/delete/` + ids,
        method: 'delete'
    })
}

export function updateStatusApi(data: any) {
    return request({
        url: `/sys/blog/update/status`,
        method: 'put',
        data
    })
}
