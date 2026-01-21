<template>
  <div class="enhanced-floor-plan-editor">
    <!-- 紧凑模式：显示坐标输入 + 打开大编辑器按钮 -->
    <div v-if="!dialogVisible" class="compact-mode">
      <el-row :gutter="12">
        <el-col :span="5">
          <el-form-item label="X坐标(米)">
            <el-input-number
              v-model="localCoordinates.x"
              :min="0"
              :max="buildingWidth"
              :precision="2"
              :step="0.1"
              @change="handleCoordinateChange"
              class="w-full"
              size="small"
            />
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="Y坐标(米)">
            <el-input-number
              v-model="localCoordinates.y"
              :min="0"
              :max="buildingHeight"
              :precision="2"
              :step="0.1"
              @change="handleCoordinateChange"
              class="w-full"
              size="small"
            />
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="Z坐标(米)">
            <el-input-number
              v-model="localCoordinates.z"
              :min="0"
              :max="10"
              :precision="2"
              :step="0.1"
              @change="handleCoordinateChange"
              class="w-full"
              size="small"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="设备图标">
            <el-select 
              v-model="localIconConfig.icon" 
              placeholder="选择图标"
              size="small"
              @change="handleIconChange"
            >
              <el-option
                v-for="icon in deviceIcons"
                :key="icon.value"
                :label="icon.label"
                :value="icon.value"
              >
                <div class="flex items-center gap-2">
                  <Icon :icon="icon.value" :size="20" />
                  <span>{{ icon.label }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="3">
          <el-form-item label=" ">
            <el-button type="primary" @click="openDialog" size="small" class="w-full">
              <el-icon><Location /></el-icon>
              平面图选点
            </el-button>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 状态图标预览 -->
      <div class="icon-preview-panel">
        <el-divider content-position="left">图标状态预览</el-divider>
        <div class="icon-preview-grid">
          <div 
            v-for="state in deviceStates" 
            :key="state.value"
            class="icon-preview-item"
          >
            <div class="icon-wrapper" :style="{ backgroundColor: state.bgColor }">
              <Icon 
                :icon="localIconConfig.icon || 'ep:camera'" 
                :size="32" 
                :color="state.color"
              />
            </div>
            <span class="state-label">{{ state.label }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 大弹框编辑器 -->
    <el-dialog
      v-model="dialogVisible"
      title="平面图设备定位编辑器"
      width="90%"
      :close-on-click-modal="false"
      append-to-body
      destroy-on-close
    >
      <div class="editor-container">
        <!-- 左侧：平面图 + 工具栏 -->
        <div class="editor-left">
          <!-- 工具栏 -->
          <div class="toolbar">
            <div class="toolbar-section">
              <span class="toolbar-label">显示图层:</span>
              <el-checkbox-group v-model="visibleLayers" size="small">
                <el-checkbox label="DXF-0层" value="layer0" border />
                <el-checkbox label="网格" value="grid" border />
                <el-checkbox label="标尺" value="ruler" border />
                <el-checkbox label="其他设备" value="devices" border />
              </el-checkbox-group>
            </div>

            <el-divider direction="vertical" />

            <div class="toolbar-section">
              <el-button-group size="small">
                <el-button @click="zoomIn">
                  <el-icon><ZoomIn /></el-icon>
                  放大
                </el-button>
                <el-button @click="zoomOut">
                  <el-icon><ZoomOut /></el-icon>
                  缩小
                </el-button>
                <el-button @click="resetView">
                  <el-icon><RefreshLeft /></el-icon>
                  重置
                </el-button>
              </el-button-group>
            </div>

            <div class="toolbar-section ml-auto">
              <el-tag v-if="hoveredPoint" type="info">
                <el-icon><Aim /></el-icon>
                悬停: ({{ hoveredPoint.x.toFixed(2) }}m, {{ hoveredPoint.y.toFixed(2) }}m)
              </el-tag>
              <el-tag v-if="selectedPoint" type="success">
                <el-icon><LocationFilled /></el-icon>
                已选: ({{ selectedPoint.x.toFixed(2) }}m, {{ selectedPoint.y.toFixed(2) }}m)
              </el-tag>
            </div>
          </div>

          <!-- Canvas 画布 -->
          <div class="canvas-wrapper" v-loading="loading">
            <canvas
              ref="canvas"
              @click="handleCanvasClick"
              @mousemove="handleCanvasMouseMove"
              @mouseleave="handleCanvasMouseLeave"
              @wheel.prevent="handleCanvasWheel"
            ></canvas>

            <!-- 空状态提示 -->
            <div v-if="!floorPlanData && !loading" class="empty-state">
              <el-empty description="暂无平面图数据">
                <el-text type="info" size="small">请先在楼层管理中上传 DXF 平面图文件</el-text>
              </el-empty>
            </div>

            <!-- 坐标轴标签 -->
            <div class="axis-labels">
              <div class="axis-label x-axis">
                <el-icon><Right /></el-icon>
                东 (X轴正方向)
              </div>
              <div class="axis-label y-axis">
                <el-icon><Top /></el-icon>
                北 (Y轴正方向)
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：配置面板 -->
        <div class="editor-right">
          <!-- 坐标设置 -->
          <el-card shadow="never" class="config-card">
            <template #header>
              <div class="card-header">
                <el-icon><LocationInformation /></el-icon>
                <span>设备位置坐标</span>
              </div>
            </template>
            
            <el-form :model="localCoordinates" label-width="100px" size="small">
              <el-form-item label="X坐标 (米)">
                <el-input-number
                  v-model="localCoordinates.x"
                  :min="0"
                  :max="buildingWidth"
                  :precision="2"
                  :step="0.1"
                  @change="handleCoordinateChange"
                  class="w-full"
                />
                <el-text type="info" size="small" class="mt-1 block">
                  东西方向（向东为正）
                </el-text>
              </el-form-item>

              <el-form-item label="Y坐标 (米)">
                <el-input-number
                  v-model="localCoordinates.y"
                  :min="0"
                  :max="buildingHeight"
                  :precision="2"
                  :step="0.1"
                  @change="handleCoordinateChange"
                  class="w-full"
                />
                <el-text type="info" size="small" class="mt-1 block">
                  南北方向（向北为正）
                </el-text>
              </el-form-item>

              <el-form-item label="Z坐标 (米)">
                <el-input-number
                  v-model="localCoordinates.z"
                  :min="0"
                  :max="10"
                  :precision="2"
                  :step="0.1"
                  @change="handleCoordinateChange"
                  class="w-full"
                />
                <el-text type="info" size="small" class="mt-1 block">
                  安装高度（距地面）
                </el-text>
              </el-form-item>

              <el-form-item label="安装位置">
                <el-input
                  v-model="localCoordinates.installLocation"
                  placeholder="如：大堂吊顶、走廊墙面等"
                  @change="handleCoordinateChange"
                />
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 图标设置 -->
          <el-card shadow="never" class="config-card mt-3">
            <template #header>
              <div class="card-header">
                <el-icon><Picture /></el-icon>
                <span>设备图标配置</span>
              </div>
            </template>

            <el-form :model="localIconConfig" label-width="100px" size="small">
              <el-form-item label="图标类型">
                <el-select 
                  v-model="localIconConfig.icon" 
                  placeholder="选择设备图标"
                  @change="handleIconChange"
                  class="w-full"
                >
                  <el-option
                    v-for="icon in deviceIcons"
                    :key="icon.value"
                    :label="icon.label"
                    :value="icon.value"
                  >
                    <div class="flex items-center gap-2">
                      <Icon :icon="icon.value" :size="20" />
                      <span>{{ icon.label }}</span>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>

              <el-form-item label="图标大小">
                <el-radio-group v-model="localIconConfig.size" @change="handleIconChange">
                  <el-radio-button label="small">小</el-radio-button>
                  <el-radio-button label="medium">中</el-radio-button>
                  <el-radio-button label="large">大</el-radio-button>
                </el-radio-group>
              </el-form-item>

              <!-- 状态图标预览 -->
              <el-divider content-position="left">状态预览</el-divider>
              <div class="state-preview-grid">
                <div 
                  v-for="state in deviceStates" 
                  :key="state.value"
                  class="state-preview-item"
                >
                  <div class="state-icon-wrapper" :style="{ backgroundColor: state.bgColor }">
                    <Icon 
                      :icon="localIconConfig.icon" 
                      :size="getIconSize(localIconConfig.size)" 
                      :color="state.color"
                    />
                  </div>
                  <div class="state-info">
                    <span class="state-name">{{ state.label }}</span>
                    <el-tag :type="state.tagType" size="small">{{ state.description }}</el-tag>
                  </div>
                </div>
              </div>
            </el-form>
          </el-card>

          <!-- 快捷操作 -->
          <el-card shadow="never" class="config-card mt-3">
            <template #header>
              <div class="card-header">
                <el-icon><Tools /></el-icon>
                <span>快捷操作</span>
              </div>
            </template>

            <el-space direction="vertical" class="w-full" :size="8">
              <el-button @click="quickSetCorner('topLeft')" size="small" class="w-full">
                <el-icon><TopLeft /></el-icon>
                左上角 (0, {{ buildingHeight }})
              </el-button>
              <el-button @click="quickSetCorner('topRight')" size="small" class="w-full">
                <el-icon><TopRight /></el-icon>
                右上角 ({{ buildingWidth }}, {{ buildingHeight }})
              </el-button>
              <el-button @click="quickSetCorner('center')" size="small" class="w-full">
                <el-icon><Position /></el-icon>
                中心点 ({{ (buildingWidth / 2).toFixed(1) }}, {{ (buildingHeight / 2).toFixed(1) }})
              </el-button>
            </el-space>
          </el-card>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmSelection">
            <el-icon><Check /></el-icon>
            确认定位
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import {
  Location,
  LocationFilled,
  LocationInformation,
  Picture,
  Tools,
  ZoomIn,
  ZoomOut,
  RefreshLeft,
  Aim,
  Right,
  Top,
  TopLeft,
  TopRight,
  Position,
  Check
} from '@element-plus/icons-vue'

/** Props 定义 */
interface Props {
  /** 楼层ID */
  floorId?: number
  /** 平面图DXF数据URL */
  dxfDataUrl?: string
  /** 建筑宽度（米） */
  buildingWidth?: number
  /** 建筑高度（米） */
  buildingHeight?: number
  /** 初始坐标 */
  initialX?: number
  initialY?: number
  initialZ?: number
  /** 初始图标配置 */
  initialIcon?: string
  initialIconSize?: 'small' | 'medium' | 'large'
  /** 已有设备列表 */
  existingDevices?: Array<{
    id: number
    name: string
    x: number
    y: number
    status: 'online' | 'offline' | 'fault'
    icon?: string
  }>
}

const props = withDefaults(defineProps<Props>(), {
  buildingWidth: 50,
  buildingHeight: 30,
  initialZ: 2.5,
  initialIcon: 'ep:camera',
  initialIconSize: 'medium'
})

/** Emits 定义 */
const emit = defineEmits<{
  'update:coordinates': [{ x: number; y: number; z: number; installLocation?: string }]
  'update:icon': [{ icon: string; size: string }]
}>()

/** 状态管理 */
const dialogVisible = ref(false)
const loading = ref(false)
const canvas = ref<HTMLCanvasElement>()

// 本地坐标数据
const localCoordinates = reactive({
  x: props.initialX || 0,
  y: props.initialY || 0,
  z: props.initialZ || 2.5,
  installLocation: ''
})

// 本地图标配置
const localIconConfig = reactive({
  icon: props.initialIcon,
  size: props.initialIconSize
})

// 可见图层
const visibleLayers = ref(['layer0', 'grid', 'ruler', 'devices'])

// 选中点和悬停点
const selectedPoint = ref<{ x: number; y: number } | null>(null)
const hoveredPoint = ref<{ x: number; y: number } | null>(null)

// 缩放和偏移
const scale = ref(1)
const panOffset = reactive({ x: 0, y: 0 })

// DXF 平面图数据（模拟，实际需要解析DXF）
const floorPlanData = ref<any>(null)

/** Canvas 配置 */
const CANVAS_WIDTH = 1200
const CANVAS_HEIGHT = 800
const GRID_SIZE = 5 // 网格间距（米）
const PADDING = 60

/** 设备图标选项 */
const deviceIcons = [
  { label: '摄像头', value: 'ep:camera' },
  { label: '球机', value: 'ep:video-camera' },
  { label: '门禁', value: 'ep:lock' },
  { label: '烟感', value: 'ep:smoking' },
  { label: '温感', value: 'ep:hot-water' },
  { label: '消火栓', value: 'ep:turn-off' },
  { label: '报警器', value: 'ep:bell' },
  { label: '灯光', value: 'ep:light' },
  { label: '传感器', value: 'ep:monitor' },
  { label: '通用设备', value: 'ep:platform' }
]

/** 设备状态配置 */
const deviceStates = [
  { 
    label: '在线', 
    value: 'online', 
    color: '#52c41a', 
    bgColor: '#f6ffed',
    tagType: 'success',
    description: '正常运行'
  },
  { 
    label: '离线', 
    value: 'offline', 
    color: '#8c8c8c', 
    bgColor: '#f5f5f5',
    tagType: 'info',
    description: '设备离线'
  },
  { 
    label: '故障', 
    value: 'fault', 
    color: '#ff4d4f', 
    bgColor: '#fff1f0',
    tagType: 'danger',
    description: '设备异常'
  },
  { 
    label: '告警', 
    value: 'alarm', 
    color: '#fa8c16', 
    bgColor: '#fff7e6',
    tagType: 'warning',
    description: '产生告警'
  }
]

/** 坐标转换器 */
class SimpleCoordinateConverter {
  private pixelsPerMeter: number
  private offsetX: number
  private offsetY: number

  constructor(
    buildingWidth: number,
    buildingHeight: number,
    canvasWidth: number,
    canvasHeight: number,
    padding: number,
    scale: number
  ) {
    const availableWidth = canvasWidth - 2 * padding
    const availableHeight = canvasHeight - 2 * padding

    const scaleX = availableWidth / buildingWidth
    const scaleY = availableHeight / buildingHeight

    this.pixelsPerMeter = Math.min(scaleX, scaleY) * scale
    this.offsetX = padding
    this.offsetY = padding
  }

  realToPixel(x: number, y: number): { x: number; y: number } {
    return {
      x: this.offsetX + x * this.pixelsPerMeter,
      y: this.offsetY + (props.buildingHeight - y) * this.pixelsPerMeter
    }
  }

  pixelToReal(px: number, py: number): { x: number; y: number } {
    return {
      x: (px - this.offsetX) / this.pixelsPerMeter,
      y: props.buildingHeight - (py - this.offsetY) / this.pixelsPerMeter
    }
  }

  isInBounds(x: number, y: number): boolean {
    return x >= 0 && x <= props.buildingWidth && y >= 0 && y <= props.buildingHeight
  }
}

let converter: SimpleCoordinateConverter | null = null

/** 初始化 Canvas */
const initCanvas = () => {
  if (!canvas.value) return

  canvas.value.width = CANVAS_WIDTH
  canvas.value.height = CANVAS_HEIGHT

  converter = new SimpleCoordinateConverter(
    props.buildingWidth,
    props.buildingHeight,
    CANVAS_WIDTH,
    CANVAS_HEIGHT,
    PADDING,
    scale.value
  )

  draw()
}

/** 绘制整个场景 */
const draw = () => {
  const ctx = canvas.value?.getContext('2d')
  if (!ctx || !converter) return

  // 清空画布
  ctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)

  // 绘制背景
  ctx.fillStyle = '#fafafa'
  ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)

  // 绘制DXF 0层（如果有数据）
  if (visibleLayers.value.includes('layer0') && floorPlanData.value) {
    drawDxfLayer0(ctx)
  }

  // 绘制网格
  if (visibleLayers.value.includes('grid')) {
    drawGrid(ctx)
  }

  // 绘制标尺
  if (visibleLayers.value.includes('ruler')) {
    drawRuler(ctx)
  }

  // 绘制建筑边界
  drawBuildingBoundary(ctx)

  // 绘制已有设备
  if (visibleLayers.value.includes('devices') && props.existingDevices) {
    drawExistingDevices(ctx)
  }

  // 绘制选中点
  if (selectedPoint.value) {
    drawDeviceMarker(ctx, selectedPoint.value, localIconConfig.icon, localIconConfig.size, 'online', true)
  }

  // 绘制悬停点
  if (hoveredPoint.value && !selectedPoint.value) {
    drawCrosshair(ctx, hoveredPoint.value)
  }
}

