<template>
  <div class="upload-container">
    <!-- 平台选择框 -->
    <!-- <div class="platform-selector">
      <el-select 
        v-model="selectedPlatform" 
        placeholder="请选择上传平台"
        @change="handlePlatformChange"
        size="small"
      >
        <el-option
          v-for="platform in uploadPlatforms"
          :key="platform.value"
          :label="platform.label"
          :value="platform.value"
        />
      </el-select>
    </div> -->

    <el-upload
      v-model:file-list="fileList"
      :action="uploadBatchUrl"
      list-type="picture-card"
      :headers="headers"
      :multiple="multiple"
      :limit="limit"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      :on-exceed="handleExceed"
      :before-upload="beforeUpload"
      :before-remove="beforeRemove"
      :on-change="handleChange"
      :auto-upload="false"
      ref="uploadRef"
    >
      <el-icon><Plus /></el-icon>
      <template #tip>
        <div class="upload-tip">
          只能上传jpg/png/gif文件，且不超过{{ fileSize }}MB，最多上传{{ limit }}个文件
        </div>
      </template>
    </el-upload>

    <!-- 图片排序区域 -->
    <div v-if="fileList.length > 0" class="sortable-container">
      <div class="sort-title">
        <el-icon><Sort /></el-icon>
        <span>拖拽图片调整顺序</span>
      </div>
      <draggable 
        v-model="fileList" 
        item-key="url"
        class="sortable-list"
        @end="handleSortEnd"
      >
        <template #item="{ element, index }">
          <div class="sortable-item">
            <el-image
              :src="element.url"
              fit="cover"
              class="sortable-image"
            />
            <div class="sortable-overlay">
              <span class="sort-index">{{ index + 1 }}</span>
              <el-icon class="sort-handle"><Rank /></el-icon>
            </div>
          </div>
        </template>
      </draggable>
    </div>
    
    <div v-if="rawFileList.length > 0" style="margin-top: 10px;">
      <el-button type="success" @click="submitUpload" :loading="uploadLoading">开始上传</el-button>
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="dialogVisible" top="5vh" title="预览图片">
      <img :src="dialogImageUrl" alt="Preview Image" style="width: 100%; height: 500px; object-fit: contain;" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Sort, Rank } from '@element-plus/icons-vue'
import type { UploadProps, UploadUserFile, UploadInstance } from 'element-plus'
import { getToken } from '@/utils/auth'
import { deleteFileApi } from '@/api/file'
import request from '@/utils/request'
import { ref, computed } from 'vue'
import draggable from 'vuedraggable'

const props = defineProps({
  modelValue: {
    type: [String, Array],
    default: ''
  },
  limit: {
    type: Number,
    default: 9
  },
  fileSize: {
    type: Number,
    default: 10
  },
  multiple: {
    type: Boolean,
    default: true
  },
  source: {
    type: String,
    default: 'default'
  }
})

const emit = defineEmits(['update:modelValue'])

// 解析环境变量中的平台配置
// const parsePlatforms = () => {
//   const platformsStr = import.meta.env.VITE_APP_UPLOAD_PLATFORMS
//   if (platformsStr) {
//     try {
//       return JSON.parse(platformsStr)
//     } catch (e) {
//       console.error('解析上传平台配置失败:', e)
//       return [
//         { label: '本地云', value: 'local' },
//         { label: '阿里云', value: 'ali' }
//       ]
//     }
//   }
//   // 默认平台配置
//   return [
//     { label: '本地云', value: 'local' },
//     { label: '阿里云', value: 'ali' }
//   ]
// }

// 平台选项
// const uploadPlatforms = ref(parsePlatforms())

// // 平台选择，默认为第一个平台
// const selectedPlatform = ref(uploadPlatforms.value[0].value)

// 上传地址（不包含参数）
const uploadBatchUrl = computed(() => {
  return `${import.meta.env.VITE_APP_BASE_API}/file/uploadBatch`
})

// 请求头
const headers = {
  Authorization: getToken()
}

const fileList = ref<UploadUserFile[]>([])
const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const uploadRef = ref<UploadInstance>()
const uploadLoading = ref(false)
const rawFileList = ref<File[]>([])

// 处理平台切换
const handlePlatformChange = () => {
  // 平台切换后，下次上传会使用新平台
}

// 初始化文件列表
const initFileList = () => {
  if (!props.modelValue) return
  
  if (typeof props.modelValue === 'string') {
    fileList.value = [{
      name: props.modelValue.substring(props.modelValue.lastIndexOf('/') + 1),
      url: props.modelValue
    }]
  } else if (Array.isArray(props.modelValue)) {
    fileList.value = (props.modelValue as string[]).map(url => ({
      name: url.substring(url.lastIndexOf('/') + 1),
      url: url
    }))
  }
}

// 处理文件变化
const handleChange: UploadProps['onChange'] = (file, fileList) => {
  // 只在文件添加时处理
  if (file.status === 'ready') {
    // 确保文件对象存在且未被添加过
    if (file.raw && !rawFileList.value.includes(file.raw)) {
      rawFileList.value.push(file.raw)
    }
  }
}

