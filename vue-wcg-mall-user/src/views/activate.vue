<script setup>
import { computed, watch, ref, reactive } from "vue";
import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
import { useScrollToTop } from "@/assets/base.js";
import { ElMessageBox, ElMessage } from "element-plus";
const { showLoader, backtopStyle, handleMouseEnter, handleMouseLeave } =
  useScrollToTop();
import { useRouter } from "vue-router";
const router = useRouter();
import useUserInfoStore from "@/stores/userInfo";
import Cropper from "cropperjs";
import "cropperjs/dist/cropper.css";
const userInfoStore = useUserInfoStore();
const userInfo = ref({ ...userInfoStore.info });
const imgUrl = ref();

const activateData = ref({
  email: userInfo.value.email,
  nickName: userInfo.value.nickName,
  username: "",
  phone: "",
  avatar: "",
});

const uploadSuccess = (result) => {
  //回显图片
  imgUrl.value = result.data;
  activateData.value.avatar = result.data;
  console.log(activateData.value);
};
import { userActiveService, userCheckCode, userGetCode } from "@/api/user.js";
//验证码提示
const codeErr = ref(false);
const checkData = ref({
  email: userInfo.value.email,
  code: "",
});

const checkStatus = ref(false);
//检验验证码
const checkCode = async () => {
  if (checkData.value.code !== "") {
    let result = await userCheckCode(checkData.value);
    if (result.code === 0) {
      codeErr.value = true;
      getCodeButton.value = true;
    } else if (result.code === 1) {
      codeErr.value = false;
      checkStatus.value = true;
    }
  }
};
//激活
const activate = async () => {
  if (checkStatus.value === false) {
    ElMessage({
            showClose: true,
            type: "error",
            message: "验证码已过期，请重新注册账户",
            plain: true,
          });
  } else {
    let result = await userActiveService(activateData.value);
    if (result.code === 1) {
      ElMessage({
            showClose: true,
            type: "success",
            message: result.message ? result.message : "激活成功,请登录",
            plain: true,
          });
      userInfoStore.removeInfo();
      userInfoStore.setInfo(activateData.value);
      router.push("/login");
    } else if (result.code === 0) {
      ElMessage({
            showClose: true,
            type: "error",
            message: result.message ? result.message : "激活失败",
            plain: true,
          });
    }
  }
};

const getCodeButton = ref(false);
const getCOdeButoonDisabled = ref(false);
const getCodeButtonContent = ref("获取验证码");

let isCountingDown = false;
const getCode = async () => {
  if (!isCountingDown) {
    getCodeButtonContent.value = "已发送";
    getCOdeButoonDisabled.value = true;
    let result = await userGetCode(userInfo.value.email);
    if (result.code === 1) {
      codeErr.value = false;
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
    ElMessage({
            showClose: true,
            type: "error",
            message: "验证码已发送，请稍后再试",
            plain: true,
          });
  }
};

const dialogVisible = ref(false);
const imageUrl = ref(null);
const image = ref(null);
let cropper = ref(null);
// 重置上传对话框
const resetUpload = () => {
  imageUrl.value = null;
  image.value = null;
  dialogVisible.value = false;
};
// 图片上传浏览器预览
const handleImageChange = (file) => {
  const reader = new FileReader();
  reader.onload = (e) => {
    imageUrl.value = e.target.result;
    setTimeout(initCropper, 0); // 延迟调用
  };
  reader.readAsDataURL(file.raw);
};
// 上传图片前的校验
const handleFileChange = (file) => {
  // 检查文件是否通过验证
  const isValid = beforeUpload(file);
  if (!isValid) {
    return; // 如果不合法，直接返回
  }
  // 继续处理文件
  handleImageChange(file);
};
const beforeUpload = (file) => {
  const isJpgOrPng =
    file.raw.type === "image/jpeg" || file.raw.type === "image/png";
  if (!isJpgOrPng) {
    ElMessage({
      showClose: true,
      type: "error",
      message: "只能上传JPG",
      plain: true,
    });
    return false;
  }
  const isLt2M = file.raw.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    ElMessage({
      showClose: true,
      type: "error",
      message: "图片大小不能超过 2MB",
      plain: true,
    });
    return false;
  }
  return true;
};

// 初始化裁剪器
const initCropper = () => {
  if (image.value) {
    if (cropper.value) {
      cropper.value.destroy();
    }
    cropper.value = new Cropper(image.value, {
      viewMode: 1,
      dragMode: "move",
      background: true,
      autoCropArea: 0.8,
      zoomOnWheel: true,
      center: true,
      aspectRatio: 1,
    });
  }
};

