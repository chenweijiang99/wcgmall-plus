<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="文件名" prop="filename">
          <el-input
            v-model="queryParams.filename"
            placeholder="请输入文件名"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="文件类型" prop="ext">
          <el-select v-model="queryParams.ext" placeholder="请选择文件类型" clearable>
            <el-option
              v-for="item in fileTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮区域 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <ButtonGroup>
            <el-button type="success" icon="Setting" @click="handleOpenOssConfig"
              >云存储配置</el-button
            >
            <el-button
              type="danger"
              plain
              icon="Delete"
              :disabled="selectedIds.length === 0"
              @click="handleBatchDelete"
              >批量删除
            </el-button>
            <el-button type="primary" icon="Upload" @click="handleUpload"
              >文件上传测试</el-button
            >
          </ButtonGroup>
        </div>
      </template>
      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="fileList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="文件内容" align="center" prop="filename">
          <template #default="scope">
            <div v-if="isImage(scope.row.ext)">
              <el-image
                :initial-index="0"
                :src="scope.row.url"
                style="width: 50px; height: 50px"
                @click="handlePreview(scope.row)"
              />
            </div>
            <div v-else>
              <el-icon style="color: #999; font-size: 24px">
                <Files />
              </el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="文件名"
          align="center"
          prop="filename"
          show-overflow-tooltip
        />
        <el-table-column
          label="文件类型"
          align="center"
          prop="ext"
          show-overflow-tooltip
        />
        <el-table-column label="文件大小" align="center" prop="size">
          <template #default="scope">
            <span>{{ (scope.row.size / 1024).toFixed(1) }} KB</span>
          </template>
        </el-table-column>
        <el-table-column
          label="url"
          align="center"
          prop="url"
          width="300"
          show-overflow-tooltip
        />
        <el-table-column
          label="文件地址"
          align="center"
          prop="url"
          width="300"
          show-overflow-tooltip
        >
          <template #default="scope">
            <span
              >{{ scope.row.basePath }}{{ scope.row.path }}{{ scope.row.filename }}</span
            >
          </template>
        </el-table-column>
        <el-table-column label="存储平台" align="center" prop="platform">
          <template #default="scope">
            <span v-for="item in ossOptions">
              <el-tag :type="item.style" v-if="scope.row.platform === item.value">
                {{ item.label }}
              </el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" align="center" prop="createTime" width="150" />
        <el-table-column label="操作" align="center" width="250" fixed="right">
          <template #default="scope">
            <el-button
              v-permission="['sys:file:delete']"
              type="danger"
              link
              icon="Delete"
              @click="handleDelete(scope.row)"
              >删除</el-button
            >
            <el-button
              type="primary"
              link
              icon="Download"
              @click="handleDownload(scope.row)"
              >下载</el-button
            >
            <!-- 所有文件都可以预览 -->
            <el-button type="success" link icon="View" @click="handlePreview(scope.row)"
              >预览</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 云存储配置 -->
    <el-drawer v-model="drawerVisible" title="云存储配置" direction="rtl" size="40%">
      <el-form
        :model="ossConfigForm"
        label-position="left"
        label-width="100px"
        :rules="rules"
        ref="ossConfigFormRef"
      >
        <el-form-item label="平台" prop="platform">
          <el-radio-group v-model="ossConfigForm.platform" @change="handleChangePlatform">
            <el-radio
              v-for="item in ossOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-radio-group>
        </el-form-item>
        <div v-if="ossConfigForm.platform !== 'local'">
          <el-form-item label="access-key" prop="accessKey">
            <el-input v-model="ossConfigForm.accessKey" placeholder="请输入accessKey" />
          </el-form-item>
          <el-form-item label="secret-key" prop="secretKey">
            <el-input v-model="ossConfigForm.secretKey" placeholder="请输入secretKey" />
          </el-form-item>
          <el-form-item label="空间名" prop="bucket">
            <el-input v-model="ossConfigForm.bucket" placeholder="请输入空间名" />
          </el-form-item>
          <el-form-item label="节点" prop="endpoint">
            <el-input v-model="ossConfigForm.endpoint" placeholder="请输入节点名" />
          </el-form-item>
          <el-form-item label="地域" prop="region">
            <el-input v-model="ossConfigForm.region" placeholder="请输入地域" />
          </el-form-item>
        </div>

        <el-form-item label="域名" prop="domain">
          <el-input v-model="ossConfigForm.domain" placeholder="请输入域名，/结尾" />
        </el-form-item>
        <el-form-item label="存储基础路径" prop="basePath">
          <el-input
            v-model="ossConfigForm.basePath"
            placeholder="请输入存储基础路径，/结尾"
          />
        </el-form-item>
        <div v-if="ossConfigForm.platform === 'local'">
          <el-form-item label="本地存储路径" prop="storagePath" label-width="120px">
            <el-input
              v-model="ossConfigForm.storagePath"
              placeholder="请输入本地存储路径，/结尾,如 D:/Temp/"
            />
          </el-form-item>
          <el-form-item
            label="访问路径"
            v-if="ossConfigForm.enableAccess === 1"
            prop="pathPatterns"
            label-width="120px"
          >
            <el-input
              v-model="ossConfigForm.pathPatterns"
              placeholder="访问路经要与域名后面的路径一致,并/**结尾"
            />
          </el-form-item>
          <el-form-item label="启用访问" prop="enableAccess">
            <el-switch
              v-model="ossConfigForm.enableAccess"
              :active-value="1"
              :inactive-value="0"
            />
          </el-form-item>
        </div>
        <el-form-item label="启用存储" prop="isEnable">
          <el-switch
            v-model="ossConfigForm.isEnable"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
      </el-form>
      <div class="dialog-footer">
        <el-button
          type="primary"
          v-permission="['sys:oss:submit']"
          icon="CircleCheck"
          :loading="ossConfigLoading"
          @click="handleSaveOssConfig"
          >保存</el-button
        >
      </div>
    </el-drawer>

    <!-- 上传图片对话框 -->
    <el-dialog :title="uploadDialog.title" v-model="uploadDialog.visible" width="300px" append-to-body destroy-on-close
      class="custom-dialog">
    <UploadImage v-model="uploadUrl" :source="'filemanager'" :limit="1" />

    </el-dialog>

    <!-- 图片预览弹窗 -->
    <el-image-viewer
      v-if="openPreview && isImage(currentPreviewFile.ext)"
      @close="closeViewer"
      :url-list="previewList"
    />

    <!-- 视频预览弹窗 -->
    <el-dialog
      v-if="openPreview && isVideo(currentPreviewFile.ext)"
      title="视频预览"
      v-model="openPreview"
      width="600px"
      append-to-body
    >
      <div class="video-preview-container">
        <video :src="currentPreviewFile.url" controls style="width: 100%; height: 400px">
          您的浏览器不支持视频播放。
        </video>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeViewer">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 文档预览弹窗 -->
    <el-dialog
      v-if="
        openPreview &&
        !(isImage(currentPreviewFile.ext) || isVideo(currentPreviewFile.ext))
      "
      :title="`文件预览: ${currentPreviewFile.filename}`"
      v-model="openPreview"
      width="80%"
      append-to-body
    >
      <div class="file-preview-container">
        <iframe
          v-if="isPreviewableDocument(currentPreviewFile.ext)"
          :src="currentPreviewFile.url"
          style="width: 100%; height: 600px; border: none"
        ></iframe>
        <div v-else class="no-preview">
          <p>该文件类型不支持在线预览，请下载后查看。</p>
          <el-button type="primary" @click="handleDownload(currentPreviewFile)"
            >下载文件</el-button
          >
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeViewer">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import UploadImage from '@/components/Upload/Image.vue'
import { ElMessage, ElMessageBox } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import {
  getFileListApi,
  deleteFileApi,
  getOssConfigApi,
  addOssApi,
  updateOssApi,
  deleteBatchApi,
} from "@/api/file";
import { getDictDataByDictTypesApi } from "@/api/system/dict";
import { Document } from "@element-plus/icons-vue";

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  filename: undefined,
  ext: undefined,
});

