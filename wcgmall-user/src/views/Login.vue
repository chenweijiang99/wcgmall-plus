<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import router from "@/router";
import { useUserStore } from "@/stores/modules/user";
import { ElMessage, ElNotification } from "element-plus";
import { User, Lock, Mail, ArrowLeft } from "lucide-vue-next";
import type { FormInstance } from "element-plus";
import type { FormItemRule } from "element-plus";
import SliderVerify from "@/components/SliderVerify.vue";
import {
  getCaptchaSwitchApi,
  getEmailCodeApi,
  registerApi,
  forgotPasswordApi,
  checkUsernameApi,
  checkEmailApi,
  getSocialLoginUrlApi,
  getEnabledSocialConfigApi,
  getSocialSettingsApi,
  type SocialConfigItem,
} from "@/api/auth";
import { emitter } from "@/event/emitter";

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

const loginTypes = {
  qq: {
    title: "QQ账号登录",
    icon: "qq",
    type: 1,
  },
  wechat: {
    title: "微信扫码登录",
    icon: "wechat",
    type: 2,
  },
  alipay: {
    title: "支付宝扫码登录",
    icon: "alipay",
    type: 3,
  },
  weibo: {
    title: "微博账号登录",
    icon: "weibo",
    type: 4,
  },
  baidu: {
    title: "百度账号登录",
    icon: "baidu",
    type: 5,
  },
  huawei: {
    title: "华为账号登录",
    icon: "huawei",
    type: 6,
  },
  xiaomi: {
    title: "小米账号登录",
    icon: "xiaomi",
    type: 7,
  },
  microsoft: {
    title: "微软账号登录",
    icon: "microsoft",
    type: 8,
  },
  dingding: {
    title: "钉钉账号登录",
    icon: "dingding",
    type: 9,
  },
  gitee: {
    title: "Gitee账号登录",
    icon: "gitee",
    type: 10,
  },
  github: {
    title: "GitHub账号登录",
    icon: "github",
    type: 11,
  },
  douyin: {
    title: "抖音账号登录",
    icon: "douyin",
    type: 12,
  },
};

// 第三方登录配置（从后端获取）
const enabledLoginTypes = ref<SocialConfigItem[]>([]);
// 第三方登录全局设置
const socialSettings = ref({
  enabled: true,
  loginMode: 'juhe'
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
    {
      validator: (rule: any, value: string, callback: any) => {
        if (!value) {
          callback();
          return;
        }
        handleCheckUsername(value)
          .then(() => {
            callback();
          })
          .catch((error) => {
            callback(new Error(error.message));
          });
      },
      trigger: "blur",
    },
  ] as FormItemRule[],
  nickname: [
    { required: true, message: "请输入昵称", trigger: "blur" },
    { min: 3, max: 20, message: "长度在 3 到 20 个字符", trigger: "blur" },
  ] as FormItemRule[],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (!value) {
          callback();
          return;
        }
        handleCheckEmail(value)
          .then(() => {
            callback();
          })
          .catch((error) => {
            callback(new Error(error.message));
          });
      },
      trigger: "blur",
    },
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

/**
 * 第三方登录 - 使用统一接口
 */
const handleThirdPartyLogin = async (item: SocialConfigItem) => {
  try {
    const res = await getSocialLoginUrlApi(item.socialType);
    if (res.code !== 200) {
      ElMessage.error(res.message || "获取登录链接失败");
      return;
    }
    const data = res.data;
    if (data.error) {
      ElMessage.error(data.error);
      return;
    }
    // 跳转到登录URL
    if (data.loginUrl) {
      window.open(data.loginUrl, "_self");
    }
  } catch (error) {
    console.error("获取登录链接失败:", error);
    ElMessage.error("获取登录链接失败");
  }
};

/**
 * 获取第三方登录配置
 */
