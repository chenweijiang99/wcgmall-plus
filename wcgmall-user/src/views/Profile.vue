<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from "vue";
import { useRouter } from "vue-router";
import {
  Package,
  Heart,
  LogOut,
  Edit2,
  Save,
  X,
  MapPin,
  Plus,
  Trash2,
  Camera,
  ChevronDown,
  ChevronUp,
  CreditCard,
  Clock,
  Loader2,
  CheckCircle,
  Truck,
  ShoppingCart,
  Lock,
  RefreshCw,
} from "lucide-vue-next";
import { ElMessage, ElMessageBox, ElLoading } from "element-plus";
import { Cart, Favorites, Order, Address } from "@/types";
import { useUserStore } from "@/stores/modules/user";
import {
  getFavoritesApi,
  deleteFavoritesApi,
  addFavoritesToCartApi,
} from "@/api/favorites";
import { uploadApi } from "@/api/file";
import { updateUserApi, getEmailCodeApi, updatePasswordApi } from "@/api/auth";
import {
  getAddtessListApi,
  addAddressApi,
  updateAddressApi,
  deleteAddressApi,
} from "@/api/address";
import {
  getOrderListApi,
  getOrderDetailApi,
  cancelOrderApi,
  refundOrderApi,
  confirmReceiptApi,
  deleteOrderApi,
  alipayPayApi,
  getOrderStatusApi,
  getOrderStatusCountApi,
} from "@/api/order";
import { getOrderLogistics } from "@/api/logistics";
import { emitter } from "@/event/emitter";

const userStore = useUserStore();
const router = useRouter();
const isEditing = ref(false);
const activeTab = ref<"orders" | "favorites" | "addresses">("orders");
const expandedOrder = ref<number | null>(null);
const shoppingCart = ref<Cart[]>([]);
const favorites = ref<Favorites[]>([]);
const orders = ref<Order[]>([]);
const totalOrders = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const orderDetailsMap = ref<Record<string, any[]>>({});
const addresses = ref<Address[]>([]);
const showAddressForm = ref(false);
const addressForm = ref<any>({});
const addressFormRef = ref();

// === 订单状态分类 ===
const orderStatusTab = ref<number | null>(null); // null表示全部
const orderStatusCount = ref<Record<number, number>>({
  0: 0, 1: 0, 2: 0, 3: 0, 4: 0, 5: 0, 6: 0
});

// 订单状态标签配置
const orderStatusTabs = [
  { value: null, label: "全部", icon: "Package" },
  { value: 0, label: "待付款", icon: "Clock" },
  { value: 1, label: "已付款", icon: "CreditCard" },
  { value: 2, label: "待发货", icon: "Package" },
  { value: 3, label: "已发货", icon: "Truck" },
  { value: 4, label: "已完成", icon: "CheckCircle" },
  { value: 5, label: "已取消", icon: "X" },
  { value: 6, label: "已退款", icon: "RefreshCw" },
];

// 计算全部订单数量
const totalOrderCount = computed(() => {
  return Object.values(orderStatusCount.value).reduce((acc, val) => acc + val, 0);
});
// 编辑表单数据
const editForm = ref({
  nickname: "",
  email: "",
  mobile: "",
  avatar: "",
  signature: "",
  sex: undefined,
});

// === 修改密码相关 ===
const showPasswordDialog = ref(false);
const passwordFormRef = ref();
const passwordForm = ref({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === "") {
    callback(new Error("请再次输入密码"));
  } else if (value !== passwordForm.value.newPassword) {
    callback(new Error("两次输入密码不一致!"));
  } else {
    callback();
  }
};

const passwordRules = {
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于6位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    { validator: validateConfirmPassword, trigger: "blur" },
  ],
};

// === 验证码相关 ===
const emailCode = ref("");
const countdown = ref(0);
let timer: ReturnType<typeof setInterval> | null = null;

// === 支付相关 ===
const showPaymentDialog = ref(false);
const paymentStep = ref<"checking" | "success">("checking");
const currentOrderNumber = ref("");
const payCountdown = ref(15 * 60);
const payTimer = ref<any>(null);
const pollingTimer = ref<any>(null);

// === 物流相关 ===
const showLogisticsDialog = ref(false);
const currentLogistics = ref<any>(null);


const formattedPayTime = computed(() => {
  const m = Math.floor(payCountdown.value / 60)
    .toString()
    .padStart(2, "0");
  const s = (payCountdown.value % 60).toString().padStart(2, "0");
  return `${m}:${s}`;
});

// 计算订单剩余支付时间
const getOrderRemainingTime = (createTime: string) => {
  const orderCreateTime = new Date(createTime).getTime();
  const now = Date.now();
  const elapsedSeconds = Math.floor((now - orderCreateTime) / 1000);
  const totalSeconds = 5 * 60; // 总共5分钟
  const remainingSeconds = Math.max(0, totalSeconds - elapsedSeconds);
  return remainingSeconds;
};

const formatTime = (seconds: number) => {
  const m = Math.floor(seconds / 60)
    .toString()
    .padStart(2, "0");
  const s = (seconds % 60).toString().padStart(2, "0");
  return `${m}:${s}`;
};

// 订单倒计时Map
const orderCountdowns = ref<Record<string, number>>({});
const orderCountdownTimer = ref<any>(null);
const autoCanceledOrders = ref<Set<string>>(new Set()); // 记录已自动取消的订单

