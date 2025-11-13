<template>
  <div class="global-search">
    <!-- 搜索图标 -->
    <div class="search-trigger" :class="{ 'is-dark': settingsStore.theme === 'dark' }" @click="handleSearchClick">
      <el-icon class="search-icon"><Search /></el-icon>
      <span class="shortcut-hint">
        <span class="key">CTRL</span>
        <span class="key">K</span>
      </span>
    </div>

    <!-- 搜索弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title=" "
      width="560px"
      :show-close="false"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      destroy-on-close
      class="search-dialog"
      :modal-class="'search-dialog-modal'"
    >
      <el-input
        v-model="searchKeyword"
        placeholder="搜索菜单... (ESC 关闭, ↑↓ 选择, Enter 跳转)"
        clearable
        @input="handleSearchInput"
        ref="searchInputRef"
        class="search-input"
      >
        <template #prefix>
          <el-icon class="search-prefix-icon"><Search /></el-icon>
        </template>
      </el-input>
      
      <div class="search-result" v-if="searchResults.length">
        <el-scrollbar height="400px">
          <template v-for="group in searchResults" :key="group.title">
            <div class="menu-group-title">
              <el-icon><component :is="group.icon" /></el-icon>
              <span>{{ group.title }}</span>
              <span class="item-count">{{ group.children.length }}个结果</span>
            </div>
            <div 
              v-for="(item, idx) in group.children" 
              :key="item.path"
              class="search-item"
              :class="{ 'is-active': currentIndex === getItemIndex(group, idx) }"
              @click="handleSelectMenu(item)"
              @mouseenter="currentIndex = getItemIndex(group, idx)"
            >
              <el-icon class="item-icon"><component :is="item.icon" /></el-icon>
              <div class="item-content">
                <span class="item-title">{{ item.title }}</span>
                <span class="item-path">{{ item.path }}</span>
              </div>
              <el-icon class="enter-icon"><ArrowRight /></el-icon>
            </div>
          </template>
        </el-scrollbar>
      </div>
      <div v-else-if="searchKeyword" class="no-result">
        <el-empty description="未找到相关菜单" />
      </div>
      <div class="keyboard-tips">
        <div class="tip-item">
          <span class="key">↑↓</span>
          <span class="desc">选择</span>
        </div>
        <div class="tip-item">
          <span class="key">Enter</span>
          <span class="desc">跳转</span>
        </div>
        <div class="tip-item">
          <span class="key">ESC</span>
          <span class="desc">关闭</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ArrowRight } from '@element-plus/icons-vue'
import { useSettingsStore } from '@/store/modules/settings'
import { usePermissionStore } from '@/store/modules/permission'

const router = useRouter()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()

// 响应式变量
const dialogVisible = ref(false)
const searchKeyword = ref('')
const searchInputRef = ref()
const searchResults = ref<Array<any>>([])
const currentIndex = ref(-1)
const flatSearchResults = ref<any[]>([])

// 修改获取菜单列表的方法，添加三级菜单支持
const getMenuList = () => {
  const routes = permissionStore.routes
  return routes
    .filter(route => !route.meta?.hidden)
    .map(route => ({
      title: route.meta?.title || '',
      path: route.path,
      icon: route.meta?.icon || '',
      children: route.children
        ?.filter(child => !child.meta?.hidden)
        .map(child => {
          // 如果有三级菜单
          if (child.children?.length) {
            return child.children
              .filter(grandChild => !grandChild.meta?.hidden)
              .map(grandChild => ({
                title: `${child.meta?.title} / ${grandChild.meta?.title}`, // 显示二级和三级菜单名称
                path: `${route.path}/${child.path}/${grandChild.path}`,
                icon: grandChild.meta?.icon || child.meta?.icon || ''
              }))
          }
          // 返回二级菜单
          return {
            title: child.meta?.title || '',
            path: `${route.path}/${child.path}`,
            icon: child.meta?.icon || ''
          }
        })
        .flat() || []
    }))
}

// 处理搜索图标点击
const handleSearchClick = () => {
  dialogVisible.value = true
  searchKeyword.value = ''
  searchResults.value = []
  flatSearchResults.value = []
  currentIndex.value = -1
  nextTick(() => {
    searchInputRef.value?.focus()
    document.addEventListener('keydown', handleKeydown)
  })
}

// 处理搜索输入
const handleSearchInput = () => {
  if (!searchKeyword.value) {
    searchResults.value = []
    flatSearchResults.value = []
    currentIndex.value = -1
    return
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  const menuList = getMenuList()
  
  searchResults.value = menuList
    .map(group => {
      const matchedChildren = group.children?.filter(item =>
        (typeof item.title === 'string' ? item.title : '').toLowerCase().includes(keyword) ||
        (typeof item.path === 'string' ? item.path : '').toLowerCase().includes(keyword)
      )
      
      if (matchedChildren?.length > 0) {
        return {
          ...group,
          children: matchedChildren
        }
      }
      return null
    })
    .filter(Boolean) as any[]

  // 更新扁平化的搜索结果数组
  flatSearchResults.value = searchResults.value.reduce((acc, group) => {
    return acc.concat(group.children)
  }, [])
  
  // 重置选中索引
  currentIndex.value = flatSearchResults.value.length ? 0 : -1
}

// 添加键盘事件处理
const handleKeydown = (e: KeyboardEvent) => {
  if (!flatSearchResults.value.length) return

  switch (e.key) {
    case 'ArrowDown':
      e.preventDefault()
      currentIndex.value = (currentIndex.value + 1) % flatSearchResults.value.length
      scrollToActive()
      break
    case 'ArrowUp':
      e.preventDefault()
      currentIndex.value = currentIndex.value <= 0 
        ? flatSearchResults.value.length - 1 
        : currentIndex.value - 1
      scrollToActive()
      break
    case 'Enter':
      if (currentIndex.value >= 0) {
        handleSelectMenu(flatSearchResults.value[currentIndex.value])
      }
      break
  }
}

// 滚动到选中项
const scrollToActive = () => {
  nextTick(() => {
    const activeItem = document.querySelector('.search-item.is-active')
    activeItem?.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
  })
}

// 处理菜单选择
const handleSelectMenu = (item: any) => {
  router.push(item.path)
  dialogVisible.value = false
}

// 添加键盘快捷键
onMounted(() => {
  window.addEventListener('keydown', (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
      e.preventDefault()
      handleSearchClick()
    }
  })
})

