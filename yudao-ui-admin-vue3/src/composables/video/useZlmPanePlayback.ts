import type { ZlmPlayerInstance } from '@/composables/useZlmPlayer'
import useZlmPlayer from '@/composables/useZlmPlayer'
import { getLivePlayUrl, stopStream } from '@/api/iot/video/zlm'
import type {
  IbmsChannel,
  PlayerPane
} from '@/views/security/VideoSurveillance/RealTimePreview/types'
import { adaptStreamPlayUrls, getDefaultPreferWebrtc } from './streamPlayUtils'

export interface ZlmPanePlayback extends PlayerPane {
  zlmInstance?: ZlmPlayerInstance | null
  zlmChannelId?: number | null
  ibmsChannel?: IbmsChannel | null
}

interface ReplacePanePlaybackOptions {
  pane: ZlmPanePlayback
  paneIndex: number
  ibmsChannel: IbmsChannel
  resolvePaneContainer: (paneIndex: number, pane: ZlmPanePlayback) => Promise<HTMLElement | null> | HTMLElement | null
}

const resetPaneFlags = (pane: ZlmPanePlayback) => {
  pane.isPlaying = false
  pane.isLoading = false
  pane.isRecording = false
  pane.error = null
}

const waitForJessibucaRelease = async () => {
  await new Promise<void>((resolve) => setTimeout(resolve, 100))
}

export const useZlmPanePlayback = () => {
  const { playLive, stopInstance } = useZlmPlayer()

  const stopPanePlayback = async (pane: ZlmPanePlayback) => {
    if (!pane) {
      return
    }

    if (pane.zlmInstance) {
      await stopInstance(pane.zlmInstance)
      pane.zlmInstance = null
    }

    if (pane.container) {
      await waitForJessibucaRelease()
      pane.container.innerHTML = ''
    }

    if (pane.zlmChannelId) {
      stopStream(pane.zlmChannelId).catch(() => {})
      pane.zlmChannelId = null
    }

    resetPaneFlags(pane)
  }

  const playPaneChannel = async ({ pane, paneIndex, ibmsChannel, resolvePaneContainer }: ReplacePanePlaybackOptions) => {
    const channelId = ibmsChannel.id
    if (!channelId) {
      throw new Error('通道ID缺失，无法通过流媒体播放')
    }

    pane.ibmsChannel = ibmsChannel
    pane.isLoading = true
    pane.isPlaying = false
    pane.error = null

    try {
      const rawUrls = await getLivePlayUrl(channelId, pane.streamType === 'sub' ? 1 : 0)
      const playUrls = adaptStreamPlayUrls(rawUrls)

      if (!playUrls || (!playUrls.wsFlvUrl && !playUrls.flvUrl && !playUrls.webrtcUrl)) {
        throw new Error('未获取到可用的播放地址')
      }

      const container = await resolvePaneContainer(paneIndex, pane)
      if (!container) {
        throw new Error('播放器容器不存在')
      }

      pane.container = container
      const instance = await playLive({
        container,
        urls: {
          wsFlvUrl: playUrls.wsFlvUrl,
          flvUrl: playUrls.flvUrl,
          webrtcUrl: playUrls.webrtcUrl
        },
        preferWebrtc: getDefaultPreferWebrtc()
      })

      pane.zlmInstance = instance
      pane.zlmChannelId = channelId
      pane.isPlaying = true
      pane.isLoading = false
      pane.error = null
    } catch (error: any) {
      pane.isLoading = false
      pane.isPlaying = false
      pane.error = error?.message || '播放失败，请稍后重试'
      throw error
    }
  }

  const replacePanePlayback = async (options: ReplacePanePlaybackOptions) => {
    await stopPanePlayback(options.pane)
    await playPaneChannel(options)
  }

  const stopAllPanes = async (panes: ZlmPanePlayback[]) => {
    for (const pane of panes.slice()) {
      await stopPanePlayback(pane)
    }
  }

  const retryPanePlayback = async (options: Omit<ReplacePanePlaybackOptions, 'ibmsChannel'>) => {
    const ibmsChannel = options.pane.ibmsChannel
    if (!ibmsChannel) {
      throw new Error('当前窗口缺少通道信息，无法重试播放')
    }
    await replacePanePlayback({
      ...options,
      ibmsChannel
    })
  }

  const switchPaneStream = async (
    options: Omit<ReplacePanePlaybackOptions, 'ibmsChannel'> & { streamType: 'main' | 'sub' }
  ) => {
    const ibmsChannel = options.pane.ibmsChannel
    if (!ibmsChannel) {
      throw new Error('当前窗口缺少通道信息，无法切换码流')
    }

    options.pane.streamType = options.streamType
    await replacePanePlayback({
      pane: options.pane,
      paneIndex: options.paneIndex,
      ibmsChannel,
      resolvePaneContainer: options.resolvePaneContainer
    })
  }

  return {
    stopPanePlayback,
    stopAllPanes,
    replacePanePlayback,
    retryPanePlayback,
    switchPaneStream
  }
}
