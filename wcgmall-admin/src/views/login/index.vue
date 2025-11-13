<template>
  <div class="login-container" :class="{ dark: isDark }">
    <!-- 左侧背景区域 -->
    <div class="login-background">
      <div class="background-wrapper">
        <!-- 添加简单的装饰图形 -->
        <div class="animated-background">
          <div class="gradient-circle"></div>
          <div class="geometric-shapes">
            <div class="shape" v-for="n in 5" :key="n"></div>
          </div>
        </div>

        <!-- Logo和标题区域 -->
        <div class="brand-content">
          <div class="logo-wrapper">
            <Logo :size="80" class="floating-logo" :color="logoColor" />
          </div>
          <h1 class="brand-title">{{ settings.title }}</h1>
          <p class="brand-description">基于 Vue3 + TypeScript 的后台管理系统</p>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区域 -->
    <div class="login-form">
      <div class="form-wrapper">
        <!-- 主题切换按钮 -->
        <div class="theme-switch">
          <el-button class="theme-button" circle @click="toggleTheme">
            <el-icon><component :is="isDark ? 'Sunny' : 'Moon'" /></el-icon>
          </el-button>
        </div>

        <!-- 表单类型切换标签 -->
        <div class="login-tabs">
          <div
            class="tab-item"
            :class="{ active: currentTab === 'login' }"
            @click="switchTab('login')"
          >
            <el-icon><User /></el-icon>
            <span>账号登录</span>
          </div>
          <div
            class="tab-item"
            :class="{ active: currentTab === 'register' }"
            @click="switchTab('register')"
          >
            <el-icon><CirclePlus /></el-icon>
            <span>账号注册</span>
          </div>
          <div
            class="tab-item"
            :class="{ active: currentTab === 'forgot' }"
            @click="switchTab('forgot')"
          >
            <el-icon><HelpFilled /></el-icon>
            <span>忘记密码</span>
          </div>
        </div>

        <!-- 登录表单内容 -->
        <transition name="fade-transform" mode="out-in">
          <!-- 登录表单 -->
          <el-form
            v-if="currentTab === 'login'"
            ref="loginFormRef"
            :model="loginForm"
            :rules="rules"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入邮箱或用户名"
                prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <!-- <div class="login-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <span class="forget-password" @click="switchTab('forgot')">忘记密码？</span>
            </div> -->
            <!-- 社交登录 -->
            <!-- <div class="social-login">
              <div class="divider">其他登录方式</div>
              <div class="social-icons">
                <div class="social-icon" @click="handleSocialLogin('github')">
                  <svg-icon name="github" />
                </div>
                <div class="social-icon" @click="handleSocialLogin('qq')">
                  <svg-icon name="qq" />
                </div>
                <div class="social-icon" @click="handleSocialLogin('gitee')">
                  <svg-icon name="gitee" />
                </div>
                <div class="social-icon" @click="handleSocialLogin('weibo')">
                  <svg-icon name="weibo" />
                </div>
              </div>
            </div> -->

            <el-button
              :loading="loading"
              type="primary"
              class="login-button"
              @click="handleLogin"
            >
              {{ loading ? "登录中..." : "登录" }}
            </el-button>
          </el-form>

          <!-- 注册表单 -->
          <el-form
            v-else-if="currentTab === 'register'"
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            @keyup.enter="handleRegister"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="nickname">
              <el-input
                v-model="registerForm.nickname"
                placeholder="请输入昵称"
                prefix-icon="UserFilled"
              />
            </el-form-item>
            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                type="email"
                placeholder="请输入邮箱"
                prefix-icon="Message"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请设置密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请确认密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item prop="code">
              <el-input
                v-model="registerForm.code"
                placeholder="请输入验证码"
                prefix-icon="CircleCheck"
                style="width: 60%"
              />
              <el-button
                type="primary"
                plain
                @click="getRegisterCaptcha"
                :disabled="countdown > 0"
                style="width: 35%; margin-left: 5%"
              >
                {{ countdown > 0 ? `${countdown}s后重试` : "获取验证码" }}
              </el-button>
            </el-form-item>

            <el-button
              :loading="registerLoading"
              type="primary"
              class="login-button"
              @click="handleRegister"
            >
              {{ registerLoading ? "注册中..." : "注册" }}
            </el-button>
          </el-form>

          <!-- 忘记密码表单 -->
          <el-form
            v-else-if="currentTab === 'forgot'"
            ref="forgotFormRef"
            :model="forgotForm"
            :rules="forgotRules"
            @keyup.enter="handleForgot"
          >
            <el-form-item prop="email">
              <el-input
                v-model="forgotForm.email"
                type="email"
                placeholder="请输入注册邮箱"
                prefix-icon="Message"
              />
            </el-form-item>
            <el-form-item prop="code">
              <el-input
                v-model="forgotForm.code"
                placeholder="请输入验证码"
                prefix-icon="CircleCheck"
                style="width: 60%"
              />
              <el-button
                type="primary"
                plain
                @click="getForgotCaptcha"
                :disabled="forgotCountdown > 0"
                style="width: 35%; margin-left: 5%"
              >
                {{ forgotCountdown > 0 ? `${forgotCountdown}s后重试` : "获取验证码" }}
              </el-button>
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="forgotForm.password"
                type="password"
                placeholder="请设置新密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="forgotForm.confirmPassword"
                type="password"
                placeholder="请确认新密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-button
              :loading="forgotLoading"
              type="primary"
              class="login-button"
              @click="handleForgot"
            >
              {{ forgotLoading ? "重置中..." : "重置密码" }}
            </el-button>
          </el-form>
        </transition>
      </div>

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

      <!-- 页脚版权信息 -->
      <footer class="footer">
        <p>Copyright © River-Admin</p>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import router from "@/router";
