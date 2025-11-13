<script setup>
import { computed, watch, ref, onMounted, onUnmounted } from "vue";
import { useRoute } from "vue-router";
import {BASE_URL} from "@/api/wsConfig.js"
const route = useRoute();
const selectedItems = ref([]);

import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
import { useScrollToTop } from "@/assets/base.js";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Open,
  Check,
  Delete,
  Edit,
  Message,
  Search,
  Star,
  Warning,
  Refresh
} from "@element-plus/icons-vue";
const { showLoader, backtopStyle, handleMouseEnter, handleMouseLeave } =
  useScrollToTop();
const userAddressList = ref([]);
import {
  userGetAddressListService,
  userUpdateAddressService,
  userDeleteAddressService,
  userAddAddressService,
} from "@/api/address.js";
// 获取用户地址列表
const getUserAddressList = async () => {
  let result = await userGetAddressListService();
  if (result.code == 1) {
    userAddressList.value = result.data;
    const defaultAddress = userAddressList.value.find(
      (address) => address.default === 1
    );
    if (defaultAddress) {
      selectedAddress.value = defaultAddress;
    }
  }
};
getUserAddressList();

//修改地址
const updateAddress = async (value) => {
  let result = await userUpdateAddressService(value);
  if (result.code === 1) {
    ElMessage({
      showClose: true,
      type: "success",
      message: "修改成功",
      plain: true,
    });
    getUserAddressList();
  }
};

// 删除地址
const deleteAddressService = async (index) => {
  let result = await userDeleteAddressService(index);
  if (result.code === 1) {
    ElMessage({
      showClose: true,
      type: "success",
      message: "删除成功",
      plain: true,
    });
    getUserAddressList();
  }
};
// 新增地址
const addAddressService = async (value) => {
  let result = await userAddAddressService(value);
  if (result.code === 1) {
    ElMessage({
      showClose: true,
      type: "success",
      message: "新增成功",
      plain: true,
    });
    getUserAddressList();
  }
};

const addressEdit = ref([]); // 用于动态修改行内数据

const loadAddressData = () => {
  // 发起请求获取数据
  // request...
  for (let i in addressData.value) {
    addressEdit.value[i] = false; // 初始化布尔数组，false：查看状态（非编辑状态）
  }
};
const add = ref(false);
// 新增地址
const addAddress = () => {
  userAddressList.value.push({}); // 新增地址
  addressEdit.value[userAddressList.value.length - 1] = true;
  add.value = true;
};

// 编辑地址
const editAddress = (index) => {
  addressEdit.value[index] = !addressEdit.value[index];
};
// 保存地址
const saveAddress = (index, value) => {
  
  if (value.default === 1) {
    const defaultAddresses = userAddressList.value.filter(
      (address) => address.default === 1
    );
    if (defaultAddresses.length > 1) {
      ElMessage.error("只能有一个默认地址");
      return;
    }
  }

  if (add.value == true) {
    addAddressService(value);
    add.value = false;
  } else {
    updateAddress(value);
  }

  addressEdit.value[index] = !addressEdit.value[index];
};

// 删除地址
const deleteAddress = (index, value) => {
  deleteAddressService(value.id);
  userAddressList.value.splice(index, 1);
};

const selectedAddress = ref(null); // 用于存储选中的地址

// 选择地址
const selectAddress = (address) => {
  selectedAddress.value = address;
};
// 操作列不触发选择事件
const handleRowClick = (row, column, event) => {
  if (column.label !== "操作") {
    selectAddress(row);
  }
};
const carts = ref([]);
import { userGetShoppingCartService } from "@/api/shoppingCart.js";

// 获取购物车数据
// const getShoppingCarts = async () => {
//   let result = await userGetShoppingCartService();
//   if (result.code == 1) {
//     carts.value = result.data;
//   }
// };
const getShoppingCarts = async () => {
  let result = await userGetShoppingCartService();
  if (result.code == 1) {
    carts.value = result.data;
  }
};
// getShoppingCarts();

// 计算总金额
const shoppingCartTotalPrice = computed(() => {
  let total = 0;
  for (const item of carts.value) {
    total += item.productPrice * item.number;
  }
  return total;
});

