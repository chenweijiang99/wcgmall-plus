<script setup lang="ts">
import { ref, onMounted, onUnmounted } from "vue";
import { CheckCircle, X } from "lucide-vue-next";

const countdown = ref(3); // 3秒后自动关闭
let timer: any = null;

const handleClose = () => {
  // 尝试关闭窗口
  // 注意：只有通过脚本(window.open)打开的窗口才能被脚本关闭
  // 在我们的场景中，因为是 handlePay 里 window.open 打开的，所以这里生效
  window.close();
  
  // 如果浏览器拦截了关闭操作（比如用户刷新了页面后），提示用户手动关闭
  if (!window.closed) {
    window.location.href = "about:blank"; // 降级处理：跳转空白页
  }
};

onMounted(() => {
  timer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer);
      handleClose();
    }
  }, 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center bg-gray-50 dark:bg-zinc-900 px-4">
    <div class="bg-white dark:bg-black p-8 rounded-3xl shadow-xl text-center max-w-sm w-full border border-gray-100 dark:border-zinc-800">
      
      <div class="w-20 h-20 bg-green-100 text-green-600 rounded-full flex items-center justify-center mx-auto mb-6 shadow-green-200 shadow-lg">
        <CheckCircle :size="40" stroke-width="3" />
      </div>

      <h1 class="text-2xl font-bold mb-2 text-gray-900 dark:text-white">支付已受理</h1>
      <p class="text-gray-500 dark:text-gray-400 mb-8">
        请返回原窗口查看订单状态<br>
        本页面将在 <span class="text-blue-600 font-bold">{{ countdown }}</span> 秒后自动关闭
      </p>

      <button 
        @click="handleClose"
        class="w-full py-3 px-6 rounded-full bg-gray-100 dark:bg-zinc-800 text-gray-700 dark:text-gray-200 font-medium hover:bg-gray-200 dark:hover:bg-zinc-700 transition-colors flex items-center justify-center gap-2"
      >
        <X :size="18" />
        立即关闭
      </button>
    </div>
  </div>
</template>