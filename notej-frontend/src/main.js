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

// Pinia
import { createPinia } from 'pinia'

// Styles
import 'unfonts.css'

const app = createApp(App)

registerPlugins(app)

app.use(createPinia()) // Pinia 등록
app.mount('#app')