// 更新所有未支付订单的倒计时
const updateOrderCountdowns = () => {
  orders.value.forEach((order) => {
    if (order.status === 0) {
      const remainingTime = getOrderRemainingTime(order.createTime);
      orderCountdowns.value[order.orderNumber] = remainingTime;

      // 如果倒计时结束且未被自动取消过,则自动取消订单
      if (remainingTime <= 0 && !autoCanceledOrders.value.has(order.orderNumber)) {
        autoCanceledOrders.value.add(order.orderNumber);
        autoCancelOrder(order.orderNumber);
      }
    }
  });
};

// 自动取消订单
const autoCancelOrder = async (orderNumber: string) => {
  try {
    await cancelOrderApi(orderNumber);
    ElMessage.warning(`订单 ${orderNumber} 已超时,已自动取消`);
    // 延迟刷新订单列表,避免立即刷新导致用户看不到提示
    setTimeout(() => {
      getOrders();
      getOrderStatusCount();
    }, 2000);
  } catch (error) {
    console.error("自动取消订单失败:", error);
    // 如果取消失败,从已取消集合中移除,下次继续尝试
    autoCanceledOrders.value.delete(orderNumber);
  }
};

// 判断邮箱是否改变
const isEmailChanged = computed(() => {
  return userStore.user && editForm.value.email !== userStore.user.email;
});

const getFavorites = async () => {
  const data = await getFavoritesApi();
  favorites.value = data.data;
};

// 删除收藏
const handleDeleteFavorite = async (productId: number) => {
  try {
    await deleteFavoritesApi(productId);
    ElMessage.success("已取消收藏");
    await getFavorites();
    emitter.emit("refresh");
  } catch (error) {
    console.error(error);
    ElMessage.error("取消收藏失败");
  }
};

// 添加到购物车
const handleAddToCart = async (favorite: Favorites) => {
  try {
    await addFavoritesToCartApi(favorite.productId);
    ElMessage.success("已添加到购物车");
    await getFavorites();
    emitter.emit("refresh");
  } catch (error) {
    console.error(error);
    ElMessage.error("添加失败");
  }
};

// 地址簿相关规则
const addressRules = {
  consignee: [{ required: true, message: "请输入收货人姓名", trigger: "blur" }],
  consigneeAddress: [{ required: true, message: "请输入收货人地址", trigger: "blur" }],
  consigneePhone: [{ required: true, message: "请输入收货人手机号", trigger: "blur" }],
};

// ... 地址簿逻辑保持不变 ...
const getAddresses = async () => {
  const data = await getAddtessListApi();
  addresses.value = data.data;
};

const setDefault = async (selectedAddressId: number) => {
  const hasDefault = addresses.value.find((item) => item.isDefault);
  if (hasDefault) {
    await updateAddressApi({ id: hasDefault.id, isDefault: 0 });
    await updateAddressApi({ id: selectedAddressId, isDefault: 1 });
  } else {
    await updateAddressApi({ id: selectedAddressId, isDefault: 1 });
  }
  getAddresses();
  ElMessage.success("设置默认地址成功");
};

const cancelDefault = async (selectedAddressId: number) => {
  if (!selectedAddressId) return;
  const hasDefault = addresses.value.find((item) => item.isDefault === 1);
  if (!hasDefault) return;
  await updateAddressApi({ id: hasDefault.id, isDefault: 0 });
  getAddresses();
  ElMessage.success("取消成功");
};

const editAddr = (address: Address) => {
  addressForm.value = { ...address };
  showAddressForm.value = true;
};

const resetAddressForm = () => {
  addressForm.value = {
    id: undefined,
    consignee: "",
    consigneeAddress: "",
    consigneePhone: "",
    isDefault: undefined,
  };
};

const addAddress = () => {
  resetAddressForm();
  showAddressForm.value = true;
};

const deleteAddress = async (selectedAddressId: number) => {
  ElMessageBox.confirm("此操作将永久删除该地址, 是否继续?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        await deleteAddressApi(selectedAddressId);
        ElMessage.success("删除成功");
        await getAddresses();
      } catch (error) {
        ElMessage.error("删除失败");
      }
    })
    .catch(() => {});
};

const saveAddress = async () => {
  addressFormRef.value?.validate((valid: any) => {
    if (valid) {
      if (addressForm.value.id !== undefined) {
        updateAddressApi(addressForm.value).then(() => {
          ElMessage.success("修改成功");
          showAddressForm.value = false;
          getAddresses();
        });
      } else {
        addAddressApi(addressForm.value).then(() => {
          ElMessage.success("添加成功");
          showAddressForm.value = false;
          getAddresses();
        });
      }
    }
  });
};

// === 个人信息编辑逻辑 ===

const startEditing = () => {
  if (userStore.user) {
    Object.assign(editForm.value, { ...userStore.user });
    isEditing.value = true;
    emailCode.value = ""; // 重置验证码
  }
};

const handleAvatarChange = async (uploadFile: any) => {
  if (uploadFile.raw) {
    const formData = new FormData();
    formData.append("file", uploadFile.raw);
    const response = await uploadApi(formData, "avatar");
    if (response.data) {
      editForm.value.avatar = response.data;
    }
  }
};

