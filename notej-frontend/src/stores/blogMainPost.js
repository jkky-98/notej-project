import { defineStore } from 'pinia';
import api from '@/utils/axios-interceptor';

export const useBlogPostStore = defineStore('blogPost', {
  state: () => ({
    posts: [],
    page: 1,
    limit: 10,
    isLoading: false,
    hasMore: true,
    error: null,
    thumbnailByteCache: {}, // 썸네일 바이트 데이터 캐시 (Blob URL 아님!)
    filters: {
      search: null,
      categoryName: null,
      tagName: null,
      sortBy: 'latest',
    },
  }),

  // Getter는 특별히 필요 없지만, 상태 직접 접근으로도 가능

  actions: {
    setBlogUrl (url) {
      this.blogUrl = url;
    },

    resetState () {
      console.log('[Pinia] 상태 초기화');
      this.posts = [];
      this.page = 1;
      this.hasMore = true;
      this.error = null;
      this.thumbnailByteCache = {};
      // 필터는 초기화 X
    },

    setFilters (filters) {
      console.log('[Pinia:setFilters] 필터 업데이트 요청. 이전 필터:', this.filters);
      // 새로운 필터 값으로 기존 필터를 업데이트
      this.filters = { ...this.filters, ...filters };
      console.log('[Pinia:setFilters] 필터 업데이트 완료. 새 필터:', this.filters);

      // 필터가 변경되었으므로 게시물 목록 및 관련 상태를 초기화합니다.
      // 이렇게 해야 새로운 필터 기준으로 첫 페이지부터 다시 로드할 준비가 됩니다.
      this.resetPosts(); // <-- 이 부분이 핵심! (PostList에서 resetState() 대신 이걸 호출)
    },

    resetPosts () {
      console.log('[Pinia:resetPosts] 포스트 목록 및 페이징 상태 초기화');
      this.posts = [];
      this.page = 1;
      this.hasMore = true;
      this.error = null;
      this.thumbnailByteCache = {}; // 필요하다면 썸네일 캐시도 초기화
    },


    async loadMorePosts (params = {}) {
      console.log('[Pinia:loadMorePosts] 호출됨');
      if (this.isLoading) { // 이미 로딩 중인 경우
        console.log('[Pinia:loadMorePosts] 이미 로딩 중. 새로운 요청 무시.');
        return { success: false };
      }
      if (!this.hasMore) { // 더 불러올 데이터가 없는 경우
        console.log('[Pinia:loadMorePosts] 더 이상 불러올 데이터 없음. 종료.');
        return { success: false };
      }

      this.isLoading = true;
      this.error = null; // 새로운 요청 전에 에러 상태 초기화

      try {
        const requestParams = {
          page: this.page,
          limit: this.limit,
          ...this.filters,
          ...params,
        };
        // null, undefined, 빈 문자열 값은 제거
        Object.keys(requestParams).forEach(key => {
          if (requestParams[key] === null || requestParams[key] === undefined || requestParams[key] === '') {
            delete requestParams[key];
          }
        });

        console.log('[Pinia:loadMorePosts] API 요청 파라미터:', requestParams);
        console.log('[Pinia:loadMorePosts] API 요청 await 직전: /api/testurl/posts');

        const response = await api.get('/api/testurl/posts', { params: requestParams });
        console.log('[Pinia:loadMorePosts] API 응답 받음 (HTTP Status:', response.status, ')');
        const data = response.data;
        console.log('[Pinia:loadMorePosts] API 응답 데이터 (콘텐츠 길이:', data.content?.length, ')');
        console.log('[Pinia:loadMorePosts] API 응답 데이터 세부: ', data);

        if (data.content && data.content.length > 0) {
          console.log('[Pinia:loadMorePosts] 새로운 포스트 추가:', data.content.length, '개');
          // 배열을 직접 교체하지 않고 push
          this.posts.push(...data.content);
          this.page++;

          if (!data.hasNext && data.content.length < this.limit) { // `data.content.length < this.limit`만으로도 충분할 수 있지만 안전하게 `hasNext` 체크
            this.hasMore = false;
            console.log('[Pinia:loadMorePosts] 더 이상 불러올 데이터 없음. hasMore = false');
          }
        } else {
          this.hasMore = false;
          console.log('[Pinia:loadMorePosts] 응답에 content가 없거나 비어있음. hasMore = false');
        }

        console.log('[Pinia:loadMorePosts] 요청 성공적으로 완료.');
        return { success: true, hasMore: this.hasMore };
      } catch (err) {
        console.error('[Pinia:loadMorePosts] API 요청 중 에러 발생:', err.response?.data || err.message || err);
        this.error = err.response?.data?.message || err.message || '데이터를 불러오는 중 오류가 발생했습니다';
        return { success: false, error: this.error };
      } finally {
        this.isLoading = false;
        console.log('[Pinia:loadMorePosts] 로딩 상태 finally 종료. isLoading = false');
      }
    },

    // 썸네일 바이트 데이터 로드 및 Blob URL 생성 후 반환
    async fetchThumbnailBytes (thumbnailUrl) {
      console.log(`[Pinia:fetchThumbnailBytes] 호출됨: ${thumbnailUrl}`);
      if (!thumbnailUrl) {
        console.log('[Pinia:fetchThumbnailBytes] thumbnailUrl이 없음, null 반환');
        return null;
      }

      // 이미 캐시된 바이트 데이터가 있는지 확인
      if (this.thumbnailByteCache[thumbnailUrl]) {
        console.log('[Pinia:fetchThumbnailBytes] 썸네일 바이트 데이터 캐시 히트! Blob URL 재생성.');
        const blob = new Blob([this.thumbnailByteCache[thumbnailUrl]], { type: 'image/jpeg' });
        return URL.createObjectURL(blob);
      }

      try {
        console.log('[Pinia:fetchThumbnailBytes] 썸네일 API 요청 시작');
        const response = await api.get('/api/post/thumbnail', {
          params: { filename: thumbnailUrl },
          responseType: 'arraybuffer', // 바이너리 데이터로 받음
        });
        console.log('[Pinia:fetchThumbnailBytes] 썸네일 API 응답 받음, 데이터 길이:', response.data.byteLength);

        const thumbnailBytes = new Uint8Array(response.data);
        // 캐시에 바이트 데이터 저장
        this.thumbnailByteCache[thumbnailUrl] = thumbnailBytes;

        // Blob 생성 및 URL 반환
        const blob = new Blob([thumbnailBytes], { type: 'image/jpeg' });
        const url = URL.createObjectURL(blob);
        console.log('[Pinia:fetchThumbnailBytes] Blob URL 생성됨:', url);
        return url;
      } catch (err) {
        console.error('[Pinia:fetchThumbnailBytes] 썸네일 로드 실패:', err.response?.data || err.message || err);
        return null;
      }
    },
  },
});
