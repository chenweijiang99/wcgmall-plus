import request from '@/utils/request'
import { ApiResponse, CommentView } from '@/types';
// 发表评论
export function addCommentsApi(data: any) {
    return request.post<any, ApiResponse<boolean>>('/user/comments/addComments', data);
}

// 查询评论树
export function selectTreeApi(params: any) {
    return request.get<any, ApiResponse<{ records: CommentView[], total: number }>>('/user/comments/selectTree', { params });
}

export const getCountApi = (fid: any, module: any) => {
    return request.get<any, ApiResponse<number>>(`/user/comments/selectCount/${fid}/${module}`);
}

export const deleteCommentsApi = (id: any) => {
    return request.delete<any, ApiResponse<boolean>>('/user/comments/deleteComments?id=' + id);
}