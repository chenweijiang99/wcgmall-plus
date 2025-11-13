import request from '@/utils/request.js'
export const userGetBrandListService = ()=> {
    return request.get('/user/brand');
}