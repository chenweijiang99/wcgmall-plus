<template>
  <span>{{ displayValue }}</span>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'

const props = withDefaults(defineProps<{
  startVal?: number
  endVal: number
  duration?: number
  decimals?: number
  autoplay?: boolean
}>(), {
  startVal: 0,
  duration: 2000,
  decimals: 0,
  autoplay: true
})

const displayValue = ref(props.startVal)
let startTime: number | null = null
let timer: number | null = null

const formatNumber = (num: number) => {
  return num.toFixed(props.decimals)
}

const animate = (timestamp: number) => {
  if (!startTime) startTime = timestamp
  const progress = timestamp - startTime
  const currentValue = props.startVal + ((props.endVal - props.startVal) * (progress / props.duration))

  if (progress < props.duration) {
    displayValue.value = parseFloat(formatNumber(currentValue))
    timer = requestAnimationFrame(animate)
  } else {
    displayValue.value = props.endVal
  }
}

const start = () => {
  if (timer) cancelAnimationFrame(timer)
  startTime = null
  timer = requestAnimationFrame(animate)
}

watch(() => props.endVal, () => {
  if (props.autoplay) start()
})

onMounted(() => {
  if (props.autoplay) start()
})
</script> 