<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import { Echart } from '@/components/Echart'
import EnergyPageContainer from '../components/EnergyPageContainer.vue'
import EnergyPageHeader from '../components/EnergyPageHeader.vue'
import * as EnergyApi from '@/api/iot/building/energy'
import type { IbmsEnergyOverviewVO } from '@/api/iot/building/energy'

defineOptions({ name: 'EnergyOverview' })
// 数据来源与能耗分析、报表管理一致：/iot/building/energy 接口，表 ibms_energy_statistics_daily

type DatePeriod = 'today' | 'week' | 'month' | 'year' | 'custom'

const datePeriod = ref<DatePeriod>('today')
const customRange = ref<[Date, Date] | null>(null)

const loading = ref(false)
const overview = ref<IbmsEnergyOverviewVO | null>(null)

const periodText = computed(() => {
  if (datePeriod.value === 'custom' && customRange.value) {
    return `${dayjs(customRange.value[0]).format('YYYY年MM月DD日')} - ${dayjs(customRange.value[1]).format(
      'YYYY年MM月DD日'
    )}`
  }
  const map: Record<Exclude<DatePeriod, 'custom'>, string> = {
    today: dayjs().format('YYYY年MM月DD日'),
    week: `本周（${dayjs().startOf('week').format('MM/DD')} - ${dayjs().endOf('week').format('MM/DD')}）`,
    month: dayjs().format('YYYY年MM月'),
    year: dayjs().format('YYYY年')
  }
  return map[datePeriod.value as Exclude<DatePeriod, 'custom'>]
})

const formatNumber = (value?: number, fractionDigits = 1) => {
  const v = value ?? 0
  return v.toLocaleString('zh-CN', { maximumFractionDigits: fractionDigits })
}

const periodPrefix = computed(() => {
  if (datePeriod.value === 'today') return '今日'
  if (datePeriod.value === 'week') return '本周'
  if (datePeriod.value === 'month') return '本月'
  if (datePeriod.value === 'year') return '本年'
  return '所选周期'
})

const coreCards = computed(() => {
  const data = overview.value
  const elec = formatNumber(data?.todayElectricity, 1)
  const water = formatNumber(data?.todayWater, 1)
  const gas = formatNumber(data?.todayGas, 1)
  const totalCost =
    (data?.todayElectricity ?? 0) * 1.0 +
    (data?.todayWater ?? 0) * 4.5 +
    (data?.todayGas ?? 0) * 3.0
  return [
    {
      key: 'electric',
      title: `${periodPrefix.value}用电量`,
      icon: '⚡',
      value: elec,
      sub: 'kWh | 所选周期合计',
      theme: 'electric'
    },
    {
      key: 'water',
      title: `${periodPrefix.value}用水量`,
      icon: '💧',
      value: water,
      sub: 'm³ | 所选周期合计',
      theme: 'water'
    },
    {
      key: 'gas',
      title: `${periodPrefix.value}用气量`,
      icon: '🔥',
      value: gas,
      sub: 'm³ | 所选周期合计',
      theme: 'gas'
    },
    {
      key: 'cost',
      title: `${periodPrefix.value}费用估算`,
      icon: '💰',
      value: `¥${formatNumber(totalCost, 0)}`,
      sub: '按统一单价估算',
      theme: 'cost'
    }
  ] as const
})

const ratioChartOptions = computed<EChartsOption>(() => {
  const data = [
    { name: '电力', value: 84.6, itemStyle: { color: '#f59e0b' } },
    { name: '水务', value: 1.3, itemStyle: { color: '#06b6d4' } },
    { name: '燃气', value: 13.8, itemStyle: { color: '#f97316' } }
  ]
  return {
    tooltip: { trigger: 'item' },
    legend: { show: false },
    series: [
      {
        type: 'pie',
        radius: ['58%', '78%'],
        center: ['50%', '50%'],
        label: { show: false },
        data
      }
    ]
  }
})

const areaRank = ref([
  { name: '2层研发区', value: 1420, percent: 88 },
  { name: '3层办公区', value: 680, percent: 42 },
  { name: '1层大堂', value: 420, percent: 26 },
  { name: '地下停车场', value: 280, percent: 17 },
  { name: '会议室区域', value: 210, percent: 13 }
])

