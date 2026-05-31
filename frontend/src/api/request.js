import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data.code && data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        const isAdminRoute = router.currentRoute.value.path.startsWith('/admin')
        localStorage.removeItem('token')
        localStorage.removeItem('role')
        ElMessage.error('登录已过期，请重新登录')
        router.push(isAdminRoute ? '/admin/login' : '/login')
      } else if (status === 403) {
        ElMessage.error('没有权限')
      } else if (status >= 500) {
        ElMessage.error('服务器错误')
      } else {
        const msg = error.response.data?.message || '请求失败'
        ElMessage.error(msg)
      }
    } else {
      ElMessage.error('网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
