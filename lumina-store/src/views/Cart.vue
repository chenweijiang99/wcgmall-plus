<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { Minus, Plus, Trash2, ArrowRight } from "lucide-vue-next";
import { RouterLink, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus"; // 引入 Element Plus 反馈组件

// 引入 API
import {
  getCartApi,
  addProductApi,
  reduceProductApi,
  deleteShoppingCartApi,
} from "@/api/cart";

import { useUserStore } from "@/stores/modules/user";
import { Cart } from "@/types";
import { emitter } from "@/event/emitter";
const userStore = useUserStore();
const router = useRouter();
const cart = ref<Cart[]>([]);

// 计算总价
const total = computed(() => {
  return cart.value.reduce((acc, item) => {
    return acc + item.productPrice * item.number;
  }, 0);
});

// 获取购物车列表的方法（封装以便复用）
const initCart = async () => {
  try {
    const res = await getCartApi();
    cart.value = res.data;
  } catch (error) {
    console.error("获取购物车失败", error);
  }
};

/**
 * 更新商品数量
 * @param item 购物车项对象
 * @param delta 变化量 (1 或 -1)
 */
const updateQuantity = async (item: Cart, delta: number) => {
  // 注意：这里优先使用 productId，如果你的接口返回中 id 就是 productId，则使用 id
  // @ts-ignore: 忽略类型检查，防止 Cart 类型未定义 productId
  const pId = item.productId || item.id;

  try {
    if (delta > 0) {
      // 增加数量
      await addProductApi(pId);
      emitter.emit("refresh");
      await initCart();
      ElMessage.success("增加成功");
    } else {
      // 减少数量
      if (item.number <= 1) {
        // 如果数量已经是1，点击减少则询问是否删除
        await removeFromCart(item);
      } else {
        await reduceProductApi(pId);
        emitter.emit("refresh");
        await initCart();
        ElMessage.success("减少成功");
      }
    }
  } catch (error) {
    console.error("更新数量失败", error);
  }
};

/**
 * 从购物车移除商品
 * @param item 购物车项对象
 */
const removeFromCart = async (item: Cart) => {
  // @ts-ignore
  const pId = item.productId || item.id;

  try {
    // 弹出确认框
    await ElMessageBox.confirm(
      `确定要将 "${item.productName}" 从购物车移除吗？`,
      "提示",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }
    );

    // 确认后调用 API
    await deleteShoppingCartApi(pId);
    emitter.emit("refresh");
    ElMessage.success("删除成功");
    await initCart();
  } catch (error) {
    // 用户取消或 API 报错，不做处理
    if (error !== "cancel") {
      console.error(error);
    }
  }
};

// 页面加载时获取数据
onMounted(() => {
  initCart();
});
</script>

<template>
  <div
    v-if="cart.length === 0"
    class="pt-32 pb-20 text-center min-h-[60vh] flex flex-col items-center justify-center"
  >
    <h2 class="text-3xl font-bold mb-4">你的购物车是空的</h2>
    <p class="text-gray-500 mb-8">找一些你喜欢的东西来填满它</p>
    <RouterLink
      to="/shop"
      class="px-8 py-3 bg-black text-white dark:bg-white dark:text-black rounded-full font-medium"
    >
      购物
    </RouterLink>
  </div>

  <div v-else class="pt-24 pb-20 max-w-4xl mx-auto px-4 sm:px-6">
    <h1 class="text-3xl font-bold mb-12">购物车</h1>

    <div class="space-y-8 mb-12">
      <div
        v-for="item in cart"
        :key="item.id"
        class="flex flex-col sm:flex-row items-center gap-6 p-6 bg-white dark:bg-zinc-900 rounded-2xl border border-gray-100 dark:border-zinc-800"
      >
        <div
          class="w-24 h-24 bg-gray-50 dark:bg-black rounded-xl flex items-center justify-center p-2"
        >
          <img
            :src="item.productImage"
            :alt="item.productName"
            class="max-w-full max-h-full object-contain"
          />
        </div>

        <div class="flex-1 text-center sm:text-left">
          <h3 class="text-xl font-semibold">{{ item.productName }}</h3>
          <p class="text-gray-500 text-sm">数量: {{ item.number }}</p>
        </div>

        <div class="flex items-center gap-4">
          <div
            class="flex items-center gap-3 bg-gray-100 dark:bg-black rounded-full px-3 py-1"
          >
            <!-- 减少按钮：传入整个 item 对象和 -1 -->
            <button @click="updateQuantity(item, -1)" class="p-1 hover:text-blue-600">
              <Minus :size="16" />
            </button>

            <span class="font-medium w-4 text-center">{{ item.number }}</span>

            <!-- 增加按钮：传入整个 item 对象和 1 -->
            <button @click="updateQuantity(item, 1)" class="p-1 hover:text-blue-600">
              <Plus :size="16" />
            </button>
          </div>

          <!-- 删除按钮：传入整个 item 对象 -->
          <button @click="removeFromCart(item)" class="text-gray-400 hover:text-red-500">
            <Trash2 :size="20" />
          </button>
        </div>

        <div class="font-bold text-lg min-w-[80px] text-right">
          ￥{{ (item.productPrice * item.number).toLocaleString() }}
        </div>
      </div>
    </div>

    <div class="border-t border-gray-200 dark:border-zinc-800 pt-8">
      <div class="flex justify-between items-center mb-8">
        <span class="text-gray-500">总计</span>
        <span class="text-3xl font-bold">￥{{ total.toLocaleString() }}</span>
      </div>
      <div class="flex justify-end">
        <el-button
          type="primary"
          @click="router.push('/checkout')"
          class="!rounded-full !px-8 !text-base !font-bold"
        >
          结算 <ArrowRight :size="20" />
        </el-button>
      </div>
    </div>
  </div>
</template>
