import request from '@/utils/request.js'

export const userGetCategoryListService = ()=> {
    return request.get('/user/productCategory')
}