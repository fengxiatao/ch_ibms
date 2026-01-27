<template>
  <div class="playback-player-grid">
    <!-- 播放器网格 -->
    <div class="player-grid" :class="gridLayoutClass" ref="gridRef">
      <div
        v-for="(pane, idx) in panes"
        :key="'pane-' + idx"
        class="player-pane"
        :class="{ 
          active: activePane === idx, 
          'drag-over': dragOverPane === idx,
          playing: pane.isPlaying
        }"
        @click="handlePaneClick(idx)"
        @drop="handleDrop($event, idx)"
        @dragover.prevent="handleDragOver(idx)"
        @dragleave="handleDragLeave"
      >
        <!-- Jessibuca 播放器容器 - 使用固定 ID，避免 Vue 响应式干扰 -->
        <div 
          class="jessibuca-container"
          :id="'playback-player-' + idx"
        >
          <!-- 播放器会在这里创建 -->
        </div>

        <!-- 覆盖层 - 使用 v-show 替代 v-if 避免频繁创建销毁 -->
        <div class="pane-overlay" :class="{ hidden: pane.isPlaying && !pane.isLoading }">
          <div class="overlay-top">
            <span class="window-title">窗口 {{ idx + 1 }}</span>
          </div>

          <!-- 加载中 -->
          <div v-if="pane.isLoading" class="overlay-center loading">
            <div class="spinner"></div>
            <p class="window-label">正在加载录像...</p>
            <p class="tip-text">{{ pane.channel?.name }}</p>
          </div>

          <!-- 空闲状态 -->
          <div v-else-if="!pane.channel" class="overlay-center">
            <Icon icon="ep:video-pause" :size="48" />
            <p class="window-label">窗口 {{ idx + 1 }}</p>
            <p class="tip-text">拖拽通道到此处</p>
            <p class="tip-text">或选择通道后点击时间轴播放</p>
          </div>

          <!-- 已绑定通道但未播放 -->
          <div v-else-if="!pane.isPlaying" class="overlay-center">
            <Icon icon="ep:video-camera" :size="48" />
            <p class="window-label">{{ pane.channel.name }}</p>
            <p v-if="pane.hasRecording" class="tip-text has-recording">有录像，点击时间轴播放</p>
            <p v-else class="tip-text no-recording">当前时间范围内无录像</p>
          </div>

          <div class="overlay-bottom">
            <span v-if="pane.channel" class="device-name">
              {{ pane.channel.name }}
              <span v-if="pane.hasRecording" class="status-dot has-recording">●</span>
              <span v-else class="status-dot no-recording">●</span>
            </span>
          </div>
        </div>

        <!-- 播放中信息条（悬停显示） -->
        <div v-if="pane.isPlaying" class="playing-info-bar">
          <span class="channel-name">{{ pane.channel?.name }}</span>
          <span class="time-info">{{ formatPlayTime(pane.currentPlaySeconds) }}</span>
        </div>

        <!-- 错误提示 -->
        <div v-if="pane.error" class="pane-error">
          <Icon icon="ep:warning-filled" :size="24" />
          <span>{{ pane.error }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@/components/Icon'
import type { PlaybackPane, GridLayoutType } from '../types'
import { GRID_LAYOUT_CLASS } from '../types'

// Props
const props = defineProps<{
  panes: PlaybackPane[]
  activePane: number
  gridLayout: GridLayoutType
}>()

// Emits
const emit = defineEmits<{
  (e: 'update:activePane', idx: number): void
  (e: 'pane-drop', event: DragEvent, paneIndex: number): void
  (e: 'pane-ref', paneIndex: number, el: HTMLElement | null): void
}>()

// 状态
const gridRef = ref<HTMLElement | null>(null)
const dragOverPane = ref<number | null>(null)

// 计算属性
const gridLayoutClass = computed(() => GRID_LAYOUT_CLASS[props.gridLayout] || 'grid-2x3')

// 初始化时发送所有窗格容器的引用
onMounted(() => {
  // 延迟发送引用，确保 DOM 已渲染
  setTimeout(() => {
    for (let i = 0; i < props.panes.length; i++) {
      const el = document.getElementById(`playback-player-${i}`)
      if (el) {
        emit('pane-ref', i, el)
      }
    }
  }, 100)
})

// 事件处理
const handlePaneClick = (idx: number) => {
  emit('update:activePane', idx)
}

const handleDragOver = (idx: number) => {
  dragOverPane.value = idx
}

const handleDragLeave = () => {
  dragOverPane.value = null
}

const handleDrop = (e: DragEvent, idx: number) => {
  e.preventDefault()
  dragOverPane.value = null
  emit('pane-drop', e, idx)
}

// 格式化播放时间
const formatPlayTime = (seconds: number): string => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = Math.floor(seconds % 60)
  if (h > 0) {
    return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  }
  return `${m}:${String(s).padStart(2, '0')}`
}

