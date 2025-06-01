<template>
  <v-app-bar
    app
    class="px-4 transition-colors"
    color="surface"
    elevation="1"
    flat
  >
    <v-app-bar-title class="text-h5 font-weight-bold">
      <RouterLink class="text-decoration-none text-text hover:underline" to="/">
        NoteJ
      </RouterLink>
    </v-app-bar-title>

    <v-spacer />

    <v-btn
      class="mr-2 text-body-1"
      rounded
      size="large"
      variant="tonal"
      @click="onCreatePost"
    >
      새 포스트
    </v-btn>

    <v-btn class="mr-1" icon :title="'알림'">
      <v-icon>mdi-bell-outline</v-icon>
    </v-btn>

    <v-btn class="mr-1" icon :title="'검색'">
      <v-icon>mdi-magnify</v-icon>
    </v-btn>

    <v-btn
      class="mr-2"
      icon
      :title="isDark ? '라이트 모드로 전환' : '다크 모드로 전환'"
      @click="toggleTheme"
    >
      <v-icon>{{ isDark ? 'mdi-white-balance-sunny' : 'mdi-weather-night' }}</v-icon>
    </v-btn>

    <v-menu offset-y transition="slide-y-transition">
      <template #activator="{ props }">
        <v-btn v-bind="props" class="rounded-xl elevation-1 transition-all" icon>
          <v-avatar
            class="hover:scale-105 transition-transform"
            image="https://via.placeholder.com/40"
            size="36"
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
  import { useThemeStore } from '@/stores/theme' // 추가

  const router = useRouter()
  const theme = useTheme()
  const themeStore = useThemeStore()

  function onCreatePost () {
    router.push('/new-post')
  }

  function goToProfile () {
    router.push('/profile')
  }

  function logout () {
    console.log('로그아웃')
  }

  function toggleTheme () {
    themeStore.toggleTheme()
    theme.global.name.value = themeStore.isDark ? 'dark' : 'light'
  }

  const isDark = computed(() => themeStore.isDark)
</script>


<style scoped>
.transition-colors {
  transition: background-color 0.3s ease;
}
.hover\:underline:hover {
  text-decoration: underline;
}
</style>
