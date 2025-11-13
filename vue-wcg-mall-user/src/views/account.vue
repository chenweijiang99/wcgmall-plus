<script setup>
import { computed, watch, ref, onMounted, reactive, markRaw } from "vue";
import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
import { useScrollToTop } from "@/assets/base.js";
import { ElMessage, ElMessageBox, ElImageViewer } from "element-plus";
import useUserInfoStore from "@/stores/userInfo.js";
import { useTokenStore } from "@/stores/token.js";
import { useRouter, useRoute } from "vue-router";
import Cropper from "cropperjs";
import "cropperjs/dist/cropper.css";

import { Delete } from "@element-plus/icons-vue";
const router = useRouter();
const route = useRoute();
const tokenStore = useTokenStore();
const { showLoader, backtopStyle, handleMouseEnter, handleMouseLeave } = useScrollToTop();

import emitter from "@/event/eventBus.js";
const activeTap = ref(1);
if (route.query.activeTap) {
  activeTap.value = parseInt(route.query.activeTap);
}

const showEditProfileModal = ref(false);
const userInfoStore = useUserInfoStore();
const userInfo = ref({});
const avatar = ref("");
const getUserInfo = async () => {
  if (tokenStore.token !== "") {
    userInfo.value = userInfoStore.info;
  }
};

let originalUserInfo = ref({}); // 保存原始表单数据
const uploadSuccess = async (result1) => {
  //回显图片
  userInfo.value.avatar = result1.data;
  let result = await userUpdateService(userInfo.value);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message:  "修改成功",
      plain: true,
    });
    //发送事件，刷新头部
    emitter.emit("refresh");
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "修改失败",
      plain: true,
    });
  }
};

const openEditProfileModal = () => {
  Object.assign(originalUserInfo.value, userInfo.value); // 在打开模态框时保存原始表单数据
  showEditProfileModal.value = true;
};

const closeEditProfileModal = () => {
  Object.assign(userInfo.value, originalUserInfo.value); // 关闭模态框时恢复原始表单数据
  showEditProfileModal.value = false;
};
import { userUpdateService, userEditePasswordService } from "@/api/user.js";
// 修改资料
const saveUserInfo = async () => {
  // 在这里处理保存表单数据的逻辑，例如提交到后端或其他操作
  console.log(userInfo.value);
  let result = await userUpdateService(userInfo.value);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message:  "修改成功",
      plain: true,
    });
    //发送事件，刷新头部
    emitter.emit("refresh");
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "修改失败",
      plain: true,
    });
  }
  showEditProfileModal.value = false; // 保存后关闭编辑资料模态框
};
//密码数据模型
const passwordData = ref({
  oldPassword: "",
  newPassword: "",
  rePassword: "",
});
// 修改密码
const changePassword = async () => {
  let result = await userEditePasswordService(passwordData.value);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message:  "修改成功",
      plain: true,
    });
    tokenStore.removeToken();
    userInfoStore.removeInfo();
    emitter.emit("clear");
    router.push("/login");
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "修改失败",
      plain: true,
    });
  }
};

const orders = ref([]);

import { userGerOrderListService } from "@/api/order.js";
const gerUserOrderList = async () => {
  let result = await userGerOrderListService();
  if (result.code === 200) {
    orders.value = result.data;
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "获取订单失败",
      plain: true,
    });
  }
};

const currentPage = ref(1);
const ordersPerPage = 5;
const totalProducts = computed(() => orders.value.length);
const totalPages = computed(() => Math.ceil(totalProducts.value / ordersPerPage));
const startResult = computed(() => (currentPage.value - 1) * ordersPerPage + 1);
const endResult = computed(() => {
  const end = currentPage.value * ordersPerPage;
  return end > totalProducts.value ? totalProducts.value : end;
});
const displayedOrders = computed(() => {
  const start = (currentPage.value - 1) * ordersPerPage;
  const end = currentPage.value * ordersPerPage;
  return orders.value.slice(start, end);
});

const changePage = (page) => {
  currentPage.value = page;
};

