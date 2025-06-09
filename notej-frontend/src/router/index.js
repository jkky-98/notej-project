import { createRouter, createWebHistory } from 'vue-router/auto'
import { routes } from 'vue-router/auto-routes'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// ✅ 로그인 가드 설정
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 자동 로그인 시도
  if (!authStore.isAuthenticated && document.cookie.includes('refresh_token')) {
    await authStore.fetchUser()
  }

  const isAuth = authStore.isAuthenticated
  const isCompleted = authStore.user?.completed === true

  console.log(isAuth)
  console.log(isCompleted)
  // 🔐 보호된 페이지
  if (to.meta.requiresAuth && !isAuth) {
    return next('/login')
  }

  // 🚫 온보딩 페이지 직접 접근 차단
  if (to.path === '/onboarding') {
    if (!isAuth) return next('/login')
    if (isCompleted) return next('/')
  }

  next()
})

// 🔁 Vite 동적 모듈 오류 대응 (기존 유지)
router.onError((err, to) => {
  if (err?.message?.includes?.('Failed to fetch dynamically imported module')) {
    if (!localStorage.getItem('vuetify:dynamic-reload')) {
      console.log('Reloading page to fix dynamic import error')
      localStorage.setItem('vuetify:dynamic-reload', 'true')
      location.assign(to.fullPath)
    } else {
      console.error('Dynamic import error, reloading page did not fix it', err)
    }
  } else {
    console.error(err)
  }
})

router.isReady().then(() => {
  localStorage.removeItem('vuetify:dynamic-reload')
})

export default router
