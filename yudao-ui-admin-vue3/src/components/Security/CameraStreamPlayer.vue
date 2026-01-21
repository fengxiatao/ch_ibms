<template>
  <div class="camera-stream-player" :class="{ 'ptz-enabled': ptzEnabled }">
    <!-- 视频显示区域 -->
    <div class="video-container" @dblclick="toggleFullscreen">
      <video
        ref="videoRef"
        v-if="streamUrl && !error"
        :src="streamUrl"
        autoplay
        muted
        :controls="showControls"
        @error="handleVideoError"
        @loadstart="handleVideoLoad"
        @canplay="handleVideoReady"
      ></video>
      
      <!-- 视频加载状态 -->
      <div v-if="loading" class="video-status loading">
        <Icon icon="ep:loading" class="loading-icon" />
        <span>正在连接摄像头...</span>
      </div>
      
      <!-- 视频错误状态 -->
      <div v-else-if="error" class="video-status error">
        <Icon icon="ep:video-camera-filled" :size="48" />
        <span>{{ error }}</span>
        <el-button size="small" @click="retryConnection">重试连接</el-button>
      </div>
      
      <!-- 设备离线状态 -->
      <div v-else-if="!online" class="video-status offline">
        <Icon icon="ep:video-camera-filled" :size="48" />
        <span>设备离线</span>
      </div>
      
      <!-- 视频覆盖层信息 -->
      <div v-if="!error && online" class="video-overlay">
        <div class="camera-info">
          <span class="camera-name">{{ cameraName }}</span>
          <span class="camera-status" :class="{ online, offline: !online }">
            {{ online ? '在线' : '离线' }}
          </span>
        </div>
        
        <!-- PTZ控制面板 -->
        <div v-if="ptzEnabled && showPtzControls" class="ptz-panel">
          <div class="ptz-title">云台控制</div>
          <div class="ptz-controls">
            <!-- 方向控制 -->
            <div class="direction-controls">
              <el-button class="ptz-btn up" @mousedown="startPTZ('up')" @mouseup="stopPTZ">
                <Icon icon="ep:arrow-up" />
              </el-button>
              <div class="ptz-row">
                <el-button class="ptz-btn left" @mousedown="startPTZ('left')" @mouseup="stopPTZ">
                  <Icon icon="ep:arrow-left" />
                </el-button>
                <el-button class="ptz-btn center" @click="resetPTZ">
                  <Icon icon="ep:aim" />
                </el-button>
                <el-button class="ptz-btn right" @mousedown="startPTZ('right')" @mouseup="stopPTZ">
                  <Icon icon="ep:arrow-right" />
                </el-button>
              </div>
              <el-button class="ptz-btn down" @mousedown="startPTZ('down')" @mouseup="stopPTZ">
                <Icon icon="ep:arrow-down" />
              </el-button>
            </div>
            
            <!-- 变焦控制 -->
            <div class="zoom-controls">
              <el-button size="small" @mousedown="startPTZ('zoom-in')" @mouseup="stopPTZ">
                <Icon icon="ep:zoom-in" /> 拉近
              </el-button>
              <el-button size="small" @mousedown="startPTZ('zoom-out')" @mouseup="stopPTZ">
                <Icon icon="ep:zoom-out" /> 拉远
              </el-button>
            </div>
            
            <!-- 预置位控制 -->
            <div v-if="presetCount > 0" class="preset-controls">
              <el-select v-model="selectedPreset" size="small" placeholder="预置位">
                <el-option
                  v-for="i in presetCount"
                  :key="i"
                  :label="`预置位${i}`"
                  :value="i"
                />
              </el-select>
              <el-button size="small" @click="gotoPreset">调用</el-button>
              <el-button size="small" @click="setPreset">设置</el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="video-actions">
        <el-button-group>
          <el-button size="small" @click="takeSnapshot">
            <Icon icon="ep:camera" /> 抓拍
          </el-button>
          <el-button size="small" @click="toggleRecording">
            <Icon :icon="recording ? 'ep:video-pause' : 'ep:video-play'" />
            {{ recording ? '停止录像' : '开始录像' }}
          </el-button>
          <el-button v-if="ptzEnabled" size="small" @click="togglePtzControls">
            <Icon icon="ep:operation" /> 云台
          </el-button>
          <el-button size="small" @click="toggleFullscreen">
            <Icon icon="ep:full-screen" /> 全屏
          </el-button>
        </el-button-group>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

// Props定义
interface Props {
  cameraName: string
  streamUrl: string
  online: boolean
  ptzEnabled?: boolean
  presetCount?: number
  showControls?: boolean
  config?: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  ptzEnabled: false,
  presetCount: 0,
  showControls: false,
  config: () => ({})
})

// 事件定义
const emit = defineEmits<{
  ptzControl: [direction: string, speed?: number]
  presetControl: [action: string, presetId: number]
  recordControl: [action: string, duration?: number]
  snapshot: []
}>()

// 响应式数据
const videoRef = ref<HTMLVideoElement>()
const loading = ref(true)
const error = ref('')
const recording = ref(false)
const showPtzControls = ref(false)
const selectedPreset = ref(1)
const ptzTimer = ref<number>()

// 视频状态处理
const handleVideoLoad = () => {
  loading.value = true
  error.value = ''
}

