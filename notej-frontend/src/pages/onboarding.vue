<template>
  <v-container class="d-flex justify-center align-center" style="height: 100vh;">
    <v-card class="pa-6 rounded-xl" elevation="12" width="500">
      <v-card-title class="text-h5 text-center mb-4">블로그 초기 설정</v-card-title>

      <v-form @submit.prevent="submit">
        <!-- 이메일 (읽기 전용) -->
        <v-text-field
          v-model="email"
          class="pb-6 mb-2 text-grey"
          hide-details
          label="이메일"
          prepend-inner-icon="mdi-email"
          readonly
          variant="outlined"
        />

        <!-- 유저 닉네임 -->
        <v-text-field
          v-model="nickname"
          class="mb-4"
          label="유저 닉네임"
          prepend-inner-icon="mdi-account"
          :rules="[v => !!v || '닉네임을 입력하세요']"
          variant="outlined"
        />

        <!-- 블로그 이름 -->
        <v-text-field
          v-model="blogTitle"
          class="mb-4"
          label="블로그 이름"
          prepend-inner-icon="mdi-book-open-page-variant"
          :rules="[v => !!v || '블로그 이름을 입력하세요']"
          variant="outlined"
        />

        <!-- 블로그 이름 -->
        <v-text-field
          v-model="blogUrl"
          class="mb-6"
          :error-messages="blogUrlError"
          label="블로그 URL"
          prepend-inner-icon="mdi-book-open-page-variant"
          :rules="[
            v => !!v || '블로그 URL에 사용될 키워드를 입력하세요(영어)',
            v => /^[a-zA-Z]+$/.test(v) || '영문자만 입력 가능합니다'
          ]"
          variant="outlined"
          @input="onInput"
        />

        <!-- 제출 버튼 -->
        <v-btn block color="primary" type="submit">시작하기</v-btn>
      </v-form>
    </v-card>
  </v-container>
</template>

<script setup>
  import { onMounted, ref } from 'vue'
  import { useAuthStore } from '@/stores/auth'
  import { useRouter } from 'vue-router'
  import api from '@/utils/axios-interceptor'

  const authStore = useAuthStore()
  const router = useRouter()

  const email = ref('')
  const nickname = ref('')
  const blogTitle = ref('')
  const blogUrl = ref('')
  const blogUrlError = ref('')

  function onInput () {
    blogUrl.value = blogUrl.value.replace(/[^a-zA-Z]/g, '')
    blogUrlError.value = ''
  }

  onMounted(() => {
    if (authStore.user) {
      email.value = authStore.user.email
    }
  })

  const submit = async () => {
    blogUrlError.value = ''
    try {
      await api.post('http://localhost:8080/api/users/complete-profile', {
        nickname: nickname.value,
        blogTitle: blogTitle.value,
        blogUrl: blogUrl.value,
      })

      await authStore.fetchUser() // 갱신
      router.push('/')
    } catch (err) {
      console.error(err)
      const errMsg = err.response?.data?.error
      if (errMsg === 'DUPLICATE_BLOG_URL') {
        blogUrlError.value = '이미 사용 중인 URL입니다.'
      } else {
        alert('정보 등록 중 오류가 발생했습니다.')
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
