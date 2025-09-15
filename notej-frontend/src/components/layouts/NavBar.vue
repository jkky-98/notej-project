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

    <!-- 알림 -->
    <v-tooltip location="bottom">
      <template #activator="{ props }">
        <v-btn class="mr-1" icon v-bind="props">
          <v-icon>mdi-bell-outline</v-icon>
        </v-btn>
      </template>
      <span>알림</span>
    </v-tooltip>

    <!-- 검색 -->
    <v-tooltip location="bottom">
      <template #activator="{ props }">
        <v-btn class="mr-1" icon v-bind="props">
          <v-icon>mdi-magnify</v-icon>
        </v-btn>
      </template>
      <span>검색</span>
    </v-tooltip>

    <!-- 다크/라이트 모드 전환 -->
    <v-tooltip location="bottom">
      <template #activator="{ props }">
        <v-btn
          class="mr-2"
          icon
          v-bind="props"
          @click="toggleTheme"
        >
          <v-icon>{{ isDark ? 'mdi-white-balance-sunny' : 'mdi-weather-night' }}</v-icon>
        </v-btn>
      </template>
      <span>{{ isDark ? '라이트 모드로 전환' : '다크 모드로 전환' }}</span>
    </v-tooltip>

    <v-menu offset-y transition="slide-y-transition">
      <template #activator="{ props }">
        <v-btn
          v-bind="props"
          class="rounded-circle elevation-0"
          icon
          :title="'프로필'"
        >
          <v-icon size="36">mdi-account-circle</v-icon>
        </v-btn>
      </template>

      <v-list>
        <!-- 로그인 경우 -->
        <template v-if="isLoggedIn">
          <v-list-item @click="goToProfile">
            <v-list-item-title>내 프로필</v-list-item-title>
          </v-list-item>
          <v-list-item @click="goToBlog">
            <v-list-item-title>내 블로그</v-list-item-title>
          </v-list-item>
          <v-list-item @click="openLogoutConfirmDialog">
            <v-list-item-title>로그아웃</v-list-item-title>
          </v-list-item>
        </template>
        <!-- 로그아웃 상태 경우 -->
        <template v-else>
          <v-list-item @click="goToLogin">
            <v-list-item-title>로그인</v-list-item-title>
          </v-list-item>
        </template>
      </v-list>
    </v-menu>
  </v-app-bar>

  <v-dialog v-model="showLogoutConfirm" max-width="400">
    <v-card class="pa-3">
      <v-card-title class="text-h6">로그아웃 하시겠습니까?</v-card-title>
      <v-card-actions class="justify-end">
        <v-btn text @click="showLogoutConfirm = false">취소</v-btn>
        <v-btn color="red" variant="tonal" @click="confirmLogout">로그아웃</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

</template>

<script setup>
  import { computed } from 'vue'
  import { useRouter } from 'vue-router'
  import { useTheme } from 'vuetify'
  import { useThemeStore } from '@/stores/theme'
  import { useAuthStore } from '@/stores/auth'
  import { ref } from 'vue'

  const router = useRouter()
  const theme = useTheme()
  const themeStore = useThemeStore()
  const authStore = useAuthStore()
  const showLogoutConfirm = ref(false)

  const isLoggedIn = computed(() => authStore.isAuthenticated)

  function onCreatePost () {
    router.push('/write')
  }

  function goToProfile () {
    router.push('/profile')
  }

  function goToBlog () {
    const blogUrl = authStore.user?.blogUrl
    if (blogUrl) {
      router.push(`/@${blogUrl}`)
    } else {
      router.push('/onboarding') // 초기 설정 안된 경우
    }
  }

  function goToLogin () {
    router.push('/login')
  }

  function openLogoutConfirmDialog () {
    showLogoutConfirm.value = true
  }

  function confirmLogout () {
    authStore.logout()
    showLogoutConfirm.value = false
    router.push('/')
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
