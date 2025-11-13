<script setup>
    

</script>
<template>
  <!-- footer -->
  <footer class="footer">
    <div class="container" >
      <div class="footer__top-row">
        <div class="row">
          <div class="col-lg-5 col-md-6 footer__content">
            <div class="footer-logo footer_heading">
              <img src="@/assets/images/logo-footer.jpg" alt="" />
            </div>
            <div class="footer-list">
              <p >
                “文创购”(英文:WCG,亦称文创购物商城、文创商城),是一个综合性文创购物购物网站。“文创购”是长江师范学院计科2301陈韦江毕业设计。提供100%品质保证的商品,7天无理由退货的售后服务,以及购物积分返现等优质服务。
              </p>
              <ul>
                <li class="social-media__item">
                  <a href="javascript:void(0)"><i class="bi-tencent-qq"></i></a>
                </li>
                <li class="social-media__item">
                  <a href="javascript:void(0)"><i class="bi-sina-weibo"></i></a>
                </li>
                <li class="social-media__item">
                  <a href="javascript:void(0)"><i class="bi-wechat"></i></a>
                </li>
              </ul>
            </div>
          </div>
          <div class="col-lg-2 col-md-6 footer__content">
            <h5 class="footer-heading">快速连接</h5>
            <div class="footer-list">
              <ul>
                <li class="footer-list__item">
                  <router-link to="/index"> 
                    <a href="">回到主页</a>
                  </router-link>
                </li>
                <li class="footer-list__item">
                  <router-link to="/blog">
                    <a href="">博客文章</a>
                  </router-link>
                </li>
                <li class="footer-list__item">
                  <router-link to="/shop">
                    <a href="">商品列表</a>
                  </router-link>
                </li>
                <li class="footer-list__item">
                  <router-link to="/about">
                    <a href="">服务详情</a>
                  </router-link>
                </li>
              </ul>
            </div>
          </div>
          <div class="col-lg-3 col-md-6 footer__content">
            <h5 class="footer-heading">需要帮助</h5>
            <div class="footer-list">
              <ul>
                <li class="footer-list__item">
                  <router-link to="/contact">
                    <a href="">联系我们</a>
                  </router-link>
                </li>
                <li class="footer-list__item">
                  <router-link to="/about">
                    <a href="">隐私政策</a>
                  </router-link>
                </li>
                <li class="footer-list__item">
                  <router-link to="/about">
                    <a href="">付款和运输</a>
                  </router-link>
                </li>
              </ul>
            </div>
          </div>
          <div class="col-lg-2  col-md-6 footer__content">
            <h5 class="footer-heading">联系方式</h5>
            <div class="footer-list">
              <ul>
                <li class="footer-list__item">
                  <span><i class="bi-geo-alt-fill"></i></span>
                  <span
                    >&nbsp;重庆<br />
                    长江师范学院<br />
                    计科2301</span
                  >
                </li>
                <li class="footer-list__item">
                  <span><i class="bi-telephone-plus-fill"></i></span>
                  <span>&nbsp;123456789</span>
                </li>
                <li class="footer-list__item">
                  <span><i class="bi-envelope-fill"></i></span>
                  <span>1774532899@qq.com</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="col-lg-12">
          <div class="copyright">
            <p>&#169; copyright 2025. Designed with love by ChenWeiJiang</p>
          </div>
        </div>
      </div>
    </div>
  </footer>
  <!-- end footer -->
</template>

<style scoped>
@import '@/assets/main.css';
a {
  text-decoration: none;
}


</style>