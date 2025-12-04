<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, reactive } from "vue";
import { useRouter } from "vue-router";
import { 
  CreditCard, Truck, CheckCircle, Plus, Loader2, 
  MapPin, Phone, User, Pencil, Trash2, Star, X, Save, Pin
} from "lucide-vue-next";
import { ElMessage, ElLoading, ElMessageBox, FormInstance } from "element-plus";
import { useCartStore } from "@/stores/modules/cart";
import { submitOrderApi, alipayPayApi, getOrderStatusApi } from "@/api/order"; // 确保引入了查询订单状态接口
import {
  addAddressApi,
  getAddtessListApi,
  updateAddressApi,
  deleteAddressApi,
} from "@/api/address";
import { Address } from "@/types";
import { emitter } from "@/event/emitter";

const router = useRouter();
const cartStore = useCartStore();

// --- 核心状态 ---
// 增加 'checking' 状态，表示正在轮询支付结果
const step = ref<"shipping" | "payment" | "checking" | "success">("shipping");
const selectedAddressId = ref<string | number>(""); 
const isSubmitting = ref(false);
const addresses = ref<Address[]>([]);

// --- 编辑/新增 相关状态 ---
const editingId = ref<number | null>(null); 
const addressFormRef = ref<FormInstance>();

const addressForm = ref<Partial<Address>>({
  consignee: "",
  consigneeAddress: "",
  consigneePhone: "",
  isDefault: 0,
});

const addressRules = {
  consignee: [{ required: true, message: "请输入收货人", trigger: "blur" }],
  consigneeAddress: [{ required: true, message: "请输入详细地址", trigger: "blur" }],
  consigneePhone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" }
  ],
};

const total = computed(() => cartStore.selectedTotal);
const orderItems = computed(() => cartStore.selectedCartItems);
const countdown = ref(5 * 60);
const timer = ref<any>(null); // 倒计时 Timer
const pollingTimer = ref<any>(null); // 轮询 Timer

const formattedTime = computed(() => {
  const m = Math.floor(countdown.value / 60).toString().padStart(2, "0");
  const s = (countdown.value % 60).toString().padStart(2, "0");
  return `${m}:${s}`;
});

// --- API & 逻辑方法 ---

const getAddtessList = async () => {
  const res = await getAddtessListApi();
  addresses.value = res.data;
  if (!addresses.value.find(a => a.id === selectedAddressId.value)) {
     const defaultAddr = addresses.value.find(a => a.isDefault);
     if (defaultAddr) selectedAddressId.value = defaultAddr.id;
     else if (addresses.value.length > 0) selectedAddressId.value = addresses.value[0].id;
     else selectedAddressId.value = "";
  }
};

const startAddAddress = () => {
  editingId.value = -1;
  addressForm.value = { consignee: "", consigneeAddress: "", consigneePhone: "", isDefault: 0 };
};

const startEditAddress = (addr: Address, event: Event) => {
  event.stopPropagation();
  editingId.value = addr.id;
  addressForm.value = { ...addr };
};

const cancelEdit = () => {
  editingId.value = null;
  addressFormRef.value?.clearValidate();
};

const saveAddress = async () => {
  if (!addressFormRef.value) return;
  await addressFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (editingId.value === -1) {
          await addAddressApi(addressForm.value as Address);
          ElMessage.success("地址添加成功");
        } else {
          await updateAddressApi(addressForm.value as Address);
          ElMessage.success("地址修改成功");
        }
        await getAddtessList();
        editingId.value = null;
      } catch (error) {
        console.error(error);
      }
    }
  });
};

const handleDeleteAddress = (id: number, event: Event) => {
  event.stopPropagation();
  ElMessageBox.confirm("确定要删除这个地址吗?", "提示", { confirmButtonText: "删除", cancelButtonText: "取消", type: "warning" })
    .then(async () => {
      try {
        await deleteAddressApi(id);
        ElMessage.success("删除成功");
        await getAddtessList();
      } catch (error) {}
    });
};

