<script setup lang="ts">
/**
 * 文件说明：智慧工厂 - 环保监测工作台
 *
 * <p>说明：本页面独立承接工厂环保监测工作台原型，
 * 与建筑环境总览、合规管理中的环保监测 Tab 完全解耦。</p>
 */
import { computed, onMounted, ref } from 'vue'
import {
  getEnvironmentalDashboard,
  type EnvironmentalAlertItem,
  type EnvironmentalDashboardData,
  type EnvironmentalKpiCard
} from '@/api/factory/environmental'

defineOptions({ name: 'FactoryEnvironmental' })

/**
 * 页面加载态
 */
const loading = ref(false)

/**
 * 工作台数据
 */
const dashboard = ref<EnvironmentalDashboardData | null>(null)

/**
 * 顶部 KPI
 */
const kpiCards = computed<EnvironmentalKpiCard[]>(() => dashboard.value?.kpiCards || [])

/**
 * 废气监测卡
 */
const airEmission = computed(() => {
  const source = dashboard.value?.airEmission
  if (!source) {
    return undefined
  }
  return {
    ...source,
    items: (source.items || []).filter((item) => item.pointCode !== 'EXHAUST_FLOW')
  }
})

/**
 * 废水监测卡
 */
const wastewater = computed(() => dashboard.value?.wastewater)

/**
 * 噪声监测卡
 */
const noise = computed(() => dashboard.value?.noise)

/**
 * 预警列表
 */
const alerts = computed<EnvironmentalAlertItem[]>(() => dashboard.value?.alerts || [])

/**
 * 生命周期：页面挂载后加载工作台数据
 */
onMounted(() => {
  loadDashboard()
})

/**
 * 加载环保监测工作台数据
 */
async function loadDashboard() {
  loading.value = true
  try {
    dashboard.value = await getEnvironmentalDashboard()
  } finally {
    loading.value = false
  }
}

/**
 * 获取 KPI 主题类
 *
 * @param theme 主题
 * @returns 样式类
 */
function getKpiThemeClass(theme?: string | null) {
  return `is-${theme || 'blue'}`
}

/**
 * 获取状态标签类
 *
 * @param tone 语义色
 * @returns 样式类
 */
function getToneClass(tone?: string | null) {
  return tone === 'warning' ? 'is-warning' : 'is-success'
}

/**
 * 获取废水主题类
 *
 * @param theme 主题
 * @returns 样式类
 */
function getWastewaterThemeClass(theme?: string | null) {
  return `is-${theme || 'emerald'}`
}

/**
 * 计算环形进度的描边长度
 *
 * @param percent 百分比
 * @returns stroke-dasharray
 */
function getGaugeDasharray(percent?: number | null) {
  const safePercent = Math.max(0, Math.min(100, Number(percent || 0)))
  const circumference = 220
  return `${(safePercent / 100) * circumference} ${circumference}`
}

/**
 * 格式化时间
 *
 * @param value 时间
 * @returns 文本
 */