import type { FormInstance } from "element-plus";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/store/modules/user";
import { useSettingsStore } from "@/store/modules/settings";
import Logo from "@/layouts/components/Sidebar/Logo.vue";
import settings from "@/config/settings";
import SliderVerify from "./components/SliderVerify.vue";
import type { FormItemRule } from "element-plus";
import {
  getCaptchaSwitchApi,
  getAuthRenderApi,
  getEmailCodeApi,
  registerApi,
  forgotPasswordApi,
} from "@/api/system/auth";
import { ref, reactive, computed, onUnmounted } from "vue";

// 表单类型切换
const currentTab = ref<string>("login");

// 登录表单相关
const userStore = useUserStore();
const settingsStore = useSettingsStore();
const loginFormRef = ref<FormInstance>();
const loading = ref(false);
const rememberMe = ref(false);
const showSliderVerify = ref(false);
const sliderVerifyRef = ref();

// 注册表单相关
const registerFormRef = ref<FormInstance>();
const registerLoading = ref(false);
const countdown = ref(0);
let countdownTimer: number | null = null;

// 忘记密码表单相关
const forgotFormRef = ref<FormInstance>();
const forgotLoading = ref(false);
const forgotCountdown = ref(0);
let forgotCountdownTimer: number | null = null;

// 登录表单数据
const loginForm = reactive({
  username: "",
  password: "",
  rememberMe: false,
  source: "ADMIN",
  nonceStr: "",
  value: "",
});

// 注册表单数据
const registerForm = reactive({
  username: "",
  nickname: "",
  email: "",
  password: "",
  confirmPassword: "",
  code: "",
});

