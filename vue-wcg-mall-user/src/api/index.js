import request from '@/utils/request.js'

export const userGetOLListService = () => {
    return request.get('/user/index/getOL');
}
export const userGetNewProductService = () => {
    return request.get('/user/index/getNewProduct');
}
export const userGetNewBlogService = () => {
    return request.get('/user/index/getNewBlog');
}
export const userGetIndexSliderService = () => {
    return request.get('/user/index/getIndexSlider');
}