import { createRouter, createWebHashHistory, RouteRecordRaw ,createWebHistory} from 'vue-router';
import Home from '@/views/Home.vue';
import Shop from '@/views/Shop.vue';
import ProductDetail from '@/views/ProductDetail.vue';
import Cart from '@/views/Cart.vue';
import Checkout from '@/views/Checkout.vue';
import Blog from '@/views/Blog.vue';
import BlogDetail from '@/views/BlogDetail.vue';
import CreateBlog from '@/views/CreateBlog.vue';
import Profile from '@/views/Profile.vue';
import Login from '@/views/Login.vue';
import PayClose from '@/views/PayClose.vue';
import { useSiteConfigStore } from '@/stores/modules/siteConfig';

// 定义路由表
const routes: RouteRecordRaw[] = [
  { path: '/', component: Home, meta: { title: '首页' } },
  { path: '/shop', component: Shop, meta: { title: '商品' } },
  { path: '/product/:id', component: ProductDetail, meta: { title: '商品详情' } },
  { path: '/cart', component: Cart, meta: { title: '购物车' } },
  { path: '/checkout', component: Checkout, meta: { title: '结算' } },
  { path: '/blog', component: Blog, meta: { title: '博客' } },
  { path: '/blog/:id', component: BlogDetail, meta: { title: '博客详情' } },
  { path: '/create-blog', component: CreateBlog, meta: { title: '发布博客' } },
  { path: '/profile', component: Profile, meta: { title: '个人中心' } },
  { path: '/login',name: 'Login', component: Login, meta: { hideLayout: true, title: '登录' }  },
  { path: '/payClose',name: 'PayClose', component: PayClose, meta: { hideLayout: true, title: '支付' }  },
];

// 创建路由实例
const router = createRouter({
  // history: createWebHashHistory(), // 使用 Hash 模式
  history: createWebHistory(), // 使用 History 模式
  routes,
  // 路由切换时滚动到顶部
   scrollBehavior: () => ({ left: 0, top: 0 }),
});

// 路由守卫 - 动态设置页面标题
router.beforeEach(async (to, from, next) => {
  const siteConfigStore = useSiteConfigStore()

  // 获取网站配置
  await siteConfigStore.fetchConfig()

  // 动态设置页面标题
  const pageTitle = to.meta.title as string
  const siteTitle = siteConfigStore.userTitle || 'WCG Store'
  if (pageTitle) {
    document.title = `${pageTitle} - ${siteTitle}`
  } else {
    document.title = siteTitle
  }

  next()
})

export default router;