// 忘记密码表单数据
const forgotForm = reactive({
  email: "",
  code: "",
  password: "",
  confirmPassword: "",
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

const isDark = computed(() => settingsStore.theme === "dark");

// 切换表单标签
const switchTab = (tab: string) => {
  currentTab.value = tab;
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
  loading.value = true;
  userStore
    .login(loginForm)
    .then(() => {
      sliderVerifyRef?.value?.verifySuccessEvent();
      router.push("/");
      ElMessage.success("登录成功");
    })
    .catch(() => {
      refresh();
    })
    .finally(() => {
      loading.value = false;
    });
};

/* 滑动验证失败*/
const onFail = (msg: string) => {
  refresh();
};
/* 滑动验证异常*/
const onAgain = () => {
  ElMessage.error("滑动操作异常，请重试");
};
// 切换主题
const toggleTheme = () => {
  const newTheme = isDark.value ? "light" : "dark";
  settingsStore.saveSettings({ theme: newTheme });
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
// 处理第三方登录
const handleSocialLogin = (type: string) => {
  getAuthRenderApi(type).then((res) => {
    // 在新窗口中打开第三方登录页面，设置合适的尺寸和位置
    const width = 600;
    const height = 800;
    const left = (window.screen.width - width) / 2;
    const top = (window.screen.height - height) / 2;

    window.open(
      res.data,
      "_blank",
      `width=${width},height=${height},left=${left},top=${top},resizable=yes,scrollbars=yes`
    );
  });
};

// 获取注册验证码
const getRegisterCaptcha = async () => {
  if (!registerForm.email) {
    ElMessage.warning("请先输入邮箱");
    return;
  }

  getEmailCodeApi(registerForm.email).then(() => {
    ElMessage.success("验证码已发送到您的邮箱");
  });

  // 倒计时逻辑
  countdown.value = 60;
  if (countdownTimer) clearInterval(countdownTimer);
  countdownTimer = window.setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      if (countdownTimer) clearInterval(countdownTimer);
      countdownTimer = null;
    }
  }, 1000);
};

// 获取忘记密码验证码
const getForgotCaptcha = async () => {
  if (!forgotForm.email) {
    ElMessage.warning("请先输入邮箱");
    return;
  }
  getEmailCodeApi(forgotForm.email).then(() => {
    ElMessage.success("验证码已发送到您的邮箱");
  });

  // 倒计时逻辑
  forgotCountdown.value = 60;
  if (forgotCountdownTimer) clearInterval(forgotCountdownTimer);
  forgotCountdownTimer = window.setInterval(() => {
    forgotCountdown.value--;
    if (forgotCountdown.value <= 0) {
      if (forgotCountdownTimer) clearInterval(forgotCountdownTimer);
      forgotCountdownTimer = null;
    }
  }, 1000);
};

// 处理注册
const handleRegister = async () => {
  registerFormRef.value?.validate(async (valid) => {
    if (valid) {
      registerLoading.value = true;
      registerApi(registerForm).then(() => {
        ElMessage.success("注册成功，请登录");
        switchTab("login");
      });
      // 清空注册表单
      Object.keys(registerForm).forEach((key) => {
        (registerForm as any)[key] = "";
      });
      registerLoading.value = false;
    }
  });
};

// 处理忘记密码
const handleForgot = async () => {
  forgotFormRef.value?.validate(async (valid) => {
    if (valid) {
      forgotLoading.value = true;
      forgotPasswordApi(forgotForm).then(() => {
        ElMessage.success("重置密码成功，请登录");
        switchTab("login");
      });

      // 清空忘记密码表单
      Object.keys(forgotForm).forEach((key) => {
        (forgotForm as any)[key] = "";
      });

      forgotLoading.value = false;
    }
  });
};

// 清理定时器
onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer);
  if (forgotCountdownTimer) clearInterval(forgotCountdownTimer);
});

// 添加 logo 颜色计算
const logoColor = computed(() => {
  return isDark.value ? "#4ecdc4" : "#ff6b6b";
});
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: var(--el-bg-color);
}