// 裁剪并上传图片
const cropImage = () => {
  if (cropper.value) {
    const canvas = cropper.value.getCroppedCanvas();
    canvas.toBlob(async (blob) => {
      if (blob) {
        const formData = new FormData();
        formData.append("file", blob, "avatar.jpg"); // 指定文件名
        try {
          const response = await fetch("/api/user/user/upload", {
            method: "POST",
            body: formData,
          });
          const result = await response.json();
          if (result.code === 1) {
            uploadSuccess(result); // 处理上传成功
            dialogVisible.value = false;
          } else {
            ElMessage({
              showClose: true,
              type: "error",
              message: result.message ? result.message : "上传失败",
              plain: true,
            });
            dialogVisible.value = false;
          }
        } catch (error) {
          ElMessage({
            showClose: true,
            type: "error",
            message: "上传过程中发生错误",
            plain: true,
          });
          dialogVisible.value = false;
        }
      } else {
        ElMessage({
          showClose: true,
          type: "error",
          message: "剪裁失败，请重试",
          plain: true,
        });
        dialogVisible.value = false;
      }
    });
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
            <li class="breadcrumb-item active" aria-current="page">激活</li>
          </ol>
        </nav>
      </div>
    </div>
  </div>
  <!-- end breadcrumb -->
  <div class="main-content pb-100">
    <div class="container">
      <div class="row">
        <div class="col-lg-6 col-md-8 col-12 mx-auto">
          <div class="custom-form custom-form--box">
            <h3 class="custom-form__title">激活账户</h3>
            <el-form :model="activateData" @submit.prevent="activate">
              <div class="form-group custom-form__input">
                <label for="name">真实姓名</label>
                <el-form-item>
                  <input
                    type="username"
                    class="form-control"
                    v-model="activateData.username"
                  />
                </el-form-item>
              </div>

              <div class="form-group custom-form__input">
                <label for="email">电话</label>
                <el-form-item>
                  <input
                    type="tel"
                    class="form-control"
                    v-model="activateData.phone"
                  />
                </el-form-item>
              </div>
              <div class="form-group custom-form__input">
                <label for="email">验证码</label>
                <el-form-item class="code-input-container">
                  <div class="input-container">
                    <input
                      type="text"
                      class="form-control"
                      @blur="checkCode"
                      v-model="checkData.code"
                    />

                    <el-button
                      type="info"
                      @click="getCode"
                      class="getCode"
                      v-if="getCodeButton"
                      :disabled="getCOdeButoonDisabled"
                      >{{ getCodeButtonContent }}</el-button
                    >
                  </div>
                  <small v-if="codeErr" style="color: red">验证码已过期</small>
                </el-form-item>
              </div>
              <div class="form-group custom-form__input">
                <label>头像</label>
                <el-dialog
                  v-model="dialogVisible"
                  title="更改头像"
                  width="500"
                  :before-close="resetUpload"
                >
                  <el-upload
                    name="file"
                    action="#"
                    :auto-upload="true"
                    :on-change="handleFileChange"
                    :before-upload="beforeUpload"
                    :on-success="uploadSuccess"
                  >
                    <el-button type="primary">选择图片</el-button>
                  </el-upload>
                  <el-button type="success" @click="cropImage" v-if="imageUrl"
                    >剪裁并上传</el-button
                  >
                  <div v-if="imageUrl">
                    <img
                      ref="image"
                      :src="imageUrl"
                      alt="Cropper Image"
                      style="max-width: 100%; height: auto"
                    />
                  </div>
                </el-dialog>
                <img v-if="imgUrl" :src="imgUrl" class="avatar" />
                <img
                  v-else
                  src="@/assets/images/avatar.jpg"
                  class="avatar"
                  alt="选择图片"
                  @click="dialogVisible = true"
                />
              </div>

              <div class="custom-form__btn">
                <button type="submit" class="btn submit-btn">激活</button>
              </div>
              <div class="custom-form__footer">
                <!-- login/sign up with social media -->

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
.avatar {
  margin-left: 7rem;
  margin-right: 7rem;
  width: 10rem;
  height: 10rem;
}

.code-input-container {
  display: flex;
  align-items: center;
}
.input-container {
  display: flex;
  align-items: center;
}
.getCode {
  width: 15rem;
  height: 2.6rem;
  font-size: 1rem;
  text-align: center;
  border-radius: 1.5rem;
  margin-left: 1rem;
  /* background-color: #f6f7fb;
  color: rgb(78, 76, 76);
  border-color: #f6f7fb; */
}
/* .getCode:hover {
  background-color: rgb(78, 76, 76);
  color: #f6f7fb;
  border-color: #f6f7fb;
} */
</style>