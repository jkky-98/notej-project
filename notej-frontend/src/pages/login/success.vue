<template>
  <v-container
    class="d-flex justify-center align-center"
    style="height: 100vh;"
  >
    <v-progress-circular
      color="primary"
      indeterminate
      size="64"
      width="6"
    />
  </v-container>
</template>

<script setup>
  import { useAuthStore } from '@/stores/auth'
  import { useRouter } from 'vue-router'
  import { onMounted } from 'vue'

  const authStore = useAuthStore()
  const router = useRouter()

  onMounted(async () => {
    try {
      await authStore.fetchUser()

      if (authStore.user?.completed === false) {
        router.replace('/onboarding') // 🔄 첫 로그인 유저는 온보딩
      } else {
        router.replace('/') // ✅ 이미 등록된 유저는 메인
      }
    } catch (e) {
      console.error('사용자 정보 조회 실패:', e)
      router.replace('/login') // ❌ 실패 시 로그인 페이지로
    }
  })
</script>
