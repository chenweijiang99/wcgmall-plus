<template>
  <div class="product-review">
    <div class="review-header">
      <h3>商品评价 ({{ reviewCount }})</h3>
    </div>

    <!-- 评价表单 -->
    <div v-if="showReviewForm" class="review-form">
      <div class="score-section">
        <div class="score-item">
          <span>商品评分：</span>
          <el-rate v-model="form.productScore" :max="5" />
        </div>
        <div class="score-item">
          <span>物流评分：</span>
          <el-rate v-model="form.logisticsScore" :max="5" />
        </div>
        <div class="score-item">
          <span>商家评分：</span>
          <el-rate v-model="form.merchantScore" :max="5" />
        </div>
      </div>

      <MdEditor
        ref="mdRef"
        v-model="form.content"
        style="height: 300px"
        placeholder="分享你的购物体验..."
        :toolbars="toolbars"
      >
        <template #defToolbars>
          <EmojiExtension :onInsert="insert" />
        </template>
      </MdEditor>

      <!-- 单独的图片上传区域 -->
      <div class="image-upload-section">
        <div class="upload-label">上传图片（最多9张）</div>
        <ImageBatch v-model="form.imageList" :limit="9" source="product-review" />
      </div>

      <div class="form-actions">
        <el-button @click="showReviewForm = false">取消</el-button>
        <el-button type="primary" @click="submitReview">发表评价</el-button>
      </div>
    </div>

    <!-- 评价列表 -->
    <div class="review-list">
      <div v-for="review in reviewList" :key="review.id" class="review-item">
        <div class="review-main">
          <img :src="review.userAvatar" alt="" class="user-avatar" />
          <div class="review-content">
            <div class="review-info">
              <span class="username">{{ review.username }}</span>
              <span class="review-time">{{ formatTime(review.createTime) }}</span>
            </div>

            <!-- 评分显示 -->
            <div class="review-scores">
              <span>商品：<el-rate v-model="review.productScore" disabled :max="5" size="small" /></span>
              <span>物流：<el-rate v-model="review.logisticsScore" disabled :max="5" size="small" /></span>
              <span>商家：<el-rate v-model="review.merchantScore" disabled :max="5" size="small" /></span>
            </div>

            <!-- 评价图片（先显示图片） -->
            <div v-if="review.imageList && review.imageList.length > 0" class="review-images">
              <el-image
                v-for="(img, idx) in review.imageList"
                :key="idx"
                :src="img"
                :preview-src-list="review.imageList"
                :initial-index="idx"
                fit="cover"
                class="review-image"
              />
            </div>

            <!-- 评价内容（后显示文字） -->
            <MdPreview :modelValue="review.content" class="review-text" />

            <!-- 操作按钮 -->
            <div class="review-actions">
              <span class="action-btn" @click="handleReply(review)">回复</span>
              <span
                v-if="review.userId === currentUserId"
                class="action-btn"
                @click="deleteReview(review.id)"
              >删除</span>
            </div>

            <!-- 回复表单 -->
            <div v-if="review.showReply" class="reply-form">
              <el-input
                v-model="review.replyContent"
                type="textarea"
                :rows="3"
                placeholder="请输入回复内容"
              />
              <div class="reply-actions">
                <el-button size="small" @click="review.showReply = false">取消</el-button>
                <el-button size="small" type="primary" @click="submitReply(review)">回复</el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 子评价 -->
        <div v-if="review.children && review.children.length > 0" class="review-children">
          <div v-for="child in review.children" :key="child.id" :class="['review-item', 'child', { 'admin-reply': child.isAdmin === 1 }]">
            <img :src="child.userAvatar" alt="" class="user-avatar" />
            <div class="review-content">
              <div class="review-info">
                <span v-if="child.isAdmin === 1" class="admin-badge">商家回复</span>
                <!-- <span class="username">{{ child.isAdmin === 1 ? (child.adminName || '商家') : child.username }}</span> -->
                <span v-if="child.parentUsername && child.isAdmin !== 1" class="reply-to">
                  回复 @{{ child.parentUsername }}
                </span>
                <span class="review-time">{{ formatTime(child.createTime) }}</span>
              </div>
              <MdPreview :modelValue="child.content" class="review-text" />
              <div class="review-actions">
                <span class="action-btn" @click="handleReply(child)">回复</span>
                <span
                  v-if="child.userId === currentUserId && child.isAdmin !== 1"
                  class="action-btn"
                  @click="deleteReview(child.id)"
                >删除</span>
              </div>
              <div v-if="child.showReply" class="reply-form">
                <el-input
                  v-model="child.replyContent"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入回复内容"
                />
                <div class="reply-actions">
                  <el-button size="small" @click="child.showReply = false">取消</el-button>
                  <el-button size="small" type="primary" @click="submitReply(child)">回复</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadReviews"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { MdEditor, MdPreview, ToolbarNames } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import type { ExposeParam } from 'md-editor-v3';
