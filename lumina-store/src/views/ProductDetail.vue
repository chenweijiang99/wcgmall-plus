<script setup lang="ts">
import { ref, watch, computed, reactive } from "vue";
import { useRoute } from "vue-router";
import { getProductDetailApi, addToWishListApi } from "@/api/product";
import { selectTreeApi, addCommentsApi } from "@/api/comments";
import { addShoppingCartApi } from "@/api/cart";
import { useUserStore } from "@/stores/modules/user";
import { Product, Comment, ProductView } from "@/types";
import {
  Heart,
  ShoppingBag,
  ChevronRight,
  PackageCheck,
  AlertCircle,
} from "lucide-vue-next";
import Comments from "@/components/comments/index.vue";
import { ElMessage } from "element-plus";
import { emitter } from "@/event/emitter";

// === Swiper 相关引入 ===
import { Swiper, SwiperSlide } from "swiper/vue";
import { Autoplay, Navigation } from "swiper/modules";
import "swiper/css";
import "swiper/css/navigation";

const route = useRoute();
const store = useUserStore();
const product = ref<ProductView | undefined>(undefined);
const activeImage = ref("");
const loading = ref(true);
const comments = ref<Comment[]>([]);
// === 放大镜相关逻辑 ===
const isZoomed = ref(false);
const zoomPosition = reactive({ x: 0, y: 0 });
const mainImageRef = ref<HTMLElement | null>(null);

const handleMouseMove = (e: MouseEvent) => {
  if (!mainImageRef.value) return;

  const { left, top, width, height } = mainImageRef.value.getBoundingClientRect();

  // 计算鼠标在图片内的相对坐标 (0-100%)
  const x = ((e.clientX - left) / width) * 100;
  const y = ((e.clientY - top) / height) * 100;

  // 限制范围在 0-100 之间
  zoomPosition.x = Math.max(0, Math.min(100, x));
  zoomPosition.y = Math.max(0, Math.min(100, y));
};

// === Swiper 配置 ===
const swiperModules = [Autoplay, Navigation];

const fetchProduct = async () => {
  const id = route.params.id as string;
  if (id) {
    loading.value = true;
    try {
      const res = await getProductDetailApi(Number(id));
      if (res.code === 200) {
        const data = res.data;

        // 解析 DetailImages
        let parsedDetailImages: string[] = [];
        try {
          if (data.detailImages?.startsWith("[")) {
            parsedDetailImages = JSON.parse(data.detailImages);
          } else if (data.detailImages) {
            parsedDetailImages = data.detailImages.split(",");
          } else {
            parsedDetailImages = [data.image];
          }
        } catch (e) {
          parsedDetailImages = [data.image];
        }

        // 解析 DescriptionImages
        let parsedDescImages: string[] = [];
        try {
          if (data.descriptionImage?.startsWith("[")) {
            parsedDescImages = JSON.parse(data.descriptionImage);
          } else if (data.descriptionImage) {
            parsedDescImages = data.descriptionImage.split(",");
          }
        } catch (e) {}

        product.value = {
          ...data,
          detailImages: parsedDetailImages,
          descriptionImage: parsedDescImages,
          comments: [],
        };

        // 初始化主图
        if (product.value) activeImage.value = product.value.image;
        loadComments(id);
      }
    } catch (error) {
      console.error(error);
    } finally {
      loading.value = false;
    }
  }
};

const addToCart = async (p: Product) => {
  await addShoppingCartApi(Number(p.id));
  ElMessage.success("添加到购物车成功");
  emitter.emit("refresh");
};
const toggleFavorite = async (id: number) => {
  await addToWishListApi(Number(id));
  ElMessage.success("添加到收藏成功");
  emitter.emit("refresh");
};
const handleAddComment = async (text: string) => {
  if (!product.value || !store.user) {
    ElMessage.warning("请登录");
    return;
  }
  const res = await addCommentsApi({
    fid: Number(product.value.id),
    module: "product",
    comment: text,
    pid: 0, // 根评论
  });
  ElMessage.success("评论成功");
  loadComments(product.value.id);
};
const loadComments = async (fid: Number) => {
  const res = await selectTreeApi({ fid: Number(fid), module: "product" });
  if (product.value) {
    product.value.comments = res.data.map((c: any) => ({
      id: c.id.toString(),
      userId: c.userId.toString(),
      userName: c.username,
      userAvatar: c.userAvatar || "",
      content: c.comment,
      date: c.createTime,
    }));
  }
};

