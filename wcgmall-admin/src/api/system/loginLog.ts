/**
 * 登录日志接口
 */
import request from '@/utils/request'

// 获取登录日志列表
export function getLoginLogsApi(params: any) {
  return request<any>({
    url: '/sys/loginLog',
    method: 'get',
    params
  })
}

// 批量删除登录日志
export function deleteLoginLogsApi(ids: any) {
  return request({
    url: `/sys/loginLog/delete/${ids}`,
    method: 'delete'
  })
}

// 清空登录日志
export function cleanLoginLogsApi() {
  return request({
    url: '/sys/loginLog/clean',
    method: 'delete'
  })
}
