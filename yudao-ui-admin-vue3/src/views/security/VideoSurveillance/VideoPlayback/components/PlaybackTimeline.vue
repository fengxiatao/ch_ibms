<template>
  <div class="playback-timeline">
    <!-- 时间刻度 -->
    <div class="time-scale">
      <div class="scale-marks">
        <div
          v-for="(mark, i) in scaleMarks"
          :key="i"
          class="scale-mark"
          :class="{ long: mark.long }"
          :style="{ left: mark.left + '%' }"
        >
          <span v-if="mark.long" class="mark-time">{{ mark.label }}</span>
        </div>
      </div>
    </div>

    <!-- 时间轴条 -->
    <div class="timeline-bar" @click="handleTimelineClick" @mousedown="startDrag">
      <!-- 录像时间段 -->
      <div
        v-for="(seg, i) in timelineSegments"
        :key="i"
        class="timeline-segment"
        :style="{ left: seg.left + '%', width: seg.width + '%' }"
        :title="seg.label"
      ></div>

      <!-- 当前播放位置指示器 -->
      <div
        v-if="showCursor"
        class="timeline-cursor"
        :style="{ left: currentTimePercent + '%' }"
      >
        <div class="cursor-line"></div>
        <div class="cursor-time">{{ currentTimeLabel }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import type { ChannelRecordingInfo, TimelineSegment } from '../types'
import { parseTimeString } from '../types'

// Props
const props = defineProps<{
  // 时间范围
  startTime: string
  endTime: string
  // 录像信息列表
  recordingInfoList: ChannelRecordingInfo[]
  // 当前播放时间（毫秒）
  currentPlayTime: number
  // 是否显示游标
  showCursor?: boolean
}>()

// Emits
const emit = defineEmits<{
  (e: 'timeline-click', clickTime: Date, percent: number): void
  (e: 'time-change', time: number): void
}>()

// 状态
const isDragging = ref(false)

// 计算属性：时间范围
const timelineStart = computed(() => parseTimeString(props.startTime))
const timelineEnd = computed(() => parseTimeString(props.endTime))
const timeRangeMs = computed(() => Math.max(timelineEnd.value - timelineStart.value, 1))

// 计算属性：刻度标记
const scaleMarks = computed<Array<{ left: number; label: string; long: boolean }>>(() => {
  const start = timelineStart.value
  const total = timeRangeMs.value
  const majors = 12  // 主刻度数量
  const minorsBetween = 3  // 每个主刻度之间的小刻度数量
  const marks: Array<{ left: number; label: string; long: boolean }> = []

  for (let j = 0; j <= majors; j++) {
    const leftMajor = (j / majors) * 100
    const tMajor = start + (j / majors) * total
    const date = new Date(tMajor)
    const hh = String(date.getHours()).padStart(2, '0')
    const mm = String(date.getMinutes()).padStart(2, '0')
    marks.push({ left: leftMajor, label: `${hh}:${mm}`, long: true })

    if (j < majors) {
      for (let k = 1; k <= minorsBetween; k++) {
        const leftMinor = ((j + k / (minorsBetween + 1)) / majors) * 100
        marks.push({ left: leftMinor, label: '', long: false })
      }
    }
  }

  return marks
})

// 计算属性：时间轴片段
const timelineSegments = computed<TimelineSegment[]>(() => {
  if (!props.recordingInfoList.length) return []

  const rangeStart = timelineStart.value
  const rangeEnd = timelineEnd.value
  const rangeMs = timeRangeMs.value

  // 收集所有有录像的时间段
  const allIntervals: { start: number; end: number }[] = []

  props.recordingInfoList.forEach((channelInfo) => {
    channelInfo.segments.forEach((segment) => {
      if (segment.hasRecording) {
        const start = parseTimeString(segment.startTime)
        const end = parseTimeString(segment.endTime)
        if (start > 0 && end > 0 && end > start) {
          allIntervals.push({ start, end })
        }
      }
    })
  })

  if (allIntervals.length === 0) return []

  // 按开始时间排序
  allIntervals.sort((a, b) => a.start - b.start)

  // 合并重叠的时间段
  const mergedIntervals: { start: number; end: number }[] = []
  for (const interval of allIntervals) {
    if (
      mergedIntervals.length === 0 ||
      mergedIntervals[mergedIntervals.length - 1].end < interval.start
    ) {
      mergedIntervals.push({ ...interval })
    } else {
      mergedIntervals[mergedIntervals.length - 1].end = Math.max(
        mergedIntervals[mergedIntervals.length - 1].end,
        interval.end
      )
    }
  }

  // 转换为时间轴片段
  const segments: TimelineSegment[] = []
  mergedIntervals.forEach((interval) => {
    const left = ((interval.start - rangeStart) / rangeMs) * 100
    const width = ((interval.end - interval.start) / rangeMs) * 100

    if (width > 0) {
      const clampedLeft = Math.max(0, Math.min(100, left))
      const clampedWidth = Math.min(width, 100 - clampedLeft)

      if (clampedWidth > 0) {
        segments.push({
          left: clampedLeft,
          width: clampedWidth,
          label: `${new Date(interval.start).toLocaleTimeString()} ~ ${new Date(interval.end).toLocaleTimeString()}`,
          startTime: interval.start,
          endTime: interval.end
        })
      }
    }
  })

  return segments
})

// 计算属性：当前时间百分比
const currentTimePercent = computed(() => {
  const start = timelineStart.value
  const end = timelineEnd.value
  const total = Math.max(end - start, 1)
  const t = Math.min(Math.max(props.currentPlayTime || start, start), end)
  return ((t - start) / total) * 100
})

// 计算属性：当前时间标签
const currentTimeLabel = computed(() => {
  const t = props.currentPlayTime || timelineStart.value
  return new Date(t).toLocaleTimeString()
})

// 事件处理
const handleTimelineClick = (event: MouseEvent) => {
  const bar = event.currentTarget as HTMLElement
  const rect = bar.getBoundingClientRect()
  const clickX = event.clientX - rect.left
  const percent = clickX / rect.width

  const start = timelineStart.value
  const end = timelineEnd.value
  const clickTime = new Date(start + (end - start) * percent)

  emit('timeline-click', clickTime, percent)
}

// 拖拽处理
let dragCleanup: (() => void) | null = null

const startDrag = (e: MouseEvent) => {
  isDragging.value = true
  const bar = e.currentTarget as HTMLElement
  const rect = bar.getBoundingClientRect()

  const updateTime = (clientX: number) => {
    const percent = Math.min(Math.max((clientX - rect.left) / rect.width, 0), 1)
    const start = timelineStart.value
    const end = timelineEnd.value
    const time = start + percent * (end - start)
    emit('time-change', time)
  }

  const move = (ev: MouseEvent) => updateTime(ev.clientX)
  const up = () => {
    isDragging.value = false
    window.removeEventListener('mousemove', move)
    window.removeEventListener('mouseup', up)
    dragCleanup = null
  }

  window.addEventListener('mousemove', move)
  window.addEventListener('mouseup', up)
  dragCleanup = () => {
    window.removeEventListener('mousemove', move)
    window.removeEventListener('mouseup', up)
  }

  updateTime(e.clientX)
}

onUnmounted(() => {
  if (dragCleanup) dragCleanup()
})
</script>

<style lang="scss" scoped>
.playback-timeline {
  width: 100%;
  flex-shrink: 0;

  .time-scale {
    height: 24px;
    background: #252525;
    border-top: 1px solid #3a3a3a;
    position: relative;

    .scale-marks {
      position: relative;
      height: 100%;
    }

    .scale-mark {
      position: absolute;
      bottom: 0;
      width: 1px;
      height: 40%;
      background: #3a3a3a;

      &.long {
        background: #909399;
        height: 50%;
      }

      .mark-time {
        position: absolute;
        bottom: calc(50% + 4px);
        left: 0;
        transform: translateX(-50%);
        font-size: 10px;
        color: #909399;
        white-space: nowrap;
      }
    }
  }

  .timeline-bar {
    position: relative;
    height: 20px;
    background: rgba(255, 255, 255, 0.06);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 4px;
    cursor: pointer;
    overflow: hidden;

    .timeline-segment {
      position: absolute;
      top: 0;
      bottom: 0;
      background: #67c23a;
      opacity: 0.8;
      border-radius: 2px;
      min-width: 2px;
      transition: opacity 0.2s;

      &:hover {
        opacity: 1;
      }
    }

    .timeline-cursor {
      position: absolute;
      top: 0;
      bottom: 0;
      width: 0;
      transform: translateX(-1px);
      pointer-events: none;
      z-index: 10;

      .cursor-line {
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 2px;
        background: #409eff;
      }

      .cursor-time {
        position: absolute;
        top: -22px;
        left: 50%;
        transform: translateX(-50%);
        padding: 2px 8px;
        font-size: 11px;
        color: #333;
        background: #eaeaea;
        border-radius: 10px;
        white-space: nowrap;
      }
    }
  }
}
</style>
