<template>
  <div class="notification-container" v-click-outside="closeNotification">
    <el-badge 
      :value="unreadCount" 
      :max="99" 
      :hidden="!unreadCount"
      class="notification-badge"
    >
      <el-icon class="notification-icon" @click="toggleNotification">
        <Bell />
      </el-icon>
    </el-badge>

    <transition name="dropdown">
      <div v-show="isOpen" class="notification-dropdown">
        <div class="dropdown-header">
          <span>通知中心</span>
          <div class="header-actions">
            <el-button type="primary" link @click="markAllRead">
              全部已读
            </el-button>
            <el-button type="danger" link @click="clearAll">
              清空全部
            </el-button>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="notification-tabs">
          <el-tab-pane label="未读消息" name="unread">
            <el-scrollbar height="300px">
              <template v-if="unreadNotifications.length">
                <div 
                  v-for="notification in unreadNotifications" 
                  :key="notification.id"
                  class="notification-item"
                  :class="{ unread: !notification.read }"
                  @click="handleNotificationClick(notification)"
                >
                  <div class="notification-icon">
                    <el-icon :class="notification.type">
                      <component :is="getIcon(notification.type)" />
                    </el-icon>
                  </div>
                  <div class="notification-content">
                    <div class="notification-title">{{ notification.title }}</div>
                    <div class="notification-message">{{ notification.message }}</div>
                    <div class="notification-time">{{ formatTime(notification.time) }}</div>
                  </div>
                </div>
              </template>
              <el-empty v-else description="暂无未读消息" />
            </el-scrollbar>
          </el-tab-pane>

          <el-tab-pane label="全部消息" name="all">
            <el-scrollbar height="300px">
              <template v-if="notifications.length">
                <div 
                  v-for="notification in notifications" 
                  :key="notification.id"
                  class="notification-item"
                  :class="{ unread: !notification.read }"
                  @click="handleNotificationClick(notification)"
                >
                  <div class="notification-icon">
                    <el-icon :class="notification.type">
                      <component :is="getIcon(notification.type)" />
                    </el-icon>
                  </div>
                  <div class="notification-content">
                    <div class="notification-title">{{ notification.title }}</div>
                    <div class="notification-message">{{ notification.message }}</div>
                    <div class="notification-time">{{ formatTime(notification.time) }}</div>
                  </div>
                </div>
              </template>
              <el-empty v-else description="暂无消息" />
            </el-scrollbar>
          </el-tab-pane>
        </el-tabs>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Bell, InfoFilled, SuccessFilled, Warning } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { useSettingsStore } from '@/store/modules/settings'

const settingsStore = useSettingsStore()

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

interface Notification {
  id: number
  title: string
  message: string
  type: 'info' | 'success' | 'warning' | 'error'
  time: Date
  read: boolean
}

const isOpen = ref(false)
const activeTab = ref('unread')
const notifications = ref<Notification[]>([
  // {
  //   id: 1,
  //   title: '系统通知',
  //   message: '欢迎使用通知中心',
  //   type: 'info',
  //   time: new Date(),
  //   read: false
  // },
  // {
  //   id: 2,
  //   title: '更新提醒',
  //   message: '系统已更新到最新版本，新增多项功能优化',
  //   type: 'success',
  //   time: new Date(Date.now() - 1000 * 60 * 30), // 30分钟前
  //   read: false
  // },
  // {
  //   id: 3,
  //   title: '安全提醒',
  //   message: '检测到您的账号在新设备上登录',
  //   type: 'warning',
  //   time: new Date(Date.now() - 1000 * 60 * 60), // 1小时前
  //   read: false
  // },
  // {
  //   id: 4,
  //   title: '安全提醒',
  //   message: '检测到您的账号在新设备上登录',
  //   type: 'warning',
  //   time: new Date(Date.now() - 1000 * 60 * 60), // 1小时前
  //   read: false
  // }
])

const unreadNotifications = computed(() => {
  return notifications.value.filter(n => !n.read)
})

const unreadCount = computed(() => unreadNotifications.value.length)

const toggleNotification = () => {
  isOpen.value = !isOpen.value
}

const getIcon = (type: string) => {
  const icons = {
    info: InfoFilled,
    success: SuccessFilled,
    warning: Warning,
    error: Warning
  }
  return icons[type] || InfoFilled
}

