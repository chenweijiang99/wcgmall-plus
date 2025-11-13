<script setup>
import { onMounted, ref, watch, computed, inject } from "vue";
//tap栏选中控制
const tap = ref(1);

/**
 * 搜索框控制
 */
const showSearchBox = ref(false);
const searchToggle = () => {
  if (showSearchBox.value === false) {
    showSearchBox.value = true;
  } else {
    showSearchBox.value = false;
  }
};

/**
 * 用户信息控制
 */
const showUserBox = ref(false);
const userToggle = () => {
  if (showUserBox.value === false) {
    showUserBox.value = true;
    showCartBox.value = false;
    showWishListBox.value = false;
  } else {
    showUserBox.value = false;
  }
};
/**
 * 购物车控制
 */
const showCartBox = ref(false);
const cartToggle = () => {
  if (showCartBox.value === false) {
    showCartBox.value = true;
    showUserBox.value = false;
    showWishListBox.value = false;
  } else {
    showCartBox.value = false;
  }
};
/**
 * 心愿单控制
 */
const showWishListBox = ref(false);
const wishListToggle = () => {
  if (showWishListBox.value === false) {
    showWishListBox.value = true;
    showCartBox.value = false;
    showUserBox.value = false;
  } else {
    showWishListBox.value = false;
  }
};

import { getUserInfoService, logoutService } from "@/api/user";

// 登录信息
const loginData = ref({
  email: "",
  password: "",
});

import { useTokenStore } from "@/stores/token.js";
import { useRouter, useRoute } from "vue-router";
const router = useRouter();
const route = useRoute();
const tokenStore = useTokenStore();
import { userLoginService } from "@/api/user.js";
import { ElMessage, ElMessageBox } from "element-plus";
import useUserInfoStore from "@/stores/userInfo.js";
const userInfoStore = useUserInfoStore();

const emailError = ref("");
const validateEmail = () => {
  const email = document.getElementById("email");
  if (!email.value) {
    emailError.value = "邮箱不能为空";
  } else if (!/\S+@\S+\.\S+/.test(email.value)) {
    emailError.value = "请输入有效的邮箱地址";
  } else {
    emailError.value = "";
  }
};

const passwordError = ref("");
const validatePassword = () => {
  const password = document.getElementById("password");
  if (password.value === "") {
    passwordError.value = "请输入密码";
  } else if (password.value.length < 6 || password.value.length > 12) {
    passwordError.value = "密码长度在6-12个字符之间";
  } else {
    passwordError.value = "";
  }
};
// 登录
const isLogin = ref(false);
const login = async () => {
  if (
    emailError.value != "" ||
    passwordError.value != "" ||
    loginData.value.email == "" ||
    loginData.value.password == ""
  ) {
    ElMessage({
      showClose: true,
      type: "error",
      message: "请检查邮箱或密码是否符合规则",
      plain: true,
    });
    return;
  }
  let result = await userLoginService(loginData.value);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message: "登录成功",
      plain: true,
    });

    tokenStore.setToken(result.data);
    showUserBox.value = false;
    isLogin.value = true;
    emitter.emit("refresh");
    router.push("/");
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "登录失败",
      plain: true,
    });
  }
};
// 退出
const logout = async () => {
  let result = await logoutService();
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      message: "退出成功",
      plain: true,
    });
    isLogin.value = false;
    showUserBox.value = false;
    tokenStore.removeToken();
    userInfoStore.removeInfo();
    shoppingCart.value = "";
    wishList.value = "";
    avatar.value = "";
    userInfo.value = "";
    router.push("/login");
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "退出失败",
      plain: true,
    });
  }
};

const userInfo = ref({
  id: "",
  createTime: "",
  updateTime: "",
  username: "",
  nickName: "",
  email: "",
  phone: "",
  avatar: "",
});

const avatar = ref("");

// 获取用户信息
const getUserInfo = async () => {
  if (tokenStore.token !== "") {
    let result = await getUserInfoService();
    if (result.code === 200) {
      userInfo.value = result.data;
      userInfoStore.setInfo(userInfo.value);
      avatar.value = userInfo.value.avatar;
    }
  }
};

const shoppingCart = ref([]);
const wishList = ref([]);
import {
  userGetShoppingCartService,
  userDeleteCartService,
  userAddCartProductToWishListService,
} from "@/api/shoppingCart.js";
// 获取购物车信息
const getShoppingCart = async () => {
  if (tokenStore.token !== "") {
    let result = await userGetShoppingCartService();
    if (result.code === 200) {
      shoppingCart.value = result.data;
    }
  }
};

