<script setup lang="ts">
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { useRoute } from 'vue-router';
import { computed } from 'vue';

const route = useRoute();
// 在登录页隐藏导航栏和页脚
const isAuthPage = computed(() => !!route.meta.hideLayout);
</script>

<template>
  <div class="flex flex-col min-h-screen bg-gray-50 text-slate-900 dark:bg-[#0a0a0a] dark:text-gray-100 transition-colors duration-300 font-sans antialiased">
    <Navbar v-if="!isAuthPage" />
    <main class="flex-grow">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <Footer v-if="!isAuthPage" />
    <el-backtop :right="100" :bottom="100" />
  </div>
</template>

<style scoped>
*{
    font-family: 'Handwriting', 'sans-serif';;
}
</style>