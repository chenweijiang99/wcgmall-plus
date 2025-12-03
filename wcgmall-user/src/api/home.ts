import request from '@/utils/request'
import { ApiResponse, Blog, IndexSlider, Product, OfficialCollection } from '@/types';
// 获取首页官方收藏数据
export function getOLApi() {
    return request.get<any, ApiResponse<OfficialCollection[]>>('/user/index/getOL');
}

// 获取首页最新商品数据
export function getNewProductApi() {
    return request.get<any, ApiResponse<Product[]>>('/user/index/getNewProduct');
}

// 获取首页最新博客数据
export function getNewBlogApi() {
    return request.get<any, ApiResponse<Blog[]>>('/user/index/getNewBlog');
}
// 获取轮播图数据 
export function getIndexSliderApi() {
    return request.get<any, ApiResponse<IndexSlider[]>>('/user/index/getIndexSlider');
}
