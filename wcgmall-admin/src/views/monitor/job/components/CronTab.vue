<template>
  <div class="cron-container">
    <el-tabs v-model="activeName" class="cron-tabs">
      <el-tab-pane v-for="(tab, index) in tabList" :key="index" :label="tab.label" :name="tab.name">
        <div class="tab-content">
          <!-- 基础选项 -->
          <div class="radio-group">
            <el-radio-group v-model="timeUnits[tab.name].type">
              <el-radio 
                v-for="option in tab.options" 
                :key="option.value" 
                :value="option.value"
                class="radio-item"
              >
                {{ option.label }}
              </el-radio>
            </el-radio-group>
          </div>

          <!-- 周期选择 -->
          <div v-if="timeUnits[tab.name].type === '2'" class="cycle-select">
            <span class="cycle-text">从</span>
            <el-input-number 
              v-model="timeUnits[tab.name].start" 
              :min="tab.range?.min || 0" 
              :max="tab.range?.max || 59"
              controls-position="right"
            />
            <span class="cycle-text">{{ tab.unit }}开始，每</span>
            <el-input-number 
              v-model="timeUnits[tab.name].interval" 
              :min="1" 
              :max="tab.range?.max || 59"
              controls-position="right"
            />
            <span class="cycle-text">{{ tab.unit }}执行一次</span>
          </div>

          <!-- 指定值选择 -->
          <div v-if="timeUnits[tab.name].type === '3'" class="specific-select">
            <el-checkbox-group v-model="timeUnits[tab.name].specificValues">
              <template v-if="tab.name === 'week'">
                <el-checkbox 
                  v-for="day in weekDays" 
                  :key="day.value" 
                  :label="day.value"
                  class="checkbox-item"
                >
                  {{ day.label }}
                </el-checkbox>
              </template>
              <template v-else-if="tab.name === 'year'">
                <el-checkbox 
                  v-for="year in Array.from({length: 11}, (_, i) => currentYear + i)" 
                  :key="year" 
                  :label="year"
                  class="checkbox-item"
                >
                  {{ year }}
                </el-checkbox>
              </template>
              <template v-else>
                <el-checkbox 
                  v-for="i in tab.range?.max + 1" 
                  :key="i-1" 
                  :value="i-1"
                  class="checkbox-item"
                >
                  {{ i-1 }}
                </el-checkbox>
              </template>
            </el-checkbox-group>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <div class="result-box">
      <div class="expression-header">
        <div class="expression-title">
          <el-icon><Document /></el-icon>
          <span>Cron 表达式</span>
        </div>
        <el-tooltip content="复制表达式" placement="top">
          <el-button type="primary" link @click="handleCopy">
            <el-icon><DocumentCopy /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
      
      <el-input v-model="cronExpression" readonly class="expression-input">
        <template #prepend>
          <el-icon><Timer /></el-icon>
        </template>
      </el-input>

      <div class="expression-desc">
        <el-alert
          type="info"
          :closable="false"
          show-icon
        >
          <template #title>
            表达式从左到右（用空格隔开）：秒 分 小时 日期 月份 星期 年份
          </template>
        </el-alert>
      </div>

      <div class="next-execute-time" v-if="nextExecuteTime">
        <el-tag type="success" effect="plain">
          <el-icon><Timer /></el-icon>
          <span>下次执行时间：{{ nextExecuteTime }}</span>
        </el-tag>
      </div>

      <div class="next-times" v-if="nextTimes.length">
        <div class="next-times-header">
          <el-icon><Timer /></el-icon>
          <span>最近5次执行时间</span>
        </div>
        <div class="next-times-list">
          <el-tag 
            v-for="(time, index) in nextTimes" 
            :key="index"
            type="success"
            effect="light"
            class="next-time-item"
          >
            {{ time }}
          </el-tag>
        </div>
      </div>

      <div class="action-bar">
        <el-button type="primary" @click="handleConfirm">确 定</el-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ElMessage } from 'element-plus'
import parser from 'cron-parser'
import dayjs from 'dayjs'

const props = defineProps({
  modelValue: {
    type: String,
    default: '* * * * * ?'
  },
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'update:visible'])

const activeName = ref('second')
const nextExecuteTime = ref('')

