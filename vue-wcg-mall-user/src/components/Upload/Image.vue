<template>
  <div class="upload-container">
    <el-upload
      v-model:file-list="fileList"
      :action="uploadUrl"
      list-type="picture-card"
      :headers="headers"
      :multiple="multiple"
      :limit="limit"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      :on-success="handleSuccess"
      :on-exceed="handleExceed"
      :before-upload="beforeUpload"
      :data="uploadData"
    >
      <el-icon><Plus /></el-icon>
      <template #tip>
        <div class="upload-tip">只能上传jpg/png/gif文件，且不超过{{ fileSize }}MB</div>
      </template>
    </el-upload>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="dialogVisible" top="5vh" title="预览图片">
      <img
        :src="dialogImageUrl"
        alt="Preview Image"
        style="width: 100%; height: 500px; object-fit: contain"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessage } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { useTokenStore } from "@/stores/token.js";
import { uploadApi, deleteFileApi } from "@/api/file.js";
import { ref, computed, watch } from "vue";
const props = defineProps({
  modelValue: {
    type: [String, Array],
    default: "",
  },
  limit: {
    type: Number,
    default: 1,
  },
  fileSize: {
    type: Number,
    default: 10,
  },
  multiple: {
    type: Boolean,
    default: false,
  },
  source: {
    type: String,
    default: "default",
  },
});

const emit = defineEmits(["update:modelValue"]);

// 上传地址（不包含参数）
const uploadUrl = computed(() => {
  return `http://localhost:8080/file/upload`;
});

// 上传额外参数
const uploadData = computed(() => {
  return {
    source: props.source,
  };
});

// 请求头 - 使用 computed 确保响应式更新
const headers = computed(() => {
  const tokenStore = useTokenStore();
  return {
    Authorization: tokenStore.token.token,
  };
});

const fileList = ref([]);
const dialogImageUrl = ref("");
const dialogVisible = ref(false);

// 处理平台切换
const handlePlatformChange = () => {
  // 平台切换后，下次上传会使用新平台
};

// 初始化文件列表
const initFileList = () => {
  // 当 modelValue 为空时，清空 fileList
  if (
    !props.modelValue ||
    (typeof props.modelValue === "string" && props.modelValue === "")
  ) {
    fileList.value = [];
    return;
  }

  if (typeof props.modelValue === "string") {
    fileList.value = [
      {
        name: props.modelValue.substring(props.modelValue.lastIndexOf("/") + 1),
        url: props.modelValue,
      },
    ];
  } else if (Array.isArray(props.modelValue)) {
    fileList.value = props.modelValue.map((url) => ({
      name: url.substring(url.lastIndexOf("/") + 1),
      url: url,
    }));
  }
};

// 处理图片预览
const handlePreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url;
  dialogVisible.value = true;
};

// 处理图片删除
const handleRemove = async (uploadFile) => {
  if (props.multiple) {
    await deleteFileApi(uploadFile.url);
    const urls = props.modelValue.filter((url) => url !== uploadFile.url);
    emit("update:modelValue", urls);
  } else {
    await deleteFileApi(uploadFile.url);
    emit("update:modelValue", "");
  }
};

// 处理上传成功
const handleSuccess = async (response) => {
  console.log(response);
  if (response.code === 200) {
    const url = response.data;
    if (props.multiple) {
      const urls = props.modelValue ? [...props.modelValue] : [];
      urls.push(url);
      emit("update:modelValue", urls);
      fileList.value = urls.map((u) => ({
        name: u.substring(u.lastIndexOf("/") + 1),
        url: u,
      }));
    } else {
      emit("update:modelValue", url);
      fileList.value = [
        {
          name: url.substring(url.lastIndexOf("/") + 1),
          url: url,
        },
      ];
    }
    ElMessage.success("上传成功");
  } else {
    ElMessage.error("上传失败");
  }
};

// 处理超出限制
const handleExceed = () => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件`);
};

// 上传前的校验
const beforeUpload = (file) => {
  const isImage = /^image\/(jpeg|png|gif)$/.test(file.type);
  const isLt = file.size / 1024 / 1024 < props.fileSize;

  if (!isImage) {
    ElMessage.error("只能上传jpg/png/gif格式的图片!");
    return false;
  }
  if (!isLt) {
    ElMessage.error(`图片大小不能超过 ${props.fileSize}MB!`);
    return false;
  }
  return true;
};

// 监听modelValue变化
watch(
  () => props.modelValue,
  () => {
    initFileList();
  },
  { immediate: true }
);
</script>

<style scoped>
.upload-container {
  .upload-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
  }

  .platform-selector {
    margin-bottom: 16px;
  }
}

:deep(.el-upload--picture-card) {
  --el-upload-picture-card-size: 100px;
}

:deep(.el-upload-list--picture-card) {
  --el-upload-list-picture-card-size: 100px;
}
</style>