<script setup>
import { computed, watch, ref, onMounted, onBeforeUnmount } from "vue";
import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
import { useScrollToTop } from "@/assets/base.js";
import { ElMessageBox, dialogEmits, ElMessage } from "element-plus";
const { showLoader, backtopStyle, handleMouseEnter, handleMouseLeave } =
  useScrollToTop();
import { Plus, Edit, Delete } from "@element-plus/icons-vue";
import useUserInfoStore from "@/stores/userInfo.js";
const userInfoStore = useUserInfoStore();
import { useRoute, useRouter } from "vue-router";

import Comment from "@/components/Comment.vue";
const route = useRoute();
const router = useRouter();
const blogId = route.query.id;
const latestBlogList = ref([]);
const instagramPostsList = ref([]);
const blogDetail = ref({});
const comments = ref([]);
const relatedBlogList = ref([]);

import {
  userGetLatestBlogService,
  userGetBlogByIdService,
  userGetRelatedBlogService,
  userGetBlogCommentListService,
  userAddBlogCommentsService,
  userDeleteBlogCommentsService,
} from "@/api/blog.js";

const getLatestBlog = async () => {
  let result = await userGetLatestBlogService();
  latestBlogList.value = result.data;
};
const getBlogDetail = async (blogId) => {
  let result = await userGetBlogByIdService(blogId);
  blogDetail.value = result.data;

};
const getRelatedBlog = async () => {
  let result = await userGetRelatedBlogService(blogId);
  relatedBlogList.value = result.data;
};

const getComments = async (blogId) => {
  let result = await userGetBlogCommentListService(blogId);
  comments.value = result.data;
};
const commentsData = ref("");
const addComments = async () => {
  if (commentsData.value.trim() == "") {
    ElMessage({
      showClose: true,
      type: "error",
      message: "评论不能为空",
      plain: true,
    });
    return;
  }
  let result = await userAddBlogCommentsService(blogId, commentsData.value);
  if (result.code == 1) {
    ElMessage({
      showClose: true,
      type: "success",
      message: result.message ? result.message : "发表评论成功",
      plain: true,
    });
    commentsData.value = "";
    getComments(blogId);
  } else {
    ElMessage({
      showClose: true,
      type: "error",
      message: result.message ? result.message : "发表评论失败",
      plain: true,
    });
    commentsData.value = "";
  }
};
const deleteComments = async (id) => {
  ElMessageBox.confirm("你确认删除该评论吗？", "温馨提示", {
    confirmButtonText: "确认",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      let result = await userDeleteBlogCommentsService(blogId, id);
      if (result.code === 1) {
        ElMessage({
          showClose: true,
          type: "success",
          message: result.message ? result.message : "删除成功",
          plain: true,
        });
        getComments(blogId);
      } else if (result.code === 0) {
        ElMessage({
          showClose: true,
          type: "error",
          message: result.message ? result.message : "删除失败",
          plain: true,
        });
      }
    })
    .catch(() => {
      ElMessage({
        showClose: true,
        message: "用户取消删除",
        plain: true,
      });
    });
};

onMounted(() => {
  getBlogDetail(blogId);
  getLatestBlog();
  getRelatedBlog();
  //getComments(blogId);
});

// 查看博客详情
const goToBlogDetail = (id) => {
  if (router.currentRoute.value.path === "/blogDetail") {
    router.go(0); // 刷新当前页面
    router.replace({ path: "/blogDetail", query: { id: id } }); // 重新给blogId赋值
  } else {
    router.push({ path: "/blogDetail", query: { id: id } });
  }
};
</script>

