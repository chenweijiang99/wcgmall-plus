<template>
  <div class="app-container">
     <!-- 搜索表单 -->
     <div class="search-wrapper">
        <el-form :model="queryParams" ref="queryFormRef" :inline="true">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="queryParams.username"
            placeholder="请输入用户名"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="操作内容" prop="operation">
          <el-input
            v-model="queryParams.operation"
            placeholder="请输入操作内容"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
     </div>

    <el-card class="box-card">
      <!-- 操作按钮区域 -->
      <template #header>
        <div class="card-header">
          <ButtonGroup>
            <el-button
              v-permission="['sys:operateLog:delete']"
              type="danger"
              icon="Delete"
              :disabled="selectedIds.length === 0"
              @click="handleBatchDelete"
            >批量删除</el-button>
          </ButtonGroup>
        </div>
      </template>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="logList"
        @selection-change="handleSelectionChange"
      >
      <el-table-column type="expand">
          <template #default="scope">
            <el-scrollbar max-height="400px">
              <el-form label-position="left" inline class="demo-table-expand">
                <el-row>
                  <el-form-item label="请求接口">
                    <span>{{
                      scope.row.classPath + scope.row.requestUrl
                    }}</span>
                  </el-form-item>
                </el-row>
                <el-row>
                  <el-form-item label="请求参数">
                    <span>{{ scope.row.paramsJson }}</span>
                  </el-form-item>
                </el-row>
              </el-form>
            </el-scrollbar>
          </template>
        </el-table-column>
        <el-table-column align="center" type="selection" width="55" />
        <el-table-column
          prop="username"
          align="center"
          width="100"
          label="操作人"
        />
        <el-table-column
          prop="requestUrl"
          align="center"
          width="250"
          label="请求接口"
        />
        <el-table-column
          prop="type"
          align="center"
          width="100"
          label="请求方式"
        >
          <template #default="scope">
            <span v-for="item in methodStyle" :key="item.name">
              <el-tag v-if="scope.row.type === item.name" :type="item.type">{{
                scope.row.type
              }}</el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="operationName" align="center" label="接口名" />
        <el-table-column prop="ip" width="100" align="center" label="IP" />
        <el-table-column prop="source" align="center" label="IP来源"  width="200"/>
        <el-table-column align="center" label="请求耗时" width="150">
          <template #default="scope">
            <span
              ><el-tag type="info">{{ scope.row.spendTime }} ms</el-tag></span
            >
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          align="center"
          width="200"
          label="创建时间"
        />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleDelete(row)" v-permission="['sys:operateLog:delete']">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
          background
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOperationLogsApi,
  deleteOperationLogsApi,
} from '@/api/system/operLog'

const loading = ref(false)
const total = ref(0)
const logList = ref<any[]>([])
const selectedIds = ref<number[]>([])
const dateRange = ref<[string, string]>()


const methodStyle = ref<any[]>([
  {
    name: "POST",
    type: "success",
  },
  {
    name: "GET",
    type: "primary",
  },
  {
    name: "DELETE",
    type: "danger",
  },
  {
    name: "PUT",
    type: "warning",
  },
]);

// 查询参数
const queryParams = reactive<any>({
  pageNum: 1,
  pageSize: 10,
  username: '',
  operation: ''
})

// 监听日期范围变化
watch(dateRange, (val) => {
  if (val) {
    queryParams.startTime = val[0]
    queryParams.endTime = val[1]
  } else {
    queryParams.startTime = undefined
    queryParams.endTime = undefined
  }
})

// 获取日志列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getOperationLogsApi(queryParams)
    logList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取操作日志失败:', error)
  }
  loading.value = false
}

// 搜索
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  dateRange.value = undefined
  queryParams.pageNum = 1
  queryParams.username = ''
  queryParams.operation = ''
  queryParams.startTime = undefined
  queryParams.endTime = undefined
  getList()
}

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的记录')
    return
  }
  
  ElMessageBox.confirm(`是否确认删除 ${selectedIds.value.length} 个操作日志?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteOperationLogsApi(selectedIds.value)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
    }
  }).catch(() => {
  })
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除 ${row.username} 这个用户的操作日志吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteOperationLogsApi(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
    }
  }).catch(() => {
  })
}

// 分页大小变化
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  getList()
}

// 页码变化
const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val
  getList()
}

// 初始化
getList()
</script>
<style scoped>

.mb-2 {
  margin-bottom: 16px;
}


</style>
