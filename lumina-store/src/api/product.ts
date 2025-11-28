import request from '@/utils/request'
import { ApiResponse, Product,ProductView} from '@/types';
// 分页获取商品列表
export function getProductPageApi(params: any) {

     return request.get<any, ApiResponse<{ records: Product[], total: number }>>('/user/product/page', { params });
};

// 获取轮播图数据
export function getShopSliderApi() {
    return request({
        url: '/user/product/getShopSlider',
        method: 'get'
    })
}

// 商品详情
export function getProductDetailApi(id: number) {
    // return request({
    //     url: '/user/product/getProductDetail',
    //     method: 'get',
    //     params: { id }
    // })
    return request.get<any, ApiResponse<ProductView>>(`/user/product/getProductDetail/${id}`);
}


export function addToWishListApi(id: number) {
  return request({
    url: `/user/product/addToWishList/${id}`,
    method: 'put'
  })
}
