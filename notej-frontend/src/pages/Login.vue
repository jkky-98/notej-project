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

  const id = ref('')
  const password = ref('')
  const showPassword = ref(false)
  const authStore = useAuthStore()
  const router = useRouter()

  const togglePassword = () => {
    showPassword.value = !showPassword.value
  }

  const login = async () => {
    try {
      // 실제 로그인 요청은 백엔드 API 호출로 대체
      if (id.value === 'test' && password.value === '1234') {
        document.cookie = 'token=example_token' // 임시 토큰
        authStore.setUser({ id: id.value })
        router.push('/') // 로그인 후 메인 페이지로 이동
      } else {
        alert('아이디 또는 비밀번호가 틀렸습니다.')
      }
    } catch (err) {
      console.error(err)
      alert('로그인 중 오류 발생')
    }
  }

  const backendUrl = import.meta.env.VITE_BACKEND_URL

  const loginGoogle = () => {
    window.location.href = `${backendUrl}/oauth2/authorization/google?mode=login`
  }

  const loginNaver = () => {
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
