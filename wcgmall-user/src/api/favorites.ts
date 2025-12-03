import request from '@/utils/request'
import { ApiResponse, Favorites } from '@/types';
export function getFavoritesApi() {
  return request.get<any, ApiResponse<Favorites[]>>('/user/wishList');
}

export function addWishListProductToCartApi(productId: number) {
  return request.post<any, ApiResponse<boolean>>(`/user/wishList/${productId}`);
}

export function deleteWishListApi(productId: number) {
  return request.delete<any, ApiResponse<boolean>>(`/user/wishList/${productId}`);
}