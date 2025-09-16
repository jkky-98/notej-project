<!-- WritePost.vue -->
<template>
  <v-container>
    <v-row>
      <v-col class="pb-0 pt-0" cols="12">
        <v-text-field
          v-model="title"
          class="title-input"
          label="제목을 입력하세요"
          variant="underlined"
        />
      </v-col>
    </v-row>

    <!-- 태그 입력 필드 (네가 준 템플릿 그대로 사용) -->
    <v-row>
      <v-col class="pb-0 pt-0" cols="12">
        <v-label class="mb-2">태그</v-label> <!-- Vuetify 스타일 라벨 -->
        <div
          ref="tagifyEl"
          class="tagify-container-vuetify"
          :class="{ 'tagify-dark-theme': themeStore.isDark, 'tagify-light-theme': !themeStore.isDark }"
        >
          <input
            ref="tagInput"
            v-model="tagsInput"
            placeholder="태그를 입력하고 엔터를 누르세요"
          >
        </div>
      </v-col>
    </v-row>

    <!-- 에디터 부분 (나머지 코드 그대로) -->
    <v-row>
      <v-col cols="12">
        <v-card class="editor-content-wrapper" :class="{'editor-dark-theme': themeStore.isDark, 'editor-light-theme': !themeStore.isDark}">
          <v-card-text class="pa-0">
            <div ref="toastEditor" class="tui-editor-container" />
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- 버튼 및 스낵바 (나머지 코드 그대로) -->
    <div class="d-flex justify-end mt-4">
      <v-btn class="mr-2" color="grey" @click="saveTemporary">임시저장</v-btn>
      <v-btn color="primary" @click="openPublishModal">발행하기</v-btn>
    </div>

    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="snackbar.timeout"
    >
      {{ snackbar.text }}
      <template #actions>
        <v-btn color="white" variant="text" @click="snackbar.show = false">닫기</v-btn>
      </template>
    </v-snackbar>
  </v-container>
  <PublishModal v-if="postStore.isPublishModalOpen" />
