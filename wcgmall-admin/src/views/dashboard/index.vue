<template>
  <div class="dashboard-container">
    <!-- 数据卡片 -->
    <el-row :gutter="20" class="mb-20">
      <el-col :span="6" v-for="(item, index) in statistics" :key="item.title">
        <el-card shadow="hover" :body-style="{ padding: '20px' }" class="data-card"
          :style="{ animationDelay: `${index * 0.1}s` }">
          <div class="card-content">
            <div class="icon-wrapper" :class="item.type">
              <el-icon>
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="data-wrapper">
              <count-to :start-val="0" :end-val="item.value" :duration="2000" class="card-value" />
              <div class="card-title">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="mb-20">
      <!-- 用户增长趋势 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
            </div>
          </template>
          <div ref="userGrowthChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <!-- 订单趋势 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>订单趋势</span>
            </div>
          </template>
          <div ref="orderGrowthChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mb-20">
      <!-- 商品分类分布 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>商品分类分布</span>
            </div>
          </template>
          <div ref="categoryChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <!-- 数据总览 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>数据总览</span>
            </div>
          </template>
          <div class="overview-content">
            <div class="overview-item" v-for="(item, index) in overviewData" :key="item.label">
              <div class="overview-label">{{ item.label }}</div>
              <div class="overview-value">{{ item.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 表格区域 -->
    <el-row :gutter="20">
      <!-- 最新订单 -->
      <el-col :span="12">
        <el-card shadow="hover" class="table-card">
          <template #header>
            <div class="card-header">
              <span>最新订单</span>
              
              <!-- <el-button type="primary" size="small">查看全部</el-button> -->
            </div>
          </template>
          <el-table :data="latestOrders" stripe style="width: 100%" size="small">
            <el-table-column prop="id" label="订单号" width="180"></el-table-column>
            <el-table-column prop="userName" label="用户名" width="120"></el-table-column>
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="scope">
                <span>¥{{ scope.row.amount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusColor(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <!-- 最新商品 -->
      <el-col :span="12">
        <el-card shadow="hover" class="table-card">
          <template #header>
            <div class="card-header">
              <span>最新商品</span>
              <!-- <el-button type="primary" size="small">查看全部</el-button> -->
            </div>
          </template>
          <el-table :data="latestProducts" stripe style="width: 100%" size="small">
            <el-table-column prop="name" label="商品名称" width="180"></el-table-column>
            <el-table-column prop="price" label="价格" width="100">
              <template #default="scope">
                <span>¥{{ scope.row.price }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="inventory" label="库存" width="100"></el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                  {{ scope.row.status === 1 ? '上架' : '下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import type { EChartsOption, ECharts } from 'echarts'
import {
  CaretTop,
  CaretBottom,
  Document,
  Collection,
  ChatLineRound,
  View,
  ShoppingCart,
  Menu,
  Goods,
  PieChart,
  Star,
  Comment,
  Bell,
  UserFilled,
  User
} from '@element-plus/icons-vue'
import CountTo from '@/views/dashboard/components/CountTo.vue'
import { getDashboardDataApi } from '@/api/system'
import { onMounted, onUnmounted, ref, markRaw } from 'vue'

// 图表引用
const userGrowthChartRef = ref<HTMLElement>()
const orderGrowthChartRef = ref<HTMLElement>()
const categoryChartRef = ref<HTMLElement>()

// 图表实例
let userGrowthChart: ECharts | null = null
let orderGrowthChart: ECharts | null = null
let categoryChart: ECharts | null = null

// 图标定义
const icons = {
  User: markRaw(User),
  UserFilled: markRaw(UserFilled),
  Goods: markRaw(Goods),
  Category: markRaw(PieChart),
  Star: markRaw(Star),
  Order: markRaw(Bell),
  Document: markRaw(Document),
  Comment: markRaw(Comment),
  ShoppingCart: markRaw(ShoppingCart),
  Menu: markRaw(Menu)
}

// 数据卡片配置
const statistics = ref([
  { title: '用户总数', value: 0, type: 'success', icon: icons.User },
  { title: '角色总数', value: 0, type: 'danger', icon: icons.UserFilled },
  { title: '商品总数', value: 0, type: 'primary', icon: icons.Goods },
  { title: '商品分类', value: 0, type: 'danger', icon: icons.Category },
  { title: '商品品牌', value: 0, type: 'success', icon: icons.Star },
  { title: '订单总数', value: 0, type: 'warning', icon: icons.Order },
  { title: '博客总数', value: 0, type: 'info', icon: icons.Document },
  { title: '评论总数', value: 0, type: 'primary', icon: icons.Comment },
])

// 数据概览
const overviewData = ref([
  { label: '总销售额', value: 0, type: 'primary' },
  { label: '待付款订单', value: 0, type: 'warning' },
  { label: '今日新增用户', value: 0, type: 'success' },
  { label: '上架商品', value: 0, type: 'info' }
])

// 最新订单
const latestOrders = ref<Array<any>>([])

// 最新商品
const latestProducts = ref<Array<any>>([])

// 后端返回数据
const dashboardData = ref<any>(null)

// 订单状态颜色
// 订单状态 0待付款 1已付款 2待发货 3已发货 4已完成 5已取消 6已退款
const getStatusColor = (status: number) => {
  const colorMap: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'primary',
    4: 'success',
    5: 'danger',
    6: 'default'

  }
  return colorMap[status] || 'default'
}
const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '待付款',
    1: '已付款',
    2: '待发货',
    3: '已发货',
    4: '已完成',
    5: '已取消',
    6: '已退款'

  }
  return textMap[status] || '未知'
}

// 初始化用户增长图表
const initUserGrowthChart = () => {
  if (!userGrowthChartRef.value) return

  userGrowthChart = echarts.init(userGrowthChartRef.value)
  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c}人'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dashboardData.value?.userGrowth?.map((item: any) => item.date) || []
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '新增用户',
      type: 'line',
      smooth: true,
      data: dashboardData.value?.userGrowth?.map((item: any) => item.count) || [],
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(82, 196, 26, 0.5)' },
          { offset: 1, color: 'rgba(82, 196, 26, 0.1)' }
        ])
      },
      itemStyle: {
        color: '#52c41a'
      }
    }]
  }
  userGrowthChart.setOption(option)
}