/** 绘制DXF 0层（简化示例） */
const drawDxfLayer0 = (ctx: CanvasRenderingContext2D) => {
  // TODO: 实际项目中需要解析DXF文件并渲染0层内容
  // 这里仅绘制一个示例矩形代表建筑轮廓
  ctx.strokeStyle = '#1890ff'
  ctx.lineWidth = 2
  ctx.setLineDash([5, 5])
  
  const topLeft = converter!.realToPixel(2, props.buildingHeight - 2)
  const bottomRight = converter!.realToPixel(props.buildingWidth - 2, 2)
  
  ctx.strokeRect(
    topLeft.x,
    topLeft.y,
    bottomRight.x - topLeft.x,
    bottomRight.y - topLeft.y
  )
  
  ctx.setLineDash([])
}

/** 绘制网格 */
const drawGrid = (ctx: CanvasRenderingContext2D) => {
  ctx.strokeStyle = '#e0e0e0'
  ctx.lineWidth = 1

  // 垂直线
  for (let x = 0; x <= props.buildingWidth; x += GRID_SIZE) {
    const start = converter!.realToPixel(x, 0)
    const end = converter!.realToPixel(x, props.buildingHeight)
    
    ctx.beginPath()
    ctx.moveTo(start.x, start.y)
    ctx.lineTo(end.x, end.y)
    ctx.stroke()
  }

  // 水平线
  for (let y = 0; y <= props.buildingHeight; y += GRID_SIZE) {
    const start = converter!.realToPixel(0, y)
    const end = converter!.realToPixel(props.buildingWidth, y)
    
    ctx.beginPath()
    ctx.moveTo(start.x, start.y)
    ctx.lineTo(end.x, end.y)
    ctx.stroke()
  }
}

