<template>
  <div class="video-player-grid">
    <!-- 播放器网格 -->
    <div class="player-grid" :class="gridLayoutClass" ref="gridRef">
      <div
        v-for="(pane, idx) in panes"
        :key="'pane-' + idx"
        class="player-pane"
        :class="{
          active: activePane === idx && !isPatrolling,
          'drag-over': dragOverPane === idx,
          'patrol-mode': isPatrolling,
          'area-zoom-mode': areaZoomActive && pane.isPlaying
        }"
        @click="handlePaneClick(idx)"
        @drop="handleDrop($event, idx)"
        @dragover.prevent="handleDragOver(idx)"
        @dragleave="handleDragLeave"
      >
        <!-- Jessibuca 播放器容器 -->
        <div
          class="pane-video jessibuca-container"
          :ref="(el) => setPaneVideoRef(el as HTMLElement, idx)"
          :data-index="idx"
        ></div>
        
        <!-- 区域放大选择层 -->
        <div 
          v-if="areaZoomActive && pane.isPlaying"
          class="area-zoom-layer"
          @mousedown="handleAreaZoomStart($event, idx)"
          @mousemove="handleAreaZoomMove($event, idx)"
          @mouseup="handleAreaZoomEnd($event, idx)"
          @mouseleave="handleAreaZoomEnd($event, idx)"
        >
          <!-- 选择框 -->
          <div 
            v-if="isSelecting && selectionPaneIndex === idx" 
            class="selection-box"
            :style="selectionRect"
          ></div>
          <!-- 提示文字 -->
          <div v-if="!isSelecting || selectionPaneIndex !== idx" class="area-zoom-hint">
            <Icon icon="ep:crop" :size="32" />
            <span>按住鼠标拖拽框选放大区域</span>
          </div>
        </div>

        <!-- 覆盖层 -->
        <div class="pane-overlay">
          <!-- 加载中状态 -->
          <div v-if="pane.isLoading" class="overlay-center loading">
            <Icon icon="ep:loading" :size="64" class="loading-icon" />
            <p class="window-label">正在连接视频流...</p>
            <p class="tip-text">{{ pane.channelName || '' }}</p>
          </div>

          <!-- 未播放时显示提示信息 -->
          <div v-else-if="!pane.isPlaying" class="overlay-center idle">
            <Icon icon="ep:video-pause" :size="64" />
            <p class="window-label">窗口 {{ idx + 1 }}</p>
            <p class="tip-text">拖拽通道到此处播放实时视频</p>
            <p class="tip-text">或双击通道选择窗口播放</p>
          </div>

          <!-- 错误状态 -->
          <div v-if="pane.error && !pane.isLoading" class="overlay-center error">
            <Icon icon="ep:warning-filled" :size="64" />
            <p class="error-text">{{ pane.error }}</p>
            <el-button size="small" @click="emit('retry', idx)">重试</el-button>
          </div>

          <!-- 底部通道名称（非播放时） -->
          <div v-if="pane.channelName && !pane.isPlaying" class="overlay-bottom">
            <span class="device-name">{{ pane.channelName }}</span>
          </div>

          <!-- 悬停工具栏（仅在播放时显示，轮巡时隐藏） -->
          <div v-if="pane.isPlaying && !isPatrolling" class="pane-toolbar">
            <el-button size="small" @click.stop="emit('stop', idx)" title="停止播放" type="danger">
              <Icon icon="ep:video-camera-filled" />
            </el-button>

            <el-button size="small" @click.stop="emit('snapshot', idx)" title="截图">
              <Icon icon="ep:camera" />
            </el-button>

            <!-- 录像按钮 -->
            <el-button 
              size="small" 
              @click.stop="emit('record', idx)" 
              :title="pane.isRecording ? '停止录像' : '开始录像'"
              :type="pane.isRecording ? 'danger' : 'default'"
              :class="{ 'recording-btn': pane.isRecording }"
            >
              <Icon :icon="pane.isRecording ? 'ep:video-pause' : 'ep:video-play'" />
            </el-button>

            <el-button size="small" @click.stop="emit('fullscreen', idx)" title="全屏">
              <Icon icon="ep:full-screen" />
            </el-button>

            <!-- 码流切换 -->
            <el-dropdown @command="(cmd: string) => emit('stream-switch', idx, cmd)" trigger="click">
              <el-button size="small" title="码流切换">
                <Icon icon="ep:switch" />
                <span style="margin-left: 4px">{{ pane.streamType === 'sub' ? '子' : '主' }}</span>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="main" :disabled="pane.streamType !== 'sub'">
                    主码流
                  </el-dropdown-item>
                  <el-dropdown-item command="sub" :disabled="pane.streamType === 'sub'">
                    子码流
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <!-- 录像状态指示器 -->
          <div v-if="pane.isRecording" class="recording-indicator">
            <span class="recording-dot"></span>
            <span class="recording-text">录像中</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部控制栏 -->
    <div class="playback-controls">
      <div class="controls-left">
        <!-- 当前视图显示 -->
        <div class="current-view-info" v-if="currentViewName">
          <Icon icon="ep:video-camera" style="color: #409eff" />
          <span class="view-name">{{ currentViewName }}</span>
          <el-tag size="small" type="info">{{ gridLayout }}分屏</el-tag>
        </div>
        <div class="current-view-info" v-else>
          <Icon icon="ep:video-camera" style="color: #909399" />
          <span class="view-name" style="color: #909399">未加载视图</span>
        </div>

        <!-- 视图操作按钮 -->
        <el-button-group size="small" v-if="!currentViewName">
          <!-- 未加载视图时：显示"保存为视图" -->
          <el-button @click="emit('view-save-as')" :disabled="!hasPlayingPanes" title="保存当前画面为视图">
            <Icon icon="ep:document-add" />
            保存为视图
          </el-button>
        </el-button-group>
        <el-button-group size="small" v-else>
          <!-- 已加载视图时：显示"更新当前视图"和"另存新视图" -->
          <el-button @click="emit('view-update')" :disabled="!hasPlayingPanes" title="更新当前视图">
            <Icon icon="ep:refresh" />
            更新当前视图
          </el-button>
          <el-button @click="emit('view-save-as')" :disabled="!hasPlayingPanes" title="另存为新视图">
            <Icon icon="ep:document-add" />
            另存新视图
          </el-button>
        </el-button-group>

        <!-- 轮巡控制 -->
        <el-divider direction="vertical" />
        <slot name="patrol-controls"></slot>
      </div>

      <div class="controls-right">
        <!-- 大华直连模式标识 -->
        <!-- <el-tag type="success" size="small">
          <Icon icon="ep:monitor" style="margin-right: 4px" />
          大华直连
        </el-tag> -->

        <!-- <el-divider direction="vertical" /> -->

        <!-- 停止所有 -->
        <el-button
          size="small"
          @click="emit('stop-all')"
          type="danger"
          :disabled="!hasPlayingPanes"
          title="停止所有播放器"
        >
          <Icon icon="ep:video-camera-filled" />
          停止全部
        </el-button>

        <!-- 分屏布局 -->
        <el-select
          :model-value="gridLayout"
          size="small"
          style="width: 96px"
          @change="handleLayoutChange"
          title="分屏布局"
        >
          <el-option :value="1" label="1×1" />
          <el-option :value="4" label="2×2" />
          <el-option :value="6" label="2×3" />
          <el-option :value="9" label="3×3" />
          <el-option :value="12" label="3×4" />
          <el-option :value="16" label="4×4" />
        </el-select>

        <!-- 全屏 -->
        <el-button size="small" @click="handleFullscreen" title="全屏">
          <Icon icon="ep:full-screen" />
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Icon } from '@/components/Icon'
import type { PlayerPane, GridLayoutType } from '../types'
import { GRID_LAYOUT_CLASS } from '../types'

