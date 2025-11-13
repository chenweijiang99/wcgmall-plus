<script setup>
import Comment from "@/components/Comment.vue";
import { computed, watch, ref, onMounted } from "vue";
import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
import { useScrollToTop } from "@/assets/base.js";
import { ElMessageBox, ElMessage } from "element-plus";
const { showLoader, backtopStyle, handleMouseEnter, handleMouseLeave } =
  useScrollToTop();
const productDetail = ref([]);
import { userGetProductByIdService } from "@/api/product.js";
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();
const productId = route.query.id;
const getProductDetail = async () => {
  let result = await userGetProductByIdService(productId);
  productDetail.value = result.data;
  detailImages.value = JSON.parse(productDetail.value.detailImages);
  descriptionImages.value = JSON.parse(productDetail.value.descriptionImage);
  selectImage(0);
};
const descriptionImages = ref([]);
const detailImages = ref([]);
const selectedImageIndex = ref(0);
const selectedImage = ref(detailImages.value[selectedImageIndex.value]);
const selectImage = (index) => {
  selectedImageIndex.value = index;
  selectedImage.value = detailImages.value[index];
};
import emitter from "@/event/eventBus.js";
import { userAddProductToWishListService } from "@/api/product.js";
// 添加商品到心愿单
const addProductToWishList = async (row) => {
  let result = await userAddProductToWishListService(row.id);
  if (result.code === 200) {
    ElMessage({
        showClose: true,
        type: "success",
        message: "添加成功",
        plain: true,
      });
    //发送事件，刷新头部
    emitter.emit("refresh");
  } else {
    ElMessage.error(result.message ? result.message : "添加失败");
  }
};

import { userAddProductToShoppingCartService } from "@/api/shoppingCart.js";
//添加商品到购物车
const addProductToCart = async (row) => {
  let result = await userAddProductToShoppingCartService(row.id);
  if (result.code === 200) {
    ElMessage({
        showClose: true,
        type: "success",
        message:  "添加成功",
        plain: true,
      });
    //发送事件，刷新头部
    emitter.emit("refresh");
  } else {
    ElMessage.error(result.message ? result.message : "添加失败");
  }
};

watch(selectedImageIndex, () => {
  selectedImage.value = detailImages.value[selectedImageIndex.value];
});



onMounted(() => {
  getProductDetail();

  // setInterval(() => {
  //   selectedImageIndex.value =
  //     (selectedImageIndex.value + 1) % detailImages.value.length;
  // }, 5000);
});

