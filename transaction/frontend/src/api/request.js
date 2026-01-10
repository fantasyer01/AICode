import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// Request interceptor
service.interceptors.request.use(
  config => {
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('Response error:', error)
    ElMessage.error(error.message || 'Request failed')
    return Promise.reject(error)
  }
)

export default service
