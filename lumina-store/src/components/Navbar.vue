<script setup lang="ts">
import { ref, computed, watch,onMounted,onUnmounted } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { ShoppingBag, User as UserIcon, Sun, Moon, Menu, X, Heart, Trash2,ShoppingCart } from 'lucide-vue-next';
import { useUserStore } from "@/stores/modules/user";
import {toggleTheme,themeStore} from '@/stores/modules/theme';
import { emitter } from '@/event/emitter';
import { getCartApi } from '@/api/cart';
import { getFavoritesApi } from '@/api/favorites';
import {Cart,Favorites} from '@/types';
import router from "@/router";
import { getToken } from '@/utils/auth';
const userStore = useUserStore();
const route = useRoute();
const isMenuOpen = ref(false);
const shoppingCart = ref<Cart[]>([]);
const cartTotal=computed(() => shoppingCart.value.reduce((acc, item) => acc + item.productPrice * item.number, 0));
const favorites=ref<Favorites[]>([]);

const isActive = (path: string) => route.path === path ? 'text-black dark:text-white' : 'text-gray-500 hover:text-black dark:text-gray-400 dark:hover:text-white';


const getCart = async () => {
  const data = await getCartApi();
  shoppingCart.value = data.data;
};

const getFavorites = async () => {
  const data = await getFavoritesApi();
  favorites.value = data.data;
};


const handleToggleFavorite = (id: number) => {

};

const handleRemoveFromCart = (id: number) => {

};

onMounted(() => {
  if(getToken()){
        getCart();
        getFavorites();
        userStore.getUserInfo();
  }else{
    router.push('/login');
  }

  emitter.on('refresh', () => {
    getCart();
    getFavorites();
    userStore.getUserInfo();
  });
});

onUnmounted(() => {
  emitter.off('refresh');
});
</script>

