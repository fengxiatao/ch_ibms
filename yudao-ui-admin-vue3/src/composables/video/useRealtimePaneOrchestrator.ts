import { nextTick, ref } from 'vue'
import type { Ref } from 'vue'
import type {
  GridLayoutType,
  IbmsChannel,
  PatrolScene,
  PatrolSceneChannel,
  PlayerPane,
  VideoViewPane,
  VideoView
} from '@/views/security/VideoSurveillance/RealTimePreview/types'

interface RestoreViewOptions {
  view: VideoView
  stopAllPanes: () => Promise<void>
  createPanes: (layout: GridLayoutType) => PlayerPane[]
  panesRef: Ref<PlayerPane[]>
  gridLayoutRef: Ref<GridLayoutType>
  activePaneRef?: Ref<number>
  isPatrollingRef?: Ref<boolean>
  setCurrentView: (view: VideoView) => void
  clearCurrentView?: () => void
  findIbmsChannelByChannelNo: (channelNo: number) => Promise<IbmsChannel | undefined>
  playChannelInPane: (channel: IbmsChannel, paneIndex: number) => Promise<void>
  syncCurrentViewSelection?: (viewId: number | null) => void
}

interface CurrentViewStateOptions {
  currentViewRef: Ref<VideoView | null>
  syncCurrentViewSelection?: (viewId: number | null) => void
}

interface ExecuteSceneOptions {
  scene: PatrolScene
  isPatrollingRef: Ref<boolean>
  panesRef: Ref<PlayerPane[]>
  gridLayoutRef: Ref<GridLayoutType>
  activePaneRef?: Ref<number>
  createPanes: (layout: GridLayoutType) => PlayerPane[]
  stopAllPanes: () => Promise<void>
  clearCurrentView?: () => void
  syncCurrentViewSelection?: (viewId: number | null) => void
  playChannelByNo: (paneIndex: number, channel: PatrolSceneChannel) => Promise<boolean>
}

interface ResetPaneLayoutOptions {
  layout: GridLayoutType
  stopAllPanes: () => Promise<void>
  createPanes: (layout: GridLayoutType) => PlayerPane[]
  panesRef: Ref<PlayerPane[]>
  gridLayoutRef: Ref<GridLayoutType>
  activePaneRef?: Ref<number>
  clearCurrentView?: () => void
  syncCurrentViewSelection?: (viewId: number | null) => void
  waitMs?: number
}

const resolveGridLayout = (gridLayout: string | number, gridCount?: number) => {
  if (typeof gridLayout === 'string' && gridLayout.includes('x')) {
    const [cols, rows] = gridLayout.split('x').map(Number)
    return (cols * rows) as GridLayoutType
  }
  return (parseInt(String(gridLayout)) || gridCount || 6) as GridLayoutType
}

const collectViewPanes = (panes: PlayerPane[]): VideoViewPane[] => {
  return panes
    .map((pane, index) => ({
      paneIndex: index,
      channelId: pane.config?.channelNo,
      channelName: pane.channelName
    }))
    .filter((pane) => pane.channelId)
}

