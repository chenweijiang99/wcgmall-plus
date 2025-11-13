<template>
  <div
    class="slide-verify"
    :style="{ width: canvasWidth + 'px' }"
    onselectstart="return false;"
  >
    <!-- 图片加载遮蔽罩 -->
    <div
      :class="{ 'img-loading': isLoading }"
      :style="{ height: canvasHeight + 'px' }"
      v-if="isLoading"
    />
    <!-- 认证成功后的文字提示 -->
    <div
      class="success-hint"
      :style="{ height: canvasHeight + 'px' }"
      v-if="verifySuccess"
    >
      {{ successHint }}
    </div>
    <!--刷新按钮-->
    <div class="refresh-icon" @click="refresh" />
    <!--前端生成-->
    <template v-if="isFrontCheck">
      <!--验证图片-->
      <canvas
        ref="canvas"
        class="slide-canvas"
        :width="canvasWidth"
        :height="canvasHeight"
      />
      <!--阻塞块-->
      <canvas
        ref="block"
        class="slide-block"
        :width="canvasWidth"
        :height="canvasHeight"
      />
    </template>
    <!--后端生成-->
    <template v-else>
      <!--验证图片-->
      <img
        ref="canvasRef"
        class="slide-canvas"
        :width="canvasWidth"
        :height="canvasHeight"
      />
      <!--阻塞块-->
      <img
        ref="blockRef"
        :class="['slide-block', { 'verify-fail': verifyFail }]"
      />
    </template>
    <!-- 滑动条 -->
    <div
      class="slider"
      :class="{
        'verify-active': verifyActive,
        'verify-success': verifySuccess,
        'verify-fail': verifyFail,
      }"
    >
      <!--滑块-->
      <div class="slider-box" :style="{ width: sliderBoxWidth }">
        <!-- 按钮 -->
        <div
          class="slider-button"
          id="slider-button"
          :style="{ left: sliderButtonLeft }"
        >
          <!-- 按钮图标 -->
          <div class="slider-button-icon">
            <el-icon :size="30" style="position: absolute; top: -10px; left: -10px;">
              <ArrowRight />
            </el-icon>
          </div>
        </div>
      </div>
      <!--滑动条提示文字-->
      <span class="slider-hint">{{ sliderHint }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCaptchaApi } from "@/api/system/auth"
import { ArrowRight } from '@element-plus/icons-vue'

// Props 定义
const props = withDefaults(defineProps<{
  blockLength?: number
  blockRadius?: number 
  canvasWidth?: number
  canvasHeight?: number
  sliderHint?: string
  accuracy?: number
  imageList?: any[]
}>(), {
  blockLength: 42,
  blockRadius: 10,
  canvasWidth: 320,
  canvasHeight: 155,
  sliderHint: "向右滑动",
  accuracy: 3,
  imageList: () => []
})

// Emits 定义
const emit = defineEmits(['success', 'fail', 'again'])

// 辅助函数
const sum = (x: number, y: number) => x + y
const square = (x: number) => x * x

// 响应式状态
const isFrontCheck = ref(false)
const verifyActive = ref(false)
const verifySuccess = ref(false)
const verifyFail = ref(false)
const blockObj = ref<HTMLImageElement>()
const canvasCtx = ref<CanvasRenderingContext2D | null>(null)
const blockCtx = ref<CanvasRenderingContext2D | null>(null)
const blockWidth = ref(props.blockLength * 2)
const blockX = ref<number>()
const blockY = ref<number>()
const image = ref<HTMLImageElement>()
const originX = ref<number>()
const originY = ref<number>()
const dragDistanceList = ref<number[]>([])
const sliderBoxWidth = ref<string>('0px')
const sliderButtonLeft = ref<string>('0px')
const isMouseDown = ref(false)
const isLoading = ref(true)
const timestamp = ref<number | null>(null)
const successHint = ref('')
const nonceStr = ref<string>()

// 模板引用
const blockRef = ref<HTMLImageElement>()
const canvasRef = ref<HTMLImageElement>()

// 方法定义
const initDom = () => {
  blockObj.value = blockRef.value!
  if (isFrontCheck.value) {
    canvasCtx.value = canvasRef.value?.getContext('2d') || null
    blockCtx.value = blockObj.value?.getContext('2d') || null
    initImage()
  } else {
    getCaptcha()
  }
}

