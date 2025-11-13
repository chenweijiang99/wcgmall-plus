<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form :model="queryParams" ref="queryFormRef" inline>
        <el-form-item label="订单编号" prop="orderNumber">
          <el-input
            v-model="queryParams.orderNumber"
            placeholder="请输入订单编号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item
          label="订单状态 1待付款 2待发货 3已发货 4已完成 5已取消 6已退款"
          prop="status"
        >
          <el-input
            v-model="queryParams.status"
            placeholder="请输入订单状态 1待付款 2待发货 3已发货 4已完成 5已取消 6已退款"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="支付方式" prop="payMethod">
          <el-input
            v-model="queryParams.payMethod"
            placeholder="请输入支付方式"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="支付状态 0未支付 1已支付 2退款" prop="payStatus">
          <el-input
            v-model="queryParams.payStatus"
            placeholder="请输入支付状态 0未支付 1已支付 2退款"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            v-permission="['sys:order']"
            type="primary"
            icon="Search"
            @click="handleQuery"
            >搜索</el-button
          >
          <el-button v-permission="['sys:order']" icon="Refresh" @click="resetQuery"
            >重置</el-button
          >
        </el-form-item>
      </el-form>
    </div>
    <el-card class="box-card">
      <!-- 操作工具栏 -->
      <template #header>
        <ButtonGroup>
          <el-button type="primary" plain icon="Plus" @click="handleAdd">新增 </el-button>
          <el-button
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
        <el-table-column label="主键ID" align="center" prop="id" />
        <el-table-column label="订单编号" align="center" prop="orderNumber" />
        <el-table-column
          label="订单状态 1待付款 2待发货 3已发货 4已完成 5已取消 6已退款"
          align="center"
          prop="status"
        />
        <el-table-column label="用户ID" align="center" prop="userId" />
        <el-table-column label="收货人姓名" align="center" prop="consignee" />
        <el-table-column label="收货地址" align="center" prop="consigneeAddress" />
        <el-table-column label="收货人电话" align="center" prop="consigneePhone" />
        <el-table-column label="邮箱" align="center" prop="email" />
        <el-table-column label="支付方式" align="center" prop="payMethod" />
        <el-table-column
          label="支付状态 0未支付 1已支付 2退款"
          align="center"
          prop="payStatus"
        />
        <el-table-column label="订单金额" align="center" prop="amount" />
        <el-table-column label="下单时间" align="center" prop="orderTime" />
        <el-table-column label="结账时间" align="center" prop="checkoutTime" />
        <el-table-column label="创建时间" align="center" prop="createTime" />
        <el-table-column label="更新时间" align="center" prop="updateTime" />
        <el-table-column
          label="操作"
          align="center"
          class-name="small-padding fixed-width"
        >
          <template #default="scope">
            <el-button type="primary" link icon="Edit" @click="handleUpdate(scope.row)"
              >修改
            </el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(scope.row)"
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
          <el-form-item label="订单编号" prop="orderNumber">
            <el-input v-model="form.orderNumber" placeholder="请输入订单编号" />
          </el-form-item>
          <el-form-item
            label="订单状态 1待付款 2待发货 3已发货 4已完成 5已取消 6已退款"
            prop="status"
          >
            <el-input
              v-model="form.status"
              placeholder="请输入订单状态 1待付款 2待发货 3已发货 4已完成 5已取消 6已退款"
            />
          </el-form-item>
          <el-form-item label="用户ID" prop="userId">
            <el-input v-model="form.userId" placeholder="请输入用户ID" />
          </el-form-item>
          <el-form-item label="收货人姓名" prop="consignee">
            <el-input v-model="form.consignee" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="收货地址" prop="consigneeAddress">
            <el-input v-model="form.consigneeAddress" placeholder="请输入收货地址" />
          </el-form-item>
          <el-form-item label="收货人电话" prop="consigneePhone">
            <el-input v-model="form.consigneePhone" placeholder="请输入收货人电话" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="支付方式" prop="payMethod">
            <el-input v-model="form.payMethod" placeholder="请输入支付方式" />
          </el-form-item>
          <el-form-item label="支付状态 0未支付 1已支付 2退款" prop="payStatus">
            <el-input
              v-model="form.payStatus"
              placeholder="请输入支付状态 0未支付 1已支付 2退款"
            />
          </el-form-item>
          <el-form-item label="订单金额" prop="amount">
            <el-input v-model="form.amount" placeholder="请输入订单金额" />
          </el-form-item>
          <el-form-item label="下单时间" prop="orderTime">
            <el-input v-model="form.orderTime" placeholder="请输入下单时间" />
          </el-form-item>
          <el-form-item label="结账时间" prop="checkoutTime">
            <el-input v-model="form.checkoutTime" placeholder="请输入结账时间" />
          </el-form-item>
          <el-form-item label="创建时间" prop="createTime">
            <el-input v-model="form.createTime" placeholder="请输入创建时间" />
          </el-form-item>
          <el-form-item label="更新时间" prop="updateTime">
            <el-input v-model="form.updateTime" placeholder="请输入更新时间" />
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
  listOrderApi,
  detailOrderApi,
  deleteOrderApi,
  addOrderApi,
  updateOrderApi,
} from "@/api/order/order";
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
  orderNumber: undefined,
  status: undefined,
  userId: undefined,
  consignee: undefined,
  consigneeAddress: undefined,
  consigneePhone: undefined,
  email: undefined,
  payMethod: undefined,
  payStatus: undefined,
  amount: undefined,
  orderTime: undefined,
  checkoutTime: undefined,
  createTime: undefined,
  updateTime: undefined,
});

// 表单参数
const form = reactive<any>({});
// 表单校验
const rules = reactive({
  id: [{ required: true, message: "主键ID不能为空", trigger: "blur" }],
});

const queryFormRef = ref();
const formRef = ref();

/** 查询列表 */
const getList = async () => {
  loading.value = true;
  try {
    const { data } = await listOrderApi(queryParams);
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
    orderNumber: undefined,
    status: undefined,
    userId: undefined,
    consignee: undefined,
    consigneeAddress: undefined,
    consigneePhone: undefined,
    email: undefined,
    payMethod: undefined,
    payStatus: undefined,
    amount: undefined,
    orderTime: undefined,
    checkoutTime: undefined,
    createTime: undefined,
    updateTime: undefined,
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
  detailOrderApi(row.id).then((response) => {
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
          await updateOrderApi(form);
          ElMessage.success("修改成功");
        } else {
          await addOrderApi(form);
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
      await deleteOrderApi(selectedIds.value);
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
      await deleteOrderApi(row.id);
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
