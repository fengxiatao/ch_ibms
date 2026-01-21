<!--
  区域多边形组件
  支持：直线多边形、曲线走廊、不规则形状
-->

<template>
  <!-- 区域填充 -->
  <v-line
    :config="{
      points: displayPoints,
      fill: fillColor,
      stroke: strokeColor,
      strokeWidth: selected ? 3 : 2,
      closed: area.type !== 'CORRIDOR', // 走廊不封闭
      opacity: hovered || selected ? 0.7 : 0.4,
      tension: area.type === 'CORRIDOR' ? 0.5 : 0, // 走廊使用曲线
      dash: area.type === 'CORRIDOR' ? [10, 5] : undefined, // 走廊使用虚线边框
      shadowBlur: selected ? 10 : 0,
      shadowColor: strokeColor,
      shadowOpacity: 0.5,
      listening: true
    }"
    @mouseover="handleMouseOver"
    @mouseout="handleMouseOut"
    @click="handleClick"
    @dblclick="handleDoubleClick"
  />

  <!-- 区域名称标签 -->
  <v-text
    v-if="showLabel"
    :config="{
      x: centerX - labelWidth / 2,
      y: centerY - 10,
      text: area.name,
      fontSize: 14,
      fontStyle: 'bold',
      fill: '#333',
      align: 'center',
      width: labelWidth,
      listening: false
    }"
  />

  <!-- 区域类型图标 -->
  <v-text
    v-if="showIcon"
    :config="{
      x: centerX - 10,
      y: centerY + 5,
      text: getAreaIcon(area.type),
      fontSize: 20,
      fill: '#666',
      align: 'center',
      listening: false
    }"
  />

  <!-- 编辑模式：显示控制点 -->
  <template v-if="editMode && selected">
    <v-circle
      v-for="(point, index) in controlPoints"
      :key="`control-${index}`"
      :config="{
        x: point.x,
        y: point.y,
        radius: 6,
        fill: '#fff',
        stroke: '#409eff',
        strokeWidth: 2,
        draggable: true,
        listening: true
      }"
      @dragmove="(e) => handleControlPointDrag(e, index)"
      @dblclick="(e) => handleControlPointDoubleClick(e, index)"
    />
  </template>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { getPolygonCenter } from '@/utils/coordinate/transform'

// 区域类型定义
export interface Area {
  id: number
  name: string
  code: string
  type: string // ROOM, CORRIDOR, ELEVATOR, STAIRCASE, RESTROOM, EQUIPMENT_ROOM, PUBLIC
  points: number[] // Konva 格式: [x1, y1, x2, y2, ...]
  color?: string
  localGeom?: string // PostGIS 格式（可选）
}

// Props
const props = withDefaults(
  defineProps<{
    area: Area
    selected?: boolean
    hovered?: boolean
    editMode?: boolean
    showLabel?: boolean
    showIcon?: boolean
  }>(),
  {
    selected: false,
    hovered: false,
    editMode: false,
    showLabel: true,
    showIcon: true
  }
)

// Emits
const emit = defineEmits<{
  click: [area: Area]
  dblclick: [area: Area]
  hover: [area: Area]
  hoverEnd: []
  update: [area: Area, newPoints: number[]]
  controlPointMove: [area: Area, pointIndex: number, newPos: { x: number; y: number }]
  controlPointDelete: [area: Area, pointIndex: number]
}>()

// 内部状态
const localHovered = ref(false)

// 计算属性

// 显示的点（可能经过平滑处理）
const displayPoints = computed(() => {
  if (props.area.type === 'CORRIDOR' && props.area.points.length >= 4) {
    // 走廊类型：使用曲线平滑
    // Konva 的 tension 参数会自动处理，这里直接返回原始点
    return props.area.points
  }
  return props.area.points
})

// 控制点（编辑模式）
const controlPoints = computed(() => {
  const points: { x: number; y: number }[] = []
  for (let i = 0; i < props.area.points.length; i += 2) {
    points.push({
      x: props.area.points[i],
      y: props.area.points[i + 1]
    })
  }
  return points
})

// 中心点
const center = computed(() => {
  return getPolygonCenter(props.area.points)
})

const centerX = computed(() => center.value.x)
const centerY = computed(() => center.value.y)

// 标签宽度
const labelWidth = computed(() => {
  return Math.max(props.area.name.length * 10, 60)
})

// 填充颜色
const fillColor = computed(() => {
  if (props.area.color) return props.area.color
  return getDefaultColor(props.area.type)
})

// 边框颜色
const strokeColor = computed(() => {
  if (props.selected) return '#409eff'
  if (localHovered.value || props.hovered) return '#67c23a'
  return darkenColor(fillColor.value, 20)
})

// 方法

/**
 * 获取区域默认颜色
 */
function getDefaultColor(areaType: string): string {
  const colorMap: Record<string, string> = {
    ROOM: 'rgba(66, 139, 202, 0.4)',           // 蓝色 - 房间
    CORRIDOR: 'rgba(245, 166, 35, 0.3)',       // 橙色 - 走廊
    ELEVATOR: 'rgba(92, 184, 92, 0.4)',        // 绿色 - 电梯
    STAIRCASE: 'rgba(217, 83, 79, 0.4)',       // 红色 - 楼梯
    RESTROOM: 'rgba(91, 192, 222, 0.4)',       // 青色 - 洗手间
    EQUIPMENT_ROOM: 'rgba(153, 153, 153, 0.4)', // 灰色 - 设备房
    PUBLIC: 'rgba(240, 173, 78, 0.4)'          // 黄色 - 公共区域
  }
  return colorMap[areaType] || 'rgba(200, 200, 200, 0.4)'
}

/**
 * 加深颜色
 */
function darkenColor(color: string, percent: number): string {
  // 简单实现：将 rgba 的不透明度增加
  if (color.startsWith('rgba')) {
    const match = color.match(/rgba\((\d+),\s*(\d+),\s*(\d+),\s*([\d.]+)\)/)
    if (match) {
      const [, r, g, b, a] = match
      const newA = Math.min(parseFloat(a) + percent / 100, 1)
      return `rgba(${r}, ${g}, ${b}, ${newA})`
    }
  }
  return color
}

/**
 * 获取区域类型图标
 */
function getAreaIcon(areaType: string): string {
  const iconMap: Record<string, string> = {
    ROOM: '🚪',
    CORRIDOR: '🚶',
    ELEVATOR: '🛗',
    STAIRCASE: '🪜',
    RESTROOM: '🚻',
    EQUIPMENT_ROOM: '⚙️',
    PUBLIC: '👥'
  }
  return iconMap[areaType] || '📍'
}

/**
 * 鼠标悬停
 */
function handleMouseOver() {
  localHovered.value = true
  emit('hover', props.area)
}

/**
 * 鼠标离开
 */
function handleMouseOut() {
  localHovered.value = false
  emit('hoverEnd')
}

/**
 * 单击
 */
function handleClick() {
  emit('click', props.area)
}

/**
 * 双击
 */
function handleDoubleClick() {
  emit('dblclick', props.area)
}

/**
 * 控制点拖拽
 */
function handleControlPointDrag(e: any, index: number) {
  const node = e.target
  const newPos = {
    x: node.x(),
    y: node.y()
  }
  emit('controlPointMove', props.area, index, newPos)
}

/**
 * 控制点双击（删除）
 */
function handleControlPointDoubleClick(e: any, index: number) {
  e.cancelBubble = true
  emit('controlPointDelete', props.area, index)
}
</script>

<style scoped lang="scss">
// Konva 组件不需要样式
</style>


















