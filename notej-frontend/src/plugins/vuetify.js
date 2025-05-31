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
          background: '#eef0f2',
          surface: '#c6c7c4',
          primary: '#a2999e',
          secondary: '#846a6a',
          accent: '#b4b0b1',
          info: '#5d5353',
          text: '#353b3c',
        },
      },
      dark: {
        colors: {
          background: '#353b3c',
          surface: '#5d5353',
          primary: '#a2999e',
          secondary: '#846a6a',
          accent: '#b4b0b1',
          info: '#c6c7c4',
          text: '#eef0f2',
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
      color: 'primary',
      variant: 'flat',
      rounded: 'lg',
      elevation: 1,
    },
    VTextField: {
      variant: 'outlined',
      density: 'comfortable',
      color: 'primary',
    },
    VCard: {
      elevation: 2,
      rounded: 'lg',
    },
    VAppBar: {
      color: 'primary',
      flat: true,
    },
    VContainer: {
      fluid: true,
    },
  },
})
