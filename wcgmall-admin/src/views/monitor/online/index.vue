<template>
    <div class="app-container">
      <!-- 搜索表单 -->
      <div class="search-wrapper">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true">
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="queryParams.username"
              placeholder="请输入用户名"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
  
      <!-- 操作按钮区域 -->
      <el-card class="box-card">
        <!-- 数据表格 -->
        <el-table
          v-loading="loading"
          :data="tableData"
          style="width: 100%"
        >
          <el-table-column type="selection"  width="55" align="center" />
          <el-table-column label="会话凭证" align="center" width="180" prop="tokenValue" show-overflow-tooltip />
          <el-table-column label="账号" align="center" prop="username" show-overflow-tooltip />
          <el-table-column label="登录IP" align="center" prop="ip" width="180" />
          <el-table-column label="登录地点" align="center" prop="ipLocation" width="180" />
          <el-table-column label="浏览器" align="center" prop="browser" width="180" />
          <el-table-column label="操作系统" align="center" prop="os" width="180" />
          <el-table-column label="登录时间" align="center" prop="lastLoginTime" width="180" />
          <el-table-column label="操作" align="center" width="280" fixed="right">
            <template #default="scope">
              <el-button
                v-permission="['monitor:online:forceLogout']"
                type="danger"
                link
                icon="Delete"
                @click="handleDelete(scope.row)"
              >强退</el-button>
            </template>
          </el-table-column>
        </el-table>
  
        <!-- 分页组件 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[10, 20, 30, 50]"
            :total="total"
            :background="true"
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
  import type { FormInstance } from 'element-plus'
  import {
    getOnlineUserApi,
    forceLogoutApi
  } from '@/api/system/user'
  
  // 查询参数
  const queryParams = reactive({
    pageNum: 1,
    pageSize: 10,
    username: null
  })
  
  const loading = ref(false)
  const total = ref(0)
  const tableData = ref([])
  const queryFormRef = ref<FormInstance>()
  
  
  // 获取标签列表
  const getList = async () => {
    loading.value = true
    try {
      const { data } = await getOnlineUserApi(queryParams)
      tableData.value = data.records
      total.value = data.total
    } catch (error) {
    }
    loading.value = false
  }
  
  // 删除
  const handleDelete = (row: any) => {
    ElMessageBox.confirm(`是否确认强退 ${row.tokenValue} 这个用户?`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await forceLogoutApi(row.tokenValue)
        ElMessage.success('强制下线成功')
        getList()
      } catch (error) {
      }
    })
  }

  // 搜索
  const handleQuery = () => {
    queryParams.pageNum = 1
    getList()
  }
  
  // 重置查询
  const resetQuery = () => {
    queryFormRef.value?.resetFields()
    handleQuery()
  }
  
  // 分页大小改变
  const handleSizeChange = (val: number) => {
    queryParams.pageSize = val
    getList()
  }
  
  // 页码改变
  const handleCurrentChange = (val: number) => {
    queryParams.pageNum = val
    getList()
  }
  
  // 初始化
  onMounted(() => {
    getList()
  })
  </script>