const handleSetDefault = async (addr: Address, event: Event) => {
  event.stopPropagation();
  try {
    const oldDefault = addresses.value.find(a => a.isDefault === 1);
    if (oldDefault) await updateAddressApi({ ...oldDefault, isDefault: 0 });
    await updateAddressApi({ ...addr, isDefault: 1 });
    ElMessage.success("已设为默认地址");
    await getAddtessList();
  } catch (error) {
    ElMessage.error("设置失败");
  }
};

// 提交订单
const handlePlaceOrder = async () => {
  if (editingId.value !== null) return ElMessage.warning("请先保存地址");
  if (!selectedAddressId.value) return ElMessage.error("请选择收货地址");

  try {
    isSubmitting.value = true;
    const params = {
      addressId: selectedAddressId.value,
      cartItemIds: orderItems.value.map((item) => item.id),
      remark: "",
    };
    const res = await submitOrderApi(params);
    emitter.emit("refresh");
    const orderNumber = res.data;
    step.value = "payment";
    currentOrderNumber.value = orderNumber;
    startCountdown();
  } catch (error) {
    console.error(error);
  } finally {
    isSubmitting.value = false;
  }
};

const currentOrderNumber = ref("");

const startCountdown = () => {
  timer.value = setInterval(() => {
    if (countdown.value > 0) countdown.value--;
    else {
      clearInterval(timer.value);
      ElMessage.error("订单已超时");
      router.push("/cart");
    }
  }, 1000);
};

// --- 核心修改：支付与轮询 ---

// 开始轮询订单状态
const startPollingOrder = () => {
  const startTime = Date.now();
  
  const poll = async () => {
    // 如果组件被销毁或状态不再是 checking，停止轮询
    if (step.value !== 'checking') return;

    try {
      const res = await getOrderStatusApi(currentOrderNumber.value);
      if (res.data === true) { 
        step.value = 'success';
        ElMessage.success("支付成功！");
        return; 
      }
    } catch (error) {
      console.warn("轮询状态异常", error);
    }

    // 计算已过去的时间（秒）
    const elapsedSeconds = (Date.now() - startTime) / 1000;
    
    // 动态决定下一次间隔
    let delay = 3000; // 默认 3秒
    
    if (elapsedSeconds > 300) {
      delay = 30 * 1000; // 5分钟后：30秒一次
    } else if (elapsedSeconds > 180) {
      delay = 10 * 1000; // 3-5分钟：10秒一次
    } else if (elapsedSeconds > 60) {
      delay = 5 * 1000;  // 1-3分钟：5秒一次
    }
    // 0-1分钟：3秒一次 (默认)

    // 递归调用
    pollingTimer.value = setTimeout(poll, delay);
  };

  poll(); // 立即开始第一次
};

const handlePay = async () => {
  if (!currentOrderNumber.value) return;

  // 1. 【关键】在 await 之前立刻打开新窗口，防止浏览器拦截，并确保是新标签页
  const newWindow = window.open("", "_blank");

  if (!newWindow) {
    ElMessage.error("支付窗口被拦截，请在浏览器设置中允许弹出窗口");
    return;
  }

  // 2. 给新窗口先写入一些提示，避免用户看到白屏
  newWindow.document.title = "正在跳转支付宝...";
  newWindow.document.body.innerHTML = `
    <div style="text-align:center; padding-top:100px;">
      <h3 style="font-family: sans-serif;">正在连接支付宝安全支付...</h3>
      <p style="color:#666;">请勿关闭此窗口</p>
    </div>
  `;

  const loading = ElLoading.service({
    lock: true,
    text: "正在获取支付信息...",
    background: "rgba(0, 0, 0, 0.7)",
  });

  try {
    // 3. 获取后端返回的 HTML 表单
    const res = await alipayPayApi(currentOrderNumber.value);
    
    // 4. 【关键】将表单写入刚才打开的 newWindow，而不是当前 document
    newWindow.document.open();
    newWindow.document.write(res.data);
    newWindow.document.close(); // 关闭写入流，触发表单内的 script 自动提交

    // 5. 本页面进入“轮询中”状态
    step.value = "checking";
    startPollingOrder();

  } catch (error) {
    console.error("支付调起失败", error);
    newWindow.close(); // 如果报错，关闭刚才打开的空窗口
    ElMessage.error("获取支付信息失败，请重试");
  } finally {
    loading.close();
  }
};

