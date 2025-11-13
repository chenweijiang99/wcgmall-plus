<template>
  <div class="tags-view-container" :class="settingsStore.tagsStyle">
    <el-scrollbar ref="scrollbarRef" class="tags-view-wrapper" wrap-class="scrollbar-wrapper">
      <router-link
        v-for="tag in tagsViewStore.visitedViews"
        :key="tag.path"
        :class="isActive(tag) ? 'active' : ''"
        class="tags-view-item"
        :to="{ path: tag.path, query: tag.query }"
        @contextmenu.prevent="openMenu($event, tag)"
      >
        <el-icon v-if="tag.meta?.icon" class="tag-icon">
          <component :is="tag.meta.icon" />
        </el-icon>
        {{ tag.meta?.title }}
        <el-icon 
          v-if="!isAffix(tag)" 
          class="close-icon"
          @click.prevent.stop="closeSelectedTag(tag)"
        >
          <Close />
        </el-icon>
      </router-link>
    </el-scrollbar>

    <!-- 右键菜单 -->
    <ul v-show="visible" :style="{ left: left + 'px', top: top + 'px' }" class="contextmenu">
      <li @click="refreshSelectedTag(selectedTag)">
        <el-icon><Refresh /></el-icon>
        <span>刷新页面</span>
      </li>
      <li v-if="!isAffix(selectedTag)" @click="closeSelectedTag(selectedTag)">
        <el-icon><Close /></el-icon>
        <span>关闭当前</span>
      </li>
      <li @click="closeLeftTags(selectedTag)">
        <el-icon><ArrowLeft /></el-icon>
        <span>关闭左侧</span>
      </li>
      <li @click="closeRightTags(selectedTag)">
        <el-icon><ArrowRight /></el-icon>
        <span>关闭右侧</span>
      </li>
      <li @click="closeOthersTags(selectedTag)">
        <el-icon><CircleClose /></el-icon>
        <span>关闭其他</span>
      </li>
      <li @click="closeAllTags(selectedTag)">
        <el-icon><FolderDelete /></el-icon>
        <span>关闭所有</span>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { 
  Refresh, 
  Close, 
  ArrowLeft,
  ArrowRight,
  CircleClose,
  FolderDelete 
} from '@element-plus/icons-vue'
import { useTagsViewStore } from '@/store/modules/tagsView'
import { useSettingsStore } from '@/store/modules/settings'

const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()
const settingsStore = useSettingsStore()

const visible = ref(false)
const left = ref(0)
const top = ref(0)
const selectedTag = ref<any>(null)

const isActive = (tag: any) => {
  return tag.path === route.path
}

const isAffix = (tag: any) => {
  return tag?.meta?.affix
}

// 关闭选中标签
const closeSelectedTag = (view: any) => {
  tagsViewStore.delVisitedView(view)
  if (isActive(view)) {
    toLastView()
  }
}

// 关闭其他标签
const closeOthersTags = (view: any) => {
  tagsViewStore.delOthersViews(view)
  if (!isActive(view)) {
    router.push(view.path)
  }
}

// 关闭所有标签
const closeAllTags = (view: any) => {
  tagsViewStore.delAllViews()
  if (!isAffix(view)) {
    toLastView()
  }
}

// 关闭左侧标签
const closeLeftTags = (view: any) => {
  tagsViewStore.delLeftViews(view)
  if (!isActive(view)) {
    router.push(view.path)
  }
}

// 关闭右侧标签
const closeRightTags = (view: any) => {
  tagsViewStore.delRightViews(view)
  if (!isActive(view)) {
    router.push(view.path)
  }
}

// 刷新选中标签
const refreshSelectedTag = (view: any) => {
  // 先删除缓存
  tagsViewStore.delCachedView(view)
  
  const { fullPath, query, path } = view
  const redirectPath = '/redirect' + (fullPath || path)
  router.replace({
    path: redirectPath,
    query
  })
}

// 跳转到最后一个标签
const toLastView = () => {
  const visitedViews = tagsViewStore.visitedViews
  const latestView = visitedViews[visitedViews.length - 1]
  if (latestView?.path) {
    router.push(latestView.path)
  } else {
    router.push('/')
  }
}

// 打开右键菜单
const openMenu = (e: MouseEvent, tag: any) => {
  const menuMinWidth = 105
  const offsetLeft = document.documentElement.clientWidth - e.clientX
  const offsetTop = document.documentElement.clientHeight - e.clientY
  visible.value = true
  selectedTag.value = tag
  left.value = offsetLeft > menuMinWidth ? e.clientX : e.clientX - menuMinWidth
  top.value = offsetTop > 36 ? e.clientY : e.clientY - 36
}

