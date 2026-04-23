<template>
  <div class="video-player" :id="containerId" ref="containerRef">
    <div v-if="loading" class="player-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    <div v-if="errorMessage" class="player-error">
      <el-icon><CircleClose /></el-icon>
      <span>{{ errorMessage }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { Loading, CircleClose } from '@element-plus/icons-vue'
import { getLivePlayUrl } from '@/api/iot/video/zlm'
import {
  adaptStreamPlayUrls,
  getDefaultPreferWebrtc
} from '@/composables/video/streamPlayUtils'
import { useStreamRenderer } from '@/composables/video/useStreamRenderer'

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

const { containerRef, loading, errorMessage, play: renderStream, stop: stopRenderer } = useStreamRenderer()

// 播放视频
const play = async (channelData?: any) => {
  const targetChannel = channelData || props.channel
  
  if (!targetChannel) {
    errorMessage.value = '缺少通道数据'
    return
  }

  try {
    loading.value = true
    errorMessage.value = ''

    // 停止之前的播放
    stop()

    const channelId = targetChannel.id || targetChannel.channelId
    const rawPlayUrls = await getLivePlayUrl(channelId, 1) // 默认子码流
    const playUrls = adaptStreamPlayUrls(rawPlayUrls)
    
    if (!playUrls?.wsFlvUrl && !playUrls?.webrtcUrl) {
      throw new Error('未获取到播放地址')
    }
    
    await nextTick()
    await renderStream(
      { wsFlvUrl: playUrls.wsFlvUrl, webrtcUrl: playUrls.webrtcUrl, hlsUrl: playUrls.hlsUrl },
      getDefaultPreferWebrtc()
    )
    
    loading.value = false
    emit('playStart')
    
  } catch (e: any) {
    loading.value = false
    errorMessage.value = e?.message || '播放失败'
    emit('error', e)
    console.error('[VideoPlayer] 播放失败:', e)
  }
}

// 停止播放
const stop = () => {
  stopRenderer()
  emit('playStop')
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
