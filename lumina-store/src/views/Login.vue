<script setup lang="ts">
import { ref, reactive, computed } from "vue";
import router from "@/router";
import { useUserStore } from "@/stores/modules/user";
import { ElMessage } from "element-plus";
import { User, Lock, Mail, ArrowLeft } from "lucide-vue-next";
import type { FormInstance } from "element-plus";
import type { FormItemRule } from "element-plus";
import SliderVerify from "@/components/SliderVerify.vue";
import {
  getCaptchaSwitchApi,
  getAuthRenderApi,
  getEmailCodeApi,
  registerApi,
  forgotPasswordApi,
} from "@/api/auth";
import { emitter } from '@/event/emitter';

type AuthMode = "login" | "register" | "forgot";

// 当前模式状态
const authMode = ref<AuthMode>("login");
const isLoading = ref(false);

// 倒计时状态
const countdown = ref(0);
let timer: number | undefined;

// 登录表单相关
const userStore = useUserStore();
const loginFormRef = ref<FormInstance>();
// 表单数据
const loginForm = reactive({
  username: "",
  password: "",
  rememberMe: false,
  source: "USER",
  nonceStr: "",
  value: "",
});

const showSliderVerify = ref(false);
const sliderVerifyRef = ref();
const registerFormRef = ref<FormInstance>();
const registerForm = reactive({
  username: "",
  nickname: "",
  email: "",
  password: "",
  confirmPassword: "",
  code: "",
});
const forgotFormRef = ref<FormInstance>();
const forgotForm = reactive({
  email: "",
  code: "",
  password: "",
  confirmPassword: "",
});

// 标题和描述计算属性
const pageTitle = computed(() => {
  switch (authMode.value) {
    case "login":
      return "登录到WCG STORE";
    case "register":
      return "创建账户";
    case "forgot":
      return "重置密码";
  }
});

const pageSubtitle = computed(() => {
  switch (authMode.value) {
    case "login":
      return "欢迎回来，请输入您的账户信息";
    case "register":
      return "加入我们的社区";
    case "forgot":
      return "输入您的邮箱重置密码";
  }
});
// 登录表单验证规则
const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "长度在 3 到 50 个字符", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" },
  ],
};

// 注册表单验证规则
const registerRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "长度在 3 到 20 个字符", trigger: "blur" },
  ] as FormItemRule[],
  nickname: [
    { required: true, message: "请输入昵称", trigger: "blur" },
    { min: 3, max: 20, message: "长度在 3 到 20 个字符", trigger: "blur" },
  ] as FormItemRule[],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
  ] as FormItemRule[],
  password: [
    { required: true, message: "请设置密码", trigger: "blur" },
    { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" },
  ] as FormItemRule[],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== registerForm.password) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ] as FormItemRule[],
  code: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { min: 4, max: 6, message: "验证码长度为4-6位", trigger: "blur" },
  ] as FormItemRule[],
};

// 忘记密码表单验证规则
const forgotRules = {
  email: [
    { required: true, message: "请输入注册邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
  ] as FormItemRule[],
  code: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { min: 4, max: 6, message: "验证码长度为4-6位", trigger: "blur" },
  ] as FormItemRule[],
  password: [
    { required: true, message: "请设置新密码", trigger: "blur" },
    { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" },
  ] as FormItemRule[],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== forgotForm.password) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ] as FormItemRule[],
};
// 模拟发送验证码
const sendCode = (email: string) => {
  if (!email) {
    ElMessage.warning("Please enter your email address first.");
    return;
  }
  if (countdown.value > 0) return;

  // 模拟 API 调用
  ElMessage.success(`Verification code sent to ${email} (Mock: 123456)`);

  countdown.value = 60;
  timer = window.setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) clearInterval(timer);
  }, 1000);
};

// 滑动验证刷新
const refresh = () => {
  sliderVerifyRef.value?.refresh();
};
// 滑动验证成功
const onSuccess = (captcha: any) => {
  loginForm.nonceStr = captcha.nonceStr;
  loginForm.value = captcha.value;

  login();
};
// 登录
const login = () => {
  userStore
    .login(loginForm)
    .then(() => {
      sliderVerifyRef?.value?.verifySuccessEvent();
      router.push("/");
      ElMessage.success("登录成功");
      // emitter.emit('refresh');
    })
    .catch(() => {
      refresh();
    }
  );
};

