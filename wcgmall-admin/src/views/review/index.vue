<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form :model="queryParams" ref="queryFormRef" inline>
        <el-form-item label="商品ID" prop="productId">
          <el-input
            v-model="queryParams.productId"
            placeholder="请输入商品ID"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="评分类型" prop="scoreType">
          <el-select v-model="queryParams.scoreType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="全部" :value="0" />
            <el-option label="好评" :value="1" />
            <el-option label="中评" :value="2" />
            <el-option label="差评" :value="3" />
            <el-option label="有图" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="具体评分" prop="productScore">
          <el-select v-model="queryParams.productScore" placeholder="请选择" clearable style="width: 120px">
            <el-option label="1星" :value="1" />
            <el-option label="2星" :value="2" />
            <el-option label="3星" :value="3" />
            <el-option label="4星" :value="4" />
            <el-option label="5星" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="title">评价列表</span>
          <el-button
            type="danger"
            icon="Delete"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
          >
            批量删除 {{ selectedIds.length > 0 ? `(${selectedIds.length})` : '' }}
          </el-button>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="dataList"
        @expand-change="handleExpandChange"
        :row-key="getRowKey"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="expand" width="60">
          <template #default="{ row }">
            <div class="review-detail-container">
              <!-- 评价图片 -->
              <div v-if="row.imageList && row.imageList.length > 0" class="review-images">
                <h4>评价图片</h4>
                <div class="image-list">
                  <el-image
                    v-for="(img, idx) in row.imageList"
                    :key="idx"
                    :src="img"
                    :preview-src-list="row.imageList"
                    :initial-index="idx"
                    fit="cover"
                    class="review-image"
                  />
                </div>
              </div>

              <!-- 评价内容 -->
              <div class="review-content-section">
                <h4>评价内容</h4>
                <div class="review-text" v-html="row.content"></div>
              </div>

              <!-- 回复列表 -->
              <div v-if="row.children && row.children.length > 0" class="reply-list">
                <h4>回复记录 ({{ row.children.length }})</h4>
                <div v-for="reply in row.children" :key="reply.id" class="reply-item">
                  <div class="reply-header">
                    <el-tag v-if="reply.isAdmin === 1" type="danger" size="small">管理员</el-tag>
                    <el-tag v-else type="info" size="small">用户</el-tag>
                    <span class="reply-user">{{ reply.isAdmin === 1 ? (reply.adminName || '管理员') : reply.username }}</span>
                    <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
                    <el-button
                      type="danger"
                      link
                      size="small"
                      @click="handleDeleteReply(reply)"
                    >删除</el-button>
                  </div>
                  <div class="reply-content">{{ reply.content }}</div>
                </div>
              </div>

              <!-- 回复表单 -->
              <div class="reply-form">
                <h4>回复评价</h4>
                <el-input
                  v-model="replyContent[row.id]"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入回复内容..."
                />
                <el-button
                  type="primary"
                  size="small"
                  style="margin-top: 10px"
                  @click="handleReply(row)"
                  :loading="replyLoading[row.id]"
                >
                  发送回复
                </el-button>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="商品信息" align="left" min-width="250">
          <template #default="{ row }">
            <div class="product-info">
              <el-image
                v-if="row.productImage"
                :src="row.productImage"
                fit="cover"
                style="width: 60px; height: 60px; border-radius: 4px"
              />
              <div class="product-detail">
                <div class="product-name">{{ row.productName }}</div>
                <div class="product-id">ID: {{ row.productId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="用户" align="center" width="150">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :src="row.userAvatar" :size="32" />
              <span class="username">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="评分" align="center" width="200">
          <template #default="{ row }">
            <div class="score-info">
              <div class="score-item">
                <span>商品:</span>
                <el-rate v-model="row.productScore" disabled size="small" />
              </div>
              <div class="score-item">
                <span>物流:</span>
                <el-rate v-model="row.logisticsScore" disabled size="small" />
              </div>
              <div class="score-item">
                <span>商家:</span>
                <el-rate v-model="row.merchantScore" disabled size="small" />
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="回复数" align="center" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.children && row.children.length > 0" type="success">
              {{ row.children.length }}
            </el-tag>
            <el-tag v-else type="info">0</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="评价时间" align="center" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" fixed="right" width="100">
          <template #default="{ row }">
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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
import { ElMessage, ElMessageBox } from "element-plus";
import { listReviewApi, replyReviewApi, deleteReviewApi, deleteBatchReviewApi } from "@/api/review/review";

const loading = ref(false);
const total = ref(0);
const dataList = ref<any[]>([]);

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  productId: undefined,
  productScore: undefined,
  scoreType: undefined,
});

const queryFormRef = ref();

