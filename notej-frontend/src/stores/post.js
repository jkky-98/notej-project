// src/stores/post.js
import { defineStore } from 'pinia';
import api from '@/utils/axios-interceptor'


export const usePostStore = defineStore('post', {
  state: () => ({
    currentPost: null,
    isLoading: false,
    error: null,
    // 모달 이전 데이터
    post : {
      id: null,
      title: '',
      content: '',
      tags: [],
      isPublic: false,
    },
    // 발행 모달 관련 상태 추가
    isPublishModalOpen: false,
    thumbnailUrl: '',
    displayedThumbnailUrl: null, // v-img가 사용할 최종 Base64 이미지 URL
    isPublic: true,
    selectedCategory: null, // 수정 프로세스에선 이게 차있을 수 있음.
    shortDescription: '',
    categories: [],

  }),
  getters: {
    hasThumbnailToDisplay: state => !!state.displayedThumbnailUrl,
    // 태그 목록 가져오기
    getTags: state => state.post.tags,
    // 태그 개수
    tagCount: state => state.post.tags.length,
  },

  actions: {
    // 태그 추가 액션
    addTag (tag) {
      // 이미 존재하는 태그인지 확인
      if (!this.post.tags.includes(tag)) {
        this.post.tags.push(tag);
        console.log('태그 추가됨:', tag);
      }
    },

    // 태그 제거 액션
    removeTag (tag) {
      const index = this.post.tags.indexOf(tag);
      if (index !== -1) {
        this.post.tags.splice(index, 1);
        console.log('태그 제거됨:', tag);
      }
    },
    // 태그 전체 설정 (덮어쓰기)
    setTags (tags) {
      this.post.tags = [...tags];
    },

    // 태그 전체 초기화
    clearTags () {
      this.post.tags = [];
    },
    // 새 글 작성 시작할 때 호출할 액션
    resetPublishModalState () {
      this.thumbnailUrl = '';
      this.displayedThumbnailUrl = null;
      this.isPublic = true;
      this.selectedCategory = null;
      this.shortDescription = '';
    },
    // Write.vue에서 새 글 작성 시작할 때 호출
    startNewPost () {
    // 새로운 포스트를 시작할 때 post를 빈 객체로 초기화 (null 아님!)
      this.post = {
        id: null, // 새로운 포스트는 ID가 없겠지
        title: '', // 제목도 비어있고
        content: '', // 내용도 비어있고
        tags: [], // 태그 배열도 비어있어야지
        isPublic: false,
      // 만약 다른 기본값이 있다면 여기에 추가
      };
      this.resetPublishModalState(); // 이건 원래대로
    },

    async startEditPost (postId) {
      this.isLoading = true;
      this.error = null;

      let response;
      try {
        response = await api.get(`/api/secure/post/${postId}`);
        // 데이터 배치
        this.post.id = response.id;
        this.post.title = response.title;
        this.post.content = response.content;
        this.post.tags = response.tags;
        this.post.isPublic = response.active;
        this.selectedCategory = response.categoryName;
        this.thumbnailUrl = response.thumbnailUrl;
        this.shortDescription = response.bio;
      } catch (error) {
        this.error = error.message || '작성 게시글 데이터 불러오기중 오류 발생'
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async savePost (postData) {
      this.isLoading = true;
      this.error = null;

      try {
        let response;

        console.log('게시글 삽입 or 업데이트 요청 데이터', postData);
        if (postData.id) {
          // 수정 (PUT 요청)
          response = await api.put(`/api/secure/post/${postData.id}`, postData);
        } else {
          // 새 글 작성 (POST 요청)
          response = await api.post('/api/secure/post', postData);
        }

        this.currentPost = response.data;
        console.log('성공적으로 게시글을 등록했습니다 등록한 게시물 ID : ', this.currentPost)
        return response.data;
      } catch (error) {
        this.error = error.message || '저장 중 오류가 발생했습니다';
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async getPost (postId) {
      this.isLoading = true;
      this.error = null;

      try {
        const response = await api.get(`/api/posts/${postId}`);
        this.currentPost = response.data;
        return response.data;
      } catch (error) {
        this.error = error.message || '글을 불러오는 중 오류가 발생했습니다';
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async getCategories () {
      this.isLoading = true;
      this.error = null;

      try {
        const response = await api.get('/api/secure/category');
        console.log('가져온 카테고리 데이터 : {}', response.data);
        this.categories = response.data.categoryAll;
        console.log('Pinia stores에 저장된 categories:', this.categories); // 잘 저장되었는지 확인
        return response.data;
      } catch (error) {
        this.error = error.message || '전체 카테고리 호출 중 오류가 발생했습니다';
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    // 이미지 업로드 액션 추가
    async uploadImage (formData) {
      this.isLoading = true;
      this.error = null;

      try {
        const response = await api.post('/api/secure/editor/image', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        });

        return response.data; // 이미지 URL이 포함된 응답 반환
      } catch (error) {
        this.error = error.message || '이미지 업로드 중 오류가 발생했습니다';
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    // 썸네일 파일 업로드 (컴포넌트에서 호출)
    async uploadThumbnail (formData) {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await api.post('/api/secure/post/thumbnail', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        });

        // S3 키만 저장
        this.thumbnailKey = response.data.url; // 응답 데이터의 키가 's3Key'라고 가정
        console.log('uploadThumbnail 성공 : 도착 데이터 - ', response.data)
        console.log('업로드된 썸네일 키:', this.thumbnailKey);

        // ★★★ S3 키를 받았으면, 이제 해당 키로 이미지를 가져와서 displayedThumbnailUrl 업데이트
        await this.fetchAndSetDisplayedThumbnail();

        return response.data;
      } catch (error) {
        this.error = error.message || '썸네일 업로드 중 오류가 발생하였습니다';
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    // 썸네일 바이트 데이터를 가져와서 displayedThumbnailUrl을 설정하는 액션
    async fetchAndSetDisplayedThumbnail () {
      console.log('fetchAndSetDisplayedThumbnail 실행');
      if (!this.thumbnailKey) {
        this.displayedThumbnailUrl = null; // 키가 없으면 초기화
        return;
      }

      this.error = null;
      try {
        const response = await api.get(`/api/secure/post/thumbnail`, {
          params: {
            filename: this.thumbnailKey,
          },
          responseType: 'arraybuffer', // 바이트 데이터 요청
        });

        // 바이트 데이터를 Base64 URL로 변환 후 저장
        this.displayedThumbnailUrl = this.convertBytesToImageUrl(response.data);
      } catch (error) {
        this.error = error.message || '썸네일 이미지 불러오기 실패';
        this.displayedThumbnailUrl = null; // 실패 시 초기화
        console.error('썸네일 이미지 불러오기 실패:', error);
        throw error; // 에러를 다시 던져서 컴포넌트에서도 처리 가능하게
      }
    },

    // 바이트 데이터를 Data URL (Base64)로 변환하는 헬퍼 함수
    convertBytesToImageUrl (byteData) {
      if (!byteData) return null;
      const bytes = new Uint8Array(byteData);
      let binary = '';
      for (let i = 0; i < bytes.byteLength; i++) {
        binary += String.fromCharCode(bytes[i]);
      }
      const base64 = btoa(binary);
      // 백엔드에서 이미지 타입 정보도 같이 내려주면 더 좋음. 일단 jpeg로 가정
      return `data:image/jpeg;base64,${base64}`;
    },

    // 발행 모달 관련 액션들 추가
    // 임시 저장용 draft 설정 함수 추가
    setPostDraft (draftData) {
      console.log('Pinia에 저장되는 postDraft 데이터:', draftData);
      this.postDraft = { ...draftData };

      // 디버깅을 위해 저장 후 상태도 확인
      console.log('저장 후 Pinia postDraft 상태:', this.postDraft);
    },

    openPublishModal () {
      this.isPublishModalOpen = true;
    },

    closePublishModal () {
      this.isPublishModalOpen = false;
    },

    setThumbnail (url) {
      this.thumbnailUrl = url;
    },

    setPublishSettings ({ isPublic, categoryId }) {
      this.isPublic = isPublic;
      this.selectedCategory = categoryId;
    },

    // 발행하기 최종 액션
    async publishPost () { // ★ postContent, title 인자 제거!
      this.isLoading = true;
      this.error = null;

      // postDraft 데이터가 없으면 발행 불가 (유효성 검사는 컴포넌트에서 했지만, 여기서도 한 번 더 체크)
      if (!this.post) {
        this.error = '발행할 게시글 정보가 없습니다.';
        this.isLoading = false;
        throw new Error('발행할 게시글 정보가 없습니다.');
      }

      try {
        const postData = {
          // postDraft에서 핵심 데이터 가져오기
          id: this.post.id,
          title: this.post.title,
          content: this.post.content,
          tags: this.post.tags, // draft에 저장된 태그도 함께

          // 모달에서 설정된 추가 정보
          thumbnailUrl: this.thumbnailUrl,
          isPublic: this.isPublic,
          categoryId: this.selectedCategory,
          shortDescription: this.shortDescription,
        };

        // savePost 액션 재사용
        const result = await this.savePost(postData); // postData 통째로 넘김
        this.closePublishModal();
        return result;
      } catch (error) {
        this.error = error.message || '발행 중 오류가 발생했습니다';
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
  },
});
