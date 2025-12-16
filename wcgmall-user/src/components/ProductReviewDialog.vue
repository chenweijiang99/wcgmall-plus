<template>
  <el-dialog v-model="dialogVisible" title="评价商品" width="900px" @close="handleClose">
    <div class="review-dialog">
      <!-- 商品信息 -->
      <div class="product-info">
        <el-image
          :src="productInfo.productImage"
          fit="cover"
          style="width: 80px; height: 80px; border-radius: 4px"
        />
        <div class="product-detail">
          <div class="product-name">{{ productInfo.productName }}</div>
          <div class="order-number">订单号：{{ orderNumber }}</div>
        </div>
      </div>

      <!-- 评分 -->
      <div class="score-section">
        <div class="score-item">
          <span class="score-label">商品评分：</span>
          <el-rate v-model="form.productScore" :max="5" size="large" />
          <span class="score-text">{{ getScoreText(form.productScore) }}</span>
        </div>
        <div class="score-item">
          <span class="score-label">物流评分：</span>
          <el-rate v-model="form.logisticsScore" :max="5" size="large" />
          <span class="score-text">{{ getScoreText(form.logisticsScore) }}</span>
        </div>
        <div class="score-item">
          <span class="score-label">商家评分：</span>
          <el-rate v-model="form.merchantScore" :max="5" size="large" />
          <span class="score-text">{{ getScoreText(form.merchantScore) }}</span>
        </div>
      </div>

      <!-- 图片上传区域 -->
      <div class="image-upload-section">
        <div class="upload-label">上传图片（最多9张）</div>
        <ImageBatch v-model="form.imageList" :limit="9" source="product-review" />
      </div>

      <!-- 评价内容 -->
      <MdEditor
        ref="mdRef"
        v-model="form.content"
        style="height: 250px"
        placeholder="分享你的购物体验，帮助其他买家做出更好的选择..."
        :toolbars="toolbars"
      >
        <template #defToolbars>
          <EmojiExtension :onInsert="insert" />
        </template>
      </MdEditor>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="submitReview">发表评价</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { MdEditor, ToolbarNames } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import type { ExposeParam } from 'md-editor-v3';
import { ElMessage } from 'element-plus';
import EmojiExtension from '@/components/MarkdownExtensions/EmojiExtension/index.vue';
import ImageBatch from '@/components/Upload/ImageBatch.vue';
import { addProductReviewApi, type ProductReviewDTO } from '@/api/productReview';

const props = defineProps<{
  visible: boolean;
  orderNumber: string;
  productInfo: {
    productId: number;
    productName: string;
    productImage: string;
  };
}>();

const emit = defineEmits(['update:visible', 'success']);

const dialogVisible = ref(false);
const mdRef = ref<ExposeParam>();

const form = reactive<ProductReviewDTO>({
  orderNumber: props.orderNumber,
  productId: props.productInfo.productId,
  productScore: 5,
  logisticsScore: 5,
  merchantScore: 5,
  content: '',
  imageList: []
});

// 工具栏配置：表情、撤销/重做、基础格式
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

watch(
  () => props.visible,
  (val) => {
    dialogVisible.value = val;
    if (val) {
      // 重置表单
      form.productScore = 5;
      form.logisticsScore = 5;
      form.merchantScore = 5;
      form.content = '';
      form.imageList = [];
    }
  }
);

// 评分文本
const getScoreText = (score: number) => {
  const texts = ['', '非常差', '差', '一般', '满意', '非常满意'];
  return texts[score] || '';
};

// 插入表情
const insert = (generator: any) => {
  mdRef.value?.insert(generator);
};

// 提交评价
const submitReview = async () => {
  if (!form.content.trim()) {
    ElMessage.warning('请输入评价内容');
    return;
  }

  try {
    const res = await addProductReviewApi({
      ...form,
      orderNumber: props.orderNumber,
      productId: props.productInfo.productId
    });
    if (res.code === 200) {
      ElMessage.success('评价成功');
      emit('success');
      handleClose();
    }
  } catch (error) {
    ElMessage.error('评价失败');
  }
};

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false;
  emit('update:visible', false);
};
</script>

<style scoped lang="scss">
.review-dialog {
  .product-info {
    display: flex;
    gap: 15px;
    padding: 15px;
    background: #f5f5f5;
    border-radius: 8px;
    margin-bottom: 20px;

    .product-detail {
      flex: 1;

      .product-name {
        font-size: 16px;
        font-weight: bold;
        margin-bottom: 8px;
      }

      .order-number {
        font-size: 14px;
        color: #666;
      }
    }
  }

  .score-section {
    margin-bottom: 20px;
    padding: 15px;
    background: #fafafa;
    border-radius: 8px;

    .score-item {
      display: flex;
      align-items: center;
      margin-bottom: 15px;

      &:last-child {
        margin-bottom: 0;
      }

      .score-label {
        width: 100px;
        font-weight: 500;
      }

      .score-text {
        margin-left: 15px;
        color: #f59e0b;
        font-weight: 500;
      }
    }
  }

  .image-upload-section {
    margin-bottom: 15px;
    padding: 15px;
    background: #fafafa;
    border-radius: 8px;

    .upload-label {
      margin-bottom: 10px;
      font-size: 14px;
      color: #666;
    }
  }
}
</style>
