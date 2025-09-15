// src/stores/post.js
import { defineStore } from 'pinia';
import api from '@/utils/axios-interceptor'

export const usePostStore = defineStore('post', {
  state: () => ({
    currentPost: null,
    isLoading: false,
    error: null,
  }),

  actions: {
    async savePost (postData) {
      this.isLoading = true;
      this.error = null;

      try {
        let response;

        if (postData.id) {
          // 수정 (PUT 요청)
          response = await api.put(`/api/posts/${postData.id}`, postData);
        } else {
          // 새 글 작성 (POST 요청)
          response = await api.post('/api/posts', postData);
        }

        this.currentPost = response.data;
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

    // 이미지 업로드 액션 추가
    async uploadImage (formData) {
      this.isLoading = true;
      this.error = null;

      try {
        const response = await api.post('/api/editor/image', formData, {
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
  },
});
