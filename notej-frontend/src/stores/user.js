import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    username: 'nologin', // 로그인되면 실제 username으로 갱신
  }),
})
