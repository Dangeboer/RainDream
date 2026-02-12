import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('rd_token')
  if (token && token !== 'undefined' && token !== 'null') {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data

    // Backend wraps all controller responses as: { code, message, data }
    if (payload && typeof payload === 'object' && 'code' in payload && 'data' in payload) {
      if (payload.code !== 0 && payload.code !== 200) {
        ElMessage.error(payload.message || '请求失败')
        return Promise.reject(new Error(payload.message || '请求失败'))
      }
      return payload.data
    }

    return payload
  },
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
