/**
 * 认证接口
 */
import request from '@/utils/request'

interface LoginParams {
  username: string
  password: string
  captchaCode: string
  captchaKey: string
  rememberMe: boolean
}

// 登录接口
export function loginApi(data: LoginParams) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function logoutApi() {
  return request({
    url: '/auth/logout',
    method: 'post',
  })
}

// 获取用户信息
export function getUserInfoApi() {
  return request({
    url: "/auth/info",
    method: "get",
    params: {
      source: "user"
    }
  })
}

// 更新用户信息
export function updateUserApi(data: any) {
  return request({
    url: '/user/user/update',
    method: 'put',
    data
  })
}


// 获取验证码
export function getCaptchaApi() {
  return request({
    url: '/auth/getCaptcha',
    method: 'get'
  })
}

// 获取验证码开关
export function getCaptchaSwitchApi() {
  return request({
    url: '/sys/config/getConfigByKey/slider_verify_switch',
    method: 'get'
  })
}

// 获取第三方登录地址
export function getAuthRenderApi(type: string) {
  return request({
    url: '/auth/render/' + type,
    method: 'get'
  })
}

// 获取邮箱验证码
export function getEmailCodeApi(email: string) {
  return request({
    url: '/auth/getEmailCode',
    method: 'get',
    params: {
      email
    }
  })
}


interface RegisterParams {
  email: string
  nickname: string
  username: string
  password: string
  code: string
}

// 注册
export function registerApi(data: RegisterParams) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

interface ResetParams {
  email: string
  password: string
  code: string
}

// 忘记密码
export function forgotPasswordApi(data: ResetParams) {
  return request({
    url: '/auth/forgot',
    method: 'post',
    data
  })
}
