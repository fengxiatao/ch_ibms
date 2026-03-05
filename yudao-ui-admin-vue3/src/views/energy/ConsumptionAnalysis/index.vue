<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import { Echart } from '@/components/Echart'
import EnergyPageContainer from '../components/EnergyPageContainer.vue'
import EnergyPageHeader from '../components/EnergyPageHeader.vue'
import * as EnergyApi from '@/api/iot/building/energy'
import type {
  IbmsEnergyOverviewVO,
  IbmsEnergyStatisticsDailyVO
} from '@/api/iot/building/energy'

defineOptions({ name: 'EnergyConsumptionAnalysis' })
// 数据来源与数据总览、报表管理一致：/iot/building/energy 接口，表 ibms_energy_statistics_daily

type Granularity = 'today' | 'month' | 'year' | 'custom'
type AnalysisType = 'trend' | 'cost'

const granularity = ref<Granularity>('today')
const analysisType = ref<AnalysisType>('trend')
const customRange = ref<[Date, Date] | null>(null)

// 后端统计数据
const loading = ref(false)
const overview = ref<IbmsEnergyOverviewVO | null>(null)
const electricityStats = ref<IbmsEnergyStatisticsDailyVO[]>([])
const waterStats = ref<IbmsEnergyStatisticsDailyVO[]>([])
const gasStats = ref<IbmsEnergyStatisticsDailyVO[]>([])

// 价格估算（测试用，方便演示费用曲线）
const estimatePrices = {
  electricity: 1.0,
  water: 4.5,
  gas: 3.0
}

const nowText = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'))
let timer: number | undefined

onMounted(() => {
  timer = window.setInterval(() => {
    nowText.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  }, 1000)
  loadAnalysisData()
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})