const alarmTypeStats = ref([
  { type: '电力异常', count: 12, percent: 46.2, color: 'var(--el-color-danger)' },
  { type: '用水异常', count: 7, percent: 26.9, color: 'var(--el-color-warning)' },
  { type: '设备离线', count: 4, percent: 15.4, color: 'var(--el-color-primary)' },
  { type: '燃气异常', count: 2, percent: 7.7, color: 'var(--el-color-info)' },
  { type: '其他异常', count: 1, percent: 3.8, color: 'var(--el-color-success)' }
])

const switchDatePeriod = (period: Exclude<DatePeriod, 'custom'>) => {
  datePeriod.value = period
  customRange.value = null
}

const buildDateRange = () => {
  const today = dayjs()
  if (datePeriod.value === 'custom' && customRange.value) {
    return {
      startDate: dayjs(customRange.value[0]).format('YYYY-MM-DD'),
      endDate: dayjs(customRange.value[1]).format('YYYY-MM-DD')
    }
  }
  if (datePeriod.value === 'today') {
    const d = today.format('YYYY-MM-DD')
    return { startDate: d, endDate: d }
  }
  if (datePeriod.value === 'week') {
    return {
      startDate: today.startOf('week').format('YYYY-MM-DD'),
      endDate: today.endOf('week').format('YYYY-MM-DD')
    }
  }
  if (datePeriod.value === 'month') {
    return {
      startDate: today.startOf('month').format('YYYY-MM-DD'),
      endDate: today.endOf('month').format('YYYY-MM-DD')
    }
  }
  // year
  return {
    startDate: today.startOf('year').format('YYYY-MM-DD'),
    endDate: today.endOf('year').format('YYYY-MM-DD')
  }
}

const loadOverviewByRange = async () => {
  const { startDate, endDate } = buildDateRange()
  loading.value = true
  try {
    overview.value = await EnergyApi.getOverviewByRange(startDate, endDate)
  } catch (e) {
    console.error('加载能耗总览数据失败', e)
  } finally {
    loading.value = false
  }
}

const applyCustomRange = () => {
  if (!customRange.value) {
    ElMessage.warning('请选择自定义日期范围')
    return
  }
  datePeriod.value = 'custom'
  ElMessage.success('已应用自定义日期范围')
  loadOverviewByRange()
}

const refreshDashboard = () => {
  loadOverviewByRange()
  ElMessage.success('已刷新')
}

const toggleFullscreen = async () => {
  try {
    if (document.fullscreenElement) {
      await document.exitFullscreen()
      return
    }
    await document.documentElement.requestFullscreen()
  } catch (e) {
    ElMessage.warning('浏览器不支持全屏或已被拦截')
  }
}

onMounted(() => {
  loadOverviewByRange()
})

watch([datePeriod, customRange], () => {
  if (datePeriod.value === 'custom' && !customRange.value) return
  loadOverviewByRange()
})
</script>

