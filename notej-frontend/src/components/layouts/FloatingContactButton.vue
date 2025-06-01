<template>
  <div class="contact-button-wrapper">
    <v-tooltip location="top">
      <template #activator="{ props }">
        <v-btn
          v-bind="props"
          class="contact-btn"
          color="primary"
          icon
          size="large"
          @click="dialog = true"
        >
          <v-icon>mdi-email</v-icon>
        </v-btn>
      </template>
      <span>문의하기</span>
    </v-tooltip>

    <v-dialog v-model="dialog" max-width="500">
      <v-card>
        <v-card-title class="text-h6 font-weight-bold">
          문의하기
        </v-card-title>
        <v-card-text>
          <v-text-field
            v-model="email"
            label="문의 답변 받을 이메일"
            prepend-icon="mdi-email-outline"
            required
            type="email"
          />
          <v-textarea
            v-model="message"
            auto-grow
            label="문의 내용"
            prepend-icon="mdi-message-text-outline"
            required
            rows="4"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn text @click="dialog = false">취소</v-btn>
          <v-btn color="primary" @click="submit">보내기</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>


<script setup>
  import { ref } from 'vue'

  const dialog = ref(false)
  const email = ref('')
  const message = ref('')

  function submit () {
    if (!email.value || !message.value) {
      alert('이메일과 문의 내용을 입력해주세요.')
      return
    }

    // 여기에 실제 제출 로직 추가 가능 (API 호출 등)
    console.log('문의 전송:', { email: email.value, message: message.value })

    // 초기화 및 모달 닫기
    email.value = ''
    message.value = ''
    dialog.value = false
  }
</script>

<style scoped>
.contact-button-wrapper {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 1000;
}

.contact-btn {
  transition: all 0.2s ease-in-out;
}
.contact-btn:hover {
  transform: scale(1.1);
  background-color: var(--v-theme-accent);
}
</style>
