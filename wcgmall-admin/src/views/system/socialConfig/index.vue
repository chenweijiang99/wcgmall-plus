<template>
  <div class="app-container">
    <!-- 全局设置卡片 -->
    <el-card class="box-card" style="margin-bottom: 20px">
      <template #header>
        <div class="card-header">
          <span>全局设置</span>
        </div>
      </template>
      <el-form :inline="true" :model="globalSettings">
        <el-form-item label="第三方登录">
          <el-switch
            v-model="globalSettings.enabled"
            active-text="开启"
            inactive-text="关闭"
            @change="handleSettingsChange"
          />
        </el-form-item>
        <el-form-item label="登录模式">
          <el-radio-group v-model="globalSettings.loginMode" @change="handleSettingsChange">
            <el-radio-button label="juhe">聚合登录</el-radio-button>
            <el-radio-button label="oauth2">OAuth2登录</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <el-alert
        v-if="globalSettings.loginMode === 'juhe'"
        title="聚合登录模式：使用第三方聚合登录服务，支持多种登录方式，无需单独配置各平台AppId"
        type="info"
        :closable="false"
        show-icon
      />
      <el-alert
        v-else
        title="OAuth2登录模式：需要为每种登录方式单独配置AppId、AppSecret和回调地址"
        type="warning"
        :closable="false"
        show-icon
      />
    </el-card>

    <!-- 登录方式配置列表 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>登录方式配置</span>
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-permission="['sys:socialConfig:add']">
            新增
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="dataList" border>
        <el-table-column label="排序" align="center" prop="sort" width="70" />
        <el-table-column label="图标" align="center" width="80">
          <template #default="scope">
            <div class="icon-preview" v-if="scope.row.icon">
              <div class="svg-icon-wrapper" v-html="scope.row.icon"></div>
            </div>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column label="登录类型" align="center" prop="socialType" width="100" />
        <el-table-column label="显示名称" align="center" prop="socialName" width="100" />
        <el-table-column label="支持模式" align="center" width="150">
          <template #default="scope">
            <el-tag v-if="scope.row.supportJuhe === 1" type="success" size="small" style="margin-right: 5px">聚合</el-tag>
            <el-tag v-if="scope.row.supportOauth2 === 1" type="warning" size="small">OAuth2</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="OAuth2配置" align="center" min-width="200">
          <template #default="scope">
            <template v-if="scope.row.supportOauth2 === 1">
              <el-tag v-if="scope.row.appId" type="success" size="small">已配置</el-tag>
              <el-tag v-else type="danger" size="small">未配置</el-tag>
            </template>
            <span v-else class="text-gray">不支持</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
              v-permission="['sys:socialConfig:update']"
            />
          </template>
        </el-table-column>
        <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
        <el-table-column label="操作" align="center" width="150" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              link
              icon="Edit"
              @click="handleUpdate(scope.row)"
              v-permission="['sys:socialConfig:update']"
            >修改</el-button>
            <el-button
              type="danger"
              link
              icon="Delete"
              @click="handleDelete(scope.row)"
              v-permission="['sys:socialConfig:delete']"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加或修改对话框 -->
    <el-dialog v-model="open" :title="title" width="650px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="登录类型" prop="socialType">
          <el-input v-model="form.socialType" placeholder="请输入登录类型标识，如: qq, wechat" :disabled="form.id !== undefined" />
        </el-form-item>
        <el-form-item label="显示名称" prop="socialName">
          <el-input v-model="form.socialName" placeholder="请输入显示名称，如: QQ, 微信" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <div class="icon-edit-wrapper">
            <div class="icon-edit-preview">
              <div class="preview-box" v-if="form.icon">
                <div class="svg-icon-wrapper svg-icon-large" v-html="form.icon"></div>
              </div>
              <div class="preview-box preview-empty" v-else>
                <span>预览</span>
              </div>
            </div>
            <div class="icon-edit-input">
              <el-input
                v-model="form.icon"
                type="textarea"
                :rows="4"
                placeholder="请粘贴SVG代码，例如：<svg>...</svg>"
              />
              <div class="form-tip">
                请直接粘贴完整的SVG代码。推荐尺寸：24x24 或 32x32
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="支持模式" prop="supportMode">
          <el-checkbox v-model="form.supportJuheChecked" label="聚合登录" />
          <el-checkbox v-model="form.supportOauth2Checked" label="OAuth2登录" />
        </el-form-item>
        <el-form-item label="聚合登录编号" prop="juheTypeCode" v-if="form.supportJuheChecked">
          <el-input-number v-model="form.juheTypeCode" :min="1" :max="20" placeholder="聚合登录类型编号" />
          <div class="form-tip">1=QQ,2=微信,3=支付宝,4=微博,5=百度,6=华为,7=小米,8=微软,9=钉钉,10=Gitee,11=GitHub,12=抖音</div>
        </el-form-item>

        <el-divider v-if="form.supportOauth2Checked">OAuth2 配置</el-divider>
        <template v-if="form.supportOauth2Checked">
          <el-form-item label="AppId" prop="appId">
            <el-input v-model="form.appId" placeholder="请输入OAuth2 AppId/ClientId" />
          </el-form-item>
          <el-form-item label="AppSecret" prop="appSecret">
            <el-input v-model="form.appSecret" placeholder="请输入OAuth2 AppSecret/ClientSecret" show-password />
          </el-form-item>
          <el-form-item label="回调地址" prop="redirectUrl">
            <el-input v-model="form.redirectUrl" placeholder="请输入OAuth2回调地址" />
          </el-form-item>
        </template>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getAllSocialConfigApi,
  detailSocialConfigApi,
  deleteSocialConfigApi,
  addSocialConfigApi,
  updateSocialConfigApi,
  updateSocialConfigStatusApi,
  getSocialSettingsApi,
  updateSocialSettingsApi,
} from "@/api/system/socialConfig";

