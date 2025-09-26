// stores/auth.js
import { defineStore } from 'pinia'
import api from '@/utils/axios-interceptor' // 너의 axios-interceptor 경로 그대로
import { hasAuthCookies } from '@/utils/cookie'

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
      }
    },

    // ✅ 새로 추가: 앱 시작/새로고침 시 호출될 초기화 액션
    // async initializeAuth () {
    //   if (this.isInitialized) {
    //     console.log('🟠 initializeAuth: 이미 초기화됨. 건너뛰기.');
    //     return;
    //   }

    //   console.log('🟡 initializeAuth: 인증 스토어 초기화 시작...');

    //   // 쿠키 이름 검사 없이 무조건 fetchUser 시도
    //   try {
    //     await this.fetchUser(); // 토큰이 있든 없든 일단 시도해봄
    //     console.log('🟢 initializeAuth: 사용자 정보 패치 성공');
    //   } catch (e) {
    //     console.error('🔴 initializeAuth: 사용자 정보 패치 실패', e);
    //     this.user = null;
    //     this.isAuthenticated = false;
    //     this.refreshFailed = true; // 리프레시 실패 플래그 설정
    //   } finally {
    //     this.isInitialized = true; // 초기화 완료 설정
    //     console.log('🟢 initializeAuth: 인증 스토어 초기화 완료. 최종 인증 상태:', this.isAuthenticated);
    //   }
    // },
    async initializeAuth () {
      if (this.isInitialized) {
        console.log('🟠 initializeAuth: 이미 초기화됨. 건너뛰기.');
        return;
      }

      console.log('🟡 initializeAuth: 인증 스토어 초기화 시작...');

      // ✅ 초장부터 쳐내는 로직 추가: 쿠키에 인증 관련 토큰이 아예 없는지 확인
      // 이 부분은 실제 프로젝트의 쿠키/로컬 스토리지 구현에 따라 달라질 수 있음.
      // 예를 들어, refresh_token 쿠키가 있는지 확인하는 로직.
      const hasTokensInBrowser = hasAuthCookies(); // 유틸리티 함수 호출

      if (!hasTokensInBrowser) {
        console.log('🟡 initializeAuth: 브라우저에 인증 토큰이 없음. 백엔드 요청 없이 비인증 상태로 설정.');
        this.user = null;
        this.isAuthenticated = false;
        this.refreshFailed = true; // 토큰이 없으므로 리프레시 시도조차 할 수 없어 실패로 간주
        this.isInitialized = true; // 초기화 완료 설정
        console.log('🟢 initializeAuth: 인증 스토어 초기화 완료 (토큰 없음). 최종 인증 상태:', this.isAuthenticated);
        return; // 백엔드 API 호출 없이 즉시 종료
      }
      // 토큰이 존재하면 다음 로직으로 진행 (백엔드에 물어보러 감)

      try {
        await this.fetchUser(); // 토큰이 있는 것으로 추정되므로, 백엔드에 사용자 정보 요청
        console.log('🟢 initializeAuth: 사용자 정보 패치 성공');
      } catch (e) {
        // fetchUser가 실패했다면 (토큰 만료, 유효하지 않은 토큰 등)
        console.error('🔴 initializeAuth: 사용자 정보 패치 실패 (서버에서 토큰 거부)', e);
        this.user = null;
        this.isAuthenticated = false;
        this.refreshFailed = true; // 리프레시 실패 플래그 설정
      } finally {
        this.isInitialized = true; // 초기화 완료 설정 (성공/실패 여부와 무관)
        console.log('🟢 initializeAuth: 인증 스토어 초기화 완료. 최종 인증 상태:', this.isAuthenticated);
      }
    },
  },
})
