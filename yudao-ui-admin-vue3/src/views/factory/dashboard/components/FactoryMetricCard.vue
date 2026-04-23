<script setup lang="ts">
import { Icon } from '@/components/Icon'

defineOptions({ name: 'FactoryMetricCard' })

withDefaults(
  defineProps<{
    title: string
    value: string
    unit?: string
    hint?: string
    trend?: string
    icon?: string
    badge?: string
    theme?: 'cyan' | 'emerald' | 'amber' | 'violet'
  }>(),
  {
    unit: '',
    hint: '',
    trend: '',
    icon: 'ep:data-analysis',
    badge: '',
    theme: 'cyan'
  }
)
</script>

<template>
  <div class="factory-metric-card" :class="`factory-metric-card--${theme}`">
    <div class="factory-metric-card__content">
      <div class="factory-metric-card__visual">
        <div class="factory-metric-card__orbit"></div>
        <div class="factory-metric-card__icon-wrap">
          <Icon :icon="icon" class="factory-metric-card__icon" />
        </div>
        <div v-if="badge" class="factory-metric-card__badge">{{ badge }}</div>
      </div>

      <div class="factory-metric-card__main">
        <div class="factory-metric-card__header">
          <div class="factory-metric-card__title">{{ title }}</div>
        </div>
        <div class="factory-metric-card__value-row">
          <div class="factory-metric-card__value">{{ value }}</div>
          <div v-if="unit" class="factory-metric-card__unit">{{ unit }}</div>
        </div>
        <div v-if="trend" class="factory-metric-card__trend">{{ trend }}</div>
        <div v-if="hint" class="factory-metric-card__hint">{{ hint }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.factory-metric-card {
  position: relative;
  overflow: hidden;
  min-width: 0;
  min-height: clamp(88px, 10.2vh, 112px);
  padding: 12px 12px;
  border: 1px solid rgba(83, 150, 214, 0.16);
  border-radius: 18px;
  background:
    linear-gradient(180deg, rgba(9, 18, 31, 0.92), rgba(6, 12, 22, 0.9)),
    linear-gradient(135deg, rgba(255, 255, 255, 0.03), transparent 48%);
  box-shadow:
    inset 0 1px 0 rgba(186, 225, 255, 0.04),
    0 10px 24px rgba(1, 7, 16, 0.18);
}

.factory-metric-card__content {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.factory-metric-card::after {
  position: absolute;
  top: -20px;
  right: -20px;
  width: 94px;
  height: 94px;
  content: '';
  filter: blur(10px);
  opacity: 0.2;
}

.factory-metric-card::before {
  position: absolute;
  inset: 0;
  content: '';
  background:
    linear-gradient(90deg, rgba(113, 190, 255, 0.08) 1px, transparent 1px),
    linear-gradient(rgba(113, 190, 255, 0.08) 1px, transparent 1px);
  background-size: 18px 18px;
  mask-image: linear-gradient(180deg, rgba(255, 255, 255, 0.32), transparent 68%);
  opacity: 0.08;
  pointer-events: none;
}

.factory-metric-card--cyan::after {
  background: radial-gradient(circle, #22d3ee 0%, transparent 72%);
}

.factory-metric-card--emerald::after {
  background: radial-gradient(circle, #10b981 0%, transparent 72%);
}

.factory-metric-card--amber::after {
  background: radial-gradient(circle, #f59e0b 0%, transparent 72%);
}

.factory-metric-card--violet::after {
  background: radial-gradient(circle, #8b5cf6 0%, transparent 72%);
}

.factory-metric-card__visual {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(8, 23, 37, 0.96), rgba(5, 12, 23, 0.9));
  box-shadow:
    inset 0 0 0 1px rgba(190, 228, 255, 0.06),
    0 8px 18px rgba(0, 0, 0, 0.18);
}

.factory-metric-card__orbit {
  position: absolute;
  inset: 6px;
  border: 1px solid rgba(139, 205, 255, 0.1);
  border-radius: 999px;
}

.factory-metric-card__icon-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 999px;
}

.factory-metric-card--cyan .factory-metric-card__icon-wrap {
  color: #5eead4;
  background: rgba(17, 94, 89, 0.26);
}

.factory-metric-card--emerald .factory-metric-card__icon-wrap {
  color: #6ee7b7;
  background: rgba(6, 78, 59, 0.26);
}

.factory-metric-card--amber .factory-metric-card__icon-wrap {
  color: #fbbf24;
  background: rgba(120, 53, 15, 0.28);
}

.factory-metric-card--violet .factory-metric-card__icon-wrap {
  color: #c4b5fd;
  background: rgba(76, 29, 149, 0.26);
}

.factory-metric-card__icon {
  font-size: 16px;
}

.factory-metric-card__badge {
  position: absolute;
  right: 4px;
  bottom: 6px;
  min-width: 24px;
  padding: 3px 6px;
  font-size: 9px;
  font-weight: 700;
  line-height: 1;
  text-align: center;
  color: #f8fdff;
  border-radius: 999px;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.2);
}

.factory-metric-card--cyan .factory-metric-card__badge {
  background: linear-gradient(180deg, #22d3ee, #0ea5e9);
}

.factory-metric-card--emerald .factory-metric-card__badge {
  background: linear-gradient(180deg, #34d399, #10b981);
}

.factory-metric-card--amber .factory-metric-card__badge {
  background: linear-gradient(180deg, #fbbf24, #f97316);
}

.factory-metric-card--violet .factory-metric-card__badge {
  background: linear-gradient(180deg, #a78bfa, #8b5cf6);
}

.factory-metric-card__main {
  flex: 1;
  min-width: 0;
}

.factory-metric-card__header {
  display: flex;
  align-items: center;
}

.factory-metric-card__title {
  overflow: hidden;
  font-size: 12px;
  color: rgba(204, 229, 247, 0.68);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.factory-metric-card__value-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-top: 8px;
}

.factory-metric-card__value {
  overflow: hidden;
  font-size: clamp(18px, 1.2vw, 24px);
  font-weight: 700;
  letter-spacing: 0.02em;
  color: #f6fcff;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.factory-metric-card__unit {
  flex-shrink: 0;
  font-size: 11px;
  color: rgba(202, 225, 244, 0.66);
}

.factory-metric-card__trend {
  margin-top: 6px;
  overflow: hidden;
  font-size: 11px;
  color: #8ff4c9;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.factory-metric-card__hint {
  margin-top: 4px;
  overflow: hidden;
  font-size: 11px;
  color: rgba(188, 212, 232, 0.52);
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 1599px) {
  .factory-metric-card {
    min-height: 82px;
    padding: 10px 10px;
  }

  .factory-metric-card__visual {
    width: 48px;
    height: 48px;
  }

  .factory-metric-card__icon-wrap {
    width: 30px;
    height: 30px;
  }

  .factory-metric-card__icon {
    font-size: 14px;
  }

  .factory-metric-card__value {
    font-size: 18px;
  }
}

@media (max-width: 768px) {
  .factory-metric-card__content {
    gap: 14px;
  }

  .factory-metric-card__visual {
    width: 78px;
    height: 78px;
  }

  .factory-metric-card__icon-wrap {
    width: 52px;
    height: 52px;
  }

  .factory-metric-card__icon {
    font-size: 24px;
  }

  .factory-metric-card__value {
    font-size: 28px;
  }
}
</style>
