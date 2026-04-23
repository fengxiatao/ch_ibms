import { nextTick, ref } from 'vue'
import useZlmPlayer, { type ZlmPlayUrls, type ZlmPlayerInstance } from '@/composables/useZlmPlayer'

export interface StreamRendererUrls extends ZlmPlayUrls {
  hlsUrl?: string
}

export const useStreamRenderer = () => {
  const containerRef = ref<HTMLElement>()
  const nativeVideoRef = ref<HTMLVideoElement>()
  const loading = ref(false)
  const errorMessage = ref('')
  let playerInstance: ZlmPlayerInstance | null = null

  const { playLive, stopInstance } = useZlmPlayer()

  const stop = () => {
    if (nativeVideoRef.value) {
      nativeVideoRef.value.pause()
      nativeVideoRef.value.removeAttribute('src')
      nativeVideoRef.value.load()
    }
    if (playerInstance) {
      stopInstance(playerInstance)
      playerInstance = null
    }
    errorMessage.value = ''
    loading.value = false
  }

  const play = async (urls?: StreamRendererUrls | null, preferWebrtc = true) => {
    stop()
    errorMessage.value = ''

    if (!urls?.wsFlvUrl && !urls?.webrtcUrl && !urls?.hlsUrl) {
      return
    }

    if (!urls?.wsFlvUrl && !urls?.webrtcUrl && urls?.hlsUrl) {
      await nextTick()
      if (nativeVideoRef.value) {
        try {
          nativeVideoRef.value.src = urls.hlsUrl
          await nativeVideoRef.value.play()
        } catch {}
      }
      return
    }

    if (!containerRef.value) {
      errorMessage.value = '播放器容器未就绪'
      return
    }

    loading.value = true
    try {
      playerInstance = await playLive({
        container: containerRef.value,
        urls: {
          wsFlvUrl: urls?.wsFlvUrl,
          webrtcUrl: urls?.webrtcUrl
        },
        preferWebrtc
      })
    } catch (error: any) {
      errorMessage.value = error?.message || '视频流播放失败'
      throw error
    } finally {
      loading.value = false
    }
  }

  return {
    containerRef,
    nativeVideoRef,
    loading,
    errorMessage,
    play,
    stop
  }
}