<template>
  <!-- breadcrumb -->
  <div class="container header-mt">
    <div class="row">
      <div class="col-12">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb custom-breadcrumb">
            <li class="breadcrumb-item">
              <router-link to="/index"> 主页 </router-link>
            </li>
            <li class="breadcrumb-item">
              <router-link to="/blog">
                <a href="">博客</a>
              </router-link>
            </li>
            <li class="breadcrumb-item active" aria-current="page">博客详情</li>
          </ol>
        </nav>
      </div>
    </div>
  </div>
  <!-- end breadcrumb -->
  <!-- main content -->
  <div class="main-content pb-80">
    <div class="container">
      <div class="row">
        <!-- sidebar -->
        <div class="col-lg-3 col-md-4 sidebar">
          <!-- related posts -->
          <div class="sidebar__item">
            <h3 class="sidebar__item--title">最新文章</h3>
            <ul
              class="media-list"
              v-for="item in latestBlogList"
              :key="item.id"
            >
              <li class="media-list__item">
                <div class="media">
                  <a href="javascript:void(0)">
                    <img
                      :src="item.image"
                      class="media-list__item--img"
                      alt="..."
                    />
                  </a>
                  <div class="media-body">
                    <a
                      href="javascript:void(0)"
                      @click="goToBlogDetail(item.id)"
                    >
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
          <!-- end related posts -->
        </div>
        <!-- sidebar -->
        <!-- single blog posts -->
        <div class="col-lg-9 col-md-8 single-post">
          <div class="single-post__img">
            <img :src="blogDetail.image" alt="" />
          </div>
          <h1 class="single-post__title">
            {{ blogDetail.title }}
          </h1>
          <div class="single-post__body">
            <p v-html="blogDetail.content"></p>
          </div>
          <div class="single-post__footer">
            <div class="post-details">
              <span class="author"
                >发布者:
                <a href="javascript:void(0)">{{ blogDetail.author }}</a></span
              >
              <a href="javascript:void(0)">{{ blogDetail.createTime }}</a>
            </div>
            <div class="share-post">
              <ul>
                <li>
                  <a href="javascript:void(0)" class="social-icon fb-icon"
                    ><i class="bi-facebook"></i
                  ></a>
                </li>
                <li>
                  <a href="javascript:void(0)" class="social-icon tr-icon"
                    ><i class="bi-tencent-qq"></i
                  ></a>
                </li>
                <li>
                  <a href="javascript:void(0)" class="social-icon pin-icon"
                    ><i class="bi-sina-weibo"></i
                  ></a>
                </li>
                <li>
                  <a href="javascript:void(0)" class="social-icon gr-icon"
                    ><i class="bi-wechat"></i
                  ></a>
                </li>
              </ul>
            </div>
          </div>
          <!-- <div class="single-post__tag">
            <span>标签:</span>
            <a href="javascript:void(0)" v-for="tag in tags" :key="tag">{{
              tag
            }}</a>
          </div> -->
          <!-- end single blog post -->
          <!-- related blog posts -->
          <div class="row mt-100">
            <div class="col-12">
              <h2 class="main-title wow fadeIn">作者相关文章</h2>
            </div>
            <div
              class="col-md-6"
              v-for="item in relatedBlogList"
              :key="item.id"
            >
              <div class="blog-post blog-post--grid blog-post--mb">
                <div class="blog-post__img">
                  <div class="blog-post__img--overlay"></div>
                  <img :src="item.image" alt="" />
                  <a
                    href=""
                    class="btn blog-post__btn"
                    @click="goToBlogDetail(item.id)"
                  >
                    查看详情
                  </a>
                </div>
                <div class="blog-post__inner">
                  <div class="blog-post__inner--title">
                    <h4>{{ item.title }}</h4>
                  </div>
                  <!-- <div class="blog-post__inner--details">
                    <p>
                      {{ item.content }}
                    </p>
                  </div> -->
                </div>
              </div>
            </div>
          </div>
          <!-- end related blog posts -->
          
          <!--  comments -->
          <!-- 引用评论组件 -->
          <Comment :module="'blog'" />

          <!-- <div class="comments mt-70">
          
            <div class="row">
              <div class="col-12">
                <h2 class="main-title">评论</h2>
                <div class="comments__list">
                  <ul class="media-list">
                    <li
                      class="media-list__item"
                      v-for="(item, index) in comments"
                      :key="item.id"
                      :class="{
                        'media-list__item--nested': (index + 1) % 2 == 0,
                      }"
                    >
                      <div class="media">
                        <img
                          :src="item.userImage"
                          class="media-list__item--img"
                          alt="avatar-img"
                        />
                        <div class="media-body">
                          <div class="media-list__item--content">
                            <div class="comment-details">
                              <span class="author">{{
                                item.createUserName
                              }}</span>
                              <span class="date">{{ item.createTime }}</span>
                            </div>
                            <p>
                              {{ item.comment }}
                            </p>
                          </div>
                          <a
                            class="media-list__item--icon"
                            href="javascript:void(0)"
                            v-if="item.createUser == userInfoStore.info.id"
                          >
                            <i
                              class="bi-trash-fill"
                              @click="deleteComments(item.id)"
                            ></i>
                          </a>
                        </div>
                      </div>
                    </li>
                  </ul>
                  <div>
                    <p v-if="comments.length == 0">暂无评论</p>
                  </div>
                </div>
              </div>
            </div>
            
            <form class="comments__form" @submit.prevent="addComments">
              <div class="row">
                <div class="col-12 form-group">
                  <label for="InputTextarea">评论</label>
                  <textarea
                    class="form-control comments__form--input"
                    id="InputTextarea"
                    rows="5"
                    v-model="commentsData"
                  ></textarea>
                </div>
                <div class="col-12 comments__form--btn">
                  <button type="submit" class="btn">发表评论</button>
                </div>
              </div>
            </form>
           
          </div> -->

          <!--  end comments -->
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
</template>


<style scoped>
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
</style>