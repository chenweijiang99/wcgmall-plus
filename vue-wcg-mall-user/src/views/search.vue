<script setup>
import { computed, watch, ref, onMounted, inject } from "vue";
import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
import { useScrollToTop } from "@/assets/base.js";
import emitter from "@/event/eventBus.js";
import { useRoute,useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
const route = useRoute();
const router = useRouter()
const products = ref([]);
const currentPage = ref(1);
const productsPerPage = 8;
const totalProducts = computed(() => products.value.length);
const totalPages = computed(() =>
  Math.ceil(totalProducts.value / productsPerPage)
);
const startResult = computed(
  () => (currentPage.value - 1) * productsPerPage + 1
);
const endResult = computed(() => {
  const end = currentPage.value * productsPerPage;
  return end > totalProducts.value ? totalProducts.value : end;
});
const displayedProducts = computed(() => {
  const start = (currentPage.value - 1) * productsPerPage;
  const end = currentPage.value * productsPerPage;
  return products.value.slice(start, end);
});

const changePage = (page) => {
  currentPage.value = page;
};

import {userSearchProductService,userAddProductToWishListService} from "@/api/product.js";
let searchQuery = route.query.searchQuery;
//获取商品
const getProductList = async () => {
  const result = await userSearchProductService(searchQuery);
  products.value = result.data;
};
//刷新
const refreshSearch = ()=>{
    searchQuery= route.query.searchQuery;
        console.log("refreshSearch========",searchQuery);
        getProductList();
}

import { userAddProductToShoppingCartService } from "@/api/shoppingCart.js";
//添加商品到购物车
const addProductToCart = async (row) => {
  let result = await userAddProductToShoppingCartService(row.id);
  if (result.code === 1) {
    ElMessage({
        showClose: true,
        type: "success",
        message: result.message ? result.message : "添加成功",
        plain: true,
      });
    //发送事件，刷新头部
    emitter.emit("refresh");
  } else {
    ElMessage.error(result.message ? result.message : "添加失败");
  }
};

// 添加商品到心愿单
const addProductToWishList = async (row) => {
  let result = await userAddProductToWishListService(row.id);
  if (result.code === 1) {
    ElMessage({
        showClose: true,
        type: "success",
        message: result.message ? result.message : "添加成功",
        plain: true,
      });
    //发送事件，刷新头部
    emitter.emit("refresh");
  } else {
    ElMessage.error(result.message ? result.message : "添加失败");
  }
};

// 获取商品详情
const getProductDetail = (id) => {
  router.push({
    path: "/productDetail",
    query: { productId: id },
  });
};
onMounted(()=>{
    getProductList();
    emitter.on("refreshSearch",()=>{
        refreshSearch();
    })
})
</script>
<template>
  <!-- breadcrumb -->
  <div class="container header-mt">
    <div class="row">
      <div class="col-12 d-flex justify-content-between align-items-center">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb custom-breadcrumb m-0">
            <li class="breadcrumb-item">
              <router-link to="/index"> 主页 </router-link>
            </li>
            <li class="breadcrumb-item active" aria-current="page">搜索</li>
          </ol>
        </nav>
      </div>
    </div>
  </div>
  <!-- end breadcrumb -->
  <div class="main-content pt-50 pb-80">
    <div class="container">
        <p>搜索关键字：{{ searchQuery }}</p>
      <!-- product list -->
      <div class="row">
        <p v-if="products.length === 0">暂未搜索到相应内容</p>
        <div
          class="col-lg-3 col-md-6"
          v-for="(product, index) in displayedProducts"
          :key="index"
        >
          <div class="card-item card-item--light mb-30">
            <div class="card-item__bg">
              <img :src="product.image" alt="" />
              <span class="badge card-item__bg--badge">{{
                product.label
              }}</span>
            </div>
            <div class="card-item__body">
              <div class="card-item__body--price">
                <strong>￥{{ product.price }}</strong>
              </div>
              <div class="card-item__body--title">
                <a href="javascript:void(0)">
                  <h4>{{ product.name }}</h4>
                </a>
              </div>
              <div class="product-rate">
                <div v-for="brand in brandList" :key="brand.id">
                  <div v-if="product.brandId == brand.id">
                    <label class="brand">品牌</label>
                    <el-tag type="success" size="small">
                      {{ brand.name }}</el-tag
                    >
                  </div>
                </div>
                <div v-for="category in categoryList" :key="category.id">
                  <div v-if="product.categoryId == category.id">
                    <label class="category">分类</label>
                    <el-tag type="info" size="small">
                      {{ category.name }}</el-tag
                    >
                  </div>
                </div>
              </div>
            </div>
            <div class="card-item__overlay">
              <ul class="action-btn">
                <li>
                  <a
                    href=""
                    class="icon-box"
                    @click="getProductDetail(product.id)"
                    ><i class="icon-search"></i
                  ></a>
                </li>
                <li class="wishlist-btn" @click="addProductToWishList(product)">
                  <span class="icon-box"
                    ><i class="wishlist__icon icon-heart"></i
                  ></span>
                </li>
              </ul>
              <a class="btn" @click="addProductToCart(product)">添加到购物车</a>
            </div>
          </div>
        </div>
      </div>
      <!-- end product list -->
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
                  :class="{ 'page-item': true, active: currentPage === page }"
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
</template>
<style scoped>
@import "@/assets/main.css";
a {
  text-decoration: none;
}
</style>