<script setup lang="ts">
import { Icon } from '@/components/Icon'

defineOptions({ name: 'CloudDefenseDeviceList' })

defineProps<{
  devices: Array<{
    id: number
    areaId: number
    areaName: string
    name: string
    typeLabel: string
    location: string
    online: boolean
    capabilityTags: string[]
  }>
  activeAreaId?: number | null
}>()
</script>

<template>
  <section class="cloud-defense-device-list">
    <header class="cloud-defense-device-list__header">
      <div class="cloud-defense-device-list__title">云防设备</div>
      <div class="cloud-defense-device-list__tools">
        <div class="cloud-defense-device-list__count">{{ devices.length }} 台</div>
        <button class="cloud-defense-device-list__setting" type="button">
          <Icon icon="ep:setting" />
        </button>
      </div>
    </header>

    <div class="cloud-defense-device-list__body">
      <article
        v-for="(item, index) in devices"
        :key="item.id"
        class="cloud-defense-device"
        :class="{ 'is-active': activeAreaId === item.areaId }"
      >
        <div class="cloud-defense-device__meta">
          <div class="cloud-defense-device__main">
            <div class="cloud-defense-device__icon">
              <Icon icon="ep:video-camera-filled" />
            </div>
            <div class="cloud-defense-device__content">
              <div class="cloud-defense-device__name-row">
                <div class="cloud-defense-device__name">{{ item.name }}</div>
                <span class="cloud-defense-device__index">{{ String(index + 1).padStart(2, '0') }}</span>
              </div>
              <div class="cloud-defense-device__type">{{ item.typeLabel }}</div>
            </div>
          </div>
          <span class="cloud-defense-device__status" :class="{ 'is-online': item.online }">
            {{ item.online ? '在线' : '离线' }}
          </span>
        </div>

        <div class="cloud-defense-device__location">
          <span>{{ item.areaName }}</span>
          <em>{{ item.location }}</em>
        </div>

        <div class="cloud-defense-device__tags">
          <span v-for="tag in item.capabilityTags.slice(0, 2)" :key="tag" class="cloud-defense-device__tag">
            {{ tag }}
          </span>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped lang="scss">
.cloud-defense-device-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 12px;
  border: 1px solid rgba(72, 103, 170, 0.26);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(7, 18, 39, 0.98), rgba(4, 13, 27, 0.98));
}

.cloud-defense-device-list__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.cloud-defense-device-list__title {
  font-size: 14px;
  font-weight: 600;
  color: #f2f8ff;
}

.cloud-defense-device-list__tools {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cloud-defense-device-list__count {
  font-size: 12px;
  color: rgba(195, 218, 255, 0.68);
}

.cloud-defense-device-list__setting {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  color: rgba(188, 213, 246, 0.66);
  border: 1px solid rgba(73, 112, 195, 0.2);
  border-radius: 8px;
  background: rgba(10, 20, 42, 0.82);
}

.cloud-defense-device-list__body {
  display: grid;
  flex: 1;
  gap: 6px;
  min-height: 0;
  overflow: auto;
  padding-right: 4px;
}

.cloud-defense-device {
  padding: 8px 10px;
  border: 1px solid rgba(73, 112, 195, 0.14);
  border-radius: 10px;
  background: linear-gradient(180deg, rgba(15, 28, 55, 0.92), rgba(8, 18, 39, 0.92));
}

.cloud-defense-device.is-active {
  border-color: rgba(83, 154, 255, 0.42);
  box-shadow:
    inset 0 1px 0 rgba(127, 180, 255, 0.08),
    0 8px 18px rgba(1, 8, 20, 0.16);
}

.cloud-defense-device__meta {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.cloud-defense-device__main {
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr);
  gap: 8px;
  align-items: center;
}

.cloud-defense-device__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: rgba(39, 96, 208, 0.14);
  color: #61a0ff;
  font-size: 12px;
}

.cloud-defense-device__content {
  min-width: 0;
}

.cloud-defense-device__name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.cloud-defense-device__name {
  min-width: 0;
  overflow: hidden;
  font-size: 12px;
  font-weight: 600;
  color: #edf4ff;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.cloud-defense-device__index {
  flex-shrink: 0;
  font-size: 10px;
  color: rgba(150, 183, 232, 0.52);
}

.cloud-defense-device__type {
  margin-top: 2px;
  font-size: 10px;
  color: rgba(185, 210, 247, 0.56);
}

.cloud-defense-device__status {
  display: inline-flex;
  align-items: center;
  height: 16px;
  padding: 0 6px;
  border-radius: 6px;
  background: rgba(255, 92, 92, 0.14);
  color: #ff9d96;
  font-size: 10px;
}

.cloud-defense-device__status.is-online {
  background: rgba(47, 211, 139, 0.14);
  color: #6bedb0;
}

.cloud-defense-device__tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 6px;
}

.cloud-defense-device__location {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  font-size: 10px;
}

.cloud-defense-device__location span {
  color: rgba(168, 201, 248, 0.88);
}

.cloud-defense-device__location em {
  min-width: 0;
  overflow: hidden;
  font-style: normal;
  color: rgba(185, 210, 247, 0.54);
  white-space: nowrap;
  text-overflow: ellipsis;
}

.cloud-defense-device__tag {
  display: inline-flex;
  align-items: center;
  height: 16px;
  padding: 0 5px;
  border-radius: 6px;
  background: rgba(53, 90, 165, 0.26);
  color: #bdd8ff;
  font-size: 10px;
}
</style>
