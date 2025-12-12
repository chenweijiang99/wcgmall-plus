<template>
  <img v-if="logoUrl" class="logo-img" :src="logoUrl" :width="size" :height="size" alt="Logo" />
  <img v-else class="logo-img" :src="defaultLogo" :width="size" :height="size" alt="Logo" />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

defineProps({
  size: {
    type: [Number, String],
    default: 32,
  },
});

const logoUrl = ref('');
const defaultLogo = new URL("@/assets/logo-header.jpg", import.meta.url).href;

const fetchLogo = async () => {
  try {
    const response = await fetch(import.meta.env.VITE_APP_BASE_API + '/sys/siteConfig/key/site_admin_logo');
    const result = await response.json();
    if (result.code === 200 && result.data) {
      logoUrl.value = result.data;
    }
  } catch (error) {
    console.error('获取Logo失败:', error);
  }
};

onMounted(() => {
  fetchLogo();
});
</script>

<style scoped>
.logo-img {
  transition: transform 0.3s ease;
}

.logo-img:hover {
  transform: scale(1.1);
}
</style>
