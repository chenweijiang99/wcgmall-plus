<template>
  <div class="upload-container">
    <!-- 平台选择框 -->
    <div class="platform-selector">
      <el-select 
        v-model="selectedPlatform" 
        placeholder="请选择上传平台"
        size="small"
      >
        <el-option
          v-for="platform in uploadPlatforms"
          :key="platform.value"
          :label="platform.label"
          :value="platform.value"
        />
      </el-select>
    </div>

    <el-upload
      :action="uploadUrl"
      list-type="picture-card"
      :headers="headers"
      :limit="1"
  
      :on-success="handleSuccess"
      :on-exceed="handleExceed"
      :before-upload="beforeUpload"
      :data="uploadData"
      :on-change="handleChange"
      :on-error="handleError"
      :show-file-list="false"
      ref="uploadRef"
    >
      <div v-if="uploadedVideoUrl" class="uploaded-video-container">
        <video class="upload-video" :src="uploadedVideoUrl" />
        <div class="video-actions">
          <span class="video-preview" @click.stop="handlePreviewClick">
            <el-icon size="20"><VideoPlay /></el-icon>
          </span>
          <span class="video-delete" @click.stop="handleRemove">
            <el-icon size="20"><Delete /></el-icon>
          </span>
        </div>
      </div>
      <div v-else-if="isTranscoding" class="transcoding-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <div class="transcoding-text">正在上传中...</div>
      </div>
      <div v-else class="upload-placeholder">
        <el-icon><Plus /></el-icon>
      </div>
      <template #tip>
        <div class="upload-tip">
          只能上传Mp4文件，且不超过{{ fileSize }}MB
        </div>
      </template>
    </el-upload>

    <!-- 视频预览对话框 -->
    <el-dialog v-model="dialogVisible" top="5vh" title="预览视频">
      <video :src="dialogVideoUrl" controls style="width: 100%; height: 500px; object-fit: contain;"></video>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ZoomIn, Delete, Loading } from '@element-plus/icons-vue'
import type { UploadProps, UploadInstance } from 'element-plus'
import { getToken } from '@/utils/auth'
import { deleteFileApi } from '@/api/file'
import { ref, computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  fileSize: {
    type: Number,
    default: 50
  },
  source: {
    type: String,
    default: 'default'
  }
})

const emit = defineEmits(['update:modelValue'])

// 解析环境变量中的平台配置
const parsePlatforms = () => {
  const platformsStr = import.meta.env.VITE_APP_UPLOAD_PLATFORMS
  if (platformsStr) {
    try {
      return JSON.parse(platformsStr)
    } catch (e) {
      console.error('解析上传平台配置失败:', e)
    }
  }
}

// 平台选项
const uploadPlatforms = ref(parsePlatforms())

// 平台选择，默认为第一个平台
const selectedPlatform = ref(uploadPlatforms.value[0].value)

// 上传地址（不包含参数）
const uploadUrl = computed(() => {
  return `${import.meta.env.VITE_APP_BASE_API}/file/upload`
})

// 上传额外参数
const uploadData = computed(() => {
  return {
    source: props.source,
    platform: selectedPlatform.value
  }
})

// 请求头
const headers = {
  Authorization: getToken()
}

const uploadRef = ref<UploadInstance>()
const uploadedVideoUrl = ref(props.modelValue)
const dialogVideoUrl = ref('')
const dialogVisible = ref(false)
const isUploading = ref(false)
const isTranscoding = ref(false)

// 处理平台切换
const handlePlatformChange = () => {
  // 平台切换后，下次上传会使用新平台
}

// 初始化文件URL
const initFileUrl = () => {
  uploadedVideoUrl.value = props.modelValue
}

// 处理文件状态变化
const handleChange: UploadProps['onChange'] = (file, fileList) => {
  // 当文件被添加到上传队列时，设置上传状态
  if (file.status === 'ready') {
    isUploading.value = true
    isTranscoding.value = true
  }
}

// 处理视频预览
const handlePreviewClick = () => {
  if (uploadedVideoUrl.value) {
    dialogVideoUrl.value = uploadedVideoUrl.value
    dialogVisible.value = true
  }
}

// 处理上传成功
const handleSuccess: UploadProps['onSuccess'] = async (response) => {
  isUploading.value = false
  isTranscoding.value = false
  
  if (response.code === 200) {
    const url = response.data
    uploadedVideoUrl.value = url
    emit('update:modelValue', url)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

// 处理上传错误
const handleError: UploadProps['onError'] = (error) => {
  isUploading.value = false
  isTranscoding.value = false
  ElMessage.error('上传失败: ' + error.message)
}

// 处理超出限制
const handleExceed: UploadProps['onExceed'] = () => {
  ElMessage.warning('只能上传一个文件')
}

// 上传前的校验
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isVideo = /^video\/(mp4)$/.test(file.type)
  const isLt = file.size / 1024 / 1024 < props.fileSize

  if (!isVideo) {
    ElMessage.error('只能上传Mp4格式的视频!')
    return false
  }
  if (!isLt) {
    ElMessage.error(`视频大小不能超过 ${props.fileSize}MB!`)
    return false
  }
  return true
}

// 处理视频删除
const handleRemove = async () => {
  if (!uploadedVideoUrl.value) return
  
  ElMessageBox.confirm('确定要删除该文件吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      // 调用API删除文件
      await deleteFileApi(uploadedVideoUrl.value)
      
      // 清空上传组件中的文件
      uploadRef.value?.clearFiles()
      
      // 重置状态
      uploadedVideoUrl.value = ''
      emit('update:modelValue', '')
      
      ElMessage.success('删除成功')
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 监听modelValue变化
watch(() => props.modelValue, () => {
  initFileUrl()
}, { immediate: true })
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
  --el-upload-picture-card-size: 148px;
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8c939d;
}

.uploaded-video-container {
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: none;
  align-items: center;
  justify-content: center;
  gap: 20px;
  background-color: rgba(0, 0, 0, 0.3);
}

.uploaded-video-container:hover .video-actions {
  display: flex;
}

.video-preview,
.video-delete {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  background-color: rgba(255, 255, 255, 0.5);
  border-radius: 50%;
  color: #606266;
  cursor: pointer;
  font-size: 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  transition: all 0.3s;
}

.video-preview:hover,
.video-delete:hover {
  background-color: white;
  color: #409eff;
  transform: scale(1.1);
}

.transcoding-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #409eff;
}

.transcoding-text {
  margin-top: 8px;
  font-size: 12px;
}
</style>