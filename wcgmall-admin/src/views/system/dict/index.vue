<template>
  <div class="dict-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="字典名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入字典名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
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
      <template #header>
        <div class="card-header">
          <ButtonGroup>
            <el-button
              v-permission="['sys:dict:add']"
              type="primary"
              icon="Plus"
              @click="handleAdd"
            >新增</el-button>
            <el-button
              v-permission="['sys:dict:deleteBatch']"
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
        :data="dictList"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="字典名称" prop="name" align="center"/>
        <el-table-column label="字典类型" prop="type" align="center">
          <template #default="{ row }">
            <el-tag type="warning">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
        <el-table-column label="创建时间" align="center" prop="createTime" width="200" />
        <el-table-column label="操作" width="250" align="center">
          <template #default="{ row }">
            <el-button icon="List" type="success" link @click="handleData(row)">
              字典数据
            </el-button>
            <el-button
              v-permission="['sys:dict:update']"
              type="primary"
              link
              icon="Edit"
              @click="handleEdit(row)"
            >
              修改
            </el-button>
            <el-button 
              v-permission="['sys:dict:delete']"
              type="danger"
              link
              icon="Delete"
              @click="handleDelete(row)"
            >
              删除
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

    <!-- 字典类型对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增字典' : '修改字典'"
      width="500px"
      append-to-body
    >
      <el-form
        ref="dictFormRef"
        :model="dictForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="字典名称" prop="name">
          <el-input v-model="dictForm.name" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="type">
          <el-input :disabled="dialogType === 'edit'" v-model="dictForm.type" placeholder="请输入字典类型" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dictForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="dictForm.remark"
            type="textarea"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 字典数据对话框 -->
    <el-dialog
      v-model="dataDialogVisible"
      :title="`字典数据 - ${currentDict?.name}`"
      width="800px"
      append-to-body
      :close-on-click-modal="false"
    >
      <dict-data
        :dict-id="currentDict?.id"
        :dict-type="currentDict?.type"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import {
  getDictListApi,
  addDictApi,
  updateDictApi,
  deleteDictApi
} from '@/api/system/dict'
import DictData from './components/DictData.vue'
import ButtonGroup from '@/components/ButtonGroup/index.vue'

const loading = ref(false)
const total = ref(0)
const dictList = ref<any[]>([])
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const submitLoading = ref(false)
const dictFormRef = ref<FormInstance>()

// 查询参数
const queryParams = reactive<any>({
  pageNum: 1,
  pageSize: 10,
  name: '',
  type: '',
  status: ''
})

// 字典表单对象
const dictForm = reactive<Partial<any>>({
  name: '',
  type: '',
  status: '1',
  remark: ''
})

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入字典名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请输入字典类型', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 获取字典列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getDictListApi(queryParams)
    dictList.value = data.records
    total.value = data.total
  } catch (error) {
  }
  loading.value = false
}

// 搜索
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryParams.name = ''
  queryParams.status = ''
  handleQuery()
}

// 重置表单
const resetForm = () => {
  dictForm.id = undefined
  dictForm.name = ''
  dictForm.type = ''
  dictForm.status = 1
  dictForm.remark = ''
}

// 新增字典
const handleAdd = () => {
  resetForm()
  dialogType.value = 'add'
  dialogVisible.value = true
}

// 修改字典
const handleEdit = (row: any) => {
  resetForm()
  dialogType.value = 'edit'
  dialogVisible.value = true
  Object.assign(dictForm, row)
}

// 提交表单
const submitForm = async () => {
  if (!dictFormRef.value) return
  
  await dictFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          await addDictApi(dictForm)
          ElMessage.success('新增成功')
        } else {
          await updateDictApi(dictForm)
          ElMessage.success('修改成功')
        }
        dialogVisible.value = false
        getList()
      } catch (error) {
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 删除字典
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除字典"${row.name}"吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteDictApi(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
    }
  })
}

// 字典数据相关
const dataDialogVisible = ref(false)
const currentDict = ref<any>()

// 添加 handleData 方法
const handleData = (row: any) => {
  currentDict.value = row
  dataDialogVisible.value = true
}

// 分页方法
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  getList()
}

const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val
  getList()
}

// 添加选中项数组
const selectedIds = ref<number[]>([])

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
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedIds.value.length} 条记录吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteDictApi(selectedIds.value)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
    }
  })
}

// 初始化
getList()
</script>

<style scoped>

.search-form {
  margin-bottom: 20px;
}

</style> 