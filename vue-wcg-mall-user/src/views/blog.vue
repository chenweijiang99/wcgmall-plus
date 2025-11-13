<script setup>
import { computed, watch, ref, onMounted, onBeforeUnmount,reactive } from "vue";
import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
import { useScrollToTop } from "@/assets/base.js";
import { ElMessageBox, dialogEmits, ElMessage } from "element-plus";
const { showLoader, backtopStyle, handleMouseEnter, handleMouseLeave } = useScrollToTop();
import { Plus, Edit } from "@element-plus/icons-vue";
import { useTokenStore } from "@/stores/token";
const tokenStore = useTokenStore();
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
const blogs = ref([]);

//搜索博客参数, 搜索功能暂时未实现
const authorNames = ref([]);
const searchTitles = ref([]);

const loading = ref(false);

const drawer = ref(false);
//添加博客弹窗数据
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
//上传成功的回调函数
const uploadSuccess = (result) => {
  blogData.value.image = result.data;
};

import {
  userAddBlogService,
  userGetBlogListService,
  userGetHotBlogService,
  userGetBlogListPageService,
} from "@/api/blog.js";
// 添加博客
const addBlog = async () => {
  let result = await userAddBlogService(blogData.value);
  if (result.code === 200) {
    ElMessage({
      showClose: true,
      type: "success",
      message: "添加成功",
      plain: true,
    });
    blogData.value = {
      title: "",
      content: "",
      image: "",
      content: "",
      contentMd: "",
    };
    drawer.value = false;
    loader(1);
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "添加失败",
      plain: true,
    });
  }
};
// 获取博客列表
const totalBlog = ref(0);
const pageSize = ref(0);
const loader = async (pageNum) => {
  let data = ref({
    searchTitles: searchTitles.value,
    authorNames: authorNames.value,
    pageNum: pageNum,
    pageSize: 5,
  });
  let result = await userGetBlogListPageService(data.value);
  if (result.code === 200) {
    if (pageNum === 1) {
      blogs.value = result.data.records; // 追加新博客
    } else {
      blogs.value = blogs.value.concat(result.data.records); // 追加新博客
    }

    totalBlog.value = result.data.total; // 总博客数
    pageSize.value = result.data.pageSize; // 每页博客数

    if (result.data.records.length === 0 || blogs.value.length >= totalBlog.value) {
      loading.value = true;
      if (result.data.records.length === 0 && pageNum === 1) {
        ElMessage({
          message: "没有找到符合条件的博客",
          type: "info",
          plain: true,
          showClose: true,
        });
      }
    }
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "加载失败",
      plain: true,
    });
  }
};

//获取热门博客
const hotBlogList = ref([]);
const getHotBlogList = async () => {
  let result = await userGetHotBlogService();
  if (result.code === 200) {
    hotBlogList.value = result.data;
  }
};

// 查看博客详情
const goToBlogDetail = (id) => {
  router.push({ path: "/blogDetail", query: { id: id } });
};
// 获取商品详情
const getProductDetail = (id) => {
  router.push({
    path: "/productDetail",
    query: { productId: id },
  });
};
import { userGetNewProductService } from "@/api/index.js";
const newProduct = ref([]);
// 获取最新商品
const getNewProduct = async () => {
  let result = await userGetNewProductService();
  if (result.code === 200) {
    newProduct.value = result.data;
  }
};

// 创建对话框宽度的响应式变量
const dialogWidth = ref("30%");

// 监听屏幕宽度变化
const mediaQuery = window.matchMedia("(max-width: 768px)");
const largeScreenQuery = window.matchMedia("(min-width: 769px) and (max-width: 1400px)");

// 根据屏幕宽度设置对话框宽度
const setDialogWidth = () => {
  if (mediaQuery.matches) {
    dialogWidth.value = "90%";
  } else if (largeScreenQuery.matches) {
    dialogWidth.value = "60%";
  } else {
    dialogWidth.value = "30%";
  }
};

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

