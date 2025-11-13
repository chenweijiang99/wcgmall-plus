/**
 * 角色接口
 */
import request from '@/utils/request'

/**
 * 获取角色列表
 */
export function getRoleListApi(params?: any) {
  return request({
    url: '/sys/role',
    method: 'get',
    params
  })
}

/**
 * 添加角色
 */
export function createRoleApi(data: any) {
  return request({
    url: '/sys/role',
    method: 'post',
    data
  })
}

/**
 * 修改角色
 */
export function updateRoleApi(data: any) {
  return request({
    url: `/sys/role`,
    method: 'put',
    data
  })
}


/**
 * 删除角色
 */
export function deleteRoleApi(ids: number[] | number) {
  return request({
    url: `/sys/role/delete/${ids}`,
    method: 'delete'
  })
} 

/**
 * 获取角色的菜单权限
 */
export function getRoleMenusApi(roleId: number) {
  return request({
    url: `/sys/role/menus/${roleId}`,
    method: 'get'
  })
}

/**
 * 修改角色的菜单权限
 */
export function updateRoleMenusApi(roleId: any, menuIds: any) {
  return request({
    url: `/sys/role/menus/${roleId}`,
    method: 'put',
    data: menuIds
  })
}

/**
 * 获取角色列表
 */
export function getAllRoleList() {
  return request({
    url: '/sys/role/all',
    method: 'get'
  })
}