<template>
  <EnergyPageContainer>
    <EnergyPageHeader title="数据总览" subtitle="智慧能源管理平台">
      <template #actions>
        <ElButton type="primary" @click="toggleFullscreen">
          <Icon icon="ep:full-screen" class="mr-5px" /> 全屏显示
        </ElButton>
        <ElButton @click="refreshDashboard"><Icon icon="ep:refresh" class="mr-5px" /> 刷新</ElButton>
      </template>
    </EnergyPageHeader>

    <ElCard shadow="never" class="date-filter-card" v-loading="loading">
      <div class="date-filter-content">
        <div class="date-filter-left">
          <span class="date-filter-label">日期筛选：</span>
          <ElButtonGroup>
            <ElButton :type="datePeriod === 'today' ? 'primary' : 'default'" @click="switchDatePeriod('today')">
              今日
            </ElButton>
            <ElButton :type="datePeriod === 'week' ? 'primary' : 'default'" @click="switchDatePeriod('week')">
              本周
            </ElButton>
            <ElButton :type="datePeriod === 'month' ? 'primary' : 'default'" @click="switchDatePeriod('month')">
              本月
            </ElButton>
            <ElButton :type="datePeriod === 'year' ? 'primary' : 'default'" @click="switchDatePeriod('year')">
              本年
            </ElButton>
          </ElButtonGroup>
        </div>
        <div class="date-filter-right">
          <ElDatePicker v-model="customRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
          <ElButton @click="applyCustomRange"><Icon icon="ep:search" class="mr-5px" /> 应用</ElButton>
          <span class="current-period">当前：{{ periodText }}</span>
        </div>
      </div>
    </ElCard>

    <div class="core-cards">
      <div v-for="card in coreCards" :key="card.key" class="core-card" :class="`core-card--${card.theme}`">
        <div class="core-card__title">{{ card.icon }} {{ card.title }}</div>
        <div class="core-card__value">{{ card.value }}</div>
        <div class="core-card__sub">{{ card.sub }}</div>
      </div>
    </div>

    <div class="expand-cards">
      <ElCard shadow="never" class="expand-card">
        <div class="expand-card__title">⛏️ 能耗折标准煤</div>
        <div class="expand-grid">
          <div class="expand-item">
            <div class="expand-value">0.39</div>
            <div class="expand-label">总量(tce)</div>
          </div>
          <div class="expand-item">
            <div class="expand-value">0.33</div>
            <div class="expand-label">电能(tce)</div>
          </div>
          <div class="expand-item">
            <div class="expand-value">0.005</div>
            <div class="expand-label">水量(tce)</div>
          </div>
          <div class="expand-item">
            <div class="expand-value">0.064</div>
            <div class="expand-label">燃气(tce)</div>
          </div>
        </div>
        <ElProgress :percentage="100" :stroke-width="8" :show-text="false" class="mt-10px" />
      </ElCard>

      <ElCard shadow="never" class="expand-card">
        <div class="expand-card__title">🌍 二氧化碳排放总量</div>
        <div class="co2-row">
          <div class="co2-left">
            <div class="co2-main">
              <span class="co2-tag">CO₂</span>
              <span class="co2-value">2,304</span>
              <span class="co2-unit">kgCO₂e</span>
            </div>
            <div class="co2-sub">排放总量</div>
          </div>
          <div class="co2-right">
            <span>环比率</span>
            <span class="co2-rate">+2.1%</span>
          </div>
        </div>
      </ElCard>

      <ElCard shadow="never" class="expand-card">
        <div class="expand-card__title">📏 单位面积能耗(kgce/㎡)</div>
        <div class="number-blocks">
          <div v-for="(n, i) in ['0', '0', '3', '9', '2']" :key="i" class="number-block">{{ n }}</div>
        </div>
        <div class="area-sub">建筑面积：<strong>100㎡</strong></div>
      </ElCard>
    </div>

    <div class="triple-cards">
      <ElCard shadow="never">
        <div class="triple-title">📊 能耗占比</div>
        <div class="ratio-chart">
          <Echart :options="ratioChartOptions" height="220px" />
        </div>
        <div class="ratio-legend">
          <div class="legend-item">
            <span class="legend-dot" style="background: #f59e0b"></span>
            <span class="legend-name">电力</span>
            <span class="legend-value">84.6%</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot" style="background: #06b6d4"></span>
            <span class="legend-name">水务</span>
            <span class="legend-value">1.3%</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot" style="background: #f97316"></span>
            <span class="legend-name">燃气</span>
            <span class="legend-value">13.8%</span>
          </div>
        </div>
      </ElCard>

      <ElCard shadow="never">
        <div class="triple-title">🏢 区域用电排名</div>
        <div class="rank-list">
          <div v-for="(item, index) in areaRank" :key="item.name" class="rank-item">
            <div class="rank-index">{{ index + 1 }}</div>
            <div class="rank-main">
              <div class="rank-name">{{ item.name }}</div>
              <div class="rank-sub">用电 {{ item.value }} kWh</div>
            </div>
            <ElProgress :percentage="item.percent" :stroke-width="8" :show-text="false" class="rank-progress" />
          </div>
        </div>
      </ElCard>

      <ElCard shadow="never">
        <div class="triple-title">📋 告警类型分布</div>
        <ElTable :data="alarmTypeStats" size="small">
          <ElTableColumn label="告警类型" prop="type" min-width="120" />
          <ElTableColumn label="数量" prop="count" width="80" />
          <ElTableColumn label="占比" min-width="160">
            <template #default="{ row }">
              <div class="alarm-percent">
                <ElProgress
                  :percentage="row.percent"
                  :stroke-width="8"
                  :show-text="false"
                  :color="row.color"
                  class="alarm-progress"
                />
                <span class="alarm-text">{{ row.percent.toFixed(1) }}%</span>
              </div>
            </template>
          </ElTableColumn>
        </ElTable>
      </ElCard>
    </div>
  </EnergyPageContainer>
