<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch, reactive, computed } from "vue";
import { RouterLink } from "vue-router";
import {
  getBlogPageApi,
  addBlogApi,
  getUserBlogApi,
  getBlogDetailApi,
  updateBlogApi,
  deleteBlogApi,
} from "@/api/blog";
import { uploadApi } from "@/api/file";
import { useUserStore } from "@/stores/modules/user";
import { Blog } from "@/types";

import {
  Edit3,
  Loader,
  Search,
  User,
  FileText,
  Plus,
  X,
  Pencil,
  Trash2,
  EyeOff,
  AlertCircle,
} from "lucide-vue-next";
import { ElMessage, ElMessageBox, UploadRequestOptions } from "element-plus";

import { MdEditor, ToolbarNames } from "md-editor-v3";
import "md-editor-v3/lib/style.css";
import EmojiExtension from "@/components/MarkdownExtensions/EmojiExtension/index.vue";
import VideoExtension from "@/components/MarkdownExtensions/VideoExtension/index.vue";

const store = useUserStore();

const blogs = ref<Blog[]>([]);
const page = ref(1);
const pageSize = 4;
const hasMore = ref(true);
const loading = ref(false);
const loadMoreRef = ref<HTMLElement | null>(null);
let observer: IntersectionObserver | null = null;

const searchQuery = ref("");
const searchType = ref<"title" | "author">("title");
const currentTab = ref<"all" | "my">("all");
let debounceTimer: ReturnType<typeof setTimeout> | null = null;

const showDialog = ref(false);
const dialogLoading = ref(false);
const formRef = ref();
const mdRef = ref<any>();
const form = reactive({
  id: undefined as number | undefined,
  title: "",
  author: "",
  image: "",
  content: "",
  contentMd: "",
  status: 0,
});
const dialogTitle = computed(() => (form.id ? "编辑博客" : "写博客"));

// 校验规则
const rules = reactive({
  title: [{ required: true, message: "请输入博客标题", trigger: "blur" }],
  image: [{ required: true, message: "请上传封面图", trigger: "change" }],
  contentMd: [{ required: true, message: "请输入文章内容", trigger: "blur" }],
});

// === 屏幕适配与菜单配置 ===
const screenWidth = ref(window.innerWidth);
const toolbarsDesktop: ToolbarNames[] = [
  "bold",
  "underline",
  "italic",
  "strikeThrough",
  "-",
  "title",
  "sub",
  "sup",
  "quote",
  "unorderedList",
  "orderedList",
  "task",
  "-",
  "codeRow",
  "code",
  "link",
  "image",
  "table",
  "mermaid",
  "katex",
  0,
  1,
  "-",
  "revoke",
  "next",
  "save",
  "=",
  "prettier",
  "pageFullscreen",
  "fullscreen",
  "preview",
  "htmlPreview",
  "catalog",
];
const toolbarsMobile: ToolbarNames[] = [
  "bold",
  "italic",
  "title",
  "-",
  "image",
  0,
  1,
  "-",
  "revoke",
  "next",
  "save",
  "=",
  "preview",
];
const currentToolbars = computed(() => {
  return screenWidth.value < 768 ? toolbarsMobile : toolbarsDesktop;
});
const handleResize = () => {
  screenWidth.value = window.innerWidth;
};

