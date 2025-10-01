// stores/auth.js
import { defineStore } from 'pinia'
import api from '@/utils/axios-interceptor' // 너의 axios-interceptor 경로 그대로


export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    isAuthenticated: false,
    refreshFailed: false, // 리프레시 토큰 갱신 실패 여부
    isInitialized: false,
  }),
  getters: {
    isCompleted: state => !!state.user?.completed,
    blogUrlName: state => state.user?.blogUrl || null,
    blogUsername: state => state.user?.blogUsername || null,
    blogTitle: state => state.user?.blogTitle || null,
  },
  actions: {
    async fetchUser () {
      try {
        const res = await api.get('/api/secure/users/me');
        this.user = res.data;
        this.isAuthenticated = true;
        console.log('🟢 fetchUser: 사용자 정보 성공적으로 불러옴:', res.data);
      } catch (err) {
        console.error('🔴 fetchUser: 사용자 정보 불러오기 실패 또는 토큰 무효화:', err);
        this.user = null; // 실패 시 사용자 정보 초기화
        this.isAuthenticated = false; // 인증 상태 false로
        throw err; // 에러를 호출자에게 다시 던져서 처리하게 함 (여기서 401 처리 등)
      }
    },
    async logout () {
      try {
        console.log('로그아웃 시도')
        await api.post('/api/secure/users/logout')
        console.log('로그아웃 성공')
      } catch (err) {
        console.error('로그아웃 실패:', err)
      } finally {
        this.user = null
        this.isAuthenticated = false
        this.refreshFailed = true
      }
    },

    async initializeAuth () {
      if (this.isInitialized) {
        console.log('🟠 initializeAuth: 이미 초기화됨. 건너뛰기.');
        return;
      }

      console.log('🟡 initializeAuth: 인증 스토어 초기화 시작...');

      // ✅ 핵심 수정: hasAuthCookies() 체크를 제거하고 무조건 fetchUser()를 시도합니다.
      // HttpOnly 쿠키는 JS에서 직접 접근 불가하며, 서버 요청을 통해서만 유효성 확인 가능.
      try {
        await this.fetchUser(); // 서버에 쿠키와 함께 사용자 정보 요청
        console.log('🟢 initializeAuth: 사용자 정보 패치 성공');
      } catch (e) {
        // fetchUser가 실패했다면 (예: 401 Unauthorized - 토큰 만료 또는 없음)
        console.error('🔴 initializeAuth: 사용자 정보 패치 실패 (서버에서 토큰 거부)', e);
        this.user = null;
        this.isAuthenticated = false;
        this.refreshFailed = true; // 리프레시 실패 플래그 (옵션)
      } finally {
        this.isInitialized = true; // 초기화 완료 설정 (성공/실패 여부와 무관)
        console.log('🟢 initializeAuth: 인증 스토어 초기화 완료. 최종 인증 상태:', this.isAuthenticated);
      }
    },
  },
})