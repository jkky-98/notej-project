// stores/blogMainPost.js
import { defineStore } from 'pinia'
import api from '@/utils/axios-interceptor' // 기존 axios-interceptor 임포트

export const useBlogPostStore = defineStore('blogPost', {
  state: () => ({
    posts: [],
    page: 1,
    limit: 10,
    isLoading: false,
    hasMore: true,
    error: null,
  }),

  getters: {
    getPosts: state => state.posts,
  },

  actions: {
    // 초기화 함수 (필요시 호출)
    resetPostState () {
      this.posts = [];
      this.page = 1;
      this.hasMore = true;
      this.error = null;
    },

    // 무한 스크롤을 위한 포스트 로딩 함수
    async loadMorePosts (params = {}) {
      // 이미 로딩 중이거나 더 불러올 데이터가 없으면 중단
      if (this.isLoading || !this.hasMore) return;

      try {
        this.isLoading = true;

        // API 요청 파라미터 구성
        const requestParams = {
          page: this.page,
          limit: this.limit,
          ...params, // 추가 파라미터 (카테고리, 정렬 기준 등)
        };

        // axios-interceptor를 통한 API 요청
        const response = await api.get('/api/posts', { params: requestParams });
        const data = response.data;

        // 응답 데이터 처리
        if (data.items && data.items.length > 0) {
          // 새 데이터를 기존 배열에 추가
          this.posts.push(...data.items);

          // 페이지 증가
          this.page++;

          // 더 불러올 데이터가 있는지 확인
          if (data.isLastPage || data.items.length < this.limit) {
            this.hasMore = false;
          }
        } else {
          // 불러올 데이터가 없으면 완료 처리
          this.hasMore = false;
        }

        return {
          success: true,
          hasMore: this.hasMore,
        };
      } catch (error) {
        console.error('포스트 로딩 중 오류 발생:', error);
        this.error = error.message || '데이터를 불러오는 중 오류가 발생했습니다';

        return {
          success: false,
          error: this.error,
        };
      } finally {
        this.isLoading = false;
      }
    },
  },
})