const handleVideoReady = () => {
  loading.value = false
  error.value = ''
  ElMessage.success(`摄像头 ${props.cameraName} 连接成功`)
}

const handleVideoError = (event: Event) => {
  loading.value = false
  error.value = '视频流连接失败，请检查网络或设备状态'
  console.error('Video error:', event)
}

const retryConnection = () => {
  if (videoRef.value) {
    loading.value = true
    error.value = ''
    videoRef.value.load()
  }
}

// PTZ控制
const startPTZ = (direction: string, speed: number = 5) => {
  if (!props.ptzEnabled) return
  
  emit('ptzControl', direction, speed)
  
  // 持续控制（按住时）
  ptzTimer.value = window.setTimeout(() => {
    startPTZ(direction, speed)
  }, 100)
}

const stopPTZ = () => {
  if (ptzTimer.value) {
    clearTimeout(ptzTimer.value)
    ptzTimer.value = undefined
  }
  emit('ptzControl', 'stop')
}

const resetPTZ = () => {
  emit('ptzControl', 'reset')
}

// 预置位控制
const gotoPreset = () => {
  emit('presetControl', 'goto', selectedPreset.value)
  ElMessage.info(`调用预置位 ${selectedPreset.value}`)
}

const setPreset = () => {
  emit('presetControl', 'set', selectedPreset.value)
  ElMessage.success(`设置预置位 ${selectedPreset.value}`)
}

// 录像控制
const toggleRecording = () => {
  const action = recording.value ? 'stop' : 'start'
  emit('recordControl', action, recording.value ? 0 : 300) // 默认录像5分钟
  recording.value = !recording.value
  ElMessage.info(recording.value ? '开始录像' : '停止录像')
}

// 抓拍
const takeSnapshot = () => {
  emit('snapshot')
  ElMessage.success('抓拍成功')
}

// 全屏控制
const toggleFullscreen = () => {
  if (videoRef.value) {
    if (document.fullscreenElement) {
      document.exitFullscreen()
    } else {
      videoRef.value.requestFullscreen()
    }
  }
}

// PTZ面板切换
const togglePtzControls = () => {
  showPtzControls.value = !showPtzControls.value
}

// 监听在线状态变化
watch(() => props.online, (newOnline) => {
  if (!newOnline) {
    loading.value = false
    error.value = ''
  }
})

// 监听流地址变化
watch(() => props.streamUrl, () => {
  if (videoRef.value && props.streamUrl) {
    retryConnection()
  }
})

onMounted(() => {
  // 如果有流地址且设备在线，开始加载视频
  if (props.streamUrl && props.online) {
    retryConnection()
  } else {
    loading.value = false
  }
})

onUnmounted(() => {
  if (ptzTimer.value) {
    clearTimeout(ptzTimer.value)
  }
})
</script>

<style lang="scss" scoped>
.camera-stream-player {
  position: relative;
  width: 100%;
  height: 100%;
  background: #000;
  border-radius: 8px;
  overflow: hidden;

  .video-container {
    position: relative;
    width: 100%;
    height: 100%;

    video {
      width: 100%;
      height: 100%;
      object-fit: contain;
      background: #000;
    }
  }

  .video-status {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #fff;
    background: rgba(0, 0, 0, 0.8);

    .loading-icon {
      animation: spin 1s linear infinite;
    }

    &.loading {
      color: #00d4ff;
    }

    &.error {
      color: #ef4444;
    }

    &.offline {
      color: #94a3b8;
    }

    span {
      margin: 8px 0;
      font-size: 14px;
    }
  }

  .video-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;

    .camera-info {
      position: absolute;
      top: 12px;
      left: 12px;
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 6px 12px;
      background: rgba(0, 0, 0, 0.6);
      border-radius: 6px;
      color: #fff;
      font-size: 12px;

      .camera-status {
        padding: 2px 6px;
        border-radius: 10px;
        font-size: 10px;

        &.online {
          background: #22c55e;
          color: #fff;
        }

        &.offline {
          background: #ef4444;
          color: #fff;
        }
      }
    }

    .ptz-panel {
      position: absolute;
      top: 12px;
      right: 12px;
      background: rgba(0, 0, 0, 0.8);
      border-radius: 8px;
      padding: 12px;
      color: #fff;
      pointer-events: auto;

      .ptz-title {
        font-size: 12px;
        color: #00d4ff;
        margin-bottom: 8px;
        text-align: center;
      }

      .ptz-controls {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }

      .direction-controls {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;

        .ptz-btn {
          width: 32px;
          height: 32px;
          padding: 0;
          border-radius: 50%;
          background: rgba(0, 212, 255, 0.2);
          border: 1px solid #00d4ff;
          color: #00d4ff;

          &:hover {
            background: rgba(0, 212, 255, 0.4);
          }
        }

        .ptz-row {
          display: flex;
          gap: 4px;
        }
      }

      .zoom-controls, .preset-controls {
        display: flex;
        gap: 4px;
        justify-content: center;

        .el-button {
          font-size: 10px;
        }
      }
    }
  }

  .video-actions {
    position: absolute;
    bottom: 12px;
    left: 50%;
    transform: translateX(-50%);
    opacity: 0;
    transition: opacity 0.3s ease;

    .el-button-group {
      background: rgba(0, 0, 0, 0.6);
      border-radius: 6px;
    }
  }

  &:hover .video-actions {
    opacity: 1;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>




