/** 绘制标尺 */
const drawRuler = (ctx: CanvasRenderingContext2D) => {
  ctx.fillStyle = '#595959'
  ctx.font = '11px Arial'

  // X轴标尺
  ctx.textAlign = 'center'
  for (let x = 0; x <= props.buildingWidth; x += GRID_SIZE) {
    const point = converter!.realToPixel(x, 0)
    ctx.fillText(`${x}m`, point.x, point.y + 18)
  }

  // Y轴标尺
  ctx.textAlign = 'right'
  for (let y = 0; y <= props.buildingHeight; y += GRID_SIZE) {
    const point = converter!.realToPixel(0, y)
    ctx.fillText(`${y}m`, point.x - 8, point.y + 4)
  }
}

/** 绘制建筑边界 */
const drawBuildingBoundary = (ctx: CanvasRenderingContext2D) => {
  const topLeft = converter!.realToPixel(0, props.buildingHeight)
  const bottomRight = converter!.realToPixel(props.buildingWidth, 0)

  ctx.strokeStyle = '#1890ff'
  ctx.lineWidth = 2
  ctx.strokeRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y)
}

/** 绘制已有设备 */
const drawExistingDevices = (ctx: CanvasRenderingContext2D) => {
  props.existingDevices?.forEach((device) => {
    drawDeviceMarker(
      ctx,
      { x: device.x, y: device.y },
      device.icon || 'ep:platform',
      'small',
      device.status,
      false
    )
  })
}

