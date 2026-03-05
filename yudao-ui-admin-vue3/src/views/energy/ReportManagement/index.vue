<script setup lang="ts">
import dayjs from 'dayjs'
import EnergyPageContainer from '../components/EnergyPageContainer.vue'
import EnergyPageHeader from '../components/EnergyPageHeader.vue'
import * as EnergyApi from '@/api/iot/building/energy'

defineOptions({ name: 'EnergyReportManagement' })

// 与数据总览、能耗分析同一数据源：ibms_energy_statistics_daily
const ESTIMATE_PRICES = { electric: 1.0, water: 4.5, gas: 3.0 }

type TabKey = 'energyData' | 'energyCost'
type TimeDimension = 'day' | 'month' | 'year'

const nowText = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'))
let timer: number | undefined
onMounted(() => {
  timer = window.setInterval(() => {
    nowText.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  }, 1000)
})
onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})

const activeTab = ref<TabKey>('energyData')
const reportLoading = ref(false)

const common = reactive({
  timeDimension: 'day' as TimeDimension,
  region: 'all',
  range: null as [Date, Date] | null,
  compareYoY: false,
  compareMoM: false
})

const energyDataTypes = ref<string[]>(['electric', 'water', 'gas'])
const energyCostTypes = ref<string[]>(['electric', 'water', 'gas'])

const regions = [
  { label: '所有区域', value: 'all' },
  { label: '1层大堂', value: '1' },
  { label: '2层办公区', value: '2' },
  { label: '3层办公区', value: '3' },
  { label: '地下车库', value: '4' }
]

interface ReportRow {
  time: string
  region: string
  electric: number
  water: number
  gas: number
  yoy?: number
  mom?: number
}

interface CostRow {
  time: string
  region: string
  electric: number
  water: number
  gas: number
  yoy?: number
  mom?: number
}

const dataRows = ref<ReportRow[]>([])
const costRows = ref<CostRow[]>([])

const totals = computed(() => {
  const rows = activeTab.value === 'energyData' ? dataRows.value : costRows.value
  return {
    electric: rows.reduce((sum, r) => sum + (r.electric || 0), 0),
    water: rows.reduce((sum, r) => sum + (r.water || 0), 0),
    gas: rows.reduce((sum, r) => sum + (r.gas || 0), 0)
  }
})

function getDefaultRange(): { startDate: string; endDate: string } {
  const today = dayjs()
  if (common.timeDimension === 'day') {
    return {
      startDate: today.subtract(6, 'day').format('YYYY-MM-DD'),
      endDate: today.format('YYYY-MM-DD')
    }
  }
  if (common.timeDimension === 'month') {
    return {
      startDate: today.startOf('month').format('YYYY-MM-DD'),
      endDate: today.endOf('month').format('YYYY-MM-DD')
    }
  }
  return {
    startDate: today.startOf('year').format('YYYY-MM-DD'),
    endDate: today.endOf('year').format('YYYY-MM-DD')
  }
}