import { ElMessage, ElMessageBox } from 'element-plus';
import EmojiExtension from '@/components/MarkdownExtensions/EmojiExtension/index.vue';
import ImageBatch from '@/components/Upload/ImageBatch.vue';
import {
  addProductReviewApi,
  getProductReviewTreeApi,
  getProductReviewCountApi,
  deleteProductReviewApi,
  type ProductReviewDTO,
  type ProductReviewVO
} from '@/api/productReview';
import { useUserStore } from '@/stores/modules/user';

const props = defineProps<{
  productId: number;
  orderNumber?: string;
  showForm?: boolean;
}>();

const userStore = useUserStore();
const currentUserId = ref(userStore.user?.id || 0);

const mdRef = ref<ExposeParam>();
const showReviewForm = ref(props.showForm || false);
const reviewCount = ref(0);
const reviewList = ref<ProductReviewVO[]>([]);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

const form = reactive<ProductReviewDTO>({
  orderNumber: props.orderNumber || '',
  productId: props.productId,
  productScore: 5,
  logisticsScore: 5,
  merchantScore: 5,
  content: '',
  imageList: []
});

// 工具栏配置：只保留表情、撤销/重做、基础格式
const toolbars: ToolbarNames[] = [
  'bold',
  'italic',
  'strikeThrough',
  '-',
  0, // 表情扩展
  '-',
  'revoke',
  'next',
  '=',
  'preview'
];

// 格式化时间
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

// 插入表情
const insert = (generator: any) => {
  mdRef.value?.insert(generator);
};

// 加载评价列表
const loadReviews = async () => {
  try {
    const res = await getProductReviewTreeApi({
      productId: props.productId,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    });
    if (res.code === 200) {
      reviewList.value = res.data.records;
      total.value = res.data.total;
    }

    const countRes = await getProductReviewCountApi(props.productId);
    if (countRes.code === 200) {
      reviewCount.value = countRes.data;
    }
  } catch (error) {
    ElMessage.error('加载评价失败');
  }
};

// 提交评价
const submitReview = async () => {
  if (!form.content) {
    ElMessage.warning('请输入评价内容');
    return;
  }
  if (form.productScore === 0 || form.logisticsScore === 0 || form.merchantScore === 0) {
    ElMessage.warning('请完成所有评分');
    return;
  }

  try {
    const res = await addProductReviewApi(form);
    if (res.code === 200) {
      ElMessage.success('评价成功');
      showReviewForm.value = false;
      form.content = '';
      form.imageList = [];
      form.productScore = 5;
      form.logisticsScore = 5;
      form.merchantScore = 5;
      loadReviews();
    }
  } catch (error) {
    ElMessage.error('评价失败');
  }
};

// 处理回复
const handleReply = (review: any) => {
  // 关闭其他回复框
  reviewList.value.forEach((r) => {
    r.showReply = false;
    r.children?.forEach((c: any) => (c.showReply = false));
  });
  review.showReply = true;
};

// 提交回复
const submitReply = async (review: any) => {
  if (!review.replyContent) {
    ElMessage.warning('请输入回复内容');
    return;
  }

  try {
    const replyData: ProductReviewDTO = {
      orderNumber: props.orderNumber || '',
      productId: props.productId,
      productScore: 5,
      logisticsScore: 5,
      merchantScore: 5,
      content: review.replyContent,
      imageList: [],
      parentReviewId: review.id
    };

    const res = await addProductReviewApi(replyData);
    if (res.code === 200) {
      ElMessage.success('回复成功');
      review.showReply = false;
      review.replyContent = '';
      loadReviews();
    }
  } catch (error) {
    ElMessage.error('回复失败');
  }
};

// 删除评价
const deleteReview = (id: number) => {
  ElMessageBox.confirm('确定删除这条评价吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteProductReviewApi(id);
      if (res.code === 200) {
        ElMessage.success('删除成功');
        loadReviews();
      }
    } catch (error) {
      ElMessage.error('删除失败');
    }
  });
};

