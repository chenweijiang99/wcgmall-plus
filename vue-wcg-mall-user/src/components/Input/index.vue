<template>
  <div class="my-input-wrapper">
    <el-input
      ref="inputRef"
      v-bind="$attrs"
      :model-value="modelValue"
      @update:modelValue="handleInput"
    >
      <!-- 传递所有插槽 -->
      <template v-for="(_, slotName) in $slots" #[slotName]>
        <slot :name="slotName"></slot>
      </template>
    </el-input>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 定义组件名称
defineOptions({
  name: 'MyInput'
})

// 定义组件属性
const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  }
})

// 定义组件事件
const emit = defineEmits(['update:modelValue'])

// 获取组件实例引用
const inputRef = ref(null)

// 处理输入事件
const handleInput = (value) => {
  emit('update:modelValue', value)
}

// 暴露组件实例方法
defineExpose({
  focus: () => inputRef.value?.focus(),
  blur: () => inputRef.value?.blur(),
  select: () => inputRef.value?.select()
})
</script>

<style scoped>
.my-input-wrapper {
  width: 100%;
}

/* 输入框样式 */
:deep(.el-input) {
  width: 100% !important;
  height: 40px;
  border-radius: 2rem !important;
  overflow: hidden;
  --el-input-border-radius: 2rem !important;
}

:deep(.el-input__wrapper) {
  --el-input-focus-border-color: #474747 !important;
  background-color: #f6f7fb !important;
}

/* 输入框autofill效果 */
:deep(.el-input__inner:-webkit-autofill),
:deep(.el-input__inner:-webkit-autofill:hover),
:deep(.el-input__inner:-webkit-autofill:focus),
:deep(.el-input__inner:-webkit-autofill:active) {
  -webkit-box-shadow: 0 0 0 30px #f8f8f8 inset;
  box-shadow: 0 0 0 30px #f8f8f8 inset;
  -webkit-text-fill-color: #444343;
  transition: background-color 5000s ease-in-out 0s;
}

.my-input__error-message {
  color: #f56c6c;
  font-size: 12px;
  line-height: 1;
  padding-top: 4px;
}
</style>