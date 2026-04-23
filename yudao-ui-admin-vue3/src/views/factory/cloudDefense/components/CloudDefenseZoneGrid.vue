<script setup lang="ts">
defineOptions({ name: 'CloudDefenseZoneGrid' })

defineProps<{
  zones: Array<{
    id: number
    areaId: number
    name: string
    deviceCount: number
    onlineDeviceCount: number
    zoneCount: number
    armedZoneCount: number
    alarmingZoneCount: number
    statusText: string
    healthText: string
    actionText: string
  }>
  activeAreaId?: number | null
}>()

const emit = defineEmits<{
  (e: 'select', areaId: number): void
}>()
</script>

<template>
  <section class="cloud-defense-zones">
    <header class="cloud-defense-zones__header">
      <div class="cloud-defense-zones__title">防区管理</div>
      <button class="cloud-defense-zones__action" type="button">全局设防</button>
    </header>

    <div class="cloud-defense-zones__grid">
      <button
        v-for="item in zones"
        :key="item.id"
        class="cloud-defense-zone-card"
        :class="{ 'is-active': activeAreaId === item.areaId }"
        type="button"
        @click="emit('select', item.areaId)"
      >
        <div class="cloud-defense-zone-card__top">
          <div class="cloud-defense-zone-card__info">
            <div class="cloud-defense-zone-card__name">{{ item.name }}</div>
            <div class="cloud-defense-zone-card__meta">{{ item.deviceCount }}设备 / {{ item.zoneCount }}防区</div>
          </div>
          <div class="cloud-defense-zone-card__status" :class="{ 'is-alert': item.alarmingZoneCount > 0 }">
            {{ item.statusText }}
          </div>
        </div>

        <div class="cloud-defense-zone-card__stats">
          <span>在线 {{ item.onlineDeviceCount }}</span>
          <span>已设防 {{ item.armedZoneCount }}</span>
          <span>告警 {{ item.alarmingZoneCount }}</span>
        </div>

        <div class="cloud-defense-zone-card__footer">
          <span :class="{ 'is-alert': item.alarmingZoneCount > 0 }">{{ item.healthText }}</span>
          <strong>{{ item.actionText }}</strong>
        </div>
      </button>
    </div>
  </section>
</template>

<style scoped lang="scss">
.cloud-defense-zones {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cloud-defense-zones__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.cloud-defense-zones__title {
  font-size: 14px;
  font-weight: 600;
  color: #f2f8ff;
}

.cloud-defense-zones__action {
  height: 28px;
  padding: 0 12px;
  color: #77efb5;
  border: 1px solid rgba(73, 223, 150, 0.3);
  border-radius: 8px;
  background: rgba(27, 94, 64, 0.18);
}

.cloud-defense-zones__grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 6px;
}

.cloud-defense-zone-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-height: 78px;
  padding: 7px 9px;
  text-align: left;
  border: 1px solid rgba(88, 140, 255, 0.16);
  border-radius: 12px;
  background: linear-gradient(180deg, rgba(11, 28, 57, 0.96), rgba(8, 20, 41, 0.98));
}

.cloud-defense-zone-card.is-active {
  border-color: rgba(89, 168, 255, 0.42);
  box-shadow:
    inset 0 1px 0 rgba(122, 184, 255, 0.08),
    0 8px 18px rgba(8, 18, 36, 0.2);
}

.cloud-defense-zone-card__top,
.cloud-defense-zone-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.cloud-defense-zone-card__info {
  min-width: 0;
}

.cloud-defense-zone-card__name {
  font-size: 12px;
  font-weight: 600;
  color: #eef6ff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cloud-defense-zone-card__meta,
.cloud-defense-zone-card__stats {
  margin-top: 2px;
  font-size: 10px;
  color: rgba(190, 214, 246, 0.72);
}

.cloud-defense-zone-card__stats {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin: 0;
}

.cloud-defense-zone-card__status {
  display: inline-flex;
  align-items: center;
  height: 16px;
  padding: 0 7px;
  color: #72e9af;
  font-size: 10px;
  border-radius: 999px;
  background: rgba(47, 211, 139, 0.12);
  flex-shrink: 0;
}

.cloud-defense-zone-card__status.is-alert {
  color: #ff9e95;
  background: rgba(255, 111, 97, 0.12);
}

.cloud-defense-zone-card__stats span {
  display: inline-flex;
  align-items: center;
  height: 16px;
  padding: 0 6px;
  border-radius: 999px;
  background: rgba(53, 90, 165, 0.18);
}

.cloud-defense-zone-card__footer span {
  font-size: 10px;
  color: rgba(190, 214, 246, 0.78);
}

.cloud-defense-zone-card__footer span.is-alert {
  color: #ff9e95;
}

.cloud-defense-zone-card__footer strong {
  color: #ff9259;
  font-size: 10px;
}

@media (max-width: 1600px) {
  .cloud-defense-zones__grid {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }
}

@media (max-width: 1360px) {
  .cloud-defense-zones__grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1200px) {
  .cloud-defense-zones__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