// 初始化订单趋势图表
const initOrderGrowthChart = () => {
  if (!orderGrowthChartRef.value) return

  orderGrowthChart = echarts.init(orderGrowthChartRef.value)
  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>订单数: {c0}笔<br/>金额: ¥{c1}'
    },
    legend: {
      data: ['订单数', '订单金额'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dashboardData.value?.orderGrowth?.map((item: any) => item.date) || []
    },
    yAxis: [
      {
        type: 'value',
        name: '订单数',
        position: 'left'
      },
      {
        type: 'value',
        name: '订单金额',
        position: 'right'
      }
    ],
    series: [
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        data: dashboardData.value?.orderGrowth?.map((item: any) => item.count) || [],
        itemStyle: {
          color: '#1890ff'
        }
      },
      {
        name: '订单金额',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: dashboardData.value?.orderGrowth?.map((item: any) => item.amount) || [],
        itemStyle: {
          color: '#faad14'
        }
      }
    ]
  }
  orderGrowthChart.setOption(option)
}

// 初始化商品分类分布图表
const initCategoryChart = () => {
  if (!categoryChartRef.value) return

  categoryChart = echarts.init(categoryChartRef.value)
  const option: EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
    },
    series: [
      {
        name: '商品分类',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: dashboardData.value?.categoryDistribution || []
      }
    ]
  }
  categoryChart.setOption(option)
}

// 处理窗口大小变化
const handleResize = () => {
  userGrowthChart?.resize()
  orderGrowthChart?.resize()
  categoryChart?.resize()
}

// 初始化图表
const initCharts = () => {
  initUserGrowthChart()
  initOrderGrowthChart()
  initCategoryChart()
}

// 加载数据
const loadData = async () => {
  try {
    const res = await getDashboardDataApi()
    const data = res.data
    dashboardData.value = data

    // 更新数据卡片
    statistics.value[0].value = data.userCount || 0
    statistics.value[1].value = data.roleCount || 0
    statistics.value[2].value = data.productCount || 0
    statistics.value[3].value = data.categoryCount || 0
    statistics.value[4].value = data.brandCount || 0
    statistics.value[5].value = data.orderCount || 0
    statistics.value[6].value = data.blogCount || 0
    statistics.value[7].value = data.commentCount || 0

    overviewData.value[0].value = data.totalSales || 0
    overviewData.value[1].value = data.pendingOrders || 0
    overviewData.value[2].value = data.todayNewUsers || 0
    overviewData.value[3].value = data.activeProducts || 0

    // 更新表格数据
    latestOrders.value = data.latestOrders || []
    latestProducts.value = data.latestProducts || []

    // 初始化图表
    initCharts()
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  }
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  userGrowthChart?.dispose()
  orderGrowthChart?.dispose()
  categoryChart?.dispose()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.mb-20 {
  margin-bottom: 20px;
}

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

.icon-wrapper.danger {
  background: linear-gradient(135deg, #f5222d, #ff4d4f);
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

/* 图表样式 */
.chart-card {
  height: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.chart {
  height: 350px;
  width: 100%;
}

/* 表格样式 */
.table-card {
  height: auto;
}

/* 概览内容 */
.overview-content {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  padding: 20px 0;
}

.overview-item {
  text-align: center;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  transition: all 0.3s;
}

.overview-item:hover {
  background: #f0f0f0;
  transform: translateY(-2px);
}

.overview-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.overview-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

/* 暗色主题适配 */
@media (prefers-color-scheme: dark) {

  .card-value,
  .overview-value,
  .card-header span {
    color: #e6e6e6;
  }

  .overview-item {
    background: #1a1a1a;
  }

  .overview-item:hover {
    background: #2a2a2a;
  }
}

/* 响应式布局 */
@media screen and (max-width: 1200px) {
  .overview-content {
    grid-template-columns: 1fr;
  }
}

@media screen and (max-width: 768px) {
  .dashboard-container {
    padding: 10px;
  }

  .chart {
    height: 250px;
  }

  .card-value {
    font-size: 22px;
  }

  .overview-value {
    font-size: 20px;
  }
}
</style>