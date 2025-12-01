import request from '@/utils/request'
import { ApiResponse,Cart} from '@/types';
export function getCartApi() {
   return request.get<any, ApiResponse<Cart[]>>('/user/shoppingCart');
}

export function addShoppingCartApi(productId: number) {
  return request({
    url: '/user/shoppingCart',
    method: 'post',
    params: { productId }
  })
}

export function deleteShoppingCartApi(productId: number) {
  return request({
    url: '/user/shoppingCart/delete',
    method: 'delete',
    params: { productId }
  })
}

export function reduceProductApi(productId: number) {
  return request({
    url: '/user/shoppingCart/reduceProduct',
    method: 'post',
    params: { productId }
  })
}

export function addProductApi(productId: number) {
  return request({
    url: '/user/shoppingCart/addProduct',
    method: 'post',
    params: { productId }
  })
}

export function addCarProductToWishListApi(productId: number) {
  return request({
    url: '/user/shoppingCart/addCarProductToWishList',
    method: 'post',
    params: { productId }
  })
}