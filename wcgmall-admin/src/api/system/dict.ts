/**
 * 字典接口
 */
import request from '@/utils/request'

// 获取字典类型列表
export function getDictListApi(params: any) {
  return request<any>({
    url: '/sys/dict',
    method: 'get',
    params
  })
}

// 新增字典类型
export function addDictApi(data: any) {
  return request({
    url: '/sys/dict/add',
    method: 'post',
    data
  })
}

// 修改字典类型
export function updateDictApi(data: any) {
  return request({
    url: `/sys/dict/update`,
    method: 'put',
    data
  })
}

// 删除字典类型
export function deleteDictApi(id: number[] | number) {
  return request({
    url: `/sys/dict/delete/${id}`,
    method: 'delete'
  })
}

// 获取字典数据列表
export function getDictDataListApi(params: any) {
  return request<any>({
    url: `/sys/dictData/list`,
    method: 'get',
    params
  })
}

// 新增字典数据
export function addDictDataApi(data: any) {
  return request({
    url: '/sys/dictData/add',
    method: 'post',
    data
  })
}

// 修改字典数据
export function updateDictDataApi(data: any) {
  return request({
    url: `/sys/dictData/update`,
    method: 'put',
    data
  })
}

// 批量删除字典数据
export function deleteDictDataApi(ids: number[] | number) {
  return request({
    url: `/sys/dictData/delete/${ids}`,
    method: 'delete'
  })
} 


/**
 * 根据字典类型获取字典数据
 */
export function getDictDataByDictTypesApi(dictTypes : any) {
  return request({
    url: `/sys/dictData/getDiceData/${dictTypes}`,
    method: 'get'
  })
} 


/**
 * 获取字典数据
 */
export function getDictDataApi(dictType: any) {
  return request({
    url: `/sys/dictData/selectDataByDictTypeCache/${dictType}`,
    method: 'get'
  })
}