// 处理图片预览
const handlePreview: UploadProps['onPreview'] = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url!
  dialogVisible.value = true
}

// 删除前确认
const beforeRemove: UploadProps['beforeRemove'] = (uploadFile, uploadFiles) => {
  return ElMessageBox.confirm(
    `确定要删除 ${uploadFile.name} 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 从原始文件列表中删除对应文件
    if (uploadFile.raw) {
      const index = rawFileList.value.indexOf(uploadFile.raw)
      if (index > -1) {
        rawFileList.value.splice(index, 1)
      }
    }
    
    // 如果文件已上传，需要从服务器删除
    if (uploadFile.url) {
      deleteFileApi(uploadFile.url).catch(() => {
        ElMessage.error('删除文件失败')
      })
    }
    return true
  }).catch(() => {
    return false
  })
}

// 处理图片删除
const handleRemove: UploadProps['onRemove'] = (uploadFile: any) => {
  if (props.multiple) {
    // 从原有图片数组中删除指定的图片，同时过滤掉blob临时URL
    const currentUrls = Array.isArray(props.modelValue) 
      ? props.modelValue.filter(url => !url.startsWith('blob:'))
      : []
    const newUrls = currentUrls.filter(url => url !== uploadFile.url)
    emit('update:modelValue', newUrls)
  } else {
    emit('update:modelValue', '')
  }
}

// 上传前的校验
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = /^image\/(jpeg|png|gif)$/.test(file.type)
  const isLt = file.size / 1024 / 1024 < props.fileSize

  if (!isImage) {
    ElMessage.error('只能上传jpg/png/gif格式的图片!')
    return false
  }
  if (!isLt) {
    ElMessage.error(`图片大小不能超过 ${props.fileSize}MB!`)
    return false
  }
  
  // 返回 false 阻止自动上传
  return false
}

// 处理超出限制
const handleExceed: UploadProps['onExceed'] = (files, uploadFiles) => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件，当前已选择 ${uploadFiles.length} 个文件`)
}

// 处理重复路径的函数
const normalizeUrl = (url: string) => {
  // 如果URL包含重复的photo路径，则修复它
  if (url.includes('photo,photo/')) {
    return url.replace('photo,photo/', 'photo/')
  }
  return url
}

// 手动提交上传
const submitUpload = async () => {
  if (rawFileList.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }
  
  uploadLoading.value = true
  
  try {
    // 构建 FormData
    const formData = new FormData()
    rawFileList.value.forEach(file => {
      formData.append('files', file)
    })
    // 通过 FormData 传递 source 和 platform 参数
    formData.append('source', props.source)
    
    // 发送请求
    const response = await uploadBatchFiles(formData)
    
    if (response.code === 200) {
      let urls = response.data
      if (Array.isArray(urls)) {
        // 过滤掉blob临时URL，只保留服务器返回的真实URL
        const currentUrls = Array.isArray(props.modelValue) 
          ? props.modelValue.filter(url => !url.startsWith('blob:'))
          : []
        const newUrls = [...currentUrls, ...urls]
        
        emit('update:modelValue', newUrls)
        // 更新文件列表显示
        fileList.value = newUrls.map((url: string, index: number) => ({
          name: `image_${index + 1}.${getFileExtension(url)}`,
          url: url
        }))
        // 清空原始文件列表
        rawFileList.value = []
        ElMessage.success('批量上传成功')
      }
    } else {
      ElMessage.error(response.msg || '上传失败')
    }
  } catch (error) {
    ElMessage.error('上传失败')
    console.error('上传失败:', error)
  } finally {
    uploadLoading.value = false
  }
}

// 批量上传 API
const uploadBatchFiles = (data: FormData) => {
  return request({
    url: '/file/uploadBatch',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data',
      ...headers
    }
  })
}

// 获取文件扩展名
const getFileExtension = (url: string) => {
  const match = url.match(/\.(\w+)(\?.*)?$/)
  return match ? match[1] : 'jpg'
}

// 处理拖拽排序结束
const handleSortEnd = () => {
  // 根据排序后的fileList更新modelValue
  const sortedUrls = fileList.value.map(item => item.url)
  emit('update:modelValue', sortedUrls)
}

// 监听modelValue变化
watch(() => props.modelValue, () => {
  initFileList()
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
  --el-upload-picture-card-size: 100px;
}

:deep(.el-upload-list--picture-card) {
  --el-upload-list-picture-card-size: 100px;
}

/* 排序区域样式 */
.sortable-container {
  margin-top: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 16px;
  background-color: #f8f9fa;
}

.sort-title {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}

.sort-title .el-icon {
  margin-right: 8px;
  color: #409eff;
}

.sortable-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.sortable-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  cursor: move;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.sortable-item:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.sortable-item.sortable-ghost {
  opacity: 0.5;
}

.sortable-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sortable-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.sortable-item:hover .sortable-overlay {
  opacity: 1;
}

.sort-index {
  color: white;
  font-size: 12px;
  font-weight: bold;
  margin-right: 4px;
}

.sort-handle {
  color: white;
  font-size: 14px;
}
</style>