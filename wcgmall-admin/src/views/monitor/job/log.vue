<template>
  <div class="app-container">
    <div class="search-wrapper">
      <!-- 搜索工具栏 -->
      <el-form :model="queryParams" ref="queryFormRef" :inline="true">
        <el-form-item label="任务名称" prop="jobName">
          <el-input v-model="queryParams.jobName" placeholder="请输入任务名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="任务组名" prop="jobGroup">
          <el-select v-model="queryParams.jobGroup" placeholder="请选择任务组名" clearable>
            <el-option v-for="dict in jobGroupOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择执行状态" clearable>
            <el-option v-for="dict in statusOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card>
      <!-- 操作工具栏 -->
      <template #header>
        <div class="card-header">
          <el-button v-permission="['sys:jobLog:delete']" type="danger" icon="Delete" :disabled="!selectedIds.length"
            @click="handleBatchDelete">批量删除</el-button>
          <el-button v-permission="['sys:jobLog:clean']" type="danger" icon="Delete"
            @click="handleClean">清空日志</el-button>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="logList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="任务名称" align="center" prop="jobName" :show-overflow-tooltip="true" />
        <el-table-column label="任务组名" align="center" prop="jobGroup">
          <template #default="{ row }">
            {{ jobGroupFormat(row) }}
          </template>
        </el-table-column>
        <el-table-column label="调用目标字符串" align="center" prop="invokeTarget" :show-overflow-tooltip="true" />
        <el-table-column label="日志信息" align="center" prop="jobMessage" :show-overflow-tooltip="true" />
        <el-table-column label="执行状态" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">
              {{ statusFormat(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="执行时间" align="center" prop="createTime" width="180" />
        <el-table-column label="操作" align="center" width="150">
          <template #default="{ row }">
            <el-button link type="primary" icon="Document" @click="handleDetail(row)">详情</el-button>
            <el-button v-permission="['sys:jobLog:delete']" link type="danger" icon="Delete"
              @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination background v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
          :total="total" :page-sizes="[10, 20, 30, 50]" layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawer" title="日志详情" direction="rtl" size="40%">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="日志编号">
          {{ form.id }}
        </el-descriptions-item>
        <el-descriptions-item label="任务名称">
          {{ form.jobName }}
        </el-descriptions-item>
        <el-descriptions-item label="任务组名">
          {{ jobGroupFormat(form) }}
        </el-descriptions-item>
        <el-descriptions-item label="调用方法">
          {{ form.invokeTarget }}
        </el-descriptions-item>
        <el-descriptions-item label="日志信息">
          {{ form.jobMessage }}
        </el-descriptions-item>
        <el-descriptions-item label="执行状态">
          <el-tag :type="form.status === 0 ? 'success' : 'danger'">
              {{ statusFormat(form) }}
            </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">
          {{ form.startTime }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间">
          {{ form.stopTime }}
        </el-descriptions-item>
        <el-descriptions-item label="异常信息">
          <pre style="white-space: pre-wrap; word-wrap: break-word; max-height: 300px;
           max-width: 500px; overflow-y: auto; margin: 0;">{{ form.exceptionInfo }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { listJobLogApi, delleteJobLogApi, cleanJobLogApi } from '@/api/monitor/jobLog'

// 遍历器
const queryFormRef = ref()

// 选中数组
const selectedIds = ref<Array<string | number>>([])

// 总条数
const total = ref(0)
// 日志表格数据
const logList = ref([])
const loading = ref(false)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  jobName: undefined,
  jobGroup: undefined,
  status: undefined
})

// 任务组名字典
const jobGroupOptions = [
  { value: 'DEFAULT', label: '默认' },
  { value: 'SYSTEM', label: '系统' }
]

// 状态字典
const statusOptions = [
  { value: 0, label: '成功' },
  { value: 1, label: '失败' }
]

// 抽屉
const drawer = ref(false)
// 详情表单
const form = ref<any>({})

// 任务组名格式化
const jobGroupFormat = (row: any) => {
  return jobGroupOptions.find(item => item.value === row.jobGroup)?.label
}

// 状态格式化
const statusFormat = (row: any) => {
  return statusOptions.find(item => item.value === row.status)?.label
}

/** 查询调度日志列表 */
const getList = async () => {
  loading.value = true
  try {
    const { data } = await listJobLogApi(queryParams)
    logList.value = data.records
    total.value = data.total
  } catch (error) {
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

/** 详情按钮操作 */
const handleDetail = (row: any) => {
  form.value = row
  drawer.value = true
}

/** 删除按钮操作 */
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除"' + row.jobName + '"这个调度日志吗？')
    await delleteJobLogApi(row.id)
    getList()
    ElMessage.success('删除成功')
  } catch (error) {
    selectedIds.value = []
  }
}

/** 批量删除按钮操作 */
const handleBatchDelete = async () => {

  if (!selectedIds.value?.length) {
    return ElMessage.warning('请选择要删除的数据')
  }
  try {
    await ElMessageBox.confirm('确定要删除"' + selectedIds.value.length + '"个调度日志吗？')
    await delleteJobLogApi(selectedIds.value)
    getList()
    ElMessage.success('删除成功')
  } catch (error) {
    selectedIds.value = []
  }
}

/** 清空按钮操作 */
const handleClean = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有调度日志吗？')
    await cleanJobLogApi()
    getList()
    ElMessage.success('清空成功')
  } catch (error) {
  }
}

/** 分页大小改变 */
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  getList()
}

/** 页码改变 */
const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val
  getList()
}

onMounted(() => {
  getList()
})
</script>