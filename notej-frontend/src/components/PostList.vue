<template>
  <v-container fluid>
    <v-row dense>
      <v-col
        v-for="post in blogStore.posts"
        :key="post.id"
        cols="12"
        lg="3"
        md="4"
        sm="6"
      >
        <v-card
          class="mb-4"
          elevation="3"
          max-width="400"
          style="cursor: pointer"
          @click="goToPost(post.id)"
        >
          <v-img
            v-if="thumbnailUrls[post.id]"
            :key="post.id + '-img'"
            alt="포스트 썸네일"
            class="thumbnail-image"
            cover
            height="180"
            :src="thumbnailUrls[post.id]"
          />
          <v-skeleton-loader
            v-else
            height="180"
            type="image"
          />

          <v-card-title class="text-h6 text-truncate">{{ post.title }}</v-card-title>

          <v-card-text class="text-truncate-content">
            <div class="text--secondary text-truncate-2">
              {{ post.bio }}
            </div>
          </v-card-text>

          <v-card-subtitle class="d-flex justify-space-between grey--text text--darken-1">
            <div>{{ formatDate(post.updatedAt) }}</div>
            <div class="d-flex align-center">
              <v-icon class="mr-1" size="small">mdi-eye</v-icon>
              <span>123</span>
              <v-icon class="ml-3 mr-1" size="small">mdi-comment</v-icon> <!-- 댓글 아이콘 -->
              <span>{{ post.commentCount }}</span> <!-- 댓글 수 -->
            </div>
          </v-card-subtitle>
        </v-card>
      </v-col>
    </v-row>

    <div class="text-center mt-4">
      <InfiniteLoading @infinite="infiniteHandler" />
    </div>

    <v-alert
      v-if="blogStore.error"
      class="mt-4"
      dismissible
      type="error"
    >
      {{ blogStore.error }}
      <v-btn size="small" text @click="retryLoading">다시 시도</v-btn>
    </v-alert>
  </v-container>
</template>

<script setup>
  import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
  import InfiniteLoading from 'v3-infinite-loading';
  import 'v3-infinite-loading/lib/style.css';
  import { useBlogPostStore } from '@/stores/blogMainPost';
  import { useRouter } from 'vue-router'
  import defaultThumbnail from '@/assets/defaults/thumbnail-post-default.jpg';

  const blogStore = useBlogPostStore();
  const thumbnailUrls = ref({});
  const defaultImage = defaultThumbnail // 실제 경로에 맞게 수정해주세요.
  const router = useRouter();

  onMounted(() => {
    console.log('[컴포넌트] PostList 컴포넌트 마운트됨');
    if (blogStore.posts.length > 0) {
      loadPostsThumbnails(blogStore.posts);
    }
  });

  watch(
    () => blogStore.posts,
    newPosts => {
      console.log('[컴포넌트] posts 변경 감지됨, 길이:', newPosts?.length);
      if (newPosts && newPosts.length) {
        loadPostsThumbnails(newPosts);
      }
    },
    { deep: true } // 배열 내용 변화 감지를 위해 deep: true 옵션 추가
  );

  async function loadPostsThumbnails (posts) {
    console.time('썸네일 전체 로딩 시간');

    // 필터 조건 수정: thumbnailUrl 없는 포스트도 포함시킴
    const thumbnailPromises = posts
      .filter(post => !thumbnailUrls.value[post.id]) // thumbnailUrl 체크 제거
      .map(async post => {
        if (!post.thumbnailUrl) {
          // thumbnailUrl이 없으면 기본 이미지를 바로 사용
          return { postId: post.id, useDefault: true };
        }

        // 기존 로직: thumbnailUrl이 있는 경우 로드 시도
        console.time(`썸네일 로딩 시간: 포스트 ${post.id}`);
        try {
          const url = await blogStore.fetchThumbnailBytes(post.thumbnailUrl);
          console.timeEnd(`썸네일 로딩 시간: 포스트 ${post.id}`);
          return { postId: post.id, url };
        } catch (error) {
          console.timeEnd(`썸네일 로딩 시간: 포스트 ${post.id}`);
          console.error(`[컴포넌트] 포스트 ID ${post.id} 썸네일 로드 실패:`, error);
          return { postId: post.id, useDefault: true };
        }
      });

    const results = await Promise.all(thumbnailPromises);
    console.timeEnd('썸네일 전체 로딩 시간');

    const updatedThumbnails = { ...thumbnailUrls.value };
    results.forEach(result => {
      if (result.url) {
        updatedThumbnails[result.postId] = result.url;
        console.log(`[컴포넌트] 이미지 : ${updatedThumbnails[result.postId]}`);
      } else if (result.useDefault) {
        // useDefault 플래그로 기본 이미지 사용 여부 확인
        updatedThumbnails[result.postId] = defaultImage;
        console.log(`[컴포넌트] 포스트 ID ${result.postId} 기본 이미지 사용`);
        console.log(`[컴포넌트] 기본 이미지 : ${updatedThumbnails[result.postId]}`);
      }
    });

    thumbnailUrls.value = updatedThumbnails;
  }

  const infiniteHandler = async $state => {
    console.log('[컴포넌트] InfiniteLoading: infiniteHandler 호출됨');
    try {
      const result = await blogStore.loadMorePosts();
      console.log('[컴포넌트] InfiniteLoading: loadMorePosts 결과:', result);
      if (result.success) {
        if (result.hasMore) {
          $state.loaded();
          console.log('[컴포넌트] InfiniteLoading: loaded() 호출');
        } else {
          $state.complete();
          console.log('[컴포넌트] InfiniteLoading: complete() 호출');
        }
      } else {
        $state.error();
        console.log('[컴포넌트] InfiniteLoading: error() 호출 (loadMorePosts 실패)');
      }
    } catch (error) {
      console.error('[컴포넌트] InfiniteLoading: 무한 스크롤 중 예외 발생:', error);
      $state.error();
    }
  };

  // 포스트 상세 페이지로 이동하는 함수
  function goToPost (postId) {
    router.push(`/post/${postId}`);
  }

  const retryLoading = () => {
    console.log('[컴포넌트] 재시도 버튼 클릭');
    blogStore.error = null;
    blogStore.page = Math.max(1, blogStore.page - 1);
    infiniteHandler({ loaded: () => {}, error: () => {}, complete: () => {} });
  };

  function formatDate (dateString) {
    if (!dateString) return '';
    const d = new Date(dateString);
    return d.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });
  }

  onBeforeUnmount(() => {
    console.log('[컴포넌트] PostList 컴포넌트 언마운트. Blob URL 정리 시작');
    Object.values(thumbnailUrls.value).forEach(url => {
      if (url && url.startsWith('blob:')) {
        URL.revokeObjectURL(url);
        console.log(`[컴포넌트] Blob URL 정리됨: ${url}`);
      }
    });
  });
</script>

<style>
.text-truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-truncate-2 {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  white-space: normal;
  max-height: 48px; /* 고정된 높이 지정 */
}

.text-truncate-content {
  height: 60px; /* v-card-text 영역 높이 고정 */
}

/* 카드 자체 높이도 고정 */
.v-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.v-card-title {
  height: 48px; /* 제목 영역 높이 고정 */
}

.v-card-subtitle {
  margin-top: auto; /* 하단에 고정 */
}
</style>
