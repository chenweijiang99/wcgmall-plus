<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form :model="queryParams" ref="queryFormRef" inline>
        <el-form-item label="参数名称" prop="configName">
          <el-input
            v-model="queryParams.configName"
            placeholder="请输入参数名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="参数键名" prop="configKey">
          <el-input
            v-model="queryParams.configKey"
            placeholder="请输入参数键名"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="系统内置" prop="configType">
          <el-select v-model="queryParams.configType" placeholder="请选择系统内置">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery"
            >搜索</el-button
          >
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-card class="box-card">
      <!-- 操作工具栏 -->
      <template #header>
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-permission="['sys:config:add']"
          >新增
        </el-button>
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
          v-permission="['sys:config:delete']"
          >批量删除
        </el-button>
      </template>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="dataList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="参数名称" align="center" prop="configName" />
        <el-table-column label="参数键名" align="center" prop="configKey" />
        <el-table-column label="参数键值" align="center" prop="configValue" />
        <el-table-column label="系统内置" align="center"prop="configType">
            <template #default="scope">
                <el-tag v-if="scope.row.configType === 'Y'" type="success">是</el-tag>
                <el-tag v-else type="danger">否</el-tag>
            </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" />
        <el-table-column label="更新时间" align="center" prop="updateTime" />
        <el-table-column label="备注" align="center" prop="remark" />
        <el-table-column
          label="操作"
          align="center"
          class-name="small-padding fixed-width"
        >
          <template #default="scope">
            <el-button
              type="primary"
              link
              icon="Edit"
              @click="handleUpdate(scope.row)"
              v-permission="['sys:config:update']"
              >修改
            </el-button>
            <el-button
              type="danger"
              link
              icon="Delete"
              @click="handleDelete(scope.row)"
              v-permission="['sys:config:delete']"
              >删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页工具栏 -->
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

      <!-- 添加或修改对话框 -->
      <el-dialog v-model="open" :title="title" width="500px" append-to-body>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
          <el-form-item label="参数名称" prop="configName">
            <el-input v-model="form.configName" placeholder="请输入参数名称" />
          </el-form-item>
          <el-form-item label="参数键名" prop="configKey">
            <el-input v-model="form.configKey" placeholder="请输入参数键名" />
          </el-form-item>
          <el-form-item label="参数键值" prop="configValue">
            <el-input v-model="form.configValue" placeholder="请输入参数键值" />
          </el-form-item>
          <el-form-item label="系统内置" prop="configType">
            <el-radio-group v-model="form.configType">
                <el-radio v-for="item in typeOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" placeholder="请输入备注" />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button type="primary" @click="submitForm">确 定</el-button>
            <el-button @click="cancel">取 消</el-button>
          </div>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from "element-plus";
import {
  listSysConfigApi,
  detailSysConfigApi,
  deleteSysConfigApi,
  addSysConfigApi,
  updateSysConfigApi,
} from "@/api/system/config";

// 遮罩层
const loading = ref(true);
// 选中数组
const selectedIds = ref<any[]>([]);
// 总条数
const total = ref(0);
// 表格数据
const dataList = ref([]);
// 弹出层标题
const title = ref("");
// 是否显示弹出层
const open = ref(false);
// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  configName: undefined,
  configKey: undefined,
  configType: undefined
});

const typeOptions = ref([
  { label: "是", value: "Y" },
  { label: "否", value: "N" },
]);
// 表单参数
const form = reactive<any>({});
// 表单校验
const rules = reactive({
  configName: [{ required: true, message: "参数名称不能为空", trigger: "blur" }],
  configKey: [{ required: true, message: "参数键名不能为空", trigger: "blur" }],
  configType: [{ required: true, message: "系统内置（Y是 N否）不能为空", trigger: "blur" }],
  configValue: [{ required: true, message: "参数键值不能为空", trigger: "blur" }],
});

const queryFormRef = ref();
const formRef = ref();

/** 查询列表 */
const getList = () => {
  loading.value = true;
  listSysConfigApi(queryParams).then((response) => {
    dataList.value = response.data.records;
    total.value = response.data.total;
    loading.value = false;
  });
};

/** 取消按钮 */
const cancel = () => {
  open.value = false;
  reset();
};

/** 表单重置 */
const reset = () => {
  form.id = undefined;
  form.configName = undefined;
  form.configKey = undefined;
  form.configValue = '';
  form.configType = 'N';
  form.remark = undefined;
  
  formRef.value?.resetFields();
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: { id: any }[]) => {
  selectedIds.value = selection.map((item) => item.id);
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  open.value = true;
  title.value = "添加参数配置";
};

/** 修改按钮操作 */
const handleUpdate = (row: any) => {
  reset();
  detailSysConfigApi(row.id).then((response) => {
    Object.assign(form, response.data);
    open.value = true;
    title.value = "修改参数配置";
  });
};

/** 提交按钮 */
const submitForm = () => {
  formRef.value?.validate((valid: any) => {
    if (valid) {
      if (form.id !== undefined) {
        updateSysConfigApi(form).then((response) => {
          ElMessage.success("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addSysConfigApi(form).then((response) => {
          ElMessage.success("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
};

/** 批量删除按钮操作 */
const handleBatchDelete = () => {
  if (!selectedIds.value.length) {
    return;
  }
  ElMessageBox.confirm(
    '是否确认删除"' + selectedIds.value.length + '"条数据项?',
    "警告",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  )
    .then(() => {
      deleteSysConfigApi(selectedIds.value);
    })
    .then(() => {
      getList();
      ElMessage.success("删除成功");
    });
};

/** 删除按钮操作 */
const handleDelete = (row: any) => {
  ElMessageBox.confirm('是否确认删除名称为"' + row.configName + '"的数据项?', "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      deleteSysConfigApi(row.id);
    })
    .then(() => {
      getList();
      ElMessage.success("删除成功");
    });
};

// 添加分页方法
const handleSizeChange = (val: any) => {
  queryParams.pageSize = val;
  getList();
};

const handleCurrentChange = (val: any) => {
  queryParams.pageNum = val;
  getList();
};

onMounted(() => {
  getList();
});
</script>
