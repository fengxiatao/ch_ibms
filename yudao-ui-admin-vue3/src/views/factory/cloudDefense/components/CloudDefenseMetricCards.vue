<script setup lang="ts">
import { Icon } from '@/components/Icon'

defineOptions({ name: 'CloudDefenseMetricCards' })

interface MetricCardItem {
  key: string
  title: string
  value: string
  subValue: string
  icon: string
  accent: 'cyan' | 'blue' | 'orange' | 'violet'
}

defineProps<{
  items: MetricCardItem[]
}>()
</script>

<template>
  <div class="cloud-defense-metrics">
    <article
      v-for="item in items"
      :key="item.key"
      class="cloud-defense-metric"
      :class="`is-${item.accent}`"
    >
      <div class="cloud-defense-metric__icon">
        <Icon :icon="item.icon" />
      </div>
      <div class="cloud-defense-metric__body">
        <div class="cloud-defense-metric__title">{{ item.title }}</div>
        <div class="cloud-defense-metric__value">{{ item.value }}</div>
      </div>
      <div class="cloud-defense-metric__sub">{{ item.subValue }}</div>
    </article>
  </div>
</template>

<style scoped lang="scss">
.cloud-defense-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.cloud-defense-metric {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 60px;
  padding: 10px 14px;
  overflow: hidden;
  border: 1px solid rgba(67, 92, 140, 0.26);
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(7, 18, 38, 0.98), rgba(6, 14, 29, 0.98));
  box-shadow:
    inset 0 1px 0 rgba(115, 165, 255, 0.05),
    0 14px 32px rgba(1, 8, 20, 0.24);
}

.cloud-defense-metric::after {
  position: absolute;
  inset: 0;
  content: '';
  pointer-events: none;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.02), transparent 36%);
}

.cloud-defense-metric__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  font-size: 14px;
  color: #d8ebff;
}

.cloud-defense-metric__body {
  flex: 1;
  min-width: 0;
}

.cloud-defense-metric__title {
  margin-bottom: 3px;
  font-size: 11px;
  color: rgba(185, 210, 247, 0.64);
}

.cloud-defense-metric__value {
  font-size: 16px;
  font-weight: 700;
  line-height: 1.2;
  color: #f3f8ff;
}

.cloud-defense-metric__sub {
  flex-shrink: 0;
  font-size: 11px;
  color: rgba(203, 224, 255, 0.52);
}

.is-cyan .cloud-defense-metric__icon {
  background: rgba(26, 170, 255, 0.18);
  color: #6de5ff;
}

.is-blue .cloud-defense-metric__icon {
  background: rgba(64, 114, 255, 0.18);
  color: #8ab0ff;
}

.is-orange .cloud-defense-metric__icon {
  background: rgba(255, 112, 67, 0.18);
  color: #ff9d6b;
}

.is-violet .cloud-defense-metric__icon {
  background: rgba(149, 97, 255, 0.18);
  color: #c4a8ff;
}

@media (max-width: 1600px) {
  .cloud-defense-metrics {
    gap: 10px;
  }

  .cloud-defense-metric {
    min-height: 58px;
    padding: 10px 12px;
  }

  .cloud-defense-metric__value {
    font-size: 16px;
  }
}

@media (max-width: 1280px) {
  .cloud-defense-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
