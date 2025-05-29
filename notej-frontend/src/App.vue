<template>
  <v-app>
    <!-- 공통 네비게이션 바 -->
    <NavBar />

    <v-main>
      <router-view />
    </v-main>
  </v-app>
</template>

<script setup>
import NavBar from '@/components/layouts/NavBar.vue'
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