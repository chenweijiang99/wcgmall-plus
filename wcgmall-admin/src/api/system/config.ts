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


