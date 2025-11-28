import request from '@/utils/request'
import { ApiResponse, Brand,} from '@/types';
// 品牌列表
export function getBrandListApi() {
    return request.get<any, ApiResponse<Brand[]>>('/user/brand');
}