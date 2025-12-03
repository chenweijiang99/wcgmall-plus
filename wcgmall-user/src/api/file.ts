/**
 * 文件接口
 */
import request from '@/utils/request'

// 获取文件列表
export function getFileListApi(params: any) {
  return request({
    url: '/file/list',
    method: 'get',
    params
  })
}

// 上传文件
export function uploadApi(data: any, source: string) {
  return request({
    url: '/file/upload',
    method: 'post',
    headers: { "Content-Type": "multipart/articles-data" },
    data,
    params: { source: source },
    timeout: 0
  })
}

// 删除文件
export function deleteFileApi(url: string) {
  return request({
    url: `/file/delete`,
    method: 'get',
    params: { url:url }
  })
}

// 获取云存储配置
export function getOssConfigApi() {
  return request({
    url: '/file/getOssConfig',
    method: 'get'
  })
}

// 添加云存储配置
export function addOssApi(data: any) {
  return request({
    url: '/file/addOss',
    method: 'post', 
    data
  })
}
// 更新云存储配置
export function updateOssApi(data: any) {
  return request({
    url: '/file/updateOss',
    method: 'put', 
    data
  })
}
// 添加批量上传接口
export const uploadBatchApi = (data: FormData) => {
  return request({
    url: '/file/uploadBatch',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
export const deleteBatchApi = (urls: string[]) => {
  return request({
    url: '/file/deleteBatch',
    method: 'post',
    headers: {
      'Content-Type': 'application/json'
    },
    data: urls
  })
}