// === 核心逻辑：获取博客列表 ===
const fetchBlogs = async (reset = false) => {
  if (loading.value) return;
  if (!reset && !hasMore.value) return;

  loading.value = true;

  try {
    // 分支 1: 获取“我的博客”
    if (currentTab.value === "my") {
      if (reset) blogs.value = [];
      const res = await getUserBlogApi();
      if (res.code === 200) {
        let allMyBlogs = res.data;
        if (searchQuery.value) {
          const lowerQuery = searchQuery.value.toLowerCase();
          allMyBlogs = allMyBlogs.filter((blog: Blog) => {
            return blog.title.toLowerCase().includes(lowerQuery);
          });
        }
        blogs.value = allMyBlogs;
        hasMore.value = false;
      }
    }
    // 分支 2: 获取“全部博客”
    else {
      const currentPage = reset ? 1 : page.value;
      let searchTitles =
        searchType.value === "title" && searchQuery.value
          ? [searchQuery.value]
          : undefined;
      let authorNames =
        searchType.value === "author" && searchQuery.value
          ? [searchQuery.value]
          : undefined;

      const params = {
        pageNum: currentPage,
        pageSize: pageSize,
        searchTitles,
        authorNames,
      };

      const res = await getBlogPageApi(params);
      if (res.code === 200) {
        const pageData = res.data;
        const total = pageData.total;
        const newBlogs = pageData.records;

        if (reset) {
          blogs.value = newBlogs;
          page.value = 2;
        } else {
          blogs.value = [...blogs.value, ...newBlogs];
          page.value++;
        }
        if (blogs.value.length >= total) hasMore.value = false;
        else hasMore.value = true;
      }
    }
  } catch (error) {
    console.error("Load blogs error:", error);
    hasMore.value = false;
  } finally {
    loading.value = false;
    // 仅在“全部博客”模式下检测首屏是否填满
    if (currentTab.value === "all" && hasMore.value) {
      await nextTick();
      const el = loadMoreRef.value;
      if (el) {
        const rect = el.getBoundingClientRect();
        if (rect.top < window.innerHeight + 50) {
          fetchBlogs(false);
        }
      }
    }
  }
};

watch([searchQuery, searchType, currentTab], () => {
  if (debounceTimer) clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    hasMore.value = true;
    page.value = 1;
    fetchBlogs(true);
  }, 300);
});

// === 表单操作 ===
const resetForm = () => {
  form.id = undefined;
  form.title = "";
  form.image = "";
  form.content = "";
  form.contentMd = "";
  form.author = store.user?.nickname || store.user?.username || "";
  form.status = 1;
};

const openEditDialog = async (blogId: number) => {
  dialogLoading.value = true;
  try {
    const res = await getBlogDetailApi(blogId);
    if (res.code === 200) {
      const data = res.data;
      resetForm();
      form.id = data.id;
      form.title = data.title;
      form.image = data.image;
      form.author = data.author;
      form.status = data.status;
      form.contentMd = data.contentMd || data.content || "";
      form.content = data.content;
      showDialog.value = true;
    }
  } catch (error) {
    ElMessage.error("获取详情失败");
  } finally {
    dialogLoading.value = false;
  }
};

const openCreateDialog = () => {
  if (!store.user) {
    ElMessage.warning("请先登录");
    return;
  }
  resetForm();
  showDialog.value = true;
};

// 删除博客
const handleDelete = async (blogId: number) => {
  ElMessageBox.confirm("确定要删除这篇博客吗？删除后无法恢复。", "删除确认", {
    confirmButtonText: "删除",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        await deleteBlogApi(blogId);
        ElMessage.success("删除成功");
        // 刷新列表
        hasMore.value = true;
        fetchBlogs(true);
      } catch (error) {}
    })
    .catch(() => {});
};

// 下架博客
const handleTakeDown = async (blog: Blog) => {
  ElMessageBox.confirm("确定要下架这篇博客吗？下架后其他用户将无法查看。", "下架确认", {
    confirmButtonText: "下架",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        // 调用更新接口，将状态改为 0 (下架/草稿)
        await updateBlogApi({
          id: blog.id,
          status: 0,
        });
        ElMessage.success("已下架");
        hasMore.value = true;
        fetchBlogs(true);
      } catch (error) {
        console.error(error);
      }
    })
    .catch(() => {});
};

// 图片/视频上传逻辑
const handleCoverUpload = async (options: UploadRequestOptions) => {
  try {
    const formData = new FormData();
    formData.append("file", options.file);
    const res = await uploadApi(formData, "blog-image");
    form.image = res.data;
    ElMessage.success("封面上传成功");
  } catch (error) {
    ElMessage.error("上传失败");
  }
};
const onEditorUploadImg = async (
  files: Array<File>,
  callback: (urls: string[]) => void
) => {
  const res = await Promise.all(
    files.map((file) => {
      const formData = new FormData();
      formData.append("file", file);
      return uploadApi(formData, "blog-content");
    })
  );
  callback(res.map((item: any) => item.data));
};
const onHtmlChanged = (html: string) => {
  form.content = html;
};
const insert = (generator: any) => {
  mdRef.value?.insert(generator);
};
const handleUploadVideo = async (options: UploadRequestOptions) => {
  try {
    const formData = new FormData();
    formData.append("file", options.file);
    const res = await uploadApi(formData, "article-video");
    const videoTag = `\n<video controls style="width: 100%; height: auto;" src="${res.data}">\n</video>\n`;
    insert(() => ({
      targetValue: videoTag,
      select: true,
      deviationStart: 0,
      deviationEnd: 0,
    }));
  } catch (error) {
    ElMessage.error("视频上传失败");
  }
};

