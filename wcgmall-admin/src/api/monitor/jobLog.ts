/**
 * 定时任务日志接口
 */
import request from '@/utils/request'

// 查询调度日志列表
export function listJobLogApi(query: any) {
  return request({
    url: '/monitor/jobLog/list',
    method: 'get',
    params: query
  })
}

// 删除调度日志
export function delleteJobLogApi(id: any) {
    return request({
      url: `/monitor/jobLog/delete/${id}`,
      method: 'delete'
    })
  }

// 清空调度日志
export function cleanJobLogApi() {
  return request({
    url: '/monitor/jobLog/clean',
    method: 'delete'
  })
} 