// Props
interface Props {
  panes: PlayerPane[]
  activePane: number
  gridLayout: GridLayoutType
  currentViewName?: string
  isPatrolling?: boolean
  areaZoomActive?: boolean  // 区域放大模式
}

const props = withDefaults(defineProps<Props>(), {
  currentViewName: '',
  isPatrolling: false,
  areaZoomActive: false
})

// Emits
const emit = defineEmits<{
  (e: 'update:activePane', idx: number): void
  (e: 'update:gridLayout', layout: GridLayoutType): void
  (e: 'pane-drop', event: DragEvent, paneIndex: number): void
  (e: 'stop', paneIndex: number): void
  (e: 'snapshot', paneIndex: number): void
  (e: 'record', paneIndex: number): void
  (e: 'fullscreen', paneIndex: number): void
  (e: 'stream-switch', paneIndex: number, streamType: string): void
  (e: 'stop-all'): void
  (e: 'view-save-as'): void
  (e: 'view-update'): void
  (e: 'retry', paneIndex: number): void
  (e: 'layout-change', layout: GridLayoutType): void
  (e: 'pane-ref', paneIndex: number, el: HTMLElement | null): void
  (e: 'area-zoom', paneIndex: number, rect: { startX: number; startY: number; endX: number; endY: number }): void
}>()

