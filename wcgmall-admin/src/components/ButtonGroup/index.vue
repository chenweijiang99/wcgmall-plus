<template>
  <div class="left-button-group">
    <slot></slot>
  </div>
</template>

<style lang="scss" scoped>
.left-button-group {
  display: flex;
  align-items: center;
  
  :deep(.el-button + .el-button) {
    margin-left: 8px;
  }
}
</style> 