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
  import { useAuthStore } from '@/stores/auth'
  import { onMounted } from 'vue'

  import NavBar from '@/components/layouts/NavBar.vue'
  import FloatingContactButton from '@/components/layouts/FloatingContactButton.vue'

  const theme = useTheme()
  const themeStore = useThemeStore()
  const authStore = useAuthStore()

  // 테마 적용
  themeStore.setThemeFromStorage()
  theme.global.name.value = themeStore.isDark ? 'dark' : 'light'

  // ✅ 앱 시작 시 유저 정보 불러오기
  onMounted(async () => {
    try {
      await authStore.fetchUser();
    } catch (e) {
      // 로그인이 안된 경우라면 무시해도 됨
      console.warn('로그인 정보 없음')
    }
  })
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
