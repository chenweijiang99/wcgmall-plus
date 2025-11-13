<script setup>
import { onMounted, ref } from "vue";
import { useTokenStore } from "@/stores/token.js";
const tokenStore = useTokenStore();
import { useScrollToTop } from "@/assets/base.js";

const { showLoader, backtopStyle, handleMouseEnter, handleMouseLeave } = useScrollToTop();
import { useRoute, useRouter } from "vue-router";
const router = useRouter();
import {
  userGetOLListService,
  userGetNewProductService,
  userGetNewBlogService,
  userGetIndexSliderService,
} from "@/api/index.js";

const indexSlider = ref([]);
const getIndexSlider = async () => {
  let result = await userGetIndexSliderService();
  indexSlider.value = result.data;
};

//获取官方收藏
const officialCollectionList = ref([]);
const getOLList = async () => {
  let result = await userGetOLListService();
  officialCollectionList.value = result.data;
};

//获取最新商品
const newProductList = ref([]);
const getNewProduct = async () => {
  let result = await userGetNewProductService();
  newProductList.value = result.data;
};

//获取最新博客
const newBlogList = ref([]);
const getNewBlog = async () => {
  let result = await userGetNewBlogService();
  newBlogList.value = result.data;
};

// 获取商品详情
const getProductDetail = (id) => {
  router.push({
    path: "/productDetail",
    query: { id: id },
  });
};
// 查看博客详情
const goToBlogDetail = (id) => {
  router.push({ path: "/blogDetail", query: { id: id } });
};
const carouselImage = ref([
  {
    url: new URL("@/assets/images/header/轮播图3.jpg", import.meta.url).href,
  },
  {
    url: new URL("@/assets/images/header/轮播图4.jpg", import.meta.url).href,
  },
]);

const bannerImageUrl = new URL(
  "@/assets/images/banners/banner-bg-09.jpg",
  import.meta.url
).href;

import { ElMessage } from "element-plus";
import emitter from "@/event/eventBus.js";
const showComponent = ref(false);
onMounted(() => {
  getIndexSlider();
  getOLList();
  getNewProduct();
  getNewBlog();
  setTimeout(() => {
    showComponent.value = true;
  }, 1000);
  let flag = window.location.href.indexOf("token") != -1;
  if (flag) {
    let token = window.location.href.split("token=")[1];
    tokenStore.setToken(token);
    emitter.emit("refresh");
    emitter.emit("login");
    ElMessage({
      showClose: true,
      type: "success",
      message: "登录成功",
      plain: true,
    });
  }
});
// onCreated(()=>{
//   let flag = window.location.href.indexOf("token") != -1;
//       if (flag) {
//         let token = window.location.href.split("token=")[1];
//            tokenStore.setToken(token);
//             ElMessage({
//       showClose: true,
//       type: 'success',
//       message: result.message ? result.message : "登录成功",
//       plain: true,
//             })
//       }
// })
</script>

