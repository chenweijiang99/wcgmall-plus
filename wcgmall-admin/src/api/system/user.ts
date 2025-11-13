/**
 * 用户接口
 */
import request from '@/utils/request'

/**
 * 获取用户列表
 */
export function getUserListApi(params?: any) {
  return request({
    url: '/sys/user',
    method: 'get',
    params
  })
}

/**
 * 获取用户详情
 */
export function getUserDetailApi(id: string) {
  return request({
    url: `/sys/user/${id}`,
    method: 'get'
  })
}

/**
 * 新增用户
 */
export function createUserApi(data: any) {
  return request({
    url: '/sys/user',
    method: 'post',
    data
  })
}

/**
 * 修改用户
 */
export function updateUserApi(data: any) {
  return request({
    url: `/sys/user`,
    method: 'put',
    data
  })
}

/**
 * 删除用户
 */
export function deleteUserApi(ids: string[] | number) {
  return request({
    url: `/sys/user/delete/${ids}`,
    method: 'delete'
  })
}

/**
 * 重置密码
 */
export function resetPasswordApi(data: any) {
  return request({
    url: '/sys/user/reset',
    method: 'put',
    data
  })
}


// 获取用户个人信息
export function getUserProfileApi() {
  return request({
    url: '/sys/user/profile',
    method: 'get'
  })
}

// 修改用户个人信息
export function updateUserProfileApi(data: any) {
  return request({
    url: '/sys/user/updProfile',
    method: 'put',
    data: data
  })
}

// 用户密码重置
export function updateUserPwdApi(oldPassword: string, newPassword: string) {
  const data = {
    oldPassword,
    newPassword
  }
  return request({
    url: '/sys/user/updatePwd',
    method: 'put',
    data
  })
}

// 用户头像上传
export function uploadAvatar(data: any) {
  return request({
    url: '/system/user/profile/avatar',
    method: 'post',
    data: data
  })
}

export function verifyPassword(password: string) {
  return request({
    url: `/sys/user/verifyPassword/${password}`,
    method: 'get'
  })
}

// 获取在线用户
export function getOnlineUserApi(params: any) {
  return request<any>({
    url: '/monitor/online/list',
    method: 'get',
    params
  })
}

// 强制下线
export function forceLogoutApi(token: string) {
  return request<any>({
    url: `/monitor/online/forceLogout/${token}`,
    method: 'get'
  })
}


