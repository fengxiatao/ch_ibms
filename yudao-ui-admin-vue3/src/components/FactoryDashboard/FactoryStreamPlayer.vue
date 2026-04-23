<template>
  <div class="factory-stream-player">
    <video
      v-if="useNativeVideo"
      ref="nativeVideoRef"
      class="factory-stream-player__native"
      :src="props.hlsUrl"
      autoplay
      muted
      controls
      playsinline
    ></video>
    <div v-else ref="containerRef" class="factory-stream-player__canvas"></div>

    <div v-if="loading" class="factory-stream-player__overlay">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>视频流加载中...</span>
    </div>

    <div v-else-if="errorMessage" class="factory-stream-player__overlay factory-stream-player__overlay--error">
      <el-icon><WarningFilled /></el-icon>
      <span>{{ errorMessage }}</span>
    </div>

    <div v-else-if="!hasPlayableUrl" class="factory-stream-player__overlay factory-stream-player__overlay--empty">
      <el-icon><VideoCameraFilled /></el-icon>
      <span>{{ props.emptyText }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Loading, VideoCameraFilled, WarningFilled } from '@element-plus/icons-vue'
import { computed, nextTick, onBeforeUnmount, watch } from 'vue'
import { useStreamRenderer } from '@/composables/video/useStreamRenderer'

defineOptions({ name: 'FactoryStreamPlayer' })

const props = withDefaults(
  defineProps<{
    wsFlvUrl?: string
    webrtcUrl?: string
    hlsUrl?: string
    preferWebrtc?: boolean
    emptyText?: string
  }>(),
  {
    wsFlvUrl: '',
    webrtcUrl: '',
    hlsUrl: '',
    preferWebrtc: true,
    emptyText: '等待联动视频流'
  }
)

const { containerRef, nativeVideoRef, loading, errorMessage, play, stop } = useStreamRenderer()

const hasPlayableUrl = computed(() => Boolean(props.wsFlvUrl || props.webrtcUrl || props.hlsUrl))
const useNativeVideo = computed(() => !props.wsFlvUrl && !props.webrtcUrl && Boolean(props.hlsUrl))

watch(
  () => [props.wsFlvUrl, props.webrtcUrl, props.hlsUrl, props.preferWebrtc],
  async () => {
    await nextTick()
    play(
      {
        wsFlvUrl: props.wsFlvUrl || undefined,
        webrtcUrl: props.webrtcUrl || undefined,
        hlsUrl: props.hlsUrl || undefined
      },
      props.preferWebrtc
    )
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  stop()
})
</script>

<style scoped lang="scss">
.factory-stream-player {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 360px;
  overflow: hidden;
  border-radius: 18px;
  background:
    radial-gradient(circle at top right, rgba(0, 214, 255, 0.12), transparent 28%),
    linear-gradient(180deg, rgba(3, 10, 18, 0.98), rgba(6, 15, 28, 0.98));
}

.factory-stream-player__canvas,
.factory-stream-player__native {
  width: 100%;
  height: 100%;
  background: #02070d;
}

.factory-stream-player__native {
  object-fit: cover;
}

.factory-stream-player__overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #e5f8ff;
  background: rgba(3, 10, 18, 0.72);
  backdrop-filter: blur(6px);
}

.factory-stream-player__overlay--error {
  color: #ffc1bb;
}

.factory-stream-player__overlay--empty {
  color: rgba(216, 238, 255, 0.74);
}
</style>
