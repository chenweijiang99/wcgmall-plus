<template>
  <div class="app-container">
    <!-- 基本信息 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
              <el-button v-permission="['monitor:cache']" type="danger" @click="handleClear">
                <el-icon><Delete /></el-icon>
                清空缓存
              </el-button>
            </div>
          </template>
          <div class="info-box">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="Redis版本">
                {{ cacheInfo.version }}
              </el-descriptions-item>
              <el-descriptions-item label="运行模式">
                {{ cacheInfo.mode }}
              </el-descriptions-item>
              <el-descriptions-item label="端口">
                {{ cacheInfo.port }}
              </el-descriptions-item>
              <el-descriptions-item label="运行时间(天)">
                {{ cacheInfo.uptime }}
              </el-descriptions-item>
              <el-descriptions-item label="连接客户端数">
                {{ cacheInfo.clients }}
              </el-descriptions-item>
              <el-descriptions-item label="内存配置">
                {{ cacheInfo.maxmemory }}
              </el-descriptions-item>
              <el-descriptions-item label="AOF是否开启">
                {{ cacheInfo.aofEnabled }}
              </el-descriptions-item>
              <el-descriptions-item label="RDB是否成功">
                {{ cacheInfo.rdbLastSaveStatus }}
              </el-descriptions-item>
              <el-descriptions-item label="Key数量">
                {{ cacheInfo.keys }}
              </el-descriptions-item>
              <el-descriptions-item label="网络入口/出口">
                {{ cacheInfo.instantaneousInputKbps }} / {{ cacheInfo.instantaneousOutputKbps }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card style="height: 100%;">
          <template #header>
            <div class="card-header">
              <span>内存信息</span>
              <el-button type="primary" @click="getMemoryInfo" v-permission="['monitor:cache']">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          <div class="chart-box">
            <el-progress
              type="dashboard"
              :percentage="memoryUsage"
              :color="memoryColor"
            >
              <template #default="{ percentage }">
                <div class="progress-content">
                  <h3>{{ percentage }}%</h3>
                  <p>内存使用率</p>
                </div>
              </template>
            </el-progress>
            <div class="memory-info">
              <p>已用内存：{{ formatMemory(memoryInfo.used) }}</p>
              <p>总内存：{{ formatMemory(memoryInfo.total) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 缓存列表 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>缓存列表</span>
          <div>
            <el-input
              v-model="queryParams.key"
              placeholder="请输入键名"
              style="width: 200px;"
              clearable
              @keyup.enter="handleQuery"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleQuery" style="margin-left: 10px;">
              搜索
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="keyList"
        style="width: 100%"
      >
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="键名" prop="key" :show-overflow-tooltip="true" />
        <el-table-column label="类型" prop="type" width="100" align="center" />
        <el-table-column label="大小" prop="size" width="100" align="center">
          <template #default="{ row }">
            {{ formatSize(row.size) }}
          </template>
        </el-table-column>
        <el-table-column label="过期时间" prop="ttl" width="180" align="center">
          <template #default="{ row }">
            {{ formatTTL(row.ttl) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)" v-permission="['monitor:cache']">
              <el-icon><View /></el-icon>查看详情
            </el-button>
            <el-button type="danger" link @click="handleDeleteKey(row)" v-permission="['monitor:cache']">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 缓存详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="缓存详情"
      width="60%"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="键名">
          {{ currentKey }}
        </el-descriptions-item>
        <el-descriptions-item label="键值">
          <el-scrollbar max-height="400px">
            <pre style="margin: 0; white-space: pre-wrap; word-wrap: break-word;">{{ currentValue }}</pre>
          </el-scrollbar>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCacheInfoApi, getCacheKeyListApi, getCacheMemoryApi, clearCacheApi, getCacheValueApi, deleteCacheKeyApi } from '@/api/monitor/cache'

// 数据定义
const loading = ref(false)
const total = ref(0)
const keyList = ref([])
const detailDialogVisible = ref(false)
const currentKey = ref('')
const currentValue = ref('')
const cacheInfo = ref<any>({
  version: '',
  mode: '',
  port: '',
  uptime: ''
})
const memoryInfo = ref<any>({
  used: 0,
  total: 0
})

// 查询参数
const queryParams = reactive<any>({
  pageNum: 1,
  pageSize: 10,
  key: ''
})

// 计算内存使用率
const memoryUsage = computed(() => {
  if (!memoryInfo.value.total) return 0
  return Math.round((memoryInfo.value.used / memoryInfo.value.total) * 100)
})

// 内存使用率颜色
const memoryColor = computed(() => {
  const usage = memoryUsage.value
  if (usage < 70) return '#67C23A'
  if (usage < 90) return '#E6A23C'
  return '#F56C6C'
})

// 获取缓存信息
const getInfo = async () => {
  try {
    const { data } = await getCacheInfoApi()
    cacheInfo.value = data
  } catch (error) {
    console.error('获取缓存信息失败:', error)
  }
}

// 获取内存信息
const getMemoryInfo = async () => {
  try {
    const { data } = await getCacheMemoryApi()
    memoryInfo.value = data
  } catch (error) {
    console.error('获取内存信息失败:', error)
  }
}

// 获取键值列表
const getList = async () => {
  try {
    loading.value = true
    const { data } = await getCacheKeyListApi(queryParams)
    keyList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取键值列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 清空缓存
const handleClear = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有缓存吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await clearCacheApi()
    ElMessage.success('清空成功')
    getList()
    getMemoryInfo()
  } catch (error) {
  }
}

// 查询操作
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 分页操作
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  getList()
}

const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val
  getList()
}

// 格式化函数
const formatMemory = (bytes: number) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatSize = (size: number) => {
  return formatMemory(size)
}

const formatTTL = (ttl: number) => {
  if (ttl === -1) return '永不过期'
  if (ttl === -2) return '已过期'
  return ttl + ' 秒'
}

// 查看详情
const handleViewDetail = async (row: any) => {
  try {
    currentKey.value = row.key
    const { data } = await getCacheValueApi(row.key)

    // 格式化显示
    if (data === null || data === undefined) {
      currentValue.value = '[空值]'
    } else if (typeof data === 'object') {
      // 如果是对象，格式化为 JSON
      currentValue.value = JSON.stringify(data, null, 2)
    } else if (typeof data === 'string') {
      // 如果是字符串，尝试解析为 JSON
      try {
        const parsed = JSON.parse(data)
        currentValue.value = JSON.stringify(parsed, null, 2)
      } catch {
        // 如果不是 JSON 字符串，直接显示
        currentValue.value = data
      }
    } else {
      // 其他类型（数字、布尔值等）
      currentValue.value = String(data)
    }

    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取缓存详情失败')
  }
}

// 删除单个键
const handleDeleteKey = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除键 "${row.key}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCacheKeyApi(row.key)
    ElMessage.success('删除成功')
    getList()
    getMemoryInfo()
  } catch (error) {
  }
}

// 初始化
onMounted(() => {
  getInfo()
  getMemoryInfo()
  getList()
})
</script>

<style lang="scss" scoped>

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-box {
  .el-descriptions {
    margin-bottom: 20px;
  }
}

.chart-box {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 20px;
  
  .progress-content {
    text-align: center;
    
    h3 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
    
    p {
      margin: 5px 0 0;
      font-size: 14px;
      color: #909399;
    }
  }
  
  .memory-info {
    p {
      margin: 10px 0;
      font-size: 14px;
      color: #606266;
    }
  }
}
</style> 