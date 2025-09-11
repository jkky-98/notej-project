// stores/blog.js
import { defineStore } from 'pinia'
import api from '@/utils/axios-interceptor'

export const useBlogStore = defineStore('blog', {
  state: () => ({
    user: {
      name: '',
      bio: '',
      profilePicture: null,
    },
    categories: [],
    posts: [],
    page: 0,
    hasMore: true,
    loading: false,
    selectedCategory: null,
    topPosts: [],
    drawerOpen: true,
  }),
  getters: {
    categoryTree (state) {
      const map = new Map();
      const roots = [];

      // 카테고리 데이터가 categoryId, parentId 구조라고 가정했을 때
      state.categories.forEach(cat => {
        map.set(cat.categoryId, { ...cat, children: [] });
      });

      map.forEach(cat => {
        if (cat.parentId) {
          const parent = map.get(cat.parentId);
          if (parent) parent.children.push(cat);
          else roots.push(cat);
        } else {
          roots.push(cat);
        }
      });

      return roots;
    },
  },

  actions: {
    toggleDrawer () {
      this.drawerOpen = !this.drawerOpen
      localStorage.setItem('sidebarOpen', this.drawerOpen)
    },

    selectCategory (category) {
      this.selectedCategory = category
      this.page = 0
      this.posts = []
      this.hasMore = true
      console.log('선택한 카테고리 (Pinia):', category.name)
      // 여기서 새 카테고리 포스트 로딩 호출 추가 권장
      // 예: await this.loadMorePosts(blogUrlName)
    },


    async fetchBlogInfo (blogUrlName) {
      this.loading = true
      try {
        const res = await api.get(`/api/blog/${blogUrlName}/info`)
        this.user = res.data.user || this.user
      } catch (e) {
        console.error('블로그 정보 가져오기 실패:', e)
      } finally {
        this.loading = false
      }
    },

    async fetchBlogCategories (blogUrlName) {
      this.loading = true
      try {
        const res = await api.get(`/api/blog/${blogUrlName}/categories`)
        this.categories = res.data.categoryAll || this.categories
        console.log(this.categories)
      } catch (e) {
        console.error('블로그 카테고리 정보 가져오기 실패:', e)
      } finally {
        this.loading = false
      }
    },

    async loadMorePosts (blogUrlName) {
      if (this.loading || !this.hasMore) return
      this.loading = true
      try {
        const res = await api.get(`/api/blogs/${blogUrlName}/posts`, {
          params: {
            page: this.page,
            size: 10,
            categoryId: this.selectedCategory?.id || null,
          },
        })
        if (res.data?.content) {
          this.posts.push(...res.data.content)
          this.hasMore = !res.data.last
          this.page++
        } else {
          this.hasMore = false
        }
      } catch (e) {
        console.error('포스트 로딩 실패:', e)
      } finally {
        this.loading = false
      }
    },

    async fetchTopPosts (blogUrlName) {
      try {
        const res = await api.get(`/api/blogs/${blogUrlName}/top3`)
        this.topPosts = res.data || []
      } catch (e) {
        console.error('인기 글 불러오기 실패:', e)
      }
    },

    async fetchMyProfile () {
      try {
        const res = await api.get('/api/secure/blog/my-profile')
        // myProfile 상태가 없다면 state에 추가해야 함!
        this.myProfile = res.data
        return res.data
      } catch (error) {
        console.error('내 프로필 정보 가져오기 실패:', error)
        throw error
      }
    },

    async updateMyProfile (profileData) {
      try {
        const res = await api.put('/api/secure/blog/my-profile', profileData)
        // 업데이트된 정보로 상태 갱신
        this.myProfile = res.data
        return res.data
      } catch (error) {
        console.error('내 프로필 정보 업데이트 실패:', error)
        throw error
      }
    },

    async addCategory (categoryData, blogUrlName) {
      try {
        console.log('카테고리 추가 요청 데이터 seq :', categoryData.seq); // 요청 전 데이터 확인
        // 카테고리 추가 API 호출
        const res = await api.post('/api/secure/blog/categories', {
          name: categoryData.name,
          seq: categoryData.seq,
          parentId: categoryData.parentId || null,
        });

        // 성공하면 카테고리 목록 다시 불러오기
        await this.fetchBlogCategories(blogUrlName);

        return res.data;
      } catch (error) {
        console.error('카테고리 추가 실패:', error);
        throw error;
      }
    },
    // blog.js의 updateCategoriesOrder 액션 수정
    async updateCategoriesOrder (orderedCategories, blogUrlName) {
      try {

        if (!blogUrlName) {
          throw new Error('블로그 URL이 설정되지 않았습니다');
        }
        console.log('업데이트 카테고리 데이터 : ', orderedCategories);
        // 백엔드 API 호출
        const res = await api.put('/api/secure/blog/categories', orderedCategories);

        // 서버에서 순서 업데이트 성공 후, 최신 카테고리 목록을 다시 불러와 Pinia 상태를 최신화
        await this.fetchBlogCategories(blogUrlName);

        return res.data;
      } catch (error) {
        console.error('카테고리 순서 업데이트 실패:', error);
        throw error;
      }
    },

  },
})
