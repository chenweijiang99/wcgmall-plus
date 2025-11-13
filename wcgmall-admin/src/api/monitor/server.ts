/**
 * 服务器信息接口
 */

import request from '@/utils/request'

// 获取服务器信息
export function getServerInfoApi() {
  return request<any>({
    url: '/monitor/server',
    method: 'get'
  })
} 