import axios from 'axios'
import { ElMessage,ElMessageBox } from 'element-plus'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store/modules/user'

let isRelogin = { show: false }; // 是否显示弹框

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 300000,
  headers: { "Content-Type": "application/json;charset=utf-8" },
})

service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    // 二进制数据则直接返回
    if (response.request.responseType === 'blob' || response.request.responseType === 'arraybuffer') {
      return response
    }
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求错误')
      if (res.code === 401) {
        ElMessageBox.confirm("当前页面已失效，请重新登录", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        })
        .then(() => {
          const userStore = useUserStore()
          userStore.logout()
        })
      }
      return Promise.reject(new Error(res.message || '请求错误'))
    }
    
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
    }else if (error.response?.status === 500) {
      ElMessage.error('后端接口连接异常')
    }else{
      ElMessage.error('请求错误')
    }
    return Promise.reject(error)
  }
)

export default service