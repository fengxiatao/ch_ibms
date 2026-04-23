import type {
  DahuaPlayerConfig,
  DahuaPlayerPane
} from '@/composables/useDahuaPlayer'
import { DEFAULT_NVR_CONFIG } from '@/composables/useDahuaPlayer'
import type { IbmsChannel } from '@/views/security/VideoSurveillance/RealTimePreview/types'

export interface DahuaPanePlayback extends DahuaPlayerPane {
  ibmsChannel?: IbmsChannel | null
}

export interface DahuaPaneSource {
  channelNo: number
  channelName?: string
  username?: string
  password?: string
  ibmsChannel?: IbmsChannel | null
}

interface DahuaPanePlaybackOptions {
  pane: DahuaPanePlayback
  paneIndex: number
  source: DahuaPaneSource
  resolvePaneContainer: (
    paneIndex: number,
    pane: DahuaPanePlayback
  ) => Promise<HTMLElement | null> | HTMLElement | null
  startPreview: (
    pane: DahuaPlayerPane,
    config: DahuaPlayerConfig,
    channelName?: string
  ) => Promise<boolean>
  stopPlayer: (pane: DahuaPlayerPane) => Promise<void>
  stopAllPlayers: (panes: DahuaPlayerPane[]) => Promise<void>
  switchStream: (pane: DahuaPlayerPane, streamType: 'main' | 'sub') => Promise<boolean>
}

const buildDefaultConfig = (pane: DahuaPanePlayback, source: DahuaPaneSource): DahuaPlayerConfig => ({
  ip: DEFAULT_NVR_CONFIG.ip,
  port: DEFAULT_NVR_CONFIG.port,
  rtspPort: DEFAULT_NVR_CONFIG.rtspPort,
  username: source.username || DEFAULT_NVR_CONFIG.username,
  password: source.password || DEFAULT_NVR_CONFIG.password,
  channelNo: source.channelNo,
  subtype: pane.streamType === 'sub' ? 1 : 0
})

const buildSourceFromPane = (pane: DahuaPanePlayback): DahuaPaneSource | null => {
  if (pane.ibmsChannel?.channelNo) {
    return {
      channelNo: pane.ibmsChannel.channelNo,
      channelName: pane.ibmsChannel.channelName,
      username: pane.ibmsChannel.username,
      password: pane.ibmsChannel.password,
      ibmsChannel: pane.ibmsChannel
    }
  }

  if (!pane.config?.channelNo) {
    return null
  }

  return {
    channelNo: pane.config.channelNo,
    channelName: pane.channelName,
    username: pane.config.username,
    password: pane.config.password
  }
}

export const useDahuaPanePlayback = () => {
  const stopPanePlayback = async (pane: DahuaPanePlayback, stopPlayer: DahuaPanePlaybackOptions['stopPlayer']) => {
    await stopPlayer(pane)
  }

  const stopAllPanes = async (
    panes: DahuaPanePlayback[],
    stopAllPlayers: DahuaPanePlaybackOptions['stopAllPlayers']
  ) => {
    await stopAllPlayers(panes)
  }

  const playPaneSource = async ({
    pane,
    paneIndex,
    source,
    resolvePaneContainer,
    startPreview,
    stopPlayer
  }: Omit<DahuaPanePlaybackOptions, 'stopAllPlayers' | 'switchStream'>) => {
    if (pane.isPlaying || pane.player) {
      await stopPlayer(pane)
    }

    const container = await resolvePaneContainer(paneIndex, pane)
    if (!container) {
      throw new Error('播放器容器不存在')
    }

    pane.container = container
    pane.ibmsChannel = source.ibmsChannel || null
    const success = await startPreview(pane, buildDefaultConfig(pane, source), source.channelName)
    if (!success) {
      throw new Error('播放失败，请稍后重试')
    }
  }

  const retryPanePlayback = async (
    options: Omit<DahuaPanePlaybackOptions, 'source' | 'stopAllPlayers' | 'switchStream'>
  ) => {
    const source = buildSourceFromPane(options.pane)
    if (!source) {
      throw new Error('当前窗口缺少通道信息，无法重试播放')
    }
    await playPaneSource({
      ...options,
      source
    })
  }

  const switchPaneStream = async (
    options: Omit<DahuaPanePlaybackOptions, 'source' | 'stopAllPlayers'> & { streamType: 'main' | 'sub' }
  ) => {
    const source = buildSourceFromPane(options.pane)
    if (!source) {
      throw new Error('当前窗口缺少通道信息，无法切换码流')
    }

    options.pane.streamType = options.streamType

    if (options.pane.config && options.pane.player) {
      const success = await options.switchStream(options.pane, options.streamType)
      if (!success) {
        throw new Error(`切换到${options.streamType === 'main' ? '主' : '子'}码流失败`)
      }
      return
    }

    await playPaneSource({
      pane: options.pane,
      paneIndex: options.paneIndex,
      source,
      resolvePaneContainer: options.resolvePaneContainer,
      startPreview: options.startPreview,
      stopPlayer: options.stopPlayer
    })
  }

  return {
    stopPanePlayback,
    stopAllPanes,
    playPaneSource,
    retryPanePlayback,
    switchPaneStream
  }
}
