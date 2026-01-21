<template>
  <div class="floor-plan-locator">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button-group>
          <el-button size="small" @click="resetView">
            <el-icon><RefreshLeft /></el-icon>
            重置视图
          </el-button>
          <el-button size="small" @click="toggleGrid">
            <el-icon><Grid /></el-icon>
            网格: {{ showGrid ? '开' : '关' }}
          </el-button>
          <el-button size="small" @click="toggleRuler">
            <el-icon><Operation /></el-icon>
            标尺: {{ showRuler ? '开' : '关' }}
          </el-button>
        </el-button-group>
        
        <el-divider direction="vertical" />
        
        <el-button-group>
          <el-button size="small" @click="zoomIn">
            <el-icon><ZoomIn /></el-icon>
          </el-button>
          <el-button size="small" @click="zoomOut">
            <el-icon><ZoomOut /></el-icon>
          </el-button>
        </el-button-group>
      </div>

      <div class="toolbar-right">
        <el-tag v-if="hoveredPoint" type="info">
          <el-icon><Location /></el-icon>
          ({{ hoveredPoint.x }}m, {{ hoveredPoint.y }}m)
        </el-tag>
        <el-tag v-if="selectedPoint" type="success">
          <el-icon><Select /></el-icon>
          已选: ({{ selectedPoint.x }}m, {{ selectedPoint.y }}m)
        </el-tag>
      </div>
    </div>

    <!-- Canvas容器 -->
    <div ref="canvasContainer" class="canvas-container" v-loading="loading">
      <canvas
        ref="canvas"
        @click="handleClick"
        @mousemove="handleMouseMove"
        @mouseleave="handleMouseLeave"
        @wheel.prevent="handleWheel"
      ></canvas>

      <!-- 坐标轴标签 -->
      <div class="axis-labels">
        <div class="axis-label x-axis">
          <el-icon><Right /></el-icon>
          东 (X轴)
        </div>
        <div class="axis-label y-axis">
          <el-icon><Top /></el-icon>
          北 (Y轴)
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!floorPlanImage && !loading" class="empty-state">
        <el-empty description="暂无平面图">
          <el-button type="primary" size="small" @click="$emit('upload-plan')">
            上传平面图
          </el-button>
        </el-empty>
      </div>
    </div>

    <!-- 坐标信息面板 -->
    <div class="coordinate-panel">
      <el-form :model="coordinates" label-width="90px" size="small">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="X坐标(米)">
              <el-input-number
                v-model="coordinates.x"
                :min="0"
                :max="buildingWidth"
                :precision="2"
                :step="0.1"
                @change="handleCoordinateChange"
                class="w-full"
              />
              <span class="coordinate-hint">东西方向</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="Y坐标(米)">
              <el-input-number
                v-model="coordinates.y"
                :min="0"
                :max="buildingHeight"
                :precision="2"
                :step="0.1"
                @change="handleCoordinateChange"
                class="w-full"
              />
              <span class="coordinate-hint">南北方向</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="Z坐标(米)">
              <el-input-number
                v-model="coordinates.z"
                :min="0"
                :max="10"
                :precision="2"
                :step="0.1"
                class="w-full"
              />
              <span class="coordinate-hint">安装高度</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import {
  RefreshLeft,
  Grid,
  Operation,
  ZoomIn,
  ZoomOut,
  Location,
  Select,
  Right,
  Top
} from '@element-plus/icons-vue'
import { CoordinateConverter, type FloorPlanConfig, type RealCoordinate } from '@/utils/coordinate'

// Props
interface Props {
  /** 楼层ID */
  floorId?: number
  /** 平面图URL */
  imageUrl?: string
  /** 建筑宽度（米） */
  buildingWidth?: number
  /** 建筑高度（米） */
  buildingHeight?: number
  /** 初始坐标 */
  initialX?: number
  initialY?: number
  initialZ?: number
  /** 已有设备列表 */
  devices?: Array<{
    id: number
    name: string
    x: number
    y: number
    z: number
  }>
}

const props = withDefaults(defineProps<Props>(), {
  buildingWidth: 50,
  buildingHeight: 30,
  initialZ: 0
})

const emit = defineEmits<{
  'coordinate-change': [{ x: number; y: number; z: number }]
  'upload-plan': []
}>()

