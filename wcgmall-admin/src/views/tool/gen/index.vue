<template>
  <div class="app-container">
    <!-- 搜索工具栏 -->
    <div class="search-wrapper">
      <el-form :model="queryParams" ref="queryFormRef" inline>
        <el-form-item label="表名称" prop="tableName">
          <el-input
            v-model="queryParams.tableName"
            placeholder="请输入表名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-card>
      <!-- 操作按钮区域 -->
      <template #header>
        <div class="card-header">
          <ButtonGroup>
            <el-button type="primary" icon="Download" @click="handleGenCode"
              >生成代码</el-button
            >

            <el-button type="info" icon="Upload" @click="openImportTable"
              >导入表结构</el-button
            >

            <el-button
              type="danger"
              icon="Delete"
              @click="handleBatchDelete"
              :disabled="!ids.length"
              >批量删除</el-button
            >
          </ButtonGroup>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" align="center" width="55" />
        <el-table-column
          label="表名称"
          align="center"
          prop="tableName"
          :show-overflow-tooltip="true"
        />
        <el-table-column
          label="表描述"
          align="center"
          prop="tableComment"
          :show-overflow-tooltip="true"
        />
        <el-table-column label="创建时间" align="center" prop="createTime" />
        <el-table-column
          label="操作"
          align="center"
          class-name="small-padding fixed-width"
        >
          <template #default="{ row }">
            <el-button
              type="success"
              icon="View"
              link
              @click="handlePreview(row)"
              >预览</el-button
            >
            <el-button
              type="danger"
              link
              icon="Delete"
              @click="handleDelete(row)"
              >删除</el-button
            >
            <el-button
              type="info"
              link
              icon="Refresh"
              @click="handleSynchDb(row)"
              >同步</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
      <!-- 预览界面 -->
      <el-dialog
        v-model="preview.open"
        :title="preview.title"
        width="80%"
        top="5vh"
        append-to-body
        @close="handlePreviewClose"
      >
        <el-tabs v-model="preview.activeName">
          <el-tab-pane
            v-for="(value, key) in preview.data"
            :key="key"
            :label="key"
            :name="key"
          >
            <div class="code-preview-wrapper">
              <div class="code-preview-header">
                <el-button
                  type="primary"
                  link
                  icon="CopyDocument"
                  @click="handleCopyCode(value)"
                  >复制代码</el-button
                >
              </div>
              <pre><code v-html="highlightedCode(value)"></code></pre>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-dialog>

      <!-- 导入表 -->
      <el-dialog
        v-model="import_visible"
        :title="import_title"
        width="800px"
        top="5vh"
      >
        <!-- 添加搜索框 -->
        <el-form :model="dbTableParams" ref="dbQueryFormRef" inline>
          <el-form-item label="表名称" prop="tableName">
            <el-input
              v-model="dbTableParams.tableName"
              placeholder="请输入表名称"
              clearable
              @keyup.enter="handleDbTableSearch"
            />
          </el-form-item>
          <el-form-item label="表描述" prop="tableComment">
            <el-input
              v-model="dbTableParams.tableComment"
              placeholder="请输入表描述"
              clearable
              @keyup.enter="handleDbTableSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button @click="resetDbTableSearch">重置</el-button>
            <el-button type="primary" @click="handleDbTableSearch"
              >搜索</el-button
            >
          </el-form-item>
        </el-form>

        <!-- 修改表格选择方式 -->
        <el-table
          ref="dbTableRef"
          v-loading="dbTableLoading"
          :data="dbTableList"
          @selection-change="handleDbTableSelectionChange"
          height="350px"
        >
          <el-table-column type="selection" align="center" width="55" />
          <el-table-column
            label="表名称"
            align="center"
            prop="tableName"
            :show-overflow-tooltip="true"
          />
          <el-table-column
            label="表描述"
            align="center"
            prop="tableComment"
            :show-overflow-tooltip="true"
          />
          <el-table-column label="创建时间" align="center" prop="createTime" />
          <el-table-column label="更新时间" align="center" prop="updateTime" />
        </el-table>

        <div class="pagination-container">
          <el-pagination
            background
            v-model:current-page="dbTableParams.pageNum"
            v-model:page-size="dbTableParams.pageSize"
            :total="dbTableTotal"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleDbTableSizeChange"
            @current-change="handleDbTableCurrentChange"
          />
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="closeImport">取 消</el-button>
            <el-button
              type="primary"
              @click="submitImport"
              :disabled="!dbTableSelection.length"
              >确 定</el-button
            >
          </div>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ElMessage, ElMessageBox } from "element-plus";
