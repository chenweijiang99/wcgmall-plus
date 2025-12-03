<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps({
  // SVG 文件名 (不包含 .svg 后缀)
  name: {
    type: String,
    required: true,
  },
  // 图标颜色，默认跟随字体颜色
  color: {
    type: String,
    default: '',
  },
  // 图标大小，支持 '20px' 或 数字 20
  size: {
    type: [Number, String],
    default: '1em',
  },
});

const symbolId = computed(() => `#icon-${props.name}`);

const iconStyle = computed(() => {
  const s = props.size;
  const sizeValue = typeof s === 'number' ? `${s}px` : s;
  return {
    fontSize: sizeValue,
    color: props.color
  };
});
</script>

<template>
  <svg aria-hidden="true" class="svg-icon" :style="iconStyle">
    <use :href="symbolId" :fill="color" />
  </svg>
</template>

<style scoped>
.svg-icon {
  width: 1em;
  height: 1em;
  position: relative;
  fill: currentColor;
  vertical-align: -0.15em; /* 使图标与文字基线对齐 */
  overflow: hidden;
  display: inline-block;
}
</style>