// Refs
const gridRef = ref<HTMLElement | null>(null)
const dragOverPane = ref(-1)

// 区域放大选择框状态
const isSelecting = ref(false)
const selectionPaneIndex = ref(-1)
const selectionStart = ref({ x: 0, y: 0 })
const selectionEnd = ref({ x: 0, y: 0 })
const selectionRect = computed(() => {
  const x1 = Math.min(selectionStart.value.x, selectionEnd.value.x)
  const y1 = Math.min(selectionStart.value.y, selectionEnd.value.y)
  const x2 = Math.max(selectionStart.value.x, selectionEnd.value.x)
  const y2 = Math.max(selectionStart.value.y, selectionEnd.value.y)
  return {
    left: `${x1}%`,
    top: `${y1}%`,
    width: `${x2 - x1}%`,
    height: `${y2 - y1}%`
  }
})

// 计算属性
const gridLayoutClass = computed(() => GRID_LAYOUT_CLASS[props.gridLayout] || 'grid-2x3')

const hasPlayingPanes = computed(() => props.panes.some(p => p.isPlaying))

// 设置窗格容器引用
const setPaneVideoRef = (el: HTMLElement | null, idx: number) => {
  if (!el || idx < 0 || idx >= props.panes.length) return
  // 通过 emit 通知父组件设置 videoEl
  emit('pane-ref', idx, el)
}

// 事件处理
const handlePaneClick = (idx: number) => {
  if (props.isPatrolling) return
  emit('update:activePane', idx)
}

const handleDragOver = (idx: number) => {
  dragOverPane.value = idx
}

const handleDragLeave = () => {
  dragOverPane.value = -1
}

const handleDrop = (e: DragEvent, idx: number) => {
  e.preventDefault()
  dragOverPane.value = -1
  emit('pane-drop', e, idx)
}

const handleLayoutChange = (val: GridLayoutType) => {
  emit('layout-change', val)
}

const handleFullscreen = () => {
  const el = gridRef.value
  if (el && el.requestFullscreen) {
    el.requestFullscreen()
  }
}

// 区域放大：开始选择
const handleAreaZoomStart = (e: MouseEvent, paneIndex: number) => {
  if (!props.areaZoomActive) return
  
  const pane = props.panes[paneIndex]
  if (!pane.isPlaying) return

  const target = e.currentTarget as HTMLElement
  const rect = target.getBoundingClientRect()
  const x = ((e.clientX - rect.left) / rect.width) * 100
  const y = ((e.clientY - rect.top) / rect.height) * 100

  isSelecting.value = true
  selectionPaneIndex.value = paneIndex
  selectionStart.value = { x, y }
  selectionEnd.value = { x, y }

  e.preventDefault()
  e.stopPropagation()
}

// 区域放大：移动选择
const handleAreaZoomMove = (e: MouseEvent, paneIndex: number) => {
  if (!isSelecting.value || selectionPaneIndex.value !== paneIndex) return

  const target = e.currentTarget as HTMLElement
  const rect = target.getBoundingClientRect()
  const x = Math.max(0, Math.min(100, ((e.clientX - rect.left) / rect.width) * 100))
  const y = Math.max(0, Math.min(100, ((e.clientY - rect.top) / rect.height) * 100))

  selectionEnd.value = { x, y }
}

// 区域放大：结束选择
const handleAreaZoomEnd = (e: MouseEvent, paneIndex: number) => {
  if (!isSelecting.value || selectionPaneIndex.value !== paneIndex) return

  isSelecting.value = false

  // 计算归一化坐标 (0-1)
  const x1 = Math.min(selectionStart.value.x, selectionEnd.value.x) / 100
  const y1 = Math.min(selectionStart.value.y, selectionEnd.value.y) / 100
  const x2 = Math.max(selectionStart.value.x, selectionEnd.value.x) / 100
  const y2 = Math.max(selectionStart.value.y, selectionEnd.value.y) / 100

  // 检查选择区域是否足够大（至少 3% 的宽度和高度）
  if (x2 - x1 > 0.03 && y2 - y1 > 0.03) {
    emit('area-zoom', paneIndex, {
      startX: x1,
      startY: y1,
      endX: x2,
      endY: y2
    })
  }

  // 重置选择状态
  selectionPaneIndex.value = -1
  selectionStart.value = { x: 0, y: 0 }
  selectionEnd.value = { x: 0, y: 0 }
}

