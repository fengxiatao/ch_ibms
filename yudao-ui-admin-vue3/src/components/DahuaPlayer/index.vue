<template>
  <div class="dahua-player" ref="containerRef">
    <!-- Canvas 播放器（软解） -->
    <canvas ref="canvasRef" class="player-canvas"></canvas>
    <!-- Video 播放器（硬解） -->
    <video ref="videoRef" class="player-video"></video>
    <!-- 加载提示 -->
    <div v-if="loading" class="player-loading">
      <Icon icon="ep:loading" class="loading-icon" />
      <span>{{ loadingText }}</span>
    </div>
    <!-- 错误提示 -->
    <div v-if="error" class="player-error">
      <Icon icon="ep:warning" />
      <span>{{ error }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { Icon } from '@/components/Icon'

// Props
interface Props {
  /** 设备 IP */
  ip: string
  /** 设备端口（默认 80） */
  port?: number
  /** 用户名 */
  username: string
  /** 密码 */
  password: string
  /** 通道号（默认 1） */
  channel?: number
  /** 码流类型：0=主码流，1=子码流（默认 1） */
  subtype?: number
  /** 是否自动播放 */
  autoPlay?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  port: 80,
  channel: 1,
  subtype: 1,
  autoPlay: false
})

// Emits
const emit = defineEmits<{
  (e: 'play'): void
  (e: 'stop'): void
  (e: 'error', error: string): void
}>()

// Refs
const containerRef = ref<HTMLDivElement>()
const canvasRef = ref<HTMLCanvasElement>()
const videoRef = ref<HTMLVideoElement>()

// State
const loading = ref(false)
const loadingText = ref('加载中...')
const error = ref('')
const isPlaying = ref(false)

// Player instance
let player: any = null

/**
 * 播放视频
 */
const play = async () => {
  if (isPlaying.value) {
    await stop()
  }
  
  if (!canvasRef.value || !videoRef.value) {
    error.value = '播放器容器未就绪'
    return
  }
  
  // @ts-ignore
  if (typeof window.PlayerControl === 'undefined') {
    error.value = '大华播放器 SDK 未加载'
    emit('error', error.value)
    return
  }
  
  loading.value = true
  loadingText.value = '连接中...'
  error.value = ''
  
  try {
    const { ip, port, username, password, channel, subtype } = props
    
    const options = {
      wsURL: `ws://${ip}:${port}/rtspoverwebsocket`,
      rtspURL: `rtsp://${ip}:${port}/cam/realmonitor?channel=${channel}&subtype=${subtype}&proto=Private3`,
      username,
      password,
      lessRateCanvas: true,
      h265AccelerationEnabled: true
    }
    
    console.log('[大华播放器] 开始连接:', options.rtspURL)
    
    // @ts-ignore
    player = new window.PlayerControl(options)
    
    // 播放器就绪
    player.on('WorkerReady', () => {
      console.log('[大华播放器] Worker 就绪，开始连接')
      player.connect()
    })
    
    // 开始解码
    player.on('DecodeStart', (e: any) => {
      console.log('[大华播放器] 开始解码:', e)
      // 根据解码模式显示不同元素
      if (e.decodeMode === 'video') {
        // 硬解：使用 video 元素
        canvasRef.value!.style.display = 'none'
        videoRef.value!.style.display = 'block'
      } else {
        // 软解：使用 canvas 元素
        videoRef.value!.style.display = 'none'
        canvasRef.value!.style.display = 'block'
      }
    })
    
    // 播放开始
    player.on('PlayStart', () => {
      console.log('[大华播放器] 播放开始')
      loading.value = false
      isPlaying.value = true
      error.value = ''
      emit('play')
    })
    
    // 分辨率变化
    player.on('MSEResolutionChanged', (e: any) => {
      console.log('[大华播放器] 分辨率变化:', e)
    })
    
    // 错误处理
    player.on('Error', (e: any) => {
      console.error('[大华播放器] 错误:', e)
      loading.value = false
      error.value = e?.message || '播放错误'
      emit('error', error.value)
    })
    
    // 初始化播放器
    player.init(canvasRef.value, videoRef.value)
    
    // 超时检测
    setTimeout(() => {
      if (loading.value && !isPlaying.value) {
        loading.value = false
        error.value = '连接超时'
        emit('error', error.value)
      }
    }, 15000)
    
  } catch (e: any) {
    console.error('[大华播放器] 初始化失败:', e)
    loading.value = false
    error.value = e?.message || '初始化失败'
    emit('error', error.value)
  }
}

/**
 * 停止播放
 */
const stop = async () => {
  if (player) {
    try {
      player.stop?.()
      player.close?.()
    } catch (e) {
      console.warn('[大华播放器] 停止异常:', e)
    }
    player = null
  }
  
  isPlaying.value = false
  loading.value = false
  error.value = ''
  
  // 隐藏播放元素
  if (canvasRef.value) {
    canvasRef.value.style.display = 'none'
  }
  if (videoRef.value) {
    videoRef.value.style.display = 'none'
  }
  
  emit('stop')
}

/**
 * 暂停播放
 */
const pause = () => {
  player?.pause?.()
}

/**
 * 恢复播放
 */
const resume = () => {
  player?.play?.()
}

/**
 * 截图
 */
const capture = (filename?: string) => {
  if (player) {
    player.capture(filename || 'snapshot')
  }
}

/**
 * 获取截图数据
 */
const getCapture = (type: 'jpg' | 'png' = 'jpg', quality: number = 1.0) => {
  return player?.getCapture?.(type, quality)
}

/**
 * 设置音量
 */
const setVolume = (vol: number) => {
  player?.setAudioVolume?.(vol)
}

/**
 * 云台控制
 */
const ptzControl = (command: string, speed: number = 5, isStop: boolean = false) => {
  // 这里需要 RPC 登录后才能使用，暂时返回
  console.log('[大华播放器] 云台控制:', command, speed, isStop)
}

// 自动播放
watch(() => props.autoPlay, (val) => {
  if (val && props.ip) {
    play()
  }
}, { immediate: true })

// 监听设备信息变化
watch(() => [props.ip, props.channel, props.subtype], () => {
  if (isPlaying.value) {
    // 设备信息变化时重新播放
    play()
  }
})

// 生命周期
onBeforeUnmount(() => {
  stop()
})

// 暴露方法
defineExpose({
  play,
  stop,
  pause,
  resume,
  capture,
  getCapture,
  setVolume,
  ptzControl,
  isPlaying: () => isPlaying.value
})
</script>

<style lang="scss" scoped>
.dahua-player {
  position: relative;
  width: 100%;
  height: 100%;
  background: #000;
  overflow: hidden;
}

.player-canvas,
.player-video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: none;
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
  gap: 8px;
  color: #fff;
  font-size: 14px;
}

.player-error {
  color: #f56c6c;
}

.loading-icon {
  animation: rotating 1.5s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
