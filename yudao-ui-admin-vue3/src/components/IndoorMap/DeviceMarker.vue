<!--
  设备标注组件
  支持：拖拽、点击、悬停、状态显示、图标
-->

<template>
  <v-group
    :config="{
      x: device.x,
      y: device.y,
      draggable: draggable && !device.isNew,
      listening: true
    }"
    @dragstart="handleDragStart"
    @dragmove="handleDragMove"
    @dragend="handleDragEnd"
    @click="handleClick"
    @dblclick="handleDoubleClick"
    @mouseover="handleMouseOver"
    @mouseout="handleMouseOut"
  >
    <!-- 设备圆形背景 -->
    <v-circle
      :config="{
        x: 0,
        y: 0,
        radius: radius,
        fill: fillColor,
        stroke: strokeColor,
        strokeWidth: selected ? 3 : 2,
        shadowBlur: hovered || selected ? 10 : 5,
        shadowColor: shadowColor,
        shadowOpacity: 0.5
      }"
    />

    <!-- 设备图标 -->
    <v-text
      :config="{
        x: -iconSize / 2,
        y: -iconSize / 2,
        text: deviceIcon,
        fontSize: iconSize,
        fill: '#fff',
        align: 'center',
        verticalAlign: 'middle',
        width: iconSize,
        height: iconSize,
        listening: false
      }"
    />

    <!-- 状态指示灯 -->
    <v-circle
      v-if="showStatusDot"
      :config="{
        x: radius * 0.6,
        y: -radius * 0.6,
        radius: 4,
        fill: statusDotColor,
        stroke: '#fff',
        strokeWidth: 1
      }"
    />

    <!-- 设备名称标签（悬停或选中时显示） -->
    <v-label
      v-if="showLabel"
      :config="{
        x: radius + 5,
        y: -10,
        opacity: hovered || selected ? 1 : 0
      }"
    >
      <v-tag
        :config="{
          fill: 'rgba(0, 0, 0, 0.8)',
          cornerRadius: 5,
          pointerDirection: 'left',
          pointerWidth: 8,
          pointerHeight: 8
        }"
      />
      <v-text
        :config="{
          text: device.name || '未命名设备',
          fontSize: 12,
          padding: 8,
          fill: '#fff'
        }"
      />
    </v-label>

    <!-- 新设备标识（闪烁动画） -->
    <v-circle
      v-if="device.isNew"
      :config="{
        x: 0,
        y: 0,
        radius: radius + 5,
        stroke: '#ff4444',
        strokeWidth: 2,
        dash: [5, 5],
        opacity: pulseOpacity
      }"
    />
  </v-group>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

// 设备数据类型
export interface Device {
  id?: number
  name: string
  type: string // LIGHTING, AC, CAMERA, SENSOR, DOOR, ALARM, etc.
  status?: string // online, offline, alarm
  x: number // Canvas X坐标（像素）
  y: number // Canvas Y坐标（像素）
  localX?: number // 本地X坐标（米）
  localY?: number // 本地Y坐标（米）
  localZ?: number // 本地Z坐标（米）
  areaId?: number
  areaName?: string
  floorId?: number
  isNew?: boolean // 是否为新添加的设备
  realtimeData?: Record<string, any>
}

// Props
const props = withDefaults(
  defineProps<{
    device: Device
    selected?: boolean
    draggable?: boolean
    showLabel?: boolean
    showStatusDot?: boolean
  }>(),
  {
    selected: false,
    draggable: true,
    showLabel: true,
    showStatusDot: true
  }
)

// Emits
const emit = defineEmits<{
  click: [device: Device]
  dblclick: [device: Device]
  hover: [device: Device]
  hoverEnd: []
  dragStart: [device: Device]
  dragMove: [device: Device, newPos: { x: number; y: number }]
  dragEnd: [device: Device, newPos: { x: number; y: number }]
}>()

// 内部状态
const hovered = ref(false)
const isDragging = ref(false)
const pulseOpacity = ref(1)
let pulseInterval: number | null = null

// 计算属性

// 圆形半径
const radius = computed(() => {
  if (props.selected) return 12
  if (hovered.value) return 10
  return 8
})

// 图标大小
const iconSize = computed(() => radius.value * 1.5)

// 填充颜色（根据设备状态）
const fillColor = computed(() => {
  if (props.device.isNew) return '#ff4444'
  if (props.device.status === 'alarm') return '#f56c6c'
  if (props.device.status === 'offline') return '#909399'
  return getDeviceTypeColor(props.device.type)
})

// 边框颜色
const strokeColor = computed(() => {
  if (props.selected) return '#409eff'
  if (hovered.value) return '#fff'
  return darkenColor(fillColor.value, 30)
})

// 阴影颜色
const shadowColor = computed(() => {
  return fillColor.value
})

