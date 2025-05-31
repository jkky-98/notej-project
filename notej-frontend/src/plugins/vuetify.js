import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'
import { createVuetify } from 'vuetify'

export default createVuetify({
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          background: '#ebf5ee',
          surface: '#d5cfc6',
          primary: '#78a1bb',
          secondary: '#506980',
          accent: '#bfa89e',
          info: '#8b786d',
          text: '#283044',
        },
      },
      dark: {
        colors: {
          background: '#283044',
          surface: '#506980',
          primary: '#78a1bb',
          secondary: '#bfa89e',
          accent: '#d5cfc6',
          info: '#ebf5ee',
          text: '#ebf5ee',
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