// 发送验证码
const sendCode = async () => {
  if (!editForm.value.email) return ElMessage.warning("请输入邮箱");
  if (countdown.value > 0) return;

  try {
    await getEmailCodeApi(editForm.value.email);
    ElMessage.success("验证码已发送，请查收");
    countdown.value = 60;
    timer = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0 && timer) clearInterval(timer);
    }, 1000);
  } catch (error) {
    console.error(error);
  }
};

const saveProfile = async () => {
  // 如果改了邮箱但没填验证码
  if (isEmailChanged.value && !emailCode.value) {
    ElMessage.warning("修改邮箱必须输入验证码");
    return;
  }

  const params = {
    ...editForm.value,
    code: isEmailChanged.value ? emailCode.value : undefined,
  };

  try {
    await updateUserApi(params);
    await userStore.getUserInfo();
    isEditing.value = false;
    ElMessage.success("保存成功");

    // 清理状态
    emailCode.value = "";
    if (timer) clearInterval(timer);
    countdown.value = 0;
  } catch (error) {
    // 错误处理
  }
};

const handleLogout = async () => {
  userStore.logout();
  router.push("/login");
};

// 修改密码
const openPasswordDialog = () => {
  passwordForm.value = {
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  };
  showPasswordDialog.value = true;
};

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return;

  await passwordFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await updatePasswordApi(passwordForm);

        ElMessage.success("密码修改成功,请重新登录");
        showPasswordDialog.value = false;

        // 延迟1秒后退出登录
        setTimeout(() => {
          userStore.logout();
          router.push("/login");
        }, 1000);
      } catch (error: any) {
        console.error(error);
        ElMessage.error(error.response?.data?.message || "密码修改失败");
      }
    }
  });
};

const toggleOrderExpand = (id: number) => {
  expandedOrder.value = expandedOrder.value === id ? null : id;
};

const getOrders = async () => {
  loading.value = true;
  try {
    const params: any = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
    };
    // 如果选择了特定状态，添加状态筛选
    if (orderStatusTab.value !== null) {
      params.status = orderStatusTab.value;
    }
    const res = await getOrderListApi(params);
    orders.value = res.data.records;
    totalOrders.value = res.data.total;

    // 初始化倒计时
    updateOrderCountdowns();
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 获取各状态订单数量
const getOrderStatusCount = async () => {
  try {
    const res = await getOrderStatusCountApi();
    orderStatusCount.value = res.data;
  } catch (error) {
    console.error("获取订单状态数量失败:", error);
  }
};

// 切换订单状态标签
const handleStatusTabChange = (status: number | null) => {
  orderStatusTab.value = status;
  currentPage.value = 1; // 重置页码
  getOrders();
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  getOrders();
};

const expandChange = async (row: any, expandedRows: any[]) => {
  if (expandedRows.length > 0) {
    // 展开时获取详情
    if (!orderDetailsMap.value[row.orderNumber]) {
      try {
        const res = await getOrderDetailApi(row.orderNumber);
        orderDetailsMap.value[row.orderNumber] = res.data;
      } catch (error) {
        console.error(error);
      }
    }
  }
};
const startPayCountdown = () => {
  payTimer.value = setInterval(() => {
    if (payCountdown.value > 0) payCountdown.value--;
    else {
      clearInterval(payTimer.value);
      // 倒计时结束逻辑，可以根据需求处理
    }
  }, 1000);
};

const startPollingOrder = () => {
  const startTime = Date.now();

  const poll = async () => {
    if (!showPaymentDialog.value || paymentStep.value !== "checking") return;

    try {
      const res = await getOrderStatusApi(currentOrderNumber.value);
      if (res.data === true) {
        paymentStep.value = "success";
        ElMessage.success("支付成功！");
        getOrders(); // 刷新列表
        return;
      }
    } catch (error) {
      console.warn("轮询状态异常", error);
    }

    const elapsedSeconds = (Date.now() - startTime) / 1000;
    let delay = 3000;

    if (elapsedSeconds > 300) delay = 30 * 1000;
    else if (elapsedSeconds > 180) delay = 10 * 1000;
    else if (elapsedSeconds > 60) delay = 5 * 1000;

    pollingTimer.value = setTimeout(poll, delay);
  };

  poll();
};

const handlePay = async (order: Order) => {
  currentOrderNumber.value = order.orderNumber;

  const newWindow = window.open("", "_blank");
  if (!newWindow) {
    ElMessage.error("支付窗口被拦截,请在浏览器设置中允许弹出窗口");
    return;
  }
  newWindow.document.title = "正在跳转支付宝...";
  newWindow.document.body.innerHTML = `
    <div style="text-align:center; padding-top:100px;">
      <h3 style="font-family: sans-serif;">正在连接支付宝安全支付...</h3>
      <p style="color:#666;">请勿关闭此窗口</p>
    </div>
  `;

  const loadingInstance = ElLoading.service({
    lock: true,
    text: "正在获取支付信息...",
    background: "rgba(0, 0, 0, 0.7)",
  });

  try {
    const res = await alipayPayApi(order.orderNumber);
    newWindow.document.open();
    newWindow.document.write(res.data);
    newWindow.document.close();

    // 打开支付弹窗
    showPaymentDialog.value = true;
    paymentStep.value = "checking";

    // 计算从订单创建时间到现在已经过去的时间
    const orderCreateTime = new Date(order.createTime).getTime();
    const now = Date.now();
    const elapsedSeconds = Math.floor((now - orderCreateTime) / 1000);
    const totalSeconds = 5 * 60; // 总共5分钟
    const remainingSeconds = Math.max(0, totalSeconds - elapsedSeconds);

    payCountdown.value = remainingSeconds;
    startPayCountdown();
    startPollingOrder();
  } catch (error) {
    newWindow.close();
    ElMessage.error("获取支付信息失败");
  } finally {
    loadingInstance.close();
  }
};

const closePaymentDialog = () => {
  showPaymentDialog.value = false;
  if (payTimer.value) clearInterval(payTimer.value);
  if (pollingTimer.value) clearTimeout(pollingTimer.value);
  getOrders(); // 关闭时刷新列表
  getOrderStatusCount(); // 刷新状态数量
};

// 查看物流
const handleViewLogistics = async (order: Order) => {
  showLogisticsDialog.value = true;
  try {
    const res = await getOrderLogistics(order.orderNumber);
    const logisticsData = res.data;
    
    currentLogistics.value = {
      orderNumber: logisticsData.orderNumber,
      company: "顺丰速运",
      trackingNumber: logisticsData.waybillNo || "暂无运单号",
      status: logisticsData.statusDesc || "待发货",
      tracks: (logisticsData.routes || []).map((route: any) => ({
        time: route.acceptTime,
        status: route.remark,
        location: route.acceptAddress || "",
      })),
    };
  } catch (error) {
    console.error("获取物流信息失败:", error);
    ElMessage.error("获取物流信息失败");
    // 使用空数据
    currentLogistics.value = {
      orderNumber: order.orderNumber,
      company: "顺丰速运",
      trackingNumber: "暂无运单号",
      status: "暂无物流信息",
      tracks: [],
    };
  }
};

const handleCancel = (order: Order) => {
  ElMessageBox.confirm("确定要取消该订单吗?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    await cancelOrderApi(order.orderNumber);
    ElMessage.success("取消成功");
    getOrders();
    getOrderStatusCount();
  });
};

