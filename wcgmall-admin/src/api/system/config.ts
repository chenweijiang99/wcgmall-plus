/**
 * 参数配置表接口
 */
import request from '@/utils/request'

/**
 * 获取参数配置表列表
 */
export function listSysConfigApi(params?: any) {
    return request({
        url: '/sys/config/list',
        method: 'get',
        params
    })
}

/**
 * 获取参数配置表详情
 */
export function detailSysConfigApi(id: any) {
    return request({
        url: '/sys/config/' + id,
        method: 'get'
    })
}

/**
 * 添加参数配置表
 */
export function addSysConfigApi(data: any) {
    return request({
        url: '/sys/config/add',
        method: 'post',
        data
    })
}

/**
 * 修改参数配置表
 */
export function updateSysConfigApi(data: any) {
    return request({
        url: `/sys/config/update`,
        method: 'put',
        data
    })
}


/**
 * 删除参数配置表
 */
export function deleteSysConfigApi(ids: number[] | number) {
    return request({
        url: `/sys/config/delete/` + ids,
        method: 'delete'
    })
}

// ==================== 网站配置接口 ====================

/**
 * 获取公开的网站配置（无需登录）
 */
export function getPublicSiteConfigApi() {
    return request({
        url: '/sys/siteConfig/public',
        method: 'get'
    })
}

/**
 * 获取所有网站配置（Map形式）
 */
export function getSiteConfigMapApi() {
    return request({
        url: '/sys/siteConfig/map',
        method: 'get'
    })
}

/**
 * 获取网站配置列表
 */
export function getSiteConfigListApi() {
    return request({
        url: '/sys/siteConfig/list',
        method: 'get'
    })
}

/**
 * 批量更新网站配置
 */
export function updateSiteConfigApi(data: Record<string, string>) {
    return request({
        url: '/sys/siteConfig/update',
        method: 'put',
        data
    })
}


