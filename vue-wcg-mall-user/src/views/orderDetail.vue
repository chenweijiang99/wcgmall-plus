<script setup>
import {
  computed,
  watch,
  ref,
  onMounted,
  onBeforeUnmount,
  onUnmounted,
} from "vue";
import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
import { useScrollToTop } from "@/assets/base.js";
import { ElMessage, ElMessageBox } from "element-plus";

import { useRouter } from "vue-router";

const router = useRouter();
import emitter from "@/event/eventBus.js";
const { showLoader, backtopStyle, handleMouseEnter, handleMouseLeave } =
  useScrollToTop();
import { useRoute } from "vue-router";
const route = useRoute();
import { userGetORderDetailService, userConfirmsService } from "@/api/order.js";
import { userAddOrderService, userCancelOrderService,userRefundService } from "@/api/order.js";
const orderDetail = ref({});
const orderNumber = route.query.orderNumber;
const getORderDetail = async () => {
  if (orderNumber !== undefined) {
    let result = await userGetORderDetailService(orderNumber);
    orderDetail.value = result.data;
    startCountdown(); // 开始倒计时
  }
};
getORderDetail();
// 确认收货
const confirmReceipt = async () => {
  ElMessageBox.confirm("你确认已收到商品吗？", "温馨提示", {
    confirmButtonText: "确认",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    let result = await userConfirmsService(orderNumber);
    if (result.code === 1) {
      ElMessage({
        showClose: true,
        type: "success",
        message: result.message ? result.message : "确认收货成功",
        plain: true,
      });
      getORderDetail();
    } else {
      ElMessage.error(result.message ? result.message : "确认收货失败");
    }
  });
};

const paybox = ref(false);
const payBoxContent = ref();
const payButtonText = ref("去支付");
const socket = ref(null);
const buttonDisabled = ref(true);
const countdown = ref();
const countdownInterval = ref(null);

const startCountdown = () => {
  const orderTime = new Date(orderDetail.value.orderTime);
  const now = new Date();
  //获取整数
  let timeDiff = 300 - Math.floor((now - orderTime) / 1000);
  console.log(timeDiff);
  if (timeDiff > 0) {
    countdown.value = timeDiff;
    countdownInterval.value = setInterval(() => {
      if (countdown.value > 0) {
        countdown.value--;
      } else {
        clearInterval(countdownInterval.value);
        autoCancelPay();
      }
    }, 1000);
  }
};

import { userPayService } from "@/api/alipay.js";
// 提交支付
const pay = async () => {
  const sid = Date.now() + Math.floor(Math.random() * 100000);
  socket.value = new WebSocket("ws://localhost:1203/ws/" + sid);
  emitter.emit("refresh");
  let payResult = await userPayService(orderNumber);
  if (payResult.code == 1) {
    paybox.value = true;
    payBoxContent.value = payResult.data;
    payButtonText.value = "已支付";
    buttonDisabled.value = false;
    socket.value.onmessage = function message(data) {
      if (data.data == orderNumber) {
        router.push({
          path: "/purchaseNotif",
          query: { orderNumber: data.data },
        });
      }
    };
  }
};
// 取消支付
const cancelPay = () => {
  ElMessageBox.confirm("确定取消支付吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      let result = await userCancelOrderService(orderNumber);
      if (result.code == 1) {
        ElMessage({
          showClose: true,
          type: "success",
          message: result.message ? result.message : "取消支付成功",
          plain: true,
        });
        router.push({ path: "/account" });
      } else {
        ElMessage.error(result.message ? result.message : "取消支付失败");
      }
    })
    .catch(() => {
      startCountdown();
    });
};
//自动取消支付
const autoCancelPay = async () => {
  let result = await userCancelOrderService(orderNumber);
  if (result.code == 1) {
    ElMessage({
      showClose: true,
      type: "success",
      message: result.message ? result.message : "取消支付成功",
      plain: true,
    });
    router.push({ path: "/account" });
  } else {
    ElMessage.error(result.message ? result.message : "取消支付失败");
  }
};


// 获取商品详情
const getProductDetail = (id) => {
  router.push({
    path: "/productDetail",
    query: { id: id },
  });
};

