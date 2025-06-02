import { defineStore } from 'pinia'
import Cookies from 'js-cookie'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null, // 로그인된 사용자 정보
  }),
  getters: {
    isLoggedIn: () => {
      return !!Cookies.get('token') // 토큰이 있으면 로그인 상태
    },
  },
  actions: {
    logout () {
      Cookies.remove('token') // 토큰 제거
      this.user = null
    },
    setUser (userData) {
      this.user = userData
    },
  },
})
