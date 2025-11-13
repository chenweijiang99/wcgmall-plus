<script setup>
import { computed, watch, ref, onMounted } from "vue";
import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
import { useScrollToTop } from "@/assets/base.js";
import { ElMessageBox } from "element-plus";
const { showLoader,backtopStyle,handleMouseEnter,handleMouseLeave} = useScrollToTop();

const teamMemberList = ref([
  {
    id: 1,
    teamMembers: [
      {
        id: 1,
        name: "陈韦江",
        position: "产品经理",
        img: new URL("@/assets/images/team/team-05.jpg", import.meta.url).href,
      },
      {
        id: 2,
        name: "陈韦江",
        position: "前端工程师",
        img: new URL("@/assets/images/team/team-05.jpg", import.meta.url).href,
      },
      {
        id: 3,
        name: "陈韦江",
        position: "后端工程师",
        img: new URL("@/assets/images/team/team-05.jpg", import.meta.url).href,
      },
      {
        id: 4,
        name: "陈韦江",
        position: "运维工程师",
        img: new URL("@/assets/images/team/team-05.jpg", import.meta.url).href,
      },
    ],
  },
  {
    id: 1,
    teamMembers: [
      {
        id: 5,
        name: "陈韦江",
        position: "产品设计师",
        img: new URL("@/assets/images/team/team-05.jpg", import.meta.url).href,
      },
      {
        id: 6,
        name: "陈韦江",
        position: "设计总监",
        img: new URL("@/assets/images/team/team-05.jpg", import.meta.url).href,
      },
      {
        id: 7,
        name: "陈韦江",
        position: "执行总监",
        img: new URL("@/assets/images/team/team-05.jpg", import.meta.url).href,
      },
      {
        id: 8,
        name: "陈韦江",
        position: "总经理",
        img: new URL("@/assets/images/team/team-05.jpg", import.meta.url).href,
      },
    ],
  },
]);
</script>

<template>
  <!-- <headerView /> -->
  <!-- breadcrumb -->
  <div class="container header-mt">
    <div class="row">
      <div class="col-12">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb custom-breadcrumb">
            <li class="breadcrumb-item"><router-link to="/index"> 主页 </router-link></li>
            <li class="breadcrumb-item active" aria-current="page">关于</li>
          </ol>
        </nav>
      </div>
    </div>
  </div>
  <!-- end breadcrumb -->
  <!-- main content -->
  <div class="main-content pb-80">
    <div class="container">
      <!-- about wrapper -->
      <div class="about-area mb-100">
        <div class="row">
          <div class="col-lg-6">
            <img
              src="@/assets/images/about.png"
              alt=""
              class="w-100"
            />
          </div>
          <div class="col-lg-6">
            <div class="about-area__content">
              <h1>关于我们</h1>
              <p>
                “文创购”(英文:WCG,亦称文创购物商城、文创商城),是一个综合性文创购物购物网站。“文创购”是长江师范学院计科2301陈韦江毕业设计。提供100%品质保证的商品,7天无理由退货的售后服务,以及购物积分返现等优质服务。
              </p>
              <p>
                “文创购”(英文:WCG,亦称文创购物商城、文创商城),是一个综合性文创购物购物网站。“文创购”是长江师范学院计科2301陈韦江毕业设计。提供100%品质保证的商品,7天无理由退货的售后服务,以及购物积分返现等优质服务。
              </p>
              <p>
                “文创购”(英文:WCG,亦称文创购物商城、文创商城),是一个综合性文创购物购物网站。“文创购”是长江师范学院计科2301陈韦江毕业设计。提供100%品质保证的商品,7天无理由退货的售后服务,以及购物积分返现等优质服务。
              </p>
            </div>
          </div>
        </div>
      </div>
      <!-- end about wrapper -->
      <!-- light text boxes -->
      <div class="mb-100">
        <div class="row">
          <div class="col-lg-4 col-12">
            <div class="text-box">
              <div class="text-box__num">01</div>
              <div class="text-box__inner">
                <h5 class="text-box__inner--title">优质服务</h5>

                <p>
                  用真诚的微笑，为您送去最真诚的服务；用优质的产品，给您一个安全、温馨、舒适的港湾。
                </p>
              </div>
            </div>
          </div>
          <div class="col-lg-4 col-12">
            <div class="text-box">
              <div class="text-box__num">02</div>
              <div class="text-box__inner">
                <h5 class="text-box__inner--title">
                 超低价格
                </h5>
                <p>
                  无需讨价还价，无需猜测折扣。每一件商品都有明确的价格标签，明码实价，童叟无欺。
                </p>
              </div>
            </div>
          </div>
          <div class="col-lg-4 col-12">
            <div class="text-box">
              <div class="text-box__num">03</div>
              <div class="text-box__inner">
                <h5 class="text-box__inner--title">精挑细选</h5>
                <p>精品、礼品、创意小品，你选、我选、文创购首选。</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- end light text boxes -->
      <!-- about video -->
      
      <!-- <div class="row mb-100">
        <div class="col-12">
          <div class="video-wrapper">
            <video poster="@/assets/images/video-thumb.jpg">
              <source src="" type="video/mp4" />
            </video>
            <button class="play-btn">
              <i class="bi-play-fill"></i>
            </button>
          </div>
        </div>
      </div> -->
     
      <!-- end about video -->
      <!-- meet our team -->
      <div class="mb-100">
        <div class="row">
          <div class="col-12">
            <h2 class="main-title">我们的团队</h2>

            <el-carousel
              :interval="5000"
              arrow="always"
              indicator-position="none"
              :autoplay="true"
            >
              <el-carousel-item
                class="el-car-item"
                v-for="(item, index) in teamMemberList"
                :key="index"
              >
                <div
                  class="ld-tm pos-rel team-card"
                  v-for="item1 in item.teamMembers"
                  :key="item1"
                >
                  <div class="team-card__img">
                    <img :src="item1.img" alt="" />
                  </div>
                  <div class="team-card__inner team-card__overlay">
                    <h3 class="ld-tm-name team-name">{{ item1.name }}</h3>
                    <h6 class="ld-tm-pos text-uppercase team-pos">
                      {{ item1.position }}
                    </h6>
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>
        </div>
      </div>
      <!-- end meet our team -->
      <!-- client slider -->
      <div class="client-slider">
        <div class="item">
          <img src="@/assets/images/brand01.png" alt="" />
        </div>
        <div class="item">
          <img src="@/assets/images/brand02.png" alt="" />
        </div>

        <div class="item">
          <img src="@/assets/images/brand04.png" alt="" />
        </div>
        <div class="item">
          <img src="@/assets/images/brand03.png" alt="" />
        </div>
        <div class="item">
          <img src="@/assets/images/brand05.png" alt="" />
        </div>
      </div>
      <!-- end client slider -->
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

.el-car-item {
  width: 100%;
  height: 100%;
  display: flex;
}

.el-car-item {
  width: 100%;
  display: flex;
}
.client-slider {
  display: flex;
}
.item {
  padding-right: 1.5rem;
  padding-left: 1.5rem;
  opacity: 0.4;
  cursor: pointer;
  -webkit-transition: opacity 0.3s;
  -moz-transition: opacity 0.3s;
  -ms-transition: opacity 0.3s;
  -o-transition: opacity 0.3s;
  transition: opacity 0.3s;
}
.item:hover {
  opacity: 1;
}
/* .video-wrapper {
  position: relative;
}
.play-btn {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
} */
</style>