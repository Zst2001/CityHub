import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { getToken } from '../utils/storage'
import { useUserStore } from '../stores/user'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.authorization = token
  return config
})

request.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result?.success === true) return result.data
    const message = result?.errorMsg || '请求未完成，请稍后重试'
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.clearAuth()
      const current = router.currentRoute.value
      if (current.path !== '/login') {
        router.push({ name: 'login', query: { redirect: current.fullPath } })
      }
    } else {
      ElMessage.error(error.response?.data?.errorMsg || '网络连接失败，请稍后重试')
    }
    return Promise.reject(error)
  },
)

export default request
