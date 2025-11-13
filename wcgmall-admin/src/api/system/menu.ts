/**
 * 菜单接口
 */
import request from '@/utils/request'

/**
 * 获取菜单列表
 */
export function getMenuListApi() {
  return request({
    url: '/sys/menu/tree',
    method: 'get'
  })
}

/**
 * 获取菜单详情
 */
export function getMenuDetailApi(id: string) {
  return request({
    url: `/sys/menu/${id}`,
    method: 'get'
  })
}

/**
 * 新增菜单
 */
export function createMenuApi(data: any) {
  return request({
    url: '/sys/menu',
    method: 'post',
    data
  })
}

/**
 * 修改菜单
 */
export function updateMenuApi(data: any) {
  return request({
    url: `/sys/menu`,
    method: 'put',
    data
  })
}

/**
 * 删除菜单
 */
export function deleteMenuApi(id: string) {
  return request({
    url: `/sys/menu/${id}`,
    method: 'delete'
  })
}


export function listRoutes() {
  return request({
    url: '/sys/menu',
    method: 'post',
  })
}