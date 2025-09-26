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

function subscribeTokenRefresh (resolve, reject) {
  console.log('[subscribeTokenRefresh] 새로운 재시도 요청 대기 등록')
  refreshSubscribers.push({ resolve, reject })
}

function onRefreshed () {
  console.log('[onRefreshed] 모든 대기 요청 재시도 실행')
  refreshSubscribers.forEach(({ resolve }) => resolve())
  refreshSubscribers = []
}

function onRefreshFailed (error) {
  console.log('[onRefreshFailed] 모든 대기 요청 에러 처리')
  refreshSubscribers.forEach(({ reject }) => reject(error))
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
    const authStore = useAuthStore()

    const isAuthKnown = authStore.user !== null || authStore.refreshFailed

    if (!isAuthKnown) {
      console.warn('[interceptor] auth 상태 미확정 → refresh 시도 강행')
      // 그냥 진행해서 refresh 로직으로 진입시킴
    } else if (!authStore.isAuthenticated) {
      // 로그인 안 된 상태가 확정되었으면 요청 거절
      return Promise.reject(error)
    }

    if (originalRequest.url === REFRESH_ENDPOINT) {
      console.warn('[interceptor] /api/refresh 요청 실패 - 더 이상 재시도하지 않음')
      authStore.refreshFailed = true
      return Promise.reject(error)
    }

    if (
      error.response?.status === 401 &&
      !authStore.refreshFailed &&
      (!originalRequest._retry || originalRequest._retryCount < 1) &&
      originalRequest.url.startsWith('/api') &&
      originalRequest.url !== LOGOUT_ENDPOINT
    ) {
      originalRequest._retry = true
      originalRequest._retryCount = (originalRequest._retryCount || 0) + 1

      return new Promise((resolve, reject) => {
        subscribeTokenRefresh(
          () => {
            console.log('[interceptor] 재시도 요청 실행 →', originalRequest.url)
            api({ ...originalRequest, _retry: true }).then(resolve).catch(reject)
          },
          reject
        )

        if (!isRefreshing) {
          console.log('[interceptor] 토큰 갱신 시작')
          isRefreshing = true
          axios.post(`${api.defaults.baseURL}${REFRESH_ENDPOINT}`, null, { withCredentials: true })
            .then(res => {
              // 필요 시 토큰 저장 예시
              // authStore.accessToken = res.data.accessToken
              // api.defaults.headers.common['Authorization'] = `Bearer ${res.data.accessToken}`
              authStore.refreshFailed = false
              console.log('[interceptor] 토큰 갱신 성공')
              onRefreshed()
            })
            .catch(refreshErr => {
              authStore.refreshFailed = true
              console.error('[interceptor] 토큰 갱신 실패 → 로그아웃')
              authStore.logout()
              onRefreshFailed(refreshErr)
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