import {
  userGetWishListService,
  userDeleteWishListService,
  userAddWishListProdcutToCartService,
} from "@/api/wishList.js";
// 获取心愿单信息
const getWishList = async () => {
  if (tokenStore.token !== "") {
    let result = await userGetWishListService();
    if (result.code === 200) {
      wishList.value = result.data;
    }
  }
};

// 删除购物车信息
const deleteCart = async (row) => {
  let result = await userDeleteCartService(row.productId);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message: "删除成功",
      plain: true,
    });
    const index = shoppingCart.value.findIndex(
      (item) => item.productId === row.productId
    );
    if (index !== -1) {
      const cart = shoppingCart.value[index];
      if (cart.number === 1) {
        shoppingCart.value.splice(index, 1);
      } else if (cart.number > 1) {
        getShoppingCart();
      }
    }
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "删除失败",
      plain: true,
    });
  }
};
// 删除心愿单信息
const deleteWishList = async (row) => {
  let result = await userDeleteWishListService(row.productId);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message: "删除成功",
      plain: true,
    });
    emitter.emit("refreshWishList");
    const index = wishList.value.findIndex((item) => item.productId === row.productId);
    if (index !== -1) {
      wishList.value.splice(index, 1);
    }
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "删除失败",
      plain: true,
    });
  }
};
//添加心愿单信息到购物车
const addWishListToCart = async (row) => {
  let result = await userAddWishListProdcutToCartService(row.productId);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message: "添加成功",
      plain: true,
    });
    emitter.emit("refreshWishList");
    const index = wishList.value.findIndex((item) => item.productId === row.productId);
    if (index !== -1) {
      wishList.value.splice(index, 1);
      getShoppingCart();
    }
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "添加失败",
      plain: true,
    });
  }
};
// 添加购物车信息到心愿单
const addCartProductToWishList = async (row) => {
  let result = await userAddCartProductToWishListService(row.productId);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message: "添加成功",
      plain: true,
    });
    emitter.emit("refresh");
    const index = shoppingCart.value.findIndex(
      (item) => item.productId === row.productId
    );
    if (index !== -1) {
      shoppingCart.value.splice(index, 1);
      getWishList();
    }
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "添加失败",
      plain: true,
    });
  }
};
//购物车总价格
const shoppingCartTotalPrice = computed(() => {
  let total = 0;
  for (const item of shoppingCart.value) {
    total += item.productPrice * item.number;
  }
  return total;
});
//购物车总数量
const shoppingCartTotal = computed(() => {
  let total = 0;
  for (const item of shoppingCart.value) {
    total += item.number;
  }
  return total;
});

