import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const api = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080',
  withCredentials: true,
})

let isRefreshing = false
let refreshSubscribers = []

function subscribeTokenRefresh (cb) {
  refreshSubscribers.push(cb)
}

function onRefreshed () {
  refreshSubscribers.forEach(cb => cb())
  refreshSubscribers = []
}

api.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config

    // 🔒 재발급 요청 자체는 무시
    if (originalRequest.url === '/api/refresh') {
      return Promise.reject(error)
    }

    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      originalRequest.url.startsWith('/api')
    ) {
      originalRequest._retry = true

      const authStore = useAuthStore()

      if (!isRefreshing) {
        isRefreshing = true
        try {
          await axios.post(
            `${api.defaults.baseURL}/api/refresh`,
            null,
            { withCredentials: true }
          )
          onRefreshed()
        } catch (refreshErr) {
          authStore.logout()
          return Promise.reject(refreshErr)
        } finally {
          isRefreshing = false
        }
      }

      return new Promise(resolve => {
        subscribeTokenRefresh(() => {
          resolve(api(originalRequest))
        })
      })
    }

    return Promise.reject(error)
  }
)

export default api
