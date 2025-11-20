<script setup>
import { useScrollToTop } from "@/assets/base.js";
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import emitter from "@/event/eventBus.js";
import SliderVerify from "@/components/SliderVerify.vue";
import { loginApi, getCaptchaSwitchApi } from "@/api/auth.js";
import { useTokenStore } from "@/stores/token.js";
import { useRouter } from "vue-router";
import { userLoginService, userGetJuHeAuthService } from "@/api/user.js";
import MyButton from "@/components/Button/index.vue";
import MyInput from "@/components/Input/index.vue";
const { showLoader, backtopStyle, handleMouseEnter, handleMouseLeave } = useScrollToTop();
const router = useRouter();
const tokenStore = useTokenStore();
const showSliderVerify = ref(false);
const sliderVerifyRef = ref();

const loginForm = ref({
  username: "",
  password: "",
  rememberMe: false,
  source: "USER",
  nonceStr: "",
  value: "",
});
// 表单引用
const formRef = ref(null);
// 表单验证规则
const rules = {
  username: [
    { required: true, message: "请输入邮箱或用户名", trigger: "blur" },
    { min: 3, max: 20, message: "长度在 3 到 50 个字符", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" },
  ],
};

// 处理登录
const handleLogin = async () => {
  formRef.value?.validate((valid) => {
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
//登录
const login = async () => {
  // let result = await userLoginService(loginData.value);
  console.log(loginForm.value);

  let result = await loginApi(loginForm.value);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message: "登录成功",
      plain: true,
    });
    tokenStore.setToken(result.data.token);
    emitter.emit("refresh");
    emitter.emit("login");
    router.push("/index");
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "登录失败",
      plain: true,
    });
  }
};

const loginWithJuHe = (type) => {
  userGetJuHeAuthService(type).then((res) => {
    if (res.code === 200) {
      window.open(res.logurl, "_self");
    }
  });
};
// 滑动验证刷新
const refresh = () => {
  sliderVerifyRef.value?.refresh();
};
// 滑动验证成功
const onSuccess = (captcha) => {
  loginForm.value.nonceStr = captcha.nonceStr;
  loginForm.value.value = captcha.value;
  login();
};
/* 滑动验证失败*/
const onFail = (msg) => {
  refresh();
};
/* 滑动验证异常*/
const onAgain = () => {
  ElMessage.error("滑动操作异常，请重试");
};
</script>
<template>
  <!-- 面包屑导航-->
  <div class="container header-mt">
    <div class="row">
      <div class="col-12">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb custom-breadcrumb">
            <li class="breadcrumb-item">
              <router-link to="/index"> 主页 </router-link>
            </li>
            <li class="breadcrumb-item active">登录</li>
          </ol>
        </nav>
      </div>
    </div>
  </div>
  <!-- end breadcrumb -->
  <!-- main content -->
  <div class="main-content pb-100">
    <div class="container">
      <div class="row">
        <div class="col-lg-6 col-md-8 col-12 mx-auto">
          <div class="custom-form custom-form--box">
            <h3 class="custom-form__title">登录您的账户</h3>
            <el-form :model="loginForm" :rules="rules" ref="formRef" label-position="top">
              <el-form-item label="邮箱或用户名" prop="username">
                <my-input
                  v-model="loginForm.username"
                  placeholder="输入您的邮箱或用户名"
                  clearable
                  :validate-event="true"
                >
                  <template #prefix>
                    <el-icon><Message /></el-icon>
                  </template>
                </my-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <my-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="输入您的密码"
                  clearable
                  :validate-event="true"
                >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </my-input>
              </el-form-item>

              <div class="custom-form__btn">
                <my-button type="primary" @click="handleLogin">登录 </my-button>
              </div>

              <div class="custom-form__footer">
                <div class="devider">
                  <span>其他账户登录</span>
                </div>
                <div class="social-login">
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
                </div>

                <div class="custom-form__footer--link">
                  <h6>您还没有账户?</h6>
                  <router-link to="/register">
                    <a href="javascript:void(0)" class="btn">注册</a>
                  </router-link>
                </div>

                <router-link to="/resetPassword">
                  <a href="">忘记密码?</a>
                </router-link>
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
</style>
