import { createRouter, createWebHistory } from 'vue-router'
import layoutVue from '@/views/layout.vue'
import loginView from '../views/login.vue'
import indexView from '../views/index.vue'
import shopView from '../views/shop.vue'
import cartView from '../views/cart.vue'
import blogView from '../views/blog.vue'
import resetPasswordView from '../views/resetPassword.vue'
import registerView from '../views/register.vue'
import accountView from '../views/account.vue'
import aboutView from '../views/about.vue'
import contactView from '../views/contact.vue'
import checkoutView from '../views/checkout.vue'
import activateView from '../views/activate.vue'
import purchaseNotifView from '@/views/purchaseNotif.vue'
import orderdetailView from '@/views/orderDetail.vue'
import userBlogView from '@/views/userBlog.vue'
import blogDetailView from '@/views/blogDetail.vue'
import productDetailView from '@/views/productDetail.vue'
import searchView from '@/views/search.vue'
import testView from '@/views/test.vue'
const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            component: layoutVue,
            redirect: '/login',
            children: [
                {
                    path: '/index',
                    component: indexView
                },
                {
                    path: '/login',
                    component: loginView
                },
                {
                    path: '/shop',
                    component: shopView
                },
                {
                    path: '/cart',
                    component: cartView
                },
                {
                    path: '/blog',
                    component: blogView,

                },
                {
                    path: '/resetPassword',
                    component: resetPasswordView
                },
                {
                    path: '/register',
                    component: registerView
                },
                {
                    path: '/account',
                    component: accountView,
                },
                {
                    path: '/about',
                    component: aboutView
                },
                {
                    path: '/contact',
                    component: contactView
                },
                {
                    path: '/checkout',
                    component: checkoutView
                },
                {
                    path: '/activate',
                    component: activateView
                },
                {
                    path: '/purchaseNotif',
                    component: purchaseNotifView
                },
                {
                    path: '/orderDetail',
                    component: orderdetailView
                },
                {
                    path: '/userBlog',
                    component: userBlogView
                },
                {
                    path: '/blogDetail',
                    component: blogDetailView
                },
                {
                    path: '/productDetail',
                    component: productDetailView
                },
                {
                    path: '/search',
                    component: searchView
                },
                {
                    path: '/test',
                    component: testView
                }
            ]
        },
    ],
    scrollBehavior(to, from, savedPosition) {
        // to 是即将进入的路由对象
        // from 是即将离开的路由对象
        // savedPosition 是页面之前的滚动位置
    
        // 返回期望的滚动位置
        return { 
            top: 0,
            behavior: 'smooth'
        }
      }
})

export default router
