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
        <el-form-item label="登录状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="成功" :value="0" />
            <el-option label="失败" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="登录时间">
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
              v-permission="['sys:loginLog:delete']"
              type="danger"
              icon="Delete"
              :disabled="selectedIds.length === 0"
              @click="handleBatchDelete"
            >批量删除</el-button>
            <el-button
              v-permission="['sys:loginLog:delete']"
              type="danger"
              icon="Delete"
              @click="handleClean"
            >清空日志</el-button>
          </ButtonGroup>
        </div>
      </template>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="logList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column align="center" type="selection" width="55" />
        <el-table-column
          prop="username"
          align="center"
          width="120"
          label="用户名"
        />
        <el-table-column prop="ip" width="130" align="center" label="登录IP" />
        <el-table-column prop="ipSource" align="center" label="IP来源" width="200" />
        <el-table-column prop="browser" align="center" label="浏览器" width="120" />
        <el-table-column prop="os" align="center" label="操作系统" width="150" />
        <el-table-column
          prop="status"
          align="center"
          width="100"
          label="登录状态"
        >
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="success">成功</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loginType" align="center" label="登录方式" width="100">
          <template #default="scope">
            <el-tag type="info">{{ scope.row.loginType || '账号密码' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" align="center" label="提示消息" min-width="150" />
        <el-table-column
          prop="loginTime"
          align="center"
          width="180"
          label="登录时间"
        />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleDelete(row)" v-permission="['sys:loginLog:delete']">
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
  getLoginLogsApi,
  deleteLoginLogsApi,
  cleanLoginLogsApi,
} from '@/api/system/loginLog'

const loading = ref(false)
const total = ref(0)
const logList = ref<any[]>([])
const selectedIds = ref<number[]>([])
const dateRange = ref<[string, string]>()

// 查询参数
const queryParams = reactive<any>({
  pageNum: 1,
  pageSize: 10,
  username: '',
  status: undefined
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
    const { data } = await getLoginLogsApi(queryParams)
    logList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取登录日志失败:', error)
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
  queryParams.status = undefined
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

  ElMessageBox.confirm(`是否确认删除 ${selectedIds.value.length} 条登录日志?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteLoginLogsApi(selectedIds.value)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
    }
  }).catch(() => {
  })
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除 ${row.username} 的登录日志吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteLoginLogsApi(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
    }
  }).catch(() => {
  })
}

// 清空日志
const handleClean = () => {
  ElMessageBox.confirm('是否确认清空所有登录日志?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cleanLoginLogsApi()
      ElMessage.success('清空成功')
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
