/**
 * 代码生成接口
 */
import request from '@/utils/request'

// 查询表列表
export function listTableApi(params: any) {
  return request({
    url: '/tool/gen/list',
    method: 'get',
    params
  })
}

// 预览生成代码
export function previewTableApi(tableId: string | number) {
  return request({
    url: '/tool/gen/preview/' + tableId,
    method: 'get'
  })
}

// 删除表数据
export function delTableApi(tableId: string | number) {
  return request({
    url: '/tool/gen/' + tableId,
    method: 'delete'
  })
}

// 生成代码
export function downloadCodeApi(tableNames: string) {
  return request({
    url: '/tool/gen/download/' + tableNames,
    method: 'get',
    responseType: 'blob'
  })
}

// 同步数据库
export function synchDbApi(tableName: string) {
  return request({
    url: '/tool/gen/sync/' + tableName,
    method: 'get'
  })
}

// 查询数据库表列表
export function listDbTableApi(params: any) {
  return request({
    url: '/tool/gen/db/list',
    method: 'get',
    params
  })
}

// 导入表结构
export function importTableApi(tables: string[]) {
  return request({
    url: '/tool/gen/importTable',
    method: 'post',
    data: tables
  })
} 