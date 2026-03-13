<template>
  <div ref="containerRef" class="stream-playback-player"></div>
</template>

<script setup lang="ts">
import { ref, watch, onBeforeUnmount, nextTick } from 'vue'
import useZlmPlayer, { type ZlmPlayerInstance } from '@/composables/useZlmPlayer'

const props = defineProps<{
  flvUrl?: string
  webrtcUrl?: string
  preferWebrtc?: boolean
}>()


const containerRef = ref<HTMLElement>()
let instance: ZlmPlayerInstance | null = null
const { playLive, stopInstance } = useZlmPlayer()

function toWsFlvUrl(httpFlvUrl: string): string {
  if (!httpFlvUrl) return ''
  return httpFlvUrl.replace(/^http:\/\//, 'ws://').replace(/^https:\/\//, 'wss://')
}

async function play() {
  if (!containerRef.value || (!props.flvUrl && !props.webrtcUrl)) return
  stop()
  const wsFlvUrl = props.flvUrl ? toWsFlvUrl(props.flvUrl) : ''
  try {
    instance = await playLive({
      container: containerRef.value,
      urls: { wsFlvUrl, webrtcUrl: props.webrtcUrl },
      preferWebrtc: props.preferWebrtc ?? true
    })
  } catch (e: any) {
    console.error('[StreamPlaybackPlayer] 播放失败:', e)
  }
}

function stop() {
  if (instance) {
    stopInstance(instance)
    instance = null
  }
}

watch(
  () => [props.flvUrl, props.webrtcUrl],
  ([a, b]) => {
    if (a || b) {
      nextTick(() => play())
    } else {
      stop()
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  stop()
})

defineExpose({ play, stop })
</script>

<style scoped>
.stream-playback-player {
  width: 100%;
  height: 100%;
  background: #000;
  overflow: hidden;
}
</style>
