<template>
  <v-container fluid>
    <v-row no-gutters>
      <v-col cols="12" md="3">
        <BlogSidebar />
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
  import { onMounted } from 'vue'
  import { useRoute } from 'vue-router'
  import { useBlogStore } from '@/stores/blog'

  import BlogSidebar from '@/components/layouts/BlogSidebar.vue'

  const route = useRoute()
  const blogStore = useBlogStore()

  onMounted(async () => {
    await blogStore.fetchBlogInfo(route.params.blogUrlName)
    await blogStore.fetchBlogCategories(route.params.blogUrlName)
  })
</script>
