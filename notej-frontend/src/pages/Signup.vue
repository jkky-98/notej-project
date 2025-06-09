<template>
  <v-container class="d-flex justify-center align-center" style="height: 100vh;">
    <v-card class="pa-6 rounded-xl" elevation="12" width="500">
      <v-card-title class="text-h5 text-center mb-4">회원가입</v-card-title>

      <v-form @submit.prevent="submit">
        <!-- 이메일 -->
        <v-text-field
          v-model="email"
          class="mb-4"
          label="이메일"
          prepend-inner-icon="mdi-email"
          :rules="[v => !!v || '이메일을 입력해주세요']"
          variant="outlined"
        />

        <!-- 이름 -->
        <v-text-field
          v-model="name"
          class="mb-4"
          label="이름"
          prepend-inner-icon="mdi-account"
          :rules="[v => !!v || '이름을 입력해주세요']"
          variant="outlined"
        />

        <!-- 비밀번호 -->
        <v-text-field
          v-model="password"
          :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
          class="mb-4"
          label="비밀번호"
          prepend-inner-icon="mdi-lock"
          :rules="[v => v.length >= 8 || '8자 이상 입력해주세요']"
          :type="showPassword ? 'text' : 'password'"
          variant="outlined"
          @click:append-inner="togglePassword"
        />

        <!-- 비밀번호 확인 -->
        <v-text-field
          v-model="confirmPassword"
          class="mb-6"
          label="비밀번호 확인"
          prepend-inner-icon="mdi-lock-check"
          :rules="[v => v === password || '비밀번호가 일치하지 않습니다']"
          :type="showPassword ? 'text' : 'password'"
          variant="outlined"
        />

        <!-- 제출 버튼 -->
        <v-btn block color="primary" type="submit">회원가입</v-btn>
      </v-form>
    </v-card>
  </v-container>
</template>

<script setup>
  import { ref } from 'vue'
  import api from '@/utils/axios-interceptor'
  import { useRouter } from 'vue-router'

  const email = ref('')
  const name = ref('')
  const password = ref('')
  const confirmPassword = ref('')
  const showPassword = ref(false)
  const router = useRouter()

  const togglePassword = () => {
    showPassword.value = !showPassword.value
  }

  const backendUrl = import.meta.env.VITE_BACKEND_URL

  const submit = async () => {
    try {
      await api.post(`${backendUrl}/api/auth/credentials/signup`, {
        email: email.value,
        password: password.value,
        confirmPassword: confirmPassword.value,
        name: name.value,
      })

      alert('회원가입이 완료되었습니다. 로그인해주세요.')
      router.push('/login')
    } catch (err) {
      console.error(err)
      if (err.response?.data?.message) {
        alert(err.response.data.message)
      } else {
        alert('회원가입 중 오류가 발생했습니다.')
      }
    }
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