<template>
  <!-- <headerView /> -->
  <!-- main header slider -->
  <div class="hero-header-slider">
    <el-carousel :interval="3000" arrow="hover" height="800px" class="header-slider">
      <el-carousel-item v-for="item in indexSlider" :key="item">
        <div class="item">
          <div
            class="hero-header-slider__img slider-img"
            :style="{ 'background-image': 'url(' + item.url + ')' }"
          >
            <div class="hero-header-slider__wrapper">
              <div class="slider-inner">
                <div class="container overflow-hidden">
                  <div v-if="showComponent" class="inner-content inner-content--white">
                    <h5 class="slider-subtitle animate__animated animate__fadeInUp">
                      最新的
                    </h5>
                    <h1 class="slider-title animate__animated animate__fadeInRight">
                      独一无二的文创产品
                    </h1>
                    <p class="slider-text animate__animated animate__fadeInLeft">
                      展现中国传统文化的魅力，走进中国文化
                    </p>
                    <div class="slider-btn animate__animated animate__jackInTheBox">
                      <router-link to="/shop">
                        <a class="btn" href="javascript:void(0)">去购物</a>
                      </router-link>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
  <!-- end of main header -->

  <!-- main content -->
  <div class="main-content py-100">
    <div class="container">
      <!-- items slider light -->
      <div class="mb-100 overflow-hidden">
        <h2 class="main-title">新品上市</h2>
        <el-carousel :interval="2000" type="card" arrow="hover">
          <el-carousel-item v-for="item in newProductList" :key="item">
            <div class="item">
              <div class="card-item card-item--light">
                <div class="card-item__bg">
                  <a href="javascript:void(0)"
                    ><img :src="item.image" alt="" style="width: 300px; height: 300px" />
                    <span class="badge card-item__bg--badge">New</span>
                  </a>
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
      <!-- end items slider light -->
    </div>
    <!-- end items slider light blue -->

    <!--  banner call to action  -->
    <div
      class="banner banner-full mb-100 overflow-hidden"
      :style="{ 'background-image': 'url(' + bannerImageUrl + ')' }"
    >
      <div class="container">
        <div class="banner-full__inner">
          <a href="javascript:void(0)">
            <h3>大促销</h3>
            <h2>最高立减<br /><span class="text-bg">45%</span></h2>
          </a>
          <a href="javascript:void(0)" class="btn">立即下单</a>
        </div>
      </div>
    </div>
    <!--  end banner call to action  -->
    <!-- gallery -->
    <div class="res-gallery res-gallery--bg mb-100">
      <div class="container">
        <div class="row">
          <!-- gallery tabs -->
          <div class="col-lg-3 res-gallery__tabs">
            <h3 class="gallery-title">
              <span>2025 官方</span>
              <span>精选收藏</span>
            </h3>
            <div
              class="nav nav-pills"
              id="v-pills-tab"
              role="tablist"
              aria-orientation="vertical"
            >
              <a class="nav-link"> 官方挑选 </a>
              <a class="nav-link"> 值得信赖 </a>
              <!-- <a
                v-for="(category, index) in categories"
                :key="index"
                class="nav-link"
                :class="{ active: activeCategory === index }"
                @click="showCategory(index)"
              >
                {{ category.name }}
              </a> -->
            </div>
          </div>
          <!-- gallery content -->
          <div class="col-lg-9">
            <div class="tab-content res-gallery__content" id="v-pills-tabContent">
              <div class="tab-pane show active">
                <div class="row">
                  <div
                    v-for="item in officialCollectionList"
                    :key="item"
                    class="col-md-4 col-6 gallery-column"
                  >
                    <div class="image-effect">
                      <img :src="item.productImage" alt="" />
                      <div class="image-effect__content">
                        <a href="javascript:void(0)">
                          <h3 class="link" @click="getProductDetail(item.productId)">
                            查看详情
                          </h3>
                        </a>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- end gallery -->

    <!-- latest blog post -->
    <div class="container">
      <div class="row overflow-hidden">
        <div class="col-12">
          <h2 class="main-title wow fadeIn">最新文章</h2>
        </div>
      </div>
      <div class="row wow fadeIn">
        <div class="col-lg-4 col-md-6" v-for="item in newBlogList" :key="item">
          <div class="blog-post blog-post--grid">
            <div class="blog-post__img">
              <div class="blog-post__img--overlay"></div>
              <img :src="item.image" alt="" />
              <a
                href="javascript:void(0)"
                class="btn blog-post__btn"
                @click="goToBlogDetail(item.id)"
              >
                阅读更多
              </a>
            </div>
            <div class="blog-post__inner">
              <div class="blog-post__inner--title">
                <a href="javascript:void(0)">
                  <h4>{{ item.title }}</h4>
                </a>
              </div>
              <div class="blog-post__inner--details">
                <!-- <span class="author">By Adam Smith,</span> -->
                <span class="date">{{ item.createTime }}</span>
              </div>
              <!-- <div class="blog-post__inner--des">
                <p v-html="item.content"></p>
                <p class="blog-post-content">
                  {{ item.content }}
                </p>
              </div> -->
            </div>
          </div>
        </div>
        <!-- <div class="col-lg-4 col-md-6">
          <div class="blog-post blog-post--grid">
            <div class="blog-post__img">
              <div class="blog-post__img--overlay"></div>
              <img src="@/assets/images/blog2.jpg" alt="" />
              <a href="javascript:void(0)" class="btn blog-post__btn">
                Read more
              </a>
            </div>
            <div class="blog-post__inner">
              <div class="blog-post__inner--title">
                <a href="javascript:void(0)">
                  <h4>How to design a happy home</h4>
                </a>
              </div>
              <div class="blog-post__inner--details">
                <span class="author">By Adam Smith,</span>
                <span class="date">December 18, 2019</span>
              </div>
              <div class="blog-post__inner--des">
                <p>
                  Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                  Error maiores nemo obcaecati provident qui, quos.
                </p>
              </div>
            </div>
          </div>
        </div>
        <div class="col-lg-4 col-md-6 mx-auto">
          <div class="blog-post blog-post--grid">
            <div class="blog-post__img">
              <div class="blog-post__img--overlay"></div>
              <img src="@/assets/images/blog3.jpg" alt="" />
              <a href="javascript:void(0)" class="btn blog-post__btn">
                Read more
              </a>
            </div>
            <div class="blog-post__inner">
              <div class="blog-post__inner--title">
                <a href="javascript:void(0)">
                  <h4>Top tips for sustainable living</h4>
                </a>
              </div>
              <div class="blog-post__inner--details">
                <span class="author">By Adam Smith,</span>
                <span class="date">December 18, 2019</span>
              </div>
              <div class="blog-post__inner--des">
                <p>
                  Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                  Error maiores nemo obcaecati provident qui, quos.
                </p>
              </div>
            </div>
          </div>
        </div>-->
      </div>
    </div>
    <!-- end latest blog post -->
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

  <!-- <footerView /> -->
</template>

<style scoped>
@import "@/assets/main.css";
a {
  text-decoration: none;
}
.banner-full__inner .btn:hover {
  background: #ffffff;
  border-color: #000000;
  color: #000000;
}
.animated {
  animation-duration: 1s;
  animation-fill-mode: both;
}
.owl-animated-in {
  z-index: 0;
}
.owl-animated-out {
  z-index: 1;
}

@media (max-width: 768px) {
  /* 在小屏幕下的样式 */
  .header-slider {
    height: 300px;
    width: 100%;
  }
}

@media (min-width: 768px) and (max-width: 1024px) {
  /* 在中等屏幕下的样式 */
  .header-slider {
    height: 600px;
  }
}

@media (min-width: 1024px) {
  /* 在大屏幕下的样式 */
  .header-slider {
    height: 800px;
  }
}
.blog-post-content {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
  -webkit-line-clamp: 5; /* 控制显示的行数 */
}
</style>
