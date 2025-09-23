<!-- components/CommentComponent.vue -->
<template>
  <v-card class="comment-section mt-8" elevation="0" flat>
    <v-card-title class="pa-0 mb-4">댓글 {{ postViewStore.totalComments }}개</v-card-title>

    <!-- 댓글 작성 폼 -->
    <v-card class="mb-6 pa-4" variant="outlined">
      <div v-if="postViewStore.replyTo" class="mb-2 reply-info">
        <span class="text-body-2">
          {{ getCommentAuthorName(postViewStore.replyTo) }}님에게 답글 작성 중
        </span>
        <v-btn icon size="small" variant="text" @click="postViewStore.resetCommentForm">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </div>

      <v-textarea
        v-model="postViewStore.commentContent"
        auto-grow
        hide-details
        :label="postViewStore.replyTo ? '답글을 남겨주세요' : '댓글을 남겨주세요'"
        rows="3"
        variant="outlined"
      />

      <div class="d-flex justify-end mt-2">
        <v-btn
          color="primary"
          :disabled="!postViewStore.commentContent.trim()"
          :loading="postViewStore.submittingComment"
          @click="addComment"
        >
          {{ postViewStore.replyTo ? '답글 작성' : '댓글 작성' }}
        </v-btn>
      </div>
    </v-card>

    <!-- 댓글 로딩 표시 -->
    <div v-if="postViewStore.commentsLoading" class="text-center pa-4">
      <v-progress-circular color="primary" indeterminate />
    </div>

    <!-- 댓글 에러 표시 -->
    <v-alert v-else-if="postViewStore.commentsError" class="mb-4" type="error">
      {{ postViewStore.commentsError }}
    </v-alert>

    <!-- 댓글 목록 -->
    <div v-else-if="postViewStore.totalComments > 0" class="comment-list">
      <template v-for="comment in postViewStore.parentComments" :key="comment.id">
        <comment-item
          :comment="comment"
          :replies="postViewStore.getRepliesFor(comment.id)"
          @delete="handleDelete"
          @reply="handleReply"
        />
      </template>
    </div>

    <div v-else class="text-center pa-4 text-subtitle-1 text-medium-emphasis">
      아직 댓글이 없습니다. 첫 댓글을 작성해보세요!
    </div>
  </v-card>
</template>

<script setup>
  import { computed, onMounted } from 'vue';
  import { useRoute } from 'vue-router';
  import { usePostViewStore } from '@/stores/postView';
  import CommentItem from './CommentItem.vue';

  const route = useRoute();
  const postViewStore = usePostViewStore();
  const postId = computed(() => route.params.postId);

  // 댓글 작성자 이름 가져오기
  const getCommentAuthorName = commentId => {
    const comment = postViewStore.comments.find(c => c.id === commentId);
    return comment ? (comment.author?.name || '익명') : '사용자';
  };

  // 댓글 작성
  const addComment = async () => {
    await postViewStore.addComment(postId.value);
  };

  // 답글 작성 모드 설정
  const handleReply = comment => {
    postViewStore.setReplyTo(comment.id);

  // 필요한 경우 답글 폼으로 스크롤
  // 스크롤 로직 구현
  };

  // 댓글 삭제
  const handleDelete = async commentId => {
    if (confirm('정말 이 댓글을 삭제하시겠습니까?')) {
      await postViewStore.deleteComment(commentId);
    }
  };

  // 컴포넌트 마운트 시 댓글 로드
  onMounted(() => {
    postViewStore.fetchComments(postId.value);
  });
</script>

<style scoped>
.comment-section {
  padding-top: 24px;
}

.reply-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background-color: rgba(0, 0, 0, 0.03);
  border-radius: 4px;
}
</style>
