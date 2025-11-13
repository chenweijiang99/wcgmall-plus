import request from '@/utils/request.js'
// 获取验证码接口
export const getCaptchaApi = ()=> {
    return request.get('/auth/getCaptcha');
}

// 获取验证码开关
export function getCaptchaSwitchApi() {
  return request.get('/sys/config/getConfigByKey/slider_verify_switch');
}

// 登录接口
export function loginApi(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
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