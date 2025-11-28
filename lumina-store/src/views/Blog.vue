<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { RouterLink } from 'vue-router';
import { getBlogPageApi } from '@/api/blog';
import { useUserStore } from '@/stores/modules/user';
import { BlogPost } from '@/types';
import { Edit3, Loader } from 'lucide-vue-next';

const store = useUserStore();
const blogs = ref<BlogPost[]>([]);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);
const loadMoreRef = ref<HTMLElement | null>(null);
let observer: IntersectionObserver | null = null;

const fetchBlogs = async () => {
  if (loading.value) return;
  loading.value = true;
  
  try {
    const res = await getBlogPageApi({
      current: page.value,
      size: 10,
      searchTitles: [],
      authorNames: []
    });

    if (res.code === 200) {
      const newBlogs = res.data.records.map((item: any) => ({
        id: item.id,
        title: item.title,
        excerpt: item.content ? item.content.substring(0, 150) + '...' : '',
        content: item.content,
        author: item.author,
        date: item.createTime,
        image: item.image,
        tags: ['Story'], // API 无 tags，给默认值
        comments: []
      }));

      blogs.value = [...blogs.value, ...newBlogs];
      hasMore.value = page.value < res.data.pages;
      page.value++;
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchBlogs();

  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && hasMore.value) {
      fetchBlogs();
    }
  });

  if (loadMoreRef.value) {
    observer.observe(loadMoreRef.value);
  }
});

onUnmounted(() => {
  observer?.disconnect();
});
</script>

<template>
  <div class="pt-24 pb-20 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
    <div class="flex justify-between items-end mb-16">
      <div>
        <h1 class="text-5xl font-bold mb-4">Stories</h1>
        <p class="text-xl text-gray-500">Insights on design, technology, and life.</p>
      </div>
      <RouterLink v-if="store.user" to="/create-blog" class="px-6 py-3 bg-black text-white dark:bg-white dark:text-black rounded-full font-medium flex items-center gap-2 hover:opacity-80 transition-opacity">
        <Edit3 :size="18" /> Write Story
      </RouterLink>
    </div>

    <div class="grid gap-12">
      <RouterLink v-for="(blog, idx) in blogs" :to="`/blog/${blog.id}`" :key="blog.id" class="group grid grid-cols-1 md:grid-cols-2 gap-8 items-center cursor-pointer">
        <div :class="`rounded-3xl overflow-hidden h-64 md:h-80 ${idx % 2 === 1 ? 'md:order-2' : ''}`">
          <img :src="blog.image" :alt="blog.title" class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105" />
        </div>
        <div :class="idx % 2 === 1 ? 'md:order-1' : ''">
          <div class="flex items-center gap-3 text-sm text-gray-500 mb-4">
            <span class="bg-gray-100 dark:bg-zinc-900 px-3 py-1 rounded-full font-medium text-black dark:text-white">{{ blog.tags[0] }}</span>
            <span>{{ blog.date }}</span>
          </div>
          <h2 class="text-3xl font-bold mb-4 group-hover:text-blue-600 transition-colors">{{ blog.title }}</h2>
          <p class="text-gray-600 dark:text-gray-400 leading-relaxed mb-6 line-clamp-3">
            {{ blog.excerpt }}
          </p>
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 rounded-full bg-gray-200 overflow-hidden">
              <img :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${blog.author}`" :alt="blog.author" />
            </div>
            <span class="text-sm font-medium">{{ blog.author }}</span>
          </div>
        </div>
      </RouterLink>
    </div>

    <!-- Infinite Scroll Trigger -->
    <div ref="loadMoreRef" class="mt-16 text-center py-8">
      <div v-if="loading" class="flex justify-center">
        <Loader class="animate-spin text-gray-400" :size="32" />
      </div>
      <p v-if="!hasMore && blogs.length > 0" class="text-gray-400 text-sm">You've reached the end.</p>
    </div>
  </div>
</template>