const payMethod = ref();
import { userAddOrderService, userCancelOrderService } from "@/api/order.js";
import { userPayService } from "@/api/alipay.js";
import { useRouter } from "vue-router";
import emitter from "@/event/eventBus.js";
const router = useRouter();
const paybox = ref(false);
const payBoxContent = ref();
const payButtonText = ref("去支付");
const socket = ref(null);
const buttonDisabled = ref(true);

const countdown = ref(300); // 5分钟倒计时（300秒）
const countdownInterval = ref(null);

const startCountdown = () => {
  countdown.value = 300; // 重置倒计时为300秒
  countdownInterval.value = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--;
    } else {
      clearInterval(countdownInterval.value);
      autoCancelPay();
    }
  }, 1000);
};
const orderNumber = ref();
// 提交订单
const addOrder = async () => {
  if (payMethod.value == null) {
    ElMessage.error("请选择支付方式");
    return;
  }
  const orderDTO = ref({
    consignee: selectedAddress.value.consignee,
    consigneeAddress: selectedAddress.value.consigneeAddress,
    consigneePhone: selectedAddress.value.consigneePhone,
    amount: shoppingCartTotalPrice.value,
    payMethod: payMethod.value,
    shoppingCartList: carts.value,
  });
  let result = await userAddOrderService(orderDTO.value);
  const sid = Date.now() + Math.floor(Math.random() * 100000);
  // socket.value = new WebSocket("ws://localhost:1203/ws/" + sid);
  socket.value = new WebSocket("ws://"+BASE_URL+"/ws/" + sid);
  if (result.code == 1) {
    ElMessage({
      showClose: true,
      type: "success",
      message: result.message ? result.message : "订单生成成功",
      plain: true,
    });
    orderNumber.value = result.data.orderNumber;
    startCountdown(); // 开始倒计时
    emitter.emit("refresh");
    
    let payResult = await userPayService(result.data.orderNumber);
    if (payResult.code == 1) {
      paybox.value = true;
      payBoxContent.value = payResult.data;
      payButtonText.value = "已支付";
      buttonDisabled.value = false;
      socket.value.onmessage = function message(data) {
        if (data.data == result.data.orderNumber) {
          router.push({
            path: "/purchaseNotif",
            query: { orderNumber: data.data },
          });
        }
      };
    }
  } else {
    ElMessage.error(result.message ? result.message : "订单生成失败");
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
      let result = await userCancelOrderService(orderNumber.value);
      if (result.code == 1) {
        ElMessage({
          showClose: true,
          type: "success",
          message: result.message ? result.message : "取消支付成功",
          plain: true,
        });
        router.push({ path: "/account",query: {activeTap: 2}});
      } else {
        ElMessage.error(result.message ? result.message : "取消支付失败");
      }
    })
    .catch(() => {
      startCountdown();
    });
};
// 自动取消支付
const autoCancelPay = async () => {
  let result = await userCancelOrderService(orderNumber.value);
  if (result.code == 1) {
    ElMessage({
      showClose: true,
      type: "success",
      message: result.message ? result.message : "取消支付成功",
      plain: true,
    });
    router.push({ path: "/account",query: {activeTap: 2}});
  } else {
    ElMessage.error(result.message ? result.message : "取消支付失败");
  }
};

const paymentIframe = ref(null);

const refreshIframe = () => {
  const iframe = paymentIframe.value;
  if (iframe) {
    iframe.srcdoc = payBoxContent.value; // 重新设置 srcdoc 属性以刷新 iframe
  }
};



onMounted(() => {
  if (route.query.items) {
    selectedItems.value = JSON.parse(route.query.items);
    carts.value = selectedItems.value; // 将选中的商品赋值给 carts
  } else {
    getShoppingCarts();
  }
});

