<template>
  <div class="sidebar-container" :class="settingsStore.sidebarStyle">
    <div
      v-if="settingsStore.showLogo"
      class="logo-container"
      :class="{ dark: settingsStore.theme === 'dark' }"
    >
      <Logo :size="32" class="logo-icon" :color="settingsStore.themeColor" />
      <span
        v-show="!isCollapse"
        class="logo-text"
        :class="{
          light:
            settingsStore.theme === 'dark'
              ? false
              : settingsStore.sidebarStyle === 'light',
        }"
        >{{ settingsStore.adminTitle }}</span
      >
    </div>
    <el-scrollbar>
      <el-menu
        style="height: 100%"
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="
          settingsStore.theme === 'dark'
            ? '#1d1e1f'
            : settingsStore.sidebarStyle === 'light'
            ? '#ffffff'
            : '#304156'
        "
        :text-color="
          settingsStore.theme === 'dark'
            ? '#bfcbd9'
            : settingsStore.sidebarStyle === 'light'
            ? '#303133'
            : '#bfcbd9'
        "
        :active-text-color="settingsStore.themeColor"
        :collapse-transition="false"
        @select="handleSelect"
        :unique-opened="true"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <menu-item v-if="!route.meta?.hidden" :route="route" :base-path="route.path" />
        </template>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { usePermissionStore } from "@/store/modules/permission";
import { useSettingsStore } from "@/store/modules/settings";
import Logo from "@/layouts/components/Sidebar/Logo.vue";
import { isExternal } from "@/utils/validate";
import MenuItem from "./MenuItem.vue";

const route = useRoute();
const permissionStore = usePermissionStore();
const settingsStore = useSettingsStore();
const router = useRouter();

// 加载网站配置
onMounted(() => {
  settingsStore.fetchSiteConfig();
});

// 从 props 接收折叠状态
defineProps({
  isCollapse: {
    type: Boolean,
    default: false,
  },
});

// 获取路由菜单
const menuRoutes = computed(() => {
  const routes = permissionStore.routes;
  return routes.map((route) => {
    // 如果是根路由且包含 dashboard 子路由
    if (route.path === "/" && route.children) {
      const dashboardRoute = route.children.find((child) => child.path === "dashboard");
      if (dashboardRoute) {
        // 将 dashboard 提升为一级路由
        return {
          path: "/dashboard",
          ...dashboardRoute,
          children: undefined,
        };
      }
    }
    return route;
  });
});
// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route;
  if (meta?.activeMenu) {
    return meta.activeMenu;
  }
  return path;
});

// 修改路径处理函数
const resolvePath = (routePath: string) => {
  // 如果是外部链接，直接返回原路径
  if (isExternal(routePath)) {
    return routePath;
  }

  // 如果是根路径，直接返回
  if (routePath === "/") return routePath;

  // 移除开头和结尾的斜杠
  routePath = routePath.replace(/^\/+|\/+$/g, "");

  // 如果是仪表盘路径，特殊处理
  if (routePath === "dashboard") {
    return "/dashboard";
  }

  // 其他路径，确保只有一个斜杠
  return "/" + routePath;
};

// 添加 select 事件处理函数
const handleSelect = (index: string) => {
  if (isExternal(index)) {
    window.open(index, "_blank");
    return;
  }

  // 内部路由跳转
  if (route.path !== index) {
    router.push(index);
  }
};
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%;
  background-color: v-bind(
    'settingsStore.theme === "dark" ? "#1d1e1f" : (settingsStore.sidebarStyle === "light" ? "#ffffff" : "#304156")'
  );

  .logo-container {
    height: 60px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    background-color: v-bind(
      'settingsStore.theme === "dark" ? "#1d1e1f" : (settingsStore.sidebarStyle === "light" ? "#ffffff" : "#304156")'
    );

    .logo-icon {
      flex-shrink: 0;
    }

    .logo-text {
      color: #fff;
      font-size: 18px;
      margin-left: 12px;
      font-weight: 600;
      white-space: nowrap;

      &.light {
        color: #303133;
      }
    }
  }

  :deep(.el-menu) {
    border-right: none;

    // 一级菜单样式
    .el-menu-item,
    .el-sub-menu__title {
      height: 56px;
      line-height: 56px;

      .el-icon {
        width: 24px;
        text-align: center;
        font-size: 18px;
        margin-right: 12px;
      }
    }

    // 激活状态
    .el-menu-item.is-active {
      background-color: v-bind("`${settingsStore.themeColor}1a`");
      &::before {
        content: "";
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 4px;
        height: 20px;
        background-color: v-bind("settingsStore.themeColor");
        border-radius: 0 4px 4px 0;
      }
    }

    // 悬停效果
    .el-menu-item:hover,
    .el-sub-menu__title:hover {
      background-color: v-bind("`${settingsStore.themeColor}0a`");
    }
  }
}

// 折叠状态样式
:deep(.el-menu--collapse) {
  width: 64px;

  .el-menu-item,
  .el-sub-menu__title {
    padding: 0 20px !important;

    .el-icon {
      margin: 0 !important;
      width: 24px !important;
      text-align: center;
    }
  }

  // 隐藏文字和箭头
  .el-sub-menu__title span,
  .el-menu-item span,
  .el-sub-menu__title .el-sub-menu__icon-arrow {
    display: none;
  }
}

:deep(.el-scrollbar__view) {
  height: 100% !important;
}

// 子菜单样式
:deep(.el-menu .el-menu) {
  background-color: v-bind(
    'settingsStore.theme === "dark" ? "#181818" : (settingsStore.sidebarStyle === "light" ? "#ffffff" : "#263445")'
  );
  .el-menu-item {
    height: 50px;
    line-height: 50px;

    &.is-active {
      background-color: v-bind("`${settingsStore.themeColor}1a`");
    }
  }
}
</style>
