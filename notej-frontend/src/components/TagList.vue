<!-- components/TagList.vue -->
<template>
  <v-card class="mb-4">
    <v-card-title>태그</v-card-title>
    <v-card-text>
      <div v-if="blogMaintagStore.isLoading">
        <v-skeleton-loader :height="40" type="chip" :width="80" />
        <v-skeleton-loader class="ml-2" :height="40" type="chip" :width="100" />
      </div>
      <div v-else-if="blogMaintagStore.error" class="text-error">
        태그를 불러올 수 없습니다.
        <v-btn small text @click="retryLoadTags">재시도</v-btn>
      </div>
      <div v-else-if="blogMaintagStore.tags.length === 0" class="text-caption grey--text">
        등록된 태그가 없습니다.
      </div>
      <div v-else>
        <v-chip
          v-for="tag in blogMaintagStore.tags"
          :key="tag.name"
          class="ma-1"
          :color="isTagActive(tag.name) ? 'primary' : ''"
          label
          size="small"
          @click="toggleTagFilter(tag.name)"
        >
          #{{ tag.name }} ({{ tag.count }})   <!-- 변경: 태그 카운트도 표시 -->
        </v-chip>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup>
  import { computed } from 'vue';
  import { useBlogMaintagStore } from '@/stores/blogMaintag';
  import { useBlogPostStore } from '@/stores/blogMainPost'; // 포스트 필터링을 위해 임포트

  const blogMaintagStore = useBlogMaintagStore();
  const blogPostStore = useBlogPostStore(); // 포스트 스토어 인스턴스 가져오기

  // 현재 선택된 태그 필터가 있는지 확인 (UI 색상 변경 용도)
  const isTagActive = computed(() => tagName => {
    return blogPostStore.filters.tagName === tagName;
  });

  // 태그 클릭 시 필터 토글
  const toggleTagFilter = tagName => {
    const currentTagName = blogPostStore.filters.tagName;
    let newTagName = null;

    if (currentTagName === tagName) {
      // 이미 선택된 태그를 다시 클릭하면 필터 해제
      newTagName = null;
      console.log(`[TagList] 태그 필터 해제: ${tagName}`);
    } else {
      // 새로운 태그 선택
      newTagName = tagName;
      console.log(`[TagList] 태그 필터 적용: ${tagName}`);
    }

    // 필터 업데이트 및 포스트 재로드
    blogPostStore.setFilters({
      tagName: newTagName,
    // 다른 필터들을 필요에 따라 초기화할 수 있습니다.
    // 예: search: null, sortBy: 'latest' 등
    // 여기서는 태그만 변경하고 나머지는 유지
    });
    console.log(`[TagList] 필터에 적용된 태그 : ${blogPostStore.filters}`);
    blogPostStore.loadMorePosts(); // 필터 적용 후 포스트 다시 불러오기
  };

  // 태그 로딩 재시도 함수 (에러 발생 시)
  const retryLoadTags = () => {
    if (blogPostStore.blogUrl) {
      blogMaintagStore.fetchTags(blogPostStore.blogUrl);
    }
  };
</script>

<style scoped>
/* 여기에 필요한 스타일 추가 */
</style>
