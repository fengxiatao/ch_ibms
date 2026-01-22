<template>
  <div class="camera-player">
    <div class="player-container">
      <!-- 视频播放器 -->
      <div class="video-wrapper">
        <video
          ref="videoRef"
          class="video-player"
          autoplay
          muted
          controls
          @error="handleVideoError"
          @loadedmetadata="handleVideoLoaded"
        >
          <source :src="streamUrl" type="application/x-rtsp" />
          您的浏览器不支持视频播放
        </video>

        <!-- 加载中状态 -->
        <div v-if="loading" class="loading-overlay">
          <Icon icon="ep:loading" class="loading-icon" />
          <p>正在加载视频流...</p>
        </div>

        <!-- 错误状态 -->
        <div v-if="error" class="error-overlay">
          <Icon icon="ep:warning" class="error-icon" />
          <p>{{ error }}</p>
          <el-button size="small" @click="retryLoad">重试</el-button>
        </div>

        <!-- 设备信息覆盖层 -->
        <div class="info-overlay" v-if="showInfo">
          <div class="device-name">{{ camera.name }}</div>
          <div class="device-status" :class="camera.status">
            <Icon :icon="getStatusIcon(camera.status)" />
            <span>{{ getStatusText(camera.status) }}</span>
          </div>
          <div class="device-location">
            <Icon icon="ep:location" />
            <span>{{ camera.location }}</span>
          </div>
        </div>

        <!-- 云台控制面板 -->
        <div v-if="showPTZControl && camera.ptzEnabled" class="ptz-control-panel">
          <div class="ptz-controls">
            <!-- 上 -->
            <div class="ptz-btn ptz-up" @mousedown="startPTZ('up')" @mouseup="stopPTZ" @mouseleave="stopPTZ">
              <Icon icon="ep:arrow-up" />
            </div>

            <!-- 左 -->
            <div class="ptz-btn ptz-left" @mousedown="startPTZ('left')" @mouseup="stopPTZ" @mouseleave="stopPTZ">
              <Icon icon="ep:arrow-left" />
            </div>

            <!-- 中心/自动 -->
            <div class="ptz-btn ptz-center">
              <Icon icon="ep:video-camera" />
            </div>

            <!-- 右 -->
            <div class="ptz-btn ptz-right" @mousedown="startPTZ('right')" @mouseup="stopPTZ" @mouseleave="stopPTZ">
              <Icon icon="ep:arrow-right" />
            </div>

            <!-- 下 -->
            <div class="ptz-btn ptz-down" @mousedown="startPTZ('down')" @mouseup="stopPTZ" @mouseleave="stopPTZ">
              <Icon icon="ep:arrow-down" />
            </div>
          </div>

          <!-- 变焦控制 -->
          <div class="zoom-controls">
            <div class="zoom-btn" @mousedown="startPTZ('zoomIn')" @mouseup="stopPTZ" @mouseleave="stopPTZ">
              <Icon icon="ep:plus" />
              <span>放大</span>
            </div>
            <div class="zoom-btn" @mousedown="startPTZ('zoomOut')" @mouseup="stopPTZ" @mouseleave="stopPTZ">
              <Icon icon="ep:minus" />
              <span>缩小</span>
            </div>
          </div>

          <!-- 预置位控制 -->
          <div class="preset-controls">
            <el-select v-model="selectedPreset" placeholder="选择预置位" size="small">
              <el-option v-for="preset in presetList" :key="preset.id" 
                         :label="`${preset.presetNo} - ${preset.presetName}`" :value="preset.presetNo" />
            </el-select>
            <el-button size="small" @click="gotoPreset">转到</el-button>
            <el-button size="small" @click="setPreset">设置</el-button>
          </div>

          <!-- 管理按钮 -->
          <div class="manage-controls">
            <el-button size="small" @click="openPresetManager">
              <Icon icon="ep:setting" /> 预设点管理
            </el-button>
            <el-button size="small" @click="openCruiseManager">
              <Icon icon="ep:video-camera" /> 巡航管理
            </el-button>
          </div>
        </div>
      </div>

      <!-- 控制按钮栏 -->
      <div class="control-bar">
        <el-button-group>
          <el-button size="small" @click="toggleInfo">
            <Icon icon="ep:info-filled" />
            信息
          </el-button>
          <el-button size="small" @click="togglePTZControl" v-if="camera.ptzEnabled">
            <Icon icon="ep:pointer" />
            云台
          </el-button>
          <el-button size="small" @click="handleSnapshot">
            <Icon icon="ep:camera" />
            截图
          </el-button>
          <el-button size="small" @click="toggleRecord">
            <Icon :icon="isRecording ? 'ep:video-pause' : 'ep:video-play'" />
            {{ isRecording ? '停止录像' : '开始录像' }}
          </el-button>
          <el-button size="small" @click="toggleFullscreen">
            <Icon icon="ep:full-screen" />
            全屏
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 预设点管理对话框 -->
    <PresetManager
      ref="presetManagerRef"
      :channelId="camera.channelId"
      :deviceId="camera.deviceId"
      @goto="handleGotoPreset"
    />

    <!-- 巡航管理对话框 -->
    <CruiseManager
      ref="cruiseManagerRef"
      :channelId="camera.channelId"
      :deviceId="camera.deviceId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { CameraApi } from '@/api/iot/device/camera'
