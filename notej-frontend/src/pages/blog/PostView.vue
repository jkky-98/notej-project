<!-- views/PostView.vue -->
<template>
  <v-container class="post-view-container my-8">
    <v-row v-if="postViewStore.loading" justify="center">
      <v-col class="text-center" cols="12">
        <v-progress-circular color="primary" indeterminate size="64" />
        <p class="mt-4 text-subtitle-1">게시글 불러오는 중...</p>
      </v-col>
    </v-row>

    <v-row v-else-if="postViewStore.error" justify="center">
      <v-col class="text-center" cols="12">
        <v-alert outlined prominent type="error">{{ postViewStore.error }}</v-alert>
        <v-btn class="mt-4" color="primary" @click="goBack">이전으로</v-btn>
      </v-col>
    </v-row>

    <v-row v-else-if="postViewStore.hasPost" justify="center">
      <v-col cols="12" lg="8" md="10">
        <v-card class="pa-6 pa-md-8" flat>

          <!-- 카테고리 -->
          <v-chip class="mb-4" color="secondary" label>
            {{ postViewStore.getPostCategoryName }}
          </v-chip>

          <!-- 제목 -->
          <v-card-title class="text-h4 text-sm-h3 font-weight-bold pa-0 mb-0 text-wrap custom-line-height">
            {{ postViewStore.getPostTitle }}
          </v-card-title>
          <!-- 메타 정보 (작성자 bio, 조회수 등) -->
          <v-card-subtitle class="text-body-2 text-medium-emphasis pa-0 mb-2">
            <div class="d-flex flex-wrap align-center">
              <v-chip class="ma-2 pa-4 text-wrap rounded-0" size="small" style="max-width: 100%;" variant="text">
                {{ postViewStore.getAuthorBio || '' }}
              </v-chip>
            </div>
          </v-card-subtitle>
          <!-- ✨ 조회수 정보 (새로운 컴포넌트 레벨로 분리, v-card-text로 예시) -->
          <!-- v-card-text나 v-row v-col 조합 등으로 변경 가능 -->
          <v-card-actions class="d-flex justify-space-between align-center pa-0 mb-8">
            <!-- 조회수 정보 (왼쪽) -->
            <div class="d-flex flex-wrap align-center">
              <v-chip class="pa-0" size="small" variant="text">
                조회수: {{ postViewStore.getPostViews }}
              </v-chip>
              <!-- 필요하다면 여기에 작성일자 같은 다른 메타 정보 추가 -->
            </div>

            <!-- 수정/삭제 버튼 (오른쪽) -->
            <div v-if="canEditOrDelete">
              <v-btn class="mr-2" size="small" variant="tonal" @click="goToEdit">수정</v-btn>
              <v-btn color="error" size="small" variant="tonal" @click="openDeleteConfirmDialog">삭제</v-btn>
            </div>
          </v-card-actions>

          <v-divider class="mb-8" />

          <!-- 썸네일 이미지 -->
          <v-img
            v-if="postViewStore.hasThumbnail"
            alt="게시글 썸네일"
            class="rounded-lg mb-8"
            cover
            max-height="400"
            :src="postViewStore.getThumbnailSrc"
          />

          <!-- TUI Viewer 내용 -->
          <div ref="viewerRef" class="tui-viewer-content" />

          <v-divider class="my-8" />

          <!-- 태그 목록 -->
          <div class="mb-8">
            <v-chip
              v-for="tag in postViewStore.getPostTags"
              :key="tag"
              class="mr-2 mb-2"
              color="info"
              label
            >
              #{{ tag }}
            </v-chip>
          </div>
          <!-- (추후) 댓글 컴포넌트 영역 -->
          <!--
          <v-card class="mt-8 pa-4" flat>
            <v-card-title class="text-h6">댓글</v-card-title>
            <CommentComponent :postId="currentPostId" />
          </v-card>
          -->

        </v-card>
      </v-col>
      <!-- 목차 컴포넌트 - 오른쪽에 고정 -->
      <div class="toc-container">
        <v-card v-if="postViewStore.tableOfContents.length > 0" class="toc-card" flat>
          <v-card-title class="text-subtitle-1 pb-2">목차</v-card-title>
          <v-list density="compact" nav>
            <v-list-item
              v-for="item in postViewStore.tableOfContents"
              :key="item.id"
              :active="activeHeading === item.id"
              :class="`pl-${(item.level - 1) * 4}`"
              link
              @click="scrollToHeading(item.id)"
            >
              <v-list-item-title class="text-body-2">
                {{ item.text }}
              </v-list-item-title>
            </v-list-item>
          </v-list>
        </v-card>
      </div>
    </v-row>

    <v-row v-else justify="center">
      <v-col class="text-center" cols="12">
        <p class="text-h6 text-medium-emphasis">게시글을 찾을 수 없습니다.</p>
        <v-btn class="mt-4" color="primary" @click="goBack">메인으로</v-btn>
      </v-col>
    </v-row>

    <!-- 삭제 확인 다이얼로그 -->
    <v-dialog v-model="showDeleteConfirm" max-width="400">
      <v-card>
        <v-card-title class="text-h5">게시글 삭제</v-card-title>
        <v-card-text>정말로 이 게시글을 삭제하시겠습니까? 삭제된 게시글은 복구할 수 없습니다.</v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn color="grey-darken-1" variant="text" @click="showDeleteConfirm = false">취소</v-btn>
          <v-btn color="error" variant="text" @click="confirmDelete">삭제</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
  import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { usePostViewStore } from '@/stores/postView'; // Pinia 스토어 임포트
  import { useAuthStore } from '@/stores/auth'; // 로그인 정보 스토어 임포트
  import '@toast-ui/editor/dist/toastui-editor-viewer.css';

  // TUI Viewer 임포트 및 CSS
  import Viewer from '@toast-ui/editor/dist/toastui-editor-viewer';
  import '@toast-ui/editor/dist/toastui-editor-viewer.css';
  import { useThemeStore } from '@/stores/theme';
  // 뷰티파이 테마와 맞춰주기 위한 TUI Viewer 테마 (선택 사항)
  // import '@toast-ui/editor/dist/theme/toastui-editor-dark.css';

  const route = useRoute();
  const router = useRouter();
  const postViewStore = usePostViewStore();
  const authStore = useAuthStore(); // 로그인 스토어
  const themeStore = useThemeStore();


  const viewerRef = ref(null); // TUI Viewer DOM 요소를 참조할 ref
  let tuiViewerInstance = null; // TUI Viewer 인스턴스를 저장할 변수
  const activeHeading = ref(''); // 현재 활성화된 헤딩을 추적하는 ref 추가

  const showDeleteConfirm = ref(false); // 삭제 확인 다이얼로그 상태

  // 현재 게시글 ID
  const currentPostId = computed(() => route.params.postId);

  // 게시글 수정/삭제 권한 확인
  const canEditOrDelete = computed(() => {
    console.log('로그인 상태 : ', authStore.isAuthenticated);
    console.log('게시글 데이터 로드 상태 : ', postViewStore.hasPost);
    // 1. 로그인 상태여야 하고
    if (!authStore.isAuthenticated) return false;
    // 2. 게시글 데이터가 로드되었어야 하고
    if (!postViewStore.hasPost) return false;
    // 3. 로그인한 유저의 UUID와 게시글 작성자의 UUID가 일치해야 함
    // (가정: postViewStore.getAuthorUuid가 게시글 작성자의 UUID를 반환)
    console.log('canEditOrDelete 결괏값 : ', authStore.user?.memberUuid === postViewStore.getAuthorUuid);
    return authStore.user?.memberUuid === postViewStore.getAuthorUuid;
  });


  // TUI Viewer 초기화 함수
  const initializeTuiViewer = () => {
    if (viewerRef.value && postViewStore.hasPost) {
      // 기존 뷰어 인스턴스가 있다면 파괴 (페이지 이동 시 재활용 방지)
      if (tuiViewerInstance) {
        tuiViewerInstance.destroy();
      }
      tuiViewerInstance = new Viewer({
        el: viewerRef.value,
        initialValue: postViewStore.getPostContent,
        theme: 'dark',
      // plugins: [codeSyntaxHighlight], // 코드 하이라이트 플러그인 등
      });

      // 목차 생성은 nextTick으로 뷰어 렌더링 완료 후 실행
      nextTick(() => {
        postViewStore.extractHeadings(viewerRef.value);
      });
    }
  };

  // 게시글 데이터를 불러오는 함수
  const loadPost = async postId => {
    if (postId) {
      await postViewStore.fetchPost(postId);
      // 게시글 데이터가 성공적으로 로드되면 TUI Viewer 초기화
      if (postViewStore.hasPost) {
        initializeTuiViewer();
      }
    }
  };

  // 컴포넌트가 마운트될 때 게시글 로드
  onMounted(async () => {
    console.log('PostView 컴포넌트 마운트, 게시글 ID:', currentPostId.value);
    await loadPost(currentPostId.value);
    updateCodeBlockStyles(themeStore.isDark);
    window.addEventListener('scroll', handleScroll);
  });

  // route.params.id가 변경될 때 (예: PostView -> 다른 PostView) 게시글 다시 로드
  watch(currentPostId, async (newId, oldId) => {
    if (newId && newId !== oldId) {
      console.log(`게시글 ID 변경 감지: ${oldId} -> ${newId}. 게시글 다시 로드.`);
      postViewStore.resetPostState(); // 이전 게시글 상태 초기화
      await loadPost(newId);
    }
  }, { immediate: true }); // 컴포넌트 로드 시 즉시 실행

  // 컴포넌트가 언마운트되기 전에 상태 및 뷰어 인스턴스 정리
  onBeforeUnmount(() => {
    console.log('PostView 컴포넌트 언마운트, 상태 초기화 및 TUI Viewer 파괴');
    postViewStore.resetPostState();
    if (tuiViewerInstance) {
      tuiViewerInstance.destroy();
      tuiViewerInstance = null;
    }
    window.addEventListener('scroll', handleScroll);
    // Blob URL 해제
    if (postViewStore.getThumbnailSrc) {
      URL.revokeObjectURL(postViewStore.getThumbnailSrc);
    }
  });

  // 이전 페이지로 돌아가기
  const goBack = () => {
    router.back(); // 뒤로 가기
  // 또는 router.push('/') 메인 페이지로 이동 등
  };

  // 게시글 수정 페이지로 이동
  const goToEdit = () => {
    router.push({ name: 'WritePost', query: { id: currentPostId.value } }); // 'WritePost'는 너의 게시글 작성/수정 컴포넌트의 라우터 이름
  };

  // 삭제 확인 다이얼로그 열기
  const openDeleteConfirmDialog = () => {
    showDeleteConfirm.value = true;
  };

  // 게시글 삭제 실행
  const confirmDelete = async () => {
    showDeleteConfirm.value = false; // 다이얼로그 닫기
    const success = await postViewStore.deletePost(currentPostId.value);
    if (success) {
    // 삭제 성공 시 토스트 메시지 등 알림 (Vuetify 스낵바 사용하면 됨)
    // showToast('success', '게시글이 삭제되었습니다.');
    } else {
    // 삭제 실패 시 에러 메시지
    // showToast('error', postViewStore.error);
    }
  };

  // 추가될 수 있는 기능: 댓글 탭으로 스크롤 이동
  const scrollToComments = () => {
  // 휠 반응하는 탭 컴포넌트 등을 고려해서 구현
  // 예: document.getElementById('comments').scrollIntoView({ behavior: 'smooth' });
  };

  // 스크롤 이벤트 감지 함수
  const handleScroll = () => {
    if (!postViewStore.tableOfContents.length) return;

    // 현재 화면에 보이는 헤딩 찾기
    const headings = postViewStore.tableOfContents.map(item => ({
      id: item.id,
      element: document.getElementById(item.id),
    })).filter(item => item.element);

    // 화면에 보이는 첫 번째 헤딩 찾기
    for (const heading of headings) {
      const rect = heading.element.getBoundingClientRect();
      if (rect.top >= 0 && rect.top <= window.innerHeight / 2) {
        activeHeading.value = heading.id;
        break;
      }
    }
  };
  // 헤딩으로 스크롤하는 함수
  const scrollToHeading = id => {
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
      activeHeading.value = id;
    }
  };

  const updateCodeBlockStyles = isDark => {
    console.log('에디터 스타일 업데이트');

    // 코드 블록 스타일 업데이트
    const codeBlocks = document.querySelectorAll('.toastui-editor-contents pre');
    if (codeBlocks && codeBlocks.length > 0) {
      codeBlocks.forEach(block => {
        if (isDark) {
          block.style.backgroundColor = '#2a2d3e';
          block.style.color = '#e4e4e4';
        } else {
          block.style.backgroundColor = '#f4f7f8';
          block.style.color = '';
        }
      });
    }

    // 문단(p) 태그 스타일 업데이트
    const paragraphs = document.querySelectorAll('.toastui-editor-contents p');
    if (paragraphs && paragraphs.length > 0) {
      paragraphs.forEach(p => {
        if (isDark) {
          p.style.color = '#e4e4e4'; // 다크모드에서는 밝은 색상
        } else {
          p.style.color = '#222'; // 라이트모드에서는 어두운 색상
        }
      });
    }

    // 제목(h1~h6) 태그 스타일 업데이트
    for (let i = 1; i <= 6; i++) {
      const headings = document.querySelectorAll(`.toastui-editor-contents h${i}`);
      if (headings && headings.length > 0) {
        headings.forEach(heading => {
          if (isDark) {
            heading.style.color = '#ffffff'; // 다크모드에서는 흰색
          } else {
            heading.style.color = '#222'; // 라이트모드에서는 어두운 색상
          }
        });
      }
    }
  };
  watch(() => themeStore.isDark, updateCodeBlockStyles, { immediate: true });