function formatTime(value?: string | null) {
  if (!value) {
    return '--'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  const hour = `${date.getHours()}`.padStart(2, '0')
  const minute = `${date.getMinutes()}`.padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

/**
 * 获取预警描述文本
 *
 * @param item 预警项
 * @returns 描述
 */
function resolveAlertDescription(item: EnvironmentalAlertItem) {
  if (item.description) {
    return item.description
  }
  return `当前值: ${item.currentValueText} ${item.unit || ''} | 限值: ${item.limitValueText}`
}
</script>

<template>
  <div v-loading="loading" class="environmental-page">
    <section class="metric-grid">
      <article
        v-for="card in kpiCards"
        :key="card.key"
        class="metric-card"
        :class="getKpiThemeClass(card.theme)"
      >
        <div class="metric-card__icon">
          <Icon :icon="card.icon" />
        </div>
        <div class="metric-card__content">
          <p class="metric-card__label">{{ card.title }}</p>
          <div class="metric-card__value">
            <strong>{{ card.valueText }}</strong>
            <span v-if="card.unit">{{ card.unit }}</span>
          </div>
        </div>
      </article>
    </section>

    <section class="panel-grid panel-grid--stack">
      <article class="panel-card panel-card--air">
        <header class="panel-card__header">
          <h3 class="panel-card__title">
            <Icon icon="mdi:weather-windy" class="panel-card__title-icon is-violet" />
            {{ airEmission?.title || '废气排放监测' }}
          </h3>
          <span class="status-badge" :class="getToneClass(airEmission?.items?.some((item) => item.tone === 'warning') ? 'warning' : 'success')">
            {{ airEmission?.overallStatusText || '暂无数据' }}
          </span>
        </header>

        <div v-if="airEmission?.items?.length" class="air-grid">
          <article
            v-for="item in airEmission.items"
            :key="item.pointCode"
            class="air-card"
          >
            <div class="air-card__header">
              <span class="air-card__name">{{ item.pointName }}</span>
              <span class="status-pill" :class="getToneClass(item.tone)">{{ item.status }}</span>
            </div>
            <div class="air-card__value-row">
              <strong>{{ item.valueText }}</strong>
              <span>{{ item.unit }}</span>
            </div>
            <div class="air-card__track">
              <div
                class="air-card__bar"
                :class="getToneClass(item.tone)"
                :style="{ width: `${item.progressPercent || 0}%` }"
              ></div>
              <div
                class="air-card__limit"
                :style="{ left: `${item.limitMarkerPercent || 0}%` }"
              ></div>
            </div>
            <div class="air-card__footer">
              <span>0</span>
              <span>限值: {{ item.limitValueText }}</span>
            </div>
          </article>
        </div>

        <div v-else class="compact-empty">暂无废气监测数据</div>
      </article>
    </section>

    <section class="panel-grid panel-grid--stack">
      <article class="panel-card panel-card--wastewater">
        <header class="panel-card__header">
          <h3 class="panel-card__title">
            <Icon icon="mdi:water-outline" class="panel-card__title-icon is-blue" />
            {{ wastewater?.title || '废水排放监测' }}
          </h3>
          <span class="status-badge" :class="getToneClass(wastewater?.items?.some((item) => item.tone === 'warning') ? 'warning' : 'success')">
            {{ wastewater?.overallStatusText || '暂无数据' }}
          </span>
        </header>

        <div v-if="wastewater?.items?.length" class="wastewater-grid">
          <article
            v-for="item in wastewater.items"
            :key="item.pointCode"
            class="wastewater-card"
          >
            <p class="wastewater-card__label">{{ item.pointName }}</p>
            <p class="wastewater-card__value">{{ item.valueText }}</p>
            <p class="wastewater-card__unit">{{ item.displayUnitText || '--' }}</p>
            <div class="wastewater-card__track">
              <div
                class="wastewater-card__bar"
                :class="getWastewaterThemeClass(item.theme)"
                :style="{ width: `${item.progressPercent || 0}%` }"
              ></div>
            </div>
          </article>
        </div>

        <div v-else class="compact-empty">暂无废水监测数据</div>
      </article>
    </section>

    <section class="panel-grid panel-grid--bottom">
      <article class="panel-card">
        <header class="panel-card__header">
          <h3 class="panel-card__title">
            <Icon icon="mdi:volume-high" class="panel-card__title-icon is-green" />
            {{ noise?.title || '噪声监测' }}
          </h3>
        </header>

        <div v-if="noise" class="noise-grid">
          <div class="noise-item">
            <div class="gauge-ring">
              <svg class="gauge-ring__svg" viewBox="0 0 88 88" aria-hidden="true">
                <circle class="gauge-ring__track" cx="44" cy="44" r="35" />
                <circle
                  class="gauge-ring__progress is-green"
                  cx="44"
                  cy="44"
                  r="35"
                  :stroke-dasharray="getGaugeDasharray(noise.day?.percent)"
                />
              </svg>
              <div class="gauge-ring__value">{{ noise.day?.valueText || '--' }}</div>
            </div>
            <p class="noise-item__label">{{ noise.day?.label || '昼间 dB' }}</p>
          </div>

          <div class="noise-item">
            <div class="gauge-ring">
              <svg class="gauge-ring__svg" viewBox="0 0 88 88" aria-hidden="true">
                <circle class="gauge-ring__track" cx="44" cy="44" r="35" />
                <circle
                  class="gauge-ring__progress is-yellow"
                  cx="44"
                  cy="44"
                  r="35"
                  :stroke-dasharray="getGaugeDasharray(noise.night?.percent)"
                />
              </svg>
              <div class="gauge-ring__value is-yellow">{{ noise.night?.valueText || '--' }}</div>
            </div>
            <p class="noise-item__label">{{ noise.night?.label || '夜间 dB' }}</p>
          </div>

          <div class="noise-item">
            <div class="noise-limit">
              <span>{{ noise.limit?.valueText || '--' }}</span>
            </div>
            <p class="noise-item__label">{{ noise.limit?.label || '限值 dB' }}</p>
          </div>
        </div>

        <div v-else class="compact-empty">暂无噪声监测数据</div>
      </article>

      <article class="panel-card">
        <header class="panel-card__header">
          <h3 class="panel-card__title">
            <Icon icon="mdi:bell-alert-outline" class="panel-card__title-icon is-amber" />
            环保预警
          </h3>
        </header>

        <div v-if="alerts.length" class="alert-list">
          <article
            v-for="item in alerts"
            :key="item.id"
            class="alert-card"
            :class="getToneClass(item.tone)"
          >
            <div class="alert-card__header">
              <div class="alert-card__title">{{ item.title }}</div>
              <div class="alert-card__time">{{ formatTime(item.happenedAt) }}</div>
            </div>
            <p class="alert-card__desc">{{ resolveAlertDescription(item) }}</p>
          </article>
        </div>

        <div v-else class="compact-empty">暂无环保预警数据</div>
      </article>
    </section>
  </div>
</template>

<style scoped lang="scss">
.environmental-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 2px 2px 18px;
  color: #fff;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(152px, 1fr));
  gap: 12px;
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 2px;
  scrollbar-width: thin;
  scrollbar-color: rgba(96, 165, 250, 0.45) transparent;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 84px;
  padding: 14px 14px 12px;
  background: linear-gradient(180deg, rgba(8, 18, 36, 0.98), rgba(11, 24, 50, 0.94));
  border: 1px solid rgba(49, 88, 152, 0.28);
  border-radius: 14px;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.04),
    0 10px 24px rgba(2, 9, 24, 0.22);
  white-space: nowrap;
}