const buildDateRange = () => {
  const today = dayjs()
  if (granularity.value === 'custom' && customRange.value) {
    return {
      startDate: dayjs(customRange.value[0]).format('YYYY-MM-DD'),
      endDate: dayjs(customRange.value[1]).format('YYYY-MM-DD')
    }
  }
  if (granularity.value === 'today') {
    const d = today.format('YYYY-MM-DD')
    return { startDate: d, endDate: d }
  }
  if (granularity.value === 'month') {
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

const sumUsage = (list: IbmsEnergyStatisticsDailyVO[]) =>
  list.reduce((total, item) => total + (item.dailyUsage || 0), 0)

const formatNumber = (value?: number, fractionDigits = 1) => {
  const v = value ?? 0
  return v.toLocaleString('zh-CN', { maximumFractionDigits: fractionDigits })
}

const getChangeClass = (value?: number | null) => {
  if (!value) return ''
  return value > 0 ? 'stat-change--up' : 'stat-change--down'
}

const getChangeText = (value?: number | null) => {
  if (value === null || value === undefined || value === 0) return '持平 同比'
  const sign = value > 0 ? '↑' : '↓'
  return `${sign} ${Math.abs(value).toFixed(1)}% 同比`
}

const totalElectricity = computed(() => sumUsage(electricityStats.value))
const totalWater = computed(() => sumUsage(waterStats.value))
const totalGas = computed(() => sumUsage(gasStats.value))
const totalCost = computed(
  () =>
    totalElectricity.value * estimatePrices.electricity +
    totalWater.value * estimatePrices.water +
    totalGas.value * estimatePrices.gas
)

const electricityYoy = computed(() => overview.value?.electricityYoy ?? 0)
const waterYoy = computed(() => overview.value?.waterYoy ?? 0)

const loadAnalysisData = async () => {
  const { startDate, endDate } = buildDateRange()
  loading.value = true
  try {
    const [ov, elec, water, gas] = await Promise.all([
      EnergyApi.getOverview(),
      EnergyApi.getStatisticsByTypeAndDateRange(1, startDate, endDate),
      EnergyApi.getStatisticsByTypeAndDateRange(2, startDate, endDate),
      EnergyApi.getStatisticsByTypeAndDateRange(3, startDate, endDate)
    ])
    overview.value = ov as IbmsEnergyOverviewVO
    electricityStats.value = (elec || []) as IbmsEnergyStatisticsDailyVO[]
    waterStats.value = (water || []) as IbmsEnergyStatisticsDailyVO[]
    gasStats.value = (gas || []) as IbmsEnergyStatisticsDailyVO[]
  } catch (e) {
    console.error('加载能耗分析数据失败', e)
  } finally {
    loading.value = false
  }
}

watch([granularity, customRange], () => {
  if (granularity.value === 'custom' && !customRange.value) return
  loadAnalysisData()
})

const timeRangeText = computed(() => {
  if (granularity.value === 'custom' && customRange.value) {
    return `${dayjs(customRange.value[0]).format('YYYY-MM-DD')} 至 ${dayjs(customRange.value[1]).format(
      'YYYY-MM-DD'
    )}`
  }
  if (granularity.value === 'today') return dayjs().format('YYYY-MM-DD')
  if (granularity.value === 'month') return dayjs().format('YYYY-MM')
  return dayjs().format('YYYY')
})

const trendChartOptions = computed<EChartsOption>(() => {
  const stats = electricityStats.value
  const dates = [
    ...new Set(stats.map((item) => item.statisticsDate || '').filter(Boolean))
  ].sort()
  const x = dates.length ? dates : Array.from({ length: 12 }).map((_, i) => `${i + 1}`)
  const usageData =
    dates.length
      ? dates.map((d) =>
          stats
            .filter((s) => s.statisticsDate === d)
            .reduce((sum, s) => sum + (s.dailyUsage || 0), 0)
        )
      : []
  const costData = usageData.map((v) => v * estimatePrices.electricity)

  const mkSeries = (name: string, color: string, data: number[]) => ({
    name,
    type: 'line',
    smooth: true,
    symbol: 'circle',
    symbolSize: 6,
    lineStyle: { width: 2, color },
    itemStyle: { color },
    areaStyle: { color: `${color}22` },
    data
  })
  return {
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { left: 16, right: 16, top: 40, bottom: 10, containLabel: true },
    xAxis: { type: 'category', data: x },
    yAxis: { type: 'value' },
    series: [mkSeries('用量', '#f59e0b', usageData), mkSeries('费用', '#10b981', costData)]
  } as EChartsOption
})

const waterChartOptions = computed<EChartsOption>(() => {
  const stats = waterStats.value
  const dates = [
    ...new Set(stats.map((item) => item.statisticsDate || '').filter(Boolean))
  ].sort()
  const x = dates.length ? dates : Array.from({ length: 12 }).map((_, i) => `${i + 1}`)
  const usageData =
    dates.length
      ? dates.map((d) =>
          stats
            .filter((s) => s.statisticsDate === d)
            .reduce((sum, s) => sum + (s.dailyUsage || 0), 0)
        )
      : []
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 16, right: 16, top: 20, bottom: 10, containLabel: true },
    xAxis: { type: 'category', data: x },
    yAxis: { type: 'value' },
    series: [
      {
        name: '用水量',
        type: 'bar',
        barWidth: 12,
        itemStyle: { borderRadius: [6, 6, 0, 0], color: '#06b6d4' },
        data: usageData
      }
    ]
  } as EChartsOption
})

const setGranularity = (v: Granularity) => {
  granularity.value = v
  if (v !== 'custom') {
    customRange.value = null
  }
}

const applyCustomRange = () => {
  if (!customRange.value) {
    ElMessage.warning('请选择自定义日期范围')
    return
  }
  ElMessage.success('已更新分析周期')
  loadAnalysisData()
}
</script>

