<template>
  <v-app>
    <!-- 공통 네비게이션 바 -->
    <NavBar />

    <v-main>
      <router-view />
    </v-main>

    <!-- 오른쪽 하단 문의 버튼 -->
    <FloatingContactButton />
  </v-app>
</template>

<script setup>
  import { useTheme } from 'vuetify'
  import { useThemeStore } from '@/stores/theme'

  import NavBar from '@/components/layouts/NavBar.vue'
  import FloatingContactButton from '@/components/layouts/FloatingContactButton.vue'

  const theme = useTheme()
  const themeStore = useThemeStore()

  // 로컬스토리지에서 불러온 후 Vuetify에 테마 적용
  themeStore.setThemeFromStorage()
  theme.global.name.value = themeStore.isDark ? 'dark' : 'light'
</script>

<style>
/* 전역 스타일 폰트 적용 */
body {
  font-family: 'Pretendard Variable', -apple-system, BlinkMacSystemFont, 'Segoe UI',
               'Noto Sans KR', 'Malgun Gothic', 'Apple SD Gothic Neo', sans-serif;
  font-weight: 400;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
