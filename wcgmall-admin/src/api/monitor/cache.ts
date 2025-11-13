/**
 * 缓存接口
 */
import request from '@/utils/request'

// 获取缓存信息
export function getCacheInfoApi() {
  return request({
    url: '/monitor/cache/info',
    method: 'get'
  })
}

// 获取缓存键名列表
export function getCacheKeyListApi(params?: any) {
  return request({
    url: '/monitor/cache/keys',
    method: 'get',
    params
  })
}

// 获取缓存内存信息
export function getCacheMemoryApi() {
  return request({
    url: '/monitor/cache/memory',
    method: 'get'
  })
}

// 清空缓存
export function clearCacheApi() {
  return request({
    url: '/monitor/cache',
    method: 'delete'
  })
} 