watch(() => route.params.id, fetchProduct, { immediate: true });

// 库存状态辅助函数
const getInventoryStatus = (inventory: number) => {
  if (inventory > 10)
    return { text: "库存充足", class: "text-green-600", icon: PackageCheck };
  if (inventory > 0)
    return { text: `仅剩 ${inventory} 件`, class: "text-orange-500", icon: AlertCircle };
  return { text: "暂时缺货", class: "text-red-500", icon: AlertCircle };
};
</script>

<template>
  <div v-if="loading" class="pt-32 text-center h-[60vh] flex items-center justify-center">
    <div class="animate-pulse text-gray-400">Loading product details...</div>
  </div>

  <div
    v-else-if="!product"
    class="pt-32 text-center h-[60vh] flex flex-col items-center justify-center"
  >
    <h2 class="text-2xl font-bold mb-2">Product Not Found</h2>
  </div>

  <div v-else class="pt-24 pb-20 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16 mb-20">
      <!-- === 左侧：图片画廊 === -->
      <!-- 
           修改说明：
           1. relative: 移动端默认相对定位，随页面滚动
           2. lg:sticky: 桌面端开启粘性定位
           3. lg:top-24: 桌面端距离顶部距离
           4. lg:self-start: 桌面端自身对齐
      -->
      <div
        class="flex flex-col gap-4 relative lg:sticky lg:top-24 lg:self-start select-none"
      >
        <!-- 1. 主图 + 放大镜容器 (保持不变) -->
        <div
          ref="mainImageRef"
          class="relative bg-white dark:bg-zinc-900 rounded-3xl overflow-hidden flex items-center justify-center aspect-square border border-gray-100 dark:border-zinc-800 cursor-crosshair group"
          @mouseenter="isZoomed = true"
          @mouseleave="isZoomed = false"
          @mousemove="handleMouseMove"
        >
          <!-- ... 图片内容不变 ... -->
          <img
            v-show="!isZoomed"
            :src="activeImage"
            :alt="product.name"
            class="max-w-full max-h-full object-contain mix-blend-multiply dark:mix-blend-normal drop-shadow-xl transition-opacity duration-200"
          />
          <div
            v-show="isZoomed"
            class="absolute inset-0 w-full h-full bg-no-repeat bg-white dark:bg-zinc-900"
            :style="{
              backgroundImage: `url(${activeImage})`,
              backgroundPosition: `${zoomPosition.x}% ${zoomPosition.y}%`,
              backgroundSize: '200%',
            }"
          ></div>
        </div>

        <!-- 2. 缩略图轮播 (保持不变) -->
        <div
          v-if="product.detailImages && product.detailImages.length > 0"
          class="w-full"
        >
          <!-- ... Swiper 代码不变 ... -->
          <Swiper
            :modules="swiperModules"
            :slides-per-view="4"
            :space-between="12"
            :loop="product.detailImages.length > 4"
            :autoplay="{
              delay: 3000,
              disableOnInteraction: false,
              pauseOnMouseEnter: true,
            }"
            class="w-full px-1"
          >
            <SwiperSlide v-for="(img, idx) in product.detailImages" :key="idx">
              <button
                @click="activeImage = img"
                :class="`w-full aspect-square bg-gray-50 dark:bg-zinc-900 rounded-xl p-2 flex items-center justify-center border-2 transition-all cursor-pointer overflow-hidden ${
                  activeImage === img
                    ? 'border-black dark:border-white ring-1 ring-black dark:ring-white'
                    : 'border-transparent hover:border-gray-200 dark:hover:border-zinc-700'
                }`"
              >
                <img
                  :src="img"
                  class="max-w-full max-h-full object-contain mix-blend-multiply dark:mix-blend-normal"
                  loading="lazy"
                />
              </button>
            </SwiperSlide>
          </Swiper>
        </div>
      </div>

      <!-- 右侧：商品信息 (保持不变) -->
      <div class="flex flex-col justify-center py-4">
        <div class="flex items-center gap-2 text-sm text-gray-500 font-medium mb-4">
          <span>{{ product.brandName }}</span> <ChevronRight :size="14" />
          <span> {{ product.categoryName }}</span>
        </div>

        <h1
          class="text-4xl md:text-5xl font-bold mb-4 tracking-tight text-slate-900 dark:text-white leading-tight"
        >
          {{ product.name }}
        </h1>

        <!-- ... 其余右侧代码保持不变 ... -->
        <div class="flex items-center gap-4 mb-6">
          <p class="text-3xl font-bold text-slate-900 dark:text-white">
            ￥{{ product.price.toLocaleString() }}
          </p>
          <span
            v-if="product.label"
            class="px-3 py-1 bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400 text-xs font-bold uppercase rounded-full tracking-wide"
          >
            {{ product.label }}
          </span>
        </div>

        <div
          :class="`flex items-center gap-2 mb-8 text-sm font-medium ${
            getInventoryStatus(product.inventory).class
          }`"
        >
          <component :is="getInventoryStatus(product.inventory).icon" :size="18" />
          {{ getInventoryStatus(product.inventory).text }}
        </div>

        <div
          class="space-y-6 mb-10 border-t border-b border-gray-100 dark:border-zinc-800 py-8"
        >
          <p class="text-lg leading-relaxed text-gray-600 dark:text-gray-300">
            {{ product.description }}
          </p>
        </div>

        <div class="flex gap-4">
          <button
            @click="addToCart(product!)"
            :disabled="product.inventory <= 0"
            :class="`flex-1 px-8 py-4 rounded-full font-bold text-lg transition-all flex items-center justify-center gap-3 shadow-lg 
              ${
                product.inventory > 0
                  ? 'bg-black hover:bg-gray-800 text-white dark:bg-white dark:hover:bg-gray-200 dark:text-black shadow-black/20 dark:shadow-white/10'
                  : 'bg-gray-200 text-gray-400 cursor-not-allowed'
              }`"
          >
            <ShoppingBag :size="20" />
            {{ product.inventory > 0 ? "加入购物车" : "缺货" }}
          </button>

          <button
            @click="toggleFavorite(product.id)"
            class="px-6 py-4 rounded-full border border-gray-200 dark:border-zinc-800 hover:bg-gray-50 dark:hover:bg-zinc-800 transition-all group"
            title="加入收藏"
          >
            <Heart
              :size="24"
              class="text-gray-900 dark:text-white group-hover:scale-110 transition-transform"
            />
          </button>
        </div>
      </div>
    </div>

    <!-- 商品详情图 -->
    <div
      v-if="product.descriptionImage && product.descriptionImage.length > 0"
      class="mb-20 animate-fade-in"
    >
      <h2 class="text-2xl font-bold mb-8 text-center">商品详情</h2>
      <div
        class="flex flex-col gap-0 rounded-3xl overflow-hidden shadow-sm border border-gray-100 dark:border-zinc-800 bg-white dark:bg-black max-w-4xl mx-auto"
      >
        <img
          v-for="(img, index) in product.descriptionImage"
          :key="`desc-${index}`"
          :src="img"
          class="w-full h-auto object-cover block"
          alt="Detail"
          loading="lazy"
        />
      </div>
    </div>

    <Comments :comments="product.comments" @add-comment="handleAddComment" />
  </div>
</template>
