<template>
  <div class="contribution-graph">
    <div class="months">
      <span v-for="month in months" :key="month">{{ month }}</span>
    </div>
    <div class="graph-wrapper">
      <div class="weekdays">
        <span>周日</span>
        <span>周一</span>
        <span>周二</span>
        <span>周三</span>
        <span>周四</span>
        <span>周五</span>
        <span>周六</span>
      </div>
      <div class="graph">
        <div v-for="(week, weekIndex) in weeklyData" 
             :key="weekIndex" 
             class="week">
          <div v-for="(day, dayIndex) in week" 
               :key="dayIndex" 
               class="day"
               :class="getActivityClass(day.count)"
               :data-empty="day.count === -1"
               :title="day.date ? `${formatDate(day.date)} · ${day.count} 次贡献` : ''">
          </div>
        </div>
      </div>
    </div>
    <div class="legend">
      <span>较少</span>
        <div v-for="level in 5" 
             :key="level" 
             class="day"
             :class="`activity-${level - 1}`">
        </div>
      <span>较多</span>
    </div>
  </div>
</template>

<script setup lang="ts">
interface DayData {
  date: string;
  count: number;
}

import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

dayjs.locale('zh-cn')

const props = defineProps<{
  data: any
}>()


// 生成过去一年的空数据结构
const generateEmptyYear = () => {
  const data: DayData[] = []
  const endDate = dayjs()
  const startDate = dayjs().subtract(1, 'year').add(1, 'day')
  let currentDate = startDate
  
  while (currentDate.isBefore(endDate) || currentDate.isSame(endDate, 'day')) {
    data.push({
      date: currentDate.format('YYYY-MM-DD'),
      count: 0
    })
    currentDate = currentDate.add(1, 'day')
  }
  return data
}

// 合并实际数据到完整年份数据中
const mergedData = computed(() => {
  const emptyYear = generateEmptyYear()
  const dataMap = new Map(props.data?.map((item: DayData) => [item.date, item.count]))
  
  return emptyYear.map(day => ({
    date: day.date,
    count: dataMap.get(day.date) ?? 0
  })) as DayData[]
})

// 固定显示12个月份
const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

// 计算每个月开始的位置
const weeklyData = computed(() => {
  const weeks: DayData[][] = []
  let week: DayData[] = []
  const startDate = dayjs().subtract(1, 'year').add(1, 'day')
  let currentDate = startDate
  
  // 补充开始日期之前的空格子
  const startDayOfWeek = currentDate.day()
  for (let i = 0; i < startDayOfWeek; i++) {
    week.push({ date: '', count: -1 }) // -1 表示空格子
  }
  
  // 填充实际数据
  mergedData.value.forEach((day, index) => {
    week.push(day)
    if (week.length === 7) {
      weeks.push(week)
      week = []
    }
  })
  
  // 补充最后一周的空格子
  if (week.length > 0) {
    while (week.length < 7) {
      week.push({ date: '', count: -1 })
    }
    weeks.push(week)
  }
  
  return weeks
})

const formatDate = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY年M月D日 dddd')
}

const getActivityClass = (value: number) => {
  return `activity-${value}`
}
</script>

<style scoped>
.contribution-graph {
  padding: 20px;
  font-size: 14px;
}

.months {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  margin-bottom: 15px;
  color: #666;
  padding: 0 15px;
  margin-left: 20px;
  width: calc(100% - 30px);
}

.months span {
  text-align: center;
}

.graph-wrapper {
  display: flex;
  gap: 10px;
}

.weekdays {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 15px 0;
  color: #666;
  font-size: 12px;
}

.weekdays span {
  height: 15px;
  line-height: 15px;
  margin-bottom: 4px;
}

.graph {
  flex: 1;
  display: flex;
  gap: 4px;
  width: calc(100% - 30px);
  padding: 15px 0;
}

.week {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.day {
  width: 100%;
  aspect-ratio: 1;
  max-width: 15px;
  height: 15px;
  border-radius: 3px;
  background-color: #ebedf0;
  cursor: pointer;
  transition: transform 0.1s ease;
  &[data-empty="true"] {
    visibility: hidden;
  }
}

.day:hover {
  transform: scale(1.2);
}

.activity-0 { background-color: #ebedf0; }
.activity-1 { background-color: #9be9a8; }
.activity-2 { background-color: #40c463; }
.activity-3 { background-color: #30a14e; }
.activity-4 { background-color: #216e39; }

.legend {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
  justify-content: flex-end;
  color: #666;
  font-size: 14px;
}



/* 暗色主题适配 */
@media (prefers-color-scheme: dark) {
  .activity-0 { background-color: #161b22; }
  .activity-1 { background-color: #0e4429; }
  .activity-2 { background-color: #006d32; }
  .activity-3 { background-color: #26a641; }
  .activity-4 { background-color: #39d353; }
}
</style> 