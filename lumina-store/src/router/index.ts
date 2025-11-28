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

// 定义路由表
const routes: RouteRecordRaw[] = [
  { path: '/', component: Home },
  { path: '/shop', component: Shop },
  { path: '/product/:id', component: ProductDetail },
  { path: '/cart', component: Cart },
  { path: '/checkout', component: Checkout },
  { path: '/blog', component: Blog },
  { path: '/blog/:id', component: BlogDetail },
  { path: '/create-blog', component: CreateBlog },
  { path: '/profile', component: Profile },
  { path: '/login',name: 'Login', component: Login, meta: { hideLayout: true }  },
];

// 创建路由实例
const router = createRouter({
  // history: createWebHashHistory(), // 使用 Hash 模式
  history: createWebHistory(), // 使用 History 模式
  routes,
  // 路由切换时滚动到顶部
   scrollBehavior: () => ({ left: 0, top: 0 }),
});

export default router;