import * as PresetApi from '@/api/iot/cameraPreset'
import PresetManager from './PresetManager.vue'
import CruiseManager from './CruiseManager.vue'

interface Props {
  camera: any
  streamUrl: string
}

const props = defineProps<Props>()

const videoRef = ref<HTMLVideoElement>()
const loading = ref(true)
const error = ref('')
const showInfo = ref(true)
const showPTZControl = ref(false)
const selectedPreset = ref(1)
const presetList = ref<PresetApi.CameraPresetVO[]>([])
const presetManagerRef = ref()
const cruiseManagerRef = ref()

// 录像状态
const isRecording = computed(() => {
  return props.camera.realtimeData?.recording_status === 1
})

// 获取状态图标
const getStatusIcon = (status: string) => {
  switch (status) {
    case 'online':
      return 'ep:circle-check'
    case 'offline':
      return 'ep:circle-close'
    default:
      return 'ep:question'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'online':
      return '在线'
    case 'offline':
      return '离线'
    default:
      return '未知'
  }
}

// 视频加载完成
const handleVideoLoaded = () => {
  loading.value = false
  error.value = ''
  console.log('视频加载成功')
}

// 视频加载错误
const handleVideoError = (e: Event) => {
  loading.value = false
  error.value = '视频流加载失败，请检查摄像头连接'
  console.error('视频加载失败:', e)
}

// 重试加载
const retryLoad = () => {
  loading.value = true
  error.value = ''
  if (videoRef.value) {
    videoRef.value.load()
  }
}

// 切换信息显示
const toggleInfo = () => {
  showInfo.value = !showInfo.value
}

// 切换云台控制
const togglePTZControl = () => {
  showPTZControl.value = !showPTZControl.value
}

// PTZ控制映射
const ptzDirectionMap: Record<string, number> = {
  up: 0,
  down: 1,
  left: 2,
  right: 3,
  zoomIn: 4,
  zoomOut: 5
}

// 开始PTZ控制
const startPTZ = async (direction: string) => {
  try {
    const directionCode = ptzDirectionMap[direction]
    if (directionCode === undefined) return

    await CameraApi.controlPTZ(props.camera.deviceId, {
      direction: directionCode,
      speed: 5
    })
  } catch (error) {
    console.error('PTZ控制失败:', error)
  }
}

// 停止PTZ控制
const stopPTZ = async () => {
  try {
    await CameraApi.controlPTZ(props.camera.deviceId, {
      direction: 6, // 停止
      speed: 0
    })
  } catch (error) {
    console.error('停止PTZ失败:', error)
  }
}

// 转到预置位
const gotoPreset = async () => {
  try {
    await CameraApi.gotoPreset(props.camera.deviceId, selectedPreset.value)
    ElMessage.success(`转到预置位${selectedPreset.value}`)
  } catch (error: any) {
    ElMessage.error(`转到预置位失败: ${error?.message}`)
  }
}

// 设置预置位
const setPreset = async () => {
  try {
    await CameraApi.setPreset(props.camera.deviceId, {
      preset_id: selectedPreset.value,
      preset_name: `预置位${selectedPreset.value}`
    })
    ElMessage.success(`设置预置位${selectedPreset.value}成功`)
  } catch (error: any) {
    ElMessage.error(`设置预置位失败: ${error?.message}`)
  }
}

// 截图
const handleSnapshot = async () => {
  try {
    const res = await CameraApi.takeSnapshot(props.camera.deviceId, { quality: 2 })
    if (res && res.code === 0) {
      ElMessage.success('截图成功')
    }
  } catch (error) {
    ElMessage.error('截图失败')
  }
}

// 切换录像
const toggleRecord = async () => {
  try {
    if (isRecording.value) {
      await CameraApi.stopRecord(props.camera.deviceId)
      ElMessage.success('停止录像')
    } else {
      await CameraApi.startRecord(props.camera.deviceId, {
        duration: 300,
        quality: 1
      })
      ElMessage.success('开始录像')
    }
  } catch (error) {
    ElMessage.error('录像操作失败')
  }
}