const generate = async () => {
  const range =
    common.range?.length === 2
      ? {
          startDate: dayjs(common.range[0]).format('YYYY-MM-DD'),
          endDate: dayjs(common.range[1]).format('YYYY-MM-DD')
        }
      : getDefaultRange()
  const regionLabel = regions.find((r) => r.value === common.region)?.label || '所有区域'

  reportLoading.value = true
  try {
    const [elecList, waterList, gasList] = await Promise.all([
      EnergyApi.getStatisticsByTypeAndDateRange(1, range.startDate, range.endDate),
      EnergyApi.getStatisticsByTypeAndDateRange(2, range.startDate, range.endDate),
      EnergyApi.getStatisticsByTypeAndDateRange(3, range.startDate, range.endDate)
    ])

    const byDate: Record<string, { electric: number; water: number; gas: number }> = {}
    const add = (
      list: { statisticsDate?: string; dailyUsage?: number }[],
      type: 'electric' | 'water' | 'gas'
    ) => {
      ;(list || []).forEach((item) => {
        const d = item.statisticsDate || ''
        if (!d) return
        if (!byDate[d]) byDate[d] = { electric: 0, water: 0, gas: 0 }
        byDate[d][type] += Number(item.dailyUsage) || 0
      })
    }
    add((elecList || []) as any[], 'electric')
    add((waterList || []) as any[], 'water')
    add((gasList || []) as any[], 'gas')

    const dates = [...new Set(Object.keys(byDate))].sort()
    let rows: ReportRow[] = dates.map((time) => ({
      time,
      region: regionLabel,
      electric: energyDataTypes.value.includes('electric') ? byDate[time]?.electric ?? 0 : 0,
      water: energyDataTypes.value.includes('water') ? byDate[time]?.water ?? 0 : 0,
      gas: energyDataTypes.value.includes('gas') ? byDate[time]?.gas ?? 0 : 0
    }))

    if (common.timeDimension === 'month') {
      const byMonth: Record<string, ReportRow> = {}
      rows.forEach((r) => {
        const month = r.time.slice(0, 7)
        if (!byMonth[month]) byMonth[month] = { time: month, region: regionLabel, electric: 0, water: 0, gas: 0 }
        byMonth[month].electric += r.electric
        byMonth[month].water += r.water
        byMonth[month].gas += r.gas
      })
      rows = Object.keys(byMonth).sort().map((month) => ({ ...byMonth[month], region: regionLabel }))
    } else if (common.timeDimension === 'year') {
      const byYear: Record<string, ReportRow> = {}
      rows.forEach((r) => {
        const year = r.time.slice(0, 4)
        if (!byYear[year]) byYear[year] = { time: year, region: regionLabel, electric: 0, water: 0, gas: 0 }
        byYear[year].electric += r.electric
        byYear[year].water += r.water
        byYear[year].gas += r.gas
      })
      rows = Object.keys(byYear).sort().map((year) => ({ ...byYear[year], region: regionLabel }))
    }

    dataRows.value = rows
    costRows.value = rows.map((r) => ({
      time: r.time,
      region: r.region,
      electric: energyCostTypes.value.includes('electric') ? r.electric * ESTIMATE_PRICES.electric : 0,
      water: energyCostTypes.value.includes('water') ? r.water * ESTIMATE_PRICES.water : 0,
      gas: energyCostTypes.value.includes('gas') ? r.gas * ESTIMATE_PRICES.gas : 0
    }))
    ElMessage.success('已从能耗统计接口生成报表，与数据总览、能耗分析同源')
  } catch (e) {
    console.error('生成报表失败', e)
    ElMessage.error('拉取能耗数据失败，请检查网络或权限')
  } finally {
    reportLoading.value = false
  }
}

const preview = () => {
  ElMessage.info('预览功能待对接')
}

const exportReport = () => {
  ElMessage.info('导出功能待对接')
}
</script>

