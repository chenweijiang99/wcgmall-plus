import request from '@/utils/request.js'

export const userGetProductListService = ()=> {
    return request.get('/user/product');
}

export const userGetProductByIdService = (id)=> {
    return request.get('/user/product/getProductDetail?id=' + id);
}
export const userAddProductToWishListService = (id)=> {
    return request.put('/user/product/addToWishList/'+id);
}

export const userFilterProductService = (filterData)=> {
    return request.post('/user/product/filter',filterData);
}

export const userSearchProductService = (searcheQuery)=>{
    return request.get('/user/product/search?searchQuery='+searcheQuery)
}

export const userGetShopSliderService = ()=>{
    return request.get('/user/product/getShopSlider')
}

export const userGetProductPageService = (data)=>{
    // return request({
    //     url: '/user/product/page',
    //     method: 'get',
    //     data
    // })
    const params = new URLSearchParams();
    for (let key in data) {
        params.append(key, data[key]);
    }
    return request({
        url: '/user/product/page',
        method: 'get',
        params
    })
}