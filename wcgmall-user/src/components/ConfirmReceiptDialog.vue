<template>
  <el-dialog
    v-model="dialogVisible"
    title="确认收货"
    width="700px"
    @close="handleClose"
  >
    <div class="confirm-dialog">
      <el-alert
        title="请勾选您已收到的商品"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      />

      <el-table
        ref="tableRef"
        :data="orderItems"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" :selectable="checkSelectable" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image
              :src="row.productImage"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="number" label="数量" width="80" />
        <el-table-column label="确认状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.confirmStatus === 1" type="success">已确认</el-tag>
            <el-tag v-else type="info">未确认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="确认时间" width="180">
          <template #default="{ row }">
            {{ row.confirmTime || '-' }}
          </template>
        </el-table-column>
      </el-table>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="confirmSelected" :disabled="selectedItems.length === 0">
        确认收货 ({{ selectedItems.length }})
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { getOrderDetailWithStatusApi, confirmReceiptItemsApi } from '@/api/order';

const props = defineProps<{
  visible: boolean;
  orderNumber: string;
}>();

const emit = defineEmits(['update:visible', 'success']);

const dialogVisible = ref(false);
const orderItems = ref<any[]>([]);
const selectedItems = ref<any[]>([]);
const tableRef = ref();

watch(
  () => props.visible,
  (val) => {
    dialogVisible.value = val;
    if (val) {
      loadOrderItems();
    }
  }
);

// 加载订单商品
const loadOrderItems = async () => {
  try {
    const res = await getOrderDetailWithStatusApi(props.orderNumber);
    if (res.code === 200) {
      orderItems.value = res.data || [];
      // 默认选中未确认的商品
      const unconfirmed = orderItems.value.filter(item => !item.confirmStatus || item.confirmStatus === 0);
      selectedItems.value = unconfirmed;
      // 更新表格选中状态
      setTimeout(() => {
        unconfirmed.forEach(row => {
          tableRef.value?.toggleRowSelection(row, true);
        });
      }, 100);
    }
  } catch (error) {
    ElMessage.error('加载订单商品失败');
  }
};

// 判断行是否可选（已确认的商品不可选）
const checkSelectable = (row: any) => {
  return !row.confirmStatus || row.confirmStatus === 0;
};

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedItems.value = selection;
};

// 确认选中的商品
const confirmSelected = async () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要确认的商品');
    return;
  }

  try {
    const itemIds = selectedItems.value.map(item => item.id);
    const res = await confirmReceiptItemsApi(props.orderNumber, itemIds);
    if (res.code === 200) {
      ElMessage.success('确认收货成功');
      emit('success');
      handleClose();
    }
  } catch (error) {
    ElMessage.error('确认收货失败');
  }
};

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false;
  emit('update:visible', false);
  selectedItems.value = [];
};
</script>

<style scoped lang="scss">
.confirm-dialog {
  :deep(.el-table) {
    .el-table__row {
      &.is-selected {
        background-color: #f0f9ff;
      }
    }
  }
}
</style>
