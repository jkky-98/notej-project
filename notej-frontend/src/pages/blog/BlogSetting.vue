<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" lg="6" md="8">
        <v-card class="pa-4 mb-4">
          <v-card-title class="text-h5 mb-4">프로필 설정</v-card-title>

          <!-- 프로필 이미지 (클릭 가능하게만) -->
          <div class="d-flex justify-center mb-6">
            <v-avatar class="mb-3 cursor-pointer" size="150" @click="triggerImageUpload">
              <v-img
                alt="프로필 이미지"
                cover
                :src="displayProfileData.profileImage || defaultProfilePic"
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
  import { useRoute } from 'vue-router'
  import { useAuthStore } from '@/stores/auth'
  import { useBlogStore } from '@/stores/blog'
  import defaultProfilePic from '@/assets/defaults/profile.png'

  const router = useRoute()
  const authStore = useAuthStore()
  const blogStore = useBlogStore()

  // 현재 보여줄 프로필 데이터
  const displayProfileData = ref({
    title: '',
    bio: '',
    profileImage: null, // URL은 여기서도 사용하지 않음
  })

  // 편집을 위한 임시 데이터
  const editProfileData = reactive({
    title: '',
    bio: '',
  })

  // UI 상태
  const isEditing = ref(false)
  const loading = ref(false)
  const snackbar = ref(false)
  const snackbarText = ref('')
  const snackbarColor = ref('success')
  const form = ref(null)
  const imageInput = ref(null) // 파일 input 요소 참조

  // 컴포넌트 마운트 시 데이터 로드
  onMounted(async () => {
    if (!authStore.isAuthenticated) {
      router.push('/login')
      return
    }

    loading.value = true
    try {
      const res = await blogStore.fetchMyProfile()
      displayProfileData.value = {
        title: res.title || '',
        bio: res.bio || '',
        profileImage: res.profileImage || null,
      }
    } catch (error) {
      showSnackbar('프로필 정보를 불러오는데 실패했습니다', 'error')
      console.error('프로필 로드 에러:', error)
    } finally {
      loading.value = false
    }
  })

  // 이미지 파일 선택 input 트리거
  function triggerImageUpload () {
    imageInput.value.click()
  }

  // 이미지 파일이 선택되었을 때 (실제 업로드 X, 선택 여부만 확인)
  function handleImageSelected (event) {
    const file = event.target.files[0]
    if (file) {
      console.log('이미지 파일이 선택되었습니다:', file.name)
      showSnackbar(`이미지 "${file.name}"이 선택되었습니다.`, 'info')
    // 여기서는 파일 선택만 하고, 실제 업로드는 이 컴포넌트에서 처리하지 않음.
    // 만약 별도의 이미지 업로드 페이지로 이동해야 한다면 router.push를 사용.
    // router.push({ path: '/image-upload', query: { someParam: 'value' }});
    }
    // 파일 인풋 초기화 (같은 파일을 다시 선택해도 change 이벤트 발생하도록)
    event.target.value = '';
  }

  // '수정하기' 버튼 클릭 시 편집 모드 시작
  function startEdit () {
    editProfileData.title = displayProfileData.value.title
    editProfileData.bio = displayProfileData.value.bio
    isEditing.value = true
  }

  // '취소' 버튼 클릭 시 편집 모드 취소
  function cancelEdit () {
    isEditing.value = false
    // 폼 유효성 메시지 초기화
    form.value?.resetValidation()
  }

  // 프로필 저장
  async function saveProfile () {
    const { valid } = await form.value.validate()
    if (!valid) return

    loading.value = true

    try {
      // URL과 이미지는 여기서 수정하지 않으므로 전송 데이터에 포함하지 않음
      await blogStore.updateMyProfile({
        title: editProfileData.title,
        bio: editProfileData.bio || '',
      })

      // 성공 시 displayProfileData를 업데이트하고 보기 모드로 전환
      displayProfileData.value.title = editProfileData.title
      displayProfileData.value.bio = editProfileData.bio
      isEditing.value = false
      showSnackbar('프로필이 저장되었습니다', 'success')

    } catch (error) {
      showSnackbar('프로필 저장에 실패했습니다', 'error')
      console.error('프로필 저장 에러:', error)
    } finally {
      loading.value = false
    }
  }

  // 스낵바 표시 함수
  function showSnackbar (text, color = 'success') {
    snackbarText.value = text
    snackbarColor.value = color
    snackbar.value = true
  }
</script>