/** 绘制设备标记 */
const drawDeviceMarker = (
  ctx: CanvasRenderingContext2D,
  point: { x: number; y: number },
  icon: string,
  size: string,
  status: string,
  isSelected: boolean
) => {
  const pixel = converter!.realToPixel(point.x, point.y)
  const iconSize = getIconSize(size)
  const state = deviceStates.find(s => s.value === status) || deviceStates[0]

  // 绘制背景圆
  ctx.fillStyle = state.bgColor
  ctx.beginPath()
  ctx.arc(pixel.x, pixel.y, iconSize / 2 + 8, 0, 2 * Math.PI)
  ctx.fill()

  // 绘制边框（选中时加粗）
  ctx.strokeStyle = state.color
  ctx.lineWidth = isSelected ? 3 : 2
  ctx.beginPath()
  ctx.arc(pixel.x, pixel.y, iconSize / 2 + 8, 0, 2 * Math.PI)
  ctx.stroke()

  // 绘制图标（使用文本模拟，实际项目中应使用真实图标）
  ctx.fillStyle = state.color
  ctx.font = `${iconSize}px Arial`
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText('📷', pixel.x, pixel.y) // 实际应渲染Icon
}

/** 绘制十字准星 */
const drawCrosshair = (ctx: CanvasRenderingContext2D, point: { x: number; y: number }) => {
  const pixel = converter!.realToPixel(point.x, point.y)

  ctx.strokeStyle = '#1890ff'
  ctx.lineWidth = 1

  // 水平线
  ctx.beginPath()
  ctx.moveTo(pixel.x - 15, pixel.y)
  ctx.lineTo(pixel.x + 15, pixel.y)
  ctx.stroke()

  // 垂直线
  ctx.beginPath()
  ctx.moveTo(pixel.x, pixel.y - 15)
  ctx.lineTo(pixel.x, pixel.y + 15)
  ctx.stroke()
}

