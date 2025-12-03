<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from "vue";
import { RouterLink } from "vue-router";
import { getProductPageApi, addToWishListApi } from "@/api/product";
import { getProductCategoryApi } from "@/api/category";
import { getBrandListApi } from "@/api/brand";
import { useUserStore } from "@/stores/modules/user";
import { addShoppingCartApi } from "@/api/cart";
import { Product } from "@/types";
import { Search, Loader, SlidersHorizontal, Heart, ShoppingCart } from "lucide-vue-next";
import { ElMessage } from "element-plus";
import { emitter } from '@/event/emitter';
const userStore = useUserStore();

// === 数据状态 ===
const products = ref<Product[]>([]);
const page = ref(1);
const pageSize = 6;
const loading = ref(false);
const hasMore = ref(true); // 是否还有更多数据

// === 筛选状态 ===
const isMobileFilterOpen = ref(false);
const category = ref<number | null>(null);
const search = ref("");
const brands = ref<number[]>([]);
const minPrice = ref<number | undefined>(undefined);
const maxPrice = ref<number | undefined>(undefined);

// === 选项数据 ===
const categories = ref<{ id: number; name: string }[]>([]);
const brandList = ref<{ id: number; name: string }[]>([]);

// === 滚动加载相关 ===
const loadMoreRef = ref<HTMLElement | null>(null);
let observer: IntersectionObserver | null = null;
let debounceTimer: ReturnType<typeof setTimeout> | null = null;

// 获取基础数据
const fetchBasics = async () => {
  try {
    const [catRes, brandRes] = await Promise.all([
      getProductCategoryApi(),
      getBrandListApi(),
    ]);
    if (catRes.code === 200) categories.value = catRes.data;
    if (brandRes.code === 200) brandList.value = brandRes.data;
  } catch (e) {
    console.error(e);
  }
};

/**
 * 获取商品列表
 */
const fetchProducts = async (reset = false) => {
  if (loading.value) return;
  if (!reset && !hasMore.value) return;

  loading.value = true;

  const currentPage = reset ? 1 : page.value;

  // 修改这里的 params 构造逻辑
  const params = {
    pageNum: currentPage,
    pageSize: pageSize,
    // 1. searchNames: 不要包在数组里，直接传字符串，Spring 会自动处理
    searchNames: search.value ? search.value : undefined,

    // 2. category: 不要包在数组里 [category.value]，直接传值
    // Axios 会发送 category=17，Spring 能识别
    category: category.value ? category.value : undefined,

    // 3. brand: 使用 join(',') 转为逗号分隔的字符串
    // Axios 会发送 brand=8,9，Spring 也能完美识别
    // 注意：如果你的 TS 报错类型不匹配，可以在这里加 as any，或者修改 API 定义
    brand: brands.value.length > 0 ? brands.value.join(",") : undefined,

    minPrice: minPrice.value,
    maxPrice: maxPrice.value,
  };

  try {
    // 如果 TS 提示 params 类型错误，可以使用 (params as any)
    const res = await getProductPageApi(params as any);

    if (res.code === 200) {
      // ... 保持原有逻辑不变 ...
      const pageData = res.data;
      const total = pageData.total;

      const newRecords = pageData.records.map((item: any) => ({
        id: item.id,
        userId: item.userId,
        name: item.name,
        categoryId: item.categoryId,
        brandId: item.brandId,
        price: item.price,
        inventory: item.inventory ?? 0,
        image: item.image,
        detailImages: item.detailImages,
        description: item.description,
        descriptionImage: item.descriptionImage,
        label: item.label,
      }));

      if (reset) {
        products.value = newRecords;
        page.value = 2;
      } else {
        products.value = [...products.value, ...newRecords];
        page.value += 1;
      }

      if (products.value.length >= total) {
        hasMore.value = false;
      } else {
        hasMore.value = true;
      }
    }
  } catch (error) {
    console.error("Load products error:", error);
    ElMessage.error("加载失败");
    hasMore.value = false;
  } finally {
    loading.value = false;

    // ... 保持原有 nextTick 检测逻辑 ...
    if (hasMore.value) {
      await nextTick();
      const el = loadMoreRef.value;
      if (el) {
        const rect = el.getBoundingClientRect();
        if (rect.top < window.innerHeight + 50) {
          fetchProducts(false);
        }
      }
    }
  }
};
// === 监听筛选变化 ===
watch(
  [category, search, brands, minPrice, maxPrice],
  () => {
    if (debounceTimer) clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      // 筛选条件变化：强制开启 hasMore，并重置加载
      hasMore.value = true;
      fetchProducts(true);
      window.scrollTo({ top: 0, behavior: "smooth" });
    }, 1000);
  },
  { deep: true }
);