<template>
  <EnergyPageContainer>
    <EnergyPageHeader title="能耗分析" subtitle="趋势与费用分析">
      <template #actions>
        <ElTag effect="plain"><Icon icon="ep:calendar" class="mr-5px" /> {{ nowText }}</ElTag>
        <ElTag type="info" effect="plain"><Icon icon="ep:user" class="mr-5px" /> 管理员</ElTag>
      </template>
    </EnergyPageHeader>

    <ElCard shadow="never" v-loading="loading">
      <div class="filter-row">
        <div class="filter-left">
          <span class="filter-label">时间粒度：</span>
          <ElButtonGroup>
            <ElButton :type="granularity === 'today' ? 'primary' : 'default'" @click="setGranularity('today')">
              今日
            </ElButton>
            <ElButton :type="granularity === 'month' ? 'primary' : 'default'" @click="setGranularity('month')">
              本月
            </ElButton>
            <ElButton :type="granularity === 'year' ? 'primary' : 'default'" @click="setGranularity('year')">
              本年
            </ElButton>
            <ElButton :type="granularity === 'custom' ? 'primary' : 'default'" @click="setGranularity('custom')">
              自定义
            </ElButton>
          </ElButtonGroup>

          <div v-if="granularity === 'custom'" class="custom-range">
            <ElDatePicker
              v-model="customRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            />
            <ElButton type="primary" @click="applyCustomRange">确定</ElButton>
          </div>

          <ElTag effect="plain" class="range-tag">{{ timeRangeText }}</ElTag>
        </div>

        <div class="filter-right">
          <ElButtonGroup>
            <ElButton :type="analysisType === 'trend' ? 'primary' : 'default'" @click="analysisType = 'trend'">
              能耗趋势
            </ElButton>
            <ElButton :type="analysisType === 'cost' ? 'primary' : 'default'" @click="analysisType = 'cost'">
              用费分析
            </ElButton>
          </ElButtonGroup>
        </div>
      </div>
    </ElCard>

    <template v-if="analysisType === 'trend'">
      <div class="stat-grid">
        <ElCard shadow="never" class="stat-card">
          <div class="stat-icon stat-icon--electric">⚡</div>
          <div class="stat-main">
            <div class="stat-label">总用电量</div>
            <div class="stat-value">
              {{ formatNumber(totalElectricity, 1) }}<span class="stat-unit">kWh</span>
            </div>
            <div class="stat-change" :class="getChangeClass(electricityYoy)">
              {{ getChangeText(electricityYoy) }}
            </div>
          </div>
        </ElCard>
        <ElCard shadow="never" class="stat-card">
          <div class="stat-icon stat-icon--water">💧</div>
          <div class="stat-main">
            <div class="stat-label">总用水量</div>
            <div class="stat-value">
              {{ formatNumber(totalWater, 1) }}<span class="stat-unit">m³</span>
            </div>
            <div class="stat-change" :class="getChangeClass(waterYoy)">
              {{ getChangeText(waterYoy) }}
            </div>
          </div>
        </ElCard>
        <ElCard shadow="never" class="stat-card">
          <div class="stat-icon stat-icon--gas">🔥</div>
          <div class="stat-main">
            <div class="stat-label">总用气量</div>
            <div class="stat-value">
              {{ formatNumber(totalGas, 1) }}<span class="stat-unit">m³</span>
            </div>
            <div class="stat-change" :class="getChangeClass(0)">
              {{ getChangeText(0) }}
            </div>
          </div>
        </ElCard>
        <ElCard shadow="never" class="stat-card">
          <div class="stat-icon stat-icon--cost">💰</div>
          <div class="stat-main">
            <div class="stat-label">总费用</div>
            <div class="stat-value">
              ¥{{ formatNumber(totalCost, 2) }}<span class="stat-unit"></span>
            </div>
            <div class="stat-change" :class="getChangeClass(0)">
              {{ getChangeText(0) }}
            </div>
          </div>
        </ElCard>
      </div>

      <div class="chart-grid">
        <ElCard shadow="never">
          <div class="chart-title">⚡ 用电趋势分析</div>
          <div class="chart-subtitle">用电量/费用汇总统计</div>
          <Echart :options="trendChartOptions" height="320px" />
        </ElCard>
        <ElCard shadow="never">
          <div class="chart-title">💧 用水趋势分析</div>
          <div class="chart-subtitle">用水量汇总统计</div>
          <Echart :options="waterChartOptions" height="320px" />
        </ElCard>
      </div>
    </template>

    <template v-else>
      <ElCard shadow="never">
        <div class="cost-header">
          <div>
            <div class="cost-title">💰 费用分析概览</div>
            <div class="cost-subtitle">统计周期：{{ timeRangeText }} | 数据更新时间：{{ nowText }}</div>
          </div>
          <div class="cost-summary">
            <div class="summary-item">
              <div class="summary-label">电费总计</div>
              <div class="summary-value summary-value--primary">¥0.85万</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">水费总计</div>
              <div class="summary-value summary-value--primary">¥0.21万</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">总费用</div>
              <div class="summary-value summary-value--danger">¥1.06万</div>
            </div>
          </div>
        </div>
      </ElCard>

      <div class="cost-grid">
        <ElCard shadow="never">
          <div class="cost-card-title">⚡ 电费详情</div>
          <div class="cost-card-sub">平均电价：1.03元/kWh</div>
          <div class="period-grid">
            <div class="period-item">
              <div class="period-name" style="color: var(--el-color-danger)">🔺 尖时段</div>
              <div class="period-sub">电价：2.00元</div>
              <div class="period-value" style="color: var(--el-color-danger)">0.25万</div>
              <div class="period-label">电费（元）</div>
              <div class="period-value">0.13万</div>
              <div class="period-label">用电量（kWh）</div>
            </div>
            <div class="period-item">
              <div class="period-name" style="color: #f97316">🔶 峰时段</div>
              <div class="period-sub">电价：1.50元</div>
              <div class="period-value" style="color: #f97316">0.20万</div>
              <div class="period-label">电费（元）</div>
              <div class="period-value">0.13万</div>
              <div class="period-label">用电量（kWh）</div>
            </div>
            <div class="period-item">
              <div class="period-name" style="color: #eab308">🔸 平时段</div>
              <div class="period-sub">电价：1.00元</div>
              <div class="period-value" style="color: #eab308">0.24万</div>
              <div class="period-label">电费（元）</div>
              <div class="period-value">0.24万</div>
              <div class="period-label">用电量（kWh）</div>
            </div>
            <div class="period-item">
              <div class="period-name" style="color: var(--el-color-success)">🔹 谷时段</div>
              <div class="period-sub">电价：0.50元</div>
              <div class="period-value" style="color: var(--el-color-success)">0.16万</div>
              <div class="period-label">电费（元）</div>
              <div class="period-value">0.33万</div>
              <div class="period-label">用电量（kWh）</div>
            </div>
          </div>
        </ElCard>

        <ElCard shadow="never">
          <div class="cost-card-title">💧 水费详情</div>
          <div class="cost-card-sub">水价：2.85元/m³</div>
          <div class="water-grid">
            <div class="water-item">
              <div class="water-value" style="color: #06b6d4">580</div>
              <div class="water-label">水费（元）</div>
            </div>
            <div class="water-item">
              <div class="water-value" style="color: #06b6d4">203</div>
              <div class="water-label">用水量（吨/m³）</div>
            </div>
          </div>
        </ElCard>
      </div>
    </template>
  </EnergyPageContainer>
