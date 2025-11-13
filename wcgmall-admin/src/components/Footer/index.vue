<template>
  <div class="footer">
    <div class="footer-content">
      <div class="left-section">
        <div class="copyright">
          Copyright © {{ currentYear }} 
          <a :href="settings.repository" target="_blank" rel="noopener noreferrer">
              文创购管理系统
          </a>
          <span class="version">v1.0.0</span>
        </div>
      </div>
      
      <div class="right-section">
        <div class="powered-by">
          Powered by Vue3 & Element Plus
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useSettingsStore } from '@/store/modules/settings'
import settings from '@/config/settings'


const settingsStore = useSettingsStore()
// const title = computed(() => settingsStore.title)
const currentYear = computed(() => new Date().getFullYear())
</script>

<style lang="scss" scoped>
.footer {
  padding: 12px 24px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
  background: var(--el-bg-color);
  border-top: 1px solid var(--el-border-color-light);
  transition: all 0.3s ease;
  
  .footer-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    max-width: 1400px;
    margin: 0 auto;
    
    @media screen and (max-width: 768px) {
      flex-direction: column;
      gap: 8px;
      text-align: center;
    }
  }
  
  .left-section {
    .copyright {
      color: var(--el-text-color-regular);
      
      a {
        color: var(--el-color-primary);
        text-decoration: none;
        font-weight: 500;
        transition: color 0.3s;
        
        &:hover {
          color: var(--el-color-primary-light-3);
        }
      }
      
      .version {
        display: inline-block;
        margin-left: 8px;
        padding: 1px 6px;
        font-size: 12px;
        color: var(--el-color-info);
        background: var(--el-fill-color-light);
        border-radius: 4px;
      }
    }
  }
  
  .right-section {
    .powered-by {
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }
  }
}

// 暗黑模式适配
:root[data-theme='dark'] {
  .footer {
    background: var(--el-bg-color-overlay);
    border-top-color: var(--el-border-color-darker);
    
    .left-section {
      .copyright .version {
        background: var(--el-fill-color-darker);
      }
    }
  }
}
</style> 