// === 购物车 & 收藏逻辑保持不变 ===
const handleAddToCart = async (e: Event, product: Product) => {
  e.preventDefault();
  e.stopPropagation();
  try {
    await addShoppingCartApi(Number(product.id));
    ElMessage.success("已加入购物车");
    emitter.emit("refresh");
  } catch (error) {}
};

const handleToggleFavorite = async (e: Event, id: number) => {
  e.preventDefault();
  e.stopPropagation();
  try {
    await addToWishListApi(id);
    ElMessage.success("已添加到收藏");
    emitter.emit("refresh");
  } catch (error) {}
};

// === 生命周期 ===
onMounted(async () => {
  await fetchBasics();

  // 首次加载
  fetchProducts(true);

  // 初始化滚动监听
  observer = new IntersectionObserver(
    (entries) => {
      const target = entries[0];
      // 当元素可见 + 没有在加载 + 还有更多数据时 -> 加载下一页
      if (target.isIntersecting && !loading.value && hasMore.value) {
        fetchProducts(false);
      }
    },
    {
      rootMargin: "100px", // 距离底部 100px 时触发
    }
  );

  if (loadMoreRef.value) {
    observer.observe(loadMoreRef.value);
  }
});

onUnmounted(() => {
  if (observer) observer.disconnect();
  if (debounceTimer) clearTimeout(debounceTimer);
});
</script>

