<template>
  <div class="stream-player-grid">
    <div class="video-wall" :style="videoWallStyle">
      <div
        v-for="(win, index) in windows"
        :key="index"
        class="video-cell"
        :class="{ selected: activePane === index }"
        @click="$emit('update:activePane', index)"
        @dblclick="$emit('cell-dblclick', index)"
        @drop="handleDrop($event, index)"
        @dragover.prevent
        @dragenter.prevent
      >
        <div class="pane-label">窗口 {{ index + 1 }}</div>
        <div class="video-content">
          <StreamPlaybackPlayer
            v-if="win.playing && win.state?.streamId"
            :key="(win.state.stream || '') + '_' + (win.state.streamStartTime || 0)"
            :flv-url="win.state.flvUrl"
            :webrtc-url="win.state.webrtcUrl"
            :prefer-webrtc="true"
          />
          <div v-else class="video-placeholder">
            <Icon icon="ep:video-pause" class="placeholder-icon" />
            <div class="hint">
              <div class="hint-name">{{ win.cameraName || `窗口 ${index + 1}` }}</div>
              <template v-if="win.cameraId">
                <div
                  class="hint-sub"
                  :class="win.hasRecording === false ? 'no-recording' : 'has-recording'"
                >
                  {{ win.hasRecording === false ? '当前时间范围内无录像' : '有录像，点击时间轴播放' }}
                </div>
              </template>
              <div v-else class="hint-sub">拖拽通道到此窗口，或选择通道后点击时间轴播放</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Icon } from '@/components/Icon'
import StreamPlaybackPlayer from './StreamPlaybackPlayer.vue'
import type { GridLayoutType } from '../types'
import type { StreamPaneState } from '../composables/useStreamPlayback'

interface StreamWindow {
  cameraId?: number
  cameraName?: string
  hasRecording?: boolean
  playing: boolean
  state?: StreamPaneState
}

const props = defineProps<{
  windows: StreamWindow[]
  activePane: number
  gridLayout: GridLayoutType
}>()

const emit = defineEmits<{
  (e: 'update:activePane', idx: number): void
  (e: 'cell-dblclick', idx: number): void
  (e: 'cell-drop', evt: DragEvent, idx: number): void
}>()

const layoutToGrid = (layout: GridLayoutType): [number, number] => {
  if (layout === 1) return [1, 1]
  if (layout === 4) return [2, 2]
  if (layout === 6) return [2, 3]
  if (layout === 9) return [3, 3]
  if (layout === 12) return [3, 4]
  return [4, 4]
}

const videoWallStyle = computed(() => {
  const [rows, cols] = layoutToGrid(props.gridLayout)
  return {
    gridTemplateColumns: `repeat(${cols}, 1fr)`,
    gridTemplateRows: `repeat(${rows}, 1fr)`
  }
})

const handleDrop = (e: DragEvent, idx: number) => {
  emit('cell-drop', e, idx)
}
</script>

<style scoped lang="scss">
.stream-player-grid {
  width: 100%;
  height: 100%;
}

.video-wall {
  display: grid;
  height: 100%;
  min-height: 0;
  padding: 0;
  background: #0b1220;
  border-radius: 4px;
  gap: 8px;
}

.video-cell {
  position: relative;
  overflow: hidden;
  background: #000;
  border: 1px solid rgb(255 255 255 / 8%);
  border-radius: 6px;

  &.selected {
    border-color: rgb(64 158 255 / 75%);
    box-shadow: 0 0 0 1px rgb(64 158 255 / 20%) inset;
  }
}

.pane-label {
  position: absolute;
  top: 6px;
  left: 8px;
  z-index: 2;
  font-size: 12px;
  color: rgb(255 255 255 / 80%);
  text-shadow: 0 1px 2px rgb(0 0 0 / 50%);
}

.video-content {
  position: absolute;
  inset: 0;
}

.video-placeholder {
  display: flex;
  height: 100%;
  color: rgb(255 255 255 / 55%);
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 10px;

  .placeholder-icon {
    font-size: 32px;
  }

  .hint {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    font-size: 12px;
  }

  .hint-name {
    color: rgb(255 255 255 / 80%);
  }

  .hint-sub {
    color: rgb(255 255 255 / 50%);

    &.has-recording {
      color: #67c23a;
    }

    &.no-recording {
      color: #f56c6c;
    }
  }
}
</style>