// 暴露方法
defineExpose({
  gridRef
})
</script>

<style lang="scss" scoped>
.video-player-grid {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;

  .player-grid {
    flex: 1;
    display: grid;
    gap: 4px;
    background: #000;
    min-height: 0;
    padding: 8px;
    overflow: hidden;

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
  }

  .player-pane {
    position: relative;
    background: #1a1a1a;
    border: 2px solid transparent;
    border-radius: 4px;
    overflow: hidden;
    transition: all 0.2s;

    &.active {
      border-color: #409eff;
      box-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
    }

    &.drag-over {
      border-color: #67c23a;
      background: rgba(103, 194, 58, 0.1);
    }

    &.patrol-mode {
      pointer-events: none;
    }

    .pane-video {
      width: 100%;
      height: 100%;
      background: #000;
    }

    .pane-overlay {
      position: absolute;
      inset: 0;
      display: flex;
      flex-direction: column;
      pointer-events: none;

      .overlay-center {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: rgba(255, 255, 255, 0.6);

        &.loading {
          .loading-icon {
            animation: rotate 1s linear infinite;
          }
        }

        &.error {
          color: #f56c6c;
          
          .error-text {
            margin: 8px 0;
          }
          
          .el-button {
            pointer-events: auto;
          }
        }

        .window-label {
          margin-top: 12px;
          font-size: 14px;
        }

        .tip-text {
          margin-top: 4px;
          font-size: 12px;
          color: rgba(255, 255, 255, 0.4);
        }
      }

      .overlay-bottom {
        padding: 8px 12px;
        background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));

        .device-name {
          font-size: 12px;
          color: #fff;
        }
      }

      .pane-toolbar {
        position: absolute;
        top: 8px;
        right: 8px;
        display: flex;
        gap: 4px;
        opacity: 0;
        transition: opacity 0.2s;
        pointer-events: auto;
      }
    }

    &:hover .pane-toolbar {
      opacity: 1;
    }

    // 录像状态指示器
    .recording-indicator {
      position: absolute;
      top: 8px;
      left: 8px;
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 4px 10px;
      background: rgba(0, 0, 0, 0.6);
      border-radius: 4px;
      z-index: 10;

      .recording-dot {
        width: 10px;
        height: 10px;
        background: #f56c6c;
        border-radius: 50%;
        animation: recording-blink 1s ease-in-out infinite;
      }

      .recording-text {
        font-size: 12px;
        color: #f56c6c;
        font-weight: 500;
      }
    }

    // 录像按钮动画
    .recording-btn {
      animation: recording-pulse 1.5s ease-in-out infinite;
    }

    // 区域放大模式
    &.area-zoom-mode {
      border-color: #f0a020;
      
      &::after {
        content: '区域放大';
        position: absolute;
        top: 4px;
        left: 4px;
        padding: 2px 8px;
        background: #f0a020;
        color: #000;
        font-size: 11px;
        font-weight: 500;
        border-radius: 2px;
        z-index: 20;
      }
    }

    // 区域放大选择层
    .area-zoom-layer {
      position: absolute;
      inset: 0;
      z-index: 15;
      cursor: crosshair;
      background: rgba(0, 0, 0, 0.1);

      .selection-box {
        position: absolute;
        border: 2px dashed #f0a020;
        background: rgba(240, 160, 32, 0.2);
        pointer-events: none;
      }

      .area-zoom-hint {
        position: absolute;
        inset: 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: rgba(240, 160, 32, 0.8);
        pointer-events: none;

        span {
          margin-top: 8px;
          font-size: 12px;
        }
      }
    }
  }

  .playback-controls {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    background: #1e1e1e;
    border-top: 1px solid #333;

    .controls-left,
    .controls-right {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .current-view-info {
      display: flex;
      align-items: center;
      gap: 6px;

      .view-name {
        font-size: 13px;
        color: #cfe3ff;
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

@keyframes recording-blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}

@keyframes recording-pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.4);
  }
  50% {
    box-shadow: 0 0 0 4px rgba(245, 108, 108, 0.2);
  }
}
</style>
