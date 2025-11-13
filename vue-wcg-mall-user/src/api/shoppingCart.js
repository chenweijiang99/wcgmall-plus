import request from '@/utils/request.js'

export const userGetShoppingCartService = ()=> {
    return request.get('/user/shoppingCart');
}

export const userDeleteCartService = (id)=> {
    return request.delete('/user/shoppingCart?productId='+id);
}

export const userAddProductToShoppingCartService = (id)=> {
    return request.post('/user/shoppingCart?productId='+id);
}

export const userDeleteCartDataService = (id)=> {
    return request.delete('/user/shoppingCart/delete?productId='+id);
}
export const userReduceCartDataService = (id)=> {
    return request.post('/user/shoppingCart/reduceProduct?productId='+id);
}
export const userAddProductToShoppingCartDataService = (id)=> {
    return request.post('/user/shoppingCart/addProduct?productId='+id);
}
export const userAddCartProductToWishListService = (id)=> {
    return request.post('/user/shoppingCart/addCarProductToWishList?productId='+id);
}