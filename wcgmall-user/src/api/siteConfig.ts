/**
 * 网站配置接口
 */
import request from '@/utils/request'

/**
 * 获取公开的网站配置（无需登录）
 */
export function getSiteConfigApi() {
  return request.get<any, any>('/sys/siteConfig/public')
}

/**
 * 根据键获取配置值
 */
export function getSiteConfigByKeyApi(key: string) {
  return request.get<any, any>(`/sys/siteConfig/key/${key}`)
}
