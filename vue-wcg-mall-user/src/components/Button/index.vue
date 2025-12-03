<script setup name="MyButton">
import { computed } from 'vue';
import { defineProps, defineEmits } from "vue";

// 定义组件属性
const props = defineProps({
  // 基础属性 - 继承自el-button
  type: {
    type: String,
    default: "primary",
  },
  size: {
    type: String,
    default: "default",
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  icon: {
    type: String,
    default: "",
  },
  circle: {
    type: Boolean,
    default: false,
  },
  round: {
    type: Boolean,
    default: false,
  },
  plain: {
    type: Boolean,
    default: false,
  },
  // 自定义属性
  customColor: {
    type: String,
    default: "",
  },
  shadow: {
    type: Boolean,
    default: false,
  },
});

// 定义组件事件
const emit = defineEmits(["click", "focus", "blur"]);

// 处理点击事件
const handleClick = (event) => {
  emit("click", event);
};

// 处理焦点事件
const handleFocus = (event) => {
  emit("focus", event);
};

// 处理失焦事件
const handleBlur = (event) => {
  emit("blur", event);
};

</script>

<template>
  <el-button
    v-bind="$attrs"
    :type="props.type"
    :size="props.size"
    :disabled="props.disabled"
    :loading="props.loading"
    :icon="props.icon"
    :circle="props.circle"
    :round="props.round"
    :plain="props.plain"
    @click="handleClick"
    @focus="handleFocus"
    @blur="handleBlur"
  >
    <slot></slot>
  </el-button>
</template>

<style scoped>
/* 按钮样式 */
.el-button {
    font-size: 0.85rem;
    padding: 0 1.25rem;
    height: 40px;
    line-height: 40px;
    border-radius: 2rem;
    transition: all 0.3s ease;
    border: 1px solid;
    box-shadow: none;
    width: 100%;
}

.el-button--primary {
    background-color: #444343;
    border-color: #444343;
    color: #ffffff;
}
.el-button--primary:hover,
.el-button--primary:focus {
    color: #444343;
    background-color: #ffffff;
    border-color: #444343;
}
.el-button--primary.is-disabled,
.el-button--primary.is-disabled:hover,
.el-button--primary.is-disabled:focus,
.el-button--primary.is-disabled:active {
    color: #fff;
    background-color: #444343;
    border-color: #444343;
    opacity: 1;
    cursor: not-allowed;
}
.el-button--info {
    background-color: #3f5d85;
    border-color: #3f5d85;
    color: #ffffff;
}
.el-button--info:hover,
.el-button--info:focus {
    color: #3f5d85;
    background-color: #ffffff;
    border-color: #3f5d85;
}
.el-button--info.is-disabled,
.el-button--info.is-disabled:hover,
.el-button--info.is-disabled:focus,
.el-button--info.is-disabled:active {
    color: #fff;
    background-color: #3f5d85;
    border-color: #3f5d85;
    opacity: 1;
    cursor: not-allowed;
}
</style>