const getThirdLoginConfig = async () => {
  try {
    // 获取全局设置
    const settingsRes = await getSocialSettingsApi();
    if (settingsRes.code === 200 && settingsRes.data) {
      socialSettings.value = settingsRes.data;
    }

    // 如果第三方登录未开启，不加载登录方式
    if (!socialSettings.value.enabled) {
      enabledLoginTypes.value = [];
      return;
    }

    // 获取启用的登录方式
    const res = await getEnabledSocialConfigApi();
    if (res.code === 200 && res.data) {
      enabledLoginTypes.value = res.data;
    }
  } catch (error) {
    console.error("获取第三方登录配置失败:", error);
  }
};

onMounted(() => {
  getThirdLoginConfig();
});

const handleCheckUsername = async (username: string) => {
  const res = await checkUsernameApi(username);
  if (res.data === true) {
    return Promise.reject(new Error("用户名已存在"));
  } else {
    return Promise.resolve();
  }
};

const handleCheckEmail = async (email: string) => {
  const res = await checkEmailApi(email);
  if (res.data === true) {
    return Promise.reject(new Error("邮箱已存在"));
  } else {
    return Promise.resolve();
  }
};

const validateEmail = () => {
  if (registerFormRef.value && registerForm.email) {
    registerFormRef.value.validateField("email");
  }
};

const validateUsername = () => {
  if (registerFormRef.value && registerForm.username) {
    registerFormRef.value.validateField("username");
  }
};

// 密码强度计算
const passwordStrength = computed(() => {
  const pwd = registerForm.password;
  if (!pwd) return 0;
  let score = 0;
  if (pwd.length >= 6) score++;
  if (/[A-Z]/.test(pwd)) score++;
  if (/[a-z]/.test(pwd)) score++;
  if (/[0-9]/.test(pwd)) score++;
  if (/[^A-Za-z0-9]/.test(pwd)) score++;
  return Math.min(score, 4);
});

const passwordStrengthText = computed(() => {
  const scores = ["", "弱", "中", "强", "极强"];
  return scores[passwordStrength.value];
});

const passwordStrengthColor = computed(() => {
  const colors = [
    "bg-gray-200",
    "bg-red-500",
    "bg-yellow-500",
    "bg-blue-500",
    "bg-green-500",
  ];
  return colors[passwordStrength.value];
});

// 发送验证码
const sendCode = async (email: string) => {
  if (!email) {
    ElMessage.warning("请输入邮箱地址");
    return;
  }
  // 简单的邮箱格式验证
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    ElMessage.warning("请输入有效的邮箱地址");
    return;
  }

  if (countdown.value > 0) return;

  try {
    await getEmailCodeApi(email);
    ElMessage.success(`验证码已发送至 ${email}`);

    countdown.value = 60;
    timer = window.setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        clearInterval(timer);
        timer = undefined;
      }
    }, 1000);
  } catch (error: any) {
    console.error(error);
    ElMessage.error(error.response?.data?.message || "验证码发送失败");
  }
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
      // ElMessage.success("登录成功");
      ElNotification.success({
        title: "登录成功",
        message: "欢迎回来",
      });
      // emitter.emit('refresh');
    })
    .catch(() => {
      refresh();
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
const handleRegister = async () => {
  if (!registerFormRef.value) return;

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      isLoading.value = true;
      try {
        await registerApi({
          username: registerForm.username,
          nickname: registerForm.nickname,
          email: registerForm.email,
          password: registerForm.password,
          code: registerForm.code,
        });

        ElMessage.success("注册成功，请登录");
        switchMode("login");
      } catch (error: any) {
        console.error(error);
        ElMessage.error(error.response?.data?.message || "注册失败");
      } finally {
        isLoading.value = false;
      }
    }
  });
};