const handleRefund = (order: Order) => {
  ElMessageBox.confirm("确定要申请退款吗?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    await refundOrderApi(order.orderNumber);
    ElMessage.success("申请退款成功");
    getOrders();
    getOrderStatusCount();
  });
};

const handleConfirm = (order: Order) => {
  ElMessageBox.confirm("确认已收到货物?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "info",
  }).then(async () => {
    await confirmReceiptApi(order.orderNumber);
    ElMessage.success("确认收货成功");
    getOrders();
    getOrderStatusCount();
  });
};

const handleDelete = (order: Order) => {
  ElMessageBox.confirm("确定要删除该订单记录吗?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    await deleteOrderApi(order.orderNumber);
    ElMessage.success("删除成功");
    getOrders();
    getOrderStatusCount();
  });
};

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: "待付款",
    1: "已付款",
    2: "待发货",
    3: "已发货",
    4: "已完成",
    5: "已取消",
    6: "已退款",
  };
  return map[status] || "未知状态";
};

const getStatusTagType = (status: number) => {
  const map: Record<number, string> = {
    0: "warning",
    1: "success",
    2: "primary",
    3: "primary",
    4: "success",
    5: "info",
    6: "info",
  };
  return map[status] || "info";
};

onMounted(() => {
  getFavorites();
  getAddresses();
  getOrders();
  getOrderStatusCount();

  // 启动订单倒计时定时器
  orderCountdownTimer.value = setInterval(() => {
    updateOrderCountdowns();
  }, 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
  if (payTimer.value) clearInterval(payTimer.value);
  if (pollingTimer.value) clearTimeout(pollingTimer.value);
  if (orderCountdownTimer.value) clearInterval(orderCountdownTimer.value);
});
</script>

<template>
  <div class="pt-24 pb-20 max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
    <!-- Profile Header -->
    <div
      v-if="userStore.user"
      class="flex flex-col md:flex-row items-center gap-8 mb-12 p-8 bg-white dark:bg-zinc-900 rounded-3xl border border-gray-100 dark:border-zinc-800 shadow-sm relative overflow-hidden"
    >
      <!-- Avatar Section -->
      <div class="relative group">
        <img
          :src="isEditing ? editForm.avatar : userStore.user.avatar"
          :alt="userStore.user.username"
          class="w-32 h-32 rounded-full object-cover border-4 border-white dark:border-zinc-800 shadow-lg"
        />
        <div v-if="isEditing" class="absolute inset-0">
          <el-upload
            class="w-full h-full flex items-center justify-center"
            action="#"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleAvatarChange"
            accept="image/*"
          >
            <div
              class="w-32 h-32 rounded-full flex items-center justify-center bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer text-white absolute top-0 left-0 inset-0 z-10"
            >
              <Camera :size="24" />
            </div>
          </el-upload>
        </div>
      </div>

      <!-- Info / Edit Form Section -->
      <div class="text-center md:text-left flex-1 w-full">
        <!-- 编辑模式 -->
        <div v-if="isEditing" class="grid gap-4 w-full max-w-md animate-fade-in">
          <el-input v-model="editForm.nickname" placeholder="昵称" />

          <!-- 邮箱编辑区域 -->
          <div v-if="userStore.user.loginType === 'email'">
            <el-input v-model="editForm.email" placeholder="邮箱" />
            <!-- 当检测到邮箱变化时显示验证码输入框 -->
            <div v-if="isEmailChanged" class="mt-2 flex gap-2 animate-fade-in">
              <el-input v-model="emailCode" placeholder="验证码" class="flex-1" />
              <el-button
                type="primary"
                plain
                :disabled="countdown > 0"
                @click="sendCode"
                class="w-32"
              >
                {{ countdown > 0 ? `${countdown}s` : "获取验证码" }}
              </el-button>
            </div>
          </div>

          <el-input v-model="editForm.mobile" placeholder="电话" />
          <el-input v-model="editForm.signature" placeholder="个性签名" />
          <el-radio-group v-model="editForm.sex">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="0">女</el-radio>
          </el-radio-group>
          <div class="flex gap-2 mt-2 justify-center md:justify-start">
            <el-button type="primary" @click="saveProfile">
              <Save :size="14" class="mr-2" /> 保存
            </el-button>
            <el-button @click="isEditing = false">
              <X :size="14" class="mr-2" /> 取消
            </el-button>
          </div>
        </div>

        <!-- 展示模式 -->
        <div v-else>
          <h1 class="text-3xl font-bold mb-2">{{ userStore.user.nickname }}</h1>
          <p class="text-gray-500 mb-1" v-if="userStore.user.loginType === 'email'">
            用户名：{{ userStore.user.username }}
          </p>
          <p class="text-gray-500 text-sm mb-1">
            性别：{{ userStore.user.sex === 1 ? "男" : "女" }}
          </p>
          <p
            class="text-gray-500 text-sm mb-1"
            v-if="userStore.user.loginType === 'email'"
          >
            邮箱：{{ userStore.user.email || "无" }}
          </p>
          <p class="text-gray-500 text-sm mb-1">
            电话：{{ userStore.user.mobile || "无" }}
          </p>
          <p class="text-gray-500 text-sm mb-1">
            个性签名：{{ userStore.user.signature || "无" }}
          </p>

          <div class="flex gap-4 justify-center md:justify-start mt-4">
            <el-button round @click="startEditing">
              <Edit2 :size="16" class="mr-2" /> 编辑信息
            </el-button>
            <el-button
              v-if="userStore.user.loginType === 'email'"
              round
              type="warning"
              plain
              @click="openPasswordDialog"
            >
              <Lock :size="16" class="mr-2" /> 修改密码
            </el-button>
            <el-button round type="danger" plain @click="handleLogout">
              <LogOut :size="16" class="mr-2" /> 退出登录
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 剩下的 Tabs 和 Content 保持不变 -->
    <div
      class="flex overflow-x-auto gap-2 mb-8 pb-2 border-b border-gray-200 dark:border-zinc-800"
    >
      <!-- Tab 按钮代码省略... -->
      <button
        @click="activeTab = 'orders'"
        :class="`px-6 py-3 rounded-full text-sm font-bold whitespace-nowrap transition-colors ${
          activeTab === 'orders'
            ? 'bg-black text-white dark:bg-white dark:text-black'
            : 'text-gray-500 hover:bg-gray-100 dark:hover:bg-zinc-800'
        }`"
      >
        <Package :size="16" class="inline mr-2" /> 订单
      </button>
      <button
        @click="activeTab = 'favorites'"
        :class="`px-6 py-3 rounded-full text-sm font-bold whitespace-nowrap transition-colors ${
          activeTab === 'favorites'
            ? 'bg-black text-white dark:bg-white dark:text-black'
            : 'text-gray-500 hover:bg-gray-100 dark:hover:bg-zinc-800'
        }`"
      >
        <Heart :size="16" class="inline mr-2" /> 收藏夹 ({{ favorites.length }})
      </button>
      <button
        @click="activeTab = 'addresses'"
        :class="`px-6 py-3 rounded-full text-sm font-bold whitespace-nowrap transition-colors ${
          activeTab === 'addresses'
            ? 'bg-black text-white dark:bg-white dark:text-black'
            : 'text-gray-500 hover:bg-gray-100 dark:hover:bg-zinc-800'
        }`"
      >
        <MapPin :size="16" class="inline mr-2" /> 地址簿({{ addresses.length }})
      </button>
    </div>

    <!-- Tab Content 代码省略 (与原代码相同) ... -->
    <div class="min-h-[400px] animate-fade-in">
      <!-- 复制你原有的 Content 区域代码即可 -->
      <div v-if="activeTab === 'orders'" class="space-y-4">
        <!-- 订单状态标签 -->
        <div class="flex flex-wrap gap-2 mb-6 p-4 bg-gray-50 dark:bg-zinc-800/50 rounded-xl">
          <button
            v-for="tab in orderStatusTabs"
            :key="tab.value ?? 'all'"
            @click="handleStatusTabChange(tab.value)"
            :class="[
              'relative px-4 py-2 rounded-lg text-sm font-medium transition-all',
              orderStatusTab === tab.value
                ? 'bg-black text-white dark:bg-white dark:text-black shadow-md'
                : 'bg-white dark:bg-zinc-700 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-zinc-600 border border-gray-200 dark:border-zinc-600'
            ]"
          >
            <span>{{ tab.label }}</span>
            <span
              v-if="tab.value === null ? totalOrderCount > 0 : orderStatusCount[tab.value] > 0"
              :class="[
                'ml-1.5 px-1.5 py-0.5 text-xs rounded-full',
                orderStatusTab === tab.value
                  ? 'bg-white/20 text-white dark:bg-black/20 dark:text-black'
                  : 'bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400'
              ]"
            >
              {{ tab.value === null ? totalOrderCount : orderStatusCount[tab.value] }}
            </span>
          </button>
        </div>

        <div v-if="orders.length === 0 && !loading" class="text-center py-20 text-gray-500">
          <Package :size="48" class="mx-auto mb-4 text-gray-300" />
          <p>{{ orderStatusTab === null ? '暂无订单' : '该状态下暂无订单' }}</p>
          <RouterLink to="/shop" class="text-blue-600 hover:underline mt-2 block"
            >去购物</RouterLink
          >
        </div>
        <div v-else>
          <el-table :data="orders" @expand-change="expandChange" row-key="id" v-loading="loading">
            <el-table-column type="expand">
              <template #default="props">
                <div class="p-6 bg-gray-50 dark:bg-zinc-800/50 rounded-xl">
                  <!-- 订单信息头部：地址 & 支付信息 -->
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
                    <!-- 收货信息 -->
                    <div class="space-y-3">
                      <div
                        class="flex items-center gap-2 text-gray-900 dark:text-white font-bold text-lg"
                      >
                        <MapPin :size="20" class="text-blue-600" />
                        收货信息
                      </div>
                      <div
                        class="pl-7 space-y-1 text-sm text-gray-600 dark:text-gray-300"
                      >
                        <div class="flex items-center gap-2">
                          <span class="font-medium text-gray-900 dark:text-white">{{
                            props.row.consignee
                          }}</span>
                          <span class="text-gray-400">|</span>
                          <span>{{ props.row.consigneePhone }}</span>
                        </div>
                        <div class="leading-relaxed">
                          {{ props.row.consigneeAddress }}
                        </div>
                      </div>
                    </div>

                    <!-- 支付信息 (仅已付款订单显示) -->
                    <div
                      v-if="[1, 2, 3, 4, 6].includes(props.row.status)"
                      class="space-y-3"
                    >
                      <div
                        class="flex items-center gap-2 text-gray-900 dark:text-white font-bold text-lg"
                      >
                        <CreditCard :size="20" class="text-green-600" />
                        支付信息
                      </div>
                      <div
                        class="pl-7 space-y-2 text-sm text-gray-600 dark:text-gray-300"
                      >
                        <div class="flex items-center gap-2">
                          <span class="text-gray-500">支付方式:</span>
                          <span class="font-medium">{{
                            props.row.payMethod || "支付宝"
                          }}</span>
                        </div>
                        <div class="flex items-center gap-2">
                          <span class="text-gray-500">支付时间:</span>
                          <div class="flex items-center gap-1">
                            <Clock :size="14" class="text-gray-400" />
                            <span>{{ props.row.checkoutTime || "未知" }}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- 商品列表 (Grid 布局) -->
                  <div v-if="orderDetailsMap[props.row.orderNumber]">
                    <div
                      class="flex items-center gap-2 mb-4 text-gray-900 dark:text-white font-bold"
                    >
                      <Package :size="20" class="text-purple-600" />
                      商品清单
                    </div>
                    <div
                      class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4"
                    >
                      <div
                        v-for="item in orderDetailsMap[props.row.orderNumber]"
                        :key="item.productId"
                        class="group relative bg-white dark:bg-zinc-900 border border-gray-200 dark:border-zinc-700 rounded-xl overflow-hidden hover:shadow-md transition-all cursor-pointer"
                        @click="router.push(`/product/${item.productId}`)"
                      >
                        <div class="aspect-square relative bg-gray-100 dark:bg-black">
                          <img
                            :src="item.productImage"
                            class="w-full h-full object-cover"
                          />
                          <div
                            class="absolute bottom-0 right-0 bg-black/60 text-white text-xs px-2 py-1 rounded-tl-lg"
                          >
                            x{{ item.number }}
                          </div>
                        </div>
                        <div class="p-3">
                          <h4 class="font-medium text-sm truncate mb-1">
                            {{ item.productName }}
                          </h4>
                          <div class="text-blue-600 font-bold text-sm">
                            ￥{{ item.productPrice }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div v-else class="text-center py-8 text-gray-500">
                    <el-icon class="is-loading mr-2"><Loading /></el-icon>
                    正在加载订单详情...
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="订单号" prop="orderNumber" width="180" />
            <el-table-column label="下单时间" prop="createTime" width="180" />
            <el-table-column label="总金额" width="120">
              <template #default="scope">
                <span class="font-bold">￥{{ scope.row.amount }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusTagType(scope.row.status)">{{
                  getStatusText(scope.row.status)
                }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" min-width="250">
              <template #default="scope">
                <div class="flex gap-2 flex-wrap items-center">
                  <el-button
                    v-if="scope.row.status === 0"
                    type="primary"
                    size="small"
                    @click="handlePay(scope.row)"
                    >去支付</el-button
                  >
                  <span
                    v-if="
                      scope.row.status === 0 && orderCountdowns[scope.row.orderNumber] > 0
                    "
                    class="text-red-600 font-mono text-sm font-bold"
                  >
                    {{ formatTime(orderCountdowns[scope.row.orderNumber]) }}
                  </span>
                  <span
                    v-if="
                      scope.row.status === 0 &&
                      orderCountdowns[scope.row.orderNumber] <= 0
                    "
                    class="text-gray-400 text-sm"
                  >
                    已超时
                  </span>
                  <el-button
                    v-if="scope.row.status === 0"
                    type="danger"
                    plain
                    size="small"
                    @click="handleCancel(scope.row)"
                    >取消</el-button
                  >

                  <el-button
                    v-if="scope.row.status === 1 || scope.row.status === 2"
                    type="warning"
                    plain
                    size="small"
                    @click="handleRefund(scope.row)"
                    >退款</el-button
                  >

                  <el-button
                    v-if="scope.row.status === 3"
                    type="success"
                    size="small"
                    @click="handleConfirm(scope.row)"
                    >确认收货</el-button
                  >
                  <el-button
                    v-if="scope.row.status === 3 || scope.row.status === 4"
                    type="info"
                    plain
                    size="small"
                    @click="handleViewLogistics(scope.row)"
                    >查看物流</el-button
                  >

                  <el-button
                    v-if="[4, 5, 6].includes(scope.row.status)"
                    type="danger"
                    link
                    size="small"
                    @click="handleDelete(scope.row)"
                    >删除订单</el-button
                  >
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div class="flex justify-center mt-6">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="totalOrders"
              layout="prev, pager, next"
              @current-change="handlePageChange"
              background
            />
          </div>
        </div>
      </div>
      <div v-if="activeTab === 'favorites'">
        <!-- ... -->
        <div v-if="favorites.length === 0" class="text-center py-20 text-gray-500">
          <Heart :size="48" class="mx-auto mb-4 text-gray-300" />
          <p>你的收藏夹是空的</p>
          <RouterLink to="/shop" class="text-blue-600 hover:underline mt-2 block"
            >去购物</RouterLink
          >
        </div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          <!-- 渲染收藏夹列表 -->
          <div
            v-for="product in favorites"
            :key="product.id"
            class="group bg-white dark:bg-zinc-900 border border-gray-100 dark:border-zinc-800 rounded-2xl p-4 transition-all hover:shadow-lg"
          >
            <div
              class="relative aspect-square bg-gray-50 dark:bg-black rounded-xl mb-4 overflow-hidden"
            >
              <img
                :src="product.productImage"
                :alt="product.productName"
                class="w-full h-full object-cover mix-blend-multiply dark:mix-blend-normal"
              />
              <el-button
                circle
                class="absolute top-2 right-2 !p-2"
                @click.prevent="handleDeleteFavorite(product.productId)"
              >
                <X :size="16" class="text-red-500" />
              </el-button>

              <el-button
                circle
                class="absolute bottom-2 right-2 !p-2 bg-blue-600 hover:bg-blue-700 border-0"
                @click.prevent="handleAddToCart(product)"
              >
                <ShoppingCart :size="16" />
              </el-button>
            </div>
            <RouterLink :to="`/product/${product.productId}`">
              <h3 class="font-semibold truncate">{{ product.productName }}</h3>
              <div class="flex justify-between items-center">
                <span class="font-bold">￥{{ product.productPrice }}</span>
                <span class="text-xs text-blue-600 font-medium group-hover:underline"
                  >查看详情</span
                >
              </div>
            </RouterLink>
          </div>
        </div>
      </div>
      <div v-if="activeTab === 'addresses'" class="space-y-6">
        <!-- ... 复制原有地址簿代码 -->
        <div v-if="userStore.user" class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <button
            @click="addAddress"
            class="border-2 border-dashed border-gray-300 dark:border-zinc-700 rounded-2xl p-6 flex flex-col items-center justify-center min-h-[180px] text-gray-500 hover:border-black dark:hover:border-white hover:text-black dark:hover:text-white transition-all"
          >
            <Plus :size="32" class="mb-2" />
            <span class="font-medium">添加收货地址</span>
          </button>

          <div
            v-for="addr in addresses"
            :key="addr.id"
            :class="`relative p-6 rounded-2xl border ${
              addr.isDefault
                ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/10'
                : 'border-gray-200 dark:border-zinc-800 bg-white dark:bg-zinc-900'
            }`"
          >
            <div
              v-if="addr.isDefault"
              class="absolute top-4 right-4 px-2 py-1 bg-blue-500 text-white text-xs font-bold rounded"
            >
              默认
            </div>
            <div class="flex items-center gap-2 mb-2">
              <MapPin :size="18" class="text-gray-400" />
              <span class="font-bold text-lg">{{ addr.consignee }}</span>
            </div>
            <p class="text-gray-600 dark:text-gray-300 text-sm leading-relaxed mb-4">
              {{ addr.consigneeAddress }}<br />
              {{ addr.consigneePhone }}
            </p>
            <div class="flex gap-3 pt-4 border-t border-gray-200 dark:border-zinc-700/50">
              <el-button
                v-if="!addr.isDefault"
                link
                type="primary"
                size="small"
                @click="setDefault(addr.id)"
                >设为默认</el-button
              >
              <el-button
                v-if="addr.isDefault"
                link
                type="primary"
                size="small"
                @click="cancelDefault(addr.id)"
                >取消默认</el-button
              >
              <el-button link type="primary" size="small" @click="editAddr(addr)"
                >编辑</el-button
              >
              <el-button link type="danger" size="small" @click="deleteAddress(addr.id)">
                <Trash2 :size="12" class="mr-1" /> 删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Dialog 保持不变 -->
    <el-dialog
      v-model="showAddressForm"
      title="添加收货地址"
      width="500px"
      class="dark:bg-zinc-900"
    >
      <!-- ... -->
      <el-form
        ref="addressFormRef"
        :model="addressForm"
        :rules="addressRules"
        label-position="top"
      >
        <el-form-item label="收货人" prop="consignee">
          <el-input v-model="addressForm.consignee" />
        </el-form-item>
        <el-form-item label="收货地址" prop="consigneeAddress">
          <el-input v-model="addressForm.consigneeAddress" />
        </el-form-item>
        <el-form-item label="电话" prop="consigneePhone">
          <el-input v-model="addressForm.consigneePhone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddressForm = false">取消</el-button>
          <el-button type="primary" @click="saveAddress">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 支付状态弹窗 -->
    <el-dialog
      v-model="showPaymentDialog"
      title="支付状态"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      class="dark:bg-zinc-900"
    >
      <div
        v-if="paymentStep === 'checking'"
        class="flex flex-col items-center py-8 text-center"
      >
        <Loader2 :size="48" class="animate-spin text-blue-600 mb-6" />
        <h2 class="text-2xl font-bold mb-2">正在等待支付结果...</h2>
        <p class="text-gray-500 max-w-md mb-6">
          已在新窗口打开支付宝页面。支付完成后，此页面将自动更新。
        </p>

        <div
          class="bg-red-50 text-red-600 px-4 py-2 rounded-lg inline-block mb-8 font-mono font-bold text-xl"
        >
          剩余支付时间: {{ formattedPayTime }}
        </div>

        <div class="flex gap-4">
          <el-button @click="handlePay({ orderNumber: currentOrderNumber } as any)"
            >重新打开支付页</el-button
          >
          <el-button type="info" text @click="closePaymentDialog"
            >遇到问题？关闭窗口</el-button
          >
        </div>
      </div>

      <div
        v-else-if="paymentStep === 'success'"
        class="flex flex-col items-center py-8 text-center"
      >
        <div
          class="w-24 h-24 bg-green-100 text-green-600 rounded-full flex items-center justify-center mb-6 shadow-lg shadow-green-500/20"
        >
          <CheckCircle :size="48" />
        </div>
        <h1 class="text-3xl font-bold mb-4">支付成功!</h1>
        <p class="text-gray-500 mb-8">感谢您的购买。</p>
        <el-button
          type="primary"
          size="large"
          round
          class="!px-12 !font-bold"
          @click="closePaymentDialog"
        >
          确定
        </el-button>
      </div>
    </el-dialog>

    <!-- 物流信息弹窗 -->
    <el-dialog
      v-model="showLogisticsDialog"
      title="物流信息"
      width="600px"
      class="dark:bg-zinc-900"
    >
      <div v-if="currentLogistics" class="space-y-4">
        <!-- 物流公司信息 -->
        <div class="bg-blue-50 dark:bg-blue-900/20 p-4 rounded-lg">
          <div class="flex items-center gap-3 mb-2">
            <Truck :size="24" class="text-blue-600" />
            <div>
              <div class="font-bold text-lg">{{ currentLogistics.company }}</div>
              <div class="text-sm text-gray-500">
                运单号: {{ currentLogistics.trackingNumber }}
              </div>
            </div>
          </div>
          <div class="text-sm mt-2">
            <span class="text-gray-600">订单号:</span>
            <span class="font-mono ml-2">{{ currentLogistics.orderNumber }}</span>
          </div>
        </div>

        <!-- 物流轨迹 -->
        <div class="relative">
          <div class="text-sm font-bold mb-4 text-gray-700 dark:text-gray-300">
            物流轨迹
          </div>
          <div class="space-y-4">
            <div
              v-for="(track, index) in currentLogistics.tracks"
              :key="index"
              class="relative pl-8 pb-4"
              :class="{
                'border-l-2 border-gray-200 dark:border-zinc-700':
                  index !== currentLogistics.tracks.length - 1,
              }"
            >
              <!-- 时间轴节点 -->
              <div
                class="absolute left-0 top-0 w-3 h-3 rounded-full"
                :class="index === 0 ? 'bg-blue-600' : 'bg-gray-300 dark:bg-zinc-600'"
              ></div>

              <!-- 物流信息 -->
              <div>
                <div class="text-xs text-gray-500 mb-1">{{ track.time }}</div>
                <div
                  class="text-sm"
                  :class="
                    index === 0
                      ? 'text-blue-600 font-bold'
                      : 'text-gray-700 dark:text-gray-300'
                  "
                >
                  {{ track.status }}
                </div>
                <div class="text-xs text-gray-400 mt-1">{{ track.location }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 提示信息 -->
        <div
          class="bg-yellow-50 dark:bg-yellow-900/20 p-3 rounded-lg text-sm text-gray-600 dark:text-gray-400"
        >
          <div class="flex items-start gap-2">
            <span class="text-yellow-600">💡</span>
            <div>
              <div class="font-medium mb-1">温馨提示</div>
              <div class="text-xs">• 物流信息仅供参考,实际配送时间可能有所延迟</div>
              <div class="text-xs">• 如有疑问请联系客服或快递公司</div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showLogisticsDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="500px"
      class="dark:bg-zinc-900"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-position="top"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码(至少6位)"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button type="primary" @click="handleUpdatePassword">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
