/**
 * 文件接口
 */
import request from '@/utils/request'

// 上传文件
export function uploadApi(data, source) {
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
export function deleteFileApi(url) {
  return request({
    url: `/file/delete`,
    method: 'get',
    params: { url:url }
  })
}

// 添加批量上传接口
export const uploadBatchApi = (data) => {
  return request({
    url: '/file/uploadBatch',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}