// 遮罩层
const loading = ref(true);
// 表格数据
const dataList = ref<any[]>([]);
// 弹出层标题
const title = ref("");
// 是否显示弹出层
const open = ref(false);

// 全局设置
const globalSettings = reactive({
  enabled: true,
  loginMode: 'juhe'
});

// 表单参数
const form = reactive<any>({
  supportJuheChecked: true,
  supportOauth2Checked: false,
});

// 表单校验
const rules = reactive({
  socialType: [{ required: true, message: "登录类型不能为空", trigger: "blur" }],
  socialName: [{ required: true, message: "显示名称不能为空", trigger: "blur" }],
});

const formRef = ref();

/** 获取全局设置 */
const getGlobalSettings = () => {
  getSocialSettingsApi().then((response) => {
    globalSettings.enabled = response.data.enabled;
    globalSettings.loginMode = response.data.loginMode;
  });
};

/** 更新全局设置 */
const handleSettingsChange = () => {
  updateSocialSettingsApi(globalSettings).then(() => {
    ElMessage.success("设置已保存");
    getList();
  });
};

/** 查询列表 */
const getList = () => {
  loading.value = true;
  getAllSocialConfigApi().then((response) => {
    dataList.value = response.data;
    loading.value = false;
  });
};

/** 取消按钮 */
const cancel = () => {
  open.value = false;
  reset();
};

/** 表单重置 */
const reset = () => {
  form.id = undefined;
  form.socialType = undefined;
  form.socialName = undefined;
  form.icon = undefined;
  form.juheTypeCode = undefined;
  form.supportJuheChecked = true;
  form.supportOauth2Checked = false;
  form.appId = undefined;
  form.appSecret = undefined;
  form.redirectUrl = undefined;
  form.status = 1;
  form.sort = 0;
  form.remark = undefined;

  formRef.value?.resetFields();
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  open.value = true;
  title.value = "添加第三方登录配置";
};

/** 修改按钮操作 */
const handleUpdate = (row: any) => {
  reset();
  detailSocialConfigApi(row.id).then((response) => {
    const data = response.data;
    Object.assign(form, data);
    form.supportJuheChecked = data.supportJuhe === 1;
    form.supportOauth2Checked = data.supportOauth2 === 1;
    open.value = true;
    title.value = "修改第三方登录配置";
  });
};

/** 状态修改 */
const handleStatusChange = (row: any) => {
  updateSocialConfigStatusApi(row.id, row.status).then(() => {
    ElMessage.success("状态修改成功");
  }).catch(() => {
    row.status = row.status === 1 ? 0 : 1;
  });
};

/** 提交按钮 */
const submitForm = () => {
  formRef.value?.validate((valid: any) => {
    if (valid) {
      // 转换checkbox为数字
      form.supportJuhe = form.supportJuheChecked ? 1 : 0;
      form.supportOauth2 = form.supportOauth2Checked ? 1 : 0;

      if (form.id !== undefined) {
        updateSocialConfigApi(form).then(() => {
          ElMessage.success("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addSocialConfigApi(form).then(() => {
          ElMessage.success("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
};

/** 删除按钮操作 */
const handleDelete = (row: any) => {
  ElMessageBox.confirm('是否确认删除"' + row.socialName + '"的配置?', "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      return deleteSocialConfigApi(row.id);
    })
    .then(() => {
      getList();
      ElMessage.success("删除成功");
    });
};

onMounted(() => {
  getGlobalSettings();
  getList();
});
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.text-gray {
  color: #909399;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
.icon-preview {
  display: flex;
  align-items: center;
  justify-content: center;
}
.svg-icon-wrapper {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.svg-icon-wrapper :deep(svg) {
  width: 100%;
  height: 100%;
}
.svg-icon-large {
  width: 40px;
  height: 40px;
}
.icon-edit-wrapper {
  display: flex;
  gap: 16px;
  width: 100%;
}
.icon-edit-preview {
  flex-shrink: 0;
}
.preview-box {
  width: 60px;
  height: 60px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
}
.preview-empty {
  color: #909399;
  font-size: 12px;
}
.icon-edit-input {
  flex: 1;
}
</style>
