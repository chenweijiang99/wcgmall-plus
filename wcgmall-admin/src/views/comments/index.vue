<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form :model="queryParams" ref="queryFormRef" inline>
        <el-form-item label="模块标识" prop="module">
          <el-input
            v-model="queryParams.module"
            placeholder="请输入模块标识"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="评论内容" prop="comment">
          <el-input
            v-model="queryParams.comment"
            placeholder="请输入评论内容"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            v-permission="['sys:comments']"
            type="primary"
            icon="Search"
            @click="handleQuery"
            >搜索</el-button
          >
          <el-button v-permission="['sys:comments']" icon="Refresh" @click="resetQuery"
            >重置</el-button
          >
        </el-form-item>
      </el-form>
    </div>
    <el-card class="box-card">
      <!-- 操作工具栏 -->
      <template #header>
        <ButtonGroup>
          <el-button
            v-permission="['sys:comments:add']"
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            >新增
          </el-button>
          <el-button
            v-permission="['sys:comments:delete']"
            type="danger"
            plain
            icon="Delete"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
            >批量删除
          </el-button>
        </ButtonGroup>
      </template>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="dataList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <!-- <el-table-column label="评论ID" align="center" prop="id" /> -->
        <el-table-column label="父级评论ID" align="center" prop="pid" />
        <el-table-column label="关联ID" align="center" prop="fid" />
        <el-table-column label="模块标识" align="center" prop="module" />
        <el-table-column label="根节点ID" align="center" prop="rootId" />
        <el-table-column label="评论内容" align="center" prop="comment" />
        <el-table-column label="评论人ID" align="center" prop="userId" />
        <el-table-column label="评论时间" align="center" prop="createTime" />
        <el-table-column label="更新时间" align="center" prop="updateTime" />

        <el-table-column
          label="操作"
          align="center"
          class-name="small-padding fixed-width"
        >
          <template #default="scope">
            <el-button
              v-permission="['sys:comments:update']"
              type="primary"
              link
              icon="Edit"
              @click="handleUpdate(scope.row)"
              >修改
            </el-button>
            <el-button
              v-permission="['sys:comments:delete']"
              type="danger"
              link
              icon="Delete"
              @click="handleDelete(scope.row)"
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
          <el-form-item label="评论内容" prop="comment">
            <el-input v-model="form.comment" placeholder="请输入评论内容" />
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
  listCommentsApi,
  detailCommentsApi,
  deleteCommentsApi,
  addCommentsApi,
  updateCommentsApi,
} from "@/api/comments/comments";
import ButtonGroup from "@/components/ButtonGroup/index.vue";

// 遮罩层
const loading = ref(false);
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
  id: undefined,
  pid: undefined,
  fid: undefined,
  module: undefined,
  rootId: undefined,
  userId: undefined,
  createTime: undefined,
  updateTime: undefined,
  comment: undefined,
});

// 表单参数
const form = reactive<any>({});
// 表单校验
const rules = reactive({
  id: [{ required: true, message: "评论ID不能为空", trigger: "blur" }],
});

const queryFormRef = ref();
const formRef = ref();

/** 查询列表 */
const getList = async () => {
  loading.value = true;
  try {
    const { data } = await listCommentsApi(queryParams);
    dataList.value = data.records;
    total.value = data.total;
  } catch (error) {}
  loading.value = false;
};

/** 取消按钮 */
const cancel = () => {
  open.value = false;
  reset();
};

/** 表单重置 */
const reset = () => {
  Object.assign(form, {
    id: undefined,
    pid: undefined,
    fid: undefined,
    module: undefined,
    rootId: undefined,
    userId: undefined,
    createTime: undefined,
    updateTime: undefined,
    comment: undefined,
  });
  formRef.value?.resetFields();
};

/** 搜索按钮操作 */
const handleQuery = async () => {
  queryParams.pageNum = 1;
  await getList();
};

/** 重置按钮操作 */
const resetQuery = async () => {
  queryFormRef.value?.resetFields();
  await handleQuery();
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: { id: any }[]) => {
  selectedIds.value = selection.map((item) => item.id);
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  open.value = true;
  title.value = "添加";
};

/** 修改按钮操作 */
const handleUpdate = (row: any) => {
  reset();
  detailCommentsApi(row.id).then((response) => {
    Object.assign(form, response.data);
    open.value = true;
    title.value = "修改";
  });
};

/** 提交按钮 */
const submitForm = async () => {
  await formRef.value?.validate(async (valid: any) => {
    if (valid) {
      try {
        if (form.id !== undefined) {
          await updateCommentsApi(form);
          ElMessage.success("修改成功");
        } else {
          await addCommentsApi(form);
          ElMessage.success("新增成功");
        }
        open.value = false;
        getList();
      } catch (error) {}
    }
  });
};

/** 批量删除按钮操作 */
const handleBatchDelete = async () => {
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
  ).then(async () => {
    try {
      await deleteCommentsApi(selectedIds.value);
      ElMessage.success("删除成功");
      getList();
    } catch (error) {
      ElMessage.error("删除失败");
      console.error("删除失败:", error);
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row: any) => {
  ElMessageBox.confirm('是否确认删除编号为"' + row.id + '"的数据项?', "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    try {
      await deleteCommentsApi(row.id);
      ElMessage.success("删除成功");
      getList();
    } catch (error) {
      ElMessage.error("删除失败");
      console.error("删除失败:", error);
    }
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
