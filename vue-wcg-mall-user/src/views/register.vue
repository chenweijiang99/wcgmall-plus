<script setup>
import { computed, watch, ref, reactive } from "vue";

import { useScrollToTop } from "@/assets/base.js";
import { ElMessageBox, ElMessage } from "element-plus";
const { showLoader,backtopStyle,handleMouseEnter,handleMouseLeave} = useScrollToTop();

//验证码图片
import Img1 from "@/assets/images/code/code1.jpg";
import Img2 from "@/assets/images/code/code2.jpg";
import Img3 from "@/assets/images/code/code3.jpg";
import Img4 from "@/assets/images/code/code4.jpg";
const captchaImages = [Img1, Img2, Img3,Img4];
//引入'vue3-puzzle-vcode'插件
import Vcode from "vue3-puzzle-vcode";
//验证码模态框是否出现，默认不展示
const isShow = ref(false);

//用户通过了验证
const success = () => {
  isShow.value = false;
  register();
};
//用户点击遮罩层，关闭模态框
const close = () => {
  isShow.value = false;
};
// 打开验证组件
const validate = () => {
  if (
    nameError.value != "" ||
    emailError.value != "" ||
    passwordError.value != "" ||
    registerData.value.nickName == "" ||
    registerData.value.email == "" ||
    registerData.value.password == ""
  ) {
    ElMessage.error("请检查输入数据是否符合要求");
    return;
  }
  //展现验证码模态框
  isShow.value = true;
};

const registerData = ref({
  nickName: "",
  email: "",
  password: "",
});
const nameError = ref("");
const validateName = () => {
  const name = registerData.value.nickName;
  if (!name) {
    nameError.value = "请输入昵称";
  } else if (name.length < 2 || name.length > 12) {
    nameError.value = "昵称长度在2-12个字符之间";
  } else {
    nameError.value = "";
  }
};
const emailError = ref("");
const validateEmail = () => {
  const email = registerData.value.email;
  if (!email) {
    emailError.value = "邮箱不能为空";
  } else if (!/\S+@\S+\.\S+/.test(email)) {
    emailError.value = "请输入有效的邮箱地址";
  } else {
    emailError.value = "";
  }
};

const passwordError = ref("");
const validatePassword = () => {
  const password = registerData.value.password;
  if (!password) {
    passwordError.value = "请输入密码";
  } else if (password.length < 6 || password.length > 18) {
    passwordError.value = "密码长度在6-18个字符之间";
  } else {
    passwordError.value = "";
  }
};

import { userRegisterService } from "@/api/user.js";
import { useRouter } from "vue-router";
import useUserInfoStore from "@/stores/userInfo";
const userInfoStore = useUserInfoStore();
const router = useRouter();
const register = async () => {
  showLoader.value = true;
  let result = await userRegisterService(registerData.value);
  if (result.code === 1) {
    showLoader.value = false;
    ElMessage({
        showClose: true,
        type: "success",
        message: result.message ? result.message : "注册成功，请激活后登录",
        plain: true,
      });
    userInfoStore.setInfo(registerData.value);
    router.push("/activate");
  } else if (result.code === 0) {
    showLoader.value = false;
    ElMessage.error(result.message ? result.message : "注册失败");
  }
};

let percentage = ref(0);
let status = ref("exception");

const checkStrong = (sValue) => {
  var modes = 0;
  if (sValue.length < 1) return modes;
  if (/\d/.test(sValue)) modes++; //数字
  if (/[a-z]/.test(sValue)) modes++; //小写
  if (/[A-Z]/.test(sValue)) modes++; //大写
  if (/\W/.test(sValue)) modes++; //特殊字符
  switch (modes) {
    case 1:
      return 1;
      break;
    case 2:
      return 2;
      break;
    case 3:
      return 3;
    case 4:
      return 4;
      break;
  }
  return modes;
};

const statusChange = (modes) => {
  if (modes == 1) {
    percentage.value = 25;
    status.value = "exception";
  } else if (modes == 2) {
    percentage.value = 50;
    status.value = "exception";
  } else if (modes == 3) {
    percentage.value = 75;
    status.value = "warning";
  } else {
    percentage.value = 100;
    status.value = "success";
  }
};
watch(
  () => registerData.value.password,
  (newValue, oldValue) => {
    let modes = checkStrong(newValue);
    console.log(modes);
    statusChange(modes);
  }
);
</script>

<template>
  <!-- <headerView /> -->
  <!-- breadcrumb -->
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
  <!-- end breadcrumb -->
  <!-- main content -->
  <div class="main-content pb-100">
    <div class="container">
      <div class="row">
        <div class="col-lg-6 col-md-8 col-12 mx-auto">
          <div class="custom-form custom-form--box">
            <h3 class="custom-form__title">注册账户</h3>
            <el-form :model="registerData" @submit.prevent="validate">
              <div class="form-group custom-form__input">
                <label for="name">昵称</label>
                <el-form-item>
                  <input
                    class="form-control"
                    id="nickName"
                    v-model="registerData.nickName"
                    @blur="validateName"
                  />
                  <small v-if="nameError">{{ nameError }}</small>
                </el-form-item>
              </div>

              <div class="form-group custom-form__input">
                <label for="email">邮箱</label>
                <el-form-item>
                  <input
                    type="email"
                    id="email"
                    class="form-control"
                    @blur="validateEmail"
                    v-model="registerData.email"
                  />
                  <small v-if="emailError">{{ emailError }}</small>
                </el-form-item>
              </div>

              <div class="form-group custom-form__input">
                <label for="password">密码</label>
                <div class="input-box">
                  <input
                    type="password"
                    class="form-control"
                    id="password"
                    v-model="registerData.password"
                    @blur="validatePassword"
                  />
                  <small v-if="passwordError">{{ passwordError }}</small>
                </div>
              </div>
              <div class="form-group custom-form__input">
                <label>密码强度</label>

                <el-progress
                  v-show="registerData.password"
                  :percentage="percentage"
                  :status="status"
                  style="width: 100%"
                />
              </div>
              <Vcode
                :show="isShow"
                @success="success"
                @close="close"
                @fail="fail"
                :imgs="captchaImages"
              ></Vcode>
              <div class="custom-form__btn">
                <button type="submit" class="btn submit-btn">注册</button>
              </div>
              <div class="custom-form__footer">
                <!-- login/sign up with social media -->
                <div class="devider">
                  <span>使用其他账户注册</span>
                </div>
                <div class="social-login">
                  <ul>
                    <li>
                      <a href="javascript:void(0)" class="social-icon tr-icon"
                        ><i class="bi-tencent-qq"></i
                      ></a>
                    </li>
                    <li>
                      <a href="javascript:void(0)" class="social-icon gl-icon"
                        ><i class="bi-sina-weibo"></i
                      ></a>
                    </li>
                    <li>
                      <a href="javascript:void(0)" class="social-icon gr-icon"
                        ><i class="bi-wechat"></i
                      ></a>
                    </li>
                  </ul>
                </div>
                <!-- form footer -->
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
  <!-- end main content -->

  <!-- scroll up btn -->
  <el-backtop
    :right="100"
    :bottom="100"
    :style="backtopStyle"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  />
  <!-- end scroll up btn -->
  <!-- loader -->
  <div class="loader" v-if="showLoader">
    <div class="spinner">
      <div class="cube1"></div>
      <div class="cube2"></div>
    </div>
  </div>
  <!-- end loader -->
  <!-- <footerView /> -->
</template>

<style scoped>
@import "@/assets/main.css";
a {
  text-decoration: none;
}
small {
  margin-left: 1rem;
  color: darkgrey;
}
</style>