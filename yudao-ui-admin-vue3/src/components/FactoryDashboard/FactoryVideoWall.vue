<script setup lang="ts">
import { computed, watch } from 'vue'
import VideoPlayerGrid from '@/views/security/VideoSurveillance/RealTimePreview/components/VideoPlayerGrid.vue'
import type { GridLayoutType } from '@/views/security/VideoSurveillance/RealTimePreview/types'
import {
  useVideoWallPlayback,
  type VideoWallSource
} from '@/composables/video/useVideoWallPlayback'

defineOptions({ name: 'FactoryVideoWall' })

const props = withDefaults(
  defineProps<{
    title?: string
    currentViewName?: string
    layout?: GridLayoutType
    sources?: VideoWallSource[]
  }>(),
  {
    title: '轻量视频墙',
    currentViewName: '驾驶舱联动视图',
    layout: 4,
    sources: () => []
  }
)

const emit = defineEmits<{
  (e: 'source-click', source: VideoWallSource): void
}>()

const { activePane, gridLayout, panes, sourceMap, assignSources, setLayout } = useVideoWallPlayback(
  props.layout
)

watch(
  () => props.layout,
  (layout) => {
    setLayout(layout)
    assignSources(props.sources)
  },
  { immediate: true }
)

watch(
  () => props.sources,
  (sources) => {
    assignSources(sources)
  },
  { immediate: true, deep: true }
)

const sourceCountText = computed(() => `${props.sources.length} 路联动源待命`)
const onlineSourceCount = computed(() => props.sources.filter((source) => source.level === 'high').length)
const activeSourceId = computed(() => sourceMap.value[activePane.value]?.id)
const assignedSourceIds = computed(() => {
  return new Set(
    Object.values(sourceMap.value)
      .map((source) => source?.id)
      .filter((id) => id !== undefined && id !== null)
  )
})

const handleSourceClick = (source: VideoWallSource) => {
  emit('source-click', source)
}
</script>

<template>
  <div class="factory-video-wall">
    <div class="factory-video-wall__toolbar">
      <div class="factory-video-wall__toolbar-main">
        <div class="factory-video-wall__view-name">{{ props.currentViewName }}</div>
        <div class="factory-video-wall__meta-list">
          <span class="factory-video-wall__meta-chip">{{ props.title }}</span>
          <span class="factory-video-wall__meta-chip">在线 {{ onlineSourceCount }}</span>
          <span class="factory-video-wall__meta-chip">{{ sourceCountText }}</span>
        </div>
      </div>
      <ElTag size="small" type="info" effect="dark">{{ gridLayout }} 分屏</ElTag>
    </div>

    <div v-if="props.sources.length" class="factory-video-wall__sources">
      <button
        v-for="source in props.sources"
        :key="source.id"
        class="factory-video-wall__source-chip"
        :class="{
          'is-assigned': assignedSourceIds.has(source.id),
          'is-active': activeSourceId === source.id
        }"
        type="button"
        @click="handleSourceClick(source)"
      >
        <span class="factory-video-wall__source-dot" :class="{ 'is-online': source.level === 'high' }"></span>
        <span class="factory-video-wall__source-main">
          <span class="factory-video-wall__source-name">{{ source.name }}</span>
          <span v-if="source.location" class="factory-video-wall__source-location">{{ source.location }}</span>
        </span>
        <span class="factory-video-wall__source-state">{{ source.level === 'high' ? '在线' : '待命' }}</span>
      </button>
    </div>

    <VideoPlayerGrid
      :panes="panes"
      :active-pane="activePane"
      :grid-layout="gridLayout"
      :current-view-name="props.currentViewName"
      :show-footer="false"
      :show-pane-toolbar="false"
      idle-title="视频联动待命"
      idle-description="当前窗口预留给驾驶舱联动视频源"
      idle-secondary-description="一期先接入轻量视频墙，二期再补播放与云台控制"
      @update:active-pane="activePane = $event"
    />
  </div>
</template>

<style scoped lang="scss">
.factory-video-wall {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.factory-video-wall__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid rgba(78, 170, 235, 0.16);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(7, 20, 34, 0.92), rgba(5, 13, 22, 0.92));
}

.factory-video-wall__toolbar-main {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.factory-video-wall__view-name {
  font-size: 16px;
  font-weight: 700;
  color: #f3fbff;
}

.factory-video-wall__meta-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.factory-video-wall__meta-chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  font-size: 12px;
  color: rgba(205, 231, 248, 0.78);
  border: 1px solid rgba(80, 162, 228, 0.18);
  border-radius: 999px;
  background: rgba(8, 25, 40, 0.72);
}

.factory-video-wall__sources {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.factory-video-wall__source-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  padding: 10px 12px;
  color: #dff7ff;
  border: 1px solid rgba(81, 179, 244, 0.2);
  border-radius: 14px;
  background: rgba(9, 25, 40, 0.78);
  cursor: pointer;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    background 0.2s ease;
}

.factory-video-wall__source-chip:hover {
  transform: translateY(-1px);
  border-color: rgba(97, 222, 255, 0.46);
  background: rgba(7, 37, 56, 0.9);
}

.factory-video-wall__source-chip.is-assigned {
  border-color: rgba(80, 194, 255, 0.28);
}

.factory-video-wall__source-chip.is-active {
  border-color: rgba(120, 211, 255, 0.5);
  background: rgba(8, 40, 60, 0.96);
}

.factory-video-wall__source-dot {
  width: 10px;
  height: 10px;
  flex-shrink: 0;
  border-radius: 50%;
  background: rgba(148, 163, 184, 0.7);
}

.factory-video-wall__source-dot.is-online {
  background: #22c55e;
  box-shadow: 0 0 12px rgba(34, 197, 94, 0.7);
}

.factory-video-wall__source-main {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
}

.factory-video-wall__source-name {
  font-size: 12px;
  font-weight: 600;
  text-align: left;
}

.factory-video-wall__source-location {
  margin-top: 4px;
  font-size: 12px;
  text-align: left;
  color: rgba(184, 214, 237, 0.66);
}

.factory-video-wall__source-state {
  font-size: 12px;
  color: rgba(143, 223, 255, 0.82);
}

:deep(.video-player-grid) {
  min-height: 360px;
}

:deep(.video-player-grid .player-grid) {
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(2, 7, 14, 0.96), rgba(6, 15, 28, 0.96));
}

:deep(.video-player-grid .player-pane) {
  border-radius: 12px;
  border: 1px solid rgba(78, 170, 235, 0.12);
}

:deep(.video-player-grid .overlay-center.idle) {
  color: rgba(223, 247, 255, 0.76);
}

:deep(.video-player-grid .pane-label) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(3, 10, 18, 0.72);
}

@media (max-width: 900px) {
  .factory-video-wall__toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .factory-video-wall__sources {
    grid-template-columns: 1fr;
  }
}

:deep(.video-player-grid .overlay-center .tip-text) {
  max-width: 220px;
  text-align: center;
  line-height: 1.6;
}
</style>
