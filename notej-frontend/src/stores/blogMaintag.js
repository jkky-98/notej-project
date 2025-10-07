// stores/blogMaintag.js
import { defineStore } from 'pinia';
import api from '@/utils/axios-interceptor'; // API 요청을 위한 axios 인스턴스

export const useBlogMaintagStore = defineStore('blogMaintag', {
  state: () => ({
    tags: [],
    isLoading: false,
    error: null,
  }),
  actions: {
    async fetchTags (blogUrlName) {
      if (this.isLoading) {
        console.log('[Pinia:blogMaintag] 태그 로딩 중. 새로운 요청 무시.');
        return;
      }

      this.isLoading = true;
      this.error = null; // 에러 상태 초기화

      try {
        console.log(`[Pinia:blogMaintag] ${blogUrlName} 블로그 태그 가져오기 시작`);
        const response = await api.get(`/api/${blogUrlName}/tags`); // API 엔드포인트 호출
        this.tags = response.data; // 응답으로 받은 태그 데이터 저장
        console.log(`[Pinia:blogMaintag] ${blogUrlName} 블로그 태그 가져오기 성공:`, this.tags);
      } catch (err) {
        this.error = err.response?.data?.message || err.message || '태그를 불러오는 중 오류가 발생했습니다.';
        console.error('[Pinia:blogMaintag] 태그 가져오기 실패:', this.error);
      } finally {
        this.isLoading = false;
      }
    },
    // 태그 상태 초기화가 필요하면 추가
    resetTags () {
      this.tags = [];
      this.error = null;
    },
  },
});