// 状态指示灯颜色
const statusDotColor = computed(() => {
  if (props.device.status === 'online') return '#67c23a'
  if (props.device.status === 'offline') return '#909399'
  if (props.device.status === 'alarm') return '#f56c6c'
  return '#e6a23c'
})

// 设备图标
const deviceIcon = computed(() => {
  return getDeviceIcon(props.device.type)
})

// 方法

/**
 * 获取设备类型颜色
 */
function getDeviceTypeColor(deviceType: string): string {
  const colorMap: Record<string, string> = {
    LIGHTING: '#f39c12',    // 黄色 - 照明
    AC: '#3498db',          // 蓝色 - 空调
    CAMERA: '#e74c3c',      // 红色 - 摄像头
    SENSOR: '#9b59b6',      // 紫色 - 传感器
    DOOR: '#2ecc71',        // 绿色 - 门禁
    ALARM: '#e67e22',       // 橙色 - 报警器
    SWITCH: '#34495e',      // 深灰 - 开关
    METER: '#16a085',       // 青色 - 电表/水表
    FIRE: '#c0392b',        // 深红 - 消防设备
    HVAC: '#2980b9'         // 深蓝 - 暖通空调
  }
  return colorMap[deviceType] || '#95a5a6'
}

/**
 * 获取设备图标
 */
function getDeviceIcon(deviceType: string): string {
  const iconMap: Record<string, string> = {
    LIGHTING: '💡',
    AC: '❄️',
    CAMERA: '📷',
    SENSOR: '📡',
    DOOR: '🚪',
    ALARM: '🔔',
    SWITCH: '🔘',
    METER: '📊',
    FIRE: '🧯',
    HVAC: '🌡️',
    SMOKE_DETECTOR: '🚨',
    WATER: '💧',
    POWER: '⚡',
    NETWORK: '🌐'
  }
  return iconMap[deviceType] || '📟'
}

/**
 * 加深颜色
 */
function darkenColor(color: string, percent: number): string {
  // 简单的颜色加深算法
  if (color.startsWith('#')) {
    const hex = color.replace('#', '')
    const r = Math.max(0, parseInt(hex.substr(0, 2), 16) - (255 * percent) / 100)
    const g = Math.max(0, parseInt(hex.substr(2, 2), 16) - (255 * percent) / 100)
    const b = Math.max(0, parseInt(hex.substr(4, 2), 16) - (255 * percent) / 100)
    return `#${Math.round(r).toString(16).padStart(2, '0')}${Math.round(g).toString(16).padStart(2, '0')}${Math.round(b).toString(16).padStart(2, '0')}`
  }
  return color
}

/**
 * 鼠标悬停
 */
function handleMouseOver() {
  hovered.value = true
  emit('hover', props.device)
}

/**
 * 鼠标离开
 */
function handleMouseOut() {
  hovered.value = false
  emit('hoverEnd')
}

/**
 * 单击
 */
function handleClick() {
  emit('click', props.device)
}

/**
 * 双击
 */
function handleDoubleClick() {
  emit('dblclick', props.device)
}

/**
 * 拖拽开始
 */
function handleDragStart() {
  isDragging.value = true
  emit('dragStart', props.device)
}

/**
 * 拖拽中
 */
function handleDragMove(e: any) {
  const node = e.target
  const newPos = {
    x: node.x(),
    y: node.y()
  }
  emit('dragMove', props.device, newPos)
}

/**
 * 拖拽结束
 */
function handleDragEnd(e: any) {
  isDragging.value = false
  const node = e.target
  const newPos = {
    x: node.x(),
    y: node.y()
  }
  emit('dragEnd', props.device, newPos)
}

/**
 * 启动脉冲动画（新设备）
 */
function startPulseAnimation() {
  if (pulseInterval) return
  
  let increasing = false
  pulseInterval = window.setInterval(() => {
    if (increasing) {
      pulseOpacity.value += 0.05
      if (pulseOpacity.value >= 1) increasing = false
    } else {
      pulseOpacity.value -= 0.05
      if (pulseOpacity.value <= 0.3) increasing = true
    }
  }, 50)
}

/**
 * 停止脉冲动画
 */
function stopPulseAnimation() {
  if (pulseInterval) {
    clearInterval(pulseInterval)
    pulseInterval = null
  }
}

// 生命周期

watch(
  () => props.device.isNew,
  (isNew) => {
    if (isNew) {
      startPulseAnimation()
    } else {
      stopPulseAnimation()
    }
  },
  { immediate: true }
)

onMounted(() => {
  if (props.device.isNew) {
    startPulseAnimation()
  }
})

onUnmounted(() => {
  stopPulseAnimation()
})
</script>

<style scoped lang="scss">
// Konva 组件不需要样式
</style>


















