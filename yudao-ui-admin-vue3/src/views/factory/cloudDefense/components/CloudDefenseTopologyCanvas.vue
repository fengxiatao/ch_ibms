<script setup lang="ts">
defineOptions({ name: 'CloudDefenseTopologyCanvas' })

interface AreaItem {
  id: number
  code: string
  name: string
  type?: string | null
  x: number
  y: number
  width: number
  height: number
  armed: boolean
  alarming: boolean
  deviceCount: number
  zoneCount: number
  detailText?: string | null
}

interface PointItem {
  id: number
  areaId: number
  code: string
  name: string
  x: number
  y: number
  armed: boolean
  alarming: boolean
  online: boolean
}

const props = defineProps<{
  title: string
  legends: Array<{ key: string; label: string; color: string }>
  areas: AreaItem[]
  points: PointItem[]
  activeAreaId?: number | null
}>()

const activeArea = computed(() => {
  return props.areas.find((item) => item.id === props.activeAreaId) || props.areas[0] || null
})
const renderAreas = computed(() => {
  return props.areas.map((item) => {
    const left = 4 + item.x * 0.92
    const top = 10 + item.y * 0.72
    const width = item.width * 0.92
    const height = Math.max(item.height * 0.92, 14)
    return {
      ...item,
      renderLeft: Math.min(left, 90),
      renderTop: Math.min(top, 78),
      renderWidth: Math.min(width, 40),
      renderHeight: Math.min(height, 26)
    }
  })
})
const renderPoints = computed(() => {
  return props.points.map((item) => ({
    ...item,
    renderLeft: 4 + item.x * 0.92,
    renderTop: 10 + item.y * 0.72
  }))
})

const emit = defineEmits<{
  (e: 'select-area', areaId: number): void
  (e: 'select-point', pointId: number): void
}>()
</script>

<template>
  <section class="cloud-defense-topology">
    <header class="cloud-defense-topology__header">
      <div class="cloud-defense-topology__title">{{ title }}</div>
      <div class="cloud-defense-topology__legend">
        <span v-for="item in legends" :key="item.key" class="cloud-defense-topology__legend-item">
          <i :style="{ background: item.color }"></i>
          {{ item.label }}
        </span>
      </div>
    </header>

    <div class="cloud-defense-topology__canvas">
      <div class="cloud-defense-topology__boundary is-inner"></div>
      <div class="cloud-defense-topology__boundary"></div>
      <div class="cloud-defense-topology__scanline"></div>

      <button
        v-for="area in renderAreas"
        :key="area.id"
        class="cloud-defense-topology__area"
        :class="{
          'is-active': activeAreaId === area.id,
          'is-armed': area.armed,
          'is-alarming': area.alarming
        }"
        type="button"
        :style="{
          left: `${area.renderLeft}%`,
          top: `${area.renderTop}%`,
          width: `${area.renderWidth}%`,
          height: `${area.renderHeight}%`
        }"
        @click="emit('select-area', area.id)"
      >
        <div class="cloud-defense-topology__area-name">{{ area.name }}</div>
        <div class="cloud-defense-topology__area-meta">{{ area.detailText || `${area.deviceCount}设备 / ${area.zoneCount}防区` }}</div>
      </button>

      <button
        v-for="point in renderPoints"
        :key="point.id"
        class="cloud-defense-topology__point"
        :class="{
          'is-armed': point.armed,
          'is-online': point.online,
          'is-alarming': point.alarming
        }"
        type="button"
        :style="{ left: `${point.renderLeft}%`, top: `${point.renderTop}%` }"
        @click="emit('select-point', point.id)"
      >
        <span>{{ point.name }}</span>
      </button>

      <div v-if="activeArea" class="cloud-defense-topology__focus">
        <div class="cloud-defense-topology__focus-title">
          <strong>{{ activeArea.name }}</strong>
          <span>{{ activeArea.alarming ? '告警关注' : activeArea.armed ? '已设防' : '未设防' }}</span>
        </div>
        <div class="cloud-defense-topology__focus-stats">
          <span>{{ activeArea.deviceCount }}设备</span>
          <span>{{ activeArea.zoneCount }}防区</span>
          <span>{{ activeArea.detailText || '空间联防区域' }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.cloud-defense-topology {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 12px 14px;
  border: 1px solid rgba(88, 140, 255, 0.12);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(8, 20, 43, 0.98), rgba(5, 14, 29, 0.98));
}

.cloud-defense-topology__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.cloud-defense-topology__title {
  font-size: 14px;
  font-weight: 600;
  color: #f2f8ff;
}

.cloud-defense-topology__legend {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.cloud-defense-topology__legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: rgba(194, 220, 255, 0.72);
}

.cloud-defense-topology__legend-item i {
  width: 8px;
  height: 8px;
  border-radius: 999px;
}