// 状态
const canvas = ref<HTMLCanvasElement>()
const canvasContainer = ref<HTMLDivElement>()
const loading = ref(false)
const showGrid = ref(true)
const showRuler = ref(true)

// 坐标数据
const coordinates = reactive({
  x: props.initialX || 0,
  y: props.initialY || 0,
  z: props.initialZ || 0
})

const selectedPoint = ref<RealCoordinate | null>(null)
const hoveredPoint = ref<RealCoordinate | null>(null)

// 平面图
const floorPlanImage = ref<HTMLImageElement>()
const imageLoaded = ref(false)

// 缩放和偏移
const scale = ref(1)
const panOffset = reactive({ x: 0, y: 0 })

// 坐标转换器
let converter: CoordinateConverter | null = null

// Canvas配置
const CANVAS_WIDTH = 800
const CANVAS_HEIGHT = 600
const GRID_SIZE = 5 // 网格间距（米）
const PADDING = 40

/**
 * 初始化Canvas
 */
const initCanvas = () => {
  if (!canvas.value) return

  canvas.value.width = CANVAS_WIDTH
  canvas.value.height = CANVAS_HEIGHT

  // 创建坐标转换器
  const autoFit = CoordinateConverter.calculateAutoFit(
    props.buildingWidth,
    props.buildingHeight,
    CANVAS_WIDTH,
    CANVAS_HEIGHT,
    PADDING
  )

  const config: FloorPlanConfig = {
    floorId: props.floorId || 0,
    imageUrl: props.imageUrl,
    buildingWidth: props.buildingWidth,
    buildingHeight: props.buildingHeight,
    pixelsPerMeter: autoFit.pixelsPerMeter * scale.value,
    canvasWidth: CANVAS_WIDTH,
    canvasHeight: CANVAS_HEIGHT,
    offsetX: autoFit.offsetX + panOffset.x,
    offsetY: autoFit.offsetY + panOffset.y
  }

  converter = new CoordinateConverter(config)

  draw()
}

/**
 * 加载平面图
 */
const loadFloorPlan = async () => {
  if (!props.imageUrl) return

  loading.value = true
  try {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    
    await new Promise((resolve, reject) => {
      img.onload = resolve
      img.onerror = reject
      img.src = props.imageUrl!
    })

    floorPlanImage.value = img
    imageLoaded.value = true
    
    await nextTick()
    draw()
  } catch (error) {
    console.error('加载平面图失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 绘制整个场景
 */
const draw = () => {
  const ctx = canvas.value?.getContext('2d')
  if (!ctx || !converter) return

  // 清空画布
  ctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)

  // 绘制背景
  ctx.fillStyle = '#f5f5f5'
  ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)

  // 绘制平面图
  if (floorPlanImage.value && imageLoaded.value) {
    drawFloorPlan(ctx)
  }

  // 绘制网格
  if (showGrid.value) {
    drawGrid(ctx)
  }

  // 绘制标尺
  if (showRuler.value) {
    drawRuler(ctx)
  }

  // 绘制建筑边界
  drawBuildingBoundary(ctx)

  // 绘制已有设备
  if (props.devices) {
    drawDevices(ctx)
  }

  // 绘制选中点
  if (selectedPoint.value) {
    drawMarker(ctx, selectedPoint.value, '#ff4d4f', 8)
  }

  // 绘制悬停点
  if (hoveredPoint.value) {
    drawMarker(ctx, hoveredPoint.value, '#1890ff', 6)
  }
}

/**
 * 绘制平面图
 */
const drawFloorPlan = (ctx: CanvasRenderingContext2D) => {
  if (!floorPlanImage.value || !converter) return

  const topLeft = converter.realToPixel(0, props.buildingHeight)
  const bottomRight = converter.realToPixel(props.buildingWidth, 0)

  const width = bottomRight.x - topLeft.x
  const height = bottomRight.y - topLeft.y

  ctx.globalAlpha = 0.7
  ctx.drawImage(floorPlanImage.value, topLeft.x, topLeft.y, width, height)
  ctx.globalAlpha = 1.0
}

/**
 * 绘制网格
 */
const drawGrid = (ctx: CanvasRenderingContext2D) => {
  if (!converter) return

  ctx.strokeStyle = '#d9d9d9'
  ctx.lineWidth = 0.5

  // 垂直网格线
  for (let x = 0; x <= props.buildingWidth; x += GRID_SIZE) {
    const start = converter.realToPixel(x, 0)
    const end = converter.realToPixel(x, props.buildingHeight)
    
    ctx.beginPath()
    ctx.moveTo(start.x, start.y)
    ctx.lineTo(end.x, end.y)
    ctx.stroke()
  }

  // 水平网格线
  for (let y = 0; y <= props.buildingHeight; y += GRID_SIZE) {
    const start = converter.realToPixel(0, y)
    const end = converter.realToPixel(props.buildingWidth, y)
    
    ctx.beginPath()
    ctx.moveTo(start.x, start.y)
    ctx.lineTo(end.x, end.y)
    ctx.stroke()
  }
}

/**
 * 绘制标尺
 */
const drawRuler = (ctx: CanvasRenderingContext2D) => {
  if (!converter) return

  ctx.fillStyle = '#595959'
  ctx.font = '10px Arial'
  ctx.textAlign = 'center'

  // X轴标尺
  for (let x = 0; x <= props.buildingWidth; x += GRID_SIZE) {
    const point = converter.realToPixel(x, 0)
    ctx.fillText(`${x}m`, point.x, point.y + 15)
  }

  // Y轴标尺
  ctx.textAlign = 'right'
  for (let y = 0; y <= props.buildingHeight; y += GRID_SIZE) {
    const point = converter.realToPixel(0, y)
    ctx.fillText(`${y}m`, point.x - 5, point.y + 4)
  }
}

/**
 * 绘制建筑边界
 */
const drawBuildingBoundary = (ctx: CanvasRenderingContext2D) => {
  if (!converter) return

  const topLeft = converter.realToPixel(0, props.buildingHeight)
  const bottomRight = converter.realToPixel(props.buildingWidth, 0)

  ctx.strokeStyle = '#1890ff'
  ctx.lineWidth = 2
  ctx.strokeRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y)
}

