import { defineStore } from 'pinia'
import api from '@/utils/axios-interceptor'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    isAuthenticated: false,
    refreshFailed: false,
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
        console.log(res.data);
      } catch (err) {
        this.user = null;
        this.isAuthenticated = false;
        throw err;
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
  },
})