onMounted(() => {
  getAddtessList();
  if (cartStore.selectedCartItems.length === 0) {
    ElMessage.warning("没有待结算的商品");
    router.push("/cart");
  }
});

onUnmounted(() => {
  if (timer.value) clearInterval(timer.value);
  if (pollingTimer.value) clearTimeout(pollingTimer.value);
});
</script>

<template>
  <div class="pt-24 pb-20 max-w-6xl mx-auto px-4 sm:px-6">
    <h1 class="text-3xl font-bold mb-12">结算中心</h1>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-12">
      <!-- 左侧内容 -->
      <div class="lg:col-span-2">
        
        <!-- 阶段一：收货地址 -->
        <div v-if="step === 'shipping'">
           <div class="bg-white dark:bg-zinc-900 p-8 rounded-3xl border border-gray-100 dark:border-zinc-800 shadow-sm">
            <div class="flex items-center gap-3 mb-6">
              <Truck class="text-blue-600" />
              <h2 class="text-xl font-bold">收货信息</h2>
            </div>
            <div class="grid grid-cols-1 gap-4">
              <template v-for="addr in addresses" :key="addr.id">
                <div v-if="editingId === addr.id" class="p-6 rounded-2xl border-2 border-blue-500 bg-blue-50/10 dark:bg-blue-900/10 animate-fade-in">
                  <div class="flex justify-between items-center mb-4">
                    <span class="font-bold text-blue-600 flex items-center gap-2"><Pencil :size="16"/> 编辑地址</span>
                  </div>
                  <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-position="top" size="large">
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <el-form-item label="收货人" prop="consignee"><el-input v-model="addressForm.consignee" /></el-form-item>
                      <el-form-item label="手机号" prop="consigneePhone"><el-input v-model="addressForm.consigneePhone" /></el-form-item>
                      <el-form-item label="详细地址" prop="consigneeAddress" class="md:col-span-2"><el-input v-model="addressForm.consigneeAddress" /></el-form-item>
                    </div>
                    <div class="flex justify-end gap-3 mt-2">
                      <el-button @click="cancelEdit" icon="Close">取消</el-button>
                      <el-button type="primary" @click="saveAddress" icon="Check">保存修改</el-button>
                    </div>
                  </el-form>
                </div>
                <div v-else @click="selectedAddressId = addr.id" :class="`group relative p-5 rounded-2xl border-2 cursor-pointer transition-all duration-200 ${selectedAddressId === addr.id ? 'border-blue-600 bg-blue-50 dark:bg-blue-900/20 shadow-md' : 'border-gray-100 dark:border-zinc-800 hover:border-gray-300 hover:shadow-sm bg-white dark:bg-zinc-800'}`">
                  <div v-if="selectedAddressId === addr.id" class="absolute top-0 right-0 p-2 text-blue-600"><CheckCircle :size="20" class="fill-blue-100 dark:fill-blue-900" /></div>
                  <div v-if="addr.isDefault" class="absolute top-0 left-0 px-3 py-1 bg-blue-600 text-white text-xs font-bold rounded-br-xl rounded-tl-xl">默认</div>
                  <div class="flex items-start gap-4">
                    <div class="mt-1 p-2 bg-gray-100 dark:bg-black rounded-full text-gray-500"><MapPin :size="20" /></div>
                    <div class="flex-1">
                      <div class="flex items-center gap-3 mb-1"><span class="font-bold text-lg">{{ addr.consignee }}</span><span class="text-gray-500 text-sm">{{ addr.consigneePhone }}</span></div>
                      <p class="text-gray-600 dark:text-gray-300 text-sm leading-relaxed pr-8">{{ addr.consigneeAddress }}</p>
                    </div>
                  </div>
                  <div class="flex items-center justify-end gap-2 mt-4 pt-3 border-t border-gray-100 dark:border-zinc-700 opacity-100 sm:opacity-0 sm:group-hover:opacity-100 transition-opacity">
                    <el-tooltip content="设为默认" placement="top" v-if="!addr.isDefault"><button @click="(e) => handleSetDefault(addr, e)" class="p-2 text-gray-400 hover:text-yellow-500 hover:bg-yellow-50 rounded-full transition-colors"><Pin :size="16" /></button></el-tooltip>
                    <el-tooltip content="编辑地址" placement="top"><button @click="(e) => startEditAddress(addr, e)" class="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-full transition-colors"><Pencil :size="16" /></button></el-tooltip>
                    <el-tooltip content="删除地址" placement="top"><button @click="(e) => handleDeleteAddress(addr.id, e)" class="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-full transition-colors"><Trash2 :size="16" /></button></el-tooltip>
                  </div>
                </div>
              </template>
              <div v-if="editingId === -1" class="p-6 rounded-2xl border-2 border-dashed border-blue-500 bg-blue-50/10 dark:bg-blue-900/10 animate-fade-in">
                 <div class="flex items-center gap-2 mb-4 font-bold text-blue-600"><Plus :size="18"/> 添加新地址</div>
                 <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-position="top" size="large">
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <el-form-item label="收货人" prop="consignee"><el-input v-model="addressForm.consignee" /></el-form-item>
                      <el-form-item label="手机号" prop="consigneePhone"><el-input v-model="addressForm.consigneePhone" /></el-form-item>
                      <el-form-item label="详细地址" prop="consigneeAddress" class="md:col-span-2"><el-input v-model="addressForm.consigneeAddress" /></el-form-item>
                    </div>
                    <div class="flex justify-end gap-3 mt-4"><el-button @click="cancelEdit">取消</el-button><el-button type="primary" @click="saveAddress">保存地址</el-button></div>
                  </el-form>
              </div>
              <div v-else @click="startAddAddress" class="group p-4 rounded-xl border-2 border-dashed border-gray-300 dark:border-zinc-700 flex flex-col items-center justify-center gap-2 text-gray-500 cursor-pointer hover:border-blue-500 hover:text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/10 transition-all min-h-[100px]">
                <div class="bg-gray-100 dark:bg-zinc-800 p-2 rounded-full group-hover:bg-blue-100 dark:group-hover:bg-blue-900 transition-colors"><Plus :size="24" /></div><span class="font-medium">使用新地址</span>
              </div>
            </div>
          </div>
          <el-button type="primary" size="large" class="w-full !mt-8 !rounded-full !h-12 !text-lg !font-bold shadow-lg shadow-blue-500/30" :loading="isSubmitting" @click="handlePlaceOrder" :disabled="editingId !== null">
            {{ editingId !== null ? '请先保存地址' : '提交订单并支付' }}
          </el-button>
        </div>

        <!-- 阶段二：支付确认页 (未支付时) -->
        <div v-else-if="step === 'payment'" class="space-y-8 animate-fade-in">
           <div class="bg-white dark:bg-zinc-900 p-8 rounded-3xl border border-gray-100 dark:border-zinc-800 text-center">
            <div class="w-16 h-16 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
              <CreditCard :size="32" />
            </div>
            <h2 class="text-2xl font-bold mb-2">订单已创建</h2>
            <p class="text-gray-500 mb-6">订单号: {{ currentOrderNumber }}</p>

            <div class="bg-red-50 text-red-600 px-4 py-2 rounded-lg inline-block mb-6 font-mono font-bold text-xl">
              剩余支付时间: {{ formattedTime }}
            </div>

            <div class="p-4 bg-gray-50 dark:bg-black rounded-xl mb-4 border border-gray-200 dark:border-zinc-800 text-left">
              <div class="flex justify-between items-center">
                <span class="font-bold">支付宝支付</span>
                <span class="font-bold text-xl text-blue-600">￥{{ total.toLocaleString() }}</span>
              </div>
            </div>
            <el-button type="primary" size="large" round class="w-full !h-12 !font-bold" @click="handlePay">立即支付</el-button>
          </div>
        </div>

        <!-- 阶段三：支付中 (轮询中) -->
        <div v-else-if="step === 'checking'" class="space-y-8 animate-fade-in">
            <div class="bg-white dark:bg-zinc-900 p-12 rounded-3xl border border-gray-100 dark:border-zinc-800 text-center flex flex-col items-center">
                <Loader2 :size="48" class="animate-spin text-blue-600 mb-6" />
                <h2 class="text-2xl font-bold mb-2">正在等待支付结果...</h2>
                <p class="text-gray-500 max-w-md">已在新窗口打开支付宝页面。支付完成后，此页面将自动更新。</p>
                <div class="mt-8 flex gap-4">
                    <el-button @click="handlePay">重新打开支付页</el-button>
                    <el-button type="info" text @click="step = 'payment'">遇到问题？返回上一级</el-button>
                </div>
            </div>
        </div>

        <!-- 阶段四：支付成功 -->
        <div v-else-if="step === 'success'" class="pt-10 flex flex-col items-center justify-center text-center animate-fade-in">
            <div class="w-24 h-24 bg-green-100 text-green-600 rounded-full flex items-center justify-center mb-6 shadow-lg shadow-green-500/20">
                <CheckCircle :size="48" />
            </div>
            <h1 class="text-3xl font-bold mb-4">支付成功!</h1>
            <p class="text-gray-500 mb-8">感谢您的购买，我们将尽快发货。</p>
            <el-button type="primary" size="large" round class="!px-12 !font-bold" @click="router.push('/profile')">
                查看我的订单
            </el-button>
        </div>

      </div>

      <!-- 右侧订单摘要 (保持不变) -->
      <div>
        <div class="bg-gray-50 dark:bg-zinc-900 p-6 rounded-3xl sticky top-24">
           <!-- ...摘要部分保持不变... -->
           <h3 class="font-bold text-lg mb-4">订单摘要</h3>
           <div class="space-y-4 mb-6 max-h-60 overflow-y-auto pr-2">
            <div v-for="item in orderItems" :key="item.id" class="flex justify-between items-center text-sm">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-white dark:bg-black rounded-lg flex items-center justify-center overflow-hidden border border-gray-200 dark:border-zinc-800">
                  <img :src="item.productImage" alt="" class="w-full h-full object-contain" />
                </div>
                <div class="flex flex-col">
                  <span class="font-medium truncate max-w-[120px]">{{ item.productName }}</span>
                  <span class="text-gray-500">x{{ item.number }}</span>
                </div>
              </div>
              <span class="font-medium">￥{{ (item.productPrice * item.number).toLocaleString() }}</span>
            </div>
          </div>
          <div class="border-t border-gray-200 dark:border-zinc-700 pt-4 space-y-2">
            <div class="flex justify-between text-gray-500"><span>商品小计</span><span>￥{{ total.toLocaleString() }}</span></div>
            <div class="flex justify-between text-gray-500"><span>运费</span><span>免运费</span></div>
            <div class="flex justify-between font-bold text-xl pt-2"><span>实付款</span><span>￥{{ total.toLocaleString() }}</span></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>