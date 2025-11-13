<script setup>
import { computed, watch, ref, onMounted } from "vue";
import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
import { useScrollToTop } from "@/assets/base.js";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete } from "@element-plus/icons-vue";
const { showLoader, backtopStyle, handleMouseEnter, handleMouseLeave } =
  useScrollToTop();

const backgroundImage = new URL(
  "@/assets/images/banners/banner-bg-08.jpg",
  import.meta.url
).href;

const carts = ref([]);

import {
  userGetShoppingCartService,
  userDeleteCartDataService,
  userAddProductToShoppingCartDataService,
  userReduceCartDataService,
  userAddCartProductToWishListService,
} from "@/api/shoppingCart.js";
// const getUserCart = async () => {
//   let result = await userGetShoppingCartService();
//   if (result.code === 200) {
//     carts.value = result.data;
//   }
// };
const getUserCart = async () => {
  let result = await userGetShoppingCartService();
  if (result.code === 200) {
    carts.value = result.data.map((item) => ({ ...item, selected: false }));
  }
};


//总价
// const totalPrice = computed(() => {
//   return carts.value.reduce((acc, item) => {
//     return acc + item.productPrice * item.number;
//   }, 0);
// });
const totalPrice = computed(() => {
  return carts.value.reduce((acc, item) => {
    return item.selected ? acc + item.productPrice * item.number : acc;
  }, 0);
});

//折扣
const discount = ref(0);
//删除
import emitter from "@/event/eventBus.js";
const deleteCart = async (item) => {
  ElMessageBox.confirm("是否删除商品？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    let result = await userDeleteCartDataService(item.productId);
    if (result.code === 200) {
      ElMessage({
        showClose: true,
        type: "success",
        message: "删除成功",
        plain: true,
      });
      emitter.emit("refresh");
      getUserCart(); // 在删除成功后重新获取购物车数据
    } else {
      ElMessage.error(result.message ? result.message : "删除失败");
    }
  });
};

//减少
const decreaseQuantity = async (item) => {
  if (item.number > 1) {
    let result = await userReduceCartDataService(item.productId);
    if (result.code === 200) {
      ElMessage({
        showClose: true,
        type: "success",
        message: "减少成功",
        plain: true,
      });
      emitter.emit("refresh");
      getUserCart();
    } else {
      ElMessage.error(result.message ? result.message : "减少失败");
    }
  } else if (item.number == 1) {
    deleteCart(item);
  }
};
//增加
const increaseQuantity = async (item) => {
  let result = await userAddProductToShoppingCartDataService(item.productId);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message:  "增加成功",
      plain: true,
    });
    emitter.emit("refresh");
    getUserCart();
  } else {
    ElMessage.error(result.message ? result.message : "增加失败");
  }
};

const addCartProductToWishList = async (item) => {
  ElMessageBox.confirm("是否将商品添加到收藏夹？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    let result = await userAddCartProductToWishListService(item.productId);
    if (result.code === 200) {
      ElMessage({
        showClose: true,
        type: "success",
        message:  "添加成功",
        plain: true,
      });
      emitter.emit("refresh");
      getUserCart();
    } else {
      ElMessage.error(result.message ? result.message : "添加失败");
    }
  });
};

import { useRouter } from "vue-router";

const router = useRouter();
// const checkout = () => {
//  if(carts.value.length == 0){
//   ElMessage.warning("购物车为空");
//  }else{
//   router.push("/checkout");
//  }

// };
const checkout = () => {
  const selectedItems = carts.value.filter((item) => item.selected);
  if (selectedItems.length == 0) {
    ElMessage.warning("请选择要结算的商品");
  } else {
    router.push({
      path: "/checkout",
      query: { items: JSON.stringify(selectedItems) },
    });
  }
};

onMounted(() => { 
  getUserCart();
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
            <li class="breadcrumb-item active" aria-current="page">购物车</li>
          </ol>
        </nav>
      </div>
    </div>
  </div>
  <!-- end breadcrumb -->
  <!-- main content -->
  <div class="main-content pb-80">
    <div class="container">
      <!-- product cart -->
      <div class="row" id="cartStyle">
        <!-- title -->
        <div class="col-12">
          <h1 class="main-title">购物车</h1>
        </div>
        <!-- cart items -->
        <div class="col-lg-9">
          <div class="item-cart" id="noBorder">
            <div class="item-cart__row" v-for="item in carts" :key="item">
              <div class="item-cart__row--img">
                <a href="javascript:void(0)">
                  <el-checkbox
                    v-model="item.selected"
                    style="padding: 10px"
                  ></el-checkbox>

                  <el-tooltip
                    content="添加到愿望单"
                    placement="top"
                    effect="light"
                  >
                    <el-button
                      size="small"
                      type="warning"
                      @click="addCartProductToWishList(item)"
                      circle
                    >
                      <i class="bi-heart"></i>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="删除" placement="top" effect="light">
                    <el-button
                      size="small"
                      type="danger"
                      @click="deleteCart(item)"
                      circle
                      :icon="Delete"
                    >
                    </el-button>
                  </el-tooltip>
                  <img :src="item.productImage" alt="" />
                  <span>{{ item.productName }}</span>
                </a>
              </div>
              <div class="item-cart__row--details">
                <span class="per-price">￥{{ item.productPrice }}</span>
                <div class="quantity">
                  <span class="quantity__minus" @click="decreaseQuantity(item)"
                    ><i class="bi-dash-circle"></i
                  ></span>
                  <input
                    class="quantity__input"
                    type="text"
                    :value="item.number"
                    size="2"
                  />
                  <span class="quantity__plus" @click="increaseQuantity(item)"
                    ><i class="bi-plus-circle"></i
                  ></span>
                </div>
                <span class="total-price"
                  ><strong
                    >￥{{ item.productPrice * item.number }}</strong
                  ></span
                >
              </div>
            </div>
          </div>
        </div>
        <!-- cart total info -->
        <div class="col-lg-3">
          <!-- total info -->
          <div class="total-info">
            <h2 class="total-info__title">购物车总价</h2>
            <div class="total-info__subprice">
              <span>总价</span>
              <span>￥{{ totalPrice }}</span>
            </div>
            <div class="total-info__totalprice">
              <span>折后</span>
              <span>￥{{ totalPrice - (totalPrice * discount) / 100 }}</span>
            </div>
            <div class="total-info__btn">
              <a
                href="javascript:void(0)"
                class="btn btn-block"
                @click="checkout"
              >
                支付</a
              >
            </div>
          </div>
        </div>
      </div>
      <!-- subscribe call to action -->
      <div
        class="newsletter-banner mt-100"
        :style="{ 'background-image': 'url(' + backgroundImage + ' )' }"
      >
        <!-- <div class="banner__overlay"></div> -->
        <div class="container">
          <div class="newsletter-banner__content">
            <h3>每日优惠和折扣</h3>
            <form>
              <div class="form-row">
                <div class="col-lg-4 col-md-6 col-sm-8 col-10 mx-auto">
                  <div class="input-group ltr">
                    <input
                      type="email"
                      class="form-control"
                      id="subscribeInputEmail"
                      placeholder="您的邮箱"
                      aria-describedby="Emailsubscribe"
                    />
                    <div class="input-group-append">
                      <button class="btn" type="submit" id="Emailsubscribe">
                        订阅
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
      <!-- end subscribe call to action -->
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
</style>