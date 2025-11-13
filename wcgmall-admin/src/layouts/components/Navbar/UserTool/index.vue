<template>
  <div class="navbar-container">
    <div class="custom-dropdown" v-click-outside="closeDropdown">
      <div class="avatar-trigger" @click="toggleDropdown">
        <el-avatar :size="32" :src="userStore.user.avatar || ''" />
        <el-icon class="dropdown-icon" :class="{ 'is-active': isOpen }">
          <CaretBottom />
        </el-icon>
      </div>
      
      <transition name="dropdown">
        <div v-show="isOpen" class="dropdown-menu">
          <!-- 用户信息卡片 -->
          <div class="user-info">
            <div class="decoration-circles">
              <div class="circle circle-1"></div>
              <div class="circle circle-2"></div>
              <div class="circle circle-3"></div>
            </div>
            
            <div class="user-profile">
              <div class="user-avatar">
                <el-avatar :size="50" :src="userStore.user.avatar || ''" />

              </div>
              
              <div class="user-details">
                <div class="nickname">{{ userStore.user.nickname }}</div>
              </div>
            </div>
          </div>
          
          <!-- 菜单项 -->
          <div class="menu-items">
            <div class="menu-item" @click="toGitee">
              <div class="menu-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="menu-content">
                <span class="menu-title">仓库地址</span>
                <span class="menu-desc">查看项目源码</span>
              </div>
            </div>
            
            <div class="divider"></div>
            
            <div class="menu-item" @click="toProfile">
              <div class="menu-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="menu-content">
                <span class="menu-title">个人中心</span>
                <span class="menu-desc">管理您的账户信息</span>
              </div>
            </div>

            <div class="menu-item" @click="handleLock">
              <div class="menu-icon">
                <el-icon><Lock /></el-icon>
              </div>
              <div class="menu-content">
                <span class="menu-title">锁定屏幕</span>
                <span class="menu-desc">锁定屏幕保护您的隐私</span>
              </div>
            </div>
            
            <div class="menu-item danger" @click="logout">
              <div class="menu-icon">
                <el-icon><SwitchButton /></el-icon>
              </div>
              <div class="menu-content">
                <span class="menu-title">退出登录</span>
                <span class="menu-desc">安全退出系统</span>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore,useSettingsStore } from '@/store/index'
import settings from '@/config/settings'

const router = useRouter()
const userStore = useUserStore()
const settingsStore = useSettingsStore()
const isOpen = ref(false)

const emit = defineEmits(['lock'])

// 切换下拉菜单
const toggleDropdown = () => {
  isOpen.value = !isOpen.value
}

// 关闭下拉菜单
const closeDropdown = () => {
  isOpen.value = false
}

const toGitee = () => {
  window.open(settings.repository, '_blank')
  closeDropdown()
}

const toProfile = () => {
  router.push('/system/profile')
  closeDropdown()
}

const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await userStore.logout()
    router.push('/login')
  })
}

const handleLock = () => {
  emit('lock')
  closeDropdown()
}

// 点击外部关闭指令
const vClickOutside = {
  mounted(el: any, binding: any) {
    el._clickOutside = (event: any) => {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value(event)
      }
    }
    document.addEventListener('click', el._clickOutside)
  },
  unmounted(el: any) {
    document.removeEventListener('click', el._clickOutside)
  }
}
</script>

<style lang="scss" scoped>
.navbar-container {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.custom-dropdown {
  position: relative;
}

.avatar-trigger {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background-color: v-bind('`${settingsStore.themeColor}1a`');
  }
  
  .dropdown-icon {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    transition: transform 0.3s;
    
    &.is-active {
      transform: rotate(180deg);
    }
  }
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 240px;
  background: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 2000;
}

.decoration-circles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
  
  .circle {
    position: absolute;
    border-radius: 50%;
    background: linear-gradient(45deg, 
      rgba(255, 255, 255, 0.1),
      rgba(255, 255, 255, 0.05)
    );
    
    &-1 {
      width: 120px;
      height: 120px;
      top: -60px;
      left: -60px;
      animation: float 8s ease-in-out infinite;
    }
    
    &-2 {
      width: 160px;
      height: 160px;
      top: -40px;
      right: -80px;
      animation: float 12s ease-in-out infinite reverse;
    }
    
    &-3 {
      width: 80px;
      height: 80px;
      bottom: -40px;
      left: 50%;
      animation: float 10s ease-in-out infinite;
    }
  }
}

.user-info {
  padding: 20px;
  background: linear-gradient(135deg, 
    v-bind('`${settingsStore.themeColor}E6`'),
    v-bind('`${settingsStore.themeColor}99`')
  );
  position: relative;
  overflow: hidden;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-avatar {
  position: relative;
  flex-shrink: 0;
  
  .el-avatar {
    border: 3px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transition: transform 0.3s;
    
    &:hover {
      transform: scale(1.05);
    }
  }
}

.user-status {
  position: absolute;
  bottom: -4px;
  right: -4px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(4px);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 12px;
  color: var(--el-color-success);
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  
  .status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--el-color-success);
    animation: pulse 2s infinite;
    box-shadow: 0 0 0 2px rgba(var(--el-color-success-rgb), 0.2);
  }
}

.user-details {
  flex: 1;
  min-width: 0;
  
  .nickname {
    font-size: 16px;
    font-weight: 600;
    color: var(--el-bg-color);
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.menu-items {
  padding: 8px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.3s;
  
  .menu-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    background: var(--el-fill-color-light);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;
    
    .el-icon {
      font-size: 18px;
      transition: all 0.3s;
    }
  }
  
  .menu-content {
    flex: 1;
    cursor: pointer;
    transition: all 0.3s;
    
    .menu-title {
      display: block;
      font-size: 14px;
      font-weight: 500;
      line-height: 1.4;
      color: var(--el-text-color-primary);
      transition: all 0.3s;
    }
    
    .menu-desc {
      display: block;
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-top: 2px;
      transition: all 0.3s;
    }
  }
  
  &:hover {
    background-color: var(--el-fill-color-light);
    
    .menu-icon {
      background: v-bind('`${settingsStore.themeColor}1a`');
      transform: scale(1.05);
      
      .el-icon {
        color: v-bind('settingsStore.themeColor');
        transform: scale(1.1);
      }
    }
    
    .menu-content {
      .menu-title {
        color: v-bind('settingsStore.themeColor');
      }
      
      .menu-desc {
        color: v-bind('`${settingsStore.themeColor}99`');
      }
    }
  }

}

.divider {
  margin: 8px 0;
  height: 1px;
  background: var(--el-border-color-light);
}

// 下拉动画
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.3s;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

// 暗色模式适配
:root[data-theme='dark'] {
  .avatar-trigger:hover {
    background: var(--el-fill-color-darker);
  }
  
  .dropdown-menu {
    background: var(--el-bg-color-overlay);
    border: 1px solid var(--el-border-color-darker);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  }
  
  .user-info {
    background: linear-gradient(135deg,
      var(--el-color-primary-dark-2) 0%,
      var(--el-color-primary) 100%
    );
  }
  
  .user-status {
    background: var(--el-bg-color-overlay);
    color: var(--el-color-success);
  }
  
  .user-stats {
    background: rgba(0, 0, 0, 0.2);
  }
  
  .divider {
    background: var(--el-border-color-darker);
  }
  
  .menu-item:hover {
    background: var(--el-fill-color-darker);
  }
  
  .user-details {
    .nickname {
      color: var(--el-text-color-primary);
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0);
  }
  50% {
    transform: translateY(-20px) rotate(8deg);
  }
}
</style> 