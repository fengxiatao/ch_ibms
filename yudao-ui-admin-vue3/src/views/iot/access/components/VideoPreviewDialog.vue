<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="800px"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    @close="handleClose"
    @opened="handleOpened"
    class="video-preview-dialog"
  >
    <div class="video-container" v-loading="loading" element-loading-text="正在连接设备...">
      <!-- 视频播放区域 -->
      <div ref="playerContainer" class="player-wrapper">
        <video 
          ref="videoEl"
          class="video-element"
          muted 
          autoplay 
          playsinline
        ></video>
      </div>
      
      <!-- 错误提示 -->
      <div v-if="error" class="error-overlay">
        <el-result icon="error" :title="error">
          <template #extra>
            <el-button type="primary" @click="handleRetry">重试</el-button>
          </template>
        </el-result>
      </div>
      
      <!-- 离线提示 -->
      <div v-if="!online && !loading && !error" class="offline-overlay">
        <el-result icon="warning" title="设备离线">
          <template #sub-title>
            设备当前不在线，无法预览视频
          </template>
          <template #extra>
            <el-button type="primary" @click="handleRetry">重试连接</el-button>
          </template>
        </el-result>
      </div>
      
      <!-- 播放状态指示 -->
      <div v-if="playing" class="status-indicator">
        <span class="status-dot"></span>
        <span>实时预览中</span>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <div class="left-actions">
          <el-button @click="handleSnapshot" :disabled="!playing">
            <Icon icon="ep:camera" class="mr-1" />
            截图
          </el-button>
          <el-button @click="handleFullscreen" :disabled="!playing">
            <Icon icon="ep:full-screen" class="mr-1" />
            全屏
          </el-button>
        </div>
        <div class="right-actions">
          <el-button @click="handleClose">关闭</el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getLivePlayUrl, stopStream } from '@/api/iot/video/zlm'
import mpegts from 'mpegts.js'

/** 组件属性 */
const props = defineProps<{
  modelValue: boolean
  deviceId: number | null
  deviceName?: string
  channelNo?: number
  channelId?: number  // ZLMediaKit 使用 channelId
  playParams?: {
    wsURL: string
    rtspURL: string
    username: string
    password: string
    target: string
    online: boolean
  } | null
}>()

/** 组件事件 */
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'close'): void
}>()

// 响应式状态
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const playing = ref(false)
const error = ref<string | null>(null)
const online = ref(true)
const reconnectCount = ref(0)
const maxReconnectAttempts = 3

// DOM 引用
const playerContainer = ref<HTMLElement | null>(null)
const videoEl = ref<HTMLVideoElement | null>(null)
let player: mpegts.Player | null = null

// 智能路由
const isIntranetAccess = (): boolean => {
  const hostname = window.location.hostname
  if (hostname === 'localhost' || hostname === '127.0.0.1') return true
  if (hostname.startsWith('192.168.')) return true
  if (hostname.startsWith('10.')) return true
  if (hostname.startsWith('172.')) {
    const secondOctet = parseInt(hostname.split('.')[1])
    if (secondOctet >= 16 && secondOctet <= 31) return true
  }
  return false
}

const adaptPlayUrls = (urls: any): any => {
  if (!urls) return urls
  const intranet = isIntranetAccess()
  if (intranet) return urls
  
  const adapted = { ...urls }
  const publicHost = window.location.hostname
  const publicPort = window.location.port ? parseInt(window.location.port) : 80
  const publicAddr = publicPort === 80 || publicPort === 443 ? publicHost : `${publicHost}:${publicPort}`
  const isHttps = window.location.protocol === 'https:'
  const wsProtocol = isHttps ? 'wss' : 'ws'
  
  if (urls.wsFlvUrl) {
    let newUrl = urls.wsFlvUrl
      .replace(/192\.168\.\d+\.\d+:\d+/g, publicAddr)
      .replace(/192\.168\.\d+\.\d+/g, publicHost)
    newUrl = newUrl.replace(/^ws:/, `${wsProtocol}:`)
    adapted.wsFlvUrl = newUrl
  }
  
  return adapted
}

// 计算属性
const dialogTitle = computed(() => {
  const name = props.deviceName || '门禁设备'
  const channel = props.channelNo === 0 ? '内置摄像头' : `通道${(props.channelNo || 0) + 1}`
  return `视频预览 - ${name} (${channel})`
})

/** 弹窗打开后初始化播放 */
const handleOpened = async () => {
  online.value = props.playParams?.online !== false
  
  if (!online.value) {
    return
  }
  
  await startPlay()
}

