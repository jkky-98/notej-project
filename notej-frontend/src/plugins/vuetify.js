/**
 * plugins/vuetify.js
 *
 * Framework documentation: https://vuetifyjs.com`
 */

// Styles
import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'

// Composables
import { createVuetify } from 'vuetify'

// https://vuetifyjs.com/en/introduction/why-vuetify/#feature-guides
export default createVuetify({
  theme: {
    defaultTheme: 'light', // ← 이 부분만 조정해도 됨
    themes: {
      light: {
        colors: {
          background: '#bdd4e7',
          surface: '#aab9cf',
          primary: '#8693ab',
          secondary: '#637074',
          text: '#212227',
        },
      },
      dark: {
        colors: {
          background: '#212227',
          surface: '#637074',
          primary: '#8693ab',
          secondary: '#aab9cf',
          text: '#bdd4e7',
        },
      },
    },
  },
})