/** Canvas 事件处理 */
const handleCanvasClick = (event: MouseEvent) => {
  if (!converter) return

  const rect = canvas.value!.getBoundingClientRect()
  const pixelX = event.clientX - rect.left
  const pixelY = event.clientY - rect.top

  const real = converter.pixelToReal(pixelX, pixelY)

  if (converter.isInBounds(real.x, real.y)) {
    localCoordinates.x = parseFloat(real.x.toFixed(2))
    localCoordinates.y = parseFloat(real.y.toFixed(2))
    selectedPoint.value = { x: localCoordinates.x, y: localCoordinates.y }
    draw()
  }
}

const handleCanvasMouseMove = (event: MouseEvent) => {
  if (!converter) return

  const rect = canvas.value!.getBoundingClientRect()
  const pixelX = event.clientX - rect.left
  const pixelY = event.clientY - rect.top

  const real = converter.pixelToReal(pixelX, pixelY)

  if (converter.isInBounds(real.x, real.y)) {
    hoveredPoint.value = { 
      x: parseFloat(real.x.toFixed(2)), 
      y: parseFloat(real.y.toFixed(2)) 
    }
    canvas.value!.style.cursor = 'crosshair'
  } else {
    hoveredPoint.value = null
    canvas.value!.style.cursor = 'default'
  }

  draw()
}