<template>
  <EnergyPageContainer>
    <EnergyPageHeader title="报表管理" subtitle="能耗数据/费用报表生成与导出">
      <template #actions>
        <ElTag effect="plain"><Icon icon="ep:calendar" class="mr-5px" /> {{ nowText }}</ElTag>
      </template>
    </EnergyPageHeader>

    <ElTabs v-model="activeTab" class="report-tabs">
      <ElTabPane label="能耗数据报表" name="energyData" />
      <ElTabPane label="能耗费用报表" name="energyCost" />
    </ElTabs>

    <ElCard shadow="never">
      <ElForm label-position="top">
        <div class="filter-grid">
          <ElFormItem label="时间维度">
            <ElSelect v-model="common.timeDimension">
              <ElOption label="日" value="day" />
              <ElOption label="月" value="month" />
              <ElOption label="年" value="year" />
            </ElSelect>
          </ElFormItem>

          <ElFormItem label="设备/区域">
            <ElSelect v-model="common.region">
              <ElOption v-for="r in regions" :key="r.value" :label="r.label" :value="r.value" />
            </ElSelect>
          </ElFormItem>

          <ElFormItem class="type-checkboxes" :label="activeTab === 'energyData' ? '设备类型' : '费用类型'">
            <ElCheckboxGroup v-if="activeTab === 'energyData'" v-model="energyDataTypes">
              <ElCheckbox label="electric">电力</ElCheckbox>
              <ElCheckbox label="water">水量</ElCheckbox>
              <ElCheckbox label="gas">燃气</ElCheckbox>
            </ElCheckboxGroup>
            <ElCheckboxGroup v-else v-model="energyCostTypes">
              <ElCheckbox label="electric">电费</ElCheckbox>
              <ElCheckbox label="water">水费</ElCheckbox>
              <ElCheckbox label="gas">燃气费</ElCheckbox>
            </ElCheckboxGroup>
          </ElFormItem>

          <ElFormItem label="对比设置">
            <div class="compare-row">
              <ElCheckbox v-model="common.compareYoY">同比</ElCheckbox>
              <ElCheckbox v-model="common.compareMoM">环比</ElCheckbox>
            </div>
          </ElFormItem>

          <ElFormItem label="时间范围">
            <ElDatePicker
              v-model="common.range"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            />
          </ElFormItem>

          <ElFormItem label=" ">
            <ElButton type="primary" class="w-full" :loading="reportLoading" @click="generate"><Icon icon="ep:document" class="mr-5px" /> 生成报表</ElButton>
          </ElFormItem>
        </div>
      </ElForm>
    </ElCard>

    <ElCard shadow="never">
      <div class="result-header">
        <div class="result-title">
          {{ activeTab === 'energyData' ? '能耗数据分析结果' : '能耗费用分析结果' }}
        </div>
        <div class="result-actions">
          <ElButton @click="preview"><Icon icon="ep:view" class="mr-5px" /> 预览报表</ElButton>
          <ElButton type="primary" @click="exportReport"><Icon icon="ep:download" class="mr-5px" /> 导出报表</ElButton>
        </div>
      </div>

      <div class="result-stats">
        <div class="stat-card">
          <div class="stat-label">{{ activeTab === 'energyData' ? '总用电量' : '总电费' }}</div>
          <div class="stat-value">{{ totals.electric.toLocaleString() }}</div>
          <div class="stat-unit">{{ activeTab === 'energyData' ? 'kWh' : '元' }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">{{ activeTab === 'energyData' ? '总用水量' : '总水费' }}</div>
          <div class="stat-value">{{ totals.water.toLocaleString() }}</div>
          <div class="stat-unit">{{ activeTab === 'energyData' ? 'm³' : '元' }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">{{ activeTab === 'energyData' ? '总用气量' : '总燃气费' }}</div>
          <div class="stat-value">{{ totals.gas.toLocaleString() }}</div>
          <div class="stat-unit">{{ activeTab === 'energyData' ? 'm³' : '元' }}</div>
        </div>
      </div>

      <ElTable v-if="activeTab === 'energyData'" :data="dataRows" stripe>
        <ElTableColumn label="时间" prop="time" width="140" />
        <ElTableColumn label="区域" prop="region" width="140" />
        <ElTableColumn label="电力(kWh)" prop="electric" min-width="120" />
        <ElTableColumn label="水量(m³)" prop="water" min-width="120" />
        <ElTableColumn label="燃气(m³)" prop="gas" min-width="120" />
        <ElTableColumn v-if="common.compareYoY" label="同比变化(%)" min-width="120">
          <template #default="{ row }">{{ row.yoy?.toFixed(1) }}</template>
        </ElTableColumn>
        <ElTableColumn v-if="common.compareMoM" label="环比变化(%)" min-width="120">
          <template #default="{ row }">{{ row.mom?.toFixed(1) }}</template>
        </ElTableColumn>
      </ElTable>

      <ElTable v-else :data="costRows" stripe>
        <ElTableColumn label="时间" prop="time" width="140" />
        <ElTableColumn label="区域" prop="region" width="140" />
        <ElTableColumn label="电费(元)" prop="electric" min-width="120" />
        <ElTableColumn label="水费(元)" prop="water" min-width="120" />
        <ElTableColumn label="燃气费(元)" prop="gas" min-width="120" />
        <ElTableColumn v-if="common.compareYoY" label="同比变化(%)" min-width="120">
          <template #default="{ row }">{{ row.yoy?.toFixed(1) }}</template>
        </ElTableColumn>
        <ElTableColumn v-if="common.compareMoM" label="环比变化(%)" min-width="120">
          <template #default="{ row }">{{ row.mom?.toFixed(1) }}</template>
        </ElTableColumn>
      </ElTable>
    </ElCard>
  </EnergyPageContainer>
</template>

<style scoped lang="scss">
.report-tabs {
  background: transparent;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.compare-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.type-checkboxes :deep(.el-form-item__content) {
  min-width: 0;
}

.type-checkboxes :deep(.el-checkbox-group) {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 8px;
  white-space: nowrap;
}

.type-checkboxes :deep(.el-checkbox) {
  margin-right: 0;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.result-title {
  font-weight: 900;
  color: var(--el-text-color-primary);
}

.result-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.result-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.stat-card {
  padding: 12px 12px;
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: 800;
}

.stat-value {
  margin-top: 6px;
  font-size: 18px;
  font-weight: 900;
  color: var(--el-text-color-primary);
}

.stat-unit {
  margin-top: 2px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 1400px) {
  .filter-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .filter-grid {
    grid-template-columns: 1fr;
  }
  .result-stats {
    grid-template-columns: 1fr;
  }
}
</style>