<template>
  <div class="pt-24 pb-20 min-h-screen max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
    <!-- 头部区域 -->
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-4xl font-bold">所有商品</h1>
      <div class="md:hidden">
        <el-button :icon="SlidersHorizontal" circle @click="isMobileFilterOpen = true" />
      </div>
    </div>

    <div class="flex gap-12">
      <!-- 左侧筛选栏 (Desktop - 保持不变) -->
      <aside class="hidden md:block w-64 shrink-0 space-y-8">
        <div class="mb-8">
          <el-input
            v-model="search"
            placeholder="搜索商品..."
            :prefix-icon="Search"
            size="large"
            clearable
          />
        </div>

        <div>
          <h3 class="font-bold mb-4 text-lg">分类</h3>
          <ul class="space-y-2">
            <li>
              <button
                @click="() => { category = null; search = ''; }"
                :class="`text-sm transition-colors ${
                  category === null
                    ? 'font-bold text-black dark:text-white'
                    : 'text-gray-500 hover:text-black dark:hover:text-white'
                }`"
              >
                全部
              </button>
            </li>
            <li v-for="cat in categories" :key="cat.id">
              <button
                @click="() => { category = cat.id; search = ''; }"
                :class="`text-sm transition-colors ${
                  category === cat.id
                    ? 'font-bold text-black dark:text-white'
                    : 'text-gray-500 hover:text-black dark:hover:text-white'
                }`"
              >
                {{ cat.name }}
              </button>
            </li>
          </ul>
        </div>

        <div>
          <h3 class="font-bold mb-4 text-lg">品牌</h3>
          <el-checkbox-group v-model="brands" class="flex flex-col gap-2">
            <el-checkbox
              v-for="brand in brandList"
              :key="brand.id"
              :label="brand.name"
              :value="brand.id"
            />
          </el-checkbox-group>
        </div>

        <div>
          <h3 class="font-bold mb-4 text-lg">价格</h3>
          <div class="flex gap-2 items-center mb-4">
            <el-input
              v-model.number="minPrice"
              placeholder="最低"
              size="small"
              type="number"
            />
            <span class="text-gray-400">-</span>
            <el-input
              v-model.number="maxPrice"
              placeholder="最高"
              size="small"
              type="number"
            />
          </div>
        </div>
      </aside>

      <!-- 右侧商品列表 -->
      <div class="flex-1">
        <div
          v-if="products.length === 0 && !loading"
          class="text-center py-20 text-gray-500"
        >
          暂无商品
        </div>

        <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
          <div v-for="product in products" :key="product.id" class="group relative">
            <RouterLink :to="`/product/${product.id}`" class="block">
              <div
                class="aspect-square bg-gray-100 dark:bg-zinc-900 rounded-3xl overflow-hidden mb-4 relative"
              >
                <img
                  :src="product.image"
                  :alt="product.name"
                  class="w-full h-full object-cover mix-blend-multiply dark:mix-blend-normal transition-transform duration-500 group-hover:scale-105"
                  loading="lazy"
                />
                
                <div
                  class="absolute inset-x-0 bottom-4 flex justify-center gap-4 
                         opacity-100 translate-y-0 
                         md:opacity-0 md:translate-y-4 
                         md:group-hover:opacity-100 md:group-hover:translate-y-0 
                         transition-all duration-300 z-10"
                >
                  <el-button
                    circle
                    class="!shadow-lg hover:!scale-110 border-0"
                    @click="(e) => handleAddToCart(e, product)"
                  >
                    <ShoppingCart :size="18" />
                  </el-button>
                  <el-button
                    circle
                    class="!shadow-lg hover:!scale-110 border-0"
                    @click="(e) => handleToggleFavorite(e, Number(product.id))"
                  >
                    <Heart :size="18" />
                  </el-button>
                </div>
              </div>
              <div>
                <h3 class="text-lg font-semibold truncate">{{ product.name }}</h3>
                <p class="font-medium">￥{{ product.price }}</p>
              </div>
            </RouterLink>
          </div>
        </div>

        <!-- 触底加载区域 -->
        <div
          ref="loadMoreRef"
          class="mt-8 text-center py-4 h-16 flex items-center justify-center"
        >
          <div v-if="loading" class="flex items-center gap-2 text-gray-400">
            <Loader class="animate-spin" :size="20" />
            <span class="text-sm">加载中...</span>
          </div>
          <div v-else-if="!hasMore && products.length > 0" class="text-gray-400 text-sm">
            —— 已经到底啦 (共{{ products.length }}件) ——
          </div>
        </div>
      </div>
    </div>

    <!-- 移动端筛选 Drawer (保持不变) -->
    <el-drawer
      v-model="isMobileFilterOpen"
      title="筛选"
      direction="rtl"
      size="85%"
      class="dark:bg-zinc-900"
    >
      <div class="p-4 space-y-8 h-full overflow-y-auto pb-20">
        <!-- 搜索 -->
        <div>
          <el-input 
            v-model="search" 
            placeholder="搜索商品..." 
            :prefix-icon="Search" 
            size="large"
            clearable
          />
        </div>

        <!-- 分类 -->
        <div>
          <h3 class="font-bold mb-4 text-lg">分类</h3>
          <div class="flex flex-col gap-2">
            <button
              @click="() => { category = null; search = ''; isMobileFilterOpen = false; }"
              :class="`text-left px-3 py-3 rounded-xl transition-all ${
                category === null
                  ? 'bg-black text-white dark:bg-white dark:text-black font-bold shadow-md'
                  : 'bg-gray-50 dark:bg-zinc-800 text-gray-600 dark:text-gray-300'
              }`"
            >
              全部
            </button>
            <button
              v-for="cat in categories"
              :key="cat.id"
              @click="() => { category = cat.id; search = ''; isMobileFilterOpen = false; }"
              :class="`text-left px-3 py-3 rounded-xl transition-all ${
                category === cat.id
                  ? 'bg-black text-white dark:bg-white dark:text-black font-bold shadow-md'
                  : 'bg-gray-50 dark:bg-zinc-800 text-gray-600 dark:text-gray-300'
              }`"
            >
              {{ cat.name }}
            </button>
          </div>
        </div>

        <!-- 品牌 -->
        <div>
          <h3 class="font-bold mb-4 text-lg">品牌</h3>
          <el-checkbox-group v-model="brands" class="flex flex-col gap-3">
            <el-checkbox
              v-for="brand in brandList"
              :key="brand.id"
              :label="brand.name"
              :value="brand.id"
              size="large"
              border
              class="!mr-0 w-full"
            />
          </el-checkbox-group>
        </div>

        <!-- 价格 -->
        <div>
          <h3 class="font-bold mb-4 text-lg">价格区间</h3>
          <div class="flex gap-2 items-center">
            <el-input
              v-model.number="minPrice"
              placeholder="最低价"
              type="number"
              size="large"
            />
            <span class="text-gray-400 font-bold">-</span>
            <el-input
              v-model.number="maxPrice"
              placeholder="最高价"
              type="number"
              size="large"
            />
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>