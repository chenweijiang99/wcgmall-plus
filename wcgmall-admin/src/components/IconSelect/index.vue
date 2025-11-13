<template>
  <el-dialog
    v-model="dialogVisible"
    title="选择图标"
    width="800px"
    append-to-body
    top="5vh"
  >
    <div class="icon-container">
      <div class="search-bar">
        <el-input
          v-model="searchText"
          placeholder="搜索图标"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <el-scrollbar height="400px">
        <div class="icon-list">
          <div
            v-for="(component, name) in filteredIcons"
            :key="name"
            class="icon-item"
            :class="{ active: modelValue === name }"
            @click="selectIcon(name)"
          >
            <el-icon>
              <component :is="component" />
            </el-icon>
            <span class="icon-name">{{ name }}</span>
          </div>
        </div>
      </el-scrollbar>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { Search } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: string
  visible: boolean
}>()

const emit = defineEmits(['update:modelValue', 'update:visible'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const searchText = ref('')

// 直接使用图标组件对象
const icons = ElementPlusIconsVue

const filteredIcons = computed(() => {
  const iconEntries = Object.entries(icons)
  if (!searchText.value) return icons
  
  return Object.fromEntries(
    iconEntries.filter(([name]) => 
      name.toLowerCase().includes(searchText.value.toLowerCase())
    )
  )
})

const selectIcon = (iconName: string) => {
  emit('update:modelValue', iconName)
  emit('update:visible', false)
}
</script>

<style scoped>
.icon-container {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.icon-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  padding: 12px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.icon-item:hover {
  background-color: #ecf5ff;
  border-color: #409EFF;
}

.icon-item.active {
  background-color: #ecf5ff;
  border-color: #409EFF;
  color: #409EFF;
}

.icon-item .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.icon-name {
  font-size: 12px;
  color: #606266;
  word-break: break-all;
  text-align: center;
}

.icon-item.active .icon-name {
  color: #409EFF;
}
</style> 