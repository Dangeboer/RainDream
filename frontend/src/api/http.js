import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('rd_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error?.response?.data?.message || error?.message || '请求失败'
    ElMessage.error(message)
    if (error?.response?.status === 401) {
      localStorage.removeItem('rd_token')
      localStorage.removeItem('rd_user')
      if (!location.pathname.includes('/login')) {
        location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default http
