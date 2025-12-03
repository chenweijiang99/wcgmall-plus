import request from '@/utils/request'
import { ApiResponse,Category} from '@/types';
// 获取分类列表
export function getProductCategoryApi() {
     return request.get<any, ApiResponse<Category[]>>('/user/productCategory');
}