// 提交表单
const submitBlog = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      dialogLoading.value = true;
      try {
        if (form.id) {
          await updateBlogApi(form);
          ElMessage.success("修改成功");
        } else {
          await addBlogApi(form);
          ElMessage.success("发布成功，请等待管理员审核");
        }
        showDialog.value = false;
        hasMore.value = true;
        fetchBlogs(true);
      } catch (error) {
        console.error(error);
      } finally {
        dialogLoading.value = false;
      }
    }
  });
};

// 获取博客状态样式和文本
const getStatusBadge = (status: number) => {
  // 1=已发布, 0=已下架/审核中
  if (status === 1) {
    return { text: "已发布", class: "bg-green-50 text-green-600 border-green-100" };
  } else {
    return {
      text: "审核中/已下架",
      class: "bg-orange-50 text-orange-600 border-orange-100",
    };
  }
};

onMounted(() => {
  window.addEventListener("resize", handleResize);
  fetchBlogs(true);
  observer = new IntersectionObserver(
    (entries) => {
      const target = entries[0];
      if (target.isIntersecting && !loading.value && hasMore.value) {
        fetchBlogs(false);
      }
    },
    { rootMargin: "100px" }
  );
  if (loadMoreRef.value) observer.observe(loadMoreRef.value);
});

onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
  observer?.disconnect();
  if (debounceTimer) clearTimeout(debounceTimer);
});
</script>