/* 左侧背景区域样式 */
.login-background {
  flex: 1;
  position: relative;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  overflow: hidden;

  .background-wrapper {
    position: relative;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    /* 添加动态背景效果 */
    &::before {
      content: "";
      position: absolute;
      inset: -50%;
      background-image: repeating-linear-gradient(
          0deg,
          transparent,
          transparent 2px,
          rgba(33, 150, 243, 0.1) 3px,
          rgba(33, 150, 243, 0.1) 3px
        ),
        repeating-linear-gradient(
          90deg,
          transparent,
          transparent 2px,
          rgba(33, 150, 243, 0.1) 3px,
          rgba(33, 150, 243, 0.1) 3px
        );
      background-size: 30px 30px;
      transform: perspective(500px) rotateX(60deg);
      animation: matrixMove 20s linear infinite;
    }

    &::after {
      content: "";
      position: absolute;
      inset: 0;
      background: radial-gradient(
          circle at 30% 30%,
          rgba(33, 150, 243, 0.3) 0%,
          transparent 50%
        ),
        radial-gradient(circle at 70% 70%, rgba(3, 169, 244, 0.3) 0%, transparent 50%);
      filter: blur(30px);
      animation: glowPulse 8s ease-in-out infinite alternate;
    }
  }

  /* 品牌内容样式优化 */
  .brand-content {
    position: relative;
    z-index: 1;
    text-align: center;
    padding: 40px;

    .logo-wrapper {
      margin-bottom: 30px;
      position: relative;
      &::before {
        content: "";
        position: absolute;
        inset: -30px;
        border: 2px solid rgba(33, 150, 243, 0.3);
        border-radius: 50%;
        animation: rotateBorder 10s linear infinite;
      }

      &::after {
        content: "";
        position: absolute;
        inset: -15px;
        background: radial-gradient(circle, rgba(33, 150, 243, 0.4), transparent 70%);
        filter: blur(15px);
        animation: glowPulse 4s ease-in-out infinite;
      }
    }

    .brand-title {
      font-size: 36px;
      font-weight: bold;
      color: #1565c0;
      margin-bottom: 16px;
      text-shadow: 0 0 10px rgba(33, 150, 243, 0.5);
    }

    .brand-description {
      font-size: 16px;
      color: #1976d2;
      max-width: 400px;
      margin: 0 auto;
      line-height: 1.6;
    }
  }
}

/* 移除之前的动画相关样式 */
.animated-background,
.gradient-circle,
.geometric-shapes {
  display: none;
}

/* 右侧登录表单区域样式 */
.login-form {
  width: 500px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: var(--el-bg-color);
  position: relative;
  box-shadow: -10px 0 20px rgba(0, 0, 0, 0.05);

  .form-wrapper {
    max-width: 400px;
    margin: 0 auto;
    width: 100%;
  }

  /* 登录方式切换 */
  .login-tabs {
    display: flex;
    margin-bottom: 30px;
    background: var(--el-fill-color-light);
    padding: 5px;
    border-radius: 12px;
    position: relative;

    .tab-item {
      flex: 1;
      padding: 12px;
      text-align: center;
      cursor: pointer;
      border-radius: 8px;
      transition: all 0.3s;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: var(--el-text-color-secondary);

      &.active {
        background: var(--el-bg-color);
        color: var(--el-color-primary);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }

      .el-icon {
        font-size: 18px;
      }
    }
  }

  /* 表单项样式 */
  :deep(.el-form-item) {
    margin-bottom: 24px;

    .el-input__wrapper {
      padding: 0 15px;
      height: 44px;
      border-radius: 8px;
      background: var(--el-fill-color-light);
      border: 2px solid transparent;
      transition: all 0.3s;

      &:hover,
      &.is-focus {
        border-color: var(--el-color-primary);
        background: var(--el-bg-color);
      }

      .el-input__inner {
        font-size: 14px;
      }
    }
  }

  /* 登录按钮 */
  .login-button {
    width: 100%;
    height: 44px;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 500;
    margin-top: 10px;
    background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
    border: none;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 5px 15px rgba(255, 107, 107, 0.3);
    }
  }

  /* 二维码登录样式 */
  .qrcode-box {
    text-align: center;
    padding: 30px 0;

    .qrcode-wrapper {
      width: 200px;
      height: 200px;
      margin: 0 auto;
      padding: 15px;
      background: white;
      border-radius: 12px;
      position: relative;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

      .qrcode-scanner {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 2px;
        background: var(--el-color-primary);
        animation: scan 2s linear infinite;
      }

      .qrcode-img {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }

      .qrcode-mask {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(255, 255, 255, 0.9);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 12px;
        border-radius: 12px;
      }
    }

    .qrcode-tip {
      margin-top: 20px;
      color: var(--el-text-color-secondary);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
    }
  }

  /* 社交登录样式 */
  .social-login {
    margin-top: 40px;

    .divider {
      display: flex;
      align-items: center;
      color: var(--el-text-color-secondary);
      font-size: 14px;
      margin-bottom: 20px;

      &::before,
      &::after {
        content: "";
        flex: 1;
        height: 1px;
        background: var(--el-border-color);
        margin: 0 16px;
      }
    }

    .social-icons {
      display: flex;
      justify-content: center;
      gap: 20px;

      .social-icon {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.3s;
        background: var(--el-fill-color-light);
        color: var(--el-text-color-regular);

        &:hover {
          transform: translateY(-2px);
          background: var(--el-color-primary-light-9);
          color: var(--el-color-primary);
        }
      }
    }
  }

  /* 页脚样式 */
  .footer {
    text-align: center;
    color: var(--el-text-color-secondary);
    font-size: 14px;

    a {
      color: inherit;
      text-decoration: none;

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }
}

