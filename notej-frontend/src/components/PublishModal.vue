<template>
  <v-dialog
    v-model="postStore.isPublishModalOpen"
    fullscreen
    persistent
    :scrim="true"
    transition="dialog-bottom-transition"
  >
    <v-card class="d-flex flex-column"> <!-- flex-column 추가 -->
      <v-toolbar color="primary" dark>
        <v-btn icon @click="postStore.closePublishModal()">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title>게시글 발행 설정</v-toolbar-title>
        <v-spacer />
        <!-- 발행하기 버튼은 하단으로 이동 -->
      </v-toolbar>

      <!-- 중앙 정렬을 위한 v-main과 v-container 설정 -->
      <v-main class="d-flex align-center justify-center pa-4"> <!-- d-flex, align-center, justify-center 적용 -->
        <v-container class="max-w-screen-lg" fluid> <!-- 최대 너비 제한 (선택 사항) -->
          <v-row dense>
            <!-- 썸네일 업로드 -->
            <v-col cols="12">
              <v-card class="mb-4 pa-4" variant="outlined">
                <v-card-title class="text-subtitle-1 pb-2">썸네일 업로드</v-card-title>
                <v-card-text>
                  <v-file-input
                    accept="image/*"
                    density="comfortable"
                    label="썸네일 이미지 선택"
                    prepend-icon="mdi-camera"
                    variant="outlined"
                    @change="onThumbnailChange"
                  />

                  <v-img
                    v-if="postStore.hasThumbnailToDisplay"
                    aspect-ratio="16 / 9"
                    class="mt-2 rounded"
                    cover
                    max-height="400"
                    :src="postStore.displayedThumbnailUrl"
                  />
                </v-card-text>
              </v-card>
            </v-col>

            <v-col cols="12" md="6">
              <!-- 공개 여부 -->
              <v-card class="mb-4 pa-4" variant="outlined">
                <v-card-title class="text-subtitle-1 pb-2">공개 여부</v-card-title>
                <v-card-text>
                  <v-radio-group v-model="postStore.isPublic" inline>
                    <v-radio label="공개" :value="true" />
                    <v-radio label="비공개" :value="false" />
                  </v-radio-group>
                </v-card-text>
              </v-card>

              <!-- 카테고리 선택 -->
              <v-card class="mb-4 pa-4" variant="outlined">
                <v-card-title class="text-subtitle-1 pb-2">카테고리 선택</v-card-title>
                <v-card-text>
                  <v-select
                    v-model="postStore.selectedCategory"
                    density="comfortable"
                    hide-details
                    item-title="name"
                    item-value="id"
                    :items="selectableCategories"
                    label="카테고리를 선택하세요"
                    variant="outlined"
                  />
                </v-card-text>
              </v-card>
            </v-col>

            <v-col cols="12" md="6">
              <!-- 짧은 소개글 -->
              <v-card class="mb-4 pa-4 h-100" variant="outlined"> <!-- h-100 추가로 같은 높이 유지 시도 -->
                <v-card-title class="text-subtitle-1 pb-2">짧은 소개글</v-card-title>
                <v-card-text class="flex-grow-1"> <!-- flex-grow-1 추가 -->
                  <v-textarea
                    v-model="postStore.shortDescription"
                    auto-grow
                    label="글의 요약이나 소개글을 작성하세요"
                    rows="5"
                    variant="outlined"
                    높이
                    조정
                  >
                    max-rows="5"
                    hide-details
                    ></v-textarea>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>

          <!-- 에러 메시지 -->
          <v-alert
            v-if="postStore.error"
            class="mt-4"
            type="error"
            variant="tonal"
          >
            {{ postStore.error }}
          </v-alert>
        </v-container>
      </v-main>

      <!-- 하단 발행/취소 버튼 -->
      <v-card-actions class="pa-4 bg-surface-variant justify-end"> <!-- 배경색 추가, 오른쪽 정렬 -->
        <v-btn
          color="primary"
          :loading="postStore.isLoading"
          size="large"
          variant="flat"
          @click="handleFinalPublish"
        >
          발행하기
        </v-btn>
        <v-btn
          color="secondary"
          :disabled="postStore.isLoading"
          size="large"
          variant="outlined"
          @click="postStore.closePublishModal()"
        >
          취소
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script setup>
  import { computed, onMounted, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { usePostStore } from '@/stores/post';


  const postStore = usePostStore();
  const router = useRouter();

  // 스낵바 상태
  const snackbar = ref({
    show: false,
    text: '',
    color: 'success',
    timeout: 3000,
  });

  const selectableCategories = computed(() => {
    const processed = [];
    // 1. 부모 ID가 없는 최상위 카테고리만 필터링 후 seq(순서) 기반으로 정렬
    const mainCategories = postStore.categories
      .filter(cat => cat.parentId === null)
      .sort((a, b) => a.seq - b.seq);

    // 2. 각 최상위 카테고리에 대해 처리
    mainCategories.forEach(mainCat => {
      // 2.1. 최상위 카테고리 자체를 추가
      processed.push({
        id: mainCat.categoryId, // ★★★ 여기서 mainCat.id -> mainCat.categoryId
        name: mainCat.name, // 최상위 카테고리는 이름만 표시
      });
      // 2.2. 해당 최상위 카테고리의 하위 카테고리들 필터링 후 seq(순서) 기반으로 정렬
      postStore.categories
        .filter(subCat => subCat.parentId === mainCat.categoryId) // ★★★ 여기서 mainCat.id -> mainCat.categoryId
        .sort((a, b) => a.seq - b.seq)
        .forEach(subCat => {
          // 2.3. 하위 카테고리는 "  - " 접두사 붙여서 추가
          processed.push({
            id: subCat.categoryId, // ★★★ 여기서 subCat.id -> subCat.categoryId
            name: `  - ${subCat.name}`, // 두 칸 띄우고 - 붙여서 시각적으로 구분
          });
        });
    });
    return processed;
  });

  async function onThumbnailChange (event) {
    const file = event.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('image', file);

    try {
      const response = await postStore.uploadThumbnail(formData);
      postStore.setThumbnail(response.url);
      showToast('success', '썸네일 업로드 완료!');
    } catch (error) {
      console.error('썸네일 업로드 실패:', error);
      showToast('error', '썸네일 업로드에 실패했습니다!');
    }
  }

  async function handleFinalPublish () {
    if (!postStore.postDraft?.title) {
      showToast('warning', '제목을 입력해주세요!');
      return;
    }
    if (!postStore.postDraft?.content) {
      showToast('warning', '내용을 입력해주세요!');
      return;
    }
    if (postStore.selectedCategory === null) {
      showToast('warning', '카테고리를 선택해주세요!');
      return;
    }
    if (postStore.shortDescription === null || postStore.shortDescription.trim() === '') {
      showToast('warning', '짧은 소개글을 입력해주세요!');
      return;
    }

    try {
      const result = await postStore.publishPost();

      if (result) {
        showToast('success', '글이 성공적으로 발행되었습니다!');
        router.push(`/post/${result.id}`);
        postStore.closePublishModal();
        // 발행 성공 후 postDraft 초기화 (선택 사항)
        postStore.postDraft = null;
      }
    } catch (error) {
      console.error('최종 발행 실패:', error);
      showToast('error', '최종 발행에 실패했습니다!');
    }
  }
  // 토스트 메시지 표시 함수
  const showToast = (type, message) => {
    snackbar.value.text = message;
    snackbar.value.color = type === 'success' ? 'success' : type === 'error' ? 'error' : 'warning';
    snackbar.value.show = true;
  };


  onMounted(() => {
    // 해당 회원의 카테고리 가져오기
    if (postStore.categories.length === 0) {
      postStore.getCategories();
    }
  });

</script>