const handleCanvasMouseLeave = () => {
  hoveredPoint.value = null
  draw()
}

const handleCanvasWheel = (event: WheelEvent) => {
  const delta = event.deltaY > 0 ? 0.9 : 1.1
  scale.value = Math.max(0.5, Math.min(3, scale.value * delta))
  initCanvas()
}

/** 工具栏操作 */
const zoomIn = () => {
  scale.value = Math.min(3, scale.value * 1.2)
  initCanvas()
}

const zoomOut = () => {
  scale.value = Math.max(0.5, scale.value / 1.2)
  initCanvas()
}

const resetView = () => {
  scale.value = 1
  panOffset.x = 0
  panOffset.y = 0
  initCanvas()
}

/** 快捷设置坐标 */
const quickSetCorner = (corner: string) => {
  switch (corner) {
    case 'topLeft':
      localCoordinates.x = 0
      localCoordinates.y = props.buildingHeight
      break
    case 'topRight':
      localCoordinates.x = props.buildingWidth
      localCoordinates.y = props.buildingHeight
      break
    case 'center':
      localCoordinates.x = props.buildingWidth / 2
      localCoordinates.y = props.buildingHeight / 2
      break
  }
  
  selectedPoint.value = { x: localCoordinates.x, y: localCoordinates.y }
  draw()
}

/** 坐标变化 */
const handleCoordinateChange = () => {
  selectedPoint.value = { x: localCoordinates.x, y: localCoordinates.y }
  draw()
  
  emit('update:coordinates', {
    x: localCoordinates.x,
    y: localCoordinates.y,
    z: localCoordinates.z,
    installLocation: localCoordinates.installLocation
  })
}