/* 动画相关 */
@keyframes scan {
  0% {
    top: 0;
  }
  50% {
    top: calc(100% - 2px);
  }
  100% {
    top: 0;
  }
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

@keyframes rotate {
  0% {
    transform: rotate(0);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 主题切换按钮样式 */
.theme-switch {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;

  .theme-button {
    width: 40px;
    height: 40px;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    border: none;
    color: var(--el-text-color-primary);
    transition: all 0.3s;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
      transform: translateY(-2px);
    }
  }
}

/* 登录选项样式 */
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;

  .el-checkbox {
    color: var(--el-text-color-regular);
  }

  .forget-password {
    color: var(--el-text-color-regular);
    text-decoration: none;
    font-size: 14px;
    transition: all 0.3s;

    &:hover {
      color: var(--el-color-primary);
    }
  }
}

/* 深色模式适配 */
.dark {
  .login-background {
    background: linear-gradient(135deg, #4f4f50 0%, #30343a 100%);

    .brand-title {
      color: #90caf9;
    }

    .brand-description {
      color: #bbdefb;
    }
  }

  .login-form {
    background: var(--el-bg-color-overlay);

    .login-tabs {
      background: rgba(255, 255, 255, 0.05);

      .tab-item.active {
        background: rgba(0, 0, 0, 0.3);
      }
    }

    :deep(.el-input__wrapper) {
      background: rgba(0, 0, 0, 0.2);

      &:hover,
      &.is-focus {
        background: rgba(0, 0, 0, 0.3);
      }
    }

    .qrcode-wrapper {
      background: var(--el-bg-color);
    }

    .social-icon {
      background: rgba(255, 255, 255, 0.05);

      &:hover {
        background: rgba(255, 255, 255, 0.1);
      }
    }
    
  }

  .theme-switch {
    .theme-button {
      background: rgba(0, 0, 0, 0.2);

      &:hover {
        background: rgba(0, 0, 0, 0.3);
      }
    }
  }

  .logo-wrapper {
    :deep(svg path) {
      fill: #063155;
    }
  }
  /* 登录按钮深色模式样式 */
  .login-button {
    background: linear-gradient(45deg, #43505c, #276792);
    
    &:hover {
      box-shadow: 0 5px 15px rgba(93, 213, 199, 0.3);
    }
  }
  /* 验证码按钮深色模式样式 */
  :deep(.el-button) {
    &.is-plain {
      background: rgba(0, 0, 0, 0.2);
      border-color: var(--el-color-primary);
      color: var(--el-color-primary);

      &:hover {
        background: rgba(0, 0, 0, 0.3);
        border-color: var(--el-color-primary-light-3);
        color: var(--el-color-primary-light-3);
      }

      &[disabled] {
        background: rgba(0, 0, 0, 0.1);
        border-color: var(--el-border-color);
        color: var(--el-text-color-placeholder);
      }
    }
  }
}

/* 更新动画 */
@keyframes matrixMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 0 30px;
  }
}

@keyframes glowPulse {
  0%,
  100% {
    opacity: 0.3;
    transform: scale(0.95);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.05);
  }
}

@keyframes rotateBorder {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
