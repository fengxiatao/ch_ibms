<template>
  <div 
    class="device-status-indicator"
    :class="[statusClass, { 'is-animating': isAnimating }]"
  >
    <!-- 状态指示点 -->
    <span class="status-dot" :class="dotClass">
      <span v-if="showPulse && isOnline" class="pulse-ring"></span>
    </span>
    
    <!-- 状态文本 -->
    <span v-if="showText" class="status-text" :class="textClass">
      {{ statusText }}
    </span>
  </div>
</template>

<script setup lang="ts">
/**
 * 设备状态指示器组件
 * 
 * 实时显示设备在线状态，支持状态变更动画效果
 * Requirements: 2.1
 */
import { computed, ref, watch } from 'vue'
import { DeviceStateEnum, DeviceStateNameMap } from '../useDeviceStatusWebSocket'

const props = withDefaults(defineProps<{
  /** 设备状态 (0-未激活, 1-在线, 2-离线) */
  state: number
  /** 是否显示状态文本 */
  showText?: boolean
  /** 是否显示脉冲动画（仅在线状态） */
  showPulse?: boolean
  /** 指示器大小 */
  size?: 'small' | 'default' | 'large'
}>(), {
  showText: true,
  showPulse: true,
  size: 'default'
})

// 动画状态
const isAnimating = ref(false)

// 监听状态变化，触发动画
watch(() => props.state, (newState, oldState) => {
  if (newState !== oldState) {
    isAnimating.value = true
    // 动画结束后重置
    setTimeout(() => {
      isAnimating.value = false
    }, 600)
  }
})

// 是否在线
const isOnline = computed(() => props.state === DeviceStateEnum.ONLINE)

// 状态文本
const statusText = computed(() => {
  return DeviceStateNameMap[props.state] || '未知'
})

// 状态样式类
const statusClass = computed(() => {
  const sizeClass = `size-${props.size}`
  switch (props.state) {
    case DeviceStateEnum.ONLINE:
      return ['status-online', sizeClass]
    case DeviceStateEnum.OFFLINE:
      return ['status-offline', sizeClass]
    case DeviceStateEnum.INACTIVE:
    default:
      return ['status-inactive', sizeClass]
  }
})

// 指示点样式类
const dotClass = computed(() => {
  switch (props.state) {
    case DeviceStateEnum.ONLINE:
      return 'dot-online'
    case DeviceStateEnum.OFFLINE:
      return 'dot-offline'
    case DeviceStateEnum.INACTIVE:
    default:
      return 'dot-inactive'
  }
})

// 文本样式类
const textClass = computed(() => {
  switch (props.state) {
    case DeviceStateEnum.ONLINE:
      return 'text-online'
    case DeviceStateEnum.OFFLINE:
      return 'text-offline'
    case DeviceStateEnum.INACTIVE:
    default:
      return 'text-inactive'
  }
})
</script>

<style scoped lang="scss">
.device-status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;

  &.is-animating {
    animation: status-change 0.6s ease;
  }

  // 尺寸变体
  &.size-small {
    .status-dot {
      width: 6px;
      height: 6px;
    }
    .status-text {
      font-size: 12px;
    }
  }

  &.size-default {
    .status-dot {
      width: 8px;
      height: 8px;
    }
    .status-text {
      font-size: 13px;
    }
  }

  &.size-large {
    .status-dot {
      width: 10px;
      height: 10px;
    }
    .status-text {
      font-size: 14px;
    }
  }
}

.status-dot {
  position: relative;
  border-radius: 50%;
  flex-shrink: 0;

  &.dot-online {
    background-color: var(--el-color-success);
  }

  &.dot-offline {
    background-color: var(--el-color-danger);
  }

  &.dot-inactive {
    background-color: var(--el-color-info);
  }
}

.pulse-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: var(--el-color-success);
  animation: pulse 2s ease-out infinite;
}

.status-text {
  font-weight: 500;
  white-space: nowrap;

  &.text-online {
    color: var(--el-color-success);
  }

  &.text-offline {
    color: var(--el-color-danger);
  }

  &.text-inactive {
    color: var(--el-color-info);
  }
}

// 脉冲动画
@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.8;
  }
  100% {
    transform: translate(-50%, -50%) scale(2.5);
    opacity: 0;
  }
}

// 状态变更动画
@keyframes status-change {
  0% {
    transform: scale(1);
  }
  25% {
    transform: scale(1.1);
  }
  50% {
    transform: scale(0.95);
  }
  75% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}
</style>