// 关闭右键菜单
const closeMenu = () => {
  visible.value = false
}

// 监听路由变化
watch(
  () => route.path,
  () => {
    tagsViewStore.addVisitedView(route)
  }
)

const scrollbarRef = ref()

// 添加鼠标滚轮处理函数
const handleScroll = (e: WheelEvent) => {
  const delta = e.deltaY || e.detail
  
  if (scrollbarRef.value) {
    // 使用 Element Plus scrollbar 的 scrollTo 方法
    const scrollbar = scrollbarRef.value
    const currentScroll = scrollbar.wrapRef.scrollLeft
    const scrollStep = 120
    
    scrollbar.wrapRef.scrollTo({
      left: currentScroll + (delta > 0 ? scrollStep : -scrollStep),
      behavior: 'smooth'
    })
    
    e.preventDefault()
  }
}

onMounted(() => {
  document.addEventListener('click', closeMenu)
  tagsViewStore.addVisitedView(route)
  // 直接在 scrollbar 容器上添加事件监听
  scrollbarRef.value?.$el?.addEventListener('wheel', handleScroll, { passive: false })
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeMenu)
  // 移除事件监听
  scrollbarRef.value?.$el?.removeEventListener('wheel', handleScroll)
})
</script>

<style lang="scss" scoped>
.tags-view-container {
  height: 34px;
  width: 100%;
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, .12), 0 0 3px 0 rgba(0, 0, 0, .04);
  position: relative;
  z-index: 10;
}

.tags-view-container.dark {
  background: var(--el-bg-color-overlay);
  border-color: var(--el-border-color);

  .tags-view-item {
    background: var(--el-bg-color-overlay);
    border-color: var(--el-border-color);
    color: var(--el-text-color-regular);

    &:hover {
      color: var(--el-color-primary);
    }

    &.active {
      background-color: var(--el-color-primary);
      border-color: var(--el-color-primary);
      color: #fff;
    }
  
  }

  .contextmenu {
    background: var(--el-bg-color-overlay);
    border: 1px solid var(--el-border-color);
    color: var(--el-text-color-regular);

    li:hover {
      background: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
    }
  }

  .close-icon {
    color: var(--el-text-color-secondary);

    &:hover {
      color: var(--el-color-primary);
    }
  }
}

.tags-view-wrapper {
  height: 34px;
  width: 100%;
  
  :deep(.scrollbar-wrapper) {
    height: 34px;
    white-space: nowrap;
    overflow-y: hidden !important;
  }

  :deep(.el-scrollbar__wrap) {
    height: 34px;
  }

  :deep(.el-scrollbar__view) {
    height: 34px;
    display: inline-flex;
    align-items: center;
    flex-wrap: nowrap;
    padding: 0 4px;
  }

  .tags-view-item {
    display: inline-flex;
    align-items: center;
    height: 24px;
    line-height: 24px;
    margin: 4px 2px;
    padding: 0 8px;
    border: 1px solid var(--el-border-color-light);
    border-radius: 3px;
    font-size: 12px;
    color: var(--el-text-color-regular);
    background: var(--el-bg-color);
    text-decoration: none;
    user-select: none;
    transition: all 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
    position: relative;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;

    &:first-of-type {
      margin-left: 5px;
    }

    &:last-of-type {
      margin-right: 5px;
    }

    &:hover {
      color: var(--el-color-primary);
      border-color: var(--el-color-primary-light-5);
      background-color: var(--el-color-primary-light-9);
    }

    &.active {
      color: v-bind('settingsStore.themeColor');
      background-color: v-bind('`${settingsStore.themeColor}15`');
      border-color: v-bind('settingsStore.themeColor');
      font-weight: bold;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 16px;
        background-color: v-bind('settingsStore.themeColor');
        border-radius: 0 2px 2px 0;
      }
    }
  }
}

.tag-icon {
  margin-right: 4px;
  width: 14px;
  height: 14px;
  color: inherit;
}

.close-icon {
  margin-left: 4px;
  width: 14px;
  height: 14px;
  padding: 1px;
  border-radius: 50%;
  box-sizing: content-box;
  transition: all 0.2s;
  
  &:hover {
    background-color: rgba(0, 0, 0, 0.1);
    transform: scale(1.1);
  }
}

.contextmenu {
  margin: 0;
  background: var(--el-bg-color);
  position: fixed;
  list-style-type: none;
  padding: 6px 0;
  border-radius: 4px;
  font-size: 13px;
  color: var(--el-text-color-regular);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid var(--el-border-color-lighter);

  li {
    margin: 0;
    padding: 8px 16px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
    }
  }
}