// 页面卸载时关闭 WebSocket 连接和清除倒计时
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
            <li class="breadcrumb-item active" aria-current="page">支付</li>
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
        <div class="col-12">
          <h1 class="main-title">账单详情</h1>
        </div>
        <div class="col-md-8">
          <el-card class="box-card;" shadow="always">
            <template #header>
              <div class="card-header" style="text-align: left">
                <span>管理地址</span>
                <el-button class="button" type="text" @click="addAddress"
                  >新增</el-button
                >
              </div>
              <el-table :data="userAddressList" @row-click="handleRowClick">
                <el-table-column prop="consignee" label="收货人">
                  <template #default="scope">
                    <el-input
                      size="medium"
                      v-model="scope.row.consignee"
                      v-if="addressEdit[scope.$index]"
                    ></el-input>
                    <span v-else>{{ scope.row.consignee }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="consigneePhone" label="电话" width="150">
                  <template #default="scope">
                    <el-input
                      size="medium"
                      v-model="scope.row.consigneePhone"
                      v-if="addressEdit[scope.$index]"
                    ></el-input>
                    <span v-else>{{ scope.row.consigneePhone }}</span>
                  </template>
                </el-table-column>

                <el-table-column
                  prop="consigneeAddress"
                  label="收货地址"
                  width="200"
                >
                  <template #default="scope">
                    <el-input
                      size="medium"
                      v-model="scope.row.consigneeAddress"
                      v-if="addressEdit[scope.$index]"
                    ></el-input>
                    <span v-else>{{ scope.row.consigneeAddress }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="default" label="默认">
                  <template #default="scope">
                    <div v-if="addressEdit[scope.$index]">
                      <el-checkbox
                        v-model="scope.row.default"
                        :true-label="1"
                        :false-label="0"
                        @change="changetest"
                      >
                        设为默认
                      </el-checkbox>
                    </div>
                    <div v-else>
                      <el-tag
                        v-if="scope.row.default == 1"
                        type="success"
                        size="mini"
                        >默认</el-tag
                      >
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="操作">
                  <template #default="scope">
                    <el-button
                      size="small"
                      type="primary"
                      :icon="Edit"
                      circle
                      v-if="!addressEdit[scope.$index]"
                      @click="editAddress(scope.$index)"
                    />
                    <el-button
                      size="small"
                      type="success"
                      :icon="Check"
                      circle
                      v-else
                      @click="saveAddress(scope.$index, scope.row)"
                    >
                    </el-button>
                    <el-popconfirm
                      confirmButtonText="确认"
                      cancelButtonText="取消"
                      :icon="Warning"
                      title="确定删除该地址？"
                      @confirm="deleteAddress(scope.$index, scope.row)"
                    >
                      <template #reference>
                        <el-button
                          size="small"
                          :icon="Delete"
                          circle
                          type="danger"
                        >
                        </el-button>
                      </template>
                    </el-popconfirm>
                  </template>
                </el-table-column>
              </el-table>
            </template>
          </el-card>
          <br />

          <p v-if="selectedAddress">
            选择的地址： {{ selectedAddress.consignee }},
            {{ selectedAddress.consigneePhone }},
            {{ selectedAddress.consigneeAddress }}
          </p>
          <br />
          <h4 class="checkout-form__title">支付方式</h4>
          <div class="checkout-form__payment">
            <div class="custom-control custom-radio">
              <input
                id="paypal"
                name="paymentMethod"
                type="radio"
                class="custom-control-input"
                required=""
                v-model="payMethod"
                value="1"
              />
              <label class="custom-control-label" for="paypal">支付宝</label>
            </div>
          </div>
          <div v-if="paybox">
            <div v-if="countdown > 0" class="countdown">
              请在倒计时之前支付，剩余时间: {{ Math.floor(countdown / 60) }}:{{
                (countdown % 60).toString().padStart(2, "0")
              }}
            </div>
            <br />
            <br />
            <iframe
              ref="paymentIframe"
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
              <el-button
                size="small"
                :icon="Refresh"
                type="primary"
                @click="refreshIframe"
              >

                刷新二维码
              </el-button>
            </div>
          </div>
          <button
            class="btn checkout-form__btn"
            type="submit"
            @click="addOrder"
            v-show="buttonDisabled"
          >
            {{ payButtonText }}
          </button>
        </div>
        <div class="col-md-4">
          <div class="order-review">
            <h5 class="order-review__title">订单</h5>
            <ul class="order-review__list">
              <li
                class="order-review__list--item"
                v-for="item in carts"
                :key="item.id"
              >
                <span>{{ item.productName }}</span>
                <span>￥{{ item.productPrice }} x {{ item.number }}</span>
              </li>

              <li class="order-review__list--item">
                <span>总价</span>
                <span>￥{{ shoppingCartTotalPrice }}</span>
              </li>
            </ul>
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
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>@/api/wsConfig.js