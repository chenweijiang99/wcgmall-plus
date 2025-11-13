<script setup>
import { computed, watch, ref, onMounted, onBeforeUnmount,shallowRef,reactive } from "vue";
import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
import { useScrollToTop } from "@/assets/base.js";
import { ElMessageBox, dialogEmits, ElMessage } from "element-plus";
const { showLoader, backtopStyle, handleMouseEnter, handleMouseLeave } =
  useScrollToTop();
import { Plus, Edit, Delete } from "@element-plus/icons-vue";
import { useTokenStore } from "@/stores/token";

import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css' 

import { useRouter } from "vue-router";
import UploadImage from "@/components/Upload/Image.vue";
import { MdEditor } from "md-editor-v3";
import "md-editor-v3/lib/style.css";
import EmojiExtension from "@/components/MarkdownExtensions/EmojiExtension/index.vue";
import VideoExtension from "@/components/MarkdownExtensions/VideoExtension/index.vue";
import { uploadApi, deleteFileApi } from "@/api/file.js";
const toolbars = [
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
  // 'github'
];
const router = useRouter();
const tokenStore = useTokenStore();
const blogs = ref([]);

const currentPage = ref(1); //默认第一页
// 计算属性，确定是否显示上一页和下一页按钮
const showPreviousPage = computed(() => currentPage.value > 1);
const showNextPage = computed(() => currentPage.value < totalPages.value);
//一页显示的博客数量
const blogsPerPage = 5;
// 总共的博客数量
const totalBlogs = computed(() => blogs.value.length);
// 总页数
const totalPages = computed(() => Math.ceil(totalBlogs.value / blogsPerPage));
// 分页博客数据
const displayedBlogs = computed(() => {
  const start = (currentPage.value - 1) * blogsPerPage;
  const end = currentPage.value * blogsPerPage;
  return blogs.value.slice(start, end);
});
// 分页按钮
const changePage = (page) => {
  currentPage.value = page;
};

const drawer = ref(false);
const drawerTitile = ref("");
const drawerButton = ref("");
const blogData = ref({
 title: "",
  content: "",
  image: "",
  content: "",
  contentMd: "",
});
const formRef = ref();
const mdRef = ref();
// 表单校验
const rules = reactive({
  title: [{ required: true, message: "请输入博客标题", trigger: "blur" }],
  image: [{ required: true, message: "请上传博客封面", trigger: "blur" }],
  author: [{ required: true, message: "请输入作者名", trigger: "blur" }],
});
// 处理图片上传

const onUploadImg = async (files, callback) => {
  const res = await Promise.all(
    files.map((file) => {
      return new Promise((resolve, reject) => {
        const form = new FormData();
        form.append("file", file);
        uploadApi(form, "blog-content")
          .then((res) => {
            resolve(res);
          })
          .catch((err) => {
            reject(err);
          });
      });
    })
  );
  callback(res.map((item) => item.data));
};
const saveHtml = (html) => {
  blogData.value.content = html;
};
const insert = (generator) => {
  mdRef.value?.insert(generator);
};
const cancel = () => {
  drawer.value = false;
  blogData.value = {
    title: "",
    content: "",
    image: "",
    content: "",
    contentMd: "",
  };
};

//上传成功的回调函数
const uploadSuccess = (result) => {
  blogData.value.image = result.data;
};

import {
  userGetBlogListByUserIdService,
  userAddBlogService,
  userDelBlogService,
  userUpdateBlogService,
} from "@/api/blog.js";
// 添加博客
const addBlog = async () => {
  let result = await userAddBlogService(blogData.value);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message:  "添加成功",
      plain: true,
    });
    clearBlogData();
    drawer.value = false;
    getBlogList();
  } else {
    ElMessage.error(result.message ? result.message : "添加失败");
    clearBlogData();
  }
};

// 获取博客列表
const getBlogList = async () => {
  let result = await userGetBlogListByUserIdService();
  if (result.code === 200) {
    blogs.value = result.data;
  }
};


