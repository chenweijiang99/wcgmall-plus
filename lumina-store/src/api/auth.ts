/**
 * 认证接口
 */
import request from '@/utils/request'
import { ApiResponse, User, Captcha, SysConfig } from '@/types';
interface LoginParams {
  username: string
  password: string
  captchaCode: string
  captchaKey: string
  rememberMe: boolean
}

// 登录接口
export function loginApi(data: LoginParams) {
  return request.post<any, ApiResponse<User>>(`/auth/login`, data);
}

export function logoutApi() {
  return request.post<any, ApiResponse<any>>(`/auth/logout`);
}

// 获取用户信息
export function getUserInfoApi() {
  return request.get<any, ApiResponse<User>>('/auth/info', { params: { source: "user" } });
}

// 更新用户信息
export function updateUserApi(data: any) {
  return request.put<any, ApiResponse<boolean>>(`/user/user/update`, data);
}


// 获取验证码
export function getCaptchaApi() {
  return request.get<any, ApiResponse<Captcha>>('/auth/getCaptcha');
}

// 获取验证码开关
export function getCaptchaSwitchApi() {
  return request.get<any, ApiResponse<SysConfig>>('/sys/config/getConfigByKey/slider_verify_switch');
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
  return request.get<any, ApiResponse<boolean>>('/auth/getEmailCode', { params: { email: email } });
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
