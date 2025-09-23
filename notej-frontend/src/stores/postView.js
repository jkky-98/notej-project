// stores/PostView.js
import { defineStore } from 'pinia';
import api from '@/utils/axios-interceptor'; // axios-interceptor 경로 확인해줘!
import { useRouter } from 'vue-router'; // 삭제 후 리다이렉션 등 필요 시 사용 가능

export const usePostViewStore = defineStore('postView', {
  state: () => ({
    post: null, // 조회된 게시글 데이터
    loading: false, // 로딩 상태
    error: null, // 에러 메시지
    tableOfContents: [], // 목록 상태 관리
    thumbnailBytes: null,
    // 추후 확장성을 위해 추가될 상태들
    // comments: [],
    // relatedPosts: [],
  }),

  getters: {
    // 게시글 데이터가 있는지 확인
    hasPost: state => !!state.post,
    // 게시글 제목
    getPostTitle: state => state.post?.title || '',
    // 게시글 내용 (TUI Viewer 용)
    getPostContent: state => state.post?.content || '',
    // 게시글 태그 목록
    getPostTags: state => state.post?.tags || [],
    // 게시글 카테고리 이름
    getPostCategoryName: state => state.post?.category?.name || '미분류',
    // 썸네일 URL
    getThumbnailUrl: state => state.post?.thumbnailUrl || null,
    // 작성자 Bio
    getAuthorBio: state => state.post?.bio || '',
    // 조회수 (임시, 백엔드에서 추가되면 변경)
    getPostViews: () => Math.floor(Math.random() * 1000) + 1, // 1~1000 사이 랜덤 값 ㅋ
    // 작성자 UUID (수정/삭제 권한 확인용)
    getAuthorUuid: state => state.post?.authorUuid || null,
    // 목차
    getToc: state => state.tableOfContents,
    getThumbnailSrc: state => {
      if (!state.thumbnailBytes) return '';

      const blob = new Blob([state.thumbnailBytes], { type: 'image/jpeg' });
      return URL.createObjectURL(blob);
    },

    // 썸네일 데이터 존재 여부 확인
    hasThumbnail: state => !!state.thumbnailBytes,
  },

  actions: {
    // 단일 게시글 데이터 불러오기
    async fetchPost (postId) {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.get(`/api/post/${postId}`); // API 엔드포인트 확인!
        this.post = response.data;
        // 게시글 로드 성공 후 썸네일 바이트 데이터 불러오기
        if (this.post?.thumbnailUrl) {
          await this.fetchThumbnailBytes();
        }
        console.log('게시글 불러오기 성공:', this.post); // 디버깅용
      } catch (err) {
        this.error = '게시글을 불러오는데 실패했습니다: ' + (err.response?.data?.message || err.message);
        console.error('게시글 fetch 오류:', err);
        this.post = null; // 에러 발생 시 게시글 데이터 초기화
      } finally {
        this.loading = false;
      }
    },
    // 썸네일 바이트 데이터 불러오기
    async fetchThumbnailBytes () {
      if (!this.post?.thumbnailUrl) return false;

      try {
        const response = await api.get(`/api/post/thumbnail`, {
          params: { filename: this.post.thumbnailUrl },
          responseType: 'arraybuffer', // 바이너리 데이터로 받기
        });

        this.thumbnailBytes = new Uint8Array(response.data);
        return true;
      } catch (err) {
        console.error('썸네일 바이트 데이터 로드 실패:', err);
        this.thumbnailBytes = null;
        return false;
      }
    },

    // 게시글 삭제
    async deletePost (postId) {
      this.loading = true;
      this.error = null;
      const router = useRouter(); // 액션 내에서 useRouter 사용
      try {
        await api.delete(`/api/posts/${postId}`); // API 엔드포인트 확인!
        console.log(`게시글 ${postId} 삭제 성공`);
        // 성공 시 특정 페이지로 리다이렉트 (예: 메인 페이지)
        router.push('/');
        // 삭제 후 상태 초기화
        this.resetPostState();
        return true;
      } catch (err) {
        this.error = '게시글 삭제에 실패했습니다: ' + (err.response?.data?.message || err.message);
        console.error('게시글 delete 오류:', err);
        return false;
      } finally {
        this.loading = false;
      }
    },

    // 게시글 상태 초기화
    resetPostState () {
      this.post = null;
      this.loading = false;
      this.error = null;
      this.tableOfContents = [];
      // 썸네일 데이터 정리
      if (this.thumbnailBytes) {
        // Blob URL이 생성된 경우 해제 (컴포넌트에서 처리해야 함)
        this.thumbnailBytes = null;
      }
    },

    // 목차 데이터 설정
    setTableOfContents (headings) {
      this.tableOfContents = headings;
    },

    // HTML에서 헤딩 추출하여 목차 생성
    extractHeadings (viewerElement) {
      if (!viewerElement) return;

      const headings = viewerElement.querySelectorAll('h1, h2, h3, h4, h5, h6');

      const tempToc = [];
      headings.forEach((heading, index) => {
        const id = heading.id || `heading-${index}-${heading.textContent.slice(0, 10).replace(/[^a-zA-Z0-9-]/g, '')}`;
        heading.id = id;

        tempToc.push({
          id,
          text: heading.textContent,
          level: parseInt(heading.tagName.charAt(1), 10),
        });
      });

      this.tableOfContents = tempToc;
    },
    // (추후) 댓글 추가, 관련 게시글 불러오기 등 확장 가능
  },
});