/** 开始播放 */
const startPlay = async () => {
  if (!videoEl.value) {
    error.value = '播放器初始化失败'
    return
  }
  
  loading.value = true
  error.value = null
  
  try {
    // 使用 channelId 获取播放地址
    const channelId = props.channelId || props.deviceId
    if (!channelId) {
      throw new Error('缺少通道ID')
    }
    
    const rawPlayUrls = await getLivePlayUrl(channelId, 1) // 子码流
    const playUrls = adaptPlayUrls(rawPlayUrls)
    
    if (!playUrls?.wsFlvUrl) {
      throw new Error('未获取到播放地址')
    }
    
    await nextTick()
    
    if (!mpegts.isSupported()) {
      throw new Error('浏览器不支持视频播放')
    }
    
    player = mpegts.createPlayer({
      type: 'flv',
      url: playUrls.wsFlvUrl,
      isLive: true,
      hasAudio: false,
      hasVideo: true
    }, {
      enableWorker: false,
      enableStashBuffer: false,
      stashInitialSize: 128,
      lazyLoad: false,
      autoCleanupSourceBuffer: false,
      liveBufferLatencyChasing: true,
      liveBufferLatencyMaxLatency: 1.5,
      liveSync: true
    })
    
    player.attachMediaElement(videoEl.value)
    player.load()
    
    player.on(mpegts.Events.ERROR, (errorType: any, errorDetail: any) => {
      if (String(errorDetail || '').includes('SourceBuffer')) return
      error.value = `播放错误: ${errorDetail}`
      playing.value = false
      loading.value = false
    })
    
    await player.play()
    
    playing.value = true
    reconnectCount.value = 0
    
  } catch (e: any) {
    console.error('[门禁视频预览] 播放失败:', e)
    error.value = e.message || '视频播放失败'
    
    // 自动重试
    if (reconnectCount.value < maxReconnectAttempts) {
      reconnectCount.value++
      setTimeout(() => startPlay(), 2000)
    }
  } finally {
    loading.value = false
  }
}

/** 停止播放 */
const stopPlay = () => {
  if (player) {
    try {
      player.pause()
      player.unload()
      player.detachMediaElement()
      player.destroy()
    } catch {}
    player = null
  }
  
  if (videoEl.value) {
    videoEl.value.srcObject = null
    videoEl.value.src = ''
  }
  
  playing.value = false
}

/** 重试 */
const handleRetry = () => {
  reconnectCount.value = 0
  error.value = null
  startPlay()
}

/** 截图 */
const handleSnapshot = () => {
  if (!videoEl.value || !playing.value) {
    ElMessage.warning('无法截图')
    return
  }
  
  try {
    const video = videoEl.value
    const canvas = document.createElement('canvas')
    canvas.width = video.videoWidth || 640
    canvas.height = video.videoHeight || 480
    
    const ctx = canvas.getContext('2d')
    if (ctx) {
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
      
      const link = document.createElement('a')
      link.download = `门禁截图_${props.deviceName || props.deviceId}_${Date.now()}.png`
      link.href = canvas.toDataURL('image/png')
      link.click()
      
      ElMessage.success('截图已保存')
    }
  } catch (e) {
    console.error('[门禁视频预览] 截图失败:', e)
    ElMessage.error('截图失败')
  }
}

/** 全屏 */
const handleFullscreen = () => {
  if (!playerContainer.value) return
  
  const container = playerContainer.value
  if (container.requestFullscreen) {
    container.requestFullscreen()
  } else if ((container as any).webkitRequestFullscreen) {
    (container as any).webkitRequestFullscreen()
  } else if ((container as any).mozRequestFullScreen) {
    (container as any).mozRequestFullScreen()
  }
}

/** 关闭弹窗 */
const handleClose = () => {
  stopPlay()
  error.value = null
  reconnectCount.value = 0
  emit('update:modelValue', false)
  emit('close')
}

// 监听弹窗关闭
watch(() => props.modelValue, (val) => {
  if (!val) {
    stopPlay()
  }
})

// 组件卸载时清理资源
onUnmounted(() => {
  stopPlay()
})
</script>

<style scoped lang="scss">
.video-preview-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.video-container {
  position: relative;
  width: 100%;
  height: 450px;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.player-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.video-element {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #000;
}

.error-overlay,
.offline-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.8);
  
  :deep(.el-result) {
    padding: 20px;
    
    .el-result__title {
      color: #fff;
    }
    
    .el-result__subtitle {
      color: #ccc;
    }
  }
}

.status-indicator {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #67c23a;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-top: 1px solid #eee;
}

.left-actions,
.right-actions {
  display: flex;
  gap: 10px;
}
</style>