// 修改时间单位的配置方式
const timeUnits = reactive<any>({
  second: {
    type: '1',
    start: 0,
    interval: 1,
    specificValues: []
  },
  minute: {
    type: '1',
    start: 0,
    interval: 1,
    specificValues: []
  },
  hour: {
    type: '1',
    start: 0,
    interval: 1,
    specificValues: []
  },
  day: {
    type: '1',
    start: 1,
    interval: 1,
    specificValues: []
  },
  month: {
    type: '1',
    start: 1,
    interval: 1,
    specificValues: []
  },
  week: {
    type: '4',
    start: 1,
    interval: 1,
    specificValues: []
  },
  year: {
    type: '1',
    start: new Date().getFullYear(),
    interval: 1,
    specificValues: []
  }
})

// 标签页配置
const currentYear = new Date().getFullYear()
const tabList = [
  {
    label: '秒',
    name: 'second',
    unit: '秒',
    range: { min: 0, max: 59 },
    options: [
      { label: '每秒', value: '1' },
      { label: '周期', value: '2' },
      { label: '指定', value: '3' }
    ]
  },
  {
    label: '分钟',
    name: 'minute',
    unit: '分',
    range: { min: 0, max: 59 },
    options: [
      { label: '每分', value: '1' },
      { label: '周期', value: '2' },
      { label: '指定', value: '3' }
    ]
  },
  {
    label: '小时',
    name: 'hour',
    unit: '小时',
    range: { min: 0, max: 23 },
    options: [
      { label: '每小时', value: '1' },
      { label: '周期', value: '2' },
      { label: '指定', value: '3' }
    ]
  },
  {
    label: '日期',
    name: 'day',
    unit: '日',
    range: { min: 1, max: 31 },
    options: [
      { label: '每日', value: '1' },
      { label: '周期', value: '2' },
      { label: '指定', value: '3' },
      { label: '工作日', value: '4' },
      { label: '不指定', value: '5' },
      { label: '本月最后一天', value: '6' }
    ]
  },
  {
    label: '月份',
    name: 'month',
    unit: '月',
    range: { min: 1, max: 12 },
    options: [
      { label: '每月', value: '1' },
      { label: '周期', value: '2' },
      { label: '指定', value: '3' }
    ]
  },
  {
    label: '星期',
    name: 'week',
    unit: '周',
    range: { min: 1, max: 7 },
    options: [
      { label: '每周', value: '1' },
      { label: '周期', value: '2' },
      { label: '指定', value: '3' },
      { label: '不指定', value: '4' },
      { label: '本月最后一个工作日', value: '5' }
    ]
  },
  {
    label: '年',
    name: 'year',
    unit: '年',
    range: { min: currentYear, max: currentYear + 10 },
    options: [
      { label: '每年', value: '1' },
      { label: '周期', value: '2' },
      { label: '指定', value: '3' }
    ]
  }
]

