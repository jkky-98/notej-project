<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" lg="6" md="8">
        <v-card class="pa-4 mb-4">
          <v-card-title class="text-h5 mb-4">프로필 설정</v-card-title>

          <!-- 프로필 이미지 (클릭 시 바로 파일 선택 창 열림, 이미지 업로드 중일 때 로딩) -->
          <div class="d-flex justify-center mb-6">
            <v-avatar class="mb-3 cursor-pointer" size="150" @click="triggerImageUpload">
              <!-- imageUploadLoading 중일 때 로딩 스피너 추가 (옵션) -->
              <v-progress-circular
                v-if="imageUploadLoading"
                color="primary"
                indeterminate
                size="60"
                width="6"
              />
              <v-img
                v-else
                alt="프로필 이미지"
                cover
                :src="imagePreviewUrl || displayProfileData.profileImage || defaultProfilePic"
              />
              <input
                ref="imageInput"
                accept="image/*"
                style="display: none"
                type="file"
                @change="handleImageSelected"
              >
            </v-avatar>
          </div>

          <!--
            나머지 v-form (블로그 제목/소개글 편집) 및 보기 모드 부분은
            이전과 완전히 동일하게 유지.
            여기에 로딩 상태는 'loading'만 연결하면 됨.
          -->
          <v-form v-if="isEditing" ref="form" @submit.prevent="saveProfile">
            <!-- 편집 모드: 블로그 제목 -->
            <v-text-field
              v-model="editProfileData.title"
              class="mb-4"
              counter="50"
              label="블로그 제목"
              :rules="[v => !!v || '블로그 제목을 입력해주세요']"
              variant="outlined"
            />

            <!-- 편집 모드: 소개글 -->
            <v-textarea
              v-model="editProfileData.bio"
              class="mb-6"
              counter="300"
              label="소개글"
              rows="4"
              :rules="[v => !v || v.length <= 300 || '최대 300자까지 입력 가능합니다']"
              variant="outlined"
            />

            <div class="d-flex justify-end">
              <v-btn
                class="mr-2"
                color="secondary"
                :disabled="loading"
                variant="outlined"
                @click="cancelEdit"
              >
                취소
              </v-btn>
              <v-btn
                color="primary"
                :disabled="loading"
                :loading="loading"
                type="submit"
              >
                저장하기
              </v-btn>
            </div>
          </v-form>

          <div v-else>
            <!-- 보기 모드: 블로그 제목 -->
            <div class="text-h6 mb-2">블로그 제목</div>
            <p class="text-body-1 mb-4">{{ displayProfileData.title || '등록된 제목이 없습니다.' }}</p>

            <!-- 보기 모드: 소개글 -->
            <div class="text-h6 mb-2">소개글</div>
            <p class="text-body-1 mb-6">{{ displayProfileData.bio || '등록된 소개글이 없습니다.' }}</p>

            <div class="d-flex justify-end">
              <v-btn
                color="info"
                :disabled="loading"
                variant="outlined"
                @click="startEdit"
              >
                수정
              </v-btn>
            </div>
          </div>
        </v-card>
      </v-col>
    </v-row>

    <!-- 알림 스낵바 -->
    <v-snackbar v-model="snackbar" :color="snackbarColor" timeout="3000">
      {{ snackbarText }}
      <template #actions>
        <v-btn variant="text" @click="snackbar = false">닫기</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue'
  import { useBlogStore } from '@/stores/blog'
  import defaultProfilePic from '@/assets/defaults/profile.png'
  import { compressImage, createImageFormData, validateImage } from '@/utils/imageUtils'

  const blogStore = useBlogStore()

  // 현재 보여줄 프로필 데이터 (Backend or S3 URL)
  const displayProfileData = ref({
    title: '',
    bio: '',
    profileImage: null, // S3 URL을 저장할 곳 (기본값 null)
  })

  // 편집을 위한 임시 데이터 (bio, title 전용)
  const editProfileData = reactive({
    title: '',
    bio: '',
  })

  // UI 상태
  const isEditing = ref(false) // 블로그 제목/소개글 편집 폼 토글
  const loading = ref(false) // 블로그 제목/소개글 저장 액션 로딩
  const imageUploadLoading = ref(false) // 프로필 이미지 업로드 액션 로딩 (따로 분리)

  const snackbar = ref(false)
  const snackbarText = ref('')
  const snackbarColor = ref('success')
  const form = ref(null) // 블로그 제목/소개글 폼 ref
  const imageInput = ref(null) // 파일 input 요소 ref

  // 프로필 이미지 미리보기 URL (이건 '프론트에서만' 사용할 임시 URL)
  const imagePreviewUrl = ref(null)

  // 컴포넌트 마운트 시 데이터 로드
  onMounted(async () => {

    loading.value = true // 초기 데이터 로딩 시 general loading 사용
    try {
      const res = await blogStore.fetchMyProfile()
      displayProfileData.value = {
        title: res.title || '',
        bio: res.bio || '',
        profileImage: res.profilePicture || null, // 초기 로딩 시 S3 URL 가져옴
      }
    } catch (error) {
      showSnackbar('프로필 정보를 불러오는데 실패했습니다', 'error')
      console.error('프로필 로드 에러:', error)
    } finally {
      loading.value = false
    }
  })

  // --- 프로필 이미지 처리 로직 ---

  // 이미지 파일 선택 input 트리거 (버튼 클릭 시 바로 파일 선택 창 띄움)
  function triggerImageUpload () {
    imageInput.value.click()
  }

  // 이미지 파일이 선택되었을 때 (파일 업로드 및 미리보기 처리)
  async function handleImageSelected (event) {
    const file = event.target.files[0];

    // 파일 없으면 종료
    if (!file) {
      if (imagePreviewUrl.value) URL.revokeObjectURL(imagePreviewUrl.value);
      imagePreviewUrl.value = null;
      event.target.value = null;
      return;
    }

    // 이미지 유효성 검증
    const validation = validateImage(file);
    if (!validation.valid) {
      showSnackbar(validation.error, 'error');
      event.target.value = null;
      return;
    }

    // 로딩 시작
    imageUploadLoading.value = true;

    try {
      // 미리보기용 URL 생성 (압축 전 원본)
      if (imagePreviewUrl.value) URL.revokeObjectURL(imagePreviewUrl.value);
      imagePreviewUrl.value = URL.createObjectURL(file);

      // 이미지 압축
      const compressedFile = await compressImage(file);
      showSnackbar('이미지 압축 완료, 업로드 중...', 'info');

      // FormData 생성 및 업로드
      const formData = createImageFormData(compressedFile);
      const newImageUrl = await blogStore.updateProfileImage(formData);

      // 성공 시 프로필 이미지 URL 업데이트
      displayProfileData.value.profileImage = newImageUrl;
      if (blogStore.user) {
        blogStore.user.profilePicture = newImageUrl;
      }
      showSnackbar('프로필 이미지가 업데이트되었습니다', 'success');

    } catch (error) {
      console.error('이미지 처리/업로드 실패:', error);
      showSnackbar('이미지 업로드에 실패했습니다', 'error');

      // 실패 시 미리보기 초기화
      if (imagePreviewUrl.value) URL.revokeObjectURL(imagePreviewUrl.value);
      imagePreviewUrl.value = null;
    } finally {
      imageUploadLoading.value = false;
      event.target.value = null;
    }
  }

  // --- 블로그 제목/소개글 편집 로직 ---

  // 편집 모드 시작
  function startEdit () {
    isEditing.value = true
    // 현재 표시되는 값으로 편집 데이터 초기화 (이미지는 이제 별개!)
    editProfileData.title = displayProfileData.value.title
    editProfileData.bio = displayProfileData.value.bio
  }

  // 편집 취소
  function cancelEdit () {
    isEditing.value = false
    // 편집 데이터는 여기서 굳이 초기화할 필요 없음 (재시작 시 덮어써짐)
    // 이미지 미리보기는 이미지 로직에 속하므로 여기서 초기화 불필요
  }

  // 프로필 정보(제목, 소개글) 저장 (이미지 로직은 완전히 분리)
  async function saveProfile () {
    const { valid } = await form.value.validate()
    if (!valid) return

    loading.value = true // general loading 시작 (제목/소개글 저장 전용)
    try {
      const updatedProfile = {
        title: editProfileData.title,
        bio: editProfileData.bio,
      }
      // Pinia 스토어 액션 호출 (제목/소개글 업데이트 전용)
      // 백엔드에서 업데이트 성공 시 HTTP 200 OK만 보내줘도 됨.
      // 여기서 res 값으로 displayProfileData를 업데이트하지 않음!
      await blogStore.updateMyProfile(updatedProfile)

      // *** 중요 변경사항: editProfileData의 값을 displayProfileData로 바로 복사 ***
      displayProfileData.value.title = editProfileData.title
      displayProfileData.value.bio = editProfileData.bio

      showSnackbar('프로필 정보가 성공적으로 업데이트되었습니다', 'success')
      isEditing.value = false // 편집 모드 종료

    } catch (error) {
      showSnackbar('프로필 정보 업데이트에 실패했습니다', 'error')
      console.error('프로필 정보 업데이트 에러:', error)
    } finally {
      loading.value = false // general loading 종료
    }
  }

  // --- 유틸리티 함수 ---

  // 스낵바 표시 함수
  function showSnackbar (text, color = 'success') {
    snackbarText.value = text
    snackbarColor.value = color
    snackbar.value = true
  }

</script>
