<template>
  <div class="app-container">
    <div class="search-wrapper">
      <!-- 搜索工具栏 -->
      <el-form :model="queryParams" ref="queryFormRef" :inline="true">
        <el-form-item label="任务名称" prop="jobName">
          <el-input
            v-model="queryParams.jobName"
            placeholder="请输入任务名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="任务组名" prop="jobGroup">
          <el-select v-model="queryParams.jobGroup" placeholder="请选择任务组名" clearable>
            <el-option
              v-for="dict in jobGroupOptions"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择任务状态" clearable>
            <el-option
              v-for="dict in statusOptions"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
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
          <el-button
            v-permission="['sys:job:add']"
            type="primary"
            icon="Plus"
            @click="handleAdd"
            >新增</el-button>
          <el-button
            v-permission="['sys:job:deleteBatch']"
            type="danger"
            icon="Delete"
            :disabled="!selectedIds.length"
            @click="handleBatcheDelete"
          >批量删除</el-button>
          <el-button
            type="info"
            icon="Document"
            @click="handleLog"
          >日志</el-button>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="jobList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="任务编号" align="center" prop="jobId" />
        <el-table-column label="任务名称" align="center" prop="jobName" :show-overflow-tooltip="true" />
        <el-table-column label="任务组名" align="center" prop="jobGroup">
          <template #default="{ row }">
            {{ jobGroupFormat(row) }}
          </template>
        </el-table-column>
        <el-table-column label="调用目标字符串" align="center" prop="invokeTarget" :show-overflow-tooltip="true" />
        <el-table-column label="cron执行表达式" align="center" prop="cronExpression" :show-overflow-tooltip="true" />
        <el-table-column label="状态" align="center" v-permission="['sys:job:update']">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="0"
              inactive-value="1"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="250">
          <template #default="{ row }">
            <el-button
              v-permission="['sys:job:update']"
              type="info"
              link
              icon="VideoPlay"
              @click="handleRun(row)"
            >执行一次</el-button>
            <el-button
              v-permission="['sys:job:update']"
              type="primary"
              link
              icon="Edit"
              @click="handleUpdate(row)"
            >修改</el-button>
            <el-button
              v-permission="['sys:job:delete']"
              link
              type="danger"
              icon="Delete"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        />
      </div>

      <!-- 添加或修改定时任务对话框 -->
      <el-dialog
        :title="title"
        v-model="open"
        width="700px"
        append-to-body
      >
        <el-form ref="jobFormRef" :model="form" :rules="rules" label-width="120px">
          <el-row>
            <el-col :span="12">
              <el-form-item label="任务名称" prop="jobName">
                <el-input v-model="form.jobName" placeholder="请输入任务名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="任务组名" prop="jobGroup">
                <el-select v-model="form.jobGroup" placeholder="请选择任务组名">
                  <el-option
                    v-for="dict in jobGroupOptions"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="调用方法" prop="invokeTarget" >
                <el-input v-model="form.invokeTarget" placeholder="请输入调用目标字符串">
                  <template #append>
                    <el-tooltip  placement="top">
                        <template #content>
                            Bean调用示例:neatTask.neatParams('neat')
                            <br />Class类调用示例:com.neat.quartz.taskQuartz.neatParams('neat')
                            <br />参数说明：支持字符串，布尔类型，长整型，浮点型，整型
                        </template>
                      <el-icon><QuestionFilled /></el-icon>
                    </el-tooltip>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="cron表达式" prop="cronExpression">
                <el-input v-model="form.cronExpression" placeholder="请输入cron执行表达式">
                  <template #append>
                    <el-tooltip content="Cron表达式生成器" placement="top">
                      <el-button @click="handleShowCron">
                        <el-icon><Timer /></el-icon>
                      </el-button>
                    </el-tooltip>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="执行策略" prop="misfirePolicy">
                <el-radio-group v-model="form.misfirePolicy">
                  <el-radio value="1">立即执行</el-radio>
                  <el-radio value="2">执行一次</el-radio>
                  <el-radio value="3">放弃执行</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="是否并发" prop="concurrent">
                <el-radio-group v-model="form.concurrent">
                  <el-radio value="0">允许</el-radio>
                  <el-radio value="1">禁止</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="cancel">取 消</el-button>
            <el-button type="primary" @click="submitForm">确 定</el-button>

          </div>
        </template>
      </el-dialog>

      <!-- Cron表达式生成器 -->
      <el-dialog top="5vh" title="Cron表达式生成器" v-model="cronVisible" width="700px" append-to-body>
        <CronTab
          v-model="form.cronExpression"
          :visible="cronVisible"
          @update:visible="cronVisible = $event"
        />
      </el-dialog>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listJobApi, getJobApi, addJobApi, updateJobApi, delJobApi,exportJobApi, changeJobStatusApi, runJobApi } from '@/api/monitor/job'
import CronTab from './components/CronTab.vue'

// 在这里初始化 router
const router = useRouter()

