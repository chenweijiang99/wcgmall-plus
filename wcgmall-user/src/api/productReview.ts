import request from '@/utils/request'
import { ApiResponse } from '@/types';

// 商品评价相关接口

export interface ProductReviewDTO {
  orderNumber: string
  productId: number
  productScore: number
  logisticsScore: number
  merchantScore: number
  content: string
  imageList: string[]
  parentReviewId?: number
}

export interface ProductReviewVO {
  id: number
  orderNumber: string
  productId: number
  productName: string
  productImage: string
  userId: number
  username: string
  userAvatar: string
  productScore: number
  logisticsScore: number
  merchantScore: number
  content: string
  imageList: string[]
  parentReviewId?: number
  parentUsername?: string
  rootReviewId?: number
  children?: ProductReviewVO[]
  createTime: string
  updateTime: string
}

// 添加商品评价
export function addProductReviewApi(data: ProductReviewDTO) {
  return request.post<any, ApiResponse<boolean>>('/user/productReview/add', data);
}

// 查询商品评价树
export function getProductReviewTreeApi(params: { productId: number, pageNum: number, pageSize: number }) {
  return request.get<any, ApiResponse<{ records: ProductReviewVO[], total: number }>>('/user/productReview/tree', { params });
}

// 获取商品评价数量
export function getProductReviewCountApi(productId: number) {
  return request.get<any, ApiResponse<number>>(`/user/productReview/count/${productId}`);
}

// 删除评价
export function deleteProductReviewApi(id: number) {
  return request.delete<any, ApiResponse<boolean>>(`/user/productReview/${id}`);
}

// 查询待评价商品列表
export function getPendingReviewProductsApi() {
  return request.get<any, ApiResponse<ProductReviewVO[]>>('/user/productReview/pending');
}

// 评价统计VO
export interface ReviewStatisticsVO {
  totalCount: number
  goodCount: number
  mediumCount: number
  badCount: number
  goodRate: number
  avgProductScore: number
  avgLogisticsScore: number
  avgMerchantScore: number
  star1Count: number
  star2Count: number
  star3Count: number
  star4Count: number
  star5Count: number
  withImageCount: number
}

// 获取商品评价统计
export function getReviewStatisticsApi(productId: number) {
  return request.get<any, ApiResponse<ReviewStatisticsVO>>(`/user/productReview/statistics/${productId}`);
}

// 按评分筛选评价列表
export function getReviewByScoreApi(params: { productId: number, scoreType?: number, pageNum: number, pageSize: number }) {
  return request.get<any, ApiResponse<{ records: ProductReviewVO[], total: number }>>('/user/productReview/list', { params });
}