const rate = ref(3.4);
</script>
<template>
  <!-- breadcrumb -->
  <div class="container header-mt">
    <div class="row">
      <div class="col-12">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb custom-breadcrumb">
            <li class="breadcrumb-item">
              <router-link to="/index"> 主页 </router-link>
            </li>
            <li class="breadcrumb-item">
              <router-link to="/shop">商品 </router-link>
            </li>
            <li class="breadcrumb-item active" aria-current="page">商品详情</li>
          </ol>
        </nav>
      </div>
    </div>
  </div>
  <!-- end breadcrumb -->
  <!-- main content -->
  <div class="main-content pb-50">
    <div class="container">
      <div class="row">
        <!-- product image gallery -->
        <div class="col-lg-7">
          <div class="gallery-on-top">
            <transition name="scale" mode="out-in">
              <img :key="selectedImage"  :src="selectedImage" alt="" class="product_image" />
          </transition>
          </div>
          <!-- product thumbnails -->
          <div class="product-details">
            <div class="product-images">
              <div class="thumbnail-images">
                <ul>
                  <li v-for="(image, index) in detailImages" :key="index">
                    <img :src="image" @click="selectImage(index)" />
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
        <!-- product main information -->
        <div class="col-lg-5 product-info">
          <h6 class="product-info__title">{{ productDetail.name }}</h6>
          <div class="product-info__rate">
            <!-- <el-rate
              v-model="rate"
              disabled
              show-score
            /> -->
            <!-- <div class="product-rate">
              <div class="product-rate__star">
                <span class="rate-3_5"></span>
              </div>
              <div class="rate-number">(.5)</div>
            </div> -->
          </div>
          <div class="product-info__price">
            <span>￥{{ productDetail.price }}</span>
            <!-- <span class="old">￥99999</span>
            <span class="badge price-badge">折扣</span> -->
          </div>
          <div class="product-info__desc">
            
            <div class="designer">
              <div>
                <strong>库存</strong>
                <span>&nbsp;&nbsp;{{ productDetail.inventory }}</span>
              </div>
              <div>
                <strong>品牌</strong>
                <span>&nbsp;&nbsp;{{ productDetail.brandName }}</span>
              </div>
              <div>
                <strong>分类</strong>
                <span>&nbsp;&nbsp;{{ productDetail.categoryName }}</span>
              </div>
            </div>
            <strong>详情</strong>
            <p>
              {{ productDetail.description }}
            </p>
          </div>

          <div class="product-info__shop">
            <a href="#" class="btn add-to-wish-list-btn"
            @click="addProductToWishList(productDetail)"
              ><i class="bi-heart"></i>加入心愿单</a
            >
            <a
              href="#"
              class="btn add-to-cart-btn"
              @click="addProductToCart(productDetail)"
              ><i class="bi-cart-plus"></i>添加购物车</a
            >
           
          </div>
          
        </div>
      </div>
      <!-- product details tabs -->
      <div class="product-details my-100">
        <ul class="nav nav-pills nav-fill product-details__tabs">
          <li class="nav-item">
            <a class="nav-link1 active">商品详情</a>
          </li>
        </ul>
        <div class="tab-content product-details__content">
          <!-- product description tab -->
          <div class="tab-pane fade product-details__content--desc show active">
            <p style="text-align: center">
              <img
                v-for="item in descriptionImages"
                :key="item.id"
                :src="item"
                alt=""
              />
            </p>
          </div>
        </div>
      </div>
      <!-- end product details tabs -->


      <!-- 引用评论组件 -->
      <Comment :module="'product'" />

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
</template>

<style scoped>
@import "@/assets/main.css";
a {
  text-decoration: none;
}

.product-images {
  display: flex;
}

.main-image img {
  max-width: 100%;
  height: auto;
}

.thumbnail-images ul {
  list-style: none;
  padding: 0;
  display: flex;
}

.thumbnail-images li {
  margin-right: 10px;
  cursor: pointer;
}

.thumbnail-images img {
  border: 3px solid transparent; /* 设置初始边框样式为透明 */
  transition: border-color 0.3s; /* 添加过渡效果 */
  width: 80px;
  max-width: 100%;
  height: auto;
}
.thumbnail-images img:hover {
  border-color: #79aee0; /* 鼠标移入时显示边框 */
}

.add-to-cart-btn{
  background-color: #474747;
  border-color: #474747;
  color: #ffffff;
}
.add-to-cart-btn:hover{
  background-color: #ffffff;
  border-color: #474747;
  color: #474747;
}
.add-to-wish-list-btn:hover {
  background: #ffffff;
  color: #79aee0;
  border-color: #79aee0;
}
.add-to-wish-list-btn {
  background-color: #79aee0;
  border-color: #79aee0;
  color: #ffffff;
}

.buy-now-btn:hover {
  background: #ffffff;
  color: #e96161;
  border-color: #e96161;
}
.buy-now-btn {
  background-color: #e96161;
  border-color: #e96161;
  color: #ffffff;
}

/* 定义过渡效果 */
.slide-enter-active,
.slide-leave-active {
  transition: transform 0.5s ease, opacity 0.5s ease;
}
.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

.scale-enter-active {
  transition: transform 0.5s ease;
}
.scale-enter-from {
  transform: scale(0.5);
}
</style>