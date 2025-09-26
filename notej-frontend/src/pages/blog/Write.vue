<!-- WritePost.vue -->
<template>
  <v-container>
    <v-row>
      <v-col class="pb-0 pt-12" cols="12">
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
      <v-col>
        <v-card class="tag-card" elevation="2" rounded="lg">
          <v-card-text class="py-3">
            <div ref="tagifyEl" class="tagify-container rounded-lg">
              <input
                ref="tagInput"
                class="custom-tagify-input"
                placeholder="태그를 입력하고 엔터를 누르세요"
              >
            </div>
          </v-card-text>
        </v-card>
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
      <v-btn class="mr-2" color="grey" @click="autoSavePost">임시저장</v-btn>
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
  import { onBeforeUnmount, onMounted, ref } from 'vue';
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
  const currentPostId = ref(route.query.id || null);

  // 태그 관련 상태
  const tagifyEl = ref(null); // tagify 초기 생성을 위한 인자
  const tagInput = ref(null); // tagify 객체에 대한 데이터 담기는 곳
  const tagify = ref(null); // tagify 객체 (태그 생성 , 삭제 랜더링 담당)
  // 스낵바 상태
  const snackbar = ref({
    show: false,
    text: '',
    color: 'success',
    timeout: 3000,
  });
  // 자동저장 관련
  const autoSaveInterval = ref(null);
  const AUTOSAVE_DELAY = 60000; // 1분마다 자동저장

  // TUI 에디터 초기화 함수
  const initializeTuiEditor = (initialContent = '') => {
    console.log('TUI 에디터 초기화');
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
            console.log('');
          }
        },
      },
    });
  };

  // 컴포넌트 마운트 시 에디터와 Tagify 초기화
  onMounted(async () => {
    console.log('마운트 시작');
    console.log('current Post id : ', currentPostId.value);
    if (!currentPostId.value) {
      await postStore.startNewPost(); // 여기서 await 추가
      initializeTuiEditor('');
      postStore.clearTags();
      const input = document.querySelector('.custom-tagify-input')
      if (tagifyEl.value) {
        tagify.value = new Tagify(input, {
          maxTags: 10,
          editTags: false,
          backspace: true,
          placeholder: '태그를 입력하고 엔터를 누르세요',
          dropdown: { enabled: 0 },
          callbacks: {
            add: onTagAdd,
            remove: onTagRemove,
          },
        });

        if (postStore.getTags.length > 0) {
          tagify.value.addTags(postStore.getTags);
        }
      }
      // 자동저장 시작
      startAutoSave();
    } else {
      await postStore.startEditPost(currentPostId.value);
      initializeTuiEditor(postStore.post.content);
      title.value = postStore.post.title;
      const input = document.querySelector('.custom-tagify-input')
      if (tagifyEl.value) {
        tagify.value = new Tagify(input, {
          maxTags: 10,
          editTags: false,
          backspace: true,
          placeholder: '태그를 입력하고 엔터를 누르세요',
          dropdown: { enabled: 0 },
          callbacks: {
            add: onTagAdd,
            remove: onTagRemove,
          },
        });

        if (!postStore.getTags || postStore.getTags.length > 0) {
          tagify.value.addTags(postStore.getTags);
        }
      }

      // 자동저장 시작
      startAutoSave();
    }
  });
  // 태그 추가 이벤트 핸들러
  const onTagAdd = e => {
    const tagData = e.detail.data;
    // Pinia 스토어에 태그 추가
    postStore.addTag(tagData.value);
    console.log('태그 추가됨:', postStore.getTags);
  };

  // 태그 제거 이벤트 핸들러
  const onTagRemove = e => {
    const tagData = e.detail.data;

    // 유효한 값인지 확인 후 Pinia 스토어에서 태그 제거
    if (tagData?.value) {
      postStore.removeTag(tagData.value);
    }
    console.log('태그 제거됨:', postStore.getTags);
  };
  // 태그 데이터 가져오기 (Pinia 스토어와 연동 시 사용)
  const getTags = () => {
    return tagify.value ? tagify.value : [];
  };

  // 외부에서 태그 설정하기
  const setTags = tags => {
    if (tagify.value) {
      tagify.value.removeAllTags();
      tagify.value.addTags(tags);
    }
  };

  // 컴포넌트 언마운트 시 정리
  onBeforeUnmount(() => {
    if (editor.value) {
      editor.value.destroy();
    }
    if (tagify.value) {
      tagify.value.destroy();
    }
    // 자동저장 중지
    stopAutoSave();
  });

  const autoSavePost = async () => {
    if (postStore.isPublic.value) {
      console.log('Post가 공개 상태이므로 자동 저장을 건너뜀');
      return;
    }
    const content = editor.value?.getMarkdown() || '';

    if (!title.value.trim() && !content.trim() && length(content.trim()) > 0) {
      console.log('제목, 내용 없어 자동저장 건너뜀');
      return;
    }

    try {
      const result = await postStore.savePost({
        id: currentPostId.value,
        title: title.value,
        content,
        tags: postStore.getTags, // 태그 정보도 함께 저장
        active: postStore.post.isPublic,
        thumbnailUrl: postStore.thumbnailUrl,
        selectedCategory: postStore.selectedCategory,
        bio: postStore.shortDescription,
      });
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
    postStore.post.id = currentPostId;
    postStore.post.title = title.value;
    postStore.post.content = content;
    postStore.post.isPublic = false;

    postStore.openPublishModal();

  };

  // 토스트 메시지 표시 함수
  const showToast = (type, message) => {
    snackbar.value.text = message;
    snackbar.value.color = type === 'success' ? 'success' : type === 'error' ? 'error' : 'warning';
    snackbar.value.show = true;
  };

  // 게시글 자동저장 기능
  // 자동저장 시작
  const startAutoSave = () => {
    // 기존 타이머 정리
    clearInterval(autoSaveInterval.value);

    // 새 타이머 설정 (1분마다)
    autoSaveInterval.value = setInterval(() => {
      autoSavePost();
    }, AUTOSAVE_DELAY);
  };
  // 자동저장 정지
  const stopAutoSave = () => {
    clearInterval(autoSaveInterval.value);
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
.custom-tagify-input {
    --tag-bg                  : #0052BF;
    --tag-hover               : #CE0078;
    --tag-text-color          : #FFF;
    --tags-border-color       : transparent; /* 여기를 수정 */
    --tag-text-color--edit    : #111;
    --tag-remove-bg           : var(--tag-hover);
    --tag-pad                 : .6em 1em;
    --tag-inset-shadow-size   : 1.4em;
    --tag-remove-btn-color    : white;
    --tag-remove-btn-bg--hover: black;

    display: inline-block;
    min-width: 0;
    border: none;
}
</style>