/** 图标变化 */
const handleIconChange = () => {
  draw()
  emit('update:icon', {
    icon: localIconConfig.icon,
    size: localIconConfig.size
  })
}

/** 确认选择 */
const confirmSelection = () => {
  handleCoordinateChange()
  dialogVisible.value = false
}

/** 打开弹框 */
const openDialog = () => {
  dialogVisible.value = true
  nextTick(() => {
    initCanvas()
    if (localCoordinates.x && localCoordinates.y) {
      selectedPoint.value = { x: localCoordinates.x, y: localCoordinates.y }
      draw()
    }
  })
}

/** 获取图标大小 */
const getIconSize = (size: string): number => {
  switch (size) {
    case 'small': return 16
    case 'large': return 32
    default: return 24
  }
}

/** 监听弹框关闭 */
watch(() => dialogVisible.value, (visible) => {
  if (visible) {
    // 模拟加载DXF数据
    setTimeout(() => {
      floorPlanData.value = { layer0: [] } // 实际应从API加载
    }, 500)
  }
})

defineExpose({
  openDialog,
  getCoordinates: () => localCoordinates,
  getIconConfig: () => localIconConfig
})
</script>

<style scoped lang="scss">
.enhanced-floor-plan-editor {
  .compact-mode {
    .icon-preview-panel {
      margin-top: 16px;

      .icon-preview-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 12px;
        padding: 12px;

        .icon-preview-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 8px;
          padding: 12px;
          border: 1px solid #e0e0e0;
          border-radius: 8px;
          background: #fafafa;

          .icon-wrapper {
            width: 56px;
            height: 56px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
          }

          .state-label {
            font-size: 13px;
            color: #595959;
            font-weight: 500;
          }
        }
      }
    }
  }

  .editor-container {
    display: flex;
    gap: 16px;
    height: calc(90vh - 120px);

    .editor-left {
      flex: 1;
      display: flex;
      flex-direction: column;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      overflow: hidden;

      .toolbar {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 16px;
        background: #fafafa;
        border-bottom: 1px solid #d9d9d9;
        flex-wrap: wrap;

        .toolbar-section {
          display: flex;
          align-items: center;
          gap: 8px;

          .toolbar-label {
            font-size: 13px;
            color: #595959;
            font-weight: 500;
          }
        }

        .ml-auto {
          margin-left: auto;
        }
      }

      .canvas-wrapper {
        flex: 1;
        position: relative;
        background: #fff;
        display: flex;
        align-items: center;
        justify-content: center;

        canvas {
          display: block;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }

        .empty-state {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
        }

        .axis-labels {
          position: absolute;
          pointer-events: none;

          .axis-label {
            display: flex;
            align-items: center;
            gap: 6px;
            padding: 6px 12px;
            background: rgba(255, 255, 255, 0.95);
            border-radius: 6px;
            font-size: 13px;
            color: #595959;
            font-weight: 500;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
          }

          .x-axis {
            position: absolute;
            bottom: 24px;
            right: 24px;
          }

          .y-axis {
            position: absolute;
            top: 24px;
            left: 24px;
          }
        }
      }
    }

    .editor-right {
      width: 360px;
      overflow-y: auto;

      .config-card {
        :deep(.el-card__header) {
          padding: 12px 16px;
        }

        .card-header {
          display: flex;
          align-items: center;
          gap: 8px;
          font-weight: 500;
          color: #262626;
        }

        .state-preview-grid {
          display: flex;
          flex-direction: column;
          gap: 12px;

          .state-preview-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px;
            border: 1px solid #e0e0e0;
            border-radius: 6px;
            background: #fafafa;

            .state-icon-wrapper {
              width: 48px;
              height: 48px;
              display: flex;
              align-items: center;
              justify-content: center;
              border-radius: 8px;
              flex-shrink: 0;
            }

            .state-info {
              flex: 1;
              display: flex;
              flex-direction: column;
              gap: 4px;

              .state-name {
                font-size: 14px;
                font-weight: 500;
                color: #262626;
              }
            }
          }
        }
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>




