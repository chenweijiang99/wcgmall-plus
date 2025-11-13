<template>
  <el-drawer
    v-model="drawerVisible"
    title="系统设置"
    direction="rtl"
    size="350px"
  >
    <div class="drawer-content">
      <el-divider>系统风格</el-divider>
      <div class="sidebar-style-list">
        <el-tooltip content="亮色侧边栏" placement="top">
          <div 
            class="system-style light" 
            :class="{ active: tempSettings.sidebarStyle === 'light' }"
            @click="handleSave('sidebarStyle', 'light')"
          >
            <div class="style-dot" v-show="tempSettings.sidebarStyle === 'light'"></div>
          </div>
        </el-tooltip>
        <el-tooltip content="暗色侧边栏" placement="top">
          <div 
            class="system-style dark" 
            :class="{ active: tempSettings.sidebarStyle === 'dark' }"
            @click="handleSave('sidebarStyle', 'dark')"
          >
            <div class="style-dot" v-show="tempSettings.sidebarStyle === 'dark'"></div>
          </div>
        </el-tooltip>
      </div>

      <el-divider>主题设置</el-divider>
      <div class="setting-item">
        <span>黑夜模式</span>
        <el-switch
          v-model="tempSettings.theme"
          :active-value="'dark'"
          :inactive-value="'light'"
          inline-prompt
          active-icon="Moon"
          inactive-icon="Sunny"
          @change="(val) => handleSave('theme', val)"
        />
      </div>
      <div class="setting-item">
        <span>主题色</span>
        <el-color-picker
          v-model="tempSettings.themeColor"
          size="small"
          @change="handleThemeChange"
          :predefine="predefineColors"
        />
      </div>

      <div class="setting-item">
        <span>灰色模式</span>
        <el-switch
          v-model="tempSettings.greyMode"
          @change="(val) => handleSave('greyMode', val)"
        />
      </div>

      <el-divider>界面设置</el-divider>
      <div class="setting-item">
        <span>显示 Logo</span>
        <el-switch
          v-model="tempSettings.showLogo"
          @change="(val) => handleSave('showLogo', val)"
        />
      </div>
      <div class="setting-item">
        <span>折叠菜单</span>
        <el-switch
          :model-value="isCollapse"
          @update:model-value="(val) => emit('update:isCollapse', val as boolean)"
        />
      </div>
      <div class="setting-item">
        <span>显示标签页</span>
        <el-switch
          v-model="tempSettings.showTags"
          @change="(val) => handleSave('showTags', val)"
        />
      </div>

      <div class="setting-item">
        <span>动态标题</span>
        <el-switch
          v-model="tempSettings.dynamicTitle"
          @change="(val) => handleSave('dynamicTitle', val)"
        />
      </div>
      <div class="setting-item">
        <span>开启水印</span>
        <el-switch
          v-model="tempSettings.watermark"
          @change="(val) => handleSave('watermark', val)"
        />
      </div>
      <div class="setting-item">
        <span>显示页脚</span>
        <el-switch
          v-model="tempSettings.showFooter"
          @change="(val) => handleSave('showFooter', val)"
        />
      </div>

      <div class="setting-item">
        <span>布局大小</span>
        <el-radio-group
          v-model="tempSettings.fontSize"
          size="small"
          @change="(val) => handleSave('fontSize', val)"
        >
          <el-radio-button value="small">小</el-radio-button>
          <el-radio-button value="default">中</el-radio-button>
          <el-radio-button value="large">大</el-radio-button>
        </el-radio-group>
      </div>

      <el-divider>动画设置</el-divider>
      <div class="setting-item">
        <span>页面切换动画</span>
        <el-select
          v-model="tempSettings.pageAnimation"
          size="small"
          style="width: 120px"
          @change="(val) => handleSave('pageAnimation', val)"
        >
          <el-option label="滑动" value="slide" />
          <el-option label="淡入淡出" value="fade" />
          <el-option label="消退" value="dissolve" />
          <el-option label="无" value="none" />
        </el-select>
      </div>

      <el-divider>标签页样式</el-divider>
      <div class="tags-style-list">
        <div
          v-for="style in tagsStyles"
          :key="style.value"
          class="style-item"
          :class="{ active: settingsStore.tagsStyle === style.value }"
          @click="handleSave('tagsStyle', style.value)"
        >
          <div class="style-preview" :class="style.value">
            <span class="tag">首页</span>
            <span class="tag">列表页</span>
            <span class="tag active">详情页</span>
          </div>
          <div class="style-name">{{ style.label }}</div>
        </div>
      </div>

      <div class="drawer-footer">
        <el-button @click="resetSettings">恢复默认</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { ElMessage } from "element-plus";
import { useSettingsStore } from "@/store/modules/settings";
import type { SettingsState } from "@/store/modules/settings";

const props = defineProps<{
  visible: boolean;
  isCollapse: boolean;
}>();

const emit = defineEmits<{
  "update:visible": [value: boolean];
  "update:isCollapse": [value: boolean];
}>();

const settingsStore = useSettingsStore();

// 处理抽屉显示状态
const drawerVisible = computed({
  get: () => props.visible,
  set: (value) => emit("update:visible", value),
});