// 遍历器
const queryFormRef = ref()
const jobFormRef = ref()

// 选中数组
const selectedIds = ref<Array<string | number>>([])

const dialogVisible = ref(false)
// 非单个禁用
const single = ref(true)
// 非多个禁用
const multiple = ref(true)
// 显示搜索条件
const showSearch = ref(true)
// 总条数
const total = ref(0)
// 定时任务表格数据
const jobList = ref([])
// 弹出层标题
const title = ref('')
// 是否显示弹出层
const open = ref(false)
// cron表达式弹出层
const cronVisible = ref(false)
const loading = ref(false)
// 是否显示详细信息
const detailOpen = ref(false)
// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  jobName: undefined,
  jobGroup: undefined,
  status: undefined
})

// 表单参数
const form = reactive({
  jobId: undefined,
  jobName: undefined,
  jobGroup: undefined,
  invokeTarget: undefined,
  cronExpression: undefined,
  misfirePolicy: '1',
  concurrent: '1',
  status: '1'
})

// 表单校验
const rules = {
  jobName: [
    { required: true, message: '任务名称不能为空', trigger: 'blur' }
  ],
  jobGroup: [
    { required: true, message: '任务组名不能为空', trigger: 'change' }
  ],
  invokeTarget: [
    { required: true, message: '调用目标字符串不能为空', trigger: 'blur' }
  ],
  cronExpression: [
    { required: true, message: 'cron执行表达式不能为空', trigger: 'blur' }
  ]
}

// 任务组名字典
const jobGroupOptions = [
  { value: 'DEFAULT', label: '默认' },
  { value: 'SYSTEM', label: '系统' }
]

// 状态字典
const statusOptions = [
  { value: '0', label: '正常' },
  { value: '1', label: '暂停' }
]

// 任务组名格式化
const jobGroupFormat = (row: any) => {
  return jobGroupOptions.find(item => item.value === row.jobGroup)?.label
}

/** 查询定时任务列表 */
const getList = async () => {
  loading.value = true
  try {
    const { data } = await listJobApi(queryParams)
    jobList.value = data.records
    total.value = data.total
  } catch (error) {
  } finally {
    loading.value = false
  }
}

/** 取消按钮 */
const cancel = () => {
  open.value = false
  reset()
}

/** 表单重置 */
const reset = () => {
  form.jobId = undefined
  form.jobName = undefined
  form.jobGroup = undefined
  form.invokeTarget = undefined
  form.cronExpression = undefined
  form.misfirePolicy = '1'
  form.concurrent = '1'
  form.status = '1'
  jobFormRef.value?.resetFields()
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
  selectedIds.value = selection.map(item => item.jobId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 任务状态修改 */
const handleStatusChange = async (row: any) => {
  const text = row.status === '0' ? '启用' : '停用'
  try {
    await changeJobStatusApi(row.jobId, row.status)
    ElMessage.success(text + '成功')
  } catch (error) {
  }
}

/** 立即执行一次 */
const handleRun = async (row: any) => {
  try {
    await runJobApi(row)
    ElMessage.success('执行成功')
  } catch (error) {
  }
}

/** 显示Cron表达式生成器 */
const handleShowCron = () => {
  cronVisible.value = true
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset()
  open.value = true
  title.value = '添加定时任务'
}

/** 修改按钮操作 */
const handleUpdate = async (row: any) => {
  try {
    reset()
    const { data } = await getJobApi(row.jobId)
    Object.assign(form, data)
    open.value = true
    title.value = '修改定时任务'
  } catch (error) {
  }
}

/** 提交按钮 */
const submitForm = async () => {
  try {
    await jobFormRef.value.validate()
    if (form.jobId) {
      await updateJobApi(form)
      ElMessage.success('修改成功')
    } else {
      await addJobApi(form)
      ElMessage.success('新增成功')
    }
    open.value = false
    getList()
  } catch (error) {
  }
}


/** 删除按钮操作 */
const handleDelete = async (row?: any) => {
  try {
    await ElMessageBox.confirm('确定要删除"' + row.jobName + '"这个定时任务吗？')
    await delJobApi(row.jobId)
    getList()
    ElMessage.success('删除成功')
  } catch (error) {
  }
}

/** 批量删除按钮操作 */
const handleBatcheDelete = async () => {
  if (!selectedIds.value?.length) {
    return ElMessage.warning('请选择要删除的数据')
  }
  try {
    await ElMessageBox.confirm('确定要删除"' + selectedIds.value.length + '"个定时任务吗？')
    await delJobApi(selectedIds.value)
    getList()
    ElMessage.success('删除成功')
  } catch (error) {
  }
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    await exportJobApi(queryParams)
    ElMessage.success('导出成功')
  } catch (error) {
  }
}

/** 跳转任务日志页面 */
const handleLog = () => {
  router.push('/monitor/job-log')
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

<style lang="scss" scoped>
.mb8 {
  margin-bottom: 8px;
}
</style> 