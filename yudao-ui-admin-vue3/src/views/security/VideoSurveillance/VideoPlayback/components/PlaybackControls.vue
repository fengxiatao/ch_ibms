<template>
  <div class="playback-controls">
    <!-- 左侧：剪切、截图等 -->
    <div class="controls-left">
      <el-button size="small" @click="emit('screenshot')" title="截图">
        <Icon icon="ep:camera" />
      </el-button>
      <el-popover placement="top" :width="280" trigger="click">
        <template #reference>
          <el-button size="small" title="截图设置">
            <Icon icon="ep:setting" />
          </el-button>
        </template>
        <div style="padding: 8px 0">
          <el-checkbox v-model="localSettings.uploadToServer">
            截图自动上传到服务器
          </el-checkbox>
          <div style="font-size: 12px; color: #909399; margin-top: 4px; margin-left: 24px">
            开启后，截图会同时保存到本地和服务器
          </div>
        </div>
      </el-popover>
      <el-divider direction="vertical" />
      <!-- 剪切录像按钮 -->
      <el-button 
        size="small" 
        :type="isCutting ? 'danger' : clipStartTime ? 'success' : 'warning'"
        @click="emit('clip-video')" 
        :title="clipButtonTitle"
        :disabled="!isPlaying && !isCutting"
      >
        <Icon :icon="isCutting ? 'ep:loading' : 'ep:scissors'" :class="{ 'is-loading': isCutting }" />
        <span style="margin-left: 4px">{{ clipButtonText }}</span>
      </el-button>
      <!-- 裁剪进度 -->
      <span v-if="isCutting" class="cut-progress">
        <el-progress 
          :percentage="cutProgress" 
          :stroke-width="12" 
          :show-text="false"
          style="width: 60px;"
        />
        <span class="progress-text">{{ cutProgress }}%</span>
      </span>
    </div>

    <!-- 中间：播放控制 -->
    <div class="controls-center">
      <el-button size="small" circle @click="emit('backward')" title="后退 30 秒" :disabled="!isPlaying">
        <Icon icon="ep:d-arrow-left" />
      </el-button>
      <el-button
        size="small"
        circle
        type="primary"
        @click="emit('toggle-play')"
        :title="isPlaying ? '暂停' : '播放'"
      >
        <Icon :icon="isPaused ? 'ep:video-play' : 'ep:video-pause'" />
      </el-button>
      <el-button size="small" circle @click="emit('stop')" title="停止">
        <Icon icon="ep:close" />
      </el-button>
      <el-button size="small" circle @click="emit('forward')" title="前进 30 秒" :disabled="!isPlaying">
        <Icon icon="ep:d-arrow-right" />
      </el-button>
      <el-divider direction="vertical" />
      <!-- 同步所有窗口播放时间 -->
      <el-button 
        size="small" 
        type="success"
        @click="emit('sync-all')" 
        title="同步所有窗口到当前时间点"
        :disabled="!isPlaying"
      >
        <Icon icon="ep:connection" />
        <span style="margin-left: 4px">同步</span>
      </el-button>
      
      <el-select
        v-model="localSpeed"
        size="small"
        style="width: 64px"
        @change="handleSpeedChange"
        title="播放速度"
      >
        <el-option :value="0.5" label="0.5x" />
        <el-option :value="1" label="1x" />
        <el-option :value="2" label="2x" />
        <el-option :value="4" label="4x" />
        <el-option :value="8" label="8x" />
      </el-select>

      <el-button size="small" @click="emit('toggle-mute')" :title="isMuted ? '取消静音' : '静音'">
        <Icon :icon="isMuted ? 'ep:mute' : 'ep:microphone'" />
      </el-button>
    </div>

    <!-- 右侧：布局、全屏 -->
    <div class="controls-right">
      <el-select
        v-model="localLayout"
        size="small"
        style="width: 80px"
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
      <el-button size="small" @click="emit('fullscreen')" title="全屏">
        <Icon icon="ep:full-screen" />
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, reactive, computed } from 'vue'
import { Icon } from '@/components/Icon'
import type { GridLayoutType } from '../types'

// Props
const props = defineProps<{
  isPlaying: boolean
  isPaused: boolean
  isMuted: boolean
  playbackSpeed: number
  gridLayout: GridLayoutType
  // 裁剪相关
  isCutting?: boolean       // 是否正在裁剪
  cutProgress?: number      // 裁剪进度 0-100
  clipStartTime?: Date | null  // 剪切起始时间
}>()

// 剪切按钮文本
const clipButtonText = computed(() => {
  if (props.isCutting) return '取消'
  if (props.clipStartTime) return '终点'
  return '剪切'
})

// 剪切按钮提示
const clipButtonTitle = computed(() => {
  if (props.isCutting) return '点击取消裁剪'
  if (props.clipStartTime) return '点击设置剪切终点并开始下载'
  return '点击设置剪切起点'
})

// Emits
const emit = defineEmits<{
  (e: 'toggle-play'): void
  (e: 'stop'): void
  (e: 'backward'): void
  (e: 'forward'): void
  (e: 'toggle-mute'): void
  (e: 'screenshot'): void
  (e: 'fullscreen'): void
  (e: 'speed-change', speed: number): void
  (e: 'layout-change', layout: GridLayoutType): void
  (e: 'clip-video'): void    // 剪切录像
  (e: 'sync-all'): void      // 同步所有窗口
}>()

// 本地状态
const localSpeed = ref(props.playbackSpeed)
const localLayout = ref(props.gridLayout)
const localSettings = reactive({
  uploadToServer: false
})

// 监听 props 变化
watch(
  () => props.playbackSpeed,
  (val) => {
    localSpeed.value = val
  }
)

watch(
  () => props.gridLayout,
  (val) => {
    localLayout.value = val
  }
)

// 事件处理
const handleSpeedChange = (speed: number) => {
  emit('speed-change', speed)
}

const handleLayoutChange = (layout: GridLayoutType) => {
  emit('layout-change', layout)
}

// 暴露设置
defineExpose({
  localSettings
})
</script>

<style lang="scss" scoped>
.playback-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #1e1e1e;
  border-top: 1px solid #3a3a3a;

  .controls-left,
  .controls-center,
  .controls-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  :deep(.el-button) {
    padding: 6px 10px;
  }

  :deep(.el-button.is-circle) {
    width: 32px;
    height: 32px;
    padding: 0;
  }
  
  // 裁剪进度
  .cut-progress {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-left: 4px;
    
    .progress-text {
      font-size: 12px;
      color: #67c23a;
      min-width: 32px;
    }
  }
  
  // 加载动画
  .is-loading {
    animation: rotating 1.5s linear infinite;
  }
  
  @keyframes rotating {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }
}
</style>
