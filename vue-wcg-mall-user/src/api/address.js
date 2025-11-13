import request from '@/utils/request.js'

export const userGetAddressListService = ()=> {
    return request.get('/user/address/list');
}

export const userAddAddressService = (data)=> {
    return request.post('/user/address/add',data);
}

export const userUpdateAddressService = (data)=> {
    return request.put('/user/address/update',data);
}

export const userDeleteAddressService = (id)=> {
    return request.delete('/user/address/delete/'+id);
}