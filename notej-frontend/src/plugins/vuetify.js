// plugins/vuetify.js

import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'
import { createVuetify } from 'vuetify'

export default createVuetify({
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          background: '#cddfed',
          surface: '#bdd4e7',
          primary: '#aab9cf',
          secondary: '#8693ab',
          accent: '#637074',
          info: '#42494e',
          text: '#212227',
        },
      },
      dark: {
        colors: {
          background: '#212227',
          surface: '#42494e',
          primary: '#637074',
          secondary: '#8693ab',
          accent: '#aab9cf',
          info: '#bdd4e7',
          text: '#cddfed',
        },
      },
    },
  },
  defaults: {
    global: {
      style: {
        fontFamily: 'Pretendard Variable, sans-serif',
      },
    },
    VBtn: {
      color: 'info',
      variant: 'flat',
      rounded: 'lg',
      elevation: 1,
    },
    VTextField: {
      variant: 'outlined',
      density: 'comfortable',
      color: 'info',
    },
    VCard: {
      elevation: 2,
      rounded: 'lg',
    },
    VAppBar: {
      color: 'info',
      flat: true,
    },
    VContainer: {
      fluid: true,
    },
  },
})
