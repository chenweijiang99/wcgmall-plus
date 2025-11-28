import request from '@/utils/request'

export function getCartApi() {
  return request({
    url: '/user/shoppingCart',
    method: 'get',
  })
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
    url: '/user/shoppingCart',
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