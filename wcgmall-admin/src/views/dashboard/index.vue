<template>
  <div class="dashboard-container">
    <!-- 数据卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="(item, index) in statistics" :key="item.title">
        <el-card 
          shadow="hover" 
          :body-style="{ padding: '20px' }"
          class="data-card"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <div class="card-content">
            <div class="icon-wrapper" :class="item.type">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="data-wrapper">
              <count-to
                :start-val="0"
                :end-val="item.value"
                :duration="2000"
                class="card-value"
              />
              <div class="card-title">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 作者信息卡片 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card 
          shadow="hover" 
          :body-style="{ padding: '24px' }"
          class="author-card"
        >
          <div class="author-content">
            <div class="author-avatar">
              <img :src="authorInfo.avatar" alt="作者头像" />
            </div>
            <div class="author-details">
              <h3 class="author-name">{{ authorInfo.name }}</h3>
              <p class="author-title">{{ authorInfo.title }}</p>
              <p class="author-description">{{ authorInfo.description }}</p>
              <div class="author-contact">
                <el-tag v-for="contact in authorInfo.contacts" :key="contact.type" :type="contact.type" size="small" style="margin-right: 8px; margin-top: 8px;">
                  {{ contact.label }}: {{ contact.value }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    </div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { 
  CaretTop, 
  CaretBottom,
  Document,
  Collection,
  ChatLineRound,
  View
} from '@element-plus/icons-vue'
import CountTo from '@/views/dashboard/components/CountTo.vue'
import ContributionGraph from './components/ContributionGraph.vue'
import { getDashboardDataApi } from '@/api/system'

const icons = {
  Document: markRaw(Document),
  Collection: markRaw(Collection),
  ChatLineRound: markRaw(ChatLineRound),
  View: markRaw(View),
  CaretTop: markRaw(CaretTop),
  CaretBottom: markRaw(CaretBottom)
}

const statistics = ref([
  { 
    title: '用户总数', 
    value: 0, 
    type: 'success',
    icon: icons.Collection
  },
  { 
    title: '角色总数', 
    value: 0, 
    type: 'warning',
    icon: icons.ChatLineRound
  },
])

const contributionData = ref([])

// 作者信息
defineOptions({
  name: 'Dashboard'
})

const authorInfo = ref({
  name: 'River',
  title: '全栈开发工程师',
  description: '致力于构建高效、美观的Web应用，拥有丰富的前后端开发经验。这个系统是个人学习和实践的项目，希望能够帮助到更多开发者。',
  avatar: 'https://picsum.photos/id/1/200/200', // 使用占位图片
  contacts: [
    { type: 'primary', label: 'GitHub', value: 'github.com/example' },
    { type: 'success', label: '邮箱', value: 'example@mail.com' },
    { type: 'warning', label: '博客', value: 'blog.example.com' }
  ]
})

// 图表相关
const lineChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
const lineChart = shallowRef<echarts.ECharts | null>(null)
const pieChart = shallowRef<echarts.ECharts | null>(null)


// 处理窗口大小变化
const handleResize = () => {
  lineChart.value?.resize()
  pieChart.value?.resize()
}


onMounted(() => {
  getDashboardDataApi().then(res => {
    statistics.value[0].value = res.data.userCount
    statistics.value[1].value = res.data.roleCount
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  lineChart.value?.dispose()
  pieChart.value?.dispose()
})
</script>

<style scoped>

/* 数据卡片样式 */
.data-card {
  animation: slideUp 0.5s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);
}

@keyframes slideUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.icon-wrapper {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s;
}

.icon-wrapper:hover {
  transform: scale(1.1);
}

.icon-wrapper .el-icon {
  font-size: 30px;
  color: #fff;
}

.icon-wrapper.primary {
  background: linear-gradient(135deg, #1890ff, #36a9ff);
}

.icon-wrapper.success {
  background: linear-gradient(135deg, #52c41a, #73d13d);
}

.icon-wrapper.warning {
  background: linear-gradient(135deg, #faad14, #ffc53d);
}

.icon-wrapper.info {
  background: linear-gradient(135deg, #13c2c2, #36cfc9);
}

.data-wrapper {
  flex: 1;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

/* 图表区域样式 */
.chart-row {
  margin-top: 20px;
}

.chart {
  height: 350px;
  width: 100%;
}

.chart-card {
  height: auto;
  margin-bottom: 20px;
}

/* 暗色主题适配 */
@media (prefers-color-scheme: dark) {
  .card-value {
    color: #e6e6e6;
  }

  .chart-placeholder {
    background: #1a1a1a;
    color: #909399;
  }

  .author-name {
    color: #e6e6e6;
  }

  .author-title,
  .author-description {
    color: #909399;
  }
}

/* 作者信息卡片样式 */
.author-card {
  animation: slideUp 0.5s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);
  animation-delay: 0.4s;
}

.author-content {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.author-avatar {
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s, box-shadow 0.3s;
}

.author-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-details {
  flex: 1;
}

.author-name {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
  margin-top: 4px;
}

.author-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 12px;
}

.author-description {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  margin-bottom: 16px;
  word-break: break-word;
}

.author-contact {
  display: flex;
  flex-wrap: wrap;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .author-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .author-avatar {
    width: 80px;
    height: 80px;
  }
  
  .author-name {
    font-size: 20px;
  }
  
  .author-contact {
    justify-content: center;
  }
}
</style>