import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', {
  state: () => ({
    isDark: localStorage.getItem('theme') === 'dark',
  }),
  actions: {
    toggleTheme () {
      this.isDark = !this.isDark
      localStorage.setItem('theme', this.isDark ? 'dark' : 'light')
    },
    setThemeFromStorage () {
      const saved = localStorage.getItem('theme')
      this.isDark = saved === 'dark'
    },
  },
})