</template>
<script setup>
  import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { usePostStore } from '@/stores/post';
  import { useThemeStore } from '@/stores/theme';
  import PublishModal from '@/components/PublishModal.vue';
  import Editor from '@toast-ui/editor';
  import '@toast-ui/editor/dist/toastui-editor.css';
  import '@toast-ui/editor/dist/theme/toastui-editor-dark.css';

  // Tagify 임포트
  import Tagify from '@yaireo/tagify';
  import '@yaireo/tagify/dist/tagify.css';

  const route = useRoute();
  const router = useRouter();
  const postStore = usePostStore();
  const themeStore = useThemeStore();

  // 상태 관리
  const title = ref('');
  const editor = ref(null);
  const toastEditor = ref(null);
  const autoSaveTimer = ref(null);
  const currentPostId = ref(route.query.id || null);


  // 태그 관련 상태
  const tagsInput = ref('');
  const tags = ref([]);
  const tagInput = ref(null);
  const tagifyEl = ref(null);
  const tagifyInstance = ref(null);

  // 스낵바 상태
  const snackbar = ref({
    show: false,
    text: '',
    color: 'success',
    timeout: 3000,
  });

  // TUI 에디터 초기화 함수
  const initializeTuiEditor = (initialContent = '') => {
    if (editor.value) {
      editor.value.destroy();
      editor.value = null;
    }

    const tuiTheme = themeStore.isDark ? 'dark' : 'light';

    editor.value = new Editor({
      el: toastEditor.value,
      height: 'auto',
      initialEditType: 'markdown',
      previewStyle: 'vertical',
      initialValue: initialContent,
      theme: tuiTheme,
      placeholder: '내용을 입력하세요.',
      hooks: {
        addImageBlobHook: async (blob, callback) => {
          try {
            const formData = new FormData();
            formData.append('image', blob);

            // 여기서 직접 axios 대신 Pinia 스토어를 통해 API 호출
            const response = await postStore.uploadImage(formData);

            // 스토어에서 반환된 URL 사용
            const fileKey = response.url;
            const domain = import.meta.env.VITE_BACKEND_URL;
            const callbackFetchUrl = domain + '/api/editor/image?filename='+fileKey;
            // TUI 에디터 콜백으로 URL 전달
            callback(callbackFetchUrl, '이미지 설명');
            showToast('success', '이미지가 성공적으로 업로드되었습니다!');
          } catch (error) {
            console.error('이미지 업로드 실패:', error);
            showToast('error', '이미지 업로드에 실패했습니다!');
          }
        },
      },
      events: {
        change: () => {
          if (!postStore.isPublic.value) {
            resetAutoSaveTimer();
          }
        },
      },
    });
  };

  // Tagify 초기화 함수
  const initializeTagify = (initialTags = []) => {
    if (tagifyInstance.value) {
      tagifyInstance.value.destroy();
    }

    nextTick(() => {
      if (!tagInput.value) {
        console.warn('Tagify를 마운트할 DOM <input> 요소가 없습니다.');
        return;
      }

      tagifyInstance.value = new Tagify(tagInput.value, {
        maxTags: 10,
        backspace: true,
        placeholder: '태그를 입력하고 엔터를 누르세요',
        dropdown: {
          enabled: 0,
        },
      });

      tagifyInstance.value.on('add', onTagsChange);
      tagifyInstance.value.on('remove', onTagsChange);

      if (initialTags.length > 0) {
        tagifyInstance.value.addTags(initialTags);
        tags.value = initialTags;
      } else {
        tags.value = [];
      }
    });
  };

  // 태그 변경 시 처리 함수
  const onTagsChange = () => {
    tags.value = tagifyInstance.value.value.map(tag => tag.value);
    console.log('현재 태그:', tags.value);
  };

  // 컴포넌트 마운트 시 에디터와 Tagify 초기화
  onMounted(async () => {
    themeStore.setThemeFromStorage();
    await nextTick();

    if (!currentPostId.value) {
      postStore.startNewPost();
    }

    if (currentPostId.value) {
      try {
        const post = await loadPostData(currentPostId.value);
        initializeTuiEditor(post.content || '');
        initializeTagify(post.tags || []);
      } catch (error) {
        console.error('글 불러오기 실패:', error);
        showToast('error', '글을 불러오지 못했습니다. 새 글을 작성합니다.');
        currentPostId.value = null;
        initializeTuiEditor('');
        initializeTagify([]);
        startAutoSaveTimer();
      }
    } else {
      initializeTuiEditor('');
      initializeTagify([]);
      startAutoSaveTimer();
    }
  });

  // 테마 변경 감지 및 에디터/Tagify 테마 업데이트
  watch(() => themeStore.isDark, newIsDark => {
    const currentContent = editor.value?.getMarkdown() || '';
    const currentTags = tags.value;

    initializeTuiEditor(currentContent);
    initializeTagify(currentTags);
  }, { immediate: false });

  // 컴포넌트 언마운트 시 정리
  onBeforeUnmount(() => {
    stopAutoSaveTimer();
    if (editor.value) {
      editor.value.destroy();
    }
    if (tagifyInstance.value) {
      tagifyInstance.value.destroy();
    }
  });

  // 자동 저장 관련 로직
  const AUTO_SAVE_INTERVAL_MS = 60000; // 1분

  const startAutoSaveTimer = () => {
    if (autoSaveTimer.value) {
      clearInterval(autoSaveTimer.value);
    }
    if (!postStore.isPublic.value) {
      autoSaveTimer.value = setInterval(autoSavePost, AUTO_SAVE_INTERVAL_MS);
      console.log('자동저장 타이머 시작');
    }
  };

  const resetAutoSaveTimer = () => {
    if (autoSaveTimer.value) {
      clearInterval(autoSaveTimer.value);
    }
    if (!postStore.isPublic.value) {
      autoSaveTimer.value = setInterval(autoSavePost, AUTO_SAVE_INTERVAL_MS);
    }
  };

  const stopAutoSaveTimer = () => {
    if (autoSaveTimer.value) {
      clearInterval(autoSaveTimer.value);
      autoSaveTimer.value = null;
      console.log('자동저장 타이머 중지');
    }
  };

  const autoSavePost = async () => {
    if (postStore.isPublic.value) {
      console.log('Post가 공개 상태이므로 자동 저장을 건너뜀');
      return;
    }

    const content = editor.value?.getMarkdown() || '';

    if (!title.value.trim() && !content.trim() && tags.value.length === 0) {
      console.log('제목, 내용, 태그가 없어 자동저장 건너뜀');
      return;
    }

    try {
      const result = await postStore.savePost({
        id: currentPostId.value,
        title: title.value,
        content,
        tags: tags.value, // 태그 정보도 함께 저장
        isPublic: false,
      });
      console.log('저장 결과 : ', result);
      if (!currentPostId.value && result) {
        currentPostId.value = result;
        router.replace({ query: { id: currentPostId.value } });
        showToast('success', '글이 자동저장되었습니다!');
      } else if (currentPostId.value) {
        showToast('success', '글이 자동저장되었습니다!');
      }
    } catch (error) {
      console.error('자동저장 실패:', error);
      showToast('error', '자동저장 실패!');
    }
  };

  // 글 데이터 로드 함수
  const loadPostData = async postId => {
    try {
      const post = await postStore.getPost(postId);

      title.value = post.title || '';
      postStore.isPublic.value = postStore.isPublic || false;

      if (!postStore.isPublic.value) {
        startAutoSaveTimer();
      } else {
        console.log('로드된 Post는 공개 상태이므로 자동 저장 타이머를 시작하지 않습니다.');
      }
      return post;
    } catch (error) {
      console.error('글 데이터 로드 실패:', error);
      throw error;
    }
  };

  // 임시저장 함수
  const saveTemporary = async () => {
    const content = editor.value?.getMarkdown() || '';

    if (!title.value.trim() && !content.trim() && tags.value.length === 0) {
      showToast('warning', '제목이나 내용을 입력해주세요.');
      return;
    }

    try {
      const result = await postStore.savePost({
        id: currentPostId.value,
        title: title.value,
        content,
        tags: tags.value, // 태그 정보도 함께 저장
        isPublic: false,
      });

      if (!currentPostId.value && result.id) {
        currentPostId.value = result.id;
        router.replace({ query: { id: currentPostId.value } });
      }
      showToast('success', '임시저장 완료!');
    } catch (error) {
      console.error('임시저장 실패:', error);
      showToast('error', '임시저장 실패!');
    }
  };

  // 발행 함수
  const openPublishModal = async () => {
    const content = editor.value?.getMarkdown() || '';

    if (!title.value.trim()) {
      showToast('warning', '제목을 입력해주세요!');
      return;
    }

    if (!content.trim()) {
      showToast('warning', '내용을 입력해주세요!');
      return;
    }

    // pinia에 데이터 업데이트
    postStore.setPostDraft({
      id: currentPostId.value,
      title: title.value,
      content,
      tags: tags.value,
      isPublic: false,
    });

    postStore.openPublishModal();

  };

  // 토스트 메시지 표시 함수
  const showToast = (type, message) => {
    snackbar.value.text = message;
    snackbar.value.color = type === 'success' ? 'success' : type === 'error' ? 'error' : 'warning';
    snackbar.value.show = true;
  };
