<script setup lang="ts">
import Navbar from "@/components/Navbar.vue";
import Footer from "@/components/Footer.vue";
import ChatDialog from "@/components/ChatDialog.vue";
import { useRoute } from "vue-router";
import { computed, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { setToken } from "./utils/auth";
import router from "./router";
import { useUserStore } from "@/stores/modules/user";
import { emitter } from "@/event/emitter";
const userStore = useUserStore();
const route = useRoute();
// 在登录页隐藏导航栏和页脚
const isAuthPage = computed(() => !!route.meta.hideLayout);

/**
 * 处理第三方登录回调
 * 第三方登录成功后，后端会重定向到前端并携带 token 参数
 */
const handleThirdPartyLogin = () => {
  const url = new URL(window.location.href);
  const token = url.searchParams.get("token");
  const errorCode = url.searchParams.get("code");
  const errorMessage = url.searchParams.get("message");

  // 处理登录错误
  if (errorCode && errorCode !== "200") {
    ElMessage.error(errorMessage || "第三方登录失败");
    // 清除URL参数
    window.history.replaceState({}, document.title, url.pathname);
    router.push("/login");
    return;
  }

  // 处理登录成功
  if (token) {
    setToken(token);
    userStore.getUserInfo();
    // 触发导航栏数据刷新（购物车、收藏夹）
    emitter.emit("refresh");
    ElMessage.success("登录成功");
    // 清除URL参数，避免刷新页面时重复处理
    window.history.replaceState({}, document.title, "/");
    router.push("/");
  }
};

onMounted(() => {
  handleThirdPartyLogin();
});
</script>

<template>
  <div
    class="flex flex-col min-h-screen bg-gray-50 text-slate-900 dark:bg-[#0a0a0a] dark:text-gray-100 transition-colors duration-300 font-sans antialiased"
  >
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
    <ChatDialog />
  </div>
</template>

<style scoped>
* {
  font-family: "Handwriting", "sans-serif";
}
</style>