// 回复相关
const replyContent = ref<Record<number, string>>({});
const replyLoading = ref<Record<number, boolean>>({});

// 批量选择
const selectedIds = ref<number[]>([]);

const getRowKey = (row: any) => row.id;

/** 格式化时间 */
const formatTime = (time: any) => {
  if (!time) return '';
  // 处理数组格式的时间 [2025, 12, 16, 14, 19, 21]
  if (Array.isArray(time)) {
    const [year, month, day, hour, minute, second] = time;
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second || 0).padStart(2, '0')}`;
  }
  // 处理字符串格式
  if (typeof time === 'string') {
    return time.replace('T', ' ').substring(0, 19);
  }
  return time;
};

/** 查询列表 */
const getList = async () => {
  loading.value = true;
  try {
    const { data } = await listReviewApi(queryParams);
    dataList.value = data.records;
    total.value = data.total;
  } catch (error) {
    console.error("获取评价列表失败:", error);
  }
  loading.value = false;
};

/** 搜索 */
const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

/** 重置 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

/** 展开行 */
const handleExpandChange = (row: any, expandedRows: any[]) => {
  // 可以在这里加载更多详情
};

/** 回复评价 */
const handleReply = async (row: any) => {
  const content = replyContent.value[row.id];
  if (!content || !content.trim()) {
    ElMessage.warning("请输入回复内容");
    return;
  }

  replyLoading.value[row.id] = true;
  try {
    await replyReviewApi({
      parentReviewId: row.id,
      content: content.trim(),
    });
    ElMessage.success("回复成功");
    replyContent.value[row.id] = "";
    // 刷新列表
    getList();
  } catch (error) {
    ElMessage.error("回复失败");
  } finally {
    replyLoading.value[row.id] = false;
  }
};

/** 删除评价 */
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    "删除评价将同时删除所有回复，是否继续？",
    "警告",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  ).then(async () => {
    try {
      await deleteReviewApi(row.id);
      ElMessage.success("删除成功");
      getList();
    } catch (error) {
      ElMessage.error("删除失败");
    }
  });
};

/** 删除回复 */
const handleDeleteReply = (reply: any) => {
  const isAdmin = reply.isAdmin === 1;
  ElMessageBox.confirm(
    isAdmin ? "确定删除此管理员回复？" : "确定删除此用户回复？",
    "警告",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  ).then(async () => {
    try {
      await deleteReviewApi(reply.id);
      ElMessage.success("删除成功");
      getList();
    } catch (error) {
      ElMessage.error("删除失败");
    }
  });
};

/** 选择变化 */
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id);
};

/** 批量删除 */
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning("请选择要删除的评价");
    return;
  }
  ElMessageBox.confirm(
    `确定删除选中的 ${selectedIds.value.length} 条评价及其所有回复？`,
    "警告",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  ).then(async () => {
    try {
      await deleteBatchReviewApi(selectedIds.value);
      ElMessage.success("批量删除成功");
      selectedIds.value = [];
      getList();
    } catch (error) {
      ElMessage.error("批量删除失败");
    }
  });
};

/** 分页 */
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val;
  getList();
};

const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val;
  getList();
};

onMounted(() => {
  getList();
});
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-size: 16px;
  font-weight: 600;
}

/* 商品信息 */
.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-detail {
  flex: 1;
}

.product-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.product-id {
  font-size: 12px;
  color: #909399;
}

/* 用户信息 */
.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.username {
  font-size: 12px;
  color: #606266;
}

/* 评分信息 */
.score-info {
  text-align: left;
}

.score-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  margin-bottom: 2px;
}

.score-item span {
  width: 36px;
  color: #909399;
}

/* 评价详情 */
.review-detail-container {
  padding: 20px;
  background-color: #f8f9fa;
}

.review-detail-container h4 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 14px;
  font-weight: 600;
}

/* 评价图片 */
.review-images {
  margin-bottom: 20px;
}

.image-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.review-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  cursor: pointer;
}

/* 评价内容 */
.review-content-section {
  margin-bottom: 20px;
}

.review-text {
  padding: 12px;
  background: #fff;
  border-radius: 4px;
  line-height: 1.6;
}

/* 回复列表 */
.reply-list {
  margin-bottom: 20px;
}

.reply-item {
  padding: 12px;
  background: #fff;
  border-radius: 4px;
  margin-bottom: 10px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.reply-user {
  font-weight: 500;
}

.reply-time {
  font-size: 12px;
  color: #909399;
}

.reply-content {
  color: #606266;
  line-height: 1.5;
}

/* 回复表单 */
.reply-form {
  padding: 16px;
  background: #fff;
  border-radius: 4px;
}

/* 分页 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