.cloud-defense-topology__canvas {
  position: relative;
  flex: 1;
  min-height: 258px;
  overflow: hidden;
  border: 1px solid rgba(76, 111, 170, 0.18);
  border-radius: 14px;
  background:
    radial-gradient(circle at center, rgba(39, 92, 196, 0.18), transparent 54%),
    linear-gradient(rgba(103, 157, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(103, 157, 255, 0.05) 1px, transparent 1px),
    linear-gradient(180deg, rgba(18, 34, 67, 0.98), rgba(10, 20, 42, 1));
  background-size:
    auto,
    22px 22px,
    22px 22px,
    auto;
}

.cloud-defense-topology__boundary {
  position: absolute;
  inset: 8% 7%;
  border: 3px solid rgba(73, 136, 255, 0.92);
  box-shadow: 0 0 18px rgba(34, 92, 232, 0.14);
}

.cloud-defense-topology__boundary.is-inner {
  inset: 5% 4%;
  border-width: 3px;
  border-style: dashed;
  border-color: rgba(133, 163, 215, 0.22);
  box-shadow: none;
}

.cloud-defense-topology__scanline {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(113, 170, 255, 0.03), transparent 42%, rgba(113, 170, 255, 0.02));
  pointer-events: none;
}

.cloud-defense-topology__canvas::before,
.cloud-defense-topology__canvas::after {
  position: absolute;
  top: 9%;
  bottom: 9%;
  width: 2px;
  content: '';
  background: linear-gradient(180deg, transparent, rgba(85, 145, 255, 0.24), transparent);
  pointer-events: none;
}

.cloud-defense-topology__canvas::before {
  left: 8%;
}

.cloud-defense-topology__canvas::after {
  right: 8%;
}

.cloud-defense-topology__area {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: 2px solid rgba(96, 145, 255, 0.78);
  background: rgba(42, 78, 146, 0.1);
  color: #d8e9ff;
  backdrop-filter: blur(2px);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease,
    background 0.2s ease;
}

.cloud-defense-topology__area.is-armed {
  border-color: rgba(82, 226, 148, 0.86);
  background: rgba(21, 97, 69, 0.12);
}

.cloud-defense-topology__area.is-alarming {
  border-color: rgba(255, 111, 97, 0.94);
  background: rgba(120, 36, 47, 0.12);
  box-shadow: 0 0 18px rgba(255, 96, 80, 0.12);
}

.cloud-defense-topology__area.is-active {
  transform: translateY(-1px);
  box-shadow:
    0 0 0 1px rgba(131, 186, 255, 0.3),
    0 12px 28px rgba(7, 17, 36, 0.3);
}

.cloud-defense-topology__area-name {
  font-size: 15px;
  font-weight: 700;
}

.cloud-defense-topology__area-meta {
  font-size: 11px;
  color: rgba(214, 230, 255, 0.68);
}

.cloud-defense-topology__point {
  position: absolute;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 34px;
  width: 34px;
  height: 34px;
  margin-left: -17px;
  margin-top: -17px;
  color: rgba(207, 228, 255, 0.82);
  border: 3px solid rgba(66, 127, 255, 0.92);
  border-radius: 999px;
  background: rgba(24, 46, 88, 0.9);
  box-shadow: 0 0 0 5px rgba(57, 107, 212, 0.12);
}

.cloud-defense-topology__point.is-armed {
  border-color: rgba(61, 214, 132, 0.96);
}

.cloud-defense-topology__point.is-online {
  box-shadow: 0 0 0 5px rgba(61, 214, 132, 0.12);
}

.cloud-defense-topology__point.is-alarming {
  border-color: rgba(255, 108, 92, 0.98);
  box-shadow:
    0 0 0 5px rgba(255, 87, 72, 0.12),
    0 0 18px rgba(255, 87, 72, 0.14);
}

.cloud-defense-topology__point span {
  position: absolute;
  bottom: -18px;
  font-size: 11px;
  font-weight: 600;
  color: rgba(190, 215, 255, 0.84);
}

.cloud-defense-topology__focus {
  position: absolute;
  right: 12px;
  bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 168px;
  max-width: 32%;
  padding: 8px 10px;
  border: 1px solid rgba(84, 157, 255, 0.14);
  border-radius: 10px;
  background: linear-gradient(180deg, rgba(7, 18, 39, 0.94), rgba(5, 14, 29, 0.94));
  box-shadow: 0 8px 20px rgba(1, 8, 20, 0.22);
}

.cloud-defense-topology__focus-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.cloud-defense-topology__focus-title strong {
  font-size: 11px;
  color: #edf5ff;
}

.cloud-defense-topology__focus-title span {
  display: inline-flex;
  align-items: center;
  height: 18px;
  padding: 0 7px;
  color: #74e8af;
  font-size: 10px;
  border-radius: 999px;
  background: rgba(47, 211, 139, 0.12);
}

.cloud-defense-topology__focus-stats {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.cloud-defense-topology__focus-stats span {
  display: inline-flex;
  align-items: center;
  height: 16px;
  padding: 0 6px;
  color: rgba(190, 214, 246, 0.76);
  font-size: 10px;
  border-radius: 999px;
  background: rgba(53, 90, 165, 0.18);
}

@media (max-width: 1600px) {
  .cloud-defense-topology__canvas {
    min-height: 232px;
  }

  .cloud-defense-topology__area-name {
    font-size: 14px;
  }
}
</style>
