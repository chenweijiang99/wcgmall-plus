<template>
  <template v-if="isLeafOrDashboard">
    <app-link v-if="route.meta" :to="resolvePath(route.path)">
      <el-menu-item :index="resolvePath(route.path)">
        <el-icon v-if="route.meta?.icon">
          <component :is="route.meta.icon" />
        </el-icon>
        <template #title>{{ route.meta?.title }}</template>
      </el-menu-item>
    </app-link>
  </template>
  <template v-else>
    <el-sub-menu :index="resolvePath(route.path)">
      <template #title>
        <el-icon v-if="route.meta?.icon">
          <component :is="route.meta.icon" />
        </el-icon>
        <span>{{ route.meta?.title }}</span>
      </template>
      <template v-for="child in route.children" :key="child.path">
        <menu-item
          v-if="!child.meta?.hidden"
          :route="child"
          :base-path="resolvePath(route.path)"
        />
      </template>
    </el-sub-menu>
  </template>
</template>

<script setup lang="ts">
import { computed } from "vue";
import AppLink from "./Link.vue";
import { isExternal } from "@/utils/validate";
import type { RouteRecordRaw } from "vue-router";

const props = defineProps<{
  route: any;
  basePath: string;
}>();

const isLeafOrDashboard = computed(() => {
  return (
    !props.route.children ||
    props.route.children.length === 0 ||
    props.route.path === "/dashboard"
  );
});

const resolvePath = (routePath: string) => {
  // 如果是外部链接，直接返回
  if (isExternal(routePath)) {
    return routePath;
  }

  // 如果是根路径，直接返回
  if (routePath === "/") {
    return routePath;
  }

  // 如果是绝对路径（以/开头），直接返回
  if (routePath.startsWith("/")) {
    return routePath;
  }

  // 如果是仪表盘路径，特殊处理
  if (routePath === "dashboard") {
    return "/dashboard";
  }

  // 处理相对路径
  const path =
    props.basePath === "/" ? `/${routePath}` : `${props.basePath}/${routePath}`;

  // 规范化路径，去除多余的斜杠
  return path.replace(/\/+/g, "/");
};
</script>
