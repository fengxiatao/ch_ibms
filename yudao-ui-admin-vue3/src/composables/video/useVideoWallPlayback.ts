import { computed, ref } from 'vue'
import type {
  GridLayoutType,
  PlayerPane
} from '@/views/security/VideoSurveillance/RealTimePreview/types'

export interface VideoWallSource {
  id: string | number
  name: string
  location?: string
  level?: 'high' | 'medium' | 'low'
}

const createEmptyPane = (): PlayerPane => ({
  ibmsChannel: null,
  channelName: '',
  player: null,
  container: null,
  isPlaying: false,
  isLoading: false,
  isPaused: false,
  isRecording: false,
  isPlayback: false,
  error: null,
  streamType: 'main',
  muted: true
})

const createPaneList = (layout: GridLayoutType) => {
  return Array.from({ length: layout }, () => createEmptyPane())
}

export const useVideoWallPlayback = (defaultLayout: GridLayoutType = 4) => {
  const gridLayout = ref<GridLayoutType>(defaultLayout)
  const activePane = ref(0)
  const panes = ref<PlayerPane[]>(createPaneList(defaultLayout))
  const sourceMap = ref<Record<number, VideoWallSource | undefined>>({})

  const hasAssignedSources = computed(() =>
    panes.value.some((pane) => Boolean(pane.channelName || pane.ibmsChannel?.channelName))
  )

  const assignSource = (paneIndex: number, source?: VideoWallSource) => {
    if (paneIndex < 0 || paneIndex >= panes.value.length) {
      return
    }
    const pane = panes.value[paneIndex]
    pane.channelName = source?.name || ''
    pane.error = null
    pane.isLoading = false
    pane.isPlaying = false
    sourceMap.value[paneIndex] = source
  }

  const assignSources = (sources: VideoWallSource[]) => {
    const nextMap: Record<number, VideoWallSource | undefined> = {}
    panes.value.forEach((pane, index) => {
      const source = sources[index]
      pane.channelName = source?.name || ''
      pane.error = null
      pane.isLoading = false
      pane.isPlaying = false
      nextMap[index] = source
    })
    sourceMap.value = nextMap
  }

  const setLayout = (layout: GridLayoutType) => {
    gridLayout.value = layout
    panes.value = createPaneList(layout)
    activePane.value = 0
    sourceMap.value = {}
  }

  const clearPane = (paneIndex: number) => {
    assignSource(paneIndex)
  }

  const clearAll = () => {
    panes.value.forEach((_, index) => clearPane(index))
  }

  return {
    activePane,
    gridLayout,
    hasAssignedSources,
    panes,
    sourceMap,
    assignSource,
    assignSources,
    clearAll,
    clearPane,
    setLayout
  }
}