.metric-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  font-size: 22px;
  border-radius: 12px;
}

.metric-card__label {
  margin: 0 0 4px;
  font-size: 12px;
  color: #9ca3af;
}

.metric-card__value {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.metric-card__value strong {
  font-size: 24px;
  line-height: 1;
  font-weight: 700;
  color: #fff;
}

.metric-card__value span {
  font-size: 13px;
  color: #cbd5e1;
}

.metric-card.is-violet .metric-card__icon {
  color: #c084fc;
  background: rgba(168, 85, 247, 0.18);
}

.metric-card.is-blue .metric-card__icon {
  color: #60a5fa;
  background: rgba(59, 130, 246, 0.18);
}

.metric-card.is-green .metric-card__icon {
  color: #4ade80;
  background: rgba(34, 197, 94, 0.18);
}

.metric-card.is-orange .metric-card__icon {
  color: #fb923c;
  background: rgba(249, 115, 22, 0.18);
}

.panel-grid {
  display: grid;
  gap: 16px;
}

.panel-grid--bottom {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.panel-card {
  min-height: 220px;
  padding: 14px 16px 16px;
  background: linear-gradient(180deg, rgba(6, 18, 42, 0.98), rgba(9, 25, 56, 0.96));
  border: 1px solid rgba(44, 86, 152, 0.28);
  border-radius: 18px;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.03),
    0 14px 32px rgba(2, 9, 24, 0.24);
}

.panel-card--air {
  min-height: 208px;
}

.panel-card--wastewater {
  min-height: 188px;
}

.panel-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-card__title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.panel-card__title-icon {
  font-size: 16px;
}

.panel-card__title-icon.is-violet {
  color: #c084fc;
}

.panel-card__title-icon.is-blue {
  color: #60a5fa;
}

.panel-card__title-icon.is-green {
  color: #4ade80;
}

.panel-card__title-icon.is-amber {
  color: #facc15;
}

.status-badge,
.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 5px 10px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 999px;
}

.status-badge.is-success,
.status-pill.is-success {
  color: #4ade80;
  background: rgba(34, 197, 94, 0.14);
}

.status-badge.is-warning,
.status-pill.is-warning {
  color: #facc15;
  background: rgba(234, 179, 8, 0.14);
}

.air-grid,
.wastewater-grid,
.noise-grid {
  display: grid;
  gap: 14px;
}

