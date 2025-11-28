import request from '@/utils/request'

// 修改博客
export function updateBlogApi(data: any) {
    return request({
        url: '/user/blog/update',
        method: 'put',
        data
    })
}

// 添加博客
export function addBlogApi(data: any) {
    return request({
        url: '/user/blog/add',
        method: 'post',
        data
    })
}

// 分页获取博客列表
export function getBlogPageApi(params: any) {
    return request({
        url: '/user/blog/page',
        method: 'get',
        params
    })
}
