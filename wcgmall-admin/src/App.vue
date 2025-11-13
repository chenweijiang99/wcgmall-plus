<template>
  <div :class="{ 'light': settingsStore.theme === 'light' }">
    <router-view ></router-view>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useSettingsStore } from '@/store/modules/settings'

const settingsStore = useSettingsStore()

onMounted(() => {
  // 初始化主题设置
  settingsStore.initTheme()
})
</script>

<style>



html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Handwriting', sans-serif;
}

/* 默认尺寸 */
:root {
  --el-header-height: 56px;
  --el-aside-width: 220px !important;
  --el-menu-item-height: 50px;
  --el-font-size-base: 14px;
}

/* 深色模式样式 */
:root[data-theme='dark'] {
  --el-bg-color: #141414;
  --el-bg-color-overlay: #1d1e1f;
  --el-text-color-primary: #E5EAF3;
  --el-text-color-regular: #CFD3DC;
  --el-border-color-light: #363637;
  --el-bg-color-page: #0a0a0a;
  --el-bg-color-container: #141414;
  --el-fill-color-blank: #1d1e1f;
  --el-mask-color: rgba(0, 0, 0, 0.8);
  --el-border-color: #363637;
  --el-border-color-lighter: #414243;
  --el-text-color-secondary: #A3A6AD;
  --el-text-color-placeholder: #8D9095;
  --el-text-color-disabled: #6C6E72;
  --el-disabled-bg-color: #2d2d2d;
  --el-disabled-text-color: #6C6E72;
  --el-box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.3);
  --el-box-shadow-light: 0 1px 6px 0 rgba(0, 0, 0, 0.2);
}

/* 深色模式下的组件样式 */
:root[data-theme='dark'] {
  .el-container,
  .el-main {
    background-color: var(--el-bg-color-page);
  }

  .el-header,
  .el-aside,
  .el-menu,
  .el-card,
  .el-dialog,
  .el-drawer {
    background-color: var(--el-bg-color-container);
    border-color: var(--el-border-color);
  }

  .el-table {
    --el-table-bg-color: var(--el-bg-color-container);
    --el-table-tr-bg-color: var(--el-bg-color-container);
    --el-table-border-color: var(--el-border-color);
    --el-table-header-bg-color: var(--el-bg-color-container);
    --el-table-row-hover-bg-color: var(--el-fill-color-light);

    th, td {
      background-color: var(--el-bg-color-container);
      border-bottom-color: var(--el-border-color);
    }
  }

  .el-input__inner,
  .el-textarea__inner {
    background-color: var(--el-bg-color-container);
    border-color: var(--el-border-color);
    color: var(--el-text-color-primary);
  }

  .el-dropdown-menu {
    background-color: var(--el-bg-color-container);
    border-color: var(--el-border-color);
  }
}

/* 字体大小设置 */
[data-size="small"] {
  --el-font-size-base: 12px !important;
  --el-font-size-large: 14px !important;
  --el-font-size-small: 10px !important;
}

[data-size="default"] {
  --el-font-size-base: 14px !important;
  --el-font-size-large: 16px !important;
  --el-font-size-small: 12px !important;
}

[data-size="large"] {
  --el-font-size-base: 16px !important;
  --el-font-size-large: 18px !important;
  --el-font-size-small: 14px !important;
}

/* 确保所有组件使用正确的字体大小 */
.el-button,
.el-input,
.el-select,
.el-menu-item,
.el-sub-menu__title,
.el-dropdown-menu__item,
.el-form-item__label,
.el-form-item__content,
.el-table,
.el-dialog__title,
.el-card__header,
.el-tabs__item,
.el-breadcrumb__item,
.el-radio__label,
.el-checkbox__label,
.el-tag,
.el-pagination,
.el-alert__title,
.el-notification__title,
.el-message-box__title,
.el-drawer__title,
.el-tooltip__trigger,
.el-popover__title {
  font-size: var(--el-font-size-base) !important;
}

/* 添加过渡效果 */
* {
  transition: font-size 0.3s ease;
}

/* 应用字体大小到所有文本元素 */
body {
  font-size: var(--el-font-size-base);
}

/* 全局应用字体大小 */
.el-container {
  font-size: var(--el-font-size-base);
}

/* Element Plus 组件字体大小 */
.el-button,
.el-input,
.el-select,
.el-menu-item,
.el-sub-menu__title,
.el-dropdown-menu__item,
.el-form-item__label,
.el-form-item__content,
.el-table,
.el-dialog__title,
.el-card__header,
.el-tabs__item,
.el-breadcrumb__item,
.el-radio__label,
.el-checkbox__label,
.el-tag,
.el-pagination,
.el-alert__title,
.el-notification__title,
.el-message-box__title,
.el-drawer__title,
.el-tooltip__trigger,
.el-popover__title {
  font-size: var(--el-font-size-base) !important;
}

/* 标题和特殊文本 */
h1 { font-size: calc(var(--el-font-size-base) * 2); }
h2 { font-size: calc(var(--el-font-size-base) * 1.75); }
h3 { font-size: calc(var(--el-font-size-base) * 1.5); }
h4 { font-size: calc(var(--el-font-size-base) * 1.25); }
h5 { font-size: calc(var(--el-font-size-base) * 1.1); }
h6 { font-size: var(--el-font-size-base); }

/* 表格内容 */
.el-table th,
.el-table td {
  font-size: var(--el-font-size-base);
}

/* 表单项 */
.el-form {
  .el-form-item__label,
  .el-form-item__content,
  .el-input__inner,
  .el-select__input,
  .el-textarea__inner {
    font-size: var(--el-font-size-base);
  }
}

/* 菜单项 */
.el-menu {
  .el-menu-item,
  .el-sub-menu__title {
    font-size: var(--el-font-size-base);
  }
}

/* 确保所有组件使用手写体字体 */
.el-button,
.el-input,
.el-select,
.el-menu-item,
.el-sub-menu__title,
.el-dropdown-menu__item,
.el-form-item__label,
.el-form-item__content,
.el-table,
.el-dialog__title,
.el-card__header,
.el-tabs__item,
.el-breadcrumb__item,
.el-radio__label,
.el-checkbox__label,
.el-tag,
.el-pagination,
.el-alert__title,
.el-notification__title,
.el-message-box__title,
.el-drawer__title,
.el-tooltip__trigger,
.el-popover__title {
  font-family: 'Handwriting', sans-serif !important;
}

</style>
