<script setup>
import { computed, watch, ref, onMounted, inject, nextTick } from "vue";
import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
import { useScrollToTop } from "@/assets/base.js";
import { ElMessage, ElMessageBox, ElNotification } from "element-plus";

// const toRefreshHeader = inject("toRefreshHeader");
import { useRoute, useRouter } from "vue-router";
const router = useRouter();
const { showLoader, backtopStyle, handleMouseEnter, handleMouseLeave } = useScrollToTop();
// 筛选弹出框
const showOverlay = ref(false);
const showCanvas = () => {
  showOverlay.value = true;
};
//筛选时分类选择模型
const categoryChooseModel = ref([]);
//筛选时品牌选择模型
const brandChooseModel = ref([]);
// 筛选标记
const isFilter = ref(false);
const isSearch = ref(false);
// 价格区间
const priceValue = ref([0, 0]);
// 搜索名称
const searchNames = ref([]);

// 商品列表
const products = ref([]);

import {
  userGetProductListService,
  userAddProductToWishListService,
  userFilterProductService,
  userGetShopSliderService,
  userGetProductPageService,
} from "@/api/product.js";
// 获取轮播图
const shopSlider = ref([]);
const getShopSlider = async () => {
  let result = await userGetShopSliderService();
  shopSlider.value = result.data;
};
getShopSlider();