/**
 * 绘制设备标记
 */
const drawDevices = (ctx: CanvasRenderingContext2D) => {
  if (!converter || !props.devices) return

  props.devices.forEach((device) => {
    const point = converter!.realToPixel(device.x, device.y)
    
    // 绘制设备图标
    ctx.fillStyle = '#52c41a'
    ctx.beginPath()
    ctx.arc(point.x, point.y, 5, 0, 2 * Math.PI)
    ctx.fill()

    // 绘制设备名称
    ctx.fillStyle = '#000'
    ctx.font = '11px Arial'
    ctx.textAlign = 'center'
    ctx.fillText(device.name, point.x, point.y - 10)
  })
}

/**
 * 绘制标记点
 */
const drawMarker = (
  ctx: CanvasRenderingContext2D,
  realCoord: RealCoordinate,
  color: string,
  radius: number
) => {
  if (!converter) return

  const point = converter.realToPixel(realCoord.x, realCoord.y)

  // 绘制外圈
  ctx.strokeStyle = color
  ctx.lineWidth = 2
  ctx.beginPath()
  ctx.arc(point.x, point.y, radius + 4, 0, 2 * Math.PI)
  ctx.stroke()

  // 绘制内圆
  ctx.fillStyle = color
  ctx.beginPath()
  ctx.arc(point.x, point.y, radius, 0, 2 * Math.PI)
  ctx.fill()

  // 绘制十字
  ctx.strokeStyle = '#fff'
  ctx.lineWidth = 2
  ctx.beginPath()
  ctx.moveTo(point.x - 4, point.y)
  ctx.lineTo(point.x + 4, point.y)
  ctx.moveTo(point.x, point.y - 4)
  ctx.lineTo(point.x, point.y + 4)
  ctx.stroke()
}

/**
 * 处理点击事件
 */
