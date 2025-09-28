<template>
  <div>
    <!-- 블로그 포스트 목록 -->
    <div v-for="post in blogStore.posts" :key="post.id" class="post-item">
      <h3>{{ post.title }}</h3>
      <p>{{ post.content }}</p>
      <!-- 추가 포스트 내용 -->
    </div>

    <!-- 에러 메시지 표시 -->
    <div v-if="blogStore.error" class="error-message">
      {{ blogStore.error }}
      <button @click="retryLoading">다시 시도</button>
    </div>

    <!-- 무한 로딩 컴포넌트 -->
    <InfiniteLoading @infinite="infiniteHandler" />
  </div>
</template>

<script setup>
  import { onMounted } from 'vue';
  import InfiniteLoading from 'v3-infinite-loading';
  import 'v3-infinite-loading/lib/style.css';
  import { useBlogPostStore } from '@/stores/blogMainPost';

  // Pinia 스토어 인스턴스 생성
  const blogStore = useBlogPostStore();

  // 컴포넌트 마운트 시 초기화 (선택 사항)
  onMounted(() => {
  // 페이지 진입 시 상태 초기화가 필요하다면 주석 해제
  // blogStore.resetPostState();
  });

  // v3-infinite-loading의 무한 스크롤 핸들러
  const infiniteHandler = async $state => {
    try {
      // 필요한 추가 파라미터 (있다면)
      const additionalParams = {
        category: 'tech', // 예시: 카테고리 필터
        sort: 'latest', // 예시: 정렬 방식
      };

      // Pinia 스토어의 액션 호출
      const result = await blogStore.loadMorePosts(additionalParams);

      // 결과에 따라 InfiniteLoading 컴포넌트 상태 업데이트
      if (result.success) {
        if (result.hasMore) {
          $state.loaded(); // 데이터 로딩 완료, 더 불러올 데이터가 있음
        } else {
          $state.complete(); // 모든 데이터 로딩 완료
        }
      } else {
        $state.error(); // 에러 발생
      }
    } catch (e) {
      console.error('무한 스크롤 처리 중 오류:', e);
      $state.error(); // 에러 상태 설정
    }
  };

  // 에러 발생 시 다시 시도하는 함수
  const retryLoading = () => {
    blogStore.error = null; // 에러 상태 초기화
    // 현재 페이지 다시 로드 (페이지 번호를 1 감소시키고 다시 시도)
    blogStore.page = Math.max(1, blogStore.page - 1);
    infiniteHandler({ loaded: () => {}, error: () => {}, complete: () => {} });
  };
</script>

<style scoped>
.post-item {
  border: 1px solid #eee;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 5px;
}

.error-message {
  color: #d32f2f;
  padding: 10px;
  margin: 10px 0;
  background-color: #ffebee;
  border-radius: 4px;
  text-align: center;
}
</style>
