<script setup lang="ts">
import { computed } from 'vue';
import { Minus, Plus, Trash2, ArrowRight } from 'lucide-vue-next';
import { RouterLink, useRouter } from 'vue-router';
import { useUserStore } from "@/stores/modules/user";
const userStore = useUserStore();

const router = useRouter();
const total = computed(() => userStore.cart.reduce((sum, item) => sum + (item.productPrice * item.number), 0));

const updateQuantity = async (id: number, quantity: number) => {
};
const removeFromCart = async (id: number) => {

};



</script>

<template>
  <div v-if="userStore.cart.length === 0" class="pt-32 pb-20 text-center min-h-[60vh] flex flex-col items-center justify-center">
    <h2 class="text-3xl font-bold mb-4">你的购物车是空的</h2>
    <p class="text-gray-500 mb-8">Find something you love to fill it up.</p>
    <RouterLink to="/shop" class="px-8 py-3 bg-black text-white dark:bg-white dark:text-black rounded-full font-medium">
      Continue Shopping
    </RouterLink>
  </div>

  <div v-else class="pt-24 pb-20 max-w-4xl mx-auto px-4 sm:px-6">
    <h1 class="text-3xl font-bold mb-12">Review your bag.</h1>
    
    <div class="space-y-8 mb-12">
      <div v-for="item in userStore.cart" :key="item.id" class="flex flex-col sm:flex-row items-center gap-6 p-6 bg-white dark:bg-zinc-900 rounded-2xl border border-gray-100 dark:border-zinc-800">
        <div class="w-24 h-24 bg-gray-50 dark:bg-black rounded-xl flex items-center justify-center p-2">
          <img :src="item.productImage" :alt="item.productName" class="max-w-full max-h-full object-contain" />
        </div>
        
        <div class="flex-1 text-center sm:text-left">
          <h3 class="text-xl font-semibold">{{ item.productName }}</h3>
          <p class="text-gray-500 text-sm">{{ item.number }}</p>
        </div>

        <div class="flex items-center gap-4">
          <div class="flex items-center gap-3 bg-gray-100 dark:bg-black rounded-full px-3 py-1">
            <button @click="updateQuantity(item.id, -1)" class="p-1 hover:text-blue-600"><Minus :size="16" /></button>
            <span class="font-medium w-4 text-center">{{ item.number }}</span>
            <button @click="updateQuantity(item.id, 1)" class="p-1 hover:text-blue-600"><Plus :size="16" /></button>
          </div>
          <button @click="removeFromCart(item.id)" class="text-gray-400 hover:text-red-500">
            <Trash2 :size="20" />
          </button>
        </div>

        <div class="font-bold text-lg min-w-[80px] text-right">
          ￥{{ item.productPrice * item.number }}
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