const loading = ref(false);
const total = ref(0);
const fileList = ref([]);
const fileTypeOptions = ref<any[]>([]);
const ossOptions = ref<any[]>([]);

const ossConfigList = ref<any>({});
const drawerVisible = ref(false);
const ossConfigLoading = ref(false);

const ossConfigForm = ref<any>({});
const ossConfigFormRef = ref<any>(null);

// 预览相关
const previewList = ref<string[]>([]);
const openPreview = ref(false);
const currentPreviewFile = ref<any>({});

// 表单校验规则
const rules = reactive<FormRules>({
  platform: [{ required: true, message: "平台不能为空", trigger: "blur" }],
  accessKey: [{ required: true, message: "accessKey不能为空", trigger: "blur" }],
  secretKey: [{ required: true, message: "secretKey不能为空", trigger: "blur" }],
  bucket: [{ required: true, message: "bucket不能为空", trigger: "blur" }],
  endpoint: [{ required: true, message: "endpoint不能为空", trigger: "blur" }],
  domain: [{ required: true, message: "域名不能为空", trigger: "blur" }],
  basePath: [{ required: false, message: "存储基础路径不能为空", trigger: "blur" }],
  storagePath: [{ required: true, message: "本地存储路径不能为空", trigger: "blur" }],
  enableAccess: [{ required: true, message: "启用访问不能为空", trigger: "blur" }],
  pathPatterns: [{ required: true, message: "访问路径不能为空", trigger: "blur" }],
});

// 上传文件弹窗控制
const uploadDialog = reactive({
  title: '',
  visible: false,
  type: 'add',

})
// 文件上传数据
const uploadUrl = ref('')
// 获取文件列表
const getList = async () => {
  loading.value = true;
  try {
    const { data } = await getFileListApi(queryParams);
    fileList.value = data.records;
    total.value = data.total;
  } catch (error) {}
  loading.value = false;
};