const formatTime = (time: Date) => {
  return dayjs(time).fromNow()
}

const handleNotificationClick = (notification: Notification) => {
  notification.read = true
  // 这里可以添加点击通知后的处理逻辑
}

const markAllRead = () => {
  notifications.value.forEach(n => n.read = true)
}

const clearAll = () => {
  notifications.value = []
}

// 添加关闭通知的方法
const closeNotification = () => {
  isOpen.value = false
}

// 添加点击外部关闭指令
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
.notification-container {
  position: relative;
  display: flex;
  align-items: center;
  margin-right: 10px;
}

.notification-badge {
  .notification-icon {
    font-size: 20px;
    cursor: pointer;
    padding: 8px;
    border-radius: 50%;
    transition: all 0.3s;
    color: #606266;
    
    &:hover {
      background-color: v-bind('`${settingsStore.themeColor}1a`');
      color: v-bind('settingsStore.themeColor');
      transform: rotate(15deg);
    }
  }
}

.notification-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 360px;
  background: var(--el-bg-color);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 2000;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}

.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--el-border-color-light);
  background: linear-gradient(to right, 
    v-bind('`${settingsStore.themeColor}0a`'), 
    transparent
  );
  
  span {
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(120deg, 
      v-bind('settingsStore.themeColor'), 
      v-bind('`${settingsStore.themeColor}99`')
    );
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.notification-tabs {
  :deep(.el-tabs__header) {
    margin: 0;
    padding: 0 8px;
    border-bottom: 1px solid var(--el-border-color-light);
  }
  
  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }
  
  :deep(.el-tabs__item) {
    padding: 12px 16px;
    font-size: 14px;
    
    &.is-active {
      color: v-bind('settingsStore.themeColor');
      font-weight: 500;
    }
  }
  
  :deep(.el-tabs__active-bar) {
    background-color: v-bind('settingsStore.themeColor');
    height: 3px;
    border-radius: 3px;
  }
  
  :deep(.el-tabs__content) {
    padding: 12px;
  }
}

.notification-item {
  display: flex;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 8px;
  border: 1px solid transparent;
  
  &:hover {
    background-color: var(--el-fill-color-light);
    transform: translateY(-1px);
    border-color: var(--el-border-color-light);
  }
  
  &.unread {
    background: v-bind('`${settingsStore.themeColor}0a`');
    border-color: v-bind('`${settingsStore.themeColor}1a`');
    
    &:hover {
      background: v-bind('`${settingsStore.themeColor}1a`');
    }
  }
  
  .notification-icon {
    margin-right: 12px;
    
    .el-icon {
      font-size: 18px;
      padding: 8px;
      border-radius: 8px;
      transition: all 0.3s;
      
      &.info {
        color: var(--el-color-primary);
        background-color: var(--el-color-primary-light-9);
      }
      
      &.success {
        color: var(--el-color-success);
        background-color: var(--el-color-success-light-9);
      }
      
      &.warning {
        color: var(--el-color-warning);
        background-color: var(--el-color-warning-light-9);
      }
      
      &.error {
        color: var(--el-color-danger);
        background-color: var(--el-color-danger-light-9);
      }
    }
  }
  
  .notification-content {
    flex: 1;
    min-width: 0;
    
    .notification-title {
      font-weight: 500;
      margin-bottom: 4px;
      color: var(--el-text-color-primary);
      font-size: 14px;
    }
    
    .notification-message {
      color: var(--el-text-color-secondary);
      font-size: 13px;
      margin-bottom: 6px;
      line-height: 1.5;
    }
    
    .notification-time {
      color: var(--el-text-color-secondary);
      font-size: 12px;
      opacity: 0.8;
    }
  }
}

// 添加下拉动画
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-12px) scale(0.95);
}

// 暗色主题适配
:root[data-theme='dark'] {
  .notification-dropdown {
    background: var(--el-bg-color-overlay);
    border-color: var(--el-border-color-darker);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  }
  
  .dropdown-header {
    border-color: var(--el-border-color-darker);
  }
  
  .notification-item {
    &:hover {
      background-color: var(--el-fill-color-darker);
    }
    
    &.unread {
      background: v-bind('`${settingsStore.themeColor}1a`');
      
      &:hover {
        background: v-bind('`${settingsStore.themeColor}2a`');
      }
    }
  }
}
</style> 