const searchQuery = ref("");
const handleSearch = () => {
  if (searchQuery.value) {
    const currentPath = router.currentRoute.value.path;
    if (currentPath === "/search") {
      // 如果已经在搜索页面，调用刷新数据的方法
      router.replace({ path: "/search", query: { searchQuery: searchQuery.value } });
      emitter.emit("refreshSearch");
      // router.go(0);
    } else {
      // 跳转到搜索页面，并传递搜索参数
      router.push({ path: "/search", query: { searchQuery: searchQuery.value } });
    }
  }
};
import emitter from "@/event/eventBus.js";
onMounted(() => {
  getUserInfo();
  getShoppingCart();
  getWishList();

  //监听购物车,心愿单,用户信息的更新
  emitter.on("refresh", () => {
    getShoppingCart();
    getWishList();
    getUserInfo();
  });
  emitter.on("clear", () => {
    // 监听清空信息
    tokenStore.removeToken();
    userInfoStore.removeInfo();
    userInfo.value = "";
    shoppingCart.value = "";
    wishList.value = "";
    avatar.value = "";
  });
  emitter.on("login", () => {
    // 监听登录
    isLogin.value = true;
  });
});
</script>
<template>
  <header>
    <!-- main header navbar -->
    <nav class="navbar navbar-expand-lg navbar-light custom-navbar" id="mainMenu">
      <div class="container">
        <router-link to="/index">
          <a class="navbar-brand" href="">
            <img src="@/assets/images/logo-header.jpg" alt="" />
          </a>
        </router-link>
        <!--  navbar actions -->
        <div class="main-navbar-action">
          <!-- navbar search box -->
          <!-- <div class="search-wrapper">
            <div class="search-wrapper__btn" @click="searchToggle">
              <i class="icon-search"></i>
            </div>
            <div class="search-wrapper__box" v-show="showSearchBox">
              <div class="search-box" id="the-basics">
                <form @submit.prevent="handleSearch">
                  <div class="form-group form-input form-input--search">
                    <input
                      type="text"
                      class="form-control typeahead"
                      id="search"
                      name="search"
                      v-model="searchQuery"
                      placeholder="输入关键字后回车搜索"
                      @keyup.enter="handleSearch"
                    />
                  </div>
                </form>
              </div>
            </div>
          </div> -->
          <!-- end of navbar search box -->
          <div id="mainNavbarDropdown">
            <!-- navbar user account dropdown -->
            <div
              class="dropdown-wrapper"
              id="usermenu"
              data-collapse="false"
              v-show="showUserBox"
            >
              <div class="account-wrapper">
                <!-- login form wrapper -->
                <div>
                  <div class="account-wrapper__heading">
                    <span>登录</span>
                    <span class="account-wrapper__heading--link"
                      >您是否拥有一个账号?
                      <router-link to="/register">
                        <a href="">注册</a>
                      </router-link>
                    </span>
                  </div>
                  <div class="account-wrapper__content">
                    <form class="custom-form" :model="loginData" @submit.prevent="login">
                      <div class="form-group custom-form__input">
                        <input
                          type="email"
                          class="form-control"
                          id="email"
                          placeholder="邮箱"
                          v-model="loginData.email"
                          @blur="validateEmail"
                        />
                        <small v-if="emailError"> {{ emailError }}</small>
                      </div>
                      <div class="form-group custom-form__input">
                        <div class="input-box password-box">
                          <input
                            type="password"
                            class="form-control"
                            id="password"
                            placeholder="密码"
                            v-model="loginData.password"
                            @blur="validatePassword"
                          />
                          <small v-if="passwordError"> {{ passwordError }}</small>
                        </div>
                      </div>
                      <div class="forgot-pass">
                        <router-link to="/resetPassword">
                          <a href="">
                            <small>忘记密码?</small>
                          </a>
                        </router-link>
                      </div>
                      <div class="custom-form__btn">
                        <button type="submit" class="btn submit-btn">登录</button>
                      </div>
                    </form>
                  </div>
                </div>

                <!-- account links when user is logged in-->
                <!-- <div v-if="showUserInfo">
                  <router-link to="/account">
                    <a class="dropdown-item" href="">
                      <span> <i class="icon-user-profile"></i></span>个人资料
                    </a>
                  </router-link>
                  <a class="dropdown-item" href="javascript:void(0)" @click="logout"
                    ><span><i class="icon-log-out"></i></span>退出登录</a
                  >
                </div> -->
              </div>
            </div>
            <!-- navbar cart dropdown -->

            <div
              class="dropdown-wrapper"
              id="cartmenu"
              data-collapse="false"
              v-show="showCartBox"
            >
              <div v-if="shoppingCart.length > 0">
                <div class="dropdown-wrapper__item">
                  <div class="media" v-for="item in shoppingCart" :key="item">
                    <div class="media__img">
                      <el-tooltip content="删除" placement="top" effect="light">
                        <a
                          class="delete-icon"
                          href="javascript:void(0)"
                          @click="deleteCart(item)"
                          ><i class="icon-bin"></i
                        ></a>
                      </el-tooltip>
                      <el-tooltip content="添加到愿望单" placement="top" effect="light">
                        <a
                          class="delete-icon"
                          href="javascript:void(0)"
                          @click="addCartProductToWishList(item)"
                          ><i class="bi-heart"></i
                        ></a>
                      </el-tooltip>
                      <a href="javascript:void(0)"
                        ><img :src="item.productImage" alt=""
                      /></a>
                    </div>
                    <div class="media-body">
                      <a href="javascript:void(0)">
                        <h6 class="media-title">
                          {{ item.productName }} x {{ item.number }}
                        </h6>
                      </a>
                      <small class="media-details">￥{{ item.productPrice }}</small>
                    </div>
                  </div>
                </div>
                <div class="dropdown-divider"></div>
                <div class="dropdown-wrapper__item total-price">
                  <span
                    >总价: <span>￥ {{ shoppingCartTotalPrice }}</span></span
                  >
                </div>
                <div class="dropdown-divider"></div>
                <div class="dropdown-wrapper__item cart-btn">
                  <router-link to="/checkout">
                    <a href="" class="btn cart-btn__checkout">去结账</a></router-link
                  >
                  <router-link to="/cart">
                    <a href="" class="btn cart-btn__details">购物车</a>
                  </router-link>
                </div>
              </div>
              <!-- the cart is empty -->
              <div class="empty-cart" v-if="shoppingCart.length == 0">
                <i class="icon-close empty-cart__icon"></i>
                <p class="empty-cart__text">您的购物车是空的</p>
              </div>
            </div>

            <!-- navbar wishlist dropdown -->
            <div
              class="dropdown-wrapper"
              id="cartmenu"
              data-collapse="false"
              v-show="showWishListBox"
            >
              <div v-if="wishList.length > 0">
                <div class="dropdown-wrapper__item">
                  <div class="media" v-for="item1 in wishList" :key="item1">
                    <div class="media__img">
                      <el-tooltip content="删除" placement="top" effect="light">
                        <a
                          class="delete-icon"
                          href="javascript:void(0)"
                          @click="deleteWishList(item1)"
                          ><i class="icon-bin"></i
                        ></a>
                      </el-tooltip>
                      <el-tooltip content="添加到购物车" placement="top" effect="light">
                        <a
                          class="delete-icon"
                          href="javascript:void(0)"
                          @click="addWishListToCart(item1)"
                          ><i class="icon-shopping-bag"></i
                        ></a>
                      </el-tooltip>
                      <a href="javascript:void(0)"
                        ><img :src="item1.productImage" alt=""
                      /></a>
                    </div>
                    <div class="media-body">
                      <a href="javascript:void(0)">
                        <h6 class="media-title">{{ item1.productName }}</h6>
                      </a>
                      <small class="media-details">￥{{ item1.productPrice }}</small>
                    </div>
                  </div>
                </div>
                <div class="dropdown-divider"></div>
              </div>
              <!-- the cart is empty -->
              <div class="empty-cart" v-if="wishList.length == 0">
                <i class="icon-close empty-cart__icon"></i>
                <p class="empty-cart__text">您的愿望单是空的</p>
              </div>
            </div>
          </div>
          <!-- navbar user account dropdown -->
          <!-- <div
            class="main-navbar-action__btn nav-dropdown"
            @click="userToggle"
            v-if="!isLogin"
          >
            <a class="dropdown-link" data-target="usermenu">
              <i class="icon-user"></i>
            </a>
          </div> -->
          <!-- navbar cart dropdown -->
          <el-tooltip content="购物车" placement="bottom" effect="light">
            <div class="main-navbar-action__btn nav-dropdown" @click="cartToggle">
              <a class="dropdown-link" data-target="cartmenu">
                <span class="cart-badge">{{ shoppingCartTotal }}</span>
                <i class="icon-shopping-bag" v-if="!showCartBox"></i>
                <el-icon v-if="showCartBox"><More /></el-icon>
              </a>
            </div>
          </el-tooltip>
          <!-- navbar wishlist dropdown -->
          <el-tooltip content="愿望单" placement="bottom" effect="light">
            <div class="main-navbar-action__btn nav-dropdown" @click="wishListToggle">
              <a class="dropdown-link" data-target="cartmenu">
                <span class="cart-badge">{{ wishList.length }}</span>
                <i class="bi-heart" v-if="!showWishListBox"></i>
                <el-icon v-if="showWishListBox"><More /></el-icon>
              </a>
            </div>
          </el-tooltip>
          <!-- navbar actions content -->
          <el-dropdown v-if="avatar">
            <img class="avatar" :src="avatar" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  ><router-link to="/resetPassword">
                    重置密码
                  </router-link></el-dropdown-item
                >
                <el-dropdown-item
                  ><router-link to="/account"> 我的账户 </router-link></el-dropdown-item
                >
                <el-dropdown-item
                  ><router-link to="/account?activeTap=2 ">
                    最近订单
                  </router-link></el-dropdown-item
                >
                <el-dropdown-item @click="logout"> 退出登录 </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <div
            v-else
            class="main-navbar-action__btn nav-dropdown"
            @click="router.push('/login')"
          >
            <a class="dropdown-link" data-target="cartmenu"> 登录 </a>
          </div>
        </div>

        <button
          class="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#mainNavbar"
          aria-controls="mainNavbar"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNavbar">
          <ul class="navbar-nav main-navbar">
            <li class="nav-item main-navbar__item">
              <router-link to="/index"><a class="nav-link" href="">主页</a></router-link>
            </li>
            <li class="nav-item main-navbar__item">
              <router-link to="/shop"><a class="nav-link" href="">商品</a></router-link>
            </li>
            <li class="nav-item main-navbar__item">
              <router-link to="/cart"><a class="nav-link" href="">购物车</a></router-link>
            </li>
            <li class="nav-item main-navbar__item">
              <router-link to="/blog"><a class="nav-link" href="">博客</a></router-link>
            </li>

            <li class="nav-item main-navbar__item">
              <router-link to="/contact"
                ><a class="nav-link" href="">联系</a></router-link
              >
            </li>
            <li class="nav-item main-navbar__item">
              <router-link to="/about">
                <a class="nav-link" href="">关于</a>
              </router-link>
            </li>
          </ul>
        </div>
      </div>
    </nav>
    <!-- end main header navbar -->
  </header>
</template>

<style scoped>
@import "@/assets/main.css";
a {
  text-decoration: none;
}
.avatar {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  margin-left: 20px;
}
small {
  margin-left: 1rem;
  color: darkgrey;
}
.nav-link:hover {
  color: #1c94da;
  font: 1em sans-serif;
}
</style>