.contextmenu li {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.contextmenu li .el-icon {
  margin-right: 8px;
  width: 14px;
  height: 14px;
}

.contextmenu li span {
  flex: 1;
}

// 卡片式样式（Chrome风格）
.card {
  background-color: var(--el-bg-color);
  padding: 6px 8px 0;
  border-bottom: 1px solid #dcdfe6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  
  .tags-view-wrapper {
    .tags-view-item {
      position: relative;
      height: 32px;
      margin: 0 -5px;
      padding: 0 24px;
      border: none;
      border-radius: 8px 8px 0 0;
      background: var(--el-fill-color-light);
      transition: all 0.2s ease;
      min-width: 100px;
      max-width: 160px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      z-index: 1;
      
      &::before,
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        width: 8px;
        height: 8px;
        background: transparent;
        transition: all 0.2s;
      }
      
      &::before {
        left: -8px;
        border-radius: 0 0 8px 0;
        box-shadow: 4px 4px 0 4px var(--el-fill-color-light);
      }
      
      &::after {
        right: -8px;
        border-radius: 0 0 0 8px;
        box-shadow: -4px 4px 0 4px var(--el-fill-color-light);
      }
      
      &:hover {
        background: var(--el-fill-color);
        z-index: 2;
        
        &::before {
          box-shadow: 4px 4px 0 4px var(--el-fill-color);
        }
        
        &::after {
          box-shadow: -4px 4px 0 4px var(--el-fill-color);
        }
      }
      
      &.active {
        background: #fff;
        color: v-bind('settingsStore.themeColor');
        font-weight: 500;
        z-index: 3;
        margin-bottom: -1px;
        padding-bottom: 1px;
        
        &::before {
          box-shadow: 4px 4px 0 4px #fff;
        }
        
        &::after {
          box-shadow: -4px 4px 0 4px #fff;
        }

        .tag-icon {
          color: inherit;
        }
      }
    }
  }

  // 暗色主题适配
  :root[data-theme='dark'] & {
    background: #202124;
    border-bottom-color: var(--el-border-color-dark);
    
    .tags-view-item {
      background: #292a2d;
      
      &:hover {
        background: #323639;
      }
      
      &.active {
        background: #323639;
        margin-bottom: -1px;
        padding-bottom: 1px;
        
        &::before {
          box-shadow: 4px 4px 0 4px #323639;
        }
        
        &::after {
          box-shadow: -4px 4px 0 4px #323639;
        }
      }
    }
  }
}

// 边框式样式
.border {
  .tags-view-item {
    margin: 2px 4px;
    height: 26px;
    line-height: 26px;
    background: transparent;
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
    position: relative;
    
    &:hover {
      border-color: v-bind('settingsStore.themeColor');
      color: v-bind('settingsStore.themeColor');
    }
    
    &.active {
      color: v-bind('settingsStore.themeColor');
      border-color: v-bind('settingsStore.themeColor');
      background: v-bind('`${settingsStore.themeColor}10`');
      font-weight: bold;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 3px;
        height: 100%;
        background-color: v-bind('settingsStore.themeColor');
        border-radius: 2px 0 0 2px;
        transform: none;
      }
    }
  }
}

// 现代风样式
.modern {
  background: transparent;
  border: none;
  padding: 4px 8px;
  
  .tags-view-item {
    margin: 0 4px;
    height: 32px;
    border: none;
    border-radius: 6px;
    background: var(--el-fill-color-light);
    transition: all 0.3s;
    position: relative;
    overflow: hidden;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      bottom: 0;
      width: 100%;
      height: 2px;
      background: transparent;
      transition: all 0.3s;
    }
    
    &:hover {
      background: var(--el-fill-color);
      transform: translateY(-1px);
      
      &::before {
        background: v-bind('`${settingsStore.themeColor}50`');
      }
    }
    
    &.active {
      background: v-bind('`${settingsStore.themeColor}10`');
      color: v-bind('settingsStore.themeColor');
      font-weight: bold;
      
      &::before {
        background: v-bind('settingsStore.themeColor');
      }
    }

    .close-icon {
      opacity: 0;
      transition: all 0.2s;
    }

    &:hover .close-icon {
      opacity: 1;
    }
  }
}

// 暗色主题适配
:root[data-theme='dark'] {
  .tags-view-container {
    background: #1d1e1f;
    
    .tags-view-item {
      &.active {
        background-color: v-bind('`${settingsStore.themeColor}2a`');
        border-color: v-bind('settingsStore.themeColor');
      }
    }
  }

  .card {
    background: #202124;
    
    .tags-view-item {
      background: #292a2d;
      
      &:hover {
        background: #323639;
      }
      
      &.active {
        background: #323639;
      }
    }
  }
}
</style>