import request from '@/utils/request'
import { ApiResponse, Address } from '@/types';

// 获取地址簿
export function getAddtessListApi() {
    return request.get<any, ApiResponse<Address[]>>('/user/address/list');
}

// 修改地址
export function updateAddressApi(data: any) {
    return request.put<any, ApiResponse<boolean>>(`/user/address/update`, data);
}

// 添加地址
export function addAddressApi(data: any) {
    return request.post<any, ApiResponse<boolean>>(`/user/address/add`, data);
}

// 删除地址
export function deleteAddressApi(id: number) {
    return request.delete<any, ApiResponse<boolean>>(`/user/address/delete/${id}`);
}