// 修改弹窗关闭时的处理
watch(dialogVisible, (newVal) => {
  if (!newVal) {
    document.removeEventListener('keydown', handleKeydown)
  }
})

// 添加 getItemIndex 方法
const getItemIndex = (group: any, idx: number) => {
  let index = 0
  for (const g of searchResults.value) {
    if (g === group) {
      return index + idx
    }
    index += g.children.length
  }
  return -1
}
</script>

<style lang="scss" scoped>
.global-search {
  .search-trigger {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    padding: 6px 10px;
    border-radius: 6px;
    transition: all 0.3s;
    
    // 明亮模式样式
    background: #f5f7fa;
    
    &:hover {
      background: #e6e8eb;
    }

    // 深色模式样式
    &.is-dark {
      background: #363637;
      
      &:hover {
        background: #404041;
      }

      .search-icon {
        color: #bfcbd9;
      }

      .shortcut-hint .key {
        background: #4a4a4a;
        color: #bfcbd9;
        box-shadow: 0 1px 1px rgba(0, 0, 0, 0.2);
      }
    }

    .search-icon {
      font-size: 16px;
      color: #606266;
    }

    .shortcut-hint {
      display: flex;
      gap: 4px;
      
      .key {
        padding: 2px 4px;
        font-size: 12px;
        background: #fff;
        border-radius: 4px;
        color: #909399;
        box-shadow: 0 1px 1px rgba(0,0,0,0.1);
      }
    }
  }
}

:deep(.search-dialog) {
  border-radius: 12px;
  overflow: hidden;

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 16px;
  }
}

.search-input {
  :deep(.el-input__wrapper) {
    padding: 8px 12px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  }

  .search-prefix-icon {
    font-size: 18px;
    margin-right: 8px;
    color: #909399;
  }

  .esc-hint {
    padding: 2px 6px;
    font-size: 12px;
    background: #f5f7fa;
    border-radius: 4px;
    color: #909399;
  }
}

.search-result {
  margin-top: 16px;

  .menu-group-title {
    display: flex;
    align-items: center;
    padding: 12px 16px 8px;
    color: #606266;
    font-size: 14px;
    font-weight: 500;
    
    .el-icon {
      font-size: 16px;
      margin-right: 8px;
      color: v-bind('settingsStore.themeColor');
    }

    .item-count {
      margin-left: auto;
      font-size: 12px;
      color: #909399;
      font-weight: normal;
    }
  }

  .search-item {
    display: flex;
    align-items: center;
    padding: 10px 16px;
    cursor: pointer;
    border-radius: 8px;
    margin: 2px 0;
    transition: all 0.2s;
    position: relative;

    &:not(:first-child) {
      margin-left: 24px;
      padding-left: 24px;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        width: 16px;
        height: 1px;
        background-color: #e4e7ed;
      }

      &::after {
        content: '';
        position: absolute;
        left: 0;
        top: -8px;
        bottom: 50%;
        width: 1px;
        background-color: #e4e7ed;
      }
    }

    &:hover {
      background-color: v-bind('`${settingsStore.themeColor}0a`');

      .enter-icon {
        opacity: 1;
        transform: translateX(0);
      }

      &::before, &::after {
        background-color: v-bind('settingsStore.themeColor');
      }
    }

    .item-icon {
      font-size: 16px;
      color: #909399;
      margin-right: 12px;
    }

    .item-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .item-title {
      font-size: 14px;
      color: #303133;
    }

    .item-path {
      font-size: 12px;
      color: #909399;
    }

    .enter-icon {
      font-size: 16px;
      color: v-bind('settingsStore.themeColor');
      opacity: 0;
      transform: translateX(-4px);
      transition: all 0.2s;
    }

    &.is-active {
      background-color: v-bind('`${settingsStore.themeColor}1a`');
      
      .enter-icon {
        opacity: 1;
        transform: translateX(0);
      }
      
      &::before, &::after {
        background-color: v-bind('settingsStore.themeColor');
      }
      
      .item-icon {
        color: v-bind('settingsStore.themeColor');
      }
      
      .item-title {
        color: v-bind('settingsStore.themeColor');
      }
    }
  }
}

.no-result {
  padding: 40px 0;
}



.keyboard-tips {
  margin-top: 16px;
  padding: 12px;
  border-top: 1px solid var(--el-border-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  
  .tip-item {
    display: flex;
    align-items: center;
    gap: 6px;
    
    .key {
      padding: 2px 6px;
      font-size: 12px;
      background: #f5f7fa;
      border-radius: 4px;
      color: #909399;
    }
    
    .desc {
      font-size: 12px;
      color: #909399;
    }
  }
}

// 暗色模式适配
:root[data-theme='dark'] {
  .keyboard-tips {
    border-color: var(--el-border-color-darker);
    
    .tip-item .key {
      background: var(--el-fill-color-darker);
    }
  }
}


</style> 