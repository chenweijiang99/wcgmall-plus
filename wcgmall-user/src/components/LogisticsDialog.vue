<template>
  <el-dialog
    v-model="visible"
    title="物流信息"
    width="600px"
    @close="handleClose"
  >
    <div v-loading="loading" class="logistics-container">
      <div v-if="logisticsData.orderNumber" class="logistics-info">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单编号">{{
            logisticsData.orderNumber
          }}</el-descriptions-item>
          <el-descriptions-item label="运单号">{{
            logisticsData.waybillNo || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="物流状态">
            <el-tag :type="getStatusType(logisticsData.status)">
              {{ logisticsData.statusDesc }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">物流轨迹</el-divider>

        <el-timeline
          v-if="logisticsData.routes && logisticsData.routes.length > 0"
          class="logistics-timeline"
        >
          <el-timeline-item
            v-for="(route, index) in logisticsData.routes"
            :key="index"
            :timestamp="route.acceptTime"
            placement="top"
            :type="index === 0 ? 'primary' : 'default'"
          >
            <p class="route-remark">{{ route.remark }}</p>
            <p v-if="route.acceptAddress" class="route-address">
              {{ route.acceptAddress }}
            </p>
          </el-timeline-item>
        </el-timeline>

        <el-empty v-else description="暂无物流轨迹信息" />
      </div>
      <el-empty v-else description="暂无物流信息" />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { getOrderLogistics } from "@/api/logistics";
import { ElMessage } from "element-plus";

interface Props {
  modelValue: boolean;
  orderNumber: string;
}

const props = defineProps<Props>();
const emit = defineEmits(["update:modelValue"]);

const visible = ref(false);
const loading = ref(false);
const logisticsData = ref<any>({
  orderNumber: "",
  waybillNo: "",
  status: 0,
  statusDesc: "",
  routes: [],
});

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val;
    if (val && props.orderNumber) {
      loadLogistics();
    }
  }
);

watch(visible, (val) => {
  emit("update:modelValue", val);
});

const loadLogistics = async () => {
  loading.value = true;
  try {
    const { data } = await getOrderLogistics(props.orderNumber);
    logisticsData.value = data;
  } catch (error: any) {
    ElMessage.error(error.message || "查询物流信息失败");
  } finally {
    loading.value = false;
  }
};

const getStatusType = (status: number) => {
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

const handleClose = () => {
  logisticsData.value = {
    orderNumber: "",
    waybillNo: "",
    status: 0,
    statusDesc: "",
    routes: [],
  };
};
</script>

<style scoped>
.logistics-container {
  min-height: 200px;
}

.logistics-info {
  padding: 10px 0;
}

.logistics-timeline {
  margin-top: 20px;
}

.route-remark {
  margin: 0 0 5px 0;
  font-weight: 500;
  color: #303133;
}

.route-address {
  margin: 0;
  font-size: 12px;
  color: #909399;
}
</style>