const getCaptcha = async () => {
  try {
    const response = await getCaptchaApi()
    const data = response.data
    nonceStr.value = data.nonceStr
    if (blockRef.value) {
      blockRef.value.src = data.blockSrc
      blockRef.value.style.top = data.blockY + 'px'
    }
    if (canvasRef.value) {
      canvasRef.value.src = data.canvasSrc
    }
  } finally {
    isLoading.value = false
  }
}

const initImage = () => {
  const image = createImage(() => {
    drawBlock()
    let {
      canvasWidth,
      canvasHeight,
    } = props
    
    if (canvasCtx.value) {
      canvasCtx.value.drawImage(image, 0, 0, canvasWidth, canvasHeight)
    }
    if (blockCtx.value) {
      blockCtx.value.drawImage(image, 0, 0, canvasWidth, canvasHeight)
    }
    
    // 将抠图防止最左边位置
    if (blockX.value && blockY.value && blockCtx.value) {
      let yAxle = blockY.value - props.blockRadius * 2
      let ImageData = blockCtx.value.getImageData(
        blockX.value,
        yAxle,
        blockWidth.value,
        blockWidth.value
      )
      
      if (blockRef.value) {
        blockRef.value.width = blockWidth.value
      }
      
      blockCtx.value.putImageData(ImageData, 0, yAxle)
    }
    
    // 图片加载完关闭遮蔽罩
    isLoading.value = false
    // 前端校验设置特殊值
    nonceStr.value = "loyer"
  })
  if (image) {
    image.value = image
  }
}

const createImage = (onload: () => void) => {
  const image = document.createElement("img")
  image.crossOrigin = "Anonymous"
  image.onload = onload
  image.onerror = () => {
    //image.src = require('../../assets/images/bgImg.jpg');
  }
  image.src = getImageSrc()
  return image
}

const getImageSrc = () => {
  const len = props.imageList.length
  return len > 0
    ? props.imageList[getNonceByRange(0, len)]
    : `https://loyer.wang/view/ftp/wallpaper/${getNonceByRange(
        1,
        1000
      )}.jpg`
}

const getNonceByRange = (start: number, end: number) => {
  return Math.round(Math.random() * (end - start) + start)
}

const drawBlock = () => {
  blockX.value = getNonceByRange(
    blockWidth.value + 10,
    props.canvasWidth - (blockWidth.value + 10)
  )
  blockY.value = getNonceByRange(
    10 + props.blockRadius * 2,
    props.canvasHeight - (blockWidth.value + 10)
  )
  draw(canvasCtx.value, "fill")
  draw(blockCtx.value, "clip")
}

const draw = (ctx: CanvasRenderingContext2D | null, operation: 'fill' | 'clip') => {
  if (!ctx) return
  
  const PI = Math.PI
  const x = blockX.value || 0
  const y = blockY.value || 0
  const l = props.blockLength
  const r = props.blockRadius
  
  // 绘制
  ctx.beginPath()
  ctx.moveTo(x, y)
  ctx.arc(x + l / 2, y - r + 2, r, 0.72 * PI, 2.26 * PI)
  ctx.lineTo(x + l, y)
  ctx.arc(x + l + r - 2, y + l / 2, r, 1.21 * PI, 2.78 * PI)
  ctx.lineTo(x + l, y + l)
  ctx.lineTo(x, y + l)
  ctx.arc(x + r - 2, y + l / 2, r + 0.4, 2.76 * PI, 1.24 * PI, true)
  ctx.lineTo(x, y)
  
  // 修饰
  ctx.lineWidth = 2
  ctx.fillStyle = "rgba(255, 255, 255, 0.9)"
  ctx.strokeStyle = "rgba(255, 255, 255, 0.9)"
  ctx.stroke()
  ctx[operation]()
  ctx.globalCompositeOperation = "destination-over"
}

const bindEvents = () => {
  // 监听鼠标按下事件
  document
    .getElementById("slider-button")
    .addEventListener("mousedown", (event) => {
      startEvent(event.clientX, event.clientY)
    })
  // 监听鼠标移动事件
  document.addEventListener("mousemove", (event) => {
    moveEvent(event.clientX, event.clientY)
  })
  // 监听鼠标离开事件
  document.addEventListener("mouseup", (event) => {
    endEvent(event.clientX)
  })
  // 监听触摸开始事件
  document
    .getElementById("slider-button")
    .addEventListener("touchstart", (event) => {
      startEvent(
        event.changedTouches[0].pageX,
        event.changedTouches[0].pageY
      )
    })
  // 监听触摸滑动事件
  document.addEventListener("touchmove", (event) => {
    moveEvent(
      event.changedTouches[0].pageX,
      event.changedTouches[0].pageY
    )
  })
  // 监听触摸离开事件
  document.addEventListener("touchend", (event) => {
    endEvent(event.changedTouches[0].pageX)
  })
}