export const useRealtimePaneOrchestrator = () => {
  const sceneRotationTimers = ref<Map<number, number>>(new Map())

  const clearSceneRotationTimers = () => {
    sceneRotationTimers.value.forEach((timer) => clearTimeout(timer))
    sceneRotationTimers.value.clear()
  }

  const syncCurrentViewState = (view: VideoView, options: CurrentViewStateOptions) => {
    options.currentViewRef.value = view
    options.syncCurrentViewSelection?.(view.id ?? null)
  }

  const clearCurrentViewState = (options: CurrentViewStateOptions, viewId?: number) => {
    if (viewId !== undefined && options.currentViewRef.value?.id !== viewId) {
      return
    }
    options.currentViewRef.value = null
    options.syncCurrentViewSelection?.(null)
  }

  const resetPaneLayout = async (options: ResetPaneLayoutOptions) => {
    await options.stopAllPanes()
    options.gridLayoutRef.value = options.layout
    options.panesRef.value = options.createPanes(options.layout)
    if (options.activePaneRef) {
      options.activePaneRef.value = 0
    }
    if (options.clearCurrentView) {
      options.clearCurrentView()
    }
    if (options.syncCurrentViewSelection) {
      options.syncCurrentViewSelection(null)
    }
    await nextTick()
    if (options.waitMs) {
      await new Promise((resolve) => setTimeout(resolve, options.waitMs))
    }
  }

  const restoreView = async (options: RestoreViewOptions) => {
    const layoutValue = (options.view.layout || options.view.gridLayout || 4) as GridLayoutType
    if (options.isPatrollingRef) {
      options.isPatrollingRef.value = false
      clearSceneRotationTimers()
    }
    await resetPaneLayout({
      layout: layoutValue,
      stopAllPanes: options.stopAllPanes,
      createPanes: options.createPanes,
      panesRef: options.panesRef,
      gridLayoutRef: options.gridLayoutRef,
      activePaneRef: options.activePaneRef,
      clearCurrentView: options.clearCurrentView,
      syncCurrentViewSelection: options.syncCurrentViewSelection,
      waitMs: 300
    })

    options.setCurrentView(options.view)

    if (options.view.panes?.length) {
      for (let i = 0; i < options.view.panes.length; i++) {
        const paneData = options.view.panes[i]
        const channelNo = paneData.channelId
        if (!channelNo || paneData.paneIndex >= options.panesRef.value.length) {
          continue
        }

        const ibmsChannel = await options.findIbmsChannelByChannelNo(channelNo)
        if (ibmsChannel) {
          window.setTimeout(() => {
            options.playChannelInPane(ibmsChannel, paneData.paneIndex)
          }, i * 500)
        }
      }
    }

    if (options.view.id && options.syncCurrentViewSelection) {
      options.syncCurrentViewSelection(options.view.id)
    }
  }

  const startGridRotation = async (
    paneIndex: number,
    channels: PatrolSceneChannel[],
    currentIndex: number,
    options: ExecuteSceneOptions
  ) => {
    if (!options.isPatrollingRef.value || currentIndex >= channels.length) {
      if (options.isPatrollingRef.value && channels.length > 0) {
        startGridRotation(paneIndex, channels, 0, options)
      }
      return
    }

    const channelData = channels[currentIndex]
    const duration = channelData.duration || 15
    const success = await options.playChannelByNo(paneIndex, channelData)

    if (!options.isPatrollingRef.value) {
      return
    }

    if (success) {
      const timer = window.setTimeout(() => {
        if (options.isPatrollingRef.value) {
          startGridRotation(paneIndex, channels, currentIndex + 1, options)
        }
      }, duration * 1000)
      sceneRotationTimers.value.set(paneIndex, timer)
      return
    }

    window.setTimeout(() => {
      if (options.isPatrollingRef.value) {
        startGridRotation(paneIndex, channels, currentIndex + 1, options)
      }
    }, 500)
  }

  const executeScene = async (options: ExecuteSceneOptions) => {
    clearSceneRotationTimers()
    const layoutValue = resolveGridLayout(options.scene.gridLayout, options.scene.gridCount)
    await resetPaneLayout({
      layout: layoutValue,
      stopAllPanes: options.stopAllPanes,
      createPanes: options.createPanes,
      panesRef: options.panesRef,
      gridLayoutRef: options.gridLayoutRef,
      activePaneRef: options.activePaneRef,
      clearCurrentView: options.clearCurrentView,
      syncCurrentViewSelection: options.syncCurrentViewSelection,
      waitMs: 100
    })

    if (!options.scene.channels?.length) {
      return
    }

    const channelsByGrid = new Map<number, PatrolSceneChannel[]>()
    for (const channel of options.scene.channels) {
      const gridPos = channel.gridPosition || 1
      if (!channelsByGrid.has(gridPos)) {
        channelsByGrid.set(gridPos, [])
      }
      channelsByGrid.get(gridPos)!.push(channel)
    }

    channelsByGrid.forEach((channels, gridPos) => {
      const paneIndex = gridPos - 1
      if (paneIndex >= 0 && paneIndex < options.panesRef.value.length) {
        startGridRotation(paneIndex, channels, 0, options)
      }
    })
  }

  const stopPatrol = async (isPatrollingRef: Ref<boolean>, stopAllPanes: () => Promise<void>) => {
    isPatrollingRef.value = false
    clearSceneRotationTimers()
    await stopAllPanes()
  }

  const buildViewPayload = (viewInfo: { id?: number; name: string; groupIds: number[] }, panes: PlayerPane[], gridLayout: GridLayoutType) => {
    return {
      id: viewInfo.id,
      name: viewInfo.name,
      groupIds: viewInfo.groupIds,
      gridLayout,
      panes: collectViewPanes(panes)
    }
  }

  const buildCurrentViewState = (
    viewInfo: { id: number; name: string; groupIds: number[] },
    payload: { gridLayout: GridLayoutType; panes: VideoViewPane[] }
  ): VideoView => {
    return {
      id: viewInfo.id,
      name: viewInfo.name,
      groupIds: viewInfo.groupIds,
      gridLayout: payload.gridLayout,
      panes: payload.panes
    }
  }

  return {
    sceneRotationTimers,
    clearSceneRotationTimers,
    buildViewPayload,
    buildCurrentViewState,
    syncCurrentViewState,
    clearCurrentViewState,
    resetPaneLayout,
    restoreView,
    executeScene,
    stopPatrol
  }
}
