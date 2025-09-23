<!-- components/CommentItem.vue -->
<template>
  <v-card class="comment-item mb-4" flat>
    <div class="d-flex">
      <v-avatar class="mr-3" size="36">
        <v-img alt="Avatar" :src="comment.author?.avatarUrl || '/default-avatar.png'" />
      </v-avatar>

      <div class="flex-grow-1">
        <div class="d-flex align-center">
          <div class="font-weight-medium">{{ comment.author?.name || '익명' }}</div>
          <div class="text-caption text-medium-emphasis ml-2">
            {{ formatDate(comment.createdAt) }}
          </div>
        </div>

        <div class="mt-1 comment-content">{{ comment.content }}</div>

        <div class="d-flex mt-2">
          <v-btn
            class="px-0 mr-4"
            size="small"
            variant="text"
            @click="$emit('reply', comment)"
          >
            답글
          </v-btn>

          <v-btn
            v-if="isCommentAuthor(comment)"
            class="px-0"
            color="error"
            size="small"
            variant="text"
            @click="$emit('delete', comment.id)"
          >
            삭제
          </v-btn>
        </div>
      </div>
    </div>

    <!-- 답글 목록 -->
    <div v-if="replies && replies.length > 0" class="replies-container mt-3 ml-8">
      <v-card
        v-for="reply in replies"
        :key="reply.id"
        class="pa-3 mb-2"
        flat
        variant="outlined"
      >
        <div class="d-flex">
          <v-avatar class="mr-2" size="32">
            <v-img alt="Avatar" :src="reply.author?.avatarUrl || '/default-avatar.png'" />
          </v-avatar>

          <div class="flex-grow-1">
            <div class="d-flex align-center">
              <div class="font-weight-medium">{{ reply.author?.name || '익명' }}</div>
              <div class="text-caption text-medium-emphasis ml-2">
                {{ formatDate(reply.createdAt) }}
              </div>
            </div>

            <div class="mt-1 comment-content">{{ reply.content }}</div>

            <div class="d-flex mt-2">
              <v-btn
                v-if="isCommentAuthor(reply)"
                class="px-0"
                color="error"
                size="small"
                variant="text"
                @click="$emit('delete', reply.id)"
              >
                삭제
              </v-btn>
            </div>
          </div>
        </div>
      </v-card>
    </div>
  </v-card>
</template>

<script setup>
  import { computed } from 'vue';
  import { useAuthStore } from '@/stores/auth'; // 인증 스토어 가정

  const props = defineProps({
    comment: {
      type: Object,
      required: true,
    },
    replies: {
      type: Array,
      default: () => [],
    },
  });

  // 이벤트 정의
  defineEmits(['reply', 'delete']);

  // 인증 스토어 사용
  const authStore = useAuthStore();

  // 현재 로그인한 사용자가 댓글 작성자인지 확인
  const isCommentAuthor = comment => {
    return authStore.user && comment.author && authStore.user.email === comment.author.email;
  };

  // 날짜 포맷 함수
  const formatDate = dateString => {
    if (!dateString) return '';

    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffSec = Math.floor(diffMs / 1000);

    // 1분 이내
    if (diffSec < 60) {
      return '방금 전';
    }

    // 1시간 이내
    const diffMin = Math.floor(diffSec / 60);
    if (diffMin < 60) {
      return `${diffMin}분 전`;
    }

    // 24시간 이내
    const diffHour = Math.floor(diffMin / 60);
    if (diffHour < 24) {
      return `${diffHour}시간 전`;
    }

    // 7일 이내
    const diffDay = Math.floor(diffHour / 24);
    if (diffDay < 7) {
      return `${diffDay}일 전`;
    }

    // 그 외에는 날짜 표시
    return `${date.getFullYear()}.${date.getMonth() + 1}.${date.getDate()}`;
  };
</script>

<style scoped>
.comment-content {
  white-space: pre-line;
  word-break: break-word;
}

.replies-container {
  border-left: 2px solid rgba(0, 0, 0, 0.08);
  padding-left: 12px;
}
</style>