// 生成cron表达式
const cronExpression = computed(() => {
  const parts = []
  
  // 秒
  switch (timeUnits.second.type) {
    case '1': parts[0] = '*'; break
    case '2': parts[0] = `${timeUnits.second.start}/${timeUnits.second.interval}`; break
    case '3': parts[0] = timeUnits.second.specificValues.sort((a: any, b: any) => a - b).join(',') || '*'; break
  }
  
  // 分
  switch (timeUnits.minute.type) {
    case '1': parts[1] = '*'; break
    case '2': parts[1] = `${timeUnits.minute.start}/${timeUnits.minute.interval}`; break
    case '3': parts[1] = timeUnits.minute.specificValues.sort((a: any, b: any) => a - b).join(',') || '*'; break
  }
  
  // 时
  switch (timeUnits.hour.type) {
    case '1': parts[2] = '*'; break
    case '2': parts[2] = `${timeUnits.hour.start}/${timeUnits.hour.interval}`; break
    case '3': parts[2] = timeUnits.hour.specificValues.sort((a: any, b: any) => a - b).join(',') || '*'; break
  }
  
  // 日
  switch (timeUnits.day.type) {
    case '1': parts[3] = '*'; break
    case '2': parts[3] = `${timeUnits.day.start}/${timeUnits.day.interval}`; break
    case '3': parts[3] = timeUnits.day.specificValues.sort((a: any, b: any) => a - b).join(',') || '*'; break
    case '4': parts[3] = 'W'; break
    case '5': parts[3] = '?'; break
    case '6': parts[3] = 'L'; break
  }
  
  // 月
  switch (timeUnits.month.type) {
    case '1': parts[4] = '*'; break
    case '2': parts[4] = `${timeUnits.month.start}/${timeUnits.month.interval}`; break
    case '3': parts[4] = timeUnits.month.specificValues.sort((a: any, b: any) => a - b).join(',') || '*'; break
  }
  
  // 周
  switch (timeUnits.week.type) {
    case '1': parts[5] = '*'; break
    case '2': parts[5] = `${timeUnits.week.start}/${timeUnits.week.interval}`; break
    case '3': parts[5] = timeUnits.week.specificValues.sort((a: any, b: any) => a - b).join(',') || '*'; break
    case '4': parts[5] = '?'; break
    case '5': parts[5] = 'L'; break
  }
  
  // 年
  switch (timeUnits.year.type) {
    case '1': parts[6] = '*'; break
    case '2': parts[6] = `${timeUnits.year.start}/${timeUnits.year.interval}`; break
    case '3': parts[6] = timeUnits.year.specificValues.sort((a: any, b: any) => a - b).join(',') || '*'; break
  }
  
  return parts.filter(Boolean).join(' ')
})

// 复制到剪贴板
const handleCopy = () => {
  navigator.clipboard.writeText(cronExpression.value)
    .then(() => ElMessage.success('复制成功'))
    .catch(() => ElMessage.error('复制失败'))
}

// 添加预览时间相关的响应式变量
const nextTimes = ref<string[]>([])

// 修改计算下次执行时间的方法
const calculateNextExecuteTimes = (expression: string) => {
  try {
    // 处理表达式
    let parsableExpression = expression

    // 如果表达式包含年份字段，需要移除它，因为 cron-parser 默认不支持年份
    const parts = parsableExpression.split(' ')
    if (parts.length === 7) {
      parsableExpression = parts.slice(0, 6).join(' ')
    }

    // 替换特殊字符
    parsableExpression = parsableExpression
      .replace(/\?/g, '*')    // 将 ? 替换为 *
      .replace(/[Ll]/g, '*')  // 将 L 替换为 *
      .replace(/[Ww]/g, '*')  // 将 W 替换为 *

    // 解析处理后的表达式
    const interval = parser.parseExpression(parsableExpression, {
      currentDate: new Date(),
      iterator: true,
      utc: false  // 改为 false，使用本地时间
    })
    
    const times: string[] = []
    
    // 获取接下来5次执行时间
    for (let i = 0; i < 5; i++) {
      const next = interval.next()
      if (next.done) break
      
      // 直接使用本地时间格式化
      const localTime = dayjs(next.value.toDate()).format('YYYY-MM-DD HH:mm:ss')
      times.push(localTime)
    }
    
    nextTimes.value = times
    
    // 设置下次执行时间
    if (times.length > 0) {
      nextExecuteTime.value = times[0]
    }
  } catch (error) {
    console.error('计算执行时间失败:', error)
    nextTimes.value = []
    nextExecuteTime.value = ''
    
    // 只在真正的表达式错误时显示错误提示
    if (!expression.includes('?') && !expression.includes('L') && !expression.includes('W')) {
      ElMessage.error('Cron表达式格式错误')
    }
  }
}

// 修改 watch 监听逻辑
watch(cronExpression, (newValue) => {
  emit('update:modelValue', newValue)
  // 当表达式有效时计算执行时间
  if (newValue && newValue.split(' ').length >= 6) {
    calculateNextExecuteTimes(newValue)
  } else {
    nextTimes.value = []
    nextExecuteTime.value = ''
  }
}, { immediate: true })

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    parseCronExpression(newValue)
  }
})

// 解析cron表达式
const parseCronExpression = (expression: string) => {
  const parts = expression.split(' ')
  // 根据parts的值设置各个时单位的type和值
  // ... 实现解析逻辑
}