import emitter from "@/event/eventBus.js";
import { userAddProductToShoppingCartService } from "@/api/shoppingCart.js";
//添加商品到购物车
const addProductToCart = async (row) => {
  let result = await userAddProductToShoppingCartService(row.id);
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

// 添加商品到心愿单
const addProductToWishList = async (row) => {
  let result = await userAddProductToWishListService(row.id);
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

//获取分类
import { userGetCategoryListService } from "@/api/category.js";
const categoryList = ref([]);
const getCategories = async () => {
  const res = await userGetCategoryListService();
  categoryList.value = res.data;
};
getCategories();

//获取品牌
import { userGetBrandListService } from "@/api/brand.js";
const brandList = ref([]);
const getBrands = async () => {
  const res = await userGetBrandListService();
  brandList.value = res.data;
};
getBrands();

// 筛选
const filter = async () => {
  products.value = [];
  isFilter.value = true;
  loading.value = false;
  showOverlay.value = false;
  ElMessage({
    showClose: true,
    type: "success",
    message: "筛选成功",
    plain: true,
  });
  await loader(1);
};
//搜索
const searchProduct = async () => {
  if (searchNames.value.length === 0) {
    ElMessage.error("请输入搜索内容");
  } else {
    products.value = [];
    loading.value = false;
    isSearch.value = true;
    await loader(1);
  }
};

// 记录总商品数
const totalProduct = ref(0);
const pageSize = ref(0);
// 获取商品
const loader = async (pageNum) => {
  let data = ref({
    searchNames: searchNames.value,
    category: categoryChooseModel.value,
    minPrice: priceValue.value[0],
    maxPrice: priceValue.value[1],
    brand: brandChooseModel.value,
    pageNum: pageNum,
    pageSize: 8,
  });
  let result = await userGetProductPageService(data.value);
  if (result.code === 200) {
    // 如果是第一页，清空现有商品；否则追加
    if (pageNum === 1) {
      products.value = result.data.records;
    } else {
      products.value = products.value.concat(result.data.records);
    }
    totalProduct.value = result.data.total;
    pageSize.value = result.data.size;

    // 如果没有商品或已经加载完所有商品，设置loading为true
    if (result.data.records.length === 0 || products.value.length >= totalProduct.value) {
      loading.value = true;
      // 如果是第一页且没有商品，显示提示信息
      if (result.data.records.length === 0 && pageNum === 1) {
        ElMessage({
          message: "没有找到符合条件的商品",
          type: "info",
          plain: true,
          showClose: true,
        });
      }
    }
  } else {
    ElMessage.error(result.message ? result.message : "加载失败");
  }
};

// 取消筛选
const cancelFilter = async () => {
  ElMessage({
    showClose: true,
    type: "success",
    message: "取消筛选成功",
    plain: true,
  });
  categoryChooseModel.value = [];
  brandChooseModel.value = [];
  priceValue.value = [0, 0];
  products.value = [];
  isFilter.value = false;
  loading.value = false;
  await loader(1);
  // router.push({ path: "/shop" });
};

// 取消搜索
const cancelSearch = async () => {
  if (searchNames.value.length === 0) {
    ElMessage.error("您还没有搜索");
  } else {
    ElMessage({
      showClose: true,
      type: "success",
      message: "取消搜索成功",
      plain: true,
    });
    searchNames.value = [];
    products.value = [];
    isSearch.value = false;
    loading.value = false;
    await loader(1);
    // router.push({ path: "/shop" });
  }
};

// 获取商品详情
const getProductDetail = (id) => {
  router.push({
    path: "/productDetail",
    query: { id: id },
  });
};

const screenWith = ref(document.body.clientWidth);
const sidebarWidth = ref("30%");
window.onresize = () => {
  return (() => {
    screenWith.value = document.body.clientWidth;
  })();
};
watch(() => {
  if (screenWith.value < 500) {
    sidebarWidth.value = "100%";
  } else if (screenWith.value < 768 && screenWith.value >= 500) {
    sidebarWidth.value = "80%";
  } else if (screenWith.value < 1024 && screenWith.value >= 768) {
    sidebarWidth.value = "60%";
  } else if (screenWith.value < 1280 && screenWith.value >= 1024) {
    sidebarWidth.value = "50%";
  } else if (screenWith.value < 1600 && screenWith.value >= 1280) {
    sidebarWidth.value = "40%";
  } else {
    sidebarWidth.value = "30%";
  }
});

const showComponent = ref(false);
const loading = ref(false);

onMounted(() => {
  setTimeout(() => {
    showComponent.value = true;
  }, 1000);
  // loader(1);

  // 创建IntersectionObserver实例实现懒加载
  const ob = new IntersectionObserver(
    async (entries) => {
      for (const entry of entries) {
        console.log("entry.isIntersecting:" + entry.isIntersecting);
        const dom_loading = document.getElementById("loading");
        var rect = dom_loading.getBoundingClientRect();
        if (entry.isIntersecting) {
          console.log("调用ntersectionObserver");
          if (products.value.length === 0 ) {
            if(isSearch.value || isFilter.value){
              return;
            }
            await loader(1);
          } else {
            if (!loading.value) {
              let pageNum = Math.ceil(products.value.length / pageSize.value) + 1;
              if (products.value.length < totalProduct.value) {
                setTimeout(async () => {
                  await loader(pageNum);
                }, 500);
              } else {
                ElMessage({
                  message: "没有更多数据了",
                  type: "success",
                  plain: true,
                  showClose: true,
                });
                loading.value = true;
              }
            }
          }
        }
      }
    },
    {
      threshold: 1,
    }
  );
  const dom_loading = document.getElementById("loading");
  ob.observe(dom_loading);
});
</script>
<template>
  <!-- <headerView /> -->
  <!-- sidebar menu -->
  <el-drawer v-model="showOverlay" direction="ltr" id="sidebar" :size="sidebarWidth">
    <div class="collapse-filter">
      <div class="collapse-filter__header">
        <img src="@/assets/images/logo-footer.jpg" alt="" />
      </div>
      <div class="collapse-filter__content sidebar">
        <div class="sidebar-shop">
          <form action="" @submit.prevent="filter()">
            <div class="accordion" id="sidebarShop">
              <!-- categories -->
              <div class="widget">
                <div class="widget__heading">
                  <a href="javascript:void(0)" class="btn widget__heading--title">
                    <h3 class="category-title">分类</h3>
                  </a>
                </div>
                <div class="collapse show">
                  <div class="widget__body">
                    <ul class="widget-list">
                      <li class="widget-list__item" v-for="c in categoryList" :key="c">
                        <div class="form-check">
                          <el-checkbox :label="c.id" v-model="categoryChooseModel">
                            {{ c.name }}
                          </el-checkbox>
                        </div>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>

              <div class="widget">
                <div class="widget__heading">
                  <a href="javascript:void(0)" class="btn widget__heading--title">
                    <h3 class="category-title">品牌</h3>
                  </a>
                </div>
                <div class="collapse show">
                  <div class="widget__body">
                    <ul class="widget-list">
                      <li class="widget-list__item" v-for="b in brandList" :key="b">
                        <div class="form-check">
                          <el-checkbox :label="b.id" v-model="brandChooseModel">
                            {{ b.name }}
                          </el-checkbox>
                        </div>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>

              <!-- price range -->
              <div class="widget">
                <div class="widget__heading" id="headingThree">
                  <a href="javascript:void(0)" class="btn widget__heading--title">
                    <h3 class="category-title">价格</h3>
                  </a>
                </div>
                <div id="collapseThree" class="collapse show">
                  <div class="widget__body">
                    <!-- product price widget -->
                    <div class="widget__body--item filter-price">
                      <div class="filter-price__value">
                        <label for="amount">价格区间(区间为0时不筛选价格):</label>
                        <input
                          type="text"
                          class="price-range"
                          name="price"
                          readonly
                          :value="'￥' + priceValue[0] + ' - ￥' + priceValue[1]"
                        />
                      </div>
                      <el-slider
                        v-model="priceValue"
                        range
                        :max="10000"
                        :show-tooltip="false"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <!-- form submit button -->
            <br />
            <div class="custom-form__btn">
              <button type="submit" class="btn submit-btn">筛选</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </el-drawer>

  <!-- end sidebar menu -->
  <!-- header slider -->
  <div class="container">
    <div class="slider-area hero-header">
      <el-carousel interval="3000" arrow="hover" height="400px">
        <el-carousel-item v-for="item in shopSlider" :key="item">
          <div class="shop-slider">
            <div class="container" v-if="showComponent">
              <div class="row shop-slider__row">
                <div class="col-lg-5 col-12">
                  <div class="shop-slider__inner">
                    <h5 class="slider-subtitle animate__animated animate__fadeInUp">
                      2024 最新设计
                    </h5>
                    <h1 class="slider-title animate__animated animate__fadeInUp">
                      <a href="javascript:void(0)"></a>故宫博物馆限定
                    </h1>
                    <p class="slider-content animate__animated animate__fadeInLeft">
                      中国传统文化与现代时尚的完美融合 <br />
                      现代时尚与故宫文化完美融合 <br />
                      顶级艺术与设计<br />
                    </p>
                    <!-- <div class="slider-btn">
                      
                      <a
                        class="btn animate__animated animate__jackInTheBox"
                        >去购物</a
                      >
                    </div> -->
                  </div>
                </div>
                <div class="col-lg-7 col-12">
                  <div class="shop-slider__img">
                    <img
                      class="animate__animated animate__fadeInRight"
                      :src="item.url"
                      alt=""
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>
  </div>
  <!-- end header slider -->
  <!-- main content -->
  <div class="main-content pt-50 pb-80">
    <div class="container">
      <!-- toolbox -->
      <div class="row">
        <div class="col-12">
          <div class="shop-toolbox">
            <div class="toolbox-filter">
              <div class="collapse-filter" id="SidefilterBtn">
                <span class="pull-bs-canvas-left" @click="showCanvas">
                  <span><i class="bi-filter"></i> 筛选</span>
                </span>
                <span class="pull-bs-canvas-left" @click="cancelFilter" v-if="isFilter">
                  <span><i class="bi-x"></i>取消筛选</span>
                </span>
              </div>
            </div>

            <div class="toolbox-sort">
              <div class="form-group" style="display: flex">
                <el-input-tag
                  v-model="searchNames"
                  placeholder="输入搜索名称按空格可以输入多个（最多3个）"
                  trigger="Space"
                  :max="3"
                >
                  <template #prefix>
                    <el-tooltip content="取消搜索" placement="top" effect="light">
                      <el-icon @click="cancelSearch"><Delete /></el-icon>
                    </el-tooltip>
                  </template>
                  <template #suffix>
                    <el-tooltip content="搜索" placement="top" effect="light">
                      <el-icon @click="searchProduct"><Search /></el-icon>
                    </el-tooltip>
                  </template>
                </el-input-tag>
                <!-- <a class="btn search-btn" @click="searchProduct">
                      <i class="bi-search"></i>
                    </a> -->
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- end toolbox -->
      <!-- product list -->
      <div class="row">
        <div class="col-lg-3 col-md-6" v-for="(product, index) in products" :key="index">
          <div class="card-item card-item--light mb-30">
            <div class="card-item__bg">
              <img :src="product.image" alt="" />
              <span class="badge card-item__bg--badge">{{ product.label }}</span>
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
                    <el-tag type="success" size="small"> {{ brand.name }}</el-tag>
                  </div>
                </div>
                <div v-for="category in categoryList" :key="category.id">
                  <div v-if="product.categoryId == category.id">
                    <label class="category">分类</label>
                    <el-tag type="info" size="small"> {{ category.name }}</el-tag>
                  </div>
                </div>
              </div>
            </div>
            <div class="card-item__overlay">
              <ul class="action-btn">
                <li>
                  <el-tooltip content="查看详情" placement="top" effect="light">
                    <a href="" class="icon-box" @click="getProductDetail(product.id)"
                      ><i class="icon-search"></i
                    ></a>
                  </el-tooltip>
                </li>
                <li class="wishlist-btn">
                  <el-tooltip content="添加到愿望单" placement="top" effect="light">
                    <span class="icon-box" @click="addProductToWishList(product)"
                      ><i class="wishlist__icon icon-heart"></i
                    ></span>
                  </el-tooltip>
                </li>
              </ul>
              <a class="btn" @click="addProductToCart(product)">添加到购物车</a>
            </div>
          </div>
        </div>
      </div>
      <!-- end product list -->

      <!-- 加载更多 -->
      <div
        style="display: flex; justify-content: center; align-items: center; height: 100px"
        id="loading"
      >
        <div class="loading" v-show="!loading">
          <div class="spinner">
            <div class="cube1"></div>
            <div class="cube2"></div>
          </div>
        </div>
        <div></div>
      </div>

      <!-- pagination -->
      <!-- <div class="row">
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
                  <a class="page-link" href="javascript:void(0)" @click="changePage(page)">
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
      </div> -->
      <!-- end pagination -->
    </div>
    <!-- end main content -->
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
.category-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.brand {
  color: dimgray;
  font-size: smaller;
  margin-right: 1rem;
}
.category {
  color: rgb(107, 107, 107);
  font-size: smaller;
  margin-right: 1rem;
}
.load-more-btn {
  background-color: #ffffff;
  border-color: #444343;
  color: #444343;
  width: 10%;
  margin: 20px;
}

.load-more-btn:hover {
  background-color: #444343;
  color: #ffffff;
}
.search-btn {
  background-color: #ffffff;
  border-color: #444343;
  color: #444343;
}
.search-btn:hover {
  background-color: #444343;
  color: #ffffff;
}
@media (max-width: 1080px) {
  .load-more-btn {
    width: 20vw;
  }
}
@media (max-width: 680px) {
  .load-more-btn {
    width: 20vw;
  }
}
.loading {
  background: #ffffff;
  height: 10%;
  left: 0;
  top: 0;
  width: 10%;
  z-index: 2000;
  display: -ms-flexbox;
  display: flex;
  -webkit-align-items: center;
  -ms-flex-align: center;
  align-items: center;
}
</style>
