<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form :model="queryParams" ref="queryFormRef" inline>
        <el-form-item label="作者" prop="author">
          <el-input
            v-model="queryParams.author"
            placeholder="请输入作者名"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="博客标题" prop="title">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入博客标题"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="是否发布" prop="status">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            @keyup.enter="handleQuery"
          >
            <el-option label="下架" value="0" />
            <el-option label="上架" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            v-permission="['sys:blog']"
            type="primary"
            icon="Search"
            @click="handleQuery"
            >搜索</el-button
          >
          <el-button v-permission="['sys:blog']" icon="Refresh" @click="resetQuery"
            >重置</el-button
          >
        </el-form-item>
      </el-form>
    </div>
    <el-card class="box-card">
      <!-- 操作工具栏 -->
      <template #header>
        <ButtonGroup>
          <el-button
            v-permission="['sys:blog:add']"
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            >新增
          </el-button>
          <el-button
            v-permission="['sys:blog:delete']"
            type="danger"
            plain
            icon="Delete"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
            >批量删除
          </el-button>
        </ButtonGroup>
      </template>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="dataList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="封面" align="center" prop="image">
          <template #default="scope">
            <el-image
              style="width: 120px; height: 80px; border-radius: 10px"
              :src="scope.row.image"
            />
          </template>
        </el-table-column>
        <el-table-column label="标题" align="center" prop="title" show-overflow-tooltip>
          <template #default="scope">
            <span style="color: var(--el-color-primary)">{{ scope.row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column label="作者" align="center" prop="author" />

        <!-- <el-table-column label="博客内容" align="center" prop="content" /> -->

        <el-table-column label="状态" align="center" prop="status">
          <template #default="scope">
            <el-switch
              @change="handleChangeStatus(scope.row)"
              style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" />
        <el-table-column label="更新时间" align="center" prop="updateTime" />
        <el-table-column
          label="操作"
          align="center"
          class-name="small-padding fixed-width"
        >
          <template #default="scope">
            <el-button
              v-permission="['sys:blog:update']"
              type="primary"
              link
              icon="Edit"
              @click="handleUpdate(scope.row)"
              >修改
            </el-button>
            <el-button
              v-permission="['sys:blog:delete']"
              type="danger"
              link
              icon="Delete"
              @click="handleDelete(scope.row)"
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
      <el-dialog
        v-model="open"
        :title="title"
        width="1400px"
        top="3vh"
        :close-on-click-modal="false"
      >
        <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
          <el-row :gutter="20" class="mb-20">
            <el-col :span="8">
              <el-form-item label="博客标题" prop="title">
                <el-input v-model="form.title" placeholder="请输入博客标题" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="作者" prop="author">
                <el-input v-model="form.author" placeholder="请输入作者名" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="博客封面" prop="image">
            <UploadImage v-model="form.image" :limit="1" :source="'blog-image'" />
          </el-form-item>

          <!-- <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择状态">
              <el-option label="下架" value="0" />
              <el-option label="上架" value="1" />
            </el-select>
          </el-form-item> -->
          <el-form-item label="博客内容" prop="content">
            <MdEditor
              ref="mdRef"
              v-model="form.contentMd"
              style="height: 500px; width: 100%"
              placeholder="输入文章内容..."
              @on-upload-img="onUploadImg"
              :toolbars="toolbars"
              @onHtmlChanged="saveHtml"
            >
              <template #defToolbars>
                <EmojiExtension :onInsert="insert" />
                <VideoExtension :onInsert="insert" :onUploadVideo="handleUploadVideo" />
                <!-- <VideoExtension @upload-video="uploadVideo" @add-video-url="dialogVisible = true" /> -->
              </template>
            </MdEditor>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button type="primary" @click="submitForm">确 定</el-button>
            <el-button @click="cancel">取 消</el-button>
          </div>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox, UploadRequestOptions } from "element-plus";
import {
  listBlogApi,
  detailBlogApi,
  deleteBlogApi,
  addBlogApi,
  updateBlogApi,
  updateStatusApi,
} from "@/api/blog/blog";
import { uploadApi, deleteFileApi } from "@/api/file";
import ButtonGroup from "@/components/ButtonGroup/index.vue";
import UploadImage from "@/components/Upload/Image.vue";
import { MdEditor, ToolbarNames, config } from "md-editor-v3";
import "md-editor-v3/lib/style.css";
import type { ExposeParam, InsertContentGenerator } from "md-editor-v3";
import EmojiExtension from "@/components/MarkdownExtensions/EmojiExtension/index.vue";
import VideoExtension from "@/components/MarkdownExtensions/VideoExtension/index.vue";
const toolbars: ToolbarNames[] = [
  "bold",
  "underline",
  "italic",
  "strikeThrough",
  "-",
  "title",
  "sub",
  "sup",
  "quote",
  "unorderedList",
  "orderedList",
  "task",
  "-",
  "codeRow",
  "code",
  "link",
  "image",
  "table",
  "mermaid",
  "katex",
  0,
  1,
  "-",
  "revoke",
  "next",
  "save",
  "=",
  "prettier",
  "pageFullscreen",
  "fullscreen",
  "preview",
  "htmlPreview",
  "catalog",
  // 'github'
];
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
// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  author: undefined,
  title: undefined,
  status: undefined,
});

