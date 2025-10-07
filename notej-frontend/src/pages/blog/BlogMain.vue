<template>
  <v-container fluid>
    <v-row no-gutters>
      <v-col cols="12" md="3">
        <BlogSidebar />
      </v-col>

      <v-col cols="10" md="6">
        <v-card>
          <!-- 새로운 v-row를 v-card-text 안에 추가 -->
          <v-card-text>
            <v-row class="mb-4" dense>
              <v-col cols="12">
                <SearchBar />
              </v-col>
            </v-row>

            <PostList />
          </v-card-text>
        </v-card>
      </v-col>

      <v-col class="pl-6" cols="2">
        <v-card>
          <!-- 태그 리스트를 가져올 부분 -->
          <TagList />
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
  import { onMounted } from 'vue'
  import { useRoute } from 'vue-router'
  import { useBlogStore } from '@/stores/blog'
  import { useBlogPostStore } from '@/stores/blogMainPost'
  import { useBlogMaintagStore } from '@/stores/blogMaintag' // 태그 스토어

  import BlogSidebar from '@/components/layouts/BlogSidebar.vue'
  import PostList from '@/components/PostList.vue'
  import SearchBar from '@/components/SearchBar.vue'

  const route = useRoute()
  const blogStore = useBlogStore()
  const blogPostStore = useBlogPostStore()
  const blogMaintagStore = useBlogMaintagStore()

  onMounted(async () => {
    const blogUrlName = route.params.blogUrlName;
    await blogStore.fetchBlogInfo(route.params.blogUrlName)
    await blogStore.fetchBlogCategories(route.params.blogUrlName)
    // 블로그 URL 설정 후 초기 포스트 로드
    blogPostStore.setBlogUrl(blogUrlName)
    blogPostStore.loadMorePosts();

    // ⭐️ 태그 리스트 데이터 로드
    await blogMaintagStore.fetchTags(blogUrlName);
  })
</script>