onMounted(() => {
  loadReviews();
});
</script>

<style scoped lang="scss">
.product-review {
  padding: 20px;

  .review-header {
    margin-bottom: 20px;
    h3 {
      font-size: 24px;
      font-weight: bold;
    }
  }

  .review-form {
    margin-bottom: 30px;
    padding: 20px;
    background: #f5f5f5;
    border-radius: 8px;

    .score-section {
      display: flex;
      gap: 30px;
      margin-bottom: 20px;

      .score-item {
        display: flex;
        align-items: center;
        gap: 10px;
      }
    }

    .image-upload-section {
      margin-top: 15px;
      padding: 15px;
      background: #fff;
      border-radius: 8px;

      .upload-label {
        margin-bottom: 10px;
        font-size: 14px;
        color: #666;
      }
    }

    .form-actions {
      margin-top: 15px;
      text-align: right;
    }
  }

  .review-list {
    .review-item {
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 1px solid #e0e0e0;

      &.child {
        margin-left: 70px;
        padding: 15px 0;
        border-bottom: none;
      }

      .review-main {
        display: flex;
        gap: 15px;
      }

      .user-avatar {
        width: 50px;
        height: 50px;
        border-radius: 50%;
        flex-shrink: 0;
      }

      .review-content {
        flex: 1;

        .review-info {
          margin-bottom: 10px;
          display: flex;
          align-items: center;
          gap: 10px;

          .username {
            font-weight: bold;
          }

          .reply-to {
            color: #666;
          }

          .review-time {
            color: #999;
            font-size: 13px;
          }
        }

        .review-scores {
          display: flex;
          gap: 20px;
          margin-bottom: 10px;
          font-size: 14px;

          span {
            display: flex;
            align-items: center;
            gap: 5px;
          }
        }

        .review-text {
          margin-bottom: 10px;
          text-align: justify;

          // 覆盖 MdPreview 的默认背景 - 使用 CSS 变量
          --md-bk-color: transparent !important;

          :deep(.md-editor) {
            --md-bk-color: transparent !important;
            background-color: transparent !important;
            background: transparent !important;
          }
          :deep(.md-editor-preview-wrapper) {
            background-color: transparent !important;
            background: transparent !important;
          }
          :deep(.md-editor-preview) {
            background-color: transparent !important;
            background: transparent !important;
          }
          :deep(.md-editor-previewOnly) {
            background-color: transparent !important;
            background: transparent !important;
          }
          :deep(article) {
            background-color: transparent !important;
            background: transparent !important;
          }
        }

        .review-images {
          display: flex;
          gap: 10px;
          margin-bottom: 10px;

          .review-image {
            width: 100px;
            height: 100px;
            border-radius: 4px;
            cursor: pointer;
          }
        }

        .review-actions {
          display: flex;
          gap: 20px;
          font-size: 13px;
          color: #666;

          .action-btn {
            cursor: pointer;
            &:hover {
              color: #409eff;
            }
          }
        }

        .reply-form {
          margin-top: 10px;
          padding: 10px;
          background: #f9f9f9;
          border-radius: 4px;

          .reply-actions {
            margin-top: 10px;
            text-align: right;
          }
        }
      }

      .review-children {
        margin-top: 15px;
      }

      // 管理员回复样式
      &.admin-reply {
        background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
        border-left: 3px solid #f56c6c;
        padding-left: 12px;
        margin-left: 55px;
      }
    }
  }

  // 管理员标识
  .admin-badge {
    display: inline-block;
    padding: 2px 8px;
    background: linear-gradient(135deg, #f56c6c 0%, #e6a23c 100%);
    color: #fff;
    font-size: 12px;
    border-radius: 4px;
    margin-right: 8px;
  }

  .el-pagination {
    margin-top: 20px;
    justify-content: center;
  }
}

// 暗黑模式适配 - 需要在 scoped 外部定义
:global(.dark) {
  .product-review {
    .review-list {
      .review-item {
        border-bottom-color: #3f3f46;

        &.admin-reply {
          background: linear-gradient(135deg, rgba(245, 108, 108, 0.15) 0%, rgba(245, 108, 108, 0.05) 100%);
          border-left-color: #f56c6c;
        }
      }
    }

    .review-form {
      background: #18181b;

      .image-upload-section {
        background: #27272a;
      }
    }

    .reply-form {
      background: #27272a !important;
    }
  }
}
</style>