</script>


<style scoped>
.title-input {
  font-size: 1.8rem;
  font-weight: 500;
  margin-bottom: 20px;
}

/* 토스트 UI 에디터 커스텀 스타일 */
:deep(.toastui-editor-defaultUI) {
  border: 1px solid #e0e0e0;
  border-radius: 4px;
}

:deep(.toastui-editor-toolbar) {
  border-bottom: 1px solid #e0e0e0;
}

/* 자동 저장 알림용 토스트 메시지 */
.toast-message {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 12px 20px;
  border-radius: 4px;
  z-index: 9999;
}

/* 제목 입력 필드 스타일 */

.title-input :deep(.v-field) {
  min-height: 72px; /* v-text-field 전체의 최소 높이 설정 (조금 더 웅장하게) */
  padding-top: 20px; /* 상단 패딩 추가하여 텍스트 필드 영역 확장 */
}

.title-input :deep(.v-field__input) {
  font-size: 2.2rem !important; /* 글자 크기 더 키움 */
  line-height: 1.2 !important;  /* 줄 간격 조절 */
  padding-bottom: 10px; /* 입력 필드 자체의 하단 패딩 조절 */
}

.title-input :deep(.v-label.v-field-label) {
  font-size: 1.2rem !important; /* 라벨 글자 크기도 조절 */
  line-height: 1.5 !important;
  top: 18px; /* 라벨 위치 조절 (padding-top 때문에 내려왔을 수 있음) */
}
/* Tagify Vuetify 스타일 (div 상자 자체) */
.tagify-container-vuetify {
  min-height: 56px;
  width: 100%;
  border: 1px solid rgb(var(--v-theme-secondary));
  border-radius: 8px;
  background-color: rgb(var(--v-theme-surface));
  color: rgb(var(--v-theme-on-surface));
  padding: 8px 12px;
  margin-bottom: 20px;
  transition: border-color 0.2s, box-shadow 0.2s;
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;

  /* Tagify 내부 CSS 변수 기본 설정 (라이트 모드 기준) */
  --tags-border-color: rgb(var(--v-theme-secondary));
  --tags-hover-border-color: rgb(var(--v-theme-secondary));
  --tags-focus-border-color: rgb(var(--v-theme-primary));
  --tag-border-radius: 8px;
  --tag-bg: rgb(var(--v-theme-primary));
  --tag-text-color: rgb(var(--v-theme-text));
  --tag-remove-btn-color: rgb(var(--v-theme-text));
  --tag-remove-btn-hover-color: rgb(var(--v-theme-error));
  --tags-disabled-bg: rgb(var(--v-theme-surface));
  --tags-placeholder-color: rgb(var(--v-theme-secondary));
}

