import Cookies from 'js-cookie'

const TokenKey = 'Neat-Admin-Token'

// 设置token到cookie，过期时间1小时
export function setToken(token: string) {
  return Cookies.set(TokenKey, token, { expires: 1/24 }) // 1/24 天 = 1小时
}

export function getToken() {
  return Cookies.get(TokenKey)
}

export function removeToken() {
  return Cookies.remove(TokenKey)
} 