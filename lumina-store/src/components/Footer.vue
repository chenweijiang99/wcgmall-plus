<script setup lang="ts">
import { RouterLink, useRoute } from "vue-router";
</script>
<template>
  <footer
    class="bg-white dark:bg-black border-t border-gray-200 dark:border-zinc-800 py-12 mt-auto"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
        <div>
          <img class="text-lg font-bold mb-4" src="/src/assets/images/logo-footer.jpg" />
          <p class="text-gray-500 text-sm">
            “文创购”(英文:WCG,亦称文创购物商城、文创商城),是一个综合性文创购物购物网站。
            “文创购”是长江师范学院计科2301陈韦江毕业设计。提供100%品质保证的商品,7天无理由退货的售后服务,以及购物积分返现等优质服务。
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
            <li>
            <el-icon><Location /></el-icon>
            长江师范学院</li>
            <li><el-icon><Message /></el-icon>1774532899@qq.com</li>
          </ul>
        </div>
      </div>
      <div
        class="mt-12 pt-8 border-t border-gray-100 dark:border-zinc-900 text-center text-xs text-gray-400"
      >
        © {{ new Date().getFullYear() }} WCG Store. All rights reserved.
      </div>
    </div>
  </footer>
</template>
