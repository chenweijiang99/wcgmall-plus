<template>
  <div class="error-container">
    <div class="error-content">
      <div class="error-image">
        <el-icon :size="180" color="#409EFF"><Warning /></el-icon>
      </div>
      <h1 class="error-title">404</h1>
      <p class="error-desc">抱歉，您访问的页面不存在</p>
      <p class="error-info">请检查您输入的网址是否正确，或者点击下面的按钮返回首页</p>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button @click="goBack">
          <el-icon><Back /></el-icon>
          返回上一页
        </el-button>
      </div>
      <div class="error-footer">
        <p class="error-time">{{ currentTime }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { HomeFilled, Back, Warning } from '@element-plus/icons-vue'

const router = useRouter()
const currentTime = ref('')
let timer: NodeJS.Timer

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}

const updateTime = () => {
  const now = new Date()
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${hours}:${minutes}:${seconds}`
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style lang="scss" scoped>
.error-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  
  .error-content {
    text-align: center;
    padding: 40px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    max-width: 600px;
    width: 90%;
    
    .error-image {
      margin-bottom: 24px;
      animation: float 6s ease-in-out infinite;
    }
    
    .error-title {
      font-size: 72px;
      font-weight: 700;
      color: #303133;
      margin: 0;
      background: linear-gradient(45deg, #409EFF, #36cfc9);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      animation: pulse 2s ease-in-out infinite;
    }
    
    .error-desc {
      font-size: 24px;
      color: #606266;
      margin: 16px 0;
      font-weight: 500;
    }
    
    .error-info {
      font-size: 16px;
      color: #909399;
      margin-bottom: 32px;
      line-height: 1.6;
    }
    
    .error-actions {
      display: flex;
      justify-content: center;
      gap: 16px;
      margin-bottom: 32px;
      
      .el-button {
        min-width: 120px;
        
        .el-icon {
          margin-right: 8px;
        }
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
      }
    }
    
    .error-footer {
      border-top: 1px solid #ebeef5;
      padding-top: 20px;
      
      .error-time {
        font-family: monospace;
        font-size: 18px;
        color: #909399;
        margin: 0;
      }
    }
  }
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
  100% {
    transform: translateY(0px);
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

// 响应式设计
@media screen and (max-width: 768px) {
  .error-container {
    .error-content {
      padding: 24px;
      
      .error-title {
        font-size: 56px;
      }
      
      .error-desc {
        font-size: 20px;
      }
      
      .error-info {
        font-size: 14px;
      }
      
      .error-actions {
        flex-direction: column;
        
        .el-button {
          width: 100%;
        }
      }
    }
  }
}

// 深色模式适配
@media (prefers-color-scheme: dark) {
  .error-container {
    background: linear-gradient(135deg, #2d3436 0%, #000000 100%);
    
    .error-content {
      background: rgba(30, 30, 30, 0.9);
      border-color: rgba(255, 255, 255, 0.1);
      
      .error-desc {
        color: #e0e0e0;
      }
      
      .error-info {
        color: #909399;
      }
      
      .error-footer {
        border-top-color: rgba(255, 255, 255, 0.1);
      }
    }
  }
}
</style> 