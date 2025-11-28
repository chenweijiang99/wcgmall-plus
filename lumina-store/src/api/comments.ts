import request from '@/utils/request'
import { ApiResponse, CommentView } from '@/types';
// 发表评论
export function addCommentsApi(data: any) {
    return request({
        url: '/user/comments/addComments',
        method: 'post',
        data
    })
}

// 查询评论树
export function selectTreeApi(params: any) {
    return request.get<any, ApiResponse<{ records: CommentView[], total: number }>>('/user/comments/selectTree', { params });
}