</template>

<style scoped lang="scss">
.date-filter-card :deep(.el-card__body) {
  padding: 16px 18px;
}

.date-filter-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.date-filter-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.date-filter-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.date-filter-label {
  color: var(--el-text-color-secondary);
  font-weight: 600;
}

.current-period {
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.core-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.core-card {
  border-radius: 16px;
  padding: 22px 22px;
  color: #fff;
}

.core-card__title {
  font-size: 14px;
  opacity: 0.92;
  margin-bottom: 10px;
  font-weight: 600;
}

.core-card__value {
  font-size: 34px;
  font-weight: 800;
  margin-bottom: 6px;
}

.core-card__sub {
  font-size: 13px;
  opacity: 0.9;
}

.core-card--electric {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}
.core-card--water {
  background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
}
.core-card--gas {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
}
.core-card--cost {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.expand-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.expand-card :deep(.el-card__body) {
  padding: 18px 18px;
}

.expand-card__title {
  font-size: 16px;
  font-weight: 800;
  color: var(--el-text-color-primary);
  margin-bottom: 12px;
}

.expand-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 18px;
}

.expand-value {
  font-size: 26px;
  font-weight: 800;
  color: var(--el-text-color-primary);
}

.expand-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.co2-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.co2-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.co2-tag {
  padding: 2px 8px;
  border-radius: 6px;
  background: rgba(59, 130, 246, 0.12);
  color: var(--el-color-info);
  font-weight: 700;
  font-size: 12px;
}

.co2-value {
  font-size: 26px;
  font-weight: 800;
  color: var(--el-text-color-primary);
}

.co2-unit {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.co2-sub {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.co2-right {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--el-text-color-secondary);
}

.co2-rate {
  color: var(--el-color-danger);
  font-weight: 700;
}

.number-blocks {
  display: flex;
  gap: 6px;
  margin-top: 6px;
}

.number-block {
  width: 30px;
  height: 42px;
  border-radius: 10px;
  background: rgba(30, 64, 175, 0.14);
  color: var(--el-text-color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 18px;
  border: 1px solid var(--el-border-color-lighter);
}

.area-sub {
  margin-top: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.triple-cards {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
}

.triple-title {
  font-weight: 800;
  color: var(--el-text-color-primary);
  margin-bottom: 10px;
}

.ratio-legend {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-name {
  flex: 1;
  color: var(--el-text-color-regular);
}

.legend-value {
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rank-index {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--el-color-danger);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 12px;
  flex-shrink: 0;
}

.rank-item:nth-child(2) .rank-index {
  background: var(--el-color-warning);
}
.rank-item:nth-child(3) .rank-index {
  background: var(--el-color-info);
}
.rank-item:nth-child(n + 4) .rank-index {
  background: var(--el-text-color-secondary);
}

.rank-main {
  flex: 1;
}

.rank-name {
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.rank-sub {
  margin-top: 2px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.rank-progress {
  width: 120px;
  flex-shrink: 0;
}

.alarm-percent {
  display: flex;
  align-items: center;
  gap: 10px;
}

.alarm-progress {
  flex: 1;
}

.alarm-text {
  width: 56px;
  text-align: right;
  color: var(--el-text-color-regular);
}

@media (max-width: 1200px) {
  .core-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .expand-cards {
    grid-template-columns: 1fr;
  }
  .triple-cards {
    grid-template-columns: 1fr;
  }
}
</style>
