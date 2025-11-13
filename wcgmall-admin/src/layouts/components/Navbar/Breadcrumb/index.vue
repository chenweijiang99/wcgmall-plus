<template>
  <el-breadcrumb separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="item.path">
        <span 
          v-if="index === breadcrumbs.length - 1" 
          class="no-redirect breadcrumb-item"
        >
        <el-icon>
          <component :is="item.meta?.icon" /> 
        </el-icon>
        {{ item.meta?.title }}
      </span>
        <a 
          v-else 
          class="redirect breadcrumb-item" 
          @click.prevent="handleLink(item)"
        >
        <el-icon>
          <component :is="item.meta?.icon" /> 
        </el-icon>
        {{ item.meta?.title }}
      </a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { useRoute, useRouter, RouteLocationMatched } from 'vue-router'

const route = useRoute()
const router = useRouter()

const breadcrumbs = ref<RouteLocationMatched[]>([])

const getBreadcrumb = () => {
  let matched = route.matched.filter(item => item.meta && item.meta.title)
  const first = matched[0]
  if (first && first.path !== '/dashboard') {
    matched = [{ path: '/dashboard', meta: { title: '首页',icon: 'Orange' } } as RouteLocationMatched].concat(matched)
  }
  breadcrumbs.value = matched
}

const handleLink = (item: RouteLocationMatched) => {
  router.push(item.path)
}

watch(
  () => route.path,
  () => getBreadcrumb(),
  { immediate: true }
)
</script>

<style scoped>
.redirect {
  color: #666;
  cursor: pointer;
}

.redirect:hover {
  color: #409EFF;
}

.no-redirect {
  color: #97a8be;
  cursor: text;
}

.breadcrumb-enter-active,
.breadcrumb-leave-active {
  transition: all 0.5s;
}

.breadcrumb-enter-from,
.breadcrumb-leave-active {
  opacity: 0;
  transform: translateX(20px);
}

.breadcrumb-leave-active {
  position: absolute;
}

.breadcrumb-item{
  display: flex;
  align-items: center;
  .el-icon{
    margin-right: 3px;
  }
}
</style> 