//删除博客
const deleteBlog = async (id) => {
  ElMessageBox.confirm("你确认删除该文章吗？", "温馨提示", {
    confirmButtonText: "确认",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      let result = await userDelBlogService(id);
      if (result.code === 200) {
        ElMessage({
          showClose: true,
          type: "success",
          message: "删除成功",
          plain: true,
        });
        getBlogList();
      } else {
        ElMessage.error(result.message ? result.message : "删除失败");
      }
    })
    .catch(() => {
      ElMessage({
        type: "info",
        message: "用户取消删除",
      });
    });
};

// 编辑博客
const editeBlog = (row) => {
  blogData.value = row;
  drawer.value = true;
  drawerTitile.value = "编辑文章";
  drawerButton.value = "保存编辑";
};
//保存编辑
const saveEdit = async () => {
  let result = await userUpdateBlogService(blogData.value);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message: "编辑成功",
      plain: true,
    });
    clearBlogData();
    drawer.value = false;
    getBlogList();
  } else {
    ElMessage.error(result.message ? result.message : "编辑失败");
    clearBlogData();
  }
};
// 查看博客详情
const goToBlogDetail = (id) => {
  router.push({ path: "/blogDetail", query: { id: id } });
};
// 清空数据
const clearBlogData = () => {
  blogData.value = {
    id: "",
    title: "",
    content: "",
    image: "",
  };
};

// 创建对话框宽度的响应式变量
const dialogWidth = ref("40%");

// 监听屏幕宽度变化
const mediaQuery = window.matchMedia("(max-width: 768px)");
const largeScreenQuery = window.matchMedia(
  "(min-width: 769px) and (max-width: 1400px)"
);

// 根据屏幕宽度设置对话框宽度
const setDialogWidth = () => {
  if (mediaQuery.matches) {
    dialogWidth.value = "90%";
  } else if (largeScreenQuery.matches) {
    dialogWidth.value = "60%";
  } else {
    dialogWidth.value = "40%";
  }
};

// 在组件挂载时添加监听器
onMounted(() => {
  getBlogList();
  setDialogWidth();
  mediaQuery.addListener(setDialogWidth);
  largeScreenQuery.addListener(setDialogWidth);
});

// 在组件卸载时移除监听器
onBeforeUnmount(() => {
  mediaQuery.removeListener(setDialogWidth);
  largeScreenQuery.removeListener(setDialogWidth);
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
});

</script>


