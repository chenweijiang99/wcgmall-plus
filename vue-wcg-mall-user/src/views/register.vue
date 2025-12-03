<script setup>
import { computed, watch, ref, reactive } from "vue";
import { useScrollToTop } from "@/assets/base.js";
import { ElMessageBox, ElMessage } from "element-plus";
import MyButton from "@/components/Button/index.vue";
import MyInput from "@/components/Input/index.vue";
import { userGetCode, userRegisterService } from "@/api/user.js";
import { useRouter } from "vue-router";
import useUserInfoStore from "@/stores/userInfo";

const userInfoStore = useUserInfoStore();
const router = useRouter();
const { showLoader, backtopStyle, handleMouseEnter, handleMouseLeave } = useScrollToTop();
// 注册表单数据
const registerData = ref({
  username: "",
  nickname: "",
  email: "",
  password: "",
  confirmPassword: "",
  code: "",
});
const formRef = ref(null);
const countdown = ref(0);
let countdownTimer = null;

// 密码强度计算
const passwordStrength = computed(() => {
  const password = registerData.value.password;
  if (!password) return { percentage: 0, status: "", text: "" };

  let strength = 0;
  let feedback = [];

  // 长度检查
  if (password.length >= 6) strength += 25;
  else feedback.push("至少6个字符");

  // 包含数字
  if (/\d/.test(password)) strength += 25;
  else feedback.push("包含数字");

  // 包含小写字母
  if (/[a-z]/.test(password)) strength += 25;
  else feedback.push("包含小写字母");

  // 包含大写字母或特殊字符
  if (/[A-Z]/.test(password) || /[!@#$%^&*(),.?":{}|<>]/.test(password)) strength += 25;
  else feedback.push("包含大写字母或特殊字符");

  // 根据强度返回状态和文本
  if (strength < 50) {
    return {
      percentage: strength,
      status: "exception",
      text: "弱 (" + feedback.join(", ") + ")",
    };
  } else if (strength < 75) {
    return {
      percentage: strength,
      status: "warning",
      text: "中等 (还需改进)",
    };
  } else {
    return {
      percentage: strength,
      status: "success",
      text: "强",
    };
  }
});
// 注册表单验证规则
const registerRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 10, message: "长度在 3 到 10 个字符", trigger: "blur" },
  ],
  nickname: [
    { required: true, message: "请输入昵称", trigger: "blur" },
    { min: 3, max: 10, message: "长度在 3 到 10 个字符", trigger: "blur" },
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请设置密码", trigger: "blur" },
    { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== registerData.value.password) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
  code: [{ required: true, message: "请输入验证码", trigger: "blur" }],
};

// 获取注册验证码
const getRegisterCaptcha = async () => {
  if (!registerData.value.email) {
    ElMessage.warning("请先输入邮箱");
    return;
  }

  userGetCode(registerData.value.email).then(() => {
    ElMessage.success("验证码已发送到您的邮箱");
  });

  // 倒计时逻辑
  countdown.value = 120;
  if (countdownTimer) clearInterval(countdownTimer);
  countdownTimer = window.setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      if (countdownTimer) clearInterval(countdownTimer);
      countdownTimer = null;
    }
  }, 1000);
};
const handleRegister = async () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      const result = await userRegisterService(registerData.value);
      if (result.code === 200) {
        ElMessage({
          showClose: true,
          type: "success",
          message: "注册成功，请登录",
          plain: true,
        });
        router.push("/login");
        // 清空注册表单
        Object.keys(registerData).forEach((key) => {
          registerData[key] = "";
        });
      } else {
        ElMessage({
          showClose: true,
          type: "error",
          message: result.message ? result.message : "登录失败",
          plain: true,
        });
      }
    }
  });
};
</script>

<template>
  <div class="container header-mt">
    <div class="row">
      <div class="col-12">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb custom-breadcrumb">
            <li class="breadcrumb-item">
              <router-link to="/index"> 主页 </router-link>
            </li>
            <li class="breadcrumb-item active" aria-current="page">注册</li>
          </ol>
        </nav>
      </div>
    </div>
  </div>

  <div class="main-content pb-100">
    <div class="container">
      <div class="row">
        <div class="col-lg-6 col-md-8 col-12 mx-auto">
          <div class="custom-form custom-form--box">
            <h3 class="custom-form__title">注册账户</h3>
            <el-form
              :model="registerData"
              ref="formRef"
              :rules="registerRules"
              label-position="top"
            >
              <el-form-item label="用户名" prop="username">
                <my-input
                  v-model="registerData.username"
                  placeholder="输入您的用户名"
                  clearable
                  prefix-icon="User"
                >
                </my-input>
              </el-form-item>
              <el-form-item label="昵称" prop="nickname">
                <my-input
                  v-model="registerData.nickname"
                  placeholder="输入您的昵称"
                  clearable
                  prefix-icon="UserFilled"
                >
                </my-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <my-input
                  v-model="registerData.password"
                  type="password"
                  placeholder="输入您的密码"
                  clearable
                  show-password
                  prefix-icon="Lock"
                >
                </my-input>
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <my-input
                  v-model="registerData.confirmPassword"
                  type="password"
                  placeholder="请确认密码"
                  clearable
                  show-password
                  prefix-icon="Lock"
                >
                </my-input>
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <my-input
                  v-model="registerData.email"
                  type="email"
                  placeholder="输入您的邮箱"
                  clearable
                  prefix-icon="Message"
                >
                </my-input>
              </el-form-item>
              <el-form-item label="验证码" prop="code">
                <my-input
                  v-model="registerData.code"
                  placeholder="请输入验证码"
                  prefix-icon="CircleCheck"
                  style="width: 60%"
                ></my-input>
                <my-button
                  type="primary"
                  :disabled="countdown > 0"
                  style="width: 35%; margin-left: 5%"
                  @click="getRegisterCaptcha"
                >
                  {{ countdown > 0 ? `${countdown}s后重试` : "获取验证码" }}
                </my-button>
              </el-form-item>

              <div class="form-group custom-form__input">
                <label
                  class="password-strength-label"
                  :class="{
                    'password-strength-weak': passwordStrength.status === 'exception',
                    'password-strength-medium': passwordStrength.status === 'warning',
                    'password-strength-strong': passwordStrength.status === 'success',
                  }"
                >
                  {{ passwordStrength.text }}
                </label>

                <el-progress
                  v-show="registerData.password"
                  :percentage="passwordStrength.percentage"
                  :status="passwordStrength.status"
                  style="width: 100%"
                />
              </div>

              <div class="custom-form__btn">
                <my-button type="primary" @click="handleRegister">注册</my-button>
              </div>
              <div class="custom-form__footer">
                <div class="custom-form__footer--link">
                  <h6>已经拥有账户?</h6>
                  <router-link to="/login" class="btn">登录</router-link>
                </div>
              </div>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>

  <el-backtop
    :right="100"
    :bottom="100"
    :style="backtopStyle"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  />

  <div class="loader" v-if="showLoader">
    <div class="spinner">
      <div class="cube1"></div>
      <div class="cube2"></div>
    </div>
  </div>

</template>

<style scoped>
</style>
