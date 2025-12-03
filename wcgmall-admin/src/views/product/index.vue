<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form :model="queryParams" ref="queryFormRef" inline>
        <el-form-item label="商品名称" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入商品名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select placeholder="请选择分类" v-model="queryParams.categoryId">
            <el-option v-for="c in categorys" :key="c.id" :label="c.name" :value="c.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="品牌" prop="brandId">
          <el-select placeholder="请选择品牌" v-model="queryParams.brandId">
            <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
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
            v-permission="['sys:product']"
            type="primary"
            icon="Search"
            @click="handleQuery"
            >搜索</el-button
          >
          <el-button v-permission="['sys:product']" icon="Refresh" @click="resetQuery"
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
            v-permission="['sys:product:add']"
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            >新增
          </el-button>
          <el-button
            v-permission="['sys:product:delete']"
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

      <!-- 商品列表 -->
      <el-table
        v-loading="loading"
        :data="dataList"
        @selection-change="handleSelectionChange"
        stripe
      >
        <el-table-column type="expand">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <div style="flex: 1">
                <h4 class="detail-title">商品详情图:</h4>
                <div style="display: flex">
                  <el-image
                    v-for="(item, index) in JSON.parse(scope.row.detailImages)"
                    :key="index"
                    :src="item"
                    class="detail-image"
                  ></el-image>
                </div>
              </div>
              <div style="flex: 1">
                <h4 class="detail-title">详情：</h4>
                <p class="detail-text">{{ scope.row.description }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="id" prop="id" width="50%;"></el-table-column>
        <el-table-column label="主图" prop="image">
          <template #default="scope">
            <el-image :src="scope.row.image" style="width: 50%"></el-image>
          </template>
        </el-table-column>
        <el-table-column label="名称" prop="name"></el-table-column>
        <el-table-column label="价格" prop="price"></el-table-column>
        <el-table-column label="库存" prop="inventory"></el-table-column>
        <el-table-column label="分类" prop="categoryId">
          <template #default="scope">
            <div v-for="c in categorys" :key="c.id">
              <div v-if="c.id == scope.row.categoryId">
                {{ c.name }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="品牌" prop="brandId">
          <template #default="scope">
            <div v-for="b in brands" :key="b.id">
              <div v-if="b.id == scope.row.brandId">
                {{ b.name }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="标签" prop="label"></el-table-column>
        <el-table-column label="状态" prop="status">
          <template #default="scope">
            <el-tag :type="scope.row.status == 1 ? 'success' : 'danger'">
              {{ scope.row.status == 1 ? "上架中" : "下架中" }}
            </el-tag>
            <el-tooltip
              class="item"
              :content="scope.row.status == 1 ? '下架' : '上架'"
              placement="top"
            >
              <el-button
                v-permission="['sys:product:status']"
                @click="stopOrStart(scope.row)"
                circle
                :type="scope.row.status == 1 ? 'info' : 'success'"
              >
                <el-icon v-if="scope.row.status == 1"><Moon /></el-icon>
                <el-icon v-if="scope.row.status == 0"><Sunny /></el-icon>
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="收藏">
          <template #default="scope">
            <el-button
              icon="Star"
              type="warning"
              @click="setOL(scope.row)"
              v-if="OLList.find((item) => item.productId == scope.row.id) == undefined"
              :disabled="OLList.length == 6"
              >设为收藏</el-button
            >
            <el-button v-else icon="Star" type="danger" @click="unSetOL(scope.row)">
              取消收藏
            </el-button>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          align="center"
          class-name="small-padding fixed-width"
        >
          <template #default="scope">
            <el-button
              v-permission="['sys:product:update']"
              type="primary"
              link
              icon="Edit"
              @click="handleUpdate(scope.row)"
              >修改
            </el-button>
            <el-button
              v-permission="['sys:product:delete']"
              type="danger"
              link
              icon="Delete"
              @click="handleDelete(scope.row)"
              >删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="没有数据" />
        </template>
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
        width="800px"
        top="3vh"
        :close-on-click-modal="false"
        @close="cancel"
      >
        <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入商品名称" />
          </el-form-item>
          <el-form-item label="分类" prop="categoryId">
            <el-select placeholder="请选择" v-model="form.categoryId">
              <el-option v-for="c in categorys" :key="c.id" :label="c.name" :value="c.id">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="品牌" prop="brandId">
            <el-select placeholder="请选择" v-model="form.brandId">
              <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="价格" prop="price">
            <el-input v-model="form.price" placeholder="请输入价格" />
          </el-form-item>
          <el-form-item label="库存" prop="inventory">
            <el-input v-model="form.inventory" placeholder="请输入库存" />
          </el-form-item>
          <el-form-item label="标签" prop="label">
            <el-input v-model="form.label" placeholder="请输入标签" />
          </el-form-item>
          <el-form-item label="主图" prop="image">
            <UploadImage v-model="form.image" :limit="1" :source="'product-image'" />
          </el-form-item>
          <el-form-item label="详情图片" prop="detailImages">
            <UploadImageBatch
              v-model="form.detailImages"
              :limit="6"
              :source="'product-detail-image'"
            />
          </el-form-item>
          <el-form-item label="商品描述" prop="description">
            <el-input
              v-model="form.description"
              placeholder="请输入商品描述"
              type="textarea"
              :rows="4"
            />
          </el-form-item>
          <el-form-item label="描述图片" prop="descriptionImage">
            <UploadImageBatch
              v-model="form.descriptionImage"
              :limit="10"
              :source="'product-description-image'"
            />
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
import { ElMessage, ElMessageBox } from "element-plus";
import {
  listProductApi,
  detailProductApi,
  deleteProductApi,
  addProductApi,
  updateProductApi,
  startOrStopApi,
} from "@/api/product/product";

import { listProductBrandAllApi } from "@/api/brand/brand";
import { listProductCategoryAllApi } from "@/api/category/category";

import {
  listOfficialCollectionApi,
  addOfficialCollectionApi,
  deleteOfficialCollectionApi,
} from "@/api/officialCollection/officialCollection";

import ButtonGroup from "@/components/ButtonGroup/index.vue";
import UploadImage from "@/components/Upload/Image.vue";
import UploadImageBatch from "@/components/Upload/ImageBatch.vue";
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
  name: undefined,
  categoryId: undefined,
  brandId: undefined,
  status: undefined,
});

// 表单参数
const form = reactive<any>({
  id: undefined,
  name: "",
  categoryId: undefined,
  brandId: undefined,
  price: undefined,
  inventory: undefined,
  image: "",
  detailImages: [],
  description: "",
  descriptionImage: [],
  label: undefined,
});
// 表单校验
const rules = reactive({
  name: [{ required: true, message: "商品名称不能为空", trigger: "blur" }],
  categoryId: [{ required: true, message: "分类不能为空", trigger: "blur" }],
  brandId: [{ required: true, message: "品牌不能为空", trigger: "blur" }],
  price: [{ required: true, message: "价格不能为空", trigger: "blur" }],
  inventory: [{ required: true, message: "库存不能为空", trigger: "blur" }],
  image: [{ required: true, message: "主图不能为空", trigger: "blur" }],
  description: [{ required: true, message: "商品描述不能为空", trigger: "blur" }],
});

const queryFormRef = ref();
const formRef = ref();
interface Category {
  id: number;
  name: string;
}

interface Brand {
  id: number;
  name: string;
}

interface OfficialCollection {
  id: number;
  productId: number;
}

const categorys = ref<Category[]>([]);
const brands = ref<Brand[]>([]);
const OLList = ref<OfficialCollection[]>([]);
/** 查询列表 */
const getList = async () => {
  loading.value = true;
  try {
    const { data } = await listProductApi(queryParams);
    dataList.value = data.records;
    total.value = data.total;
  } catch (error) {}
  loading.value = false;
};

/** 查询分类下拉列表 */
const getCategoryList = async () => {
  try {
    const { data } = await listProductCategoryAllApi();
    categorys.value = data;
  } catch (error) {}
};

/** 获取品牌下拉列表 */
const getBrandList = async () => {
  try {
    const { data } = await listProductBrandAllApi();
    brands.value = data;
  } catch (error) {}
};
/** 查询官方认证列表 */
const getOLList = async () => {
  try {
    const { data } = await listOfficialCollectionApi();
    OLList.value = data;
  } catch (error) {}
};

/** 取消按钮 */
const cancel = () => {
  open.value = false;
  reset();
};

/** 表单重置 */
const reset = () => {
  form.id = undefined;
  form.name = "";
  form.categoryId = undefined;
  form.brandId = undefined;
  form.price = undefined;
  form.inventory = undefined;
  form.image = "";
  form.detailImages = [];
  form.description = "";
  form.descriptionImage = [];
  form.label = undefined;
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
  detailProductApi(row.id).then((response) => {
    const data = response.data;
    // 解析JSON格式的图片字段
    if (data.detailImages && typeof data.detailImages === "string") {
      try {
        data.detailImages = JSON.parse(data.detailImages);
      } catch (e) {
        data.detailImages = [];
      }
    }
    if (data.descriptionImage && typeof data.descriptionImage === "string") {
      try {
        data.descriptionImage = JSON.parse(data.descriptionImage);
      } catch (e) {
        data.descriptionImage = [];
      }
    }
    Object.assign(form, data);
    open.value = true;
    title.value = "修改";
  });
};

/** 提交按钮 */
const submitForm = async () => {
  await formRef.value?.validate(async (valid: any) => {
    if (valid) {
      try {
        form.detailImages = JSON.stringify(form.detailImages);
        form.descriptionImage = JSON.stringify(form.descriptionImage);
        if (form.id !== undefined) {
          await updateProductApi(form);
          ElMessage.success("修改成功");
        } else {
          await addProductApi(form);
          ElMessage.success("新增成功");
        }
        getList();
        open.value = false;
      } catch (error) {
        ElMessage.error("操作失败");
        console.error("操作失败:", error);
      }
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
      await deleteProductApi(selectedIds.value);
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
      await deleteProductApi(row.id);
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

// 上架下架
const stopOrStart = async (row: any) => {
  try {
    await startOrStopApi(row.id);
    ElMessage.success("修改成功");
    getList();
  } catch (error) {}
};
// 官方认证

const setOL = async (row: any) => {
  try {
    await addOfficialCollectionApi(row.id);
    ElMessage.success("修改成功");
    getList();
    getOLList();
  } catch (error) {}
};
// 取消官方认证
const unSetOL = async (row: any) => {
  try {
    await deleteOfficialCollectionApi(row.id);
    ElMessage.success("修改成功");
    getList();
    getOLList();
  } catch (error) {}
};

onMounted(() => {
  getCategoryList();
  getBrandList();
  getOLList();
  getList();
});
</script>

<style lang="scss" scoped>
.detail-image {
  width: 100px;
  height: 100px;
  margin-right: 0.5rem;
  margin-left: 0.5rem;
  box-shadow: #8c939d;
}
.detail-title {
  margin-left: 1rem;
}
.detail-text {
  margin-left: 1rem;
}
</style>
