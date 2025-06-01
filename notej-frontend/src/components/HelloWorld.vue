<template>
  <v-container class="pa-0" fluid>
    <!-- 상단 애니메이션 배너 -->
    <div :class="['notej-banner', isDark ? 'dark' : 'light']">
      <div class="notej-text">
        <span
          v-for="(char, i) in textChars"
          :key="i"
          :class="[
            char === ' ' ? 'space' : '',
            char === 'J' ? 'bounceJ' : ''
          ]"
        >
          {{ char === ' ' ? '\u00A0' : char }}
        </span>
      </div>
    </div>

    <!-- 본문 콘텐츠 -->
    <v-row no-gutters>
      <!-- 왼쪽: xterm.js 터미널 -->
      <v-col class="pa-4" cols="12" md="6">
        <v-card class="h-100">
          <v-card-title class="text-h6 font-weight-bold">터미널</v-card-title>
          <v-card-text class="pa-0">
            <UserTerminal />
          </v-card-text>
        </v-card>
      </v-col>

      <!-- 오른쪽: Vuetify 템플릿 -->
      <v-col class="pa-4" cols="12" md="6">
        <v-img class="mb-4" height="150" src="@/assets/logo.png" />

        <div class="mb-8 text-center">
          <div class="text-body-2 font-weight-light mb-n1">Welcome to</div>
          <h1 class="text-h2 font-weight-bold">Vuetify</h1>
        </div>

        <v-card
          class="py-4 mb-4"
          color="surface-variant"
          image="https://cdn.vuetifyjs.com/docs/images/one/create/feature.png"
          prepend-icon="mdi-rocket-launch-outline"
          rounded="lg"
          variant="tonal"
        >
          <template #image>
            <v-img position="top right" />
          </template>
          <template #title>
            <h2 class="text-h5 font-weight-bold">Get started</h2>
          </template>
          <template #subtitle>
            <div class="text-subtitle-1">
              Change this page by updating <v-kbd>{{ `<HelloWorld />` }}</v-kbd> in
              <v-kbd>components/HelloWorld.vue</v-kbd>.
            </div>
          </template>
        </v-card>

        <v-row>
          <v-col v-for="link in links" :key="link.href" cols="12" sm="6">
            <v-card
              append-icon="mdi-open-in-new"
              class="py-4"
              color="surface-variant"
              :href="link.href"
              :prepend-icon="link.icon"
              rel="noopener noreferrer"
              rounded="lg"
              :subtitle="link.subtitle"
              target="_blank"
              :title="link.title"
              variant="tonal"
            />
          </v-col>
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
  import UserTerminal from '@/components/UserTerminal.vue'
  import { useTheme } from 'vuetify'
  import { computed } from 'vue'

  const theme = useTheme()
  const isDark = computed(() => theme.global.current.value.dark)

  const textChars = 'WELCOME TO NOTEJ'.split('')

  const links = [
    {
      href: 'https://vuetifyjs.com/',
      icon: 'mdi-text-box-outline',
      subtitle: 'Learn about all things Vuetify in our documentation.',
      title: 'Documentation',
    },
    {
      href: 'https://vuetifyjs.com/introduction/why-vuetify/#feature-guides',
      icon: 'mdi-star-circle-outline',
      subtitle: 'Explore available framework Features.',
      title: 'Features',
    },
    {
      href: 'https://vuetifyjs.com/components/all',
      icon: 'mdi-widgets-outline',
      subtitle: 'Discover components in the API Explorer.',
      title: 'Components',
    },
    {
      href: 'https://discord.vuetifyjs.com',
      icon: 'mdi-account-group-outline',
      subtitle: 'Connect with Vuetify developers.',
      title: 'Community',
    },
  ]
</script>

<style scoped>
.notej-banner {
  height: 350px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--v-theme-background);
  overflow: hidden;
  position: relative;
}

.notej-text {
  font-size: 3rem;
  font-weight: 900;
  font-family: 'Pretendard Variable', 'monospace', sans-serif;
  color: var(--v-theme-text);
  letter-spacing: 0.15em;
  display: flex;
  align-items: baseline;
  gap: 0.15em;
  animation: glowIntro 1.8s ease-out forwards;
  opacity: 0;
  transform: scale(0.8) translateY(30px) rotateX(20deg);
  backdrop-filter: blur(2px);
  text-shadow:
    0 0 10px rgba(0, 123, 255, 0.4),
    0 0 20px rgba(0, 123, 255, 0.2),
    0 0 30px rgba(0, 123, 255, 0.1);
  cursor: default;
  transition: all 0.3s ease-in-out;
  position: relative;
}

/* ✨ 아래쪽 선 그리기 */
.notej-text::after {
  content: '';
  position: absolute;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 0%;
  height: 3px;
  background-color: #2196f3;
  opacity: 0;
  animation: lineDraw 1.6s ease-out 4.4s forwards;
}

@keyframes lineDraw {
  0% {
    width: 0%;
    opacity: 0;
  }
  100% {
    width: 100%;
    opacity: 1;
  }
}

.notej-text:hover {
  transform: scale(1.03) translateY(0px);
  text-shadow:
    0 0 15px rgba(0, 123, 255, 0.6),
    0 0 30px rgba(0, 123, 255, 0.4),
    0 0 45px rgba(0, 123, 255, 0.3);
}

.bounceJ {
  display: inline-block;
  font-size: 4rem;
  font-weight: 1000;
  color: #2196f3;
  line-height: 1;
  vertical-align: baseline;
  transform-origin: center;
  animation: jumpSpinReturn 2.6s ease-in-out 1.8s both;
}

.space {
  width: 0.5em;
}

@keyframes glowIntro {
  0% {
    opacity: 0;
    transform: scale(0.8) translateY(30px) rotateX(20deg);
    letter-spacing: 0.4em;
    text-shadow: none;
  }
  40% {
    opacity: 0.6;
    transform: scale(1.1) translateY(-10px) rotateX(0deg);
    letter-spacing: 0.1em;
  }
  70% {
    transform: scale(0.98) translateY(5px);
  }
  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
    letter-spacing: 0.2em;
  }
}

@keyframes jumpSpinReturn {
  0% {
    transform: translateY(-120%) rotate(0deg) scale(1.3);
    opacity: 0;
  }
  30% {
    transform: translate(0, 0) rotate(0deg) scale(1.05);
    opacity: 1;
  }
  50% {
    transform: translate(80%, -180%) rotate(1080deg) scale(1.15);
  }
  75% {
    transform: translate(20%, -40%) rotate(1080deg) scale(1.05);
  }
  100% {
    transform: translate(0, 0) rotate(1080deg) scale(1);
  }
}
</style>