<template>
  <nav class="fixed top-0 w-full z-50 bg-white/80 dark:bg-black/80 backdrop-blur-md border-b border-gray-200 dark:border-zinc-800 transition-colors duration-300">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16 items-center">
        <!-- Logo -->
        <RouterLink to="/" class="text-xl font-bold tracking-tight flex items-center gap-2">
          <!-- <div class="w-6 h-6 bg-black dark:bg-white rounded-full"></div> -->
          <img class="w-20 h-6" src="/src/assets/images/logo-header.jpg"></img>
        </RouterLink>

        <!-- 导航 -->
        <div class="hidden md:flex items-center space-x-8">
          <RouterLink to="/" :class="`text-sm font-medium transition-colors ${isActive('/')}`">首页</RouterLink>
          <RouterLink to="/shop" :class="`text-sm font-medium transition-colors ${isActive('/shop')}`">商品</RouterLink>
          <RouterLink to="/blog" :class="`text-sm font-medium transition-colors ${isActive('/blog')}`">博客</RouterLink>
          <RouterLink to="/cart" :class="`text-sm font-medium transition-colors ${isActive('/cart')}`">购物车</RouterLink>
        </div>

        <!-- Icons -->
        <div class="flex items-center space-x-6">
          <button @click="toggleTheme" class="text-gray-500 hover:text-black dark:text-gray-400 dark:hover:text-white transition-colors">
            <Moon v-if="themeStore.theme === 'light'" :size="20" />
            <Sun v-else :size="20" />
          </button>

          <!-- 收藏夹 -->
          <div class="group relative h-16 flex items-center">
            <RouterLink to="/profile" class="relative text-gray-500 hover:text-black dark:text-gray-400 dark:hover:text-white transition-colors">
              <Heart :size="20" :fill="favorites.length > 0 ? 'currentColor' : 'none'" :class="favorites.length > 0 ? 'text-red-500' : ''" />
              <span v-if="favorites.length > 0" class="absolute -top-2 -right-2 bg-red-500 text-white text-[10px] font-bold w-4 h-4 rounded-full flex items-center justify-center">
                {{ favorites.length }}
              </span>
            </RouterLink>
            
            <!-- Popover -->
            <div class="hidden group-hover:block absolute top-16 right-0 w-80 bg-white dark:bg-zinc-900 shadow-xl border border-gray-100 dark:border-zinc-800 rounded-2xl p-4 animate-fade-in z-50">
              <h4 class="font-bold mb-3 pb-2 border-b border-gray-100 dark:border-zinc-800">收藏夹</h4>
              <p v-if="favorites.length === 0" class="text-sm text-gray-500 text-center py-4">还没有收藏内容</p>
              <div v-else class="max-h-64 overflow-y-auto space-y-3">
                <div v-for="p in favorites" :key="p.id" class="flex gap-3 items-center">
                  <img :src="p.productImage" :alt="p.productName" class="w-12 h-12 object-cover rounded-md bg-gray-50 dark:bg-black" />
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium truncate">{{ p.productName }}</p>
                    <p class="text-xs text-gray-500">${{ p.productPrice }}</p>
                  </div>
                  <button @click="(e) => handleToggleFavorite(p.id)" class="text-gray-400 hover:text-red-500">
                    <X :size="14" />
                  </button>
                </div>
                <RouterLink to="/profile" class="block text-center text-xs text-blue-600 font-medium mt-2 hover:underline">查看所有</RouterLink>
              </div>
            </div>
          </div>
          
          <!-- 购物车 -->
          <div class="group relative h-16 flex items-center">
            <RouterLink to="/cart" class="relative text-gray-500 hover:text-black dark:text-gray-400 dark:hover:text-white transition-colors">
              <ShoppingCart :size="20" />
              <span v-if="shoppingCart.length > 0" class="absolute -top-2 -right-2 bg-blue-600 text-white text-[10px] font-bold w-4 h-4 rounded-full flex items-center justify-center">
                {{ shoppingCart.length }}
              </span>
            </RouterLink>

              <!-- Popover -->
              <div class="hidden group-hover:block absolute top-16 right-0 w-80 bg-white dark:bg-zinc-900 shadow-xl border border-gray-100 dark:border-zinc-800 rounded-2xl p-4 animate-fade-in z-50">
              <h4 class="font-bold mb-3 pb-2 border-b border-gray-100 dark:border-zinc-800">购物车 ({{  shoppingCart.length }})</h4>
              <p v-if="shoppingCart.length === 0" class="text-sm text-gray-500 text-center py-4">购物车为空</p>
              <div v-else>
                <div class="max-h-64 overflow-y-auto space-y-3">
                  <div v-for="item in shoppingCart" :key="item.id" class="flex gap-3 items-center">
                    <img :src="item.productImage" :alt="item.productName" class="w-12 h-12 object-cover rounded-md bg-gray-50 dark:bg-black" />
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium truncate">{{ item.productName }}</p>
                      <div class="flex justify-between items-center text-xs text-gray-500">
                        <span>数量: {{ item.number }}</span>
                        <span>￥{{ item.productPrice * item.number }}</span>
                      </div>
                    </div>
                    <button @click="(e) => handleRemoveFromCart(item.id)" class="text-gray-400 hover:text-red-500">
                      <Trash2 :size="14" />
                    </button>
                  </div>
                </div>
                <div class="pt-3 border-t border-gray-100 dark:border-zinc-800 mt-2">
                  <div class="flex justify-between font-bold text-sm mb-3">
                    <span>总计:</span>
                    <span>￥{{  cartTotal}}</span>
                  </div>
                  <RouterLink to="/cart" class="block w-full bg-black text-white dark:bg-white dark:text-black text-center py-2 rounded-full text-sm font-medium hover:opacity-90">
                   查看购物车
                  </RouterLink>
                </div>
              </div>
            </div>
          </div>

          <RouterLink :to="userStore.user ? '/profile' : '/login'" class="hidden md:block">
            <img v-if="userStore.user" :src="userStore.user.avatar || ''"  class="w-8 h-8 rounded-full border border-gray-200 dark:border-gray-700" />
            <UserIcon v-else :size="20" class="text-gray-500 hover:text-black dark:text-gray-400 dark:hover:text-white" />
          </RouterLink>

          <!-- Mobile Menu Button -->
          <button class="md:hidden text-gray-500" @click="isMenuOpen = !isMenuOpen">
            <X v-if="isMenuOpen" :size="24" />
            <Menu v-else :size="24" />
          </button>
        </div>
      </div>
    </div>

    <!-- Mobile Menu -->
    <div v-if="isMenuOpen" class="md:hidden bg-white dark:bg-black border-b border-gray-200 dark:border-zinc-800 animate-fade-in">
      <div class="px-2 pt-2 pb-3 space-y-1 sm:px-3">
        <RouterLink to="/" class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-zinc-900">首页</RouterLink>
        <RouterLink to="/shop" class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-zinc-900">商品</RouterLink>
        <RouterLink to="/blog" class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-zinc-900">博客</RouterLink>
        <RouterLink to="/cart" class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-zinc-900">购物车</RouterLink>
        <RouterLink to="/profile" class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-zinc-900">账户</RouterLink>
      </div>
    </div>
  </nav>
</template>