// 临时设置状态
const tempSettings = ref<Partial<SettingsState>>({
  theme: settingsStore.theme as "light" | "dark",
  themeColor: settingsStore.themeColor,
  showLogo: settingsStore.showLogo,
  showTags: settingsStore.showTags,
  watermark: settingsStore.watermark,
  greyMode: settingsStore.greyMode,
  fontSize: settingsStore.fontSize,
  pageAnimation: settingsStore.pageAnimation,
  tagsStyle: settingsStore.tagsStyle,
  dynamicTitle: settingsStore.dynamicTitle,
  showFooter: settingsStore.showFooter,
  sidebarStyle: settingsStore.sidebarStyle,
});

const predefineColors = ref([
  "#ff4500",
  "#ff8c00",
  "#ffd700",
  "#90ee90",
  "#00ced1",
  "#1e90ff",
  "#c71585",
]);

// 处理保存
const handleSave = (key: keyof SettingsState, value: any) => {
  tempSettings.value[key] = value;
  settingsStore.saveSettings(tempSettings.value);

  // 处理主题模式切换
  if (key === "theme") {
    document.documentElement.setAttribute("data-theme", value);
  }

  // 处理字体大小切换
  if (key === "fontSize") {
    document.documentElement.setAttribute("data-size", value);
  }
};

// 处理主题色变化
const handleThemeChange = (color: string | null) => {
  if (color) {
    tempSettings.value.themeColor = color;
    settingsStore.saveSettings(tempSettings.value);
  }
};

// 重置设置
const resetSettings = () => {
  settingsStore.resetSettings();
  tempSettings.value = {
    theme: settingsStore.theme,
    themeColor: settingsStore.themeColor,
    showLogo: settingsStore.showLogo,
    showTags: settingsStore.showTags,
    greyMode: settingsStore.greyMode,
    fontSize: settingsStore.fontSize,
    pageAnimation: settingsStore.pageAnimation,
    dynamicTitle: settingsStore.dynamicTitle,
    showFooter: settingsStore.showFooter,
    sidebarStyle: settingsStore.sidebarStyle,
  };
  ElMessage.success("已恢复默认设置");
};

const tagsStyles = [
  { label: "谷歌", value: "card" },
  { label: "边框式", value: "border" },
  { label: "现代风", value: "modern" },
];

</script>

<style scoped lang="scss">
.drawer-content {
  padding: 0 20px 20px 20px;
  padding-bottom: 50px;
  height: 100%;
  overflow-y: auto;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 16px 0;
}

.setting-item span {
  color: var(--el-text-color-primary);
  font-size: 14px;
}

:deep(.el-divider__text) {
  font-size: 15px;
  font-weight: 500;
}

.drawer-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  background-color: var(--el-bg-color);
  border-top: 1px solid var(--el-border-color-light);
  z-index: 1;
}

.tags-style-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin: 16px 0;
}

.style-item {
  cursor: pointer;
  text-align: center;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
  }

  &.active .style-preview {
    border-color: v-bind("settingsStore.themeColor");
  }
}

.style-preview {
  padding: 8px;
  border: 2px solid transparent;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  margin-bottom: 8px;
}

.tag {
  display: inline-block;
  padding: 2px 6px;
  font-size: 12px;
  margin: 2px;
  border-radius: 4px;
  background: var(--el-bg-color);

  &.active {
    background: v-bind("settingsStore.themeColor");
    color: white;
  }
}

.style-name {
  font-size: 13px;
  color: var(--el-text-color-regular);
}

// 添加消退动画样式
:global(.dissolve-enter-active),
:global(.dissolve-leave-active) {
  transition: all 0.3s ease-in-out;
}

:global(.dissolve-enter-from),
:global(.dissolve-leave-to) {
  opacity: 0;
  transform: scale(0.95);
}

:global(.dissolve-enter-to),
:global(.dissolve-leave-from) {
  opacity: 1;
  transform: scale(1);
}

.sidebar-style-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin: 16px 0;
}

.style-item {
  cursor: pointer;
  text-align: center;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
  }

  &.active .style-preview {
    border-color: v-bind("settingsStore.themeColor");
  }
}

.style-preview {
  padding: 8px;
  border: 2px solid transparent;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  margin-bottom: 8px;
}

.tag {
  display: inline-block;
  padding: 2px 6px;
  font-size: 12px;
  margin: 2px;
  border-radius: 4px;
  background: var(--el-bg-color);

  &.active {
    background: v-bind("settingsStore.themeColor");
    color: white;
  }
}

.style-name {
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.system-style {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 60px;
  height: 60px;
  background-color: #f0f2f5;
  border-radius: 8px;
  position: relative;
  box-shadow: 0 1px 2.5px #0000002e;
  cursor: pointer;


  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: 15px;
    height: 100%;
    border-top-left-radius: 8px;
    border-bottom-left-radius: 8px;
  }
  &::after {
    content: "";
    position: absolute;
    top: 0;
    height: 15px;
    width: 100%;
    border-top-left-radius: 8px;
    border-top-right-radius: 8px;
  }

  .style-dot {
    position: absolute;
    bottom: -15px;
    left: 50%;
    transform: translateX(-50%);
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background-color: v-bind("settingsStore.themeColor");
  }

}
.light::before {
  background-color: #fff;
}
.light::after {
  background-color: #fff;
}

.dark::before {
  background-color: #001529d9;
}
.dark::after {
  background-color: #001529d9;
}
</style>
