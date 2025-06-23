<template>
  <v-container class="d-flex justify-center align-center" style="height: 100vh;">
    <v-card class="pa-6 rounded-xl" elevation="12" width="400">
      <v-card-title class="text-h5 text-center mb-4">로그인</v-card-title>

      <!-- 아이디 입력 -->
      <v-text-field
        v-model="id"
        class="mb-4"
        dense
        hide-details
        label="아이디"
        prepend-inner-icon="mdi-account"
        variant="outlined"
      />

      <!-- 비밀번호 입력 -->
      <v-text-field
        v-model="password"
        :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
        class="mb-4"
        dense
        hide-details
        label="비밀번호"
        prepend-inner-icon="mdi-lock"
        :type="showPassword ? 'text' : 'password'"
        variant="outlined"
        @click:append-inner="togglePassword"
      />

      <!-- 로그인 버튼 -->
      <v-btn
        block
        class="mb-4"
        color="primary"
        @click="login"
      >
        로그인
      </v-btn>

      <!-- 소셜 로그인 -->
      <v-row class="mb-4" justify="space-between">
        <!-- NAVER -->
        <v-col cols="6">
          <v-btn
            block
            :style="{
              borderColor: '#03C75A',
              color: '#03C75A'
            }"
            variant="outlined"
            @click="loginNaver"
          >
            <img
              alt="Naver logo"
              src="/images/naver-logo.png"
              style="width: 20px; height: 20px; object-fit: contain; margin-right: 8px;"
            >
            NAVER
          </v-btn>
        </v-col>

        <!-- GOOGLE -->
        <v-col cols="6">
          <v-btn
            block
            :style="{
              borderColor: '#DB4437',
              color: '#DB4437'
            }"
            variant="outlined"
            @click="loginGoogle"
          >
            <img
              alt="Google logo"
              src="/images/google-logo.jpeg"
              style="width: 20px; height: 20px; object-fit: contain; margin-right: 8px;"
            >
            GOOGLE
          </v-btn>
        </v-col>
      </v-row>


      <!-- 회원가입 링크 -->
      <v-card-text class="text-center text-caption">
        계정이 없으신가요?
        <RouterLink class="text-primary text-decoration-underline" to="/signup">
          회원가입
        </RouterLink>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup>
  import { ref } from 'vue'
  import { useAuthStore } from '@/stores/auth'
  import { useRouter } from 'vue-router'
  import api from '@/utils/axios-interceptor'

  const id = ref('')
  const password = ref('')
  const showPassword = ref(false)
  const authStore = useAuthStore()
  const router = useRouter()
  const backendUrl = import.meta.env.VITE_BACKEND_URL
  const frontendUrl = import.meta.env.VITE_FRONTEND_URL

  const togglePassword = () => {
    showPassword.value = !showPassword.value
  }

  const login = async () => {
    try {
      const res = await api.post('/api/auth/credentials/login', {
        email: id.value,
        password: password.value,
      })

      await authStore.fetchUser() // 로그인 후 유저 정보 갱신
      router.push('/login/success')
    } catch (err) {
      console.error(err)
      alert('아이디 또는 비밀번호가 틀렸습니다.')
    }
  }

  const setRedirectUriCookie = () => {
    const target = `${frontendUrl}/login/success` // 실제 리디렉션 대상
    document.cookie = `redirect_uri=${target}; path=/`
  }
  const loginGoogle = () => {
    setRedirectUriCookie()
    window.location.href = `${backendUrl}/oauth2/authorization/google?mode=login`
  }

  const loginNaver = () => {
    setRedirectUriCookie()
    window.location.href = `${backendUrl}/oauth2/authorization/naver?mode=login`
  }
</script>

<style scoped>
.v-card {
  transition: box-shadow 0.3s ease;
}
.v-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}
</style>
