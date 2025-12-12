<script setup lang="ts">
import { RouterLink, useRoute } from "vue-router";
import { computed, onMounted } from "vue";
import { useSiteConfigStore } from "@/stores/modules/siteConfig";

const siteConfigStore = useSiteConfigStore();

// 从Store获取配置
const footerLogo = computed(() => siteConfigStore.userFooterLogo);
const footerDescription = computed(() => siteConfigStore.footerDescription);
const footerCopyright = computed(() => siteConfigStore.footerCopyright);
const footerAddress = computed(() => siteConfigStore.footerAddress);
const footerEmail = computed(() => siteConfigStore.footerEmail);
const footerIcp = computed(() => siteConfigStore.footerIcp);

// 默认值
const defaultFooterLogo = '/src/assets/images/logo-footer.jpg';
const defaultDescription = '"文创购"(英文:WCG,亦称文创购物商城、文创商城),是一个综合性文创购物购物网站。';

onMounted(() => {
  siteConfigStore.fetchConfig();
});
</script>
<template>
  <footer
    class="bg-white dark:bg-black border-t border-gray-200 dark:border-zinc-800 py-12 mt-auto"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
        <div>
          <img class="text-lg font-bold mb-4" :src="footerLogo || defaultFooterLogo" />
          <p class="text-gray-500 text-sm">
            {{ footerDescription || defaultDescription }}
          </p>
        </div>
        <div>
          <h4 class="font-semibold mb-4">快速连接</h4>
          <ul class="space-y-2 text-sm text-gray-500 dark:text-gray-400">
            <li>
              <RouterLink to="/">回到主页</RouterLink>
            </li>
            <li>
              <RouterLink to="/blog">博客文章</RouterLink>
            </li>
            <li>
              <RouterLink to="/shop">商品列表</RouterLink>
            </li>
          </ul>
        </div>
        <div>
          <h4 class="font-semibold mb-4">需要帮助</h4>
          <ul class="space-y-2 text-sm text-gray-500 dark:text-gray-400">
            <!-- <li>联系我们</li> -->
            <li>隐私政策</li>
            <li>付款和运输</li>
          </ul>
        </div>
        <div>
          <h4 class="font-semibold mb-4">联系方式</h4>
          <ul class="space-y-2 text-sm text-gray-500 dark:text-gray-400">
            <li v-if="footerAddress">
            <el-icon><Location /></el-icon>
            {{ footerAddress }}</li>
            <li v-if="footerEmail"><el-icon><Message /></el-icon>{{ footerEmail }}</li>
          </ul>
        </div>
      </div>
      <div
        class="mt-12 pt-8 border-t border-gray-100 dark:border-zinc-900 text-center text-xs text-gray-400"
      >
        <div>© {{ new Date().getFullYear() }} {{ footerCopyright || 'WCG Store. All rights reserved.' }}</div>
        <div v-if="footerIcp" class="mt-2">
          <a href="https://beian.miit.gov.cn/" target="_blank" class="hover:text-gray-600">{{ footerIcp }}</a>
        </div>
      </div>
    </div>
  </footer>
</template>
