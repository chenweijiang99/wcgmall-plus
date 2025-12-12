import router from '@/router'
import { usePermissionStore } from '@/store/modules/permission'
import { useUserStore, useSettingsStore } from '@/store'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
NProgress.configure({ showSpinner: false })

const whiteList = ['/login'] // 路由白名单

export function setupPermission() {
  router.beforeEach(async (to, from, next) => {
    const settingsStore = useSettingsStore()

    // 获取网站配置
    await settingsStore.fetchSiteConfig()

    // 动态设置页面标题
    const pageTitle = to.meta.title as string
    const siteTitle = settingsStore.adminTitle
    if (pageTitle) {
      document.title = `${pageTitle} - ${siteTitle}`
    } else {
      document.title = siteTitle
    }

    NProgress.start();
    const hasToken = getToken();
    
    if (hasToken) {
      if (to.path === "/login") {
        // 如果已登录，跳转首页
        next({ path: "/" });
        NProgress.done();
      } else {
        const userStore = useUserStore();
        const permissionStore = usePermissionStore();
        
        // 判断是否已经获取过用户信息
        if (!userStore.user.nickname) {
          try {
            // 获取用户信息
            await userStore.getUserInfo();
            // 生成可访问路由
            const accessRoutes = await permissionStore.generateRoutes();
            // 添加路由前检查和打印日志
            if (Array.isArray(accessRoutes) && accessRoutes.length > 0) {
              accessRoutes.forEach((route: any) => {
                if (route && typeof route === 'object') {
                  if(route.meta?.isExternal) {
                    return;
                  }
                  router.addRoute(route);
                } else {
                  console.error('Invalid route object:', route);
                }
              });
              // 使用 replace 进行重定向，确保路由添加完成
              next({ ...to, replace: true });
            } else {
              console.error('No valid routes generated');
              next();
            }
          } catch (error) {
            console.error('Permission error:', error);
            // 移除 token 并跳转登录页
            await userStore.resetToken();
            next(`/login`);
            NProgress.done();
          }
        } else {
          // 已经有用户信息，直接放行
          next();
        }
      }
    } else {
      // 未登录可以访问白名单页面
      if (whiteList.indexOf(to.path) !== -1) {
        next();
      } else {
        next(`/login`);
        NProgress.done();
      }
    }
  });

  router.afterEach(() => {
    NProgress.done();
    setTimeout(() => {
    }, 300)
  });
}