// 退款
const refund =async (orderNo) => {
  ElMessageBox.confirm('退款后无法恢复，您确认退款吗？', '退款确认', 
  { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(async () => {
    let result = await userRefundService(orderNo);
    if(result.code === 1){
      ElMessage({
        showClose: true,
        type:'success',
        message: result.message ? result.message : '退款成功',
        plain: true,
        });
        window.location.reload();
    }else{
      ElMessage({
        showClose: true,
        type:'error',
        message: '退款失败,请重试',
        plain: true,
        });
    }
  }).catch(err => {})
};

// 评价
const comment = (id) => {
  ElMessage.info("待实现");
}

onUnmounted(() => {
  if (socket.value) {
    socket.value.close();
  }
  if (countdownInterval.value) {
    clearInterval(countdownInterval.value);
  }
});
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
              <router-link :to="{ path: '/account', query: { activeTap: 2 } }">
                <a href="">订单</a>
              </router-link>
            </li>
            <li class="breadcrumb-item active" aria-current="page">订单详情</li>
          </ol>
        </nav>
      </div>
    </div>
  </div>
  <!-- end breadcrumb -->

  <div class="main-content pb-100">
    <div class="container">
      <div class="row">
        <div class="custom-form--box">
          <h3 class="custom-form__title">您的订单详情</h3>
          订单号：
          <p>{{ orderDetail.orderNumber }}</p>
          <el-descriptions
            border
            v-if="orderDetail !== null"
            label-align="center"
            align="center"
          >
            <el-descriptions-item label="收货人">{{
              orderDetail.consignee
            }}</el-descriptions-item>
            <el-descriptions-item label="收货人电话">{{
              orderDetail.consigneePhone
            }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag size="small">{{
                // 订单状态  1待付款 2待发货 3已发货 4已完成  5已取消  6已退款
                orderDetail.status === 1
                  ? "待付款"
                  : orderDetail.status === 2
                  ? "待发货"
                  : orderDetail.status === 3
                  ? "已发货"
                  : orderDetail.status === 4
                  ? "已完成"
                  : orderDetail.status === 5
                  ? "已取消"
                  : orderDetail.status === 6
                  ? "已退款"
                  : "未知状态"
              }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="订单金额">{{
              orderDetail.amount
            }}</el-descriptions-item>
            <el-descriptions-item label="收货地址">{{
              orderDetail.consigneeAddress
            }}</el-descriptions-item>
            <el-descriptions-item label="支付方式">
              <el-tag size="small">{{
                orderDetail.payMethod === 1
                  ? "支付宝支付"
                  : orderDetail.payMethod === 2
                  ? "微信支付"
                  : "未知状态"
              }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="支付状态">
              <el-tag size="small">{{
                orderDetail.payStatus === 0
                  ? "未支付"
                  : orderDetail.payStatus === 1
                  ? "已支付"
                  : orderDetail.payStatus === 2
                  ? "已退款"
                  : "未知状态"
              }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="付款时间">{{
              orderDetail.checkoutTime
            }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{
              orderDetail.orderTime
            }}</el-descriptions-item>
            <el-descriptions-item label="操作" v-if="orderDetail.status === 1">
              <el-button type="success" size="mini" @click="pay" v-if="!paybox"
                >去支付</el-button
              >
              <div v-if="countdown > 0" class="countdown">
                请在倒计时之前支付，剩余时间:
                {{ Math.floor(countdown / 60) }}:{{
                  (countdown % 60).toString().padStart(2, "0")
                }}
              </div>
              <div v-if="paybox">
                <br />
                <br />
                <iframe
                  :srcdoc="payBoxContent"
                  frameborder="no"
                  border="0"
                  marginwidth="0"
                  marginheight="0"
                  scrolling="no"
                  width="300"
                  height="300"
                  style="overflow: hidden"
                >
                </iframe>
                <div>
                  <el-button
                    size="small"
                    :icon="Delete"
                    type="danger"
                    @click="cancelPay"
                  >
                    取消支付
                  </el-button>
                </div>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="操作" v-if="orderDetail.status === 2">
              <el-button type="danger" size="mini" @click="refund(orderDetail.orderNumber)"
                >退款</el-button
              >
            </el-descriptions-item>
            <el-descriptions-item label="操作" v-if="orderDetail.status === 3">
              <el-button type="success" size="mini" @click="confirmReceipt"
                >确认收货</el-button
              >
            </el-descriptions-item>
          </el-descriptions>

          <p>商品信息：</p>
          <el-row>
            <el-col
              v-for="item in orderDetail.productLists"
              :key="item"
              :span="4"
              style="margin-left: 1rem"
            >
              <el-card :body-style="{ padding: '0px' }">
                <a href="javascript:void(0)">
                  <img :src="item.productImage" class="image" />
                </a>
                <div style="padding: 14px">
                  <span>{{ item.productName }}</span>
                  <div class="bottom">
                    <p class="content">￥{{ item.productPrice }}</p>
                    <p class="content">X</p>
                    <p class="content">{{ item.productNumber }}</p>
                  </div>
                  
                </div>
                <div style="display: flex; justify-content: space-around ;">
                  <el-button
                    type="text"
                    class="button"
                    @click="getProductDetail(item.id)"
                    >查看详情</el-button
                  >
                  <el-button
                    type="text"
                    class="button"
                    @click="comment(item.id)"
                    >评价</el-button
                  >
                </div>
                
              </el-card>
            </el-col>
          </el-row>
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
.content {
  font-size: 12px;
  color: #999;
  font-weight: bold;
}

.bottom {
  margin-top: 1rem;
  /* line-height: 12px; */
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.button {
  padding: 0;
  min-height: auto;
}

.image {
  width: 100%;
  display: block;
}
</style>