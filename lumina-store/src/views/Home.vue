<script setup lang="ts">
import { ref, onMounted, onUnmounted } from "vue";
import { RouterLink } from "vue-router";
import { ArrowRight, ChevronLeft, ChevronRight } from "lucide-vue-next";
import { getNewProductApi, getNewBlogApi, getIndexSliderApi, getOLApi } from "@/api/home";
import { Product, Blog, IndexSlider, OfficialCollection } from "@/types";

const slides = ref<IndexSlider[]>([]);
const currentSlide = ref(0);
const featuredProducts = ref<Product[]>([]);
const latestBlogs = ref<Blog[]>([]);
const officialCollections = ref<OfficialCollection[]>([]);
const nextSlide = () => {
  currentSlide.value = (currentSlide.value + 1) % slides.value.length;
};

const prevSlide = () => {
  currentSlide.value =
    currentSlide.value === 0 ? slides.value.length - 1 : currentSlide.value - 1;
};

onMounted(async () => {
  // 获取最新商品
  const productRes = await getNewProductApi();
  Object.assign(featuredProducts.value, { ...productRes.data });

  // 获取最新博客
  const blogRes = await getNewBlogApi();
  Object.assign(latestBlogs.value, { ...blogRes.data });

  // 获取轮播图
  const sliderRes = await getIndexSliderApi();
  Object.assign(slides.value, { ...sliderRes.data });

  // 获取官方收藏
  const officialCollectionRes = await getOLApi();
  Object.assign(officialCollections.value, { ...officialCollectionRes.data });
});
</script>

<template>
  <div class="flex flex-col min-h-screen">
    <!-- 轮播图 -->
    <section class="relative h-[85vh] w-full bg-black text-white overflow-hidden group">
      <div
        v-for="(slide, index) in slides"
        :key="slide.id"
        :class="`absolute inset-0 transition-opacity duration-1000 ease-in-out ${
          index === currentSlide ? 'opacity-100' : 'opacity-0'
        }`"
      >
        <div class="absolute inset-0 z-0 opacity-60">
          <img :src="slide.url" :alt="slide.title" class="w-full h-full object-cover" />
        </div>
        <div
          class="relative z-10 h-full flex flex-col items-center justify-center text-center px-4 max-w-4xl mx-auto"
        >
          <h1
            class="text-5xl md:text-7xl font-bold mb-6 tracking-tighter animate-fade-in-up"
          >
            {{ slide.title }}
          </h1>
          <p
            class="text-xl md:text-2xl text-gray-200 mb-10 font-light animate-fade-in-up animation-delay-200"
          >
            {{ slide.subtitle }}
          </p>
          <div
            class="flex flex-col sm:flex-row gap-4 justify-center animate-fade-in-up animation-delay-400"
          >
            <RouterLink
              :to="slide.link"
              class="px-8 py-3 bg-white text-black rounded-full font-medium hover:bg-gray-200 transition-all transform hover:scale-105"
            >
              {{ slide.cta }}
            </RouterLink>
          </div>
        </div>
      </div>

      <!-- 切换按钮 -->
      <button
        @click="prevSlide"
        class="absolute left-4 top-1/2 -translate-y-1/2 p-2 rounded-full bg-white/10 backdrop-blur hover:bg-white/20 text-white opacity-0 group-hover:opacity-100 transition-opacity z-20"
      >
        <ChevronLeft :size="24" />
      </button>
      <button
        @click="nextSlide"
        class="absolute right-4 top-1/2 -translate-y-1/2 p-2 rounded-full bg-white/10 backdrop-blur hover:bg-white/20 text-white opacity-0 group-hover:opacity-100 transition-opacity z-20"
      >
        <ChevronRight :size="24" />
      </button>

      <!-- 下面的小圆点 -->
      <div class="absolute bottom-8 left-1/2 -translate-x-1/2 flex gap-2 z-20">
        <button
          v-for="(_, idx) in slides"
          :key="idx"
          @click="currentSlide = idx"
          :class="`w-2 h-2 rounded-full transition-all ${
            idx === currentSlide ? 'bg-white w-6' : 'bg-white/50'
          }`"
        />
      </div>
    </section>

    <!-- 最新发布商品 -->
    <section class="py-24 bg-white dark:bg-black">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-end mb-12">
          <div>
            <h2 class="text-3xl font-bold mb-2">最新发布</h2>
            <p class="text-gray-500">最新产品，新鲜设计</p>
          </div>
          <RouterLink
            to="/shop"
            class="text-blue-600 flex items-center gap-1 hover:underline"
          >
            查看所有 <ArrowRight :size="16" />
          </RouterLink>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
          <RouterLink
            v-for="product in featuredProducts"
            :to="`/product/${product.id}`"
            :key="product.id"
            class="group block"
          >
            <div
              class="aspect-square bg-gray-100 dark:bg-zinc-900 rounded-2xl overflow-hidden mb-4 relative"
            >
              <img
                :src="product.image"
                :alt="product.name"
                class="w-full h-full object-cover mix-blend-multiply dark:mix-blend-normal transition-transform duration-500 group-hover:scale-105"
              />
            </div>
            <h3 class="font-semibold truncate">{{ product.name }}</h3>
            <p class="text-gray-500 text-sm">￥{{ product.price }}</p>
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- 官方收藏 -->
    <section class="py-12 bg-gray-50 dark:bg-zinc-900">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-end mb-12">
          <div>
            <h2 class="text-3xl font-bold mb-2">官方收藏</h2>
            <p class="text-gray-500">官方挑选，值得信赖</p>
          </div>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <RouterLink
            v-for="(item, idx) in officialCollections"
            :to="`/product/${item.productId}`"
            :key="idx"
            class="group relative h-80 rounded-3xl overflow-hidden cursor-pointer"
          >
            <img
              :src="item.productImage"
              :alt="item.productName"
              class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
            />
            <div
              class="absolute inset-0 bg-black/20 group-hover:bg-black/40 transition-colors"
            />
            <div class="absolute bottom-8 left-8">
              <h3 class="text-2xl font-bold text-white mb-2">{{ item.productName }}</h3>
              <span
                class="text-white flex items-center gap-2 text-sm font-medium opacity-0 group-hover:opacity-100 transform translate-y-2 group-hover:translate-y-0 transition-all duration-300"
              >
                探索 <ArrowRight :size="16" />
              </span>
            </div>
          </RouterLink>
        </div>
      </div>
    </section>

    <!--最新博客 -->
    <section class="py-24 bg-white dark:bg-black">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-end mb-12">
          <div>
            <h2 class="text-3xl font-bold mb-2">最新博客</h2>
            <!-- <p class="text-gray-500">Insights and updates.</p> -->
          </div>
          <RouterLink
            to="/blog"
            class="text-blue-600 flex items-center gap-1 hover:underline"
          >
            阅读博客 <ArrowRight :size="16" />
          </RouterLink>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <RouterLink
            v-for="blog in latestBlogs"
            :to="`/blog/${blog.id}`"
            :key="blog.id"
            class="group"
          >
            <div class="h-84 rounded-2xl overflow-hidden mb-4">
              <img
                :src="blog.image"
                :alt="blog.title"
                class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
              />
            </div>
            <h3
              class="text-xl font-bold mt-2 mb-2 group-hover:text-blue-600 transition-colors line-clamp-2"
            >
              {{ blog.title }}
            </h3>
            <p class="text-gray-500 text-sm line-clamp-2">{{ blog.createTime }}</p>
          </RouterLink>
        </div>
      </div>
    </section>
  </div>
</template>
