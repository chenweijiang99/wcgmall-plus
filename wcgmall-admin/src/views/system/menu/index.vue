<template>
  <div class="menu-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <ButtonGroup>
            <el-button
              v-permission="['sys:menu:add']"
              type="primary"
              icon="Plus"
              @click="handleAdd(null)"
              >新增</el-button
            >
          </ButtonGroup>
        </div>
      </template>
      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="menuList"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column label="菜单名称" prop="title" show-overflow-tooltip />
        <el-table-column label="图标" align="center" width="60">
          <template #default="{ row }">
            <el-icon v-if="row.icon">
              <component :is="row.icon" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column label="类型" align="center" width="80">
          <template #default="{ row }">
            <el-tag
              :type="
                row.type === MenuTypeEnum.CATALOG
                  ? 'primary'
                  : row.type === MenuTypeEnum.MENU
                  ? 'success'
                  : 'info'
              "
            >
              {{
                row.type === MenuTypeEnum.CATALOG
                  ? "目录"
                  : row.type === MenuTypeEnum.MENU
                  ? "菜单"
                  : "按钮"
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="路由地址" prop="path" show-overflow-tooltip />
        <el-table-column label="组件路径" prop="component" show-overflow-tooltip />
        <el-table-column label="权限标识" prop="perm" show-overflow-tooltip />
        <el-table-column label="排序" prop="sort" width="60" align="center" />

        <el-table-column label="状态" align="center" width="80">
          <template #default="{ row }">
            <el-tag :type="row.hidden === 0 ? 'success' : 'info'">
              {{ row.hidden === 0 ? "显示" : "隐藏" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button
              type="success"
              link
              @click="handleAdd(row)"
              v-permission="['sys:menu:add']"
            >
              <el-icon><Plus /></el-icon>新增
            </el-button>
            <el-button
              type="primary"
              link
              @click="handleEdit(row)"
              v-permission="['sys:menu:update']"
            >
              <el-icon><Edit /></el-icon>修改
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
              v-permission="['sys:menu:delete']"
            >
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加或修改菜单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增菜单' : '修改菜单'"
      width="700px"
      append-to-body
    >
      <el-form ref="menuFormRef" :model="menuForm" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="上级菜单" prop="parentId">
              <el-tree-select
                v-model="menuForm.parentId"
                :data="menuOptions"
                :props="{ label: 'title', value: 'id' }"
                value-key="id"
                placeholder="选择上级菜单"
                check-strictly
                :render-after-expand="false"
                class="flex-grow"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="菜单类型" prop="type">
              <el-radio-group v-model="menuForm.type" class="menu-type-group">
                <el-radio-button :value="MenuTypeEnum.CATALOG">目录</el-radio-button>
                <el-radio-button :value="MenuTypeEnum.MENU">菜单</el-radio-button>
                <el-radio-button :value="MenuTypeEnum.BUTTON">按钮</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="菜单名称" prop="title">
              <el-input v-model="menuForm.title" placeholder="请输入菜单名称" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="menuForm.sort" :min="1" class="flex-grow" />
            </el-form-item>
          </el-col>

          <el-col :span="24" v-if="menuForm.type !== MenuTypeEnum.BUTTON">
            <el-form-item label="图标">
              <el-input v-model="menuForm.icon" placeholder="点击选择图标" readonly>
                <template #prefix>
                  <el-icon v-if="menuForm.icon">
                    <component :is="menuForm.icon" />
                  </el-icon>
                </template>
                <template #append>
                  <el-button @click="showIconSelect = true">
                    <el-icon><View /></el-icon>选择图标
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>

          <el-col :span="24" v-if="menuForm.type !== MenuTypeEnum.BUTTON">
            <el-form-item label="路由地址" prop="path">
              <el-input v-model="menuForm.path" placeholder="请输入路由地址" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="重定向地址" prop="redirect">
              <el-input v-model="menuForm.redirect" placeholder="请输入重定向地址" />
            </el-form-item>
          </el-col>

          <el-col :span="24" v-if="menuForm.type === MenuTypeEnum.MENU">
            <el-form-item label="页面路径" prop="component">
              <el-input v-model="menuForm.component" placeholder="/system/user/index">
                <template #prepend>src/views</template>
                <template #append>.vue</template>
              </el-input>
            </el-form-item>
          </el-col>

          <el-col :span="24" v-if="menuForm.type === MenuTypeEnum.BUTTON">
            <el-form-item label="权限标识" prop="perm">
              <el-input v-model="menuForm.perm" placeholder="请输入权限标识" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="状态" prop="hidden">
              <el-radio-group v-model="menuForm.hidden">
                <el-radio :value="0">显示</el-radio>
                <el-radio :value="1">隐藏</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="是否外链" prop="isExternal">
              <el-radio-group v-model="menuForm.isExternal">
                <el-radio :value="0">否</el-radio>
                <el-radio :value="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm"
            >确 定</el-button
          >
        </div>
      </template>
    </el-dialog>

    <!-- 图标选择器组件 -->
    <icon-select v-model="menuForm.icon" v-model:visible="showIconSelect" />
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from "element-plus";
import type { FormInstance } from "element-plus";
import IconSelect from "@/components/IconSelect/index.vue";
import {
  getMenuListApi,
  createMenuApi,
  updateMenuApi,
  deleteMenuApi,
} from "@/api/system/menu";
import { MenuTypeEnum } from "@/enum/MenuTypeEnum";

const loading = ref(false);
const dialogVisible = ref(false);
const dialogType = ref<"add" | "edit">("add");
const menuFormRef = ref<FormInstance>();
const showIconSelect = ref(false);
const submitLoading = ref(false);

// 表单校验规则
const rules = {
  parentId: [{ required: true, message: "请选择父级菜单", trigger: "blur" }],
  title: [{ required: true, message: "请输入菜单名称", trigger: "blur" }],
  sort: [{ required: true, message: "请输入排序", trigger: "blur" }],
  path: [{ required: true, message: "请输入路由地址", trigger: "blur" }],
  component: [{ required: false, message: "请输入页面路径", trigger: "blur" }],
  perm: [{ required: true, message: "请输入权限标识", trigger: "blur" }],
  hidden: [{ required: true, message: "请选择状态", trigger: "blur" }],
};

// 菜单表单对象
const menuForm = reactive<any>({
  id: undefined,
  parentId: 0,
  name: "",
  sort: 0,
  path: "",
  component: "",
  redirect: "",
  type: MenuTypeEnum.CATALOG,
  perms: "",
  icon: "",
  hidden: 0,
  isExternal: 0,
});

// 模拟菜单数据
const menuList = ref<any>([]);

// 菜单树选项
const menuOptions = ref<any>([]);

// 查询方法
const getList = async () => {
  loading.value = true;
  try {
    const { data } = await getMenuListApi();
    menuList.value = data;
    const topMenu = { id: 0, title: "顶级菜单", children: data };
    menuOptions.value.push(topMenu);
  } catch (error) {}
  loading.value = false;
};

// 提交表单
const submitForm = async () => {
  if (!menuFormRef.value) return;

  await menuFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        const formData = { ...menuForm };
        formData.icon = formatIconName(formData.icon);

        if (dialogType.value === "add") {
          await createMenuApi(formData);
          ElMessage.success("新增成功");
        } else {
          await updateMenuApi(formData);
          ElMessage.success("修改成功");
        }
        dialogVisible.value = false;
        getList();
      } catch (error) {
      } finally {
        submitLoading.value = false;
      }
    }
  });
};

