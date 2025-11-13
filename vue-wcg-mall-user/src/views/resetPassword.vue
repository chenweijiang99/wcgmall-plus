<script setup>
import { computed, watch, ref } from "vue";
import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
import { useScrollToTop } from "@/assets/base.js";
import { ElMessageBox, ElMessage } from "element-plus";
import { useRouter } from "vue-router";
const router = useRouter();
const { showLoader, backtopStyle, handleMouseEnter, handleMouseLeave } =
  useScrollToTop();
import {
  userCheckCode,
  userGetCode,
  userActiveService,
 
  userGetCodeByRestPwdService,
  userResetPasswordService,
  userActivateCodeByRestPwdService
} from "@/api/user";
const emailIsSubmit = ref(false);
const checkData = ref({
  email: "",
  code: "",
});
const emailError = ref("");
const validateEmail = () => {
  const email = checkData.value.email;
  if (!email) {
    emailError.value = "邮箱不能为空";
  } else if (!/\S+@\S+\.\S+/.test(email)) {
    emailError.value = "请输入有效的邮箱地址";
  } else {
    emailError.value = "";
  }
};
//验证码提示
const codeError  = ref(false);

const checkStatus = ref(false);
//检验验证码
const checkCode = async () => {
  if (checkData.value.code !== "") {
    let result = await userActivateCodeByRestPwdService(checkData.value);
    if (result.code === 0) {
      codeError.value = true;
      getCodeButton.value = true;
    } else if (result.code === 1) {
      codeError.value = false;
      checkStatus.value = true;
    }
  }
};

const getCodeButton = ref(true);
const getCOdeButoonDisabled = ref(false);
const getCodeButtonContent = ref("获取验证码");

let isCountingDown = false;
const getCode = async () => {
  if (!isCountingDown) {
    getCodeButtonContent.value = "已发送";
    getCOdeButoonDisabled.value = true;
    let result = await userGetCodeByRestPwdService(checkData.value.email);
    if (result.code === 1) {
      codeError.value = false;
      isCountingDown = true;
      // 开始倒计时
      let countDownSeconds = 120;
      const timer = setInterval(() => {
        countDownSeconds--;
        if (countDownSeconds <= 0) {
          clearInterval(timer);
          isCountingDown = false;
          getCodeButtonContent.value = "获取验证码";
          getCOdeButoonDisabled.value = false;
        } else {
          getCodeButtonContent.value = `倒计时 ${countDownSeconds} 秒`;
        }
      }, 1000);
    }
  } else {
    ElMessage.error("验证码已发送，请稍后再试");
  }
};

// 重置密码
const resetPassword = async () => {
  let result = await userResetPasswordService(checkData.value);
  if (result.code === 1) {

    ElMessage({
        showClose: true,
        type: "success",
        message: result.message ? result.message : "密码重置成功，请前往登录页面重新登录",
        plain: true,
      });
    router.push("/login");
  } else if(result.code === 0){
    ElMessage.error(result.message ? result.message : "密码重置失败，请稍后再试");
  }
};

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
            <li class="breadcrumb-item active" aria-current="page">重置密码</li>
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
            <h3 class="custom-form__title">重置您的密码</h3>
            <form v-if="!emailIsSubmit" action="#">
              <div class="form-group custom-form__input">
                <label for="inputEmail">邮箱</label>
                <div class="input-button-container">
                  
                  <input
                    type="email"
                    class="form-control ltr"
                    id="inputEmail"
                    placeholder="输入您的邮箱"
                    v-model="checkData.email"
                    @blur="validateEmail"
                  />
                  
                
                  <button
                    type="button"
                    class="btn submit-btn"
                    @click="getCode"
                    :disabled="getCOdeButoonDisabled"
                  >
                  {{ getCodeButtonContent }}
                  </button>
                </div>
                <small v-if="emailError">{{ emailError }}</small>
              </div>
              <div class="form-group custom-form__input">
                <label for="inputCode">验证码</label>
                <div class="input-button-container">
                  <input
                    type="text"
                    class="form-control ltr"
                    id="inputCode"
                    placeholder="输入验证码"
                    v-model="checkData.code"
                    @blur="checkCode"
                  />
                  <button
                    type="button"
                    class="btn submit-btn btn1"
                    @click="resetPassword"
                    :disabled="!checkStatus"
                  >
                  &nbsp;重置密码&nbsp;
                  </button>
                </div>
                <small v-if="codeError" style="color: red">验证码已过期</small>
              </div>
            </form>
            <div class="custom-form__img">
              <img src="@/assets/images/resetpassword.png" alt="" />
            </div>
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

.input-button-container {
  display: flex;
  align-items: center; /* 垂直居中对齐 */
}

.input-button-container input {
  flex: 1; /* 输入框占据剩余空间 */
  margin-right: 10px; /* 输入框与按钮之间的间距 */
}

.btn {
  background-color: #474747;
  border-color: #474747;;
  color: #ffffff;
}
.btn:hover{
  background-color: #ffffff;
  border-color: #474747;
  color: #474747;
}
.btn1 {
  background-color: #ffffff;
  border-color: #474747;
  color: #474747;
}
.btn1:hover{
  background-color: #474747;
  border-color: #474747;;
  color: #ffffff;
}
</style>