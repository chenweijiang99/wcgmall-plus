/**
 * 第三方登录配置接口
 */
import request from '@/utils/request'

/**
 * 获取第三方登录配置列表(分页)
 */
export function listSocialConfigApi(params?: any) {
    return request({
        url: '/sys/socialConfig/list',
        method: 'get',
        params
    })
}

/**
 * 获取所有第三方登录配置
 */
export function getAllSocialConfigApi() {
    return request({
        url: '/sys/socialConfig/all',
        method: 'get'
    })
}

/**
 * 获取当前模式下启用的第三方登录配置
 */
export function getEnabledSocialConfigApi() {
    return request({
        url: '/sys/socialConfig/enabledList',
        method: 'get'
    })
}

/**
 * 获取第三方登录配置详情
 */
export function detailSocialConfigApi(id: any) {
    return request({
        url: '/sys/socialConfig/' + id,
        method: 'get'
    })
}

/**
 * 根据类型获取第三方登录配置
 */
export function getSocialConfigByTypeApi(socialType: string) {
    return request({
        url: '/sys/socialConfig/type/' + socialType,
        method: 'get'
    })
}

/**
 * 添加第三方登录配置
 */
export function addSocialConfigApi(data: any) {
    return request({
        url: '/sys/socialConfig/add',
        method: 'post',
        data
    })
}

/**
 * 修改第三方登录配置
 */
export function updateSocialConfigApi(data: any) {
    return request({
        url: '/sys/socialConfig/update',
        method: 'put',
        data
    })
}

/**
 * 修改第三方登录配置状态
 */
export function updateSocialConfigStatusApi(id: number, status: number) {
    return request({
        url: '/sys/socialConfig/updateStatus',
        method: 'put',
        data: { id, status }
    })
}

/**
 * 删除第三方登录配置
 */
export function deleteSocialConfigApi(ids: number[] | number) {
    return request({
        url: '/sys/socialConfig/delete/' + ids,
        method: 'delete'
    })
}

/**
 * 获取第三方登录全局设置
 */
export function getSocialSettingsApi() {
    return request({
        url: '/sys/socialConfig/settings',
        method: 'get'
    })
}

/**
 * 更新第三方登录全局设置
 */
export function updateSocialSettingsApi(data: any) {
    return request({
        url: '/sys/socialConfig/settings',
        method: 'put',
        data
    })
}

/**
 * 获取全局登录模式
 */
export function getLoginModeApi() {
    return request({
        url: '/sys/socialConfig/loginMode',
        method: 'get'
    })
}

/**
 * 设置全局登录模式
 */
export function setLoginModeApi(mode: string) {
    return request({
        url: '/sys/socialConfig/loginMode/' + mode,
        method: 'put'
    })
}
