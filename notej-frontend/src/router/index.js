// router/index.js
import { createRouter, createWebHistory } from 'vue-router/auto'
import { routes as autoRoutes } from 'vue-router/auto-routes'
import { useAuthStore } from '@/stores/auth'
import BlogMain from '@/pages/blog/BlogMain.vue' // 이 라우트는 그대로 유지


const routes = [
  ...autoRoutes,
  {
    path: '/@:blogUrlName',
    name: 'UserBlog',
    component: BlogMain,
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/pages/blog/BlogSetting.vue'),
    meta: { requiresAuth: true }, // 인증 필요
  },
  {
    path: '/login', // 로그인 페이지 라우트도 명시적으로 정의 (meta.requiresAuth 없음)
    name: 'login',
    component: () => import('@/pages/Login.vue'), // 너의 실제 로그인 페이지 경로로 변경
  },
  {
    path: '/write',
    name: 'write',
    component: () => import('@/pages/blog/Write.vue'),
    meta: { requiresAuth: true }, // 인증 필요
  },
  {
    path: '/@:blogUrlName/post/:postId',
    name: 'postView',
    component: () => import('@/pages/blog/PostView.vue'),
  },
  {
    path: '/test/tagify',
    name: 'test-tagify',
    component: () => import('@/pages/TagifyTest.vue'),
    meta: { requiresAuth: true }, // 인증 필요
  },
  {
    path: '/test/tagify-error',
    name: 'test-tagify-error',
    component: () => import('@/pages/TagifyTestError.vue'),
    meta: { requiresAuth: true }, // 인증 필요
  },
  // ... 기타 라우트들
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// ✅ 로그인 가드 설정
router.beforeEach(async (to, from, next) => {
  console.log('🔁 router.beforeEach 실행됨:', `(${from.path} -> ${to.path})`);
  const authStore = useAuthStore();

  // ⭐️⭐️⭐️ 가장 중요한 부분: 인증 스토어 초기화가 완료될 때까지 기다림 ⭐️⭐️⭐️
  if (!authStore.isInitialized) {
    console.log('🟡 라우터 가드: 인증 스토어 초기화 시작...');
    await authStore.initializeAuth(); // initializeAuth 액션이 끝날 때까지 대기
    console.log('🟢 라우터 가드: 인증 스토어 초기화 완료.');
  } else {
    console.log('🟠 라우터 가드: 인증 스토어 이미 초기화됨. 현재 인증 상태:', authStore.isAuthenticated);
  }

  // 이제 authStore.isAuthenticated는 초기화 작업이 완료된 정확한 상태를 반영함
  const isAuth = authStore.isAuthenticated;
  const isCompleted = authStore.user?.completed === true;

  console.log('✅ 최종 인증 상태: isAuth:', isAuth, 'isCompleted:', isCompleted);

  // 🔐 보호된 페이지 접근 처리
  if (to.meta.requiresAuth && !isAuth) {
    console.log('🚫 보호된 페이지 접근 시도 (인증 필요):', to.path);
    // 현재 라우트가 이미 로그인 페이지가 아니라면 로그인 페이지로 리다이렉트
    if (to.name !== 'login') {
      console.log('➡️ 로그인 페이지로 리디렉션.');
      return next('/login');
    }
  }

  // 🚫 온보딩 페이지 직접 접근 차단
  if (to.path === '/onboarding') {
    if (!isAuth) {
      console.log('🚫 온보딩: 인증되지 않은 사용자. 로그인으로 리디렉션.');
      return next('/login');
    }
    if (isCompleted) {
      console.log('🚫 온보딩: 이미 완료된 사용자. 메인으로 리디렉션.');
      return next('/');
    }
  }

  // ✅ (선택 사항) 이미 로그인된 사용자가 로그인 페이지에 직접 접근 시 다른 곳으로 리다이렉트
  if (to.name === 'login' && isAuth) {
    console.log('🚫 로그인 페이지: 이미 로그인됨. 메인 페이지로 리디렉션.');
    return next('/');
  }

  console.log('➡️ 다음 라우트로 진행:', to.path);
  next(); // 모든 조건 통과, 다음 라우트로 이동
});

// 🔁 Vite 동적 모듈 오류 대응 (이 부분은 기존과 동일)
router.onError((err, to) => {
  if (err?.message?.includes?.('Failed to fetch dynamically imported module')) {
    if (!localStorage.getItem('vuetify:dynamic-reload')) {
      console.log('Reloading page to fix dynamic import error');
      localStorage.setItem('vuetify:dynamic-reload', 'true');
      location.assign(to.fullPath);
    } else {
      console.error('Dynamic import error, reloading page did not fix it', err);
    }
  } else {
    console.error(err);
  }
});

router.isReady().then(() => {
  localStorage.removeItem('vuetify:dynamic-reload');
});

export default router;
