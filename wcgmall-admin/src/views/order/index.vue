<template>
  <div class="app-container">
    <!-- 订单状态统计卡片 -->
    <div class="status-cards">
      <el-card
        v-for="item in orderStatusTabs"
        :key="item.value ?? 'all'"
        :class="['status-card', { active: currentStatusFilter === item.value }]"
        shadow="hover"
        @click="handleStatusFilter(item.value)"
      >
        <div class="status-card-content">
          <div class="status-icon" :style="{ backgroundColor: item.color + '20', color: item.color }">
            <el-icon :size="24"><component :is="item.icon" /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">{{ item.value === null ? totalStatusCount : (statusCountMap[item.value] || 0) }}</div>
            <div class="status-label">{{ item.label }}</div>
          </div>
        </div>
      </el-card>
    </div>

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
        <el-form-item label="收货人" prop="consignee">
          <el-input
            v-model="queryParams.consignee"
            placeholder="请输入收货人姓名"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="支付方式" prop="payMethod">
          <el-select v-model="queryParams.payMethod" placeholder="请选择" clearable style="width: 120px">
            <el-option label="支付宝" value="支付宝" />
            <el-option label="微信" value="微信" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card class="box-card">
      <!-- 操作工具栏 -->
      <template #header>
        <div class="card-header">
          <span class="title">订单列表</span>
          <ButtonGroup>
            <el-button
              type="danger"
              plain
              icon="Delete"
              :disabled="selectedIds.length === 0"
              @click="handleBatchDelete"
            >批量删除</el-button>
          </ButtonGroup>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="dataList"
        @selection-change="handleSelectionChange"
        :row-key="getRowKey"
        @expand-change="handleExpandChange"
        stripe
      >
        <el-table-column type="expand" width="60">
          <template #default="{ row }">
            <div v-loading="expandedRows[row.id]?.loading" class="order-detail-container">
              <div v-if="expandedRows[row.id]?.details && expandedRows[row.id].details.length > 0">
                <h4 class="detail-title">订单商品详情</h4>
                <el-table :data="expandedRows[row.id].details" border size="small" class="detail-table">
                  <el-table-column label="商品图片" align="center" width="100">
                    <template #default="{ row: detail }">
                      <el-image
                        v-if="detail.productImage"
                        :src="detail.productImage"
                        :preview-src-list="[detail.productImage]"
                        fit="cover"
                        style="width: 60px; height: 60px"
                      >
                        <template #error>
                          <div class="image-slot">
                            <el-icon><Picture /></el-icon>
                          </div>
                        </template>
                      </el-image>
                      <span v-else>无图片</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="商品名称" prop="productName" align="center" />
                  <el-table-column label="商品价格" prop="productPrice" align="center">
                    <template #default="{ row: detail }">
                      ¥{{ detail.productPrice }}
                    </template>
                  </el-table-column>
                  <el-table-column label="购买数量" prop="number" align="center" />
                  <el-table-column label="小计" align="center">
                    <template #default="{ row: detail }">
                      ¥{{ (detail.productPrice * detail.number).toFixed(2) }}
                    </template>
                  </el-table-column>
                </el-table>
                <div class="detail-summary">
                  <span>商品总数：{{ getTotalQuantity(expandedRows[row.id].details) }} 件</span>
                  <span>总金额：¥{{ getTotalAmount(expandedRows[row.id].details).toFixed(2) }}</span>
                </div>
              </div>
              <div v-else-if="expandedRows[row.id]?.error" class="detail-error">
                <el-alert
                  :title="expandedRows[row.id].error"
                  type="error"
                  :closable="false"
                />
              </div>
              <div v-else class="detail-empty">
                <el-empty description="暂无订单详情数据" />
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="订单编号" align="center" prop="orderNumber" min-width="180">
          <template #default="scope">
            <span class="order-number">{{ scope.row.orderNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" align="center" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" effect="plain">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="收货信息" align="left" min-width="200">
          <template #default="scope">
            <div class="receiver-info">
              <div><strong>{{ scope.row.consignee }}</strong> {{ scope.row.consigneePhone }}</div>
              <div class="address">{{ scope.row.consigneeAddress }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" align="center" prop="amount" width="100">
          <template #default="scope">
            <span class="amount">¥{{ scope.row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="支付信息" align="center" width="120">
          <template #default="scope">
            <div v-if="scope.row.payStatus === 1">
              <el-tag type="success" size="small">{{ scope.row.payMethod || '已支付' }}</el-tag>
            </div>
            <div v-else>
              <el-tag type="info" size="small">未支付</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" align="center" prop="createTime" width="170" />
        <el-table-column
          label="操作"
          align="center"
          fixed="right"
          width="200"
        >
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 1 || scope.row.status === 2"
              type="success"
              link
              icon="Van"
              @click="handleShip(scope.row)"
              >发货
            </el-button>
            <el-button
              v-if="scope.row.status === 3 || scope.row.status === 4"
              type="info"
              link
              icon="Position"
              @click="handleViewLogistics(scope.row)"
              >物流
            </el-button>
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
            label="订单状态"
            prop="status"
          >
            <el-select v-model="form.status" placeholder="请选择订单状态"> 
            <el-option
              v-for="item in orderStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
            </el-select>
          </el-form-item>
          <el-form-item label="用户ID" prop="userId">
            <el-input v-model="form.userId" placeholder="请输入用户ID" />
          </el-form-item>
          <el-form-item label="姓名" prop="consignee">
            <el-input v-model="form.consignee" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="收货地址" prop="consigneeAddress">
            <el-input v-model="form.consigneeAddress" placeholder="请输入收货地址" />
          </el-form-item>
          <el-form-item label="电话" prop="consigneePhone">
            <el-input v-model="form.consigneePhone" placeholder="请输入收货人电话" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="支付方式" prop="payMethod">
            <el-input v-model="form.payMethod" placeholder="请输入支付方式" />
          </el-form-item>
          <el-form-item label="支付状态" prop="payStatus">
            <el-select v-model="form.payStatus" placeholder="请选择支付状态"> 
            <el-option
              v-for="item in payStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
            </el-select>
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

      <!-- 发货对话框 -->
      <el-dialog v-model="shipDialogOpen" title="发货" width="600px" append-to-body>
        <el-form
          ref="shipFormRef"
          :model="shipForm"
          :rules="shipRules"
          label-width="100px"
        >
          <el-form-item label="订单编号">
            <el-input v-model="shipForm.orderNumber" disabled />
          </el-form-item>
          <el-form-item label="发件人姓名" prop="senderName">
            <el-input v-model="shipForm.senderName" placeholder="请输入发件人姓名" />
          </el-form-item>
          <el-form-item label="发件人电话" prop="senderPhone">
            <el-input v-model="shipForm.senderPhone" placeholder="请输入发件人电话" />
          </el-form-item>
          <el-form-item label="发件省" prop="senderProvince">
            <el-input v-model="shipForm.senderProvince" placeholder="例如：广东省" />
          </el-form-item>
          <el-form-item label="发件市" prop="senderCity">
            <el-input v-model="shipForm.senderCity" placeholder="例如：深圳市" />
          </el-form-item>
          <el-form-item label="发件区/县" prop="senderCounty">
            <el-input v-model="shipForm.senderCounty" placeholder="例如：南山区" />
          </el-form-item>
          <el-form-item label="详细地址" prop="senderAddress">
            <el-input
              v-model="shipForm.senderAddress"
              type="textarea"
              :rows="2"
              placeholder="请输入详细地址"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button type="primary" @click="submitShipForm" :loading="shipLoading"
              >确认发货</el-button
            >
            <el-button @click="shipDialogOpen = false">取 消</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 物流查看对话框 -->
      <el-dialog
        v-model="logisticsDialogOpen"
        title="物流信息"
        width="700px"
        append-to-body
      >
        <div v-loading="logisticsLoading">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单编号">{{ logisticsInfo.orderNumber }}</el-descriptions-item>
            <el-descriptions-item label="运单号">{{ logisticsInfo.waybillNo || "-" }}</el-descriptions-item>
            <el-descriptions-item label="物流状态" :span="2">
              <el-tag :type="getLogisticsStatusType(logisticsInfo.status)">
                {{ logisticsInfo.statusDesc }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">物流轨迹</el-divider>

          <el-timeline v-if="logisticsInfo.routes && logisticsInfo.routes.length > 0">
            <el-timeline-item
              v-for="(route, index) in logisticsInfo.routes"
              :key="index"
              :timestamp="route.acceptTime"
              placement="top"
            >
              <el-card>
                <p>{{ route.remark }}</p>
                <p v-if="route.acceptAddress" class="text-gray">
                  {{ route.acceptAddress }}
                </p>
              </el-card>
            </el-timeline-item>
          </el-timeline>

          <el-empty v-else description="暂无物流轨迹信息" />
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { nextTick } from 'vue'
import { ElMessage, ElMessageBox } from "element-plus";
import {
  listOrderApi,
  detailOrderApi,
  deleteOrderApi,
  addOrderApi,
  updateOrderApi,
  getOrderDetailApi,
  getOrderStatusCountApi
} from "@/api/order/order";
import { shipOrderApi, getLogisticsApi } from "@/api/order/logistics";
import ButtonGroup from "@/components/ButtonGroup/index.vue";
import { Picture, List, Clock, CreditCard, Box, Van, CircleCheck, CircleClose, RefreshRight } from '@element-plus/icons-vue'

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

// 当前状态筛选
const currentStatusFilter = ref<number | null>(null);
// 状态数量统计
const statusCountMap = ref<Record<number, number>>({});
// 总数量
const totalStatusCount = computed(() => {
  return Object.values(statusCountMap.value).reduce((acc, val) => acc + val, 0);
});

// 状态标签配置
const orderStatusTabs = [
  { value: null, label: '全部', icon: 'List', color: '#409EFF' },
  { value: 0, label: '待付款', icon: 'Clock', color: '#E6A23C' },
  { value: 2, label: '待发货', icon: 'Box', color: '#409EFF' },
  { value: 3, label: '已发货', icon: 'Van', color: '#67C23A' },
  { value: 4, label: '已完成', icon: 'CircleCheck', color: '#67C23A' },
  { value: 5, label: '已取消', icon: 'CircleClose', color: '#909399' },
  { value: 6, label: '已退款', icon: 'RefreshRight', color: '#F56C6C' },
];

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNumber: undefined,
  status: undefined,
  consignee: undefined,
  payMethod: undefined,
});

// 表单参数
const form = reactive<any>({});
// 表单校验
const rules = reactive({
  id: [{ required: true, message: "主键ID不能为空", trigger: "blur" }],
});

const queryFormRef = ref();
const formRef = ref();
const orderStatusOptions = ref([
  { value: 0, label: "待付款" },
  { value: 1, label: "已付款" },
  { value: 2, label: "待发货" },
  { value: 3, label: "已发货" },
  { value: 4, label: "已完成" },
  { value: 5, label: "已取消" },
  { value: 6, label: "已退款" },
]);
const payStatusOptions = ref([
  { value: 0, label: "未支付" },
  { value: 1, label: "已支付" },
  { value: 2, label: "退款" },
]);

// 发货相关
const shipDialogOpen = ref(false);
const shipLoading = ref(false);
const shipFormRef = ref();
const shipForm = reactive<any>({
  orderNumber: undefined,
  senderName: undefined,
  senderPhone: undefined,
  senderProvince: undefined,
  senderCity: undefined,
  senderCounty: undefined,
  senderAddress: undefined,
});
const shipRules = reactive({
  senderName: [{ required: true, message: "发件人姓名不能为空", trigger: "blur" }],
  senderPhone: [
    { required: true, message: "发件人电话不能为空", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" },
  ],
  senderProvince: [{ required: true, message: "发件省不能为空", trigger: "blur" }],
  senderCity: [{ required: true, message: "发件市不能为空", trigger: "blur" }],
  senderCounty: [{ required: true, message: "发件区/县不能为空", trigger: "blur" }],
  senderAddress: [{ required: true, message: "详细地址不能为空", trigger: "blur" }],
});

// 物流相关
const logisticsDialogOpen = ref(false);
const logisticsLoading = ref(false);
const logisticsInfo = reactive<any>({
  orderNumber: "",
  waybillNo: "",
  status: 0,
  statusDesc: "",
  routes: [],
});

// 订单详情相关 - 新增
const expandedRows = ref<Record<string, any>>({});

/** 获取行唯一标识 */
const getRowKey = (row: any) => {
  return row.id;
};

/** 处理表格展开事件 */
const handleExpandChange = async (row: any, expandedRowsArr: any[]) => {
  const isExpanded = expandedRowsArr.some(item => item.id === row.id);
  
  if (isExpanded) {
    // 展开时加载订单详情
    await loadOrderDetail(row);
  } else {
    // 收起时清理数据
    if (expandedRows.value[row.id]) {
      delete expandedRows.value[row.id];
    }
  }
};

/** 加载订单详情 */
const loadOrderDetail = async (row: any) => {
  // 如果已经加载过，直接返回
  if (expandedRows.value[row.id] && expandedRows.value[row.id].details) {
    return;
  }
  
  // 初始化订单详情状态
  expandedRows.value[row.id] = {
    loading: true,
    details: [],
    error: null
  };
  
  try {
    const response = await getOrderDetailApi(row.orderNumber);
    // 确保数据不为null，避免Element Plus表格组件内部错误
    expandedRows.value[row.id].details = response?.data || [];
    expandedRows.value[row.id].loading = false;
  } catch (error: any) {
    expandedRows.value[row.id].error = error.message || "加载订单详情失败";
    expandedRows.value[row.id].loading = false;
    expandedRows.value[row.id].details = []; // 确保details不为null
    ElMessage.error("加载订单详情失败");
  }
};

/** 计算商品总数 */
const getTotalQuantity = (details: any[]) => {
  return details.reduce((total, item) => total + (item.number || 0), 0);
};

/** 计算总金额 */
const getTotalAmount = (details: any[]) => {
  return details.reduce((total, item) => {
    const price = item.productPrice || 0;
    const quantity = item.number || 0;
    return total + (price * quantity);
  }, 0);
};

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

/** 获取订单状态数量 */
const getStatusCount = async () => {
  try {
    const { data } = await getOrderStatusCountApi();
    statusCountMap.value = data;
  } catch (error) {
    console.error('获取订单状态数量失败:', error);
  }
};

/** 状态筛选 */
const handleStatusFilter = (status: number | null) => {
  currentStatusFilter.value = status;
  queryParams.status = status as any;
  queryParams.pageNum = 1;
  getList();
};

/** 获取状态标签类型 */
const getStatusTagType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'primary',
    3: '',
    4: 'success',
    5: 'info',
    6: 'danger',
  };
  return typeMap[status] || 'info';
};

/** 获取状态标签文字 */
const getStatusLabel = (status: number) => {
  const labelMap: Record<number, string> = {
    0: '待付款',
    1: '已付款',
    2: '待发货',
    3: '已发货',
    4: '已完成',
    5: '已取消',
    6: '已退款',
  };
  return labelMap[status] || '未知';
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
  currentStatusFilter.value = null;
  queryParams.status = undefined;
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
const handleUpdate = async (row: any) => {
  reset();
  try {
    const response = await detailOrderApi(row.id);
    Object.assign(form, response.data);
    // 确保表单引用已经正确绑定
    await nextTick();
    open.value = true;
    title.value = "修改";
  } catch (error) {
    ElMessage.error("获取订单详情失败");
  }
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
        getStatusCount();
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
      getStatusCount();
    } catch (error) {
      ElMessage.error("删除失败");
      console.error("删除失败:", error);
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row: any) => {
  ElMessageBox.confirm('是否确认删除编号为"' + row.orderNumber + '"的订单?', "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    try {
      await deleteOrderApi(row.id);
      ElMessage.success("删除成功");
      getList();
      getStatusCount();
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

/** 发货操作 */
const handleShip = (row: any) => {
  Object.assign(shipForm, {
    orderNumber: row.orderNumber,
    senderName: "韋",
    senderPhone: "13290010299",
    senderProvince: "重庆市",
    senderCity: "重庆市",
    senderCounty: "重庆市",
    senderAddress: "两江新区蔡家岗街道柚米社区",
  });
  shipFormRef.value?.resetFields();
  shipDialogOpen.value = true;
};

/** 提交发货表单 */
const submitShipForm = async () => {
  await shipFormRef.value?.validate(async (valid: any) => {
    if (valid) {
      shipLoading.value = true;
      try {
        await shipOrderApi(shipForm);
        ElMessage.success("发货成功");
        shipDialogOpen.value = false;
        getList();
        getStatusCount();
      } catch (error: any) {
        ElMessage.error(error.message || "发货失败");
      } finally {
        shipLoading.value = false;
      }
    }
  });
};

/** 查看物流 */
const handleViewLogistics = async (row: any) => {
  logisticsDialogOpen.value = true;
  logisticsLoading.value = true;
  try {
    const { data } = await getLogisticsApi(row.orderNumber);
    Object.assign(logisticsInfo, data);
  } catch (error: any) {
    ElMessage.error(error.message || "查询物流信息失败");
  } finally {
    logisticsLoading.value = false;
  }
};

/** 获取物流状态类型 */
const getLogisticsStatusType = (status: number) => {
  switch (status) {
    case 0:
      return "info";
    case 1:
      return "warning";
    case 2:
      return "primary";
    case 3:
      return "success";
    default:
      return "info";
  }
};

onMounted(() => {
  getList();
  getStatusCount();
});
</script>

<style scoped>
/* 状态卡片 */
.status-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.status-card {
  flex: 1;
  min-width: 120px;
  cursor: pointer;
  transition: all 0.3s;
}

.status-card:hover {
  transform: translateY(-2px);
}

.status-card.active {
  border-color: var(--el-color-primary);
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.status-card :deep(.el-card__body) {
  padding: 16px;
}

.status-card-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-info {
  flex: 1;
}

.status-count {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.status-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

/* 卡片头 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-size: 16px;
  font-weight: 600;
}

/* 表格样式 */
.order-number {
  font-family: monospace;
  color: #409EFF;
}

.receiver-info {
  text-align: left;
}

.receiver-info .address {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.amount {
  font-weight: 600;
  color: #F56C6C;
}

/* 订单详情 */
.order-detail-container {
  padding: 16px;
  background-color: #f8f9fa;
}

.detail-title {
  margin-bottom: 16px;
  color: #333;
  font-weight: 600;
}

.detail-table {
  margin-bottom: 16px;
}

.detail-summary {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-top: 1px solid #e8e8e8;
  font-weight: 600;
  color: #333;
}

.detail-error {
  padding: 16px;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 60px;
  height: 60px;
  background: #f5f7fa;
  color: #909399;
}
</style>