const userAddressList = ref([]);
import {
  userGetAddressListService,
  userUpdateAddressService,
  userDeleteAddressService,
  userAddAddressService,
} from "@/api/address.js";
// import { el } from "element-plus/es/locale";
// 获取用户地址列表
const getUserAddressList = async () => {
  let result = await userGetAddressListService();
  if (result.code === 200) {
    userAddressList.value = result.data;
  }
};

// 取消默认地址
const unsetDefaultAddress = async (id) => {
  const address = userAddressList.value.find((item) => item.id === id);
  address.isDefault = 0;
  let result = await userUpdateAddressService(address);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message:  "取消默认地址成功",
      plain: true,
    });
    getUserAddressList();
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "取消默认地址失败",
      plain: true,
    });
  }
};
// 设置默认地址
const setDefaultAddress = async (id) => {
  const defaultAddress = userAddressList.value.find((item) => item.isDefault === 1);
  if (defaultAddress !== undefined) {
    ElMessage({
      showClose: true,
      type: "error",
      message: "只能设置一个默认地址",
      plain: true,
    });
    return;
  }
  const address = userAddressList.value.find((item) => item.id === id);
  address.isDefault = 1;
  let result = await userUpdateAddressService(address);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message: "修改默认地址成功",
      plain: true,
    });
    getUserAddressList();
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "修改默认地址失败",
      plain: true,
    });
  }
};
const addAddressModal = ref(false);
const addAddressData = ref({
  consignee: "",
  consigneePhone: "",
  consigneeAddress: "",
});
// 添加地址
const saveAddress = async () => {
  let result = await userAddAddressService(addAddressData.value);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message: "添加地址成功",
      plain: true,
    });
    getUserAddressList();
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "添加地址失败",
      plain: true,
    });
  }
  addAddressModal.value = false;
};

const editAddressData = ref({
  id: "",
  userId: "",
  consignee: "",
  consigneePhone: "",
  consigneeAddress: "",
  default: "",
});
const editAddressModal = ref(false);
const editAddress = (id) => {
  editAddressModal.value = true;
  editAddressData.value = userAddressList.value.find((item) => item.id === id);
};
// 修改地址
const saveEditAddress = async () => {
  let result = await userUpdateAddressService(editAddressData.value);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message: "修改地址成功",
      plain: true,
    });
    getUserAddressList();
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "修改地址失败",
      plain: true,
    });
  }
  editAddressModal.value = false;
};

// 删除地址
const deleteAddress = (id) => {
  ElMessageBox.confirm("是否删除该地址?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      let result = await userDeleteAddressService(id);
      if (result.code === 200) {
        ElMessage({
          showClose: true,
          type: "success",
          message:  "删除地址成功",
          plain: true,
        });
        getUserAddressList();
      } else {
        ElMessage({
          showClose: true,
          type: "error",
          message: result.message ? result.message : "删除地址失败",
          plain: true,
        });
      }
    })
    .catch(() => {
      console.log("取消删除地址:");
    });
};

const userWishList = ref([]);

import {
  userGetWishListService,
  userDeleteWishListService,
  userAddWishListProdcutToCartService,
} from "@/api/wishList.js";

const gerUserWishList = async () => {
  let result = await userGetWishListService();
  if (result.code === 1) {
    userWishList.value = result.data;
  }
};

// 删除愿望单商品
const deleteWishList = (productId) => {
  ElMessageBox.confirm("是否删除该商品?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      let result = await userDeleteWishListService(productId);
      if (result.code === 200) {
        ElMessage({
          showClose: true,
          type: "success",
          message:  "删除商品成功",
          plain: true,
        });
        gerUserWishList();
        emitter.emit("refresh");
      } else {
        ElMessage({
          showClose: true,
          type: "error",
          message: result.message ? result.message : "删除商品失败",
          plain: true,
        });
      }
    })
    .catch(() => {
      ElMessage({
        showClose: true,
        message: "取消删除商品",
        plain: true,
      });
    });
};

