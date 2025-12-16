import request from '@/utils/request'

/**
 * 获取评价列表
 */
export function listReviewApi(params?: any) {
    return request({
        url: '/sys/review/list',
        method: 'get',
        params
    })
}

/**
 * 获取评价详情
 */
export function getReviewDetailApi(id: number) {
    return request({
        url: '/sys/review/' + id,
        method: 'get'
    })
}

/**
 * 管理员回复评价
 */
export function replyReviewApi(data: { parentReviewId: number; content: string }) {
    return request({
        url: '/sys/review/reply',
        method: 'post',
        data
    })
}

/**
 * 删除评价
 */
export function deleteReviewApi(id: number) {
    return request({
        url: '/sys/review/delete/' + id,
        method: 'delete'
    })
}
