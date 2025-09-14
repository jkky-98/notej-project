// stores/auth.js
import { defineStore } from 'pinia'
import api from '@/utils/axios-interceptor' // 너의 axios-interceptor 경로 그대로
import router from '@/router' // logout 시 리다이렉션을 위해 라우터 import (필요 없으면 제거)

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    isAuthenticated: false,
    refreshFailed: false, // 리프레시 토큰 갱신 실패 여부
    isInitialized: false, // ✅ 새로 추가: 인증 스토어 초기화 완료 여부
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
        throw err; // 에러를 호출자에게 다시 던져서 처리하게 함
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
        this.refreshFailed = true // 리프레시 실패 플래그도 켬
        // router.push('/login') // ✅ 로그아웃 시 로그인 페이지로 이동
      }
    },

    // ✅ 새로 추가: 앱 시작/새로고침 시 호출될 초기화 액션
    async initializeAuth () {
      if (this.isInitialized) {
        console.log('🟠 initializeAuth: 이미 초기화됨. 건너뛰기.');
        return;
      }

      console.log('🟡 initializeAuth: 인증 스토어 초기화 시작...');

      // 쿠키 이름 검사 없이 무조건 fetchUser 시도
      try {
        await this.fetchUser(); // 토큰이 있든 없든 일단 시도해봄
        console.log('🟢 initializeAuth: 사용자 정보 패치 성공');
      } catch (e) {
        console.error('🔴 initializeAuth: 사용자 정보 패치 실패', e);
        this.user = null;
        this.isAuthenticated = false;
        this.refreshFailed = true; // 리프레시 실패 플래그 설정
      } finally {
        this.isInitialized = true; // 초기화 완료 설정
        console.log('🟢 initializeAuth: 인증 스토어 초기화 완료. 최종 인증 상태:', this.isAuthenticated);
      }
    },
  },
})
