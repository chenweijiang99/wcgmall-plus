<script setup>
import { computed, watch, ref, onMounted } from "vue";
import headerView from "../components/header.vue";
import footerView from "../components/footer.vue";
import { useScrollToTop } from "@/assets/base.js";
import { ElMessageBox } from "element-plus";
const { showLoader,backtopStyle,handleMouseEnter,handleMouseLeave} = useScrollToTop();

const backgroundImg = new URL('@/assets/images/banners/banner-bg-05.jpg',import.meta.url).href
</script>

<template>
  <!-- <headerView /> -->
  <!-- breadcrumb -->
  <div class="container header-mt">
    <div class="row">
      <div class="col-12">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb custom-breadcrumb">
            <li class="breadcrumb-item"><router-link to="/index"> 主页 </router-link></li>
            <li class="breadcrumb-item active" aria-current="page">联系</li>
          </ol>
        </nav>
      </div>
    </div>
  </div>
  <!-- end breadcrumb -->
  <!-- main content -->
  <div class="main-content pb-80">
    <div class="container">
      <!-- contact form -->
      <div class="row" id="contactForm">
        <div class="col-lg-6">
          <div class="custom-form custom-form--box">
            <h3 class="custom-form__title">我们很乐意听取您的意见</h3>
            <form>
              <div class="form-group custom-form__input">
                <label for="identity">姓名</label>
                <input
                  type="text"
                  class="form-control"
                  id="identity"
                  placeholder=""
                />
              </div>
              <div class="form-group custom-form__input">
                <label for="email">邮箱</label>
                <input
                  type="email"
                  class="form-control ltr"
                  id="email"
                  placeholder=""
                />
              </div>
              <div class="form-group custom-form__input">
                <label for="phone">电话</label>
                <input
                  type="text"
                  class="form-control ltr"
                  id="phone"
                  placeholder=""
                />
              </div>
              <div class="form-group custom-form__input">
                <label for="textareaInput">消息</label>
                <textarea
                  class="form-control"
                  id="textareaInput"
                  rows="6"
                  placeholder=""
                ></textarea>
              </div>
              <div class="custom-form__btn">
                <button type="submit" class="btn submit-btn">提交</button>
              </div>
            </form>
          </div>
        </div>
        <!-- end contact form -->
        <!-- contact information -->
        <div class="col-lg-6">
          <div class="contact-info">
            <h2 class="contact-info__title">联系信息</h2>
            <ul class="contact-info__list">
              <li class="contact-info__list--item">
                <strong>地址 :</strong>
                <span
                  >长江师范学院</span
                >
              </li>
              <li class="contact-info__list--item">
                <strong>电话 :</strong>
                <span>18222222222</span>
              </li>
              <li class="contact-info__list--item">
                <strong>邮箱 :</strong>
                <span>1774532899@qq.com</span>
              </li>
            </ul>
            <ul class="contact-info__social">
              <li>
                <a href="javascript:void(0)"><i class="bi-tencent-qq"></i></a>
              </li>
              <li>
                <a href="javascript:void(0)"><i class="bi-wechat"></i></a>
              </li>
              <li>
                <a href="javascript:void(0)"><i class="bi-sina-weibo"></i></a>
              </li>
            </ul>
          </div>
          <!-- google map -->
          <div class="location">
           <baidu-map
           class="map"
            :center="{lng:107.270382, lat:29.755593}"
            :zoom="15">
            <bm-copyright anchor="BMAP_ANCHOR_TOP_LEFT" :copyright="[{ id: 1, content: '文创购物商城' },]"></bm-copyright>
            <bm-marker :position="{lng:107.270382, lat:29.755593}" ></bm-marker>
           </baidu-map>
          </div>
          <!-- end google map -->
        </div>
        <!-- end contact information -->
      </div>
    </div>
    <div
      class="container-fluid newsletter-banner mt-100"
      :style="{'background-image': 'url('+backgroundImg+' )'}"
    >
      <div class="banner__overlay"></div>
      <div class="container">
        <div class="newsletter-banner__content">
          <h3>随时加入我们</h3>
          <span>立即订阅</span>
          <form>
            <div class="form-row">
              <div class="col-lg-4 col-md-6 col-sm-8 col-10 mx-auto">
                <div class="input-group ltr">
                  <input
                    type="email"
                    class="form-control"
                    id="subscribeInputEmail"
                    placeholder="输入您的邮箱地址"
                    aria-describedby="Emailsubscribe"
                  />
                  <div class="input-group-append">
                    <button class="btn" type="submit" id="Emailsubscribe">
                      订阅
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </form>
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

<style scoped>
@import "@/assets/main.css";
a {
  text-decoration: none;
}
.map{
  height: 400px;
  width: 100%;
}
</style>