import { defineStore } from 'pinia'
import api from '@/utils/axios-interceptor'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    isAuthenticated: false,
  }),
  getters: {
    isCompleted: state => !!state.user?.completed,
  },
  actions: {
    async fetchUser () {
      try {
        const res = await api.get('/api/users/me');
        this.user = res.data;
        this.isAuthenticated = true;
        console.log(res.data);
      } catch (err) {
        this.user = null;
        this.isAuthenticated = false;
        throw err;
      }
    },
    async logout () {
      try {
        await api.post('/api/users/logout');

        this.user = null
        this.isAuthenticated = false
      } catch (err) {
        console.error('로그아웃 실패:', err)
        // 실패해도 사용자 정보 제거는 수행 (선택사항)
        this.user = null
        this.isAuthenticated = false
      }
    },
  },
})
