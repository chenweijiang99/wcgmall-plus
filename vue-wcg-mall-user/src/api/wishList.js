import request from '@/utils/request.js'

export const userGetWishListService = ()=> {
   return request.get('/user/wishList');
}

export const userDeleteWishListService = (productId)=> {
   // return request.delete('/user/wishList/' + id);
   return request({
        url: '/user/wishList/' + productId,
        method: 'delete'
    })
}

export const userAddWishListProdcutToCartService = (productId)=> {
   return request({
        url: '/user/wishList/' + productId,
        method: 'post'
    })
}