// 删除菜单
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除菜单"${row.title}"吗？删除后，菜单下的所有子菜单也会被删除。`,
    "警告",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  )
    .then(async () => {
      try {
        await deleteMenuApi(row.id);
        ElMessage.success("删除成功");
        getList();
      } catch (error) {}
    })
    .catch(() => {});
};

// 查询
const handleQuery = () => {
  getList();
};
// 重置表单
const resetForm = () => {
  menuForm.id = undefined;
  menuForm.parentId = 0;
  menuForm.title = "";
  menuForm.sort = 0;
  menuForm.path = "";
  menuForm.redirect = "";
  menuForm.component = "";
  menuForm.type = MenuTypeEnum.CATALOG;
  menuForm.perm = "";
  menuForm.icon = "";
  (menuForm.hidden = 0), (menuForm.isExternal = 0);
};

// 新增菜单
const handleAdd = (row?: any) => {
  resetForm();
  if (row) {
    menuForm.parentId = row.id;
  }
  dialogType.value = "add";
  dialogVisible.value = true;
};

// 修改菜单
const handleEdit = (row: any) => {
  resetForm();
  dialogType.value = "edit";
  dialogVisible.value = true;
  // 格式化图标名称
  const formData = { ...row };
  formData.icon = formatIconName(formData.icon);
  Object.assign(menuForm, formData);
};

// 在组件挂载时获取数据
onMounted(() => {
  getList();
});

// 处理图标名称转换
const formatIconName = (icon: string) => {
  if (!icon) return "";
  // 如果是 el-icon- 开头，移除前缀
  if (icon.startsWith("el-icon-")) {
    return icon.replace("el-icon-", "");
  }
  // 确保首字母大写
  return icon.charAt(0).toUpperCase() + icon.slice(1);
};
</script>
