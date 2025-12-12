<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>网站配置</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" label-width="140px" v-loading="loading">
        <!-- Logo配置 -->
        <el-divider content-position="left">Logo配置</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="后台Logo">
              <UploadImage v-model="form.site_admin_logo" source="site" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="后台网站名称">
              <el-input v-model="form.site_admin_title" placeholder="请输入后台网站名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户端Header Logo">
              <UploadImage v-model="form.site_user_header_logo" source="site" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户端Footer Logo">
              <UploadImage v-model="form.site_user_footer_logo" source="site" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="用户端网站名称">
          <el-input v-model="form.site_user_title" placeholder="请输入用户端网站名称" />
        </el-form-item>

        <!-- Footer配置 -->
        <el-divider content-position="left">Footer配置</el-divider>

        <el-form-item label="网站描述">
          <el-input
            v-model="form.site_footer_description"
            type="textarea"
            :rows="4"
            placeholder="请输入网站底部描述文字"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系地址">
              <el-input v-model="form.site_footer_address" placeholder="请输入联系地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系邮箱">
              <el-input v-model="form.site_footer_email" placeholder="请输入联系邮箱" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="版权信息">
              <el-input v-model="form.site_footer_copyright" placeholder="请输入版权信息" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ICP备案号">
              <el-input v-model="form.site_footer_icp" placeholder="请输入ICP备案号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            保存配置
          </el-button>
          <el-button @click="getSettings">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from "element-plus";
import { getSiteConfigMapApi, updateSiteConfigApi } from "@/api/system/config";
import { useSettingsStore } from "@/store/modules/settings";
import UploadImage from "@/components/Upload/Image.vue";

const settingsStore = useSettingsStore();
const loading = ref(false);
const submitting = ref(false);

// 表单数据
const form = reactive<Record<string, string>>({
  site_admin_logo: "",
  site_admin_title: "",
  site_user_header_logo: "",
  site_user_footer_logo: "",
  site_user_title: "",
  site_footer_description: "",
  site_footer_copyright: "",
  site_footer_address: "",
  site_footer_email: "",
  site_footer_icp: "",
});

/** 获取网站配置 */
const getSettings = async () => {
  loading.value = true;
  try {
    const res = await getSiteConfigMapApi();
    if (res.code === 200 && res.data) {
      Object.assign(form, res.data);
    }
  } catch (error) {
    console.error("获取网站配置失败:", error);
  } finally {
    loading.value = false;
  }
};

/** 提交表单 */
const handleSubmit = async () => {
  submitting.value = true;
  try {
    await updateSiteConfigApi(form);
    ElMessage.success("保存成功");
    // 刷新 Store 中的网站配置
    await settingsStore.refreshSiteConfig();
  } catch (error) {
    console.error("保存网站配置失败:", error);
    ElMessage.error("保存失败");
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  getSettings();
});
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-divider__text) {
  font-weight: bold;
  color: #303133;
}
</style>