/* 滑动验证失败*/
const onFail = (msg: string) => {
  refresh();
};
/* 滑动验证异常*/
const onAgain = () => {
  ElMessage.error("滑动操作异常，请重试");
};
// 处理登录
const handleLogin = async () => {
  loginFormRef.value?.validate((valid) => {
    if (valid) {
      // 只有在表单验证通过时才检查验证码开关
      getCaptchaSwitchApi().then((res) => {
        if (!res.data || res.data.configValue === "Y") {
          showSliderVerify.value = true;
        } else {
          login();
        }
      });
    }
  });
};

// 处理注册
const handleRegister = () => {};

// 处理重置密码
const handleResetPassword = () => {};

const switchMode = (mode: AuthMode) => {
  authMode.value = mode;
  // 切换时清除验证码倒计时（可选，视需求而定）
};
</script>

<template>
  <div
    class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-black px-4 py-12"
  >
    <div
      class="max-w-md w-full bg-white dark:bg-zinc-900 p-8 rounded-3xl shadow-xl border border-gray-100 dark:border-zinc-800 transition-all duration-300"
    >
      <!-- 头部 -->
      <div class="text-center mb-8">
        <div
          class="w-12 h-12 bg-black dark:bg-white rounded-full mx-auto mb-4 flex items-center justify-center"
        >
          <User
            v-if="authMode === 'register'"
            class="text-white dark:text-black"
            :size="24"
          />
          <Lock
            v-else-if="authMode === 'forgot'"
            class="text-white dark:text-black"
            :size="24"
          />
          <div v-else class="w-3 h-3 bg-white dark:bg-black rounded-full"></div>
        </div>
        <h1 class="text-2xl font-bold animate-fade-in">{{ pageTitle }}</h1>
        <p class="text-gray-500 mt-2 text-sm animate-fade-in">{{ pageSubtitle }}</p>
      </div>

      <!-- Forms Container -->
      <div class="space-y-6">
        <!-- 登录 -->
        <el-form
          ref="loginFormRef"
          v-if="authMode === 'login'"
          :model="loginForm"
          label-position="top"
          size="large"
          class="animate-fade-in"
          :rules="rules"
        >
          <el-form-item label="邮箱">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入邮箱或用户名"
              :prefix-icon="Mail"
            />
          </el-form-item>

          <el-form-item label="密码">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>

          <!--第三方登录 -->
          <div class="mt-8">
            <div class="relative">
              <div class="absolute inset-0 flex items-center">
                <div class="w-full border-t border-gray-200 dark:border-zinc-800"></div>
              </div>
              <div class="relative flex justify-center text-sm">
                <span class="px-2 bg-white dark:bg-zinc-900 text-gray-500"
                  >其他方式登录</span
                >
              </div>
            </div>

            <div class="flex justify-center gap-8 mt-6">
              <div
                class="w-12 h-12 rounded-full border border-gray-200 dark:border-zinc-800 flex items-center justify-center cursor-pointer hover:bg-gray-50 dark:hover:bg-zinc-800 transition-transform hover:scale-110 shadow-sm"
                title="Sign in with Google"
              >
                <svg-icon name="github" />
              </div>

              <!-- WeChat -->
              <div
                class="w-12 h-12 rounded-full border border-gray-200 dark:border-zinc-800 flex items-center justify-center cursor-pointer hover:bg-gray-50 dark:hover:bg-zinc-800 transition-transform hover:scale-110 shadow-sm"
                title="Sign in with WeChat"
              >
                <svg-icon name="wechat" />
              </div>
            </div>
          </div>

          <div class="flex justify-end mb-4">
            <button
              type="button"
              @click="switchMode('forgot')"
              class="text-sm text-blue-600 hover:underline font-medium"
            >
              忘记密码?
            </button>
          </div>

          <el-button
            type="primary"
            :loading="isLoading"
            class="w-full !rounded-full !h-12 !text-lg !font-bold"
            @click="handleLogin"
          >
            登录
          </el-button>

          <div class="mt-6 text-center text-sm text-gray-500">
            还没有账户?
            <button
              type="button"
              @click="switchMode('register')"
              class="text-blue-600 font-medium hover:underline ml-1"
            >
              注册
            </button>
          </div>
        </el-form>

        <!-- 注册-->
        <el-form
          v-else-if="authMode === 'register'"
          :model="registerForm"
          label-position="top"
          size="large"
          class="animate-fade-in"
        >
          <el-form-item label="用户名" required>
            <el-input v-model="registerForm.username" placeholder="jordan_smith" />
          </el-form-item>

          <el-form-item label="昵称">
            <el-input v-model="registerForm.nickname" placeholder="Jordan" />
          </el-form-item>

          <el-form-item label="邮箱" required>
            <el-input
              v-model="registerForm.email"
              type="email"
              placeholder="name@example.com"
            />
          </el-form-item>

          <el-form-item label="验证码" required>
            <div class="flex gap-3">
              <el-input v-model="registerForm.code" placeholder="123456" class="flex-1" />
              <el-button
                type="primary"
                class="!w-32 !rounded-full"
                :disabled="countdown > 0"
                @click="sendCode(registerForm.email)"
              >
                {{ countdown > 0 ? `${countdown}s` : "发送验证码" }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="密码" required>
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="Create a strong password"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" required>
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="Repeat password"
              show-password
            />
          </el-form-item>

          <el-button
            type="primary"
            :loading="isLoading"
            class="w-full !rounded-full !h-12 !text-lg !font-bold mt-2"
            @click="handleRegister"
          >
            注册
          </el-button>

          <div class="mt-6 text-center text-sm text-gray-500">
            已经有账户?
            <button
              type="button"
              @click="switchMode('login')"
              class="text-blue-600 font-medium hover:underline ml-1"
            >
              登录
            </button>
          </div>
        </el-form>

        <!-- 忘记密码 -->
        <el-form
          v-else-if="authMode === 'forgot'"
          :model="forgotForm"
          label-position="top"
          size="large"
          class="animate-fade-in"
        >
          <el-form-item label="邮箱" required>
            <el-input
              v-model="forgotForm.email"
              type="email"
              placeholder="Enter your registered email"
            />
          </el-form-item>

          <el-form-item label="验证码" required>
            <div class="flex gap-3">
              <el-input v-model="forgotForm.code" placeholder="123456" class="flex-1" />
              <el-button
                type="primary"
                class="!w-32 !rounded-full"
                :disabled="countdown > 0"
                @click="sendCode(forgotForm.email)"
              >
                {{ countdown > 0 ? `${countdown}s` : "发送验证码" }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="新密码" required>
            <el-input
              v-model="forgotForm.password"
              type="password"
              placeholder="Enter new password"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" required>
            <el-input
              v-model="forgotForm.confirmPassword"
              type="password"
              placeholder="Confirm new password"
              show-password
            />
          </el-form-item>

          <el-button
            type="primary"
            native-type="submit"
            :loading="isLoading"
            class="w-full !rounded-full !h-12 !text-lg !font-bold mt-2"
          >
            重置密码
          </el-button>

          <div class="mt-6 text-center">
            <button
              type="button"
              @click="switchMode('login')"
              class="text-gray-500 hover:text-black dark:hover:text-white text-sm font-medium flex items-center justify-center mx-auto gap-1"
            >
              <ArrowLeft :size="16" /> 登录
            </button>
          </div>
        </el-form>

        <!-- 滑块验证 -->
        <el-dialog
          title="请拖动滑块完成拼图"
          width="360px"
          v-model="showSliderVerify"
          :close-on-click-modal="false"
          @closed="refresh"
          append-to-body
        >
          <slider-verify
            ref="sliderVerifyRef"
            @success="onSuccess"
            @fail="onFail"
            @again="onAgain"
          />
        </el-dialog>
      </div>
    </div>
  </div>
</template>
<style scoped>
:deep(.el-input__wrapper) {
  border-radius: 20px;
}
</style>