// 处理重置密码
const handleResetPassword = async () => {
  if (!forgotFormRef.value) return;

  await forgotFormRef.value.validate(async (valid) => {
    if (valid) {
      isLoading.value = true;
      try {
        await forgotPasswordApi({
          email: forgotForm.email,
          code: forgotForm.code,
          password: forgotForm.password,
        });

        ElMessage.success("密码重置成功，请登录");
        switchMode("login");
      } catch (error: any) {
        console.error(error);
        ElMessage.error(error.response?.data?.message || "重置密码失败");
      } finally {
        isLoading.value = false;
      }
    }
  });
};

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
          <el-form-item label="邮箱" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入邮箱或用户名"
              :prefix-icon="Mail"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>

          <!--第三方登录 -->
          <div class="mt-8" v-if="socialSettings.enabled && enabledLoginTypes.length > 0">
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

            <div class="grid grid-cols-4 gap-4 mt-6 justify-items-center">
              <div v-for="item in enabledLoginTypes" :key="item.id" @click="handleThirdPartyLogin(item)" class="flex justify-center">
                <el-tooltip :content="item.socialName + '登录'" placement="top">
                  <div
                    class="w-12 h-12 rounded-full border border-gray-200 dark:border-zinc-800 flex items-center justify-center cursor-pointer hover:bg-gray-50 dark:hover:bg-zinc-800 transition-transform hover:scale-110 shadow-sm"
                  >
                    <div class="social-icon" v-if="item.icon" v-html="item.icon"></div>
                    <span v-else class="text-gray-400 text-xs">{{ item.socialType }}</span>
                  </div>
                </el-tooltip>
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
          ref="registerFormRef"
          :rules="registerRules"
          label-position="top"
          size="large"
          class="animate-fade-in"
        >
          <el-form-item prop="username" label="用户名" required>
            <el-input
              v-model="registerForm.username"
              placeholder="注册后不可修改"
              @blur="validateUsername"
            />
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="registerForm.nickname" placeholder="请输入昵称" />
          </el-form-item>

          <el-form-item label="邮箱" required prop="email">
            <el-input
              v-model="registerForm.email"
              type="email"
              placeholder="请输入邮箱"
              @blur="validateEmail"
            />
          </el-form-item>

          <el-form-item label="验证码" required prop="code">
            <div class="flex gap-3">
              <el-input
                v-model="registerForm.code"
                placeholder="请输入验证码"
                class="flex-1"
              />
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

          <el-form-item label="密码" required prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
            />
            <!-- 密码强度条 -->
            <div class="mt-2" v-if="registerForm.password">
              <div class="flex justify-between text-xs mb-1">
                <span class="text-gray-500">密码强度</span>
                <span
                  :class="{
                    'text-red-500': passwordStrength <= 1,
                    'text-yellow-500': passwordStrength === 2,
                    'text-blue-500': passwordStrength === 3,
                    'text-green-500': passwordStrength === 4,
                  }"
                >
                  {{ passwordStrengthText }}
                </span>
              </div>
              <div class="h-1.5 w-full bg-gray-200 rounded-full overflow-hidden">
                <div
                  class="h-full transition-all duration-300"
                  :class="passwordStrengthColor"
                  :style="{ width: `${(passwordStrength / 4) * 100}%` }"
                ></div>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="确认密码" required prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
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
          :rules="forgotRules"
          ref="forgotFormRef"
          label-position="top"
          size="large"
          class="animate-fade-in"
        >
          <el-form-item label="邮箱" required prop="email">
            <el-input
              v-model="forgotForm.email"
              type="email"
              placeholder="请输入你注册账户的邮箱"
            />
          </el-form-item>

          <el-form-item label="验证码" required prop="code">
            <div class="flex gap-3">
              <el-input
                v-model="forgotForm.code"
                placeholder="请输入验证码"
                class="flex-1"
              />
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

          <el-form-item label="新密码" required prop="password">
            <el-input
              v-model="forgotForm.password"
              type="password"
              placeholder="输入你的新密码"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" required prop="confirmPassword">
            <el-input
              v-model="forgotForm.confirmPassword"
              type="password"
              placeholder="请确认新密码"
              show-password
            />
          </el-form-item>

          <el-button
            type="primary"
            native-type="submit"
            :loading="isLoading"
            class="w-full !rounded-full !h-12 !text-lg !font-bold mt-2"
            @click="handleResetPassword"
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
.social-icon {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.social-icon :deep(svg) {
  width: 100%;
  height: 100%;
}
</style>