/* Tagify 컨테이너 포커스 시 스타일 */
.tagify-container-vuetify.tagify--focus {
  border-color: rgb(var(--v-theme-primary));
  box-shadow: 0 0 0 1px rgb(var(--v-theme-primary));
}

/* Tagify의 최상위 컨테이너 (라이브러리가 생성하는 부분) */
.tagify-container-vuetify :deep(.tagify) {
  width: 100%;
  height: 100%;
  border: none;
  background: none;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

/* Tagify의 입력 필드 */
.tagify-container-vuetify :deep(.tagify__input) {
  flex-grow: 1;
  min-width: 50px;
  padding: 0 !important;
  height: 24px;
  margin: 2px 0;
  color: var(--tag-text-color);
  background: none !important;
  font-size: 1rem;
  font-family: 'Pretendard Variable, sans-serif';
}

/* 태그 스타일 */
.tagify-container-vuetify :deep(.tagify__tag) {
  background-color: var(--tag-bg);
  color: var(--tag-text-color);
  border-radius: var(--tag-border-radius);
  padding: 4px 10px;
  margin: 2px 4px 2px 0;
  font-size: 0.875rem;
  transition: background-color 0.2s, color 0.2s;
}

/* 태그 제거 버튼 */
.tagify-container-vuetify :deep(.tagify__tag__removeBtn) {
  color: var(--tag-remove-btn-color);
  opacity: 0.7;
  transition: color 0.2s, opacity 0.2s;
}
.tagify-container-vuetify :deep(.tagify__tag__removeBtn):hover {
  color: var(--tag-remove-btn-hover-color);
}

/* 플레이스홀더 */
.tagify-container-vuetify :deep(.tagify__input::placeholder) {
  color: var(--tags-placeholder-color);
  opacity: 0.7;
  transition: color 0.2s;
}

/* 다크 테마 Tagify 컨테이너 스타일 */
.tagify-dark-theme.tagify-container-vuetify {
  background-color: rgb(var(--v-theme-background));
  border-color: rgb(var(--v-theme-secondary));
  color: rgb(var(--v-theme-on-surface));

  /* !!! 다크 테마일 때 Tagify 내부 CSS 변수 오버라이드 !!! */
  --tags-border-color: rgb(var(--v-theme-secondary));
  --tags-hover-border-color: rgb(var(--v-theme-secondary));
  --tags-focus-border-color: rgb(var(--v-theme-primary));
  --tag-bg: rgb(var(--v-theme-primary));
  --tag-text-color: rgb(var(--v-theme-text));
  --tag-remove-btn-color: rgb(var(--v-theme-info));
  --tags-disabled-bg: rgb(var(--v-theme-surface));
  --tags-placeholder-color: rgb(var(--v-theme-secondary));
}
</style>
