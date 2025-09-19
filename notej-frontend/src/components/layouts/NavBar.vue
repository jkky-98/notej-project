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

  <!-- 로그아웃 확인 다이얼로그 -->
  <v-dialog v-model="showLogoutConfirm" max-width="400">
    <v-card class="pa-3">
      <v-card-title class="text-h6">로그아웃 하시겠습니까?</v-card-title>
      <v-card-actions class="justify-end">
        <v-btn text @click="showLogoutConfirm = false">취소</v-btn>
        <v-btn color="red" variant="tonal" @click="confirmLogout">로그아웃</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <!-- 글쓰기 페이지 이동 확인 다이얼로그 -->
  <v-dialog v-model="confirmDialog" max-width="400">
    <v-card>
      <v-card-title class="text-h5">
        주의
      </v-card-title>

      <v-card-text>
        작성 중인 게시글이 초기화될 수 있습니다. 초기화하시겠습니까?
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn color="grey-darken-1" variant="text" @click="cancelNavigation">
          아니오
        </v-btn>
        <v-btn color="error" variant="text" @click="proceedNavigation">
          예
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script setup>
  import { computed, nextTick, ref } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { useTheme } from 'vuetify'
  import { useThemeStore } from '@/stores/theme'
  import { useAuthStore } from '@/stores/auth'

  const router = useRouter()
  const route = useRoute()
  const theme = useTheme()
  const themeStore = useThemeStore()
  const authStore = useAuthStore()

  const showLogoutConfirm = ref(false)
  const isLoggedIn = computed(() => authStore.isAuthenticated)

  // 라우팅 관련 상태 변수
  const confirmDialog = ref(false)
  const pendingTargetPath = ref(null)
  const isRefreshingCurrentWritePage = ref(false)

  // 모든 네비게이션 요청 처리 공통 함수
  function handleAnyNavigation (targetPath) {
    // 현재 /write 페이지에 있는지 확인
    const isOnWritePage = route.path === '/write' || route.path.startsWith('/write?id=')

    if (isOnWritePage) {
      if (targetPath === '/write') {
        // 현재 /write 페이지에서 다시 /write 버튼을 누른 경우 (새로고침 의도)
        isRefreshingCurrentWritePage.value = true
      } else {
        // /write 페이지에서 다른 페이지로 이동하려는 경우
        isRefreshingCurrentWritePage.value = false
      }
      pendingTargetPath.value = targetPath
      confirmDialog.value = true // 확인 다이얼로그 표시
    } else {
      // /write 페이지가 아니면 바로 이동
      router.push(targetPath)
    }
  }

  // 네비게이션 함수들
  function onCreatePost () {
    handleAnyNavigation('/write')
  }

  function goToProfile () {
    handleAnyNavigation('/profile')
  }

  function goToBlog () {
    const blogUrl = authStore.user?.blogUrl
    if (blogUrl) {
      handleAnyNavigation(`/@${blogUrl}`)
    } else {
      handleAnyNavigation('/onboarding')
    }
  }

  function goToLogin () {
    handleAnyNavigation('/login')
  }

  // 다이얼로그 '예' 버튼 클릭 시 실행될 함수
  function proceedNavigation () {
    confirmDialog.value = false // 다이얼로그 닫기

    if (isRefreshingCurrentWritePage.value) {
      // /write 페이지에서 다시 /write 버튼을 눌러 리마운트 요청한 경우
      nextTick(() => {
        router.go(0) // 현재 페이지 강제 새로고침
      })
    } else if (pendingTargetPath.value) {
      // /write 페이지에서 다른 곳으로 이동하려던 경우
      router.push(pendingTargetPath.value)
    }

    // 상태 초기화
    pendingTargetPath.value = null
    isRefreshingCurrentWritePage.value = false
  }

  // 다이얼로그 '아니오' 버튼 클릭 시 실행될 함수
  function cancelNavigation () {
    confirmDialog.value = false // 다이얼로그 닫기
    pendingTargetPath.value = null // 목적지 정보 삭제
    isRefreshingCurrentWritePage.value = false // 상태 초기화
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