//添加到购物车
const addProductToCart = async (productId) => {
  let result = await userAddWishListProdcutToCartService(productId);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message:"添加商品成功",
      plain: true,
    });
    gerUserWishList();
    emitter.emit("refresh");
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "添加商品失败",
      plain: true,
    });
  }
};
// 订单详情
const orderDetail = (orderNumber) => {
  router.push({ path: "/orderDetail", query: { orderNumber: orderNumber } });
};

//退出登录
import { logoutService } from "@/api/user.js";
const logout = async () => {
  let result = await logoutService();
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message:  "退出成功",
      plain: true,
    });
    tokenStore.removeToken();
    userInfoStore.removeInfo();
    emitter.emit("clear");
    router.push("/login");
  } else if (result.code === 0) {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "退出失败",
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
  const isJpgOrPng = file.raw.type === "image/jpeg" || file.raw.type === "image/png";
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
          const response = await fetch("/api/file/upload", {
            method: "POST",
            body: formData,
          });
          const result = await response.json();
          if (result.code === 200) {
            uploadSuccess(result); // 处理上传成功
            dialogVisible.value = false;
          } else {
            ElMessage({
              showClose: true,
              type: "error",
              message: "上传失败",
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

onMounted(() => {
  getUserInfo();
  getUserAddressList();
  // gerUserOrderList();
  // gerUserWishList();
});
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
            <li class="breadcrumb-item active" aria-current="page">我的账户</li>
          </ol>
        </nav>
      </div>
    </div>
  </div>
  <!-- end breadcrumb -->
  <!-- main content -->
  <div class="main-content pb-80">
    <div class="container">
      <div class="row">
        <div class="col-lg-3">
          <div
            class="nav flex-column nav-pills account-pills account-tabs"
            id="v-pills-tab"
            role="tablist"
            aria-orientation="vertical"
          >
            <a
              class="nav-link"
              :class="{ active: activeTap === 1 }"
              @click="activeTap = 1"
              ><span> <i class="icon-user-profile"></i></span>个人资料</a
            >
            <a
              class="nav-link"
              :class="{ active: activeTap === 2 }"
              @click="activeTap = 2"
              ><span><i class="icon-shopping-basket"></i></span>最近订单</a
            >
            <a
              class="nav-link"
              :class="{ active: activeTap === 3 }"
              @click="activeTap = 3"
              ><span><i class="icon-sign"></i></span>我的地址</a
            >
            <a
              class="nav-link"
              :class="{ active: activeTap === 4 }"
              @click="activeTap = 4"
              ><span><i class="icon-wish-list"></i></span>愿望清单</a
            >
            <a
              class="nav-link"
              href="javascript:void(0)"
              role="tab"
              aria-selected="false"
              @click="logout"
              ><span><i class="icon-log-out"></i></span>退出登录</a
            >
          </div>
        </div>
        <div class="col-lg-9">
          <div class="tab-content account-content" id="v-pills-tabContent">
            <!-- profile tab -->
            <div class="fade show" v-show="activeTap === 1">
              <!-- profile information -->
              <div class="row">
                <div class="col-12">
                  <div class="profile-info profile-info--main">
                    <div class="profile-info__row">
                      <div class="profile-info__col">
                        <span class="content"
                          ><span>用户名:</span>
                          <span>{{ userInfo.username }}</span>
                        </span>
                      </div>
                      <div class="profile-info__col">
                        <span class="content"
                          ><span>邮箱地址:</span
                          ><span class="profile-info__col--value ltr">{{
                            userInfo.email
                          }}</span></span
                        >
                      </div>
                    </div>
                    <div class="profile-info__row">
                      <div class="profile-info__col">
                        <span class="content"
                          ><span>电话号码:</span>
                          <span class="profile-info__col--value">{{
                            userInfo.mobile
                          }}</span>
                        </span>
                      </div>
                      <div class="profile-info__col">
                        <span class="content"
                          ><span>用户昵称:</span>
                          <span>{{ userInfo.nickname }}</span>
                        </span>
                      </div>
                    </div>
                    <div class="profile-info__action">
                      <button
                        type="button"
                        data-toggle="modal"
                        data-target="#editProfileModal"
                        class="btn"
                        @click="openEditProfileModal"
                      >
                        <span><i class="icon-edit"></i></span>编辑资料
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              <!-- end profile information -->
              <!-- change the password form -->
              <div class="row">
                <div class="col-md-6">
                  <div class="custom-form mt-30">
                    <form class="change-pass" @submit.prevent="changePassword">
                      <h4 class="custom-form__title">更改密码</h4>
                      <div class="form-group custom-form__input">
                        <label for="oldPassInput">原始密码</label>
                        <div class="input-box password-box">
                          <input
                            type="password"
                            class="form-control ltr"
                            id="oldPassInput"
                            placeholder=""
                            v-model="passwordData.oldPassword"
                          />
                          <div class="input-box__icon">
                            <span class="showhidepassword"
                              ><i class="far fa-eye-slash"></i
                            ></span>
                          </div>
                        </div>
                      </div>
                      <div class="form-group custom-form__input">
                        <label for="newPassInput">新密码</label>
                        <div class="input-box password-box">
                          <input
                            type="password"
                            class="form-control ltr"
                            id="newPassInput"
                            placeholder=""
                            v-model="passwordData.newPassword"
                          />
                          <div class="input-box__icon">
                            <span class="showhidepassword"
                              ><i class="far fa-eye-slash"></i
                            ></span>
                          </div>
                        </div>
                      </div>
                      <div class="form-group custom-form__input">
                        <label for="retypePassInput">确认新密码</label>
                        <div class="input-box password-box">
                          <input
                            type="password"
                            class="form-control ltr"
                            id="rePassInput"
                            placeholder=""
                            v-model="passwordData.rePassword"
                          />
                          <div class="input-box__icon">
                            <span class="showhidepassword"
                              ><i class="far fa-eye-slash"></i
                            ></span>
                          </div>
                          <small
                            v-if="passwordData.newPassword !== passwordData.rePassword"
                            style="color: red"
                            >请与新密码保持一致</small
                          >
                        </div>
                      </div>
                      <div class="custom-form__btn">
                        <button type="submit" class="btn change-pass-btn">
                          更改密码
                        </button>
                      </div>
                    </form>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="custom-form mt-30" style="align-items: center">
                    <h4 class="custom-form__title">点击图片更改头像</h4>
                    <!-- <el-upload
                      :show-file-list="false"
                      :auto-upload="true"
                      action="/api/user/user/upload"
                      name="file"
                      :on-success="uploadSuccess"
                      style="margin-left: 20%; margin-top: 20%"
                    >
                      <img :src="userInfo.avatar" class="avatar" />
                    </el-upload> -->
                    <img
                      :src="userInfo.avatar"
                      class="avatar"
                      @click="dialogVisible = true"
                    />
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
                  </div>
                </div>
              </div>
              <!-- Modal edit profile -->
              <el-dialog
                v-model="showEditProfileModal"
                title="编辑您的个人资料"
                draggable="true"
                align-center="true"
              >
                <div class="modal-body">
                  <form class="custom-form">
                    <div class="row">
                      <div class="col-md-6">
                        <div class="form-group custom-form__input">
                          <label for="userName">用户名</label>
                          <input
                            type="text"
                            class="form-control"
                            placeholder=""
                            v-model="userInfo.username"
                          />
                        </div>
                      </div>
                      <div class="col-md-6">
                        <div class="form-group custom-form__input">
                          <label for="email">邮箱地址</label>
                          <input
                            type="email"
                            class="form-control ltr"
                            v-model="userInfo.email"
                            placeholder=""
                          />
                        </div>
                      </div>
                      <div class="col-md-6">
                        <div class="form-group custom-form__input">
                          <label for="phone">电话号码</label>
                          <input
                            type="phone"
                            class="form-control ltr"
                            v-model="userInfo.mobile"
                            placeholder=""
                          />
                        </div>
                      </div>
                      <div class="col-md-6">
                        <div class="form-group custom-form__input">
                          <label for="nickName">用户昵称</label>
                          <input
                            type="text"
                            class="form-control"
                            v-model="userInfo.nickname"
                            placeholder=""
                          />
                        </div>
                      </div>
                    </div>
                  </form>
                </div>
                <div class="modal-footer custom-form__btn">
                  <button
                    type="button"
                    class="btn btn-green"
                    id="close"
                    @click="closeEditProfileModal"
                  >
                    关闭
                  </button>
                  <button type="button" class="btn btn-green" @click="saveUserInfo">
                    保存
                  </button>
                </div>
              </el-dialog>
              <!-- end Modal edit profile -->
            </div>
            <!-- orders tab -->
            <div class="fade show" v-show="activeTap === 2">
              <div class="order-table order-table__collapse">
                <div class="order-table__head">
                  <div class="order-table__row order-table__row--head">
                    <div class="order-table__cell order-table__cell--id">订单号</div>

                    <div class="order-table__cell order-table__cell--receive">状态</div>
                    <div class="order-table__cell order-table__cell--price">总价</div>
                    <div class="order-table__cell order-table__cell--payment">
                      支付方式
                    </div>
                    <div class="order-table__cell order-table__cell--details">详情</div>
                  </div>
                </div>
                <div
                  class="order-table__body"
                  v-for="item in displayedOrders"
                  :key="item"
                >
                  <div class="order-table__row">
                    <div class="order-table__cell order-table__cell--id">
                      <div class="order-table__cell--content id-content">
                        {{ item.orderNumber }}
                      </div>
                    </div>

                    <div class="order-table__cell order-table__cell--receive">
                      <div class="order-table__cell--content receive-content">
                        <span
                          class="badge order-table__status order-table__status--cancel"
                          v-if="item.status === 1"
                          >待付款
                        </span>
                        <span
                          class="badge order-table__status order-table__status--payed"
                          v-if="item.status === 2"
                          >已付款
                        </span>
                        <span
                          class="badge order-table__status order-table__status--progress"
                          v-if="item.status === 3"
                          >已发货
                        </span>
                        <span
                          class="badge order-table__status order-table__status--success"
                          v-if="item.status === 4"
                          >已完成
                        </span>
                        <span
                          class="badge order-table__status order-table__status--cancel"
                          v-if="item.status === 5"
                          >已取消
                        </span>
                        <span
                          class="badge order-table__status order-table__status--cancel"
                          v-if="item.status === 6"
                          >已退款
                        </span>
                      </div>
                    </div>
                    <div class="order-table__cell order-table__cell--price">
                      <div class="order-table__cell--content price-content">
                        ￥{{ item.amount }}
                      </div>
                    </div>
                    <div class="order-table__cell order-table__cell--payment">
                      <div
                        class="order-table__cell--content payment-content"
                        v-if="item.payMethod === 1"
                      >
                        支付宝
                      </div>
                      <div
                        class="order-table__cell--content payment-content"
                        v-if="item.payMethod === 2"
                      >
                        微信
                      </div>
                    </div>
                    <div class="order-table__cell order-table__cell--details">
                      <div class="order-table__cell--content details-content">
                        <a
                          href="javascript:void(0)"
                          @click="orderDetail(item.orderNumber)"
                          >详情</a
                        >
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <!-- empty account tab -->
              <div class="row" v-if="orders.length === 0">
                <div class="col-12">
                  <div class="notice-wrapper">
                    <p>
                      您还没有订单
                      <router-link to="/shop">
                        <a class="notice-wrapper__link">去购物</a>
                      </router-link>
                    </p>
                  </div>
                </div>
              </div>
              <!-- empty account tab -->

              <!-- pagination -->
              <div class="row">
                <div class="col-12">
                  <div class="shop-pagination">
                    <nav aria-label="Page navigation">
                      <ul class="pagination custom-pagination">
                        <li v-if="currentPage > 1" class="page-item">
                          <a
                            class="page-link"
                            href="javascript:void(0)"
                            @click="changePage(currentPage - 1)"
                            ><i class="bi-caret-left-fill"></i
                          ></a>
                        </li>
                        <li
                          v-for="page in totalPages"
                          :key="page"
                          :class="{
                            'page-item': true,
                            active: currentPage === page ? true : false,
                          }"
                        >
                          <a
                            class="page-link"
                            href="javascript:void(0)"
                            @click="changePage(page)"
                          >
                            {{ page }}
                          </a>
                        </li>
                        <li v-if="currentPage < totalPages" class="page-item">
                          <a
                            class="page-link"
                            href="javascript:void(0)"
                            @click="changePage(currentPage + 1)"
                            ><i class="bi-caret-right-fill"></i
                          ></a>
                        </li>
                      </ul>
                    </nav>
                    <span class="result">
                      显示 {{ totalProducts }} 个结果中的 {{ startResult }}--{{
                        endResult
                      }}
                      个
                    </span>
                  </div>
                </div>
              </div>
              <!-- end pagination -->
            </div>
            <!-- addresses tab -->
            <div class="fade show" v-show="activeTap === 3">
              <div class="row profile-address">
                <!-- adding new address -->
                <div class="col-12">
                  <!-- Button trigger modal -->
                  <button
                    type="button"
                    class="btn profile-address__btn"
                    @click="addAddressModal = true"
                  >
                    <span><i class="icon-add-location"></i></span>添加新地址
                  </button>
                  <!-- Modal adding new address -->
                  <el-dialog
                    v-model="addAddressModal"
                    title="添加新地址"
                    draggable="true"
                    align-center="true"
                  >
                    <div class="modal-body">
                      <form class="custom-form">
                        <div class="row">
                          <div class="col-md-6">
                            <div class="form-group custom-form__input">
                              <label for="firstnameInput">收货人</label>
                              <input
                                type="text"
                                class="form-control"
                                id="firstnameInput"
                                placeholder=""
                                value=""
                                required=""
                                v-model="addAddressData.consignee"
                              />
                            </div>
                          </div>
                          <div class="col-md-6"></div>
                          <div class="col-md-6">
                            <div class="form-group custom-form__input">
                              <label for="stateInput">收货地址</label>
                              <input
                                type="text"
                                class="form-control"
                                id="stateInput"
                                placeholder=""
                                v-model="addAddressData.consigneeAddress"
                              />
                            </div>
                          </div>

                          <div class="col-md-6">
                            <div class="form-group custom-form__input">
                              <label for="phone">电话 </label>
                              <input
                                type="text"
                                class="form-control ltr"
                                id="phone"
                                placeholder=""
                                v-model="addAddressData.consigneePhone"
                              />
                            </div>
                          </div>
                        </div>
                      </form>
                    </div>
                    <div class="modal-footer custom-form__btn">
                      <button
                        type="button"
                        class="btn btn-green"
                        id="close"
                        @click="addAddressModal = false"
                      >
                        关闭
                      </button>
                      <button type="button" class="btn btn-green" @click="saveAddress">
                        添加
                      </button>
                    </div>
                  </el-dialog>
                </div>
                <!-- user addresses -->
                <div class="col-md-6" v-for="item in userAddressList" :key="item">
                  <div class="profile-address__card">
                    <div class="profile-address__card--desc">
                      <h4>{{ item.consignee }}</h4>
                      <h5>
                        {{ item.consigneePhone }}
                      </h5>
                      <p>
                        {{ item.consigneeAddress }}
                      </p>
                      <el-tag v-if="item.isDefault" type="primary">默认地址</el-tag>
                      <el-tag v-else type="info">非默认地址</el-tag>
                      <el-button
                        v-if="item.isDefault"
                        type="info"
                        size="small"
                        @click="unsetDefaultAddress(item.id)"
                        >取消默认</el-button
                      >
                      <el-button
                        v-else
                        type="primary"
                        size="small"
                        @click="setDefaultAddress(item.id)"
                        >设为默认</el-button
                      >
                    </div>
                    <div class="profile-address__card--footer">
                      <button
                        type="button"
                        class="btn btn-blue"
                        @click="editAddress(item.id)"
                      >
                        编辑
                      </button>
                      <button
                        type="button"
                        class="btn btn-red"
                        @click="deleteAddress(item.id)"
                      >
                        删除
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <el-dialog
              v-model="editAddressModal"
              title="编辑地址"
              draggable="true"
              align-center="true"
            >
              <div class="modal-body">
                <form class="custom-form">
                  <div class="row">
                    <div class="col-md-6">
                      <div class="form-group custom-form__input">
                        <label for="firstnameInput">收货人</label>
                        <input
                          type="text"
                          class="form-control"
                          v-model="editAddressData.consignee"
                        />
                      </div>
                    </div>
                    <div class="col-md-6"></div>
                    <div class="col-md-6">
                      <div class="form-group custom-form__input">
                        <label for="stateInput">收货地址</label>
                        <input
                          type="text"
                          class="form-control"
                          v-model="editAddressData.consigneeAddress"
                        />
                      </div>
                    </div>
                    <div class="col-md-6">
                      <div class="form-group custom-form__input">
                        <label for="phone">电话 </label>
                        <input
                          type="text"
                          class="form-control ltr"
                          v-model="editAddressData.consigneePhone"
                        />
                      </div>
                    </div>
                  </div>
                </form>
              </div>
              <div class="modal-footer custom-form__btn">
                <button
                  type="button"
                  class="btn btn-green"
                  id="close"
                  @click="editAddressModal = false"
                >
                  关闭
                </button>
                <button type="button" class="btn btn-green" @click="saveEditAddress">
                  修改
                </button>
              </div>
            </el-dialog>
            <!-- wish list tab -->
            <div class="fade show" v-show="activeTap === 4">
              <div class="row wishlist-tab">
                <div class="col-lg-4 col-md-6" v-for="item in userWishList" :key="item">
                  <div class="card-item card-item--light mb-30">
                    <div class="card-item__bg">
                      <a href="javascript:void(0)">
                        <img :src="item.productImage" alt="" />
                      </a>
                    </div>
                    <div class="card-item__body">
                      <div class="card-item__body--price">
                        <strong>{{ item.productPrice }}</strong>
                      </div>
                      <div class="card-item__body--title">
                        <a href="javascript:void(0)">
                          <h4>{{ item.productName }}</h4>
                        </a>
                      </div>
                      <div class="product-rate">
                        <div class="product-rate__star">
                          <span class="rate-5"></span>
                        </div>
                        <div class="rate-number">(5)</div>
                      </div>
                    </div>
                    <div class="card-item__overlay">
                      <ul class="action-btn">
                        <li>
                          <a class="icon-box">
                            <i class="icon-search"></i>
                          </a>
                        </li>
                        <li>
                          <a class="icon-box" @click="deleteWishList(item.productId)">
                            <i class="icon-bin"></i>
                          </a>
                        </li>
                      </ul>
                      <a
                        href="javascript:void(0)"
                        class="btn"
                        @click="addProductToCart(item.productId)"
                        >添加到购物车</a
                      >
                    </div>
                  </div>
                </div>
              </div>
              <!-- empty account tab -->
              <div class="row" v-if="userWishList.length === 0">
                <div class="col-12">
                  <div class="notice-wrapper">
                    <p>您的心愿清单为空</p>
                  </div>
                </div>
              </div>
              <!-- empty account tab -->
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
  cursor: pointer;
}
.tab-pane {
  display: none;
}
.fade {
  transition: opacity 0.15s linear;
}
@media screen and (prefers-reduced-motion: reduce) {
  .fade {
    transition: none;
  }
}
.modal-open {
  overflow: hidden;
}
.modal-open .modal {
  overflow-x: hidden;
  overflow-y: auto;
}
.modal {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1050;
  display: none;
  width: 100%;
  height: 100%;
  overflow: hidden;
  outline: 0;
}
.modal-dialog {
  position: relative;
  width: auto;
  margin: 0.5rem;
  pointer-events: none;
}
.modal.fade .modal-dialog {
  transition: -webkit-transform 0.3s ease-out;
  transition: transform 0.3s ease-out;
  transition: transform 0.3s ease-out, -webkit-transform 0.3s ease-out;
  -webkit-transform: translate(0, -50px);
  transform: translate(0, -50px);
}
@media screen and (prefers-reduced-motion: reduce) {
  .modal.fade .modal-dialog {
    transition: none;
  }
}
.modal.show .modal-dialog {
  -webkit-transform: none;
  transform: none;
}
.modal-dialog-centered {
  display: -ms-flexbox;
  display: flex;
  -ms-flex-align: center;
  align-items: center;
  min-height: calc(100% - (0.5rem * 2));
}
.modal-dialog-centered::before {
  display: block;
  height: calc(100vh - (0.5rem * 2));
  content: "";
}
.modal-content {
  position: relative;
  display: -ms-flexbox;
  display: flex;
  -ms-flex-direction: column;
  flex-direction: column;
  width: 100%;
  pointer-events: auto;
  background-color: #fff;
  background-clip: padding-box;
  border: 1px solid rgba(0, 0, 0, 0.2);
  border-radius: 0.3rem;
  outline: 0;
}
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1040;
  width: 100vw;
  height: 100vh;
  background-color: #000;
}
.modal-backdrop.fade {
  opacity: 0;
}
.modal-backdrop.show {
  opacity: 0.5;
}
.modal-header {
  display: -ms-flexbox;
  display: flex;
  -ms-flex-align: start;
  align-items: flex-start;
  -ms-flex-pack: justify;
  justify-content: space-between;
  padding: 1rem 1rem;
  border-bottom: 1px solid #e9ecef;
  border-top-left-radius: 0.3rem;
  border-top-right-radius: 0.3rem;
}
.modal-header .close {
  padding: 1rem 1rem;
  margin: -1rem -1rem -1rem auto;
}
.modal-title {
  margin-bottom: 0;
  line-height: 1.5;
}
.modal-body {
  position: relative;
  -ms-flex: 1 1 auto;
  flex: 1 1 auto;
  padding: 1rem;
}
.modal-footer {
  display: -ms-flexbox;
  display: flex;
  -ms-flex-align: center;
  align-items: center;
  -ms-flex-pack: end;
  justify-content: flex-end;
  padding: 1rem;
  border-top: 1px solid #e9ecef;
  border-bottom-right-radius: 0.3rem;
  border-bottom-left-radius: 0.3rem;
}
.modal-footer > :not(:first-child) {
  margin-left: 0.25rem;
}
.modal-footer > :not(:last-child) {
  margin-right: 0.25rem;
}
.modal-scrollbar-measure {
  position: absolute;
  top: -9999px;
  width: 50px;
  height: 50px;
  overflow: scroll;
}
@media (min-width: 576px) {
  .modal-dialog {
    max-width: 500px;
    margin: 1.75rem auto;
  }
  .modal-dialog-centered {
    min-height: calc(100% - (1.75rem * 2));
  }
  .modal-dialog-centered::before {
    height: calc(100vh - (1.75rem * 2));
  }
  .modal-sm {
    max-width: 300px;
  }
}
@media (min-width: 992px) {
  .modal-lg,
  .modal-xl {
    max-width: 800px;
  }
}
@media (min-width: 1200px) {
  .modal-xl {
    max-width: 1140px;
  }
}

#close {
  background-color: #6d6f70;
  border-color: #6d6f70;
  color: #ffffff;
}
#close:hover {
  background: #ffffff;
  border-color: #6d6f70;
  color: #6d6f70;
}
.invalid-feedback {
  display: none;
  width: 100%;
  margin-top: 0.25rem;
  font-size: 80%;
  color: #dc3545;
}
.avatar {
  width: 200px;
  border-radius: 50%;
  box-shadow: 0px 10px 10px 10px rgba(0, 0, 0, 0.2);
}
</style>