<template>
  <!-- <headerView /> -->

  <!-- breadcrumb -->
  <div class="container header-mt">
    <div class="row">
      <div class="col-12 d-flex justify-content-between align-items-center">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb custom-breadcrumb m-0">
            <li class="breadcrumb-item">
              <router-link to="/index"> 主页 </router-link>
            </li>
            <li class="breadcrumb-item" aria-current="page">
              <router-link to="/blog">
                <a href="">博客</a>
              </router-link>
            </li>
            <li class="breadcrumb-item active" aria-current="page">我的博客</li>
          </ol>
        </nav>
        <div>
          <el-button
            type="info"
            :icon="Plus"
            width="100%"
            text
            @click="
              (drawer = true),
                (drawerButton = '添加'),
                (drawerTitile = '发布文章'),
                clearBlogData()
            "
          >
            发布文章
          </el-button>
        </div>
      </div>
    </div>
  </div>
  <!-- end breadcrumb -->

  <!-- 添加或修改对话框 -->
  <el-dialog v-model="drawer"  width="1400px" top="3vh" :close-on-click-modal="false">
    <template #header>
      <h4>{{ drawerTitile }}</h4>
    </template>
    <el-form ref="formRef" :model="blogData" :rules="rules" label-width="80px">
      <el-row :gutter="20" class="mb-20">
        <el-col :span="8">
          <el-form-item label="博客标题" prop="title">
            <el-input v-model="blogData.title" placeholder="请输入博客标题" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="作者" prop="author">
            <el-input v-model="blogData.author" placeholder="请输入作者名" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="博客封面" prop="image">
        <UploadImage v-model="blogData.image" :limit="1" :source="'blog-image'" />
      </el-form-item>
      <el-form-item label="博客内容" prop="content">
        <MdEditor
          ref="mdRef"
          v-model="blogData.contentMd"
          style="height: 500px; width: 100%"
          placeholder="输入文章内容..."
          @on-upload-img="onUploadImg"
          :toolbars="toolbars"
          @onHtmlChanged="saveHtml"
        >
          <template #defToolbars>
            <EmojiExtension :onInsert="insert" />
          </template>
        </MdEditor>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
    
        <el-button v-if="drawerButton == '添加'" type="primary" @click="addBlog">{{ drawerButton }}</el-button>
        <el-button v-else="drawerButton == '编辑文章'" type="primary" @click="saveEdit">{{ drawerButton }}</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </template>
  </el-dialog>
  <!-- main content -->
  <div class="main-content pb-80">
    <div class="container">
      <div class="row">
        <!-- blog post grid view -->
        <div class="col-12">
          <div
            class="row no-gutters blog-post blog-post--list mb-30"
            v-for="item in displayedBlogs"
            :key="item"
          >
            <div class="col-lg-4" @click="goToBlogDetail(item.id)">
              <div class="blog-post__img">
                <a href="javascript:void(0)">
                  <img :src="item.image" alt="" />
                </a>
              </div>
            </div>
            <div class="col-lg-8" >
              <div class="blog-post__inner ht-100">
                <div class="blog-post__inner--title" @click="goToBlogDetail(item.id)">
                  <a href="javascript:void(0)"
                    ><h4>{{ item.title }}</h4>
                  </a>
                </div>
                <div class="blog-post__inner--content">
                  <p v-html="item.content"></p>
                  <!-- <p>
                    {{ item.content }}
                  </p> -->
                </div>

                <div class="blog-post__inner--btn">
                  <el-tag v-if="item.status == 1" type="success">已发布</el-tag>
                  <el-tag v-else="item.status == 0" type="danger">未审核</el-tag>

                  <el-tag type="info"
                    >发布时间：{{ item.createTime }}</el-tag
                  >
                  <el-button
                    type="success"
                    :icon="Edit"
                    @click="editeBlog(item)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="danger"
                    :icon="Delete"
                    @click="deleteBlog(item.id)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          <div class="row">
            <!-- custom pagination -->
            <div class="col-12">
              <nav aria-label="Page navigation">
                <ul class="pagination custom-pagination">
                  <li class="page-item" v-if="currentPage > 1">
                    <a
                      class="page-link"
                      href="javascript:void(0)"
                      @click="changePage(currentPage - 1)"
                      ><i class="bi-caret-left-fill"></i
                    ></a>
                  </li>
                  <li
                    v-for="page in totalPages"
                    :key="page"
                    :class="{ 'page-item': true, active: currentPage === page }"
                  >
                    <a class="page-link" href="javascript:void(0)">
                      {{ page }}</a
                    >
                  </li>
                  <li class="page-item" v-if="currentPage < totalPages">
                    <a
                      class="page-link"
                      href="javascript:void(0)"
                      @click="changePage(currentPage + 1)"
                      ><i class="bi-caret-right-fill"></i
                    ></a>
                  </li>
                </ul>
              </nav>
            </div>
            <!-- end custom pagination -->
          </div>
        </div>
        <!-- end blog post grid view -->
      </div>
    </div>
  </div>
  <!-- end main content -->

  <!-- scroll up btn -->
  <el-backtop
    :style="backtopStyle"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  />
  <!-- end scroll up btn -->
  <!-- loader -->
  <div class="loader" v-if="showLoader">
    <div class="spinner">
      <div class="cube1"></div>
      <div class="cube2"></div>
    </div>
  </div>
  <!-- end loader -->

  <!-- <footerView /> -->
</template>


<style scoped lang="scss">
@import "@/assets/main.css";
a {
  text-decoration: none;
}
.media-body {
  -ms-flex: 1;
  flex: 1;
}
.media {
  display: -ms-flexbox;
  display: flex;
  -ms-flex-align: start;
  align-items: flex-start;
}

.avatar-uploader {
  :deep() {
    .avatar {
      width: 178px;
      height: 178px;
      display: block;
    }

    .el-upload {
      border: 1px dashed var(--el-border-color);
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);
    }

    .el-upload:hover {
      border-color: var(--el-color-primary);
    }

    .el-icon.avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 178px;
      height: 178px;
      text-align: center;
    }
  }
}
.blog-post--list .blog-post__inner--content {
  height: 150px;
  overflow: auto;
  margin-bottom: 1rem;
}
</style>