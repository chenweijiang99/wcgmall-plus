<template>
  <div class="app-container">
     <!-- CPU信息 -->
     <el-row :gutter="20" class="server-info">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>CPU使用率</span>
              </div>
            </template>
            <div class="chart-wrapper">
              <el-progress
                type="dashboard"
                :percentage="serverInfo.cpu?.used"
                :color="customColors"
              />
            </div>
            <div class="detail-list">
              <div class="detail-item">
                <span>核心数</span>
                <span>{{ serverInfo.cpu?.cpuNum }}</span>
              </div>
              <div class="detail-item">
                <span>用户使用率</span>
                <span>{{ serverInfo.cpu?.used }}%</span>
              </div>
              <div class="detail-item">
                <span>系统使用率</span>
                <span>{{ serverInfo.cpu?.sys }}%</span>
              </div>
              <div class="detail-item">
                <span>当前空闲率</span>
                <span>{{ serverInfo.cpu?.free }}%</span>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 内存信息 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>内存使用率</span>
              </div>
            </template>
            <div class="chart-wrapper">
              <el-progress
                type="dashboard"
                :percentage="serverInfo.mem?.usage"
                :color="customColors"
              />
            </div>
            <div class="detail-list">
              <div class="detail-item">
                <span>总内存</span>
                <span>{{ formatBytes(serverInfo.mem?.total) }}</span>
              </div>
              <div class="detail-item">
                <span>已用内存</span>
                <span>{{ formatBytes(serverInfo.mem?.used) }}</span>
              </div>
              <div class="detail-item">
                <span>剩余内存</span>
                <span>{{ formatBytes(serverInfo.mem?.free) }}</span>
              </div>
              <div class="detail-item">
                <span>使用率</span>
                <span>{{ serverInfo.mem?.usage }}%</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 服务器信息 -->
      <el-card class="mt-4">
        <template #header>
          <div class="card-header">
            <span>服务器信息</span>
          </div>
        </template>
        <div class="detail-list">
          <div class="detail-item">
            <span>服务器名称</span>
            <span>{{ serverInfo.sys?.computerName }}</span>
          </div>
          <div class="detail-item">
            <span>操作系统</span>
            <span>{{ serverInfo.sys?.osName }}</span>
          </div>
          <div class="detail-item">
            <span>服务器IP</span>
            <span>{{ serverInfo.sys?.computerIp }}</span>
          </div>
          <div class="detail-item">
            <span>系统架构</span>
            <span>{{ serverInfo.sys?.osArch }}</span>
          </div>
        </div>
      </el-card>

      <!-- JVM信息 -->
      <el-card class="mt-4">
        <template #header>
          <div class="card-header">
            <span>JVM信息</span>
          </div>
        </template>
        <div class="detail-list">
          <div class="detail-item">
            <span>JVM名称</span>
            <span>{{ serverInfo.jvm?.name }}</span>
          </div>
          <div class="detail-item">
            <span>JVM版本</span>
            <span>{{ serverInfo.jvm?.version }}</span>
          </div>
          <div class="detail-item">
            <span>启动时间</span>
            <span>{{ serverInfo.jvm?.startTime }}</span>
          </div>
          <div class="detail-item">
            <span>运行时长</span>
            <span>{{ serverInfo.jvm?.runTime }}</span>
          </div>
          <div class="detail-item">
            <span>安装路径</span>
            <span>{{ serverInfo.jvm?.home }}</span>
          </div>
          <div class="detail-item">
            <span>项目路径</span>
            <span>{{ serverInfo.sys?.userDir }}</span>
          </div>
        </div>
      </el-card>

      <!-- 磁盘信息 -->
      <el-card class="mt-4">
        <template #header>
          <div class="card-header">
            <span>磁盘信息</span>
          </div>
        </template>
        <el-table :data="serverInfo.sysFiles" border style="width: 100%">
          <el-table-column prop="dirName" label="盘符路径" />
          <el-table-column prop="typeName" label="文件系统" />
          <el-table-column label="总大小">
            <template #default="{ row }">
              {{ formatToGB(row.total) }}
            </template>
          </el-table-column>
          <el-table-column label="可用大小">
            <template #default="{ row }">
              {{ formatToGB(row.free) }}
            </template>
          </el-table-column>
          <el-table-column label="已用大小">
            <template #default="{ row }">
              {{ formatToGB(row.used) }}
            </template>
          </el-table-column>
          <el-table-column prop="usage" label="使用率">
            <template #default="{ row }">
              <el-progress
                :percentage="row.usage"
                :color="customColors"
              />
            </template>
          </el-table-column>
        </el-table>
      </el-card>
  </div>
</template>

<script setup lang="ts">
import { getServerInfoApi } from '@/api/monitor/server'

const loading = ref(false)
const serverInfo = ref<any>({})
let timer: number | undefined

// 自定义进度条颜色
const customColors = [
  { color: '#67C23A', percentage: 60 },
  { color: '#E6A23C', percentage: 80 },
  { color: '#F56C6C', percentage: 100 }
]

// 格式化字节大小
const formatBytes = (bytes?: number) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`
}

// 添加格式化为GB的方法
const formatToGB = (bytes?: number) => {
  if (!bytes) return '0 GB'
  const gb = bytes / (1024 * 1024 * 1024)
  return `${gb.toFixed(2)} GB`
}

// 获取服务器信息
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getServerInfoApi()
    serverInfo.value = data
  } catch (error) {
    console.error('获取服务器信息失败:', error)
  }
  loading.value = false
}

// 定时刷新
const startTimer = () => {
  timer = window.setInterval(() => {
    getList()
  }, 30000) // 每30秒刷新一次
}

onMounted(() => {
  getList()
  startTimer()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>

.chart-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.detail-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-item span:first-child {
  color: var(--el-text-color-secondary);
}

.mt-4 {
  margin-top: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 