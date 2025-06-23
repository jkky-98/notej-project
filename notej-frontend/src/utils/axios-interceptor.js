import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const api = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080',
  withCredentials: true,
})

const REFRESH_ENDPOINT = '/api/refresh'
const LOGOUT_ENDPOINT = '/api/users/logout'

let isRefreshing = false
let refreshSubscribers = []

function subscribeTokenRefresh (cb) {
  console.log('[subscribeTokenRefresh] 새로운 재시도 요청 대기 등록')
  refreshSubscribers.push(cb)
}

function onRefreshed () {
  console.log('[onRefreshed] 모든 대기 요청 재시도 실행')
  refreshSubscribers.forEach(cb => cb())
  refreshSubscribers = []
}

api.interceptors.request.use(
  config => {
    console.log(`[request] 요청 시작 → ${config.method?.toUpperCase()} ${config.url}`)
    return config
  },
  error => {
    console.error('[request] 요청 중 에러 발생', error)
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config

    // 🔒 재발급 요청 자체는 무시
    if (originalRequest.url === REFRESH_ENDPOINT) {
      console.warn('[interceptor] /api/refresh 요청 실패 - 더 이상 재시도하지 않음')
      return Promise.reject(error)
    }

    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      originalRequest.url.startsWith('/api') &&
      originalRequest.url !== LOGOUT_ENDPOINT
    ) {
      console.warn(`[interceptor] 401 에러 감지 → ${originalRequest.url}`)
      originalRequest._retry = true
      const authStore = useAuthStore()

      return new Promise((resolve, reject) => {
        // ✅ 재시도 등록 (originalRequest 클로저로 안전하게 보존)
        subscribeTokenRefresh(() => {
          console.log('[interceptor] 재시도 요청 실행 →', originalRequest.url)
          api(originalRequest).then(resolve).catch(reject)
        })

        if (!isRefreshing) {
          console.log('[interceptor] 토큰 갱신 시작')
          isRefreshing = true
          axios.post(`${api.defaults.baseURL}/api/refresh`, null, {
            withCredentials: true,
          })
            .then(() => {
              console.log('[interceptor] 토큰 갱신 성공')
              onRefreshed() // 🔁 이때 모든 큐된 요청들이 실행됨
            })
            .catch(refreshErr => {
              console.error('[interceptor] 토큰 갱신 실패 → 로그아웃')
              authStore.logout()

              // ❗ 여기선 reject 호출 X
              // 대신 등록된 요청들이 자체적으로 reject 처리됨
              refreshSubscribers = []
            })
            .finally(() => {
              isRefreshing = false
            })
        }
      })

    }

    console.warn(`[interceptor] 기타 에러 발생 → ${error.message}`)
    return Promise.reject(error)
  }
)

export default api