const checkImgSrc = () => {
  if (isFrontCheck.value) {
    return true
  }
  return !!canvasRef.value.src
}

const startEvent = (clientX: number, clientY: number) => {
  if (!checkImgSrc() || isLoading.value || verifySuccess.value) {
    return
  }
  originX.value = clientX
  originY.value = clientY
  isMouseDown.value = true
  timestamp.value = +new Date()
}

const moveEvent = (clientX: number, clientY: number) => {
  if (!isMouseDown.value || !originX.value || !originY.value) {
    return false
  }
  const moveX = clientX - originX.value
  const moveY = clientY - originY.value
  if (moveX < 0 || moveX + 40 >= props.canvasWidth) {
    return false
  }
  sliderButtonLeft.value = `${moveX}px`
  let blockLeft = ((props.canvasWidth - 40 - 20) / (props.canvasWidth - 40)) * moveX
  if (blockRef.value) {
    blockRef.value.style.left = `${blockLeft}px`
  }
  verifyActive.value = true
  sliderBoxWidth.value = `${moveX}px`
  dragDistanceList.value.push(moveY)
}

const endEvent = (originX: number) => {
  if (!isMouseDown.value) {
    return false
  }
  isMouseDown.value = false
  if (originX === originX.value) {
    return false
  }
  // 开始校验
  isLoading.value = true
  // 校验结束
  verifyActive.value = false
  // 滑动时长
  timestamp.value = +new Date() - timestamp.value
  // 移动距离
  const moveLength = parseInt(blockRef.value?.style.left || '0')
  // 限制操作时长10S，超出判断失败
  if (timestamp.value > 10000) {
    verifyFailEvent()
  }
  // 人为操作判定
  else if (!turingTest()) {
    verifyFail.value = true
    emit("again")
  }
  // 是否前端校验
  else if (isFrontCheck.value) {
    const accuracy =
      props.accuracy <= 1 ? 1 : props.accuracy > 10 ? 10 : props.accuracy // 容错精度值
    const spliced = Math.abs(moveLength - blockX.value) <= accuracy // 判断是否重合
    if (!spliced) {
      verifyFailEvent()
    } else {
      // 设置特殊值，后台特殊处理，直接验证通过
      emit("success", { nonceStr: nonceStr.value, value: moveLength })
    }
  } else {
    emit("success", { nonceStr: nonceStr.value, value: moveLength })
  }
}

const turingTest = () => {
  const arr = dragDistanceList.value // 拖动距离数组
  const average = arr.reduce(sum) / arr.length // 平均值
  const deviations = arr.map((x) => x - average) // 偏离值
  const stdDev = Math.sqrt(deviations.map(square).reduce(sum) / arr.length) // 标准偏差
  return average !== stdDev // 判断是否人为操作
}

const verifySuccessEvent = () => {
  isLoading.value = false
  verifySuccess.value = true
  const elapsedTime = (timestamp.value / 1000).toFixed(1)
  if (elapsedTime < 1) {
    successHint.value = `仅仅${elapsedTime}S，你的速度快如闪电`
  } else if (elapsedTime < 2) {
    successHint.value = `只用了${elapsedTime}S，这速度简直完美`
  } else {
    successHint.value = `耗时${elapsedTime}S，争取下次再快一点`
  }
}

const verifyFailEvent = (msg?: string) => {
  verifyFail.value = true
  emit("fail", msg)
  refresh()
}

const refresh = () => {
  setTimeout(() => {
    verifyFail.value = false
  }, 500)
  
  isLoading.value = true
  verifyActive.value = false
  verifySuccess.value = false
  
  if (blockRef.value) {
    blockRef.value.style.left = '0px'
  }
  
  sliderBoxWidth.value = '0px'
  sliderButtonLeft.value = '0px'
  
  if (isFrontCheck.value) {
    const { canvasWidth, canvasHeight } = props
    if (canvasCtx.value) {
      canvasCtx.value.clearRect(0, 0, canvasWidth, canvasHeight)
    }
    if (blockCtx.value) {
      blockCtx.value.clearRect(0, 0, canvasWidth, canvasHeight)
    }
    if (blockRef.value) {
      blockRef.value.width = canvasWidth
    }
    if (image.value) {
      image.value.src = getImageSrc()
    }
  } else {
    getCaptcha()
  }
}