.air-grid {
  grid-template-columns: repeat(4, minmax(132px, 1fr));
  align-items: stretch;
}

.air-card,
.wastewater-card {
  padding: 16px;
  background: linear-gradient(180deg, rgba(22, 37, 69, 0.92), rgba(20, 34, 64, 0.9));
  border-radius: 14px;
  border: 1px solid rgba(46, 75, 125, 0.22);
}

.air-card__header,
.alert-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.air-card__name {
  font-weight: 600;
  color: #fff;
}

.air-card__value-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin: 14px 0 12px;
}

.air-card__value-row strong {
  font-size: 24px;
  line-height: 1;
  font-weight: 700;
  color: #fff;
}

.air-card__value-row span {
  font-size: 13px;
  color: #9ca3af;
}

.air-card__track,
.wastewater-card__track {
  position: relative;
  height: 8px;
  overflow: hidden;
  background: rgba(73, 85, 109, 0.7);
  border-radius: 999px;
}

.air-card__bar,
.wastewater-card__bar {
  height: 100%;
  border-radius: inherit;
}

.air-card__bar.is-success {
  background: #eab308;
}

.air-card__bar.is-warning {
  background: #ef4444;
}

.air-card__limit {
  position: absolute;
  top: 0;
  width: 2px;
  height: 100%;
  background: rgba(255, 255, 255, 0.95);
  transform: translateX(-50%);
}

.air-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 12px;
  color: #6b7280;
}

.wastewater-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.wastewater-card {
  text-align: center;
}

.wastewater-card__label {
  margin: 0 0 10px;
  font-size: 12px;
  color: #9ca3af;
}

.wastewater-card__value {
  margin: 0;
  font-size: 24px;
  line-height: 1;
  font-weight: 700;
  color: #fff;
}

.wastewater-card__unit {
  margin: 8px 0 14px;
  font-size: 12px;
  color: #6b7280;
}

.wastewater-card__bar.is-emerald {
  background: #22c55e;
}

.wastewater-card__bar.is-blue {
  background: #3b82f6;
}

.noise-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  align-items: center;
  min-height: 190px;
}

.noise-item {
  text-align: center;
}

.gauge-ring {
  position: relative;
  width: 94px;
  height: 94px;
  margin: 0 auto;
}

.gauge-ring__svg {
  width: 94px;
  height: 94px;
  transform: rotate(-90deg);
}

.gauge-ring__track {
  fill: none;
  stroke: rgba(55, 65, 81, 0.9);
  stroke-width: 6;
}

.gauge-ring__progress {
  fill: none;
  stroke-width: 6;
  stroke-linecap: round;
}

.gauge-ring__progress.is-green {
  stroke: #22c55e;
}

.gauge-ring__progress.is-yellow {
  stroke: #eab308;
}

.gauge-ring__value {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
}

.gauge-ring__value.is-yellow {
  color: #facc15;
}

.noise-limit {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 94px;
  height: 94px;
  margin: 0 auto;
  background: linear-gradient(180deg, rgba(31, 41, 55, 0.94), rgba(43, 56, 79, 0.88));
  border-radius: 999px;
}

.noise-limit span {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
}

.noise-item__label {
  margin: 12px 0 0;
  font-size: 14px;
  color: #9ca3af;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-card {
  padding: 13px 15px;
  border-radius: 14px;
}

.alert-card.is-warning {
  background: rgba(234, 179, 8, 0.1);
  border: 1px solid rgba(234, 179, 8, 0.28);
}

.alert-card.is-success {
  background: rgba(34, 197, 94, 0.1);
  border: 1px solid rgba(34, 197, 94, 0.28);
}

.alert-card__title {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.alert-card__time {
  font-size: 12px;
  color: #9ca3af;
}

.alert-card__desc {
  margin: 8px 0 0;
  font-size: 13px;
  color: #9ca3af;
}

.compact-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  font-size: 13px;
  color: #94a3b8;
}

@media (max-width: 1100px) {
  .wastewater-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .panel-grid--bottom {
    grid-template-columns: 1fr;
  }

  .noise-grid {
    grid-template-columns: 1fr;
    gap: 18px;
  }
}

@media (max-width: 760px) {
  .air-grid,
  .wastewater-grid {
    grid-template-columns: 1fr;
  }
}
</style>