// 添加周日到周六的配置
const weekDays = [
  { label: '周日', value: '1' },
  { label: '周一', value: '2' },
  { label: '周二', value: '3' },
  { label: '周三', value: '4' },
  { label: '周四', value: '5' },
  { label: '周五', value: '6' },
  { label: '周六', value: '7' }
]

// 修改确定按钮处理函数
const handleConfirm = () => {
  // 发送当前表达式
  emit('confirm', cronExpression.value)
  // 更新表达式值
  emit('update:modelValue', cronExpression.value)
  // 关闭弹框
  emit('update:visible', false)
  // 显示成功提示
  ElMessage.success('已更新表达式')
}
</script>

<style lang="scss" scoped>
.cron-container {
  height: 500px;  // 保持整体高度
  overflow-y: auto; // 保持外层滚动条

  :deep(.el-tabs__content) {
    height: auto;  // 改为自适应高度
    overflow-y: visible; // 移除内部滚动条
  }

  .tab-content {
    padding: 24px;
    height: auto; // 改为自适应高度
  }

  .cron-tabs {
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    :deep(.el-tabs__header) {
      margin-bottom: 0;
      padding: 0 20px;
      border-bottom: 1px solid #f0f0f0;
      
      .el-tabs__nav-wrap::after {
        display: none;
      }
      
      .el-tabs__item {
        height: 48px;
        line-height: 48px;
        font-size: 14px;
        
        &.is-active {
          font-weight: 600;
        }
      }
    }
  }

  .radio-group {
    
    .radio-item {
      margin-right: 32px;
      margin-bottom: 16px;
      
      &:hover {
        .el-radio__label {
          color: var(--el-color-primary);
        }
      }
    }
  }

  .cycle-select {
    display: flex;
    align-items: center;
    padding: 16px;
    border-radius: 8px;
    
    .cycle-text {
      margin: 0 12px;
      color: var(--el-text-color-regular);
    }
    
    .el-input-number {
      width: 100px;
    }
  }

  .specific-select {
    padding: 16px;
    border-radius: 8px;
    background-color: #9093994d;;
    .el-checkbox-group {
      display: flex;
      flex-wrap: wrap;  // 添加自动换行
      width: 100%;      // 确保占满容器宽度
    }
    
    .checkbox-item {
      margin-right: 16px;
      margin-bottom: 12px;
      
      &:hover {
        .el-checkbox__label {
          color: var(--el-color-primary);
        }
      }
    }
  }

  .result-box {
    margin-top: 24px;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

    .expression-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .expression-title {
        display: flex;
        align-items: center;
        font-size: 16px;
        font-weight: 500;
        
        .el-icon {
          margin-right: 8px;
          font-size: 18px;
          color: var(--el-color-primary);
        }
      }
    }

    .expression-input {
      :deep(.el-input-group__prepend) {
        
        .el-icon {
          color: var(--el-color-primary);
        }
      }
    }

    .expression-desc {
      margin-top: 16px;
    }

    .next-execute-time {
      margin-top: 16px;
      .el-tag {
        display: flex;
        align-items: center;
        padding: 8px 12px;
        
        .el-icon {
          margin-right: 8px;
        }
      }
    }

    .next-times {
      margin-top: 20px;
      padding: 16px;
      border-radius: 8px;
      background-color: #9093994d;;
      .next-times-header {
        display: flex;
        align-items: center;
        margin-bottom: 12px;
        font-size: 14px;
        color: var(--el-text-color-primary);
        
        .el-icon {
          margin-right: 8px;
          color: var(--el-color-success);
        }
      }
      
      .next-times-list {
        display: flex;
        flex-direction: column;
        gap: 8px;
        
        .next-time-item {
          width: fit-content;
          padding: 6px 12px;
          font-size: 13px;
        }
      }
    }

    .action-bar {
      margin-top: 20px;
      padding-top: 20px;
      border-top: 1px solid var(--el-border-color-light);
      display: flex;
      justify-content: flex-end;
      
      .el-button {
        padding: 8px 24px;
      }
    }
  }
}

// 暗色主题适配
html.dark {
  .cron-container {
    .cron-tabs,
    .result-box {
      background: var(--el-bg-color);
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    }

    .cycle-select,
    .specific-select {
      background: var(--el-fill-color-dark);
    }
  }
}
</style> 