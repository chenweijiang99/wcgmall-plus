<script setup>
import { computed, watch, ref, onMounted, onBeforeUnmount } from "vue";
import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
import { useScrollToTop } from "@/assets/base.js";
import { ElMessage, ElMessageBox } from "element-plus";
import emitter from "@/event/eventBus.js";
const { showLoader,backtopStyle,handleMouseEnter,handleMouseLeave} = useScrollToTop();

import { useRoute,useRouter } from "vue-router";
const message = ref("");
const route = useRoute();
const router = useRouter();
const orderNumber = route.query.orderNumber;

import {userGetOrderStatusService} from "@/api/order.js";
const getOrderStatus = async () =>{
  let result = await userGetOrderStatusService(orderNumber);
   message.value = result.data;
}
getOrderStatus();

const orderDetail = ()=>{
  router.push({path:'/orderDetail',query:{orderNumber:orderNumber}})
}
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
            <li class="breadcrumb-item active" aria-current="page">支付成功</li>
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
        <div class="col-lg-6 col-md-8 col-12 mx-auto">
          <div class="item-box">
            <h1 class="item-box__title" >{{ message }}</h1>
            <div class="item-box__content" >
              <span>您的订单号</span>
              <span>{{ orderNumber }}</span>
              <a href="javascript:void(0)" @click="orderDetail">查看详情</a>
            </div>
          </div>
        </div>
      </div>

      <!--            <div class="item-column item-column&#45;&#45;bg">-->
      <!--            <div class="row">-->
      <!--                <div class="col-8 item-column&#45;&#45;center">-->
      <!--                    <h1 class="main-title">Thank you for your purchase</h1>-->
      <!--                    <div class="item-column__img">-->
      <!--                        <img src="images/blog2.jpg" alt="">-->
      <!--                    </div>-->
      <!--                    <div class="item-column__content">-->
      <!--                        <span>Your order number</span>-->
      <!--                        <span>12345678</span>-->
      <!--                        <a href="javascript:void(0)">View details</a>-->
      <!--                    </div>-->
      <!--                </div>-->
      <!--            </div>-->
      <!--        </div>-->
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
</style>