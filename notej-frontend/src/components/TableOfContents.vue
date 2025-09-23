<!-- components/blog/TableOfContents.vue -->
<template>
  <v-card v-if="tableOfContents.length > 0" class="mb-8 pa-4" variant="tonal">
    <v-card-title class="text-subtitle-1 pb-2">목차</v-card-title>
    <v-list dense nav>
      <v-list-item
        v-for="item in tableOfContents"
        :key="item.id"
        :class="`pl-${(item.level - 1) * 4}`"
        link
        @click="scrollToHeading(item.id)"
      >
        <v-list-item-title class="text-body-2">
          {{ item.text }}
        </v-list-item-title>
      </v-list-item>
    </v-list>
  </v-card>
</template>

<script setup>
  import { defineProps } from 'vue';

  const props = defineProps({
    tableOfContents: {
      type: Array,
      default: () => [],
    },
  });

  const scrollToHeading = id => {
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };
</script>

<style scoped>
.v-list-item:hover {
  background-color: rgba(var(--v-theme-primary), 0.1);
}
</style>
