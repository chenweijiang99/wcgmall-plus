<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { getBlogDetailApi } from '@/api/blog'; 
import { useUserStore } from '@/stores/modules/user';
import { themeStore } from '@/stores/modules/theme';
import { Blog } from '@/types';
import Comments from '@/components/comments/index.vue';
import { 
  Clock, Calendar, User, ThumbsUp, Share2, MessageSquare, 
  ChevronLeft, List 
} from 'lucide-vue-next';
import { ElMessage } from 'element-plus';

// === Markdown 预览组件 ===
import { MdPreview, MdCatalog } from 'md-editor-v3';
import 'md-editor-v3/lib/preview.css';

const route = useRoute();
const store = useUserStore();

const post = ref<Blog | undefined>(undefined);
const loading = ref(false);
const scrollElement = document.documentElement; // 目录滚动监听对象
const catalogList = ref([]); // 目录数据

// 计算阅读时间 (简单估算: 300字/分钟)
const readTime = computed(() => {
  if (!post.value?.contentMd) return 0;
  return Math.ceil(post.value.contentMd.length / 300);
});

const fetchBlog = async (id: string) => {
  loading.value = true;
  try {
    const res = await getBlogDetailApi(Number(id));
    if (res.code === 200) {
      post.value = res.data;
      // 确保 contentMd 有值，如果没有则回退到 content
      if (!post.value.contentMd && post.value.content) {
        post.value.contentMd = post.value.content; 
      }
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 监听路由变化
watch(() => route.params.id, (id) => {
  if (id) fetchBlog(id as string);
}, { immediate: true });

// 滚动到评论区
const scrollToComments = () => {
  document.getElementById('comments-section')?.scrollIntoView({ behavior: 'smooth' });
};

// 点赞 
const handleLike = () => {
  ElMessage.success('感谢点赞，还未实现!');
};

// 分享 
const handleShare = () => {
  navigator.clipboard.writeText(window.location.href);
  ElMessage.success('链接已复制');
};

</script>

<template>
  <div class="min-h-screen bg-white dark:bg-black transition-colors duration-300">
    <!-- Loading State -->
    <div v-if="loading" class="pt-32 h-[60vh] flex flex-col items-center justify-center text-gray-400">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-gray-900 dark:border-white mb-4"></div>
      加载中...
    </div>

    <!-- Content -->
    <div v-else-if="post" class="pt-24 pb-20">
      
      <!-- 1. 顶部 Hero 区域 -->
      <article class="max-w-4xl mx-auto px-4 sm:px-6 mb-12 text-center">
        <div class="flex justify-center gap-2 mb-6 flex-wrap">
           <!-- 如果有 tags 字段 -->
          <!-- <span v-if="post.tags && post.tags.length" v-for="tag in post.tags" :key="tag" class="bg-gray-100 dark:bg-zinc-800 text-gray-600 dark:text-gray-300 px-3 py-1 rounded-full text-sm font-medium">
            #{{ tag }}
          </span> -->
        </div>
        
        <h1 class="text-4xl md:text-6xl font-extrabold mb-8 leading-tight text-slate-900 dark:text-white">
          {{ post.title }}
        </h1>
        
        <!-- Meta Data -->
        <div class="flex items-center justify-center gap-6 text-gray-500 text-sm md:text-base border-b border-gray-100 dark:border-zinc-800 pb-8">
          <div class="flex items-center gap-2">
            <img :src="post.userAvatar" class="w-8 h-8 rounded-full bg-gray-200" />
            <span class="font-medium text-black dark:text-white">{{ post.author }}</span>
          </div>
          <div class="flex items-center gap-2">
            <Calendar :size="16" />
            <span>{{ post.createTime}}</span>
          </div>
          <div class="flex items-center gap-2">
            <Clock :size="16" />
            <span>阅读时间{{ readTime }} 分钟</span>
          </div>
        </div>
      </article>

      <!-- 2. 封面图 -->
      <div class="max-w-6xl mx-auto px-4 sm:px-6 mb-16">
        <div class="relative aspect-video rounded-3xl overflow-hidden shadow-2xl">
           <img :src="post.image" :alt="post.title" class="w-full h-full object-cover" />
        </div>
      </div>

      <!-- 3. 主体布局 (侧边栏 + 内容 + 目录) -->
      <div class="max-w-7xl mx-auto px-4 sm:px-6 grid grid-cols-1 lg:grid-cols-12 gap-12">
        
        <!-- Left Sidebar: Actions (Desktop) -->
        <div class="hidden lg:block col-span-1">
          <div class="sticky top-32 flex flex-col gap-6 items-center">
            <button @click="handleLike" class="p-3 rounded-full bg-gray-50 dark:bg-zinc-900 hover:bg-blue-50 dark:hover:bg-blue-900/30 text-gray-500 hover:text-blue-600 transition-colors group">
              <ThumbsUp :size="20" class="group-hover:scale-110 transition-transform"/>
            </button>
            <button @click="scrollToComments" class="p-3 rounded-full bg-gray-50 dark:bg-zinc-900 hover:bg-green-50 dark:hover:bg-green-900/30 text-gray-500 hover:text-green-600 transition-colors group">
              <MessageSquare :size="20" class="group-hover:scale-110 transition-transform"/>
            </button>
            <button @click="handleShare" class="p-3 rounded-full bg-gray-50 dark:bg-zinc-900 hover:bg-purple-50 dark:hover:bg-purple-900/30 text-gray-500 hover:text-purple-600 transition-colors group">
              <Share2 :size="20" class="group-hover:scale-110 transition-transform"/>
            </button>
          </div>
        </div>

        <!-- Center: Markdown Content -->
        <div class="col-span-1 lg:col-span-8">
           <!-- ai摘要 -->
           <div v-if="post.aiDescribe" class="bg-gray-50 dark:bg-zinc-900 p-6 rounded-2xl mb-8 border-l-4 border-black dark:border-white">
             <h3 class="font-bold text-xl mb-2">AI摘要</h3>
              <p class="text-lg text-gray-600 dark:text-gray-300 italic">
                {{ post.aiDescribe }}
              </p>
           </div>

           <!-- Markdown 渲染 -->
           <div class="prose prose-lg dark:prose-invert max-w-none prose-img:rounded-2xl prose-headings:scroll-mt-24">
             <MdPreview 
                editorId="preview-only" 
                :modelValue="post.contentMd" 
                :theme="themeStore ? 'dark' : 'light'"
                class="!bg-transparent"
              />
           </div>

           <!-- 文章底部作者卡片 -->
           <div class="mt-16 p-8 bg-gray-50 dark:bg-zinc-900 rounded-3xl flex items-center gap-6">
              <img :src="post.userAvatar" class="w-20 h-20 rounded-full bg-white" />
              <div>
                <h3 class="font-bold text-xl mb-1">作者：{{ post.author }}</h3>
                <p class="text-gray-500">{{ post.signature }}</p>
              </div>
           </div>
        </div>

        <!-- Right Sidebar: Table of Contents -->
        <div class="hidden lg:block col-span-3">
          <div class="sticky top-32">
            <h3 class="font-bold text-gray-400 uppercase text-xs tracking-wider mb-4 flex items-center gap-2">
              <List :size="14"/> 目录
            </h3>
            <!-- 目录组件 -->
            <MdCatalog 
              editorId="preview-only" 
              :scrollElement="scrollElement" 
              class="custom-catalog"
            />
          </div>
        </div>
      </div>

      <!-- 4. 评论区 -->
      <div id="comments-section" class="max-w-4xl mx-auto px-4 sm:px-6 mt-20 pt-10 border-t border-gray-100 dark:border-zinc-800">
        <Comments :module="'blog'" />
      </div>
    </div>
    
    <!-- 404 State -->
    <div v-else class="pt-32 text-center">
      <h1 class="text-2xl font-bold">Blog not found</h1>
      <button @click="$router.push('/blog')" class="mt-4 text-blue-600 hover:underline">Back to Blogs</button>
    </div>
  </div>
</template>

<style scoped>
@reference "tailwindcss";

/* 针对 MdPreview 的样式微调 */
:deep(.md-editor-preview-wrapper) {
  padding: 0;
}

/* =========================================
   目录 (Catalog) 样式优化
   ========================================= */

/* 1. 基础链接样式 */
:deep(.md-editor-catalog-link) {
  @apply relative block py-1.5 pl-4 text-sm text-gray-500 border-l-2 border-gray-100 dark:border-zinc-800 transition-all duration-200 cursor-pointer;
  /* 去掉默认可能存在的圆点 */
  list-style: none;
}

/* 2. 悬停状态 (Hover) */
:deep(.md-editor-catalog-link:hover) {
  @apply text-gray-900 dark:text-gray-300 border-gray-400 dark:border-zinc-600;
}

/* 3. 激活状态 (Active) - 这里使用了蓝色系，你可以改成你喜欢的颜色 */
:deep(.md-editor-catalog-active) {
  @apply text-blue-600 dark:text-blue-400 border-blue-600 dark:border-blue-400 font-medium bg-blue-50 dark:bg-blue-900/10;
  /* 右侧稍微圆角，增加质感 */
  border-radius: 0 4px 4px 0;
}

/* 4. 处理层级缩进带来的文字溢出问题 (可选) */
:deep(.md-editor-catalog-link span) {
  @apply truncate block;
}

/* 5. 隐藏 md-editor 自带的那个小圆点指示器（如果存在的话），用我们的左侧边框代替 */
:deep(.md-editor-catalog-indicator) {
  display: none !important;
}
</style>