import {
  listTableApi,
  previewTableApi,
  delTableApi,
  downloadCodeApi,
  synchDbApi,
  listDbTableApi,
  importTableApi,
} from "@/api/tool/gen";
import hljs from "highlight.js";
import "highlight.js/styles/github.css";
import { saveAs } from 'file-saver'
// refs
const queryFormRef = ref();
const dbQueryFormRef = ref();
const dbTableRef = ref();

// 数据
const loading = ref(false);
const ids = ref<string[]>([]);
const total = ref(0);
const tableList = ref([]);

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  tableName: undefined,
});

const preview = reactive({
  open: false,
  title: "代码预览",
  data: {},
  activeName: "entity.java",
});

const import_visible = ref(false);
const import_title = ref("导入表");
const dbTableList = ref([]);
const dbTableTotal = ref(0);
const dbTableLoading = ref(false);
const dbTableSelection = ref<any[]>([]);

const dbTableParams = reactive({
  pageNum: 1,
  pageSize: 10,
  tableName: undefined,
  tableComment: undefined,
});

// 添加代码高亮缓存
const codeCache = new Map();

// 方法
const getList = async () => {
  try {
    loading.value = true;
    const { data } = await listTableApi(queryParams);
    tableList.value = data.rows;
    total.value = data.total;
  } catch (error) {
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

const handleSizeChange = (val: number) => {
  queryParams.pageSize = val;
  getList();
};

const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val;
  getList();
};

const handlePreview = async (row: any) => {
  try {
    const { data } = await previewTableApi(row.tableId);
    preview.data = data;
    preview.open = true;
  } catch (error) {}
};

// 优化后的代码高亮方法
const highlightedCode = (code: string) => {
  if (!code || !preview.open) return "";

  // 生成缓存key
  const cacheKey = `${preview.activeName}-${code}`;

  // 如果缓存中存在，直接返回缓存的结果
  if (codeCache.has(cacheKey)) {
    return codeCache.get(cacheKey);
  }

  let result = "";
  const language = preview.activeName.split(".").pop()?.toLowerCase() || "";

  try {
    // 根据文件扩展选择语言
    const languageMap = {
      vue: "html",
      html: "html",
      java: "java",
      xml: "xml",
      sql: "sql",
      js: "javascript",
      ts: "typescript",
    };

    const lang =
      languageMap[language as keyof typeof languageMap] || "plaintext";
    result = hljs.highlight(code, { language: lang }).value;

    // 存入缓存
    codeCache.set(cacheKey, result);
    return result;
  } catch (error) {
    console.error("代码高亮失败:", error);
    return code;
  }
};

const handleGenCode = async () => {
  if (!ids.value.length) {
    ElMessage.warning("请选择要生成的表");
    return;
  }
   try {
     const tableNames = tableList.value
       .filter((item: any) => ids.value.includes(item.tableId))
       .map((item: any) => item.tableName)
       .join(",");
     const response = await downloadCodeApi(tableNames);
     const blob = new Blob([response.data], {
           type: "application/zip",
     });
     saveAs(blob, "code.zip");
     ElMessage.success("生成成功");
   } catch (error) {}
};

const handleSynchDb = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确认要强制同步"${row.tableName}"表结构吗？`,
      "警告",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }
    );
    await synchDbApi(row.tableName);
    ElMessage.success("同步成功");
  } catch (error) {
    if (error !== "cancel") ElMessage.error("同步失败");
  }
};

const openImportTable = () => {
  import_visible.value = true;
  Object.assign(dbTableParams, {
    pageNum: 1,
    pageSize: 10,
    tableName: undefined,
    tableComment: undefined,
  });
  getDbTableList();
};

const closeImport = () => {
  import_visible.value = false;
  dbTableList.value = [];
  dbTableSelection.value = [];
  dbTableRef.value?.clearSelection();
};

const getDbTableList = async () => {
  try {
    dbTableLoading.value = true;
    const { data } = await listDbTableApi(dbTableParams);
    dbTableList.value = data.rows;
    dbTableTotal.value = data.total;
  } catch (error) {
  } finally {
    dbTableLoading.value = false;
  }
};

const handleDbTableSearch = () => {
  dbTableParams.pageNum = 1;
  getDbTableList();
};

const handleDbTableSizeChange = (val: number) => {
  dbTableParams.pageSize = val;
  getDbTableList();
};

const handleDbTableCurrentChange = (val: number) => {
  dbTableParams.pageNum = val;
  getDbTableList();
};

const resetDbTableSearch = () => {
  dbQueryFormRef.value?.resetFields();
  Object.assign(dbTableParams, {
    pageNum: 1,
    pageSize: 10,
    tableName: undefined,
    tableComment: undefined,
  });
  handleDbTableSearch();
};

const handleDbTableSelectionChange = (selection: any[]) => {
  dbTableSelection.value = selection;
};

const submitImport = async () => {
  if (!dbTableSelection.value.length) {
    ElMessage.warning("请选择要导入的表");
    return;
  }

  try {
    const tables = dbTableSelection.value.map((row: any) => row.tableName);
    await importTableApi(tables);
    ElMessage.success("导入成功");
    closeImport();
    getList();
  } catch (error) {}
};

const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.tableId);
};

const handleCopyCode = async (code: string) => {
  try {
    await navigator.clipboard.writeText(code);
    ElMessage.success("复制成功");
  } catch (error) {}
};

// 修改预览关闭处理方法
const handlePreviewClose = () => {
  preview.data = {};
  preview.activeName = "entity.java";
  // 清空缓存
  codeCache.clear();
};

// 添加 tab 切换处理法
const handleTabChange = () => {
  // 可以在这里做一些优化，比如预加载下一个 tab 的内容
};

// 添加删除方法
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认要删除表"${row.tableName}"吗？`, "警告", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    await delTableApi(row.tableId);
    ElMessage.success("删除成功");
    getList();
  } catch (error) {}
};

// 生命周期
onMounted(() => {
  getList();
});

// 批量删除
const handleBatchDelete = async () => {
  if (!ids.value.length) {
    ElMessage.warning("请选择要删除的表");
    return;
  }

  try {
    await ElMessageBox.confirm("确认要删除选中的表吗？", "警告", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    await delTableApi(ids.value.join(","));
    ElMessage.success("删除成功");
    getList();
    ids.value = [];
  } catch (error) {}
};
</script>

<style scoped>
.hljs {
  padding: 15px;
  border-radius: 5px;
  background-color: #f5f5f5;
}

.el-dialog :deep(.el-table) {
  margin: 10px 0;
}

.mb8 {
  margin-bottom: 8px;
}

/* 添加代码高亮相关样式 */
pre {
  margin: 0;
  padding: 16px;
  overflow: auto;
  font-size: 14px;
  line-height: 1.5;
  background-color: #f6f8fa;
  border-radius: 6px;
}

code {
  font-family: Consolas, Monaco, "Andale Mono", "Ubuntu Mono", monospace;
  white-space: pre;
  word-spacing: normal;
  word-break: normal;
  word-wrap: normal;
}

/* 引入指定的highlight.js样式 */
:deep(.hljs) {
  background: #f6f8fa;
  color: #24292e;
}

:deep(.hljs-keyword) {
  color: #d73a49;
}

:deep(.hljs-string) {
  color: #032f62;
}

:deep(.hljs-comment) {
  color: #6a737d;
}

:deep(.hljs-function) {
  color: #6f42c1;
}

:deep(.hljs-number) {
  color: #005cc5;
}

:deep(.hljs-operator) {
  color: #d73a49;
}

:deep(.hljs-class) {
  color: #6f42c1;
}

:deep(.hljs-attr) {
  color: #005cc5;
}

:deep(.hljs-built_in) {
  color: #6f42c1;
}

:deep(.hljs-property) {
  color: #005cc5;
}

:deep(.hljs-variable) {
  color: #24292e;
}

:deep(.hljs-title) {
  color: #6f42c1;
}

/* 修改代码预览区域样式 */
.code-preview-wrapper {
  height: 500px;
  overflow-y: auto;
  background-color: #f6f8fa;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
}

pre {
  margin: 0;
  flex: 1;
  overflow: auto;
}

/* 添加代码预览头部样式 */
.code-preview-header {
  padding: 8px 16px;
  background-color: #f6f8fa;
  border-bottom: 1px solid #e1e4e8;
  display: flex;
  justify-content: flex-end;
}

/* 自定义滚动条样式 */
.code-preview-wrapper::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.code-preview-wrapper::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.code-preview-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}
</style>