// 全屏
const toggleFullscreen = () => {
  const container = videoRef.value?.parentElement
  if (!container) return

  if (!document.fullscreenElement) {
    container.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 加载预设点列表
const loadPresets = async () => {
  if (!props.camera.channelId) return
  try {
    const res = await PresetApi.getPresetListByChannelId(props.camera.channelId)
    presetList.value = res || []
    // 如果有预设点，默认选中第一个
    if (presetList.value.length > 0 && !selectedPreset.value) {
      selectedPreset.value = presetList.value[0].presetNo
    }
  } catch (error) {
    console.error('加载预设点列表失败:', error)
  }
}

// 打开预设点管理
const openPresetManager = () => {
  presetManagerRef.value?.open()
}

// 打开巡航管理
const openCruiseManager = () => {
  cruiseManagerRef.value?.open()
}

// 从预设点管理转到预设点
const handleGotoPreset = async (presetNo: number) => {
  selectedPreset.value = presetNo
  await gotoPreset()
}

onMounted(() => {
  console.log('摄像头播放器加载:', props.camera.name, props.streamUrl)
  loadPresets()
})

// 监听 channelId 变化重新加载预设点
watch(() => props.camera.channelId, () => {
  loadPresets()
})
</script>

<style lang="scss" scoped>
.camera-player {
  width: 100%;
  height: 100%;

  .player-container {
    display: flex;
    flex-direction: column;
    height: 100%;
    background: #000;
    border-radius: 8px;
    overflow: hidden;
  }

  .video-wrapper {
    position: relative;
    flex: 1;
    background: #1a1a2e;

    .video-player {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }

    .loading-overlay,
    .error-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.8);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #fff;

      .loading-icon,
      .error-icon {
        font-size: 48px;
        margin-bottom: 16px;
        animation: spin 1s linear infinite;
      }

      p {
        margin: 0;
        font-size: 14px;
      }
    }

    @keyframes spin {
      from {
        transform: rotate(0deg);
      }
      to {
        transform: rotate(360deg);
      }
    }

    .info-overlay {
      position: absolute;
      top: 16px;
      left: 16px;
      background: rgba(0, 0, 0, 0.7);
      padding: 12px;
      border-radius: 8px;
      color: #fff;
      font-size: 13px;

      .device-name {
        font-weight: bold;
        margin-bottom: 8px;
      }

      .device-status,
      .device-location {
        display: flex;
        align-items: center;
        gap: 6px;
        margin-bottom: 4px;

        &.online {
          color: #00ff88;
        }

        &.offline {
          color: #ff6b6b;
        }
      }
    }

    .ptz-control-panel {
      position: absolute;
      top: 16px;
      right: 16px;
      background: rgba(0, 0, 0, 0.8);
      padding: 16px;
      border-radius: 12px;

      .ptz-controls {
        display: grid;
        grid-template-columns: repeat(3, 40px);
        grid-template-rows: repeat(3, 40px);
        gap: 4px;
        margin-bottom: 12px;

        .ptz-btn {
          background: rgba(255, 255, 255, 0.1);
          border: 1px solid rgba(255, 255, 255, 0.2);
          border-radius: 6px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;
          cursor: pointer;
          transition: all 0.2s;
          user-select: none;

          &:hover {
            background: rgba(0, 212, 255, 0.3);
            border-color: #00d4ff;
          }

          &:active {
            background: rgba(0, 212, 255, 0.5);
          }

          &.ptz-up {
            grid-column: 2;
            grid-row: 1;
          }

          &.ptz-left {
            grid-column: 1;
            grid-row: 2;
          }

          &.ptz-center {
            grid-column: 2;
            grid-row: 2;
            cursor: default;
            background: rgba(0, 212, 255, 0.2);
          }

          &.ptz-right {
            grid-column: 3;
            grid-row: 2;
          }

          &.ptz-down {
            grid-column: 2;
            grid-row: 3;
          }
        }
      }

      .zoom-controls {
        display: flex;
        gap: 8px;
        margin-bottom: 12px;

        .zoom-btn {
          flex: 1;
          background: rgba(255, 255, 255, 0.1);
          border: 1px solid rgba(255, 255, 255, 0.2);
          border-radius: 6px;
          padding: 8px;
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 4px;
          color: #fff;
          cursor: pointer;
          font-size: 12px;
          transition: all 0.2s;

          &:hover {
            background: rgba(0, 212, 255, 0.3);
            border-color: #00d4ff;
          }
        }
      }

      .preset-controls {
        display: flex;
        gap: 8px;
        margin-bottom: 8px;

        :deep(.el-select) {
          flex: 1;
        }
      }

      .manage-controls {
        display: flex;
        flex-direction: column;
        gap: 6px;

        .el-button {
          margin: 0;
          font-size: 11px;
        }
      }
    }
  }

  .control-bar {
    padding: 12px;
    background: rgba(0, 0, 0, 0.9);
    display: flex;
    justify-content: center;
  }
}
</style>



















