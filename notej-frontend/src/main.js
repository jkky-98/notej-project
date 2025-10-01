/**
 * main.js
 *
 * Bootstraps Vuetify and other plugins then mounts the App`
 */

// Plugins
import { registerPlugins } from '@/plugins'

// Components
import App from './App.vue'

// Composables
import { createApp } from 'vue'

// Pinia 임포트
import { createPinia } from 'pinia'
// auth 스토어 임포트
import { useAuthStore } from '@/stores/auth'

// Styles
import 'unfonts.css'

const app = createApp(App)

// Pinia 인스턴스 생성
const pinia = createPinia()

// Pinia를 먼저 Vue 앱에 등록
app.use(pinia)

// **Pinia가 등록된 후에 authStore 인스턴스 가져오기**
const authStore = useAuthStore()

// 나머지 플러그인 등록
registerPlugins(app)

// **앱 마운트 전에 인증 초기화 액션 호출**
// 비동기 함수이므로 await를 사용하려면 이 부분을 async IIFE (즉시 실행 함수)로 감싸거나,
// 별도의 비동기 함수를 만들어서 호출해야 함.
// 여기서는 IIFE 패턴으로 작성.
;(async () => {
  console.log('[main.js] 인증 초기화 시작...');
  try {
    await authStore.initializeAuth();
    console.log('[main.js] 인증 초기화 완료.');
  } catch (error) {
    console.error('[main.js] 인증 초기화 중 오류 발생:', error);
    // 오류가 발생해도 앱은 마운트하도록 처리하거나, 필요 시 다른 로직 추가
  } finally {
    // 인증 초기화 성공/실패 여부와 관계없이 앱을 마운트
    app.mount('#app');
    console.log('[main.js] Vue 앱 마운트 완료.');
  }
})();
