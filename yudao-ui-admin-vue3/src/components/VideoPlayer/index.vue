<template>
  <div class="video-player" :id="containerId" ref="containerRef">
    <video class="video-element" ref="videoRef" autoplay muted playsinline></video>
    <div v-if="loading" class="player-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    <div v-if="error" class="player-error">
      <el-icon><CircleClose /></el-icon>
      <span>{{ error }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { Loading, CircleClose } from '@element-plus/icons-vue'
import { getLivePlayUrl, stopStream } from '@/api/iot/video/zlm'
import mpegts from 'mpegts.js'

// Props
interface Props {
  channel?: any
  containerId?: string
  wndIndex?: number
  autoPlay?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  containerId: () => `video-player-${Date.now()}`,
  wndIndex: 0,
  autoPlay: false
})

// Emits
const emit = defineEmits<{
  playStart: []
  playStop: []
  error: [error: any]
}>()

// 响应式数据
const containerRef = ref<HTMLElement>()
const videoRef = ref<HTMLVideoElement>()
const loading = ref(false)
const error = ref('')
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

// 播放视频
const play = async (channelData?: any) => {
  const targetChannel = channelData || props.channel
  
  if (!targetChannel) {
    error.value = '缺少通道数据'
    return
  }

  try {
    loading.value = true
    error.value = ''

    // 停止之前的播放
    stop()

    const channelId = targetChannel.id || targetChannel.channelId
    const rawPlayUrls = await getLivePlayUrl(channelId, 1) // 默认子码流
    const playUrls = adaptPlayUrls(rawPlayUrls)
    
    if (!playUrls?.wsFlvUrl) {
      throw new Error('未获取到播放地址')
    }
    
    await nextTick()
    const videoEl = videoRef.value
    if (!videoEl) {
      throw new Error('视频元素未找到')
    }
    
    if (!mpegts.isSupported()) {
      throw new Error('浏览器不支持 FLV 播放')
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
    
    player.attachMediaElement(videoEl)
    player.load()
    
    player.on(mpegts.Events.ERROR, (errorType: any, errorDetail: any) => {
      if (String(errorDetail || '').includes('SourceBuffer')) return
      error.value = `播放错误: ${errorDetail}`
      loading.value = false
      emit('error', { type: errorType, detail: errorDetail })
    })
    
    await player.play()
    
    loading.value = false
    emit('playStart')
    
  } catch (e: any) {
    loading.value = false
    error.value = e?.message || '播放失败'
    emit('error', e)
    console.error('[VideoPlayer] 播放失败:', e)
  }
}

// 停止播放
const stop = () => {
  if (player) {
    try {
      player.pause()
      player.unload()
      player.detachMediaElement()
      player.destroy()
    } catch {}
    player = null
    emit('playStop')
  }
  
  if (videoRef.value) {
    videoRef.value.srcObject = null
    videoRef.value.src = ''
  }
  
  loading.value = false
  error.value = ''
}

// 监听 channel 变化
watch(() => props.channel, async (newChannel) => {
  if (newChannel && props.autoPlay) {
    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    play(newChannel)
  }
}, { immediate: props.autoPlay })

// 组件挂载
onMounted(async () => {
  if (props.autoPlay && props.channel) {
    play()
  }
})

// 组件卸载
onBeforeUnmount(() => {
  stop()
})

// 暴露方法
defineExpose({
  play,
  stop
})
</script>

<style scoped>
.video-player {
  position: relative;
  width: 100%;
  height: 100%;
  background: #000;
  overflow: hidden;
}

.video-element {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.player-loading,
.player-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #fff;
  font-size: 14px;
}

.player-error {
  color: #f56c6c;
}

.is-loading {
  font-size: 24px;
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
