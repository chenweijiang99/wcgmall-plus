<template>
    <svg :class="svgClass" :width="size" :height="size" aria-hidden="true" :style="{marginRight:`${mr}px`}">
      <use :xlink:href="iconName"/>
    </svg>
  </template>
  
  <script setup>
  import { defineProps, computed } from "vue";
  
  const props = defineProps({
    name: {
      type: String,
      required: true
    },
    color: {
      type: String,
      default: ''
    },
    mr:{
      type:Number,
      default:0
    },
    size: {
        type: [Number, String],
        default: 20
    }
  })
  
  const iconName = computed(()=>`#icon-${props.name}`);
  const mr = computed(()=>`#icon-${props.mr}`);
  const svgClass = computed(()=> {
    if (props.name) {
      return `svg-icon icon-${props.name}`
    }
    return 'svg-icon'
  });
  </script>
  
  <style lang='scss'>
  .svg-icon {
    fill: currentColor;
    vertical-align: middle;
  }
  </style>
  
  