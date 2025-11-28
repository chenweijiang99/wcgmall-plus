<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
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
} from "lucide-vue-next";
import { Cart, Favorites, Order, Address } from "@/types";
import { useUserStore } from "@/stores/modules/user";
import { getFavoritesApi } from "@/api/favorites";
import { uploadApi } from "@/api/file";
import { updateUserApi, getEmailCodeApi } from "@/api/auth";
import {
  getAddtessListApi,
  addAddressApi,
  updateAddressApi,
  deleteAddressApi,
} from "@/api/address";

const userStore = useUserStore();
const router = useRouter();
const isEditing = ref(false);
const activeTab = ref<"orders" | "favorites" | "addresses">("orders");
const expandedOrder = ref<number | null>(null);
const shoppingCart = ref<Cart[]>([]); // 注意：你的代码里没用到 getCartApi，如果不展示可以删掉
const favorites = ref<Favorites[]>([]);
const orders = ref<Order[]>([]);
const addresses = ref<Address[]>([]);
const showAddressForm = ref(false);
const addressForm = ref<any>({});
const addressFormRef = ref();

// 编辑表单数据
const editForm = ref({
  nickname: "",
  email: "",
  mobile: "",
  avatar: "",
  signature: "",
  sex: undefined,
});

// === 验证码相关 ===
const emailCode = ref("");
const countdown = ref(0);
let timer: ReturnType<typeof setInterval> | null = null;

// 判断邮箱是否改变
const isEmailChanged = computed(() => {
  return userStore.user && editForm.value.email !== userStore.user.email;
});

const getFavorites = async () => {
  const data = await getFavoritesApi();
  favorites.value = data.data;
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

const toggleOrderExpand = (id: number) => {
  expandedOrder.value = expandedOrder.value === id ? null : id;
};

onMounted(() => {
  getFavorites();
  getAddresses();
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
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
          <div>
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
          <p class="text-gray-500 mb-1">用户名：{{ userStore.user.username }}</p>
          <p class="text-gray-500 text-sm mb-1">
            性别：{{ userStore.user.sex === 1 ? "男" : "女" }}
          </p>
          <p class="text-gray-500 text-sm mb-1">
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
        <!-- ... -->
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
              <!-- <el-button circle class="absolute top-2 right-2 !p-2">
                        <X :size="16" class="text-red-500" />
                    </el-button> -->
            </div>
            <RouterLink :to="`/product/${product.id}`">
              <h3 class="font-semibold truncate">{{ product.productName }}</h3>
              <div class="flex justify-between items-center">
                <span class="font-bold">${{ product.productPrice }}</span>
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
  </div>
</template>