onMounted(() => {
  getHotBlogList();
  getNewProduct();
  setDialogWidth();
  mediaQuery.addListener(setDialogWidth);
  largeScreenQuery.addListener(setDialogWidth);
  // 创建IntersectionObserver实例实现懒加载
  const ob = new IntersectionObserver(
    async (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          if (blogs.value.length === 0) {
            await loader(1);
          } else {
            if (!loading.value) {
              let pageNum = Math.ceil(blogs.value.length / pageSize.value) + 1;
              if (blogs.value.length < totalBlog.value) {
                setTimeout(async () => {
                  await loader(pageNum);
                }, 500);
              } else {
                ElMessage({
                  showClose: true,
                  type: "success",
                  message: "没有更多数据了",
                  plain: true,
                });
                loading.value = true;
              }
            }
          }
        }
      }
    },
    {
      threshold: 1,
    }
  );
  const dom_loading = document.getElementById("loading");
  ob.observe(dom_loading);
});

// 在组件卸载时移除监听器
onBeforeUnmount(() => {
  mediaQuery.removeListener(setDialogWidth);
  largeScreenQuery.removeListener(setDialogWidth);
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
            <li class="breadcrumb-item active" aria-current="page">博客</li>
          </ol>
        </nav>
        <div>
          <el-button type="info" :icon="Plus" width="100%" text @click="drawer = true">
            发布文章
          </el-button>

          <el-button
            type="success"
            :icon="Edit"
            width="100%"
            text
            @click="router.push('/userBlog')"
          >
            我的文章
          </el-button>
        </div>
      </div>
    </div>
  </div>
  <!-- end breadcrumb -->

  <!-- 添加或修改对话框 -->
  <el-dialog v-model="drawer" width="1400px" top="3vh" :close-on-click-modal="false">
    <template #header>
      <h4>发布文章</h4>
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
        <el-button type="primary" @click="addBlog">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </template>
  </el-dialog>

  <!-- main content -->
  <div class="main-content pb-80">
    <div class="container">
      <div class="row">
        <!-- sidebar -->
        <div class="col-lg-3 col-md-4 sidebar">
          <!-- <div class="sidebar__item">
            <h3 class="sidebar__item--title">添加文章</h3>
            <el-button
              type="info"
              :icon="Plus"
              width="100%"
              plain
              round
              style="margin-left: 25%"
              @click="drawer = true"
            >
              添加文章
            </el-button>
          </div> -->

          <!-- popular blog posts -->
          <div class="sidebar__item">
            <h3 class="sidebar__item--title">热门文章</h3>
            <ul class="media-list" v-for="item in hotBlogList" :key="item">
              <li class="media-list__item">
                <div class="media" @click="goToBlogDetail(item.id)">
                  <a class="media-list__item--img" href="javascript:void(0)">
                    <img :src="item.image" alt="..." />
                  </a>
                  <div class="media-body">
                    <a href="javascript:void(0)">
                      <h5 class="media-list__item--title">
                        {{ item.title }}
                      </h5>
                    </a>
                    <div class="media-list__item--details">
                      <span>{{ item.createTime }}</span>
                    </div>
                  </div>
                </div>
              </li>
            </ul>
          </div>
          <!-- blog post categories -->
          <!-- <div class="sidebar__item">
            <h3 class="sidebar__item--title">分类</h3>
            <ul class="categories-list">
              <li class="categories-list__item">
                <a href="javascript:void(0)"
                  >所有
                  <span class="badge category-badge">56</span>
                </a>
              </li>
              <li class="categories-list__item">
                <a href="javascript:void(0)"
                  >Sofas and armchairs
                  <span class="badge category-badge">12</span>
                </a>
              </li>
              <li class="categories-list__item">
                <a href="javascript:void(0)"
                  >Tables
                  <span class="badge category-badge">15</span>
                </a>
              </li>
              <li class="categories-list__item">
                <a href="javascript:void(0)"
                  >Chairs
                  <span class="badge category-badge">32</span>
                </a>
              </li>
            </ul>
          </div> -->
          <!-- end blog post categories -->
          <!-- latest products -->
          <div class="sidebar__item">
            <h3 class="sidebar__item--title">最新产品</h3>
            <ul>
              <li class="product-media" v-for="item in newProduct" :key="item">
                <div class="media" @click="getProductDetail(item.id)">
                  <a class="product-media__img" href="javascript:void(0)">
                    <img :src="item.image" alt="" style="width: 80px; height: 80px" />
                  </a>
                  <div class="media-body product-media__body">
                    <div class="title">
                      <a href="javascript:void(0)">{{ item.name }}</a>
                    </div>
                    <div class="price">
                      <span>￥{{ item.price }}</span>
                      <!-- <span class="old">$230.00</span> -->
                    </div>
                    <!-- <div class="rating">
                      <div class="product-rate">
                        <div class="product-rate__star">
                          <span class="rate-5"></span>
                        </div>
                        <div class="rate-number">(5)</div>
                      </div>
                    </div> -->
                  </div>
                </div>
              </li>
              <!-- <li class="product-media">
                <div class="media">
                  <a class="product-media__img" href="javascript:void(0)">
                    <img src="@/assets/images/product-media-02.jpg" alt="" />
                  </a>
                  <div class="media-body product-media__body">
                    <div class="title">
                      <a href="javascript:void(0)">Fabric armchair</a>
                    </div>
                    <div class="price">
                      <span>$200.00</span>
                      <span class="old">$350.00</span>
                    </div>
                    <div class="rating">
                      <div class="product-rate">
                        <div class="product-rate__star">
                          <span class="rate-4"></span>
                        </div>
                        <div class="rate-number">(4)</div>
                      </div>
                    </div>
                  </div>
                </div>
              </li>
              <li class="product-media">
                <div class="media">
                  <a class="product-media__img" href="javascript:void(0)">
                    <img src="@/assets/images/product-media-03.jpg" alt="" />
                  </a>
                  <div class="media-body product-media__body">
                    <div class="title">
                      <a href="javascript:void(0)">Gray sleeper Sofa</a>
                    </div>
                    <div class="price">
                      <span>$330.00</span>
                      <span class="old">$630.00</span>
                    </div>
                    <div class="rating">
                      <div class="product-rate">
                        <div class="product-rate__star">
                          <span class="rate-4"></span>
                        </div>
                        <div class="rate-number">(4)</div>
                      </div>
                    </div>
                  </div>
                </div>
              </li> -->
            </ul>
          </div>
          <!-- end of latest products -->
        </div>
        <!-- end sidebar -->
        <!-- blog post grid view -->
        <div class="col-lg-9 col-md-8">
          <div
            class="row no-gutters blog-post blog-post--list mb-30"
            v-for="item in blogs"
            :key="item"
          >
            <div class="col-lg-4" @click="goToBlogDetail(item.id)">
              <div class="blog-post__img">
                <a href="javascript:void(0)">
                  <img :src="item.image" alt="" />
                </a>
              </div>
            </div>
            <div class="col-lg-8">
              <div class="blog-post__inner ht-100">
                <div class="blog-post__inner--title">
                  <a href="javascript:void(0)" @click="goToBlogDetail(item.id)"
                    ><h4>{{ item.title }}</h4>
                  </a>
                </div>
                <div class="blog-post__inner--content">
                  <p v-html="item.content"></p>
                </div>

                <div class="blog-post__inner--btn">
                  <a href="" class="btn" @click="goToBlogDetail(item.id)">查看详情</a>
                </div>
              </div>
            </div>
          </div>
          <div class="row">
            <!-- custom pagination -->
            <!-- <div class="col-12">
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
                    <a class="page-link" href="javascript:void(0)" @click="changePage(page)">
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
            </div> -->
            <!-- end custom pagination -->
          </div>
        </div>
        <!-- end blog post grid view -->

        <!-- 加载更多 -->
        <div
          style="
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100px;
          "
          id="loading"
        >
          <div class="loading" v-show="!loading">
            <div class="spinner">
              <div class="cube1"></div>
              <div class="cube2"></div>
            </div>
          </div>
          <div></div>
        </div>
      </div>
    </div>
  </div>
  <!-- end main content -->

  <!-- scroll up btn -->
  <el-backtop
    :right="100"
    :bottom="100"
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
.loading {
  background: #ffffff;
  height: 10%;
  left: 0;
  top: 0;
  width: 10%;
  z-index: 2000;
  display: -ms-flexbox;
  display: flex;
  -webkit-align-items: center;
  -ms-flex-align: center;
  align-items: center;
}
</style>