<template>
  <div class="pt-24 pb-20 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
    <!-- 头部-->
    <div
      class="flex flex-col md:flex-row justify-between md:items-end mb-8 md:mb-12 gap-6"
    >
      <div>
        <h1 class="text-4xl md:text-5xl font-bold mb-4">博客</h1>
        <p class="text-xl text-gray-500">对设计、技术和生活的见解。</p>
      </div>
      <button
        v-if="store.user"
        @click="openCreateDialog"
        class="w-full md:w-auto px-6 py-3 bg-black text-white dark:bg-white dark:text-black rounded-full font-medium flex items-center justify-center md:justify-start gap-2 hover:opacity-80 transition-opacity shadow-lg"
      >
        <Edit3 :size="18" /> 发布博客
      </button>
    </div>

    <!--筛选 -->
    <div
      class="mb-10 flex flex-col lg:flex-row justify-between items-stretch lg:items-center gap-4"
    >
      <div
        class="bg-gray-100 dark:bg-zinc-800 p-1.5 rounded-xl grid grid-cols-2 lg:flex gap-1 w-full lg:w-auto"
      >
        <button
          @click="currentTab = 'all'"
          :class="`px-5 py-2.5 lg:py-2 rounded-lg text-sm font-bold transition-all text-center ${
            currentTab === 'all'
              ? 'bg-white dark:bg-black shadow-sm text-black dark:text-white'
              : 'text-gray-500 hover:text-black dark:hover:text-gray-300'
          }`"
        >
          全部博客
        </button>
        <button
          v-if="store.user"
          @click="currentTab = 'my'"
          :class="`px-5 py-2.5 lg:py-2 rounded-lg text-sm font-bold transition-all text-center ${
            currentTab === 'my'
              ? 'bg-white dark:bg-black shadow-sm text-black dark:text-white'
              : 'text-gray-500 hover:text-black dark:hover:text-gray-300'
          }`"
        >
          我的博客
        </button>
      </div>
      <!-- 搜索 (只在非'我的博客'显示，或根据需求调整) -->
      <div
        class="bg-white dark:bg-zinc-900 border border-gray-100 dark:border-zinc-800 p-1.5 rounded-xl flex flex-col sm:flex-row items-center gap-2 shadow-sm w-full lg:w-auto lg:max-w-xl"
        v-if="currentTab !== 'my'"
      >
        <div
          class="grid grid-cols-2 sm:flex p-1 bg-gray-50 dark:bg-zinc-800 rounded-lg shrink-0 w-full sm:w-auto gap-1"
        >
          <button
            @click="searchType = 'title'"
            :class="`flex-1 sm:flex-none px-3 py-2 sm:py-1.5 rounded-md text-xs font-medium transition-all flex items-center justify-center gap-1 ${
              searchType === 'title'
                ? 'bg-white dark:bg-black shadow-sm text-black dark:text-white'
                : 'text-gray-500 hover:text-gray-900'
            }`"
          >
            <FileText :size="14" /> 标题
          </button>
          <button
            @click="searchType = 'author'"
            :class="`flex-1 sm:flex-none px-3 py-2 sm:py-1.5 rounded-md text-xs font-medium transition-all flex items-center justify-center gap-1 ${
              searchType === 'author'
                ? 'bg-white dark:bg-black shadow-sm text-black dark:text-white'
                : 'text-gray-500 hover:text-gray-900'
            }`"
          >
            <User :size="14" /> 作者
          </button>
        </div>
        <div class="relative flex-1 w-full">
          <Search
            class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
            :size="16"
          />
          <input
            v-model="searchQuery"
            type="text"
            :placeholder="searchType === 'title' ? '搜索文章标题...' : '搜索作者名称...'"
            class="w-full bg-transparent pl-9 pr-8 py-3 outline-none text-sm text-gray-900 dark:text-white placeholder-gray-400"
          />
          <button
            v-if="searchQuery"
            @click="searchQuery = ''"
            class="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 hover:text-black dark:hover:text-white"
          >
            <X :size="14" />
          </button>
        </div>
      </div>
    </div>

    <!-- 博客列表 -->
    <div class="grid gap-8 md:gap-12 min-h-[400px]">
      <RouterLink
        v-for="(blog, idx) in blogs"
        :to="`/blog/${blog.id}`"
        :key="blog.id"
        class="group grid grid-cols-1 md:grid-cols-2 gap-6 md:gap-8 items-center cursor-pointer animate-fade-in relative"
      >
        <!-- 图片区域 -->
        <div
          :class="`relative rounded-3xl overflow-hidden h-56 md:h-80 border border-gray-100 dark:border-zinc-800 ${
            idx % 2 === 1 ? 'md:order-2' : ''
          }`"
        >
          <img
            :src="blog.image"
            :alt="blog.title"
            class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
            loading="lazy"
          />

          <div
            v-if="currentTab === 'my' || (store.user && blog.userId === store.user.id)"
            class="absolute top-4 right-4 flex flex-col gap-2 z-30 opacity-100 translate-x-0 lg:opacity-0 lg:translate-x-4 lg:group-hover:opacity-100 lg:group-hover:translate-x-0 transition-all duration-300"
          >
            <!-- 编辑 -->
            <button
              @click.prevent.stop="openEditDialog(Number(blog.id))"
              class="p-2.5 bg-white text-black rounded-full shadow-md hover:bg-black hover:text-white transition-colors"
              title="编辑"
            >
              <Pencil :size="16" />
            </button>

            <!-- 下架 (仅当状态为 1-已发布 时显示) -->
            <button
              v-if="blog.status === 1"
              @click.prevent.stop="handleTakeDown(blog)"
              class="p-2.5 bg-white text-orange-600 rounded-full shadow-md hover:bg-orange-600 hover:text-white transition-colors"
              title="下架"
            >
              <EyeOff :size="16" />
            </button>

            <!-- 删除 -->
            <button
              @click.prevent.stop="handleDelete(Number(blog.id))"
              class="p-2.5 bg-white text-red-600 rounded-full shadow-md hover:bg-red-600 hover:text-white transition-colors"
              title="删除"
            >
              <Trash2 :size="16" />
            </button>
          </div>
        </div>

        <!-- 文本区域 -->
        <div :class="idx % 2 === 1 ? 'md:order-1' : ''">
          <div class="flex items-center gap-3 text-sm text-gray-500 mb-3 md:mb-4">
            <span>{{ blog.createTime }}</span>
            <!-- 状态标签 -->
            <span
              v-if="currentTab === 'my'"
              :class="`text-xs px-2 py-0.5 rounded border flex items-center gap-1 ${
                getStatusBadge(blog.status).class
              }`"
            >
              <AlertCircle v-if="blog.status !== 1" :size="12" />
              {{ getStatusBadge(blog.status).text }}
            </span>
          </div>
          <h2
            class="text-2xl md:text-3xl font-bold mb-3 md:mb-4 group-hover:text-blue-600 transition-colors"
          >
            {{ blog.title }}
          </h2>
          <p
            class="text-gray-600 dark:text-gray-400 leading-relaxed mb-4 md:mb-6 line-clamp-3"
          >
            {{ blog.contentMd }}
          </p>
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 rounded-full bg-gray-200 overflow-hidden">
              <img :src="blog.userAvatar" :alt="blog.author" />
            </div>
            <span class="text-sm font-medium">{{ blog.author }}</span>
          </div>
        </div>
      </RouterLink>
    </div>

    <!-- 底部加载 -->
    <div
      ref="loadMoreRef"
      class="mt-12 md:mt-16 text-center py-8 h-20 flex items-center justify-center"
    >
      <div v-if="loading" class="flex items-center gap-2 text-gray-400">
        <Loader class="animate-spin" :size="24" /><span class="text-sm">加载中...</span>
      </div>
      <p v-else-if="!hasMore && blogs.length > 0" class="text-gray-400 text-sm">
        —— 已经到底啦 (共{{ blogs.length }}) ——
      </p>
      <div
        v-else-if="!hasMore && blogs.length === 0"
        class="flex flex-col items-center text-gray-400"
      >
        <p class="mb-4">暂无博客</p>
        <button
          v-if="currentTab === 'my'"
          @click="openCreateDialog"
          class="text-blue-600 hover:underline"
        >
          发布博客?
        </button>
      </div>
    </div>

    <!-- 写博客 Dialog -->
    <el-dialog
      v-model="showDialog"
      :title="dialogTitle"
      width="90%"
      class="max-w-6xl dark:bg-zinc-900 rounded-2xl"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-4">
          <el-form-item label="标题" prop="title">
            <el-input
              v-model="form.title"
              placeholder="请输入引人注目的标题"
              size="large"
            />
          </el-form-item>
          <el-form-item label="封面图" prop="image">
            <el-upload
              class="w-full"
              action="#"
              :http-request="handleCoverUpload"
              :show-file-list="false"
              accept="image/*"
            >
              <div
                v-if="form.image"
                class="relative w-full h-32 rounded-lg overflow-hidden group border border-gray-200"
              >
                <img :src="form.image" class="w-full h-full object-cover" />
                <div
                  class="absolute inset-0 bg-black/50 hidden group-hover:flex items-center justify-center text-white"
                >
                  点击更换
                </div>
              </div>
              <div
                v-else
                class="w-full h-32 border-2 border-dashed border-gray-300 rounded-lg flex flex-col items-center justify-center text-gray-400 hover:border-blue-500 hover:text-blue-500 transition-colors cursor-pointer"
              >
                <Plus :size="24" class="mb-2" /><span>点击上传封面</span>
              </div>
            </el-upload>
          </el-form-item>
        </div>
        <el-form-item label="内容" prop="contentMd">
          <MdEditor
            ref="mdRef"
            v-model="form.contentMd"
            style="width: 100%"
            class="h-[50vh] md:h-[500px]"
            placeholder="开始创作你的故事..."
            @on-upload-img="onEditorUploadImg"
            :toolbars="currentToolbars"
            @onHtmlChanged="onHtmlChanged"
          >
            <template #defToolbars>
              <EmojiExtension :onInsert="insert" />
              <VideoExtension :onInsert="insert" :onUploadVideo="handleUploadVideo" />
            </template>
          </MdEditor>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="showDialog = false">取消</el-button>
          <el-button type="primary" @click="submitBlog" :loading="dialogLoading">
            {{ form.id ? "提交修改" : "发布文章" }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