// 表单参数
const form = reactive<any>({});
// 表单校验
const rules = reactive({
  title: [{ required: true, message: "请输入博客标题", trigger: "blur" }],
  image: [{ required: true, message: "请上传博客封面", trigger: "blur" }],
  author: [{ required: true, message: "请输入作者名", trigger: "blur" }],
});

const queryFormRef = ref();
const formRef = ref();
const mdRef = ref<ExposeParam>();

/** 查询列表 */
const getList = async () => {
  loading.value = true;
  try {
    const { data } = await listBlogApi(queryParams);
    dataList.value = data.records;
    total.value = data.total;
  } catch (error) {}
  loading.value = false;
};

/** 取消按钮 */
const cancel = () => {
  open.value = false;
  reset();
};

/** 表单重置 */
const reset = () => {
  form.id = undefined;
  form.title = undefined;
  form.image = undefined;
  form.author = undefined;
  form.contentMd = undefined;
  form.content = undefined;
  form.status = undefined;
};

/** 搜索按钮操作 */
const handleQuery = async () => {
  queryParams.pageNum = 1;
  await getList();
};

/** 重置按钮操作 */
const resetQuery = async () => {
  queryFormRef.value?.resetFields();
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
const handleUpdate = (row: any) => {
  reset();
  detailBlogApi(row.id).then((response) => {
    Object.assign(form, response.data);
    open.value = true;
    title.value = "修改";
  });
};

/** 提交按钮 */
const submitForm = async () => {
  await formRef.value?.validate(async (valid: any) => {
    if (valid) {
      try {
        if (form.id !== undefined) {
          await updateBlogApi(form);
          ElMessage.success("修改成功");
        } else {
          await addBlogApi(form);
          ElMessage.success("新增成功");
        }
        open.value = false;
        getList();
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
      await deleteBlogApi(selectedIds.value);
      ElMessage.success("删除成功");
      getList();
    } catch (error) {
      ElMessage.error("删除失败");
      console.error("删除失败:", error);
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row: any) => {
  ElMessageBox.confirm('是否确认删除编号为"' + row.id + '"的数据项?', "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    try {
      await deleteBlogApi(row.id);
      ElMessage.success("删除成功");
      getList();
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

// 处理图片上传

const onUploadImg = async (files: any[], callback: (arg0: any[]) => void) => {
  const res = await Promise.all(
    files.map((file) => {
      return new Promise((resolve, reject) => {
        const form = new FormData();
        form.append("file", file);
        uploadApi(form, "blog-content")
          .then((res) => {
            resolve(res);
          })
          .catch((err) => {
            reject(err);
          });
      });
    })
  );
  callback(res.map((item: any) => item.data));
};
const saveHtml = (html: string) => {
  form.content = html;
};
const insert = (generator: InsertContentGenerator) => {
  mdRef.value?.insert(generator);
};
// 在文章编辑组件中添加视频上传处理方法
const handleUploadVideo = async (options: UploadRequestOptions) => {
  try {
    const formData = new FormData();
    formData.append("file", options.file);

    // 调用你的视频上传 API
    const res = await uploadApi(formData, "article-video");

    // 处理上传成功后的逻辑，比如插入视频到编辑器
    const videoUrl = res.data;
    const videoTag = `\n<video controls style="width: 100%; height: auto;" src="${videoUrl}">\n  您的浏览器不支持视频标签\n</video>\n`;

    const generator: InsertContentGenerator = () => {
      return {
        targetValue: videoTag,
        select: true,
        deviationStart: 0,
        deviationEnd: 0,
      };
    };

    mdRef.value?.insert(generator);
  } catch (error) {
    ElMessage.error("视频上传失败");
  }
};
// 上架
const handleChangeStatus = async (row: any) => {
  await updateStatusApi({ id: row.id, status: row.status }).then((res) => {
    ElMessage.success("修改成功");
    getList();
  });
};
onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
.mb-20 {
  margin-bottom: 20px;
}
</style>
