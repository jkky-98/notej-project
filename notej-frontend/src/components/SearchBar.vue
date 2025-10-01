<template>
  <v-text-field
    v-model="searchQuery"
    clearable
    density="compact"
    hide-details
    label="제목 검색"
    prepend-inner-icon="mdi-magnify"
    variant="outlined"
    @click:clear="clearSearch"
    @keyup.enter="search"
  >
    <template #append>
      <v-btn
        color="primary"
        icon
        size="small"
        @click="search"
      >
        <v-icon>mdi-magnify</v-icon>
      </v-btn>
    </template>
  </v-text-field>
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import { useBlogPostStore } from '@/stores/blogMainPost';

  const props = defineProps({
    initialQuery: {
      type: String,
      default: '',
    },
  });

  const blogPostStore = useBlogPostStore();
  const searchQuery = ref(props.initialQuery || '');

  function search () {
    if (searchQuery.value.trim() === '') {
      clearSearch();
      return;
    }

    // 검색어로 Pinia 스토어 필터 설정
    blogPostStore.resetState(); // 기존 포스트 목록 초기화
    blogPostStore.setFilters({ search: searchQuery.value.trim() });
    blogPostStore.loadMorePosts(); // 검색 결과 로딩
  }

  function clearSearch () {
    searchQuery.value = '';
    blogPostStore.resetState();
    blogPostStore.setFilters({ search: null });
    blogPostStore.loadMorePosts();
  }

  // URL 파라미터에서 검색어 가져오기 (옵션)
  onMounted(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const searchParam = urlParams.get('search');
    if (searchParam) {
      searchQuery.value = searchParam;
      search();
    }
  });
</script>
