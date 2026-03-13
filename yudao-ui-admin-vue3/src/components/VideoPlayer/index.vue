<template>
  <div class="video-player" :id="containerId" ref="containerRef">
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
import useZlmPlayer, { type ZlmPlayerInstance } from '@/composables/useZlmPlayer'

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
const loading = ref(false)
const error = ref('')
let instance: ZlmPlayerInstance | null = null

const { playLive, stopInstance } = useZlmPlayer()

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
  // 本地开发环境不做“外网替换”，否则会把 192.168.* 的流媒体地址替换成 localhost:3000 导致 404
  if (publicHost === 'localhost' || publicHost === '127.0.0.1') {
    return urls
  }
  const publicPort = window.location.port ? parseInt(window.location.port) : 80
  const publicAddr = publicPort === 80 || publicPort === 443 ? publicHost : `${publicHost}:${publicPort}`
  const isHttps = window.location.protocol === 'https:'
  const httpProtocol = isHttps ? 'https' : 'http'
  const wsProtocol = isHttps ? 'wss' : 'ws'

  const envRtcHost = (import.meta as any).env?.VITE_ZLM_RTC_HOST as string | undefined
  const envRtcPortRaw = (import.meta as any).env?.VITE_ZLM_RTC_PORT as string | undefined
  const envRtcPort = envRtcPortRaw ? parseInt(envRtcPortRaw) : undefined
  const envSecret = (import.meta as any).env?.VITE_ZLM_SECRET as string | undefined
  
  if (urls.wsFlvUrl) {
    let newUrl = urls.wsFlvUrl
      .replace(/192\.168\.\d+\.\d+:\d+/g, publicAddr)
      .replace(/192\.168\.\d+\.\d+/g, publicHost)
    newUrl = newUrl.replace(/^ws:/, `${wsProtocol}:`)
    adapted.wsFlvUrl = newUrl
  }

  if (urls.webrtcUrl) {
    let newUrl = urls.webrtcUrl
      .replace(/192\.168\.\d+\.\d+:\d+/g, publicAddr)
      .replace(/192\.168\.\d+\.\d+/g, publicHost)
    newUrl = newUrl.replace(/^http:/, `${httpProtocol}:`)
    try {
      const u = new URL(newUrl)
      if (envRtcHost) u.hostname = envRtcHost
      if (envRtcPort && !Number.isNaN(envRtcPort)) u.port = String(envRtcPort)
      if (envSecret && !u.searchParams.get('secret')) u.searchParams.set('secret', envSecret)
      newUrl = u.toString()
    } catch {
      // ignore
    }
    if (envRtcHost) {
      try {
        const u = new URL(newUrl)
        u.hostname = envRtcHost
        if (envRtcPort && !Number.isNaN(envRtcPort)) u.port = String(envRtcPort)
        newUrl = u.toString()
      } catch {}
    }
    adapted.webrtcUrl = newUrl
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
    
    if (!playUrls?.wsFlvUrl && !playUrls?.webrtcUrl) {
      throw new Error('未获取到播放地址')
    }
    
    await nextTick()
    const container = containerRef.value
    if (!container) {
      throw new Error('视频元素未找到')
    }

    instance = await playLive({
      container,
      urls: { wsFlvUrl: playUrls.wsFlvUrl, webrtcUrl: playUrls.webrtcUrl },
      preferWebrtc:
        (() => {
          try {
            return new URLSearchParams(window.location.search).get('forceWebrtc') === '1'
              ? true
              : !isIntranetAccess()
          } catch {
            return !isIntranetAccess()
          }
        })()
    })
    
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
  if (instance) {
    stopInstance(instance)
    instance = null
    emit('playStop')
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