</script>

<style scoped>
.post-view-container {
  max-width: 1200px; /* 컨테이너 최대 너비 설정 */
  position: relative; /* 포지셔닝 컨텍스트 설정 */
}

.content-column {
  margin: 0 auto; /* 가운데 정렬 */
}

.toc-container {
  position: fixed;
  top: 300px;
  left: calc(50% + (800px / 2) + 8px);
  right: calc( (100vw - 960px) / 2 - 100px );;
  width: 280px; /* 목차 너비 */
  padding-left: 20px;
}

.toc-card {
  position: sticky;
  top: 0px; /* 상단에서 20px 떨어진 위치에 고정 */
  max-height: calc(100vh - 40px); /* 화면 높이에서 여백 빼기 */
  overflow-y: auto; /* 내용이 많으면 스크롤 가능하게 */
  padding: 16px;
  border-radius: 8px;
  background-color: rgb(var(--v-theme-surface));
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 목차 아이템 스타일 */
.v-list-item.active {
  background-color: rgba(var(--v-theme-primary), 0.1);
  border-left: 3px solid rgb(var(--v-theme-primary));
}

.v-list-item:hover {
  background-color: rgba(var(--v-theme-primary), 0.05);
}

/* 반응형 디자인 */
@media (max-width: 960px) {
  .toc-container {
    position: static;
    width: 100%;
    padding: 0;
    margin-bottom: 20px;
  }

  .toc-card {
    position: relative;
    top: 0;
    max-height: none;
  }
}
.custom-line-height {
  line-height: 1.2; /* 기본 1.5나 1.6 정도일텐데, 1.2로 줄여보자 */
  /* 또는 더 줄여서 1.1이나 1.05 같은 값으로 시도해보세요 */
}
/* ... 다른 스타일 ... */
.toastui-editor-contents h1 {
  color: #007bff;
  border-bottom: 2px solid #eee;
}
/* 기본 스타일 */
.toast-editor-wrapper .toastui-editor-defaultUI {
  background-color: white;
}

.toast-editor-wrapper .toastui-editor-defaultUI-toolbar {
  background-color: #f8f9fa;
}

.toast-editor-wrapper .toastui-editor-contents {
  background-color: white;
}

/* 헤딩 드롭다운 메뉴 스타일 */
.toastui-editor-popup {
  background-color: white;
}

.toastui-editor-popup-body {
  color: black;
}

.toastui-editor-popup-body button {
  color: black;
}

.toastui-editor-popup-body button:hover {
  background-color: #f1f1f1;
}
</style>
