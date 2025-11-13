<template>
  <div class="navbar-container">
    <div class="navbar-left">
      <el-icon class="collapse-btn" @click="toggleCollapse">
        <Fold v-if="!isCollapse" />
        <Expand v-else />
      </el-icon>

      <Breadcrumb />
    </div>
    <div class="navbar-right">
      <!-- 菜单全局搜索 -->
      <global-search/>
      <!-- 全屏切换 -->
      <el-tooltip content="全屏切换" placement="bottom" >  
        <el-icon class="setting-icon"  @click="toggleFullscreen">
          <FullScreen v-if="!isFullscreen" />
          <svg-icon name="exitFullscreen" v-else />
        </el-icon>
      </el-tooltip>


      <!-- 通知中心 -->
      <el-tooltip content="通知中心" placement="bottom">  
        <notification />
      </el-tooltip>
      <!-- 用户信息 -->
        <user-tool @lock="handleLock" />
    </div>

    <!-- 添加锁屏组件 -->
    <lock-screen ref="lockScreenRef" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import screenfull from 'screenfull'
import { useSettingsStore } from '@/store/modules/settings'
import GlobalSearch from '@/components/GlobalSearch/index.vue'
import Breadcrumb from './Breadcrumb/index.vue'
import UserTool from './UserTool/index.vue'
import LockScreen from '@/components/LockScreen/index.vue'
import Notification from './Notification/index.vue'
import { FullScreen, Setting } from '@element-plus/icons-vue'

const settingsStore = useSettingsStore()
const lockScreenRef = ref()
const isFullscreen = ref(false)

defineProps({
  isCollapse: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['toggle-collapse'])

const toggleCollapse = () => {
  emit('toggle-collapse')
}



const handleLock = () => {
  lockScreenRef.value?.lock()
}

const toggleFullscreen = () => {
  if (screenfull.isEnabled) {
    screenfull.toggle()
    isFullscreen.value = !isFullscreen.value
  }
}
</script>

<style lang="scss" scoped>
.navbar-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
  
  .navbar-left {
    display: flex;
    align-items: center;

    
    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      margin-right: 16px;
      color: #606266;
      transition: all 0.3s;
      
      &:hover {
        color: v-bind('settingsStore.themeColor');
      }
    }
  }
  
  .navbar-right {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-left: auto;
    padding-right: 8px;
    position: absolute;
    right: 0;
    height: 100%;
    
    .setting-icon {
      font-size: 20px !important;
      cursor: pointer;
      padding: 6px;
      border-radius: 50%;
      transition: all 0.3s;
      color: #606266;
      
      &:hover {
        background-color: v-bind('`${settingsStore.themeColor}1a`');
        color: v-bind('settingsStore.themeColor');
      }
    }

  }
}
</style>