const handleClick = (event: MouseEvent) => {
  if (!converter) return

  const rect = canvas.value!.getBoundingClientRect()
  const pixelX = event.clientX - rect.left
  const pixelY = event.clientY - rect.top

  const realCoord = converter.pixelToReal(pixelX, pixelY)

  // 检查是否在建筑范围内
  if (converter.isInBounds(realCoord.x, realCoord.y)) {
    coordinates.x = realCoord.x
    coordinates.y = realCoord.y
    selectedPoint.value = { x: realCoord.x, y: realCoord.y }

    emit('coordinate-change', {
      x: coordinates.x,
      y: coordinates.y,
      z: coordinates.z
    })

    draw()
  }
}

/**
 * 处理鼠标移动
 */
const handleMouseMove = (event: MouseEvent) => {
  if (!converter) return

  const rect = canvas.value!.getBoundingClientRect()
  const pixelX = event.clientX - rect.left
  const pixelY = event.clientY - rect.top

  const realCoord = converter.pixelToReal(pixelX, pixelY)

  if (converter.isInBounds(realCoord.x, realCoord.y)) {
    hoveredPoint.value = { x: realCoord.x, y: realCoord.y }
    canvas.value!.style.cursor = 'crosshair'
  } else {
    hoveredPoint.value = null
    canvas.value!.style.cursor = 'default'
  }
}

/**
 * 处理鼠标离开
 */
const handleMouseLeave = () => {
  hoveredPoint.value = null
}

/**
 * 处理滚轮缩放
 */
const handleWheel = (event: WheelEvent) => {
  const delta = event.deltaY > 0 ? 0.9 : 1.1
  scale.value = Math.max(0.5, Math.min(3, scale.value * delta))
  initCanvas()
}

/**
 * 坐标手动输入变化
 */
const handleCoordinateChange = () => {
  selectedPoint.value = { x: coordinates.x, y: coordinates.y }
  
  emit('coordinate-change', {
    x: coordinates.x,
    y: coordinates.y,
    z: coordinates.z
  })

  draw()
}

/**
 * 工具栏操作
 */
const resetView = () => {
  scale.value = 1
  panOffset.x = 0
  panOffset.y = 0
  initCanvas()
}

const toggleGrid = () => {
  showGrid.value = !showGrid.value
  draw()
}

const toggleRuler = () => {
  showRuler.value = !showRuler.value
  draw()
}

const zoomIn = () => {
  scale.value = Math.min(3, scale.value * 1.2)
  initCanvas()
}

const zoomOut = () => {
  scale.value = Math.max(0.5, scale.value / 1.2)
  initCanvas()
}

/**
 * 监听Props变化
 */
watch(() => props.imageUrl, () => {
  loadFloorPlan()
})

watch(() => [props.buildingWidth, props.buildingHeight], () => {
  initCanvas()
})

/**
 * 初始化
 */
onMounted(() => {
  initCanvas()
  if (props.imageUrl) {
    loadFloorPlan()
  }

  // 设置初始坐标
  if (props.initialX !== undefined && props.initialY !== undefined) {
    selectedPoint.value = { x: props.initialX, y: props.initialY }
    draw()
  }
})

defineExpose({
  getCoordinates: () => coordinates,
  setCoordinates: (x: number, y: number, z: number) => {
    coordinates.x = x
    coordinates.y = y
    coordinates.z = z
    selectedPoint.value = { x, y }
    draw()
  }
})
</script>

<style scoped lang="scss">
.floor-plan-locator {
  display: flex;
  flex-direction: column;
  height: 700px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: #fafafa;
    border-bottom: 1px solid #d9d9d9;

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .toolbar-right {
      display: flex;
      gap: 8px;
    }
  }

  .canvas-container {
    flex: 1;
    position: relative;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;

    canvas {
      display: block;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .axis-labels {
      position: absolute;
      pointer-events: none;

      .axis-label {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 4px 8px;
        background: rgba(255, 255, 255, 0.9);
        border-radius: 4px;
        font-size: 12px;
        color: #595959;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }

      .x-axis {
        position: absolute;
        bottom: 20px;
        right: 20px;
      }

      .y-axis {
        position: absolute;
        top: 20px;
        left: 20px;
      }
    }

    .empty-state {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
    }
  }

  .coordinate-panel {
    padding: 16px;
    background: #fafafa;
    border-top: 1px solid #d9d9d9;

    .coordinate-hint {
      display: block;
      margin-top: 4px;
      font-size: 12px;
      color: #8c8c8c;
    }
  }
}
</style>

