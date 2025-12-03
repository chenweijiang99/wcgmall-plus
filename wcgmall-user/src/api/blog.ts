import request from '@/utils/request'
import { ApiResponse, Blog,} from '@/types';
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
    return request.get<any, ApiResponse<{ records: Blog[], total: number }>>('/user/blog/page', { params });
}

export function getUserBlogApi() {
    return request.get<any, ApiResponse<Blog[]>>('/user/blog/userBlog');
}

export function getBlogDetailApi(id : number) {
    return request.get<any, ApiResponse<Blog>>(`/user/blog/blogDetail/${id}`);
}

export function deleteBlogApi(id : number) {
    return request.delete<any, ApiResponse<boolean>>(`/user/blog/delete/${id}`);
}

