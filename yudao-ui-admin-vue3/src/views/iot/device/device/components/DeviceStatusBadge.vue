<template>
  <el-tag
    :type="tagType"
    :size="size"
    :effect="effect"
    class="device-status-badge"
    :class="{ 'is-animating': isAnimating }"
  >
    <span class="status-dot" :class="dotClass"></span>
    <span class="status-label">{{ statusText }}</span>
  </el-tag>
</template>

<script setup lang="ts">
/**
 * 设备状态标签组件
 * 
 * 以 Element Plus Tag 形式展示设备状态
 * 支持状态变更动画效果
 * Requirements: 2.1
 */
import { computed, ref, watch } from 'vue'
import { DeviceStateEnum, DeviceStateNameMap } from '../useDeviceStatusWebSocket'

const props = withDefaults(defineProps<{
  /** 设备状态 (0-未激活, 1-在线, 2-离线) */
  state: number
  /** 标签大小 */
  size?: 'large' | 'default' | 'small'
  /** 标签效果 */
  effect?: 'dark' | 'light' | 'plain'
}>(), {
  size: 'default',
  effect: 'light'
})

// 动画状态
const isAnimating = ref(false)

// 监听状态变化，触发动画
watch(() => props.state, (newState, oldState) => {
  if (newState !== oldState) {
    isAnimating.value = true
    setTimeout(() => {
      isAnimating.value = false
    }, 600)
  }
})

// 状态文本
const statusText = computed(() => {
  return DeviceStateNameMap[props.state] || '未知'
})

// Tag 类型
const tagType = computed(() => {
  switch (props.state) {
    case DeviceStateEnum.ONLINE:
      return 'success'
    case DeviceStateEnum.OFFLINE:
      return 'danger'
    case DeviceStateEnum.INACTIVE:
    default:
      return 'info'
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
</script>

<style scoped lang="scss">
.device-status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;

  &.is-animating {
    animation: badge-pulse 0.6s ease;
  }

  .status-dot {
    width: 6px;
    height: 6px;
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

  .status-label {
    font-weight: 500;
  }
}

@keyframes badge-pulse {
  0% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(var(--el-color-primary-rgb), 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 0 4px rgba(var(--el-color-primary-rgb), 0);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(var(--el-color-primary-rgb), 0);
  }
}
</style>
