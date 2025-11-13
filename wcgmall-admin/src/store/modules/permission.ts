import { RouteRecordRaw } from "vue-router";
import { constantRoutes } from "@/router";
import { store } from "@/store";
import { getRouters } from "@/api/system/auth";
import { defineStore } from "pinia";
import { ref } from "vue";
const modules = import.meta.glob("../../views/**/**.vue");
import ParentView from '@/components/ParentView/index.vue'

const Layout = () => import("@/layouts/index.vue");

/**
 * 递归过滤有权限的异步(动态)路由
 *
 * @param routes 接口返回的异步(动态)路由
 * @param roles 用户角色集合
 * @returns 返回用户有权限的异步(动态)路由
 */
const filterAsyncRoutes = (routes: RouteRecordRaw[], isRoot = true) => {
  const asyncRoutes: any[] = [];

  routes.forEach((route) => {

    const tmpRoute = { ...route }; // ES6扩展运算符复制新对象
    
    if (!route.name) {
      tmpRoute.name = route.name;
    }

    if(tmpRoute.component) {
      if (tmpRoute.component?.toString() == "Layout") {
        tmpRoute.component = Layout;
      } else if (tmpRoute.component === 'ParentView') {
        tmpRoute.component = ParentView
      }  else {
        const component = modules[`../../views${tmpRoute.component}.vue`];
        if (component) {
          tmpRoute.component = component;
        } 
      }

      if (tmpRoute.children) {
        // 递归处理子路由时，传入 false 表示不是根级路由
        tmpRoute.children = filterAsyncRoutes(tmpRoute.children, false);
      }
    }
    asyncRoutes.push(tmpRoute);

  });

  // 只在处理根级路由时添加404路由
  if (isRoot) {
    asyncRoutes.push({
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/error-page/404.vue'),
      meta: {
        title: '404',
        hidden: true
      }
    });
  }

  return asyncRoutes;
};

// setup
export const usePermissionStore = defineStore("permission", () => {
  // state
  const routes = ref<RouteRecordRaw[]>([]);

  // actions
  function setRoutes(newRoutes: RouteRecordRaw[]) {
    routes.value = constantRoutes.concat(newRoutes);
  }
  /**
   * 生成动态路由
   *
   * @param roles 用户角色集合
   * @returns
   */
  function generateRoutes() {
    return new Promise<RouteRecordRaw[]>((resolve, reject) => {
      // 接口获取所有路由
      getRouters()
        .then(({ data: asyncRoutes }) => {
          // 根据角色获取有访问权限的路由
          const accessedRoutes = filterAsyncRoutes(asyncRoutes);
          setRoutes(accessedRoutes);
          resolve(accessedRoutes);
        })
        .catch((error) => {
          reject(error);
        });
    });
  }
  /**
   * 获取与激活的顶部菜单项相关的混合模式左侧菜单集合
   */
  const mixLeftMenus = ref<RouteRecordRaw[]>([]);
  function setMixLeftMenus(topMenuPath: string) {
    const matchedItem = routes.value.find((item) => item.path === topMenuPath);
    if (matchedItem && matchedItem.children) {
      mixLeftMenus.value = matchedItem.children;
    }
  }
  return {
    routes,
    setRoutes,
    generateRoutes,
    mixLeftMenus,
    setMixLeftMenus,
  };
});

// 非setup
export function usePermissionStoreHook() {
  return usePermissionStore(store);
}