// 批量删除相关
const selectedIds = ref<any[]>([]);
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map((item) => item.url);
};
// 批量删除
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning("请至少选择一条数据");
    return;
  }

  ElMessageBox.confirm(`是否确认删除选中的 ${selectedIds.value.length} 个文件?`, "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    try {
      await deleteBatchApi(selectedIds.value);
      ElMessage.success("删除成功");
      getList();
      selectedIds.value = [];
    } catch (error) {
      console.error(error);
    }
  });
};

// 获取状态列表
const getDictList = async () => {
  const { data } = await getDictDataByDictTypesApi(["sys_file_type", "sys_file_oss"]);
  fileTypeOptions.value = data.sys_file_type.list;
  ossOptions.value = data.sys_file_oss.list;
};

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`是否确认删除 ${row.filename} 这个文件?`, "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    try {
      await deleteFileApi(row.url);
      ElMessage.success("删除成功");
      getList();
    } catch (error) {}
  });
};

// 获取存储配置
const getOssConfig = () => {
  getOssConfigApi().then((res) => {
    ossConfigList.value = res.data;
  });
};

// 打开存储配置
const handleOpenOssConfig = () => {
  if (ossOptions.value.length === 0) {
    ElMessage.warning("请先在字典添加云存储类型");
    return;
  }
  ossConfigForm.value = {};

  const ossConfig = ossConfigList.value.find((item: any) => {
    if (item.isEnable === 1) {
      return item;
    }
  });
  Object.assign(ossConfigForm.value, ossConfig);
  drawerVisible.value = true;
};

// 平台改变
const handleChangePlatform = () => {
  ossConfigForm.value.id = undefined;
  ossConfigForm.value.accessKey = "";
  ossConfigForm.value.secretKey = "";
  ossConfigForm.value.bucket = "";
  ossConfigForm.value.domain = "";
  ossConfigForm.value.basePath = "";
  ossConfigForm.value.storagePath = "";
  ossConfigForm.value.region = "";
  ossConfigForm.value.isEnable = 0;

  const ossConfig = ossConfigList.value.find((item: any) => {
    if (item.platform === ossConfigForm.value.platform) {
      return item;
    }
  });

  if (ossConfig) {
    Object.assign(ossConfigForm.value, ossConfig);
  }
};

// 保存云存储配置
const handleSaveOssConfig = async () => {
  await ossConfigFormRef.value.validate(async (valid: any) => {
    if (valid) {
      ossConfigLoading.value = true;
      try {
        if (ossConfigForm.value.id) {
          updateOssApi(ossConfigForm.value).then(() => {
            ElMessage.success("修改成功");
            drawerVisible.value = false;
            getOssConfig();
            ossConfigLoading.value = false;
          });
        } else {
          addOssApi(ossConfigForm.value).then(() => {
            ElMessage.success("保存成功");
            drawerVisible.value = false;
            getOssConfig();
            ossConfigLoading.value = false;
          });
        }
      } catch (error) {}
    }
  });
};

const handleUpload = (file: any) => { 
    uploadDialog.visible = true;
    uploadDialog.title = '上传文件';
    uploadDialog.type = 'add';
    uploadUrl.value =''
};


// 下载
const handleDownload = (row: any) => {
  window.open(row.url);
};

// 分页大小改变
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val;
  getList();
};

// 页码改变
const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val;
  getList();
};

// 搜索
const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

// 重置
const resetQuery = () => {
  queryParams.pageNum = 1;
  queryParams.filename = undefined;
  queryParams.ext = undefined;
  getList();
};

// 判断是否为图片
const isImage = (ext: string | undefined): boolean => {
  if (!ext) return false;
  const extLower = ext.toLowerCase();
  return ["jpg", "jpeg", "png", "gif", "bmp", "webp"].includes(extLower);
};

// 判断是否为视频
const isVideo = (ext: string | undefined): boolean => {
  if (!ext) return false;
  const extLower = ext.toLowerCase();
  return ["mp4", "avi", "mov", "wmv", "flv", "webm"].includes(extLower);
};

// 判断是否为可预览的文档类型
const isPreviewableDocument = (ext: string | undefined): boolean => {
  if (!ext) return false;
  const extLower = ext.toLowerCase();
  // 支持PDF和文本文件预览
  return ["pdf", "txt"].includes(extLower);
};

// 预览文件
const handlePreview = (row: any) => {
  currentPreviewFile.value = row;
  if (isImage(row.ext)) {
    previewList.value = [row.url];
  }
  openPreview.value = true;
};

// 关闭预览
const closeViewer = () => {
  openPreview.value = false;
  previewList.value = [];
  currentPreviewFile.value = {};
};

// 初始化
onMounted(() => {
  getList();
  getDictList();
  getOssConfig();
});
</script>

<style scoped>
.video-preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.file-preview-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.no-preview {
  text-align: center;
  padding: 20px;
}
</style>
