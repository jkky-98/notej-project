<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <h1>Tagify 정상 테스트</h1>
      </v-col>
    </v-row>

    <!-- v-card로 감싼 태그 입력 필드 -->
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-text>
            <div class="tagify-container">
              <input ref="tagInput" placeholder="태그를 입력하고 엔터를 누르세요">
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- 테스트 버튼 -->
    <v-row class="mt-4">
      <v-col cols="12">
        <v-btn color="primary" @click="simulateRouterReplace">
          router.replace + showToast 테스트
        </v-btn>
      </v-col>
    </v-row>

    <!-- 로그 출력 -->
    <v-row class="mt-4">
      <v-col cols="12">
        <v-card>
          <v-card-title>로그:</v-card-title>
          <v-card-text>
            <v-sheet class="logs pa-4" color="grey-lighten-4" rounded>
              <pre>{{ logs.join('\n') }}</pre>
            </v-sheet>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="snackbar.timeout">
      {{ snackbar.text }}
      <template #actions>
        <v-btn color="white" variant="text" @click="snackbar.show = false">닫기</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script setup>
  import { onBeforeUnmount, onMounted, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { usePostStore } from '@/stores/post';
  import Tagify from '@yaireo/tagify';
  import '@yaireo/tagify/dist/tagify.css';

  const router = useRouter();
  const postStore = usePostStore();
  const tagInput = ref(null);
  const tagifyInstance = ref(null);
  const logs = ref([]);

  // 스낵바 상태
  const snackbar = ref({
    show: false,
    text: '',
    color: 'success',
    timeout: 3000,
  });

  // 로그 함수
  const addLog = message => {
    logs.value.push(`${new Date().toLocaleTimeString()}: ${message}`);
  };

  // 토스트 메시지 표시 함수
  const showToast = (type, message) => {
    snackbar.value.text = message;
    snackbar.value.color = type;
    snackbar.value.show = true;
  };

  // Tagify 초기화
  onMounted(() => {
    postStore.clearTags();

    if (tagInput.value) {
      tagifyInstance.value = new Tagify(tagInput.value, {
        maxTags: 5,
        callbacks: {
          add: e => {
            addLog(`태그 추가됨: ${e.detail.data.value}`);
            postStore.addTag(e.detail.data.value);
          },
          remove: e => {
            if (e?.detail?.data) {
              addLog(`태그 제거됨: ${e.detail.data.value}`);
              postStore.removeTag(e.detail.data.value);
            }
          },
        },
      });

      // 초기 태그 추가
      tagifyInstance.value.addTags(['테스트1', '테스트2']);
      addLog('Tagify 초기화 완료');
    }
  });

  // 컴포넌트 언마운트 시 정리
  onBeforeUnmount(() => {
    if (tagifyInstance.value) {
      tagifyInstance.value.destroy();
    }
  });

  // router.replace 시뮬레이션
  const simulateRouterReplace = () => {
    addLog('router.replace 시뮬레이션 시작');

    // 현재 URL에 쿼리 파라미터 추가
    router.replace({ query: { test: Date.now() } });

    // 라우터 변경 후 바로 스낵바 표시
    showToast('success', 'router.replace + showToast 호출됨!');

    // 태그 상태 확인
    setTimeout(() => {
      addLog(`태그 상태: ${postStore.getTags.length > 0 ? '✅ 정상' : '❌ 사라짐!'}`);
    }, 100);
  };
</script>

<style>
.tagify-container {
  margin: 10px 0;
}
.logs {
  max-height: 200px;
  overflow-y: auto;
  font-family: monospace;
}
</style>