// 生命周期钩子
onMounted(() => {
  initDom()
  bindEvents()
})


// 暴露方法给父组件
defineExpose({
  refresh,
  verifySuccessEvent,
  verifyFailEvent
})
</script>

<style scoped>
.slide-verify {
  position: relative;
}

/*图片加载样式*/
.img-loading {
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  bottom: 0;
  z-index: 999;
  animation: loading 1.5s infinite;
  background-repeat: no-repeat;
  background-position: center center;
  background-size: 100px;
  background-color: #737c8e;
  border-radius: 5px;
}

@keyframes loading {
  0% {
    opacity: 0.7;
  }

  100% {
    opacity: 9;
  }
}

/*认证成功后的文字提示*/
.success-hint {
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.8);
  color: #2cd000;
  font-size: large;
}

/*刷新按钮*/
.refresh-icon {
  position: absolute;
  right: 0;
  top: 0;
  width: 35px;
  height: 35px;
  cursor: pointer;
  background-size: 35px 470px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='white'%3E%3Cpath d='M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: center;
  border-radius: 5px;
  transition: background-color 0.3s;
}

.refresh-icon:hover {
  background-color: rgba(0, 0, 0, 0.1);
}

/*验证图片*/
.slide-canvas {
  border-radius: 5px;
}

/*阻塞块*/
.slide-block {
  position: absolute;
  left: 0;
  top: 0;
}

/*校验失败时的阻塞块样式*/
.slide-block.verify-fail {
  transition: left 0.5s linear;
}

/*滑动条*/
.slider {
  position: relative;
  text-align: center;
  width: 100%;
  height: 40px;
  line-height: 40px;
  margin-top: 15px;
  background: #f7f9fa;
  color: #45494c;
  border: 1px solid #e4e7eb;
  border-radius: 5px;
}

/*滑动盒子*/
.slider-box {
  position: absolute;
  left: 0;
  top: 0;
  height: 40px;
  border: 0 solid #1991fa;
  background: #d1e9fe;
  border-radius: 5px;
}

/*滑动按钮*/
.slider-button {
  position: absolute;
  top: 0;
  left: 0;
  width: 40px;
  height: 40px;
  background: #fff;
  box-shadow: 0 0 3px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  transition: background 0.2s linear;
  border-radius: 5px;
}

/*鼠标悬浮时的按钮样式*/
.slider-button:hover {
  background: #1991fa;
}

.slider-button:hover i {
  color: #fff;
}

/*鼠标悬浮时的按钮图标样式*/
.slider-button:hover .slider-button-icon {
  background-position: 0 -13px;
}

/*滑动按钮图标*/
.slider-button-icon {
  position: absolute;
  top: 15px;
  left: 13px;
  width: 15px;
  height: 13px;
  background-size: 35px 470px;
}

/*校验时的按钮样式*/
.verify-active .slider-button {
  height: 38px;
  top: -1px;
  border: 1px solid #1991fa;
}

/*校验时的滑动箱样式*/
.verify-active .slider-box {
  height: 38px;
  border-width: 1px;
}

/*校验成功时的滑动箱样式*/
.verify-success .slider-box {
  height: 38px;
  border: 1px solid #52ccba;
  background-color: #d2f4ef;
}

/*校验成功时的按钮样式*/
.verify-success .slider-button {
  height: 38px;
  top: -1px;
  border: 1px solid #52ccba;
  background-color: #52ccba !important;
}

/*校验成功时的按钮图标样式*/
.verify-success .slider-button-icon {
  background-position: 0 0 !important;
}

/*校验失败时的滑动箱样式*/
.verify-fail .slider-box {
  height: 38px;
  border: 1px solid #f57a7a;
  background-color: #fce1e1;
  transition: width 0.5s linear;
}

/*校验失败时的按钮样式*/
.verify-fail .slider-button {
  height: 38px;
  top: -1px;
  border: 1px solid #f57a7a;
  background-color: #f57a7a !important;
  transition: left 0.5s linear;
}

/*校验失败时的按钮图标样式*/
.verify-fail .slider-button-icon {
  top: 14px;
  background-position: 0 -82px !important;
}

/*校验状态下的提示文字隐藏*/
.verify-active .slider-hint,
.verify-success .slider-hint,
.verify-fail .slider-hint {
  display: none;
}
</style>