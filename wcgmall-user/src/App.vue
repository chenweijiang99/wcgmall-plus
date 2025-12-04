<script setup lang="ts">
import Navbar from "@/components/Navbar.vue";
import Footer from "@/components/Footer.vue";
import { useRoute } from "vue-router";
import { computed, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { setToken } from "./utils/auth";
import router from "./router";
import { useUserStore } from "@/stores/modules/user";
const userStore = useUserStore();
const route = useRoute();
// 在登录页隐藏导航栏和页脚
const isAuthPage = computed(() => !!route.meta.hideLayout);

const handleThirdPartyLogin = () => {
  let flag = window.location.href.indexOf("token") != -1;
  if (flag) {
    let token = window.location.href.split("token=")[1];
    setToken(token);
    userStore.getUserInfo();
    ElMessage.success("登录成功");
    // 延长1秒后跳转
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
  </div>
</template>

<style scoped>
* {
  font-family: "Handwriting", "sans-serif";
}
</style>
