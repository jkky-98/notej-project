<template>
  <v-app-bar
    app
    flat
    :color="theme.global.current.value.dark ? 'surface' : 'white'"
    elevation="1"
    class="px-4 transition-colors"
  >
    <!-- 왼쪽: 로고 -->
    <v-app-bar-title class="text-h5 font-weight-bold">
      <RouterLink to="/" class="text-decoration-none text-primary hover:underline">
        NoteJ
      </RouterLink>
    </v-app-bar-title>

    <v-spacer />

    <!-- 오른쪽 메뉴 -->
    <v-btn
      variant="tonal"
      size="large"
      class="mr-2 text-body-1"
      @click="onCreatePost"
      rounded
    >
      새 포스트
    </v-btn>

    <v-btn icon class="mr-1" :title="'알림'">
      <v-icon>mdi-bell-outline</v-icon>
    </v-btn>

    <v-btn icon class="mr-1" :title="'검색'">
      <v-icon>mdi-magnify</v-icon>
    </v-btn>

    <!-- 다크모드 토글 -->
    <v-btn icon @click="toggleTheme" :title="isDark ? '라이트 모드로 전환' : '다크 모드로 전환'" class="mr-2">
      <v-icon>{{ isDark ? 'mdi-white-balance-sunny' : 'mdi-weather-night' }}</v-icon>
    </v-btn>

    <!-- 프로필 메뉴 -->
    <v-menu offset-y transition="slide-y-transition">
      <template #activator="{ props }">
        <v-btn v-bind="props" icon class="rounded-xl elevation-1 transition-all">
          <v-avatar
            image="https://via.placeholder.com/40"
            size="36"
            class="hover:scale-105 transition-transform"
          />
        </v-btn>
      </template>

      <v-list>
        <v-list-item @click="goToProfile">
          <v-list-item-title>내 프로필</v-list-item-title>
        </v-list-item>
        <v-list-item @click="logout">
          <v-list-item-title>로그아웃</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>
  </v-app-bar>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useTheme } from 'vuetify'

const router = useRouter()
const theme = useTheme()

function onCreatePost() {
  router.push('/new-post')
}

function goToProfile() {
  router.push('/profile')
}

function logout() {
  console.log('로그아웃')
}

function toggleTheme() {
  theme.global.name.value = theme.global.name.value === 'light' ? 'dark' : 'light'
}

const isDark = computed(() => theme.global.current.value.dark)
</script>

<style scoped>
.transition-colors {
  transition: background-color 0.3s ease;
}
.hover\:underline:hover {
  text-decoration: underline;
}
</style>