</template>

<style scoped lang="scss">
.filter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  color: var(--el-text-color-secondary);
  font-weight: 700;
}

.custom-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.range-tag {
  margin-left: 4px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 800;
  color: #fff;
  flex-shrink: 0;
}

.stat-icon--electric {
  background: #f59e0b;
}
.stat-icon--water {
  background: #06b6d4;
}
.stat-icon--gas {
  background: #f97316;
}
.stat-icon--cost {
  background: #10b981;
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: 700;
}

.stat-value {
  margin-top: 2px;
  font-size: 20px;
  font-weight: 900;
  color: var(--el-text-color-primary);
}

.stat-unit {
  margin-left: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: 700;
}

.stat-change {
  margin-top: 2px;
  font-size: 12px;
  font-weight: 700;
}

.stat-change--up {
  color: var(--el-color-success);
}

.stat-change--down {
  color: var(--el-color-danger);
}

.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-title {
  font-weight: 900;
  color: var(--el-text-color-primary);
}

.chart-subtitle {
  margin-top: 2px;
  margin-bottom: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.cost-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.cost-title {
  font-size: 16px;
  font-weight: 900;
  color: var(--el-text-color-primary);
}

.cost-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.cost-summary {
  display: flex;
  gap: 16px;
}

.summary-item {
  min-width: 120px;
}

.summary-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: 700;
}

.summary-value {
  margin-top: 4px;
  font-weight: 900;
  font-size: 18px;
}

.summary-value--primary {
  color: var(--el-color-primary);
}

.summary-value--danger {
  color: var(--el-color-danger);
}

.cost-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.cost-card-title {
  font-weight: 900;
  color: var(--el-text-color-primary);
}

.cost-card-sub {
  margin-top: 4px;
  margin-bottom: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.period-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.period-item {
  padding: 12px 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  background: var(--el-bg-color);
}

.period-name {
  font-weight: 900;
}

.period-sub {
  margin-top: 2px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.period-value {
  margin-top: 10px;
  font-size: 16px;
  font-weight: 900;
  color: var(--el-text-color-primary);
}

.period-label {
  margin-top: 2px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.water-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.water-item {
  padding: 12px 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
}

.water-value {
  font-size: 18px;
  font-weight: 900;
}

.water-label {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .chart-grid {
    grid-template-columns: 1fr;
  }
  .cost-grid {
    grid-template-columns: 1fr;
  }
  .period-grid {
    grid-template-columns: 1fr;
  }
}
</style>