// 获取窗格容器元素
const getPaneContainer = (idx: number): HTMLElement | null => {
  return document.getElementById(`playback-player-${idx}`)
}

// 暴露方法
defineExpose({
  gridRef,
  getPaneContainer
})
</script>

<style lang="scss" scoped>
.playback-player-grid {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  .player-grid {
    flex: 1;
    display: grid;
    gap: 6px;
    background: #000;
    padding: 6px;
    min-height: 0;

    &.grid-1x1 {
      grid-template-columns: 1fr;
      grid-template-rows: 1fr;
    }

    &.grid-2x2 {
      grid-template-columns: repeat(2, 1fr);
      grid-template-rows: repeat(2, 1fr);
    }

    &.grid-2x3 {
      grid-template-columns: repeat(3, 1fr);
      grid-template-rows: repeat(2, 1fr);
    }

    &.grid-3x3 {
      grid-template-columns: repeat(3, 1fr);
      grid-template-rows: repeat(3, 1fr);
    }

    &.grid-3x4 {
      grid-template-columns: repeat(4, 1fr);
      grid-template-rows: repeat(3, 1fr);
    }

    &.grid-4x4 {
      grid-template-columns: repeat(4, 1fr);
      grid-template-rows: repeat(4, 1fr);
    }

    .player-pane {
      position: relative;
      background: #0a0a0a;
      border: 2px solid #2f2f2f;
      border-radius: 4px;
      overflow: hidden;
      cursor: pointer;
      transition: border-color 0.2s;

      &.active {
        border-color: #409eff;
        box-shadow: 0 0 8px rgba(64, 158, 255, 0.35);
      }

      &.drag-over {
        border-color: #67c23a;
        box-shadow: 0 0 12px rgba(103, 194, 58, 0.5);
        background: rgba(103, 194, 58, 0.1);
      }

      &.playing {
        .playing-info-bar {
          opacity: 0;
          transition: opacity 0.3s;
        }

        &:hover .playing-info-bar {
          opacity: 1;
        }
      }

      .jessibuca-container {
        width: 100%;
        height: 100%;
        background: #000;
      }

      .pane-overlay {
        position: absolute;
        inset: 0;
        pointer-events: none;
        z-index: 10;
        transition: opacity 0.3s;

        &.hidden {
          opacity: 0;
          pointer-events: none;
        }

        .overlay-top {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          padding: 6px 10px;
          background: linear-gradient(180deg, rgba(0, 0, 0, 0.7) 0%, transparent 100%);

          .window-title {
            color: #fff;
            font-size: 12px;
            font-weight: 500;
          }
        }

        .overlay-center {
          position: absolute;
          inset: 0;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          gap: 8px;
          color: #7a9aba;

          .window-label {
            margin: 0;
            font-size: 14px;
            font-weight: 500;
            color: #a5c0db;
          }

          .tip-text {
            margin: 0;
            font-size: 12px;
            color: #6c88a3;

            &.has-recording {
              color: #67c23a;
            }

            &.no-recording {
              color: #f56c6c;
            }
          }

          &.loading {
            .spinner {
              width: 48px;
              height: 48px;
              border: 3px solid rgba(64, 158, 255, 0.2);
              border-top-color: #409eff;
              border-radius: 50%;
              animation: rotate 1s linear infinite;
            }

            .window-label {
              color: #409eff;
            }
          }
        }

        .overlay-bottom {
          position: absolute;
          bottom: 0;
          left: 0;
          right: 0;
          padding: 6px 10px;
          background: linear-gradient(0deg, rgba(0, 0, 0, 0.7) 0%, transparent 100%);

          .device-name {
            color: #fff;
            font-size: 12px;
            display: flex;
            align-items: center;
            gap: 6px;
          }

          .status-dot {
            font-size: 8px;

            &.has-recording {
              color: #67c23a;
            }

            &.no-recording {
              color: #f56c6c;
            }
          }
        }
      }

      .playing-info-bar {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 12px;
        background: linear-gradient(0deg, rgba(0, 0, 0, 0.8) 0%, transparent 100%);
        z-index: 15;

        .channel-name {
          color: #fff;
          font-size: 12px;
          font-weight: 500;
        }

        .time-info {
          color: #67c23a;
          font-size: 12px;
        }
      }

      .pane-error {
        position: absolute;
        bottom: 40px;
        left: 10px;
        right: 10px;
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 12px;
        background: rgba(245, 108, 108, 0.9);
        border-radius: 4px;
        color: #fff;
        font-size: 12